/*
 * Copyright (c) 2018, Gluon Software
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gluonhq.plugin.intellij.menu.function;

import com.gluonhq.plugin.function.Function;
import com.gluonhq.plugin.function.FunctionFX;
import com.gluonhq.plugin.intellij.menu.ProjectUtils;
import com.gluonhq.plugin.templates.GluonProject;
import com.gluonhq.plugin.templates.ProjectConstants;
import com.gluonhq.plugin.templates.Template;
import com.gluonhq.plugin.templates.TemplateManager;
import com.intellij.ide.impl.ProjectPaneSelectInTarget;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.externalSystem.model.DataNode;
import com.intellij.openapi.externalSystem.model.project.ProjectData;
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode;
import com.intellij.openapi.externalSystem.service.project.ExternalProjectRefreshCallback;
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.gradle.util.GradleConstants;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class JFunction extends JFrame {

    private final ProjectUtils utils;
    private final Project project;
    private List<String> lines = null;

    public JFunction(ProjectUtils utils, Project project) {
        this.utils = utils;
        this.project = project;

        getContentPane().add(runFunctionFX());
    }

    private JFXPanel runFunctionFX() {
        final JFXPanel fxPanel = new JFXPanel();
        Platform.setImplicitExit(false);
        Platform.runLater(() -> {
            FunctionFX functionFX = new FunctionFX();
            Function function = functionFX.getFunction();
            function.addPropertyChangeListener(e -> {
                if (function.getFunctionName() != null && function.getPackageName() != null) {
                    ProjectUtils.runSafe(() -> WriteCommandAction.runWriteCommandAction(project, () -> {
                        ProgressManager.getInstance().runProcessWithProgressSynchronously(() ->
                                createSubProject(function), "Gluon Function", true, project);
                    }));
                }
                dispose();
            });

            final Scene scene = new Scene(functionFX);
            fxPanel.setScene(scene);
        });
        return fxPanel;
    }

    private void createSubProject(Function function) {
        File rootFile = new File(utils.getRootProject().getPath());

        // Run Template

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(ProjectConstants.PARAM_GLUON_FUNCTION_NAME, function.getFunctionName());
        parameters.put(ProjectConstants.PARAM_GLUON_FUNCTION_METHOD_NAME, function.getMethodName());
        parameters.put(ProjectConstants.PARAM_PACKAGE_NAME, function.getPackageName());
        parameters.put(ProjectConstants.PARAM_PACKAGE_FOLDER, function.getPackageName().replaceAll("\\.", "/"));
        final String fnProjectName = rootFile.getName() + function.getFunctionName();
        parameters.put(ProjectConstants.PARAM_GLUON_FUNCTION_PROJECT_NAME, fnProjectName);

        List<File> filesToOpen = new ArrayList<>();
        TemplateManager templateManager = TemplateManager.getInstance();
        Template template = templateManager.getProjectTemplate(GluonProject.FUNCTION.getType());

        template.render(rootFile, parameters);
        filesToOpen.addAll(template.getFilesToOpen());

        // create template sources
        Template sourceTemplate = templateManager.getSourceTemplate(template.getProjectName());
        if (sourceTemplate != null) {
            sourceTemplate.render(rootFile, parameters);
            filesToOpen.addAll(sourceTemplate.getFilesToOpen());
        }

        // Add include to settings.build

        final VirtualFile settingsFile = utils.getGradleSettingsFile(project.getBaseDir());
        try {
            lines = Files.readAllLines(Paths.get(settingsFile.getPath()));
        } catch (IOException ex) {}

        if (lines != null && !lines.isEmpty()) {
            int includeLines = (int) IntStream.range(0, lines.size())
                    .filter(i -> lines.get(i).startsWith("include"))
                    .count();

            List<String> newList = IntStream.range(0, includeLines)
                    .mapToObj(lines::get)
                    .collect(Collectors.toList());
            newList.add("include '" +  fnProjectName + "'");
            newList.addAll(IntStream.range(includeLines + 1, lines.size())
                    .mapToObj(lines::get)
                    .collect(Collectors.toList()));
            try {
                Files.write(Paths.get(settingsFile.getPath()), newList, StandardCharsets.UTF_8);
            } catch (IOException ex) { }

            settingsFile.refresh(true, true);
        }

        // open Gluon Fn subProject
        // select function class

        if (! filesToOpen.isEmpty()) {
            VirtualFile lastVirtualFile = null;
            for (File file : filesToOpen) {
                if (file.exists()) {
                    VirtualFile vFile = VfsUtil.findFileByIoFile(file, true);
                    if (vFile != null) {
                        OpenFileDescriptor descriptor = new OpenFileDescriptor(project, vFile);
                        if (!FileEditorManager.getInstance(project).openEditor(descriptor, true).isEmpty()) {
                            lastVirtualFile = vFile;
                        }
                    }
                }
            }

            if (lastVirtualFile != null) {
                ApplicationManager.getApplication().assertReadAccessAllowed();
                PsiFile psiFile = PsiManager.getInstance(project).findFile(lastVirtualFile);
                if (psiFile != null) {
                    ProjectPaneSelectInTarget selectAction = new ProjectPaneSelectInTarget(project);
                    selectAction.select(psiFile, false);
                }
            }
        }

        utils.refreshProject(project.getBaseDir());

        ExternalSystemUtil.refreshProject(project, GradleConstants.SYSTEM_ID, utils.getRootProject().getPath(),
                new ExternalProjectRefreshCallback() {

                        @Override
                        public void onSuccess(@Nullable DataNode<ProjectData> externalProject) {
                            ApplicationManager.getApplication().runReadAction(() -> {
                                VirtualFile functionProject = project.getBaseDir().findChild(project.getName() + function.getFunctionName());
                                ExecuteUploadFunction executeUpload = new ExecuteUploadFunction(project, functionProject);
                                executeUpload.execute();
                            });
                        }

                        @Override
                        public void onFailure(@NotNull String errorMessage, @Nullable String errorDetails) {
                            System.out.println("Error " + errorMessage + " - " + errorDetails);
                        }
                    },
                false,  ProgressExecutionMode.IN_BACKGROUND_ASYNC);

    }
}
