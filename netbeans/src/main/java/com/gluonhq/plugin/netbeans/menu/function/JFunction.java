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
package com.gluonhq.plugin.netbeans.menu.function;

import com.gluonhq.plugin.function.Function;
import com.gluonhq.plugin.function.FunctionFX;
import com.gluonhq.plugin.netbeans.menu.ProjectUtils;
import com.gluonhq.plugin.templates.GluonProject;
import com.gluonhq.plugin.templates.ProjectConstants;
import com.gluonhq.plugin.templates.Template;
import com.gluonhq.plugin.templates.TemplateManager;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

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
    private List<String> lines = null;
    private RequestProcessor.Task task;

    public JFunction(ProjectUtils utils) {
        this.utils = utils;
        
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
                    task = new RequestProcessor("FunctionRP").create(() -> createSubProject(function));
                    task.schedule(0);
                }
                dispose();
            });

            final Scene scene = new Scene(functionFX);
            fxPanel.setScene(scene);
        });
        return fxPanel;
    }
    
    private void createSubProject(Function function) {
        File rootFile = ProjectUtils.getProjectFile(utils.getRootProject());
        ProgressHandle handle = ProgressHandleFactory.createHandle("Gluon Function task", () -> {
            task.cancel();
            return true;
        });
        handle.setInitialDelay(200);
        handle.start(100);
        handle.progress(10);
        
        // Run Template
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(ProjectConstants.PARAM_GLUON_FUNCTION_NAME, function.getFunctionName());
        parameters.put(ProjectConstants.PARAM_GLUON_FUNCTION_METHOD_NAME, function.getMethodName());
        parameters.put(ProjectConstants.PARAM_PACKAGE_NAME, function.getPackageName());
        parameters.put(ProjectConstants.PARAM_PACKAGE_FOLDER, function.getPackageName().replaceAll("\\.", "/"));
        final String fnProjectName = rootFile.getName() + function.getFunctionName();
        parameters.put(ProjectConstants.PARAM_GLUON_FUNCTION_PROJECT_NAME, fnProjectName);
    
        handle.progress(15);
        List<File> filesToOpen = new ArrayList<>();
        TemplateManager templateManager = TemplateManager.getInstance();
        Template template = templateManager.getProjectTemplate(GluonProject.FUNCTION.getType());

        template.render(rootFile, parameters);
        filesToOpen.addAll(template.getFilesToOpen());
        handle.progress(30);
        
        File parent = rootFile.getParentFile();
        if (parent != null && parent.exists()) {
            ProjectChooser.setProjectsFolder(parent);
        }

        // create template sources
        Template sourceTemplate = templateManager.getSourceTemplate(template.getProjectName());
        if (sourceTemplate != null) {
            sourceTemplate.render(rootFile, parameters);
            filesToOpen.addAll(sourceTemplate.getFilesToOpen());
        }
        handle.progress(45);
        
        // Add include to settings.build

        final FileObject settingsFile = ProjectUtils.getGradleSettingsFile(utils.getRootProject());
        try {
            lines = settingsFile.asLines();
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
            
            settingsFile.refresh();
            handle.progress(55);

            ProjectUtils.refreshProject(utils.getRootProject());
        }
        
        handle.progress(65);
        
        // open Gluon Fn subProject
        File projectToBeOpenedFile = new File(rootFile, fnProjectName); 
        FileObject projectToBeOpened = FileUtil.toFileObject(projectToBeOpenedFile);
        try {
            Project fnProject = ProjectManager.getDefault().findProject(projectToBeOpened);
            OpenProjects.getDefault().open(new Project[] { fnProject }, false);
            // select function class
        
            handle.progress(75);

            if (!filesToOpen.isEmpty()) {
                filesToOpen.stream()
                    .filter(File::exists)
                    .forEach(file -> {
                        try {
                            DataObject.find(FileUtil.toFileObject(FileUtil.normalizeFile(file)))
                                    .getLookup().lookup(OpenCookie.class).open();
                        } catch (DataObjectNotFoundException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    });
            }

            ProjectUtils.refreshProject(utils.getRootProject());

            handle.progress(85);

            // build and upload gluon function
            // ExecuteUploadFunction executeUpload = new ExecuteUploadFunction(fnProject);
            // executeUpload.execute();

            // TODO: Expand function project

        } catch (IOException ex) {}
        
        handle.progress(100);
        
        handle.finish();
    }
}
