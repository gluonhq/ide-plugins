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
package com.gluonhq.plugin.intellij.module;

import com.gluonhq.plugin.intellij.util.GluonConstants;
import com.gluonhq.plugin.intellij.util.GluonIcons;
import com.gluonhq.plugin.templates.GluonProject;
import com.gluonhq.plugin.templates.ProjectConstants;
import com.gluonhq.plugin.templates.Template;
import com.gluonhq.plugin.templates.TemplateManager;
import com.intellij.ide.actions.ImportModuleAction;
import com.intellij.ide.impl.ProjectPaneSelectInTarget;
import com.intellij.ide.util.newProjectWizard.AddModuleWizard;
import com.intellij.ide.util.projectWizard.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.externalSystem.service.project.manage.ProjectDataManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.DumbAwareRunnable;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectType;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.gradle.service.project.wizard.GradleProjectImportBuilder;
import org.jetbrains.plugins.gradle.service.project.wizard.GradleProjectImportProvider;
import org.jetbrains.plugins.gradle.util.GradleConstants;

import javax.swing.*;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GluonTemplateModuleBuilder extends JavaModuleBuilder {

    private Template template;
    private String builderId;
    private Icon icon;

    private Map<String, Object> parameters = new HashMap<>();

    public GluonTemplateModuleBuilder(Template projectTemplate) {
        this.template = projectTemplate;
        this.builderId = this.template == null || this.template.getMetadata() == null ? null : this.template.getMetadata().getTitle();
    }

    @Override
    @NotNull
    public ModuleWizardStep[] createWizardSteps(@NotNull WizardContext wizardContext, @NotNull ModulesProvider modulesProvider) {
        List<ModuleWizardStep> steps = new ArrayList<>();
        if (!OptInHelper.alreadyOptedIn()) {
            steps.add(new GluonOptInWizardStep(this, modulesProvider));
        } else {
            OptInHelper.restoreOptIn(this);
        }

        if (template.getProjectName().equals(GluonProject.DESKTOP_SINGLE.getType()) ||
                template.getProjectName().equals(GluonProject.DESKTOP_MULTIVIEW.getType()) ||
                template.getProjectName().equals(GluonProject.DESKTOP_MULTIVIEWFXML.getType())){
            icon = GluonIcons.GLUON_DESKTOP;
            steps.add(new GluonDesktopWizardStep(this, modulesProvider));
            if (template.getProjectName().equals(GluonProject.DESKTOP_MULTIVIEW.getType()) ||
                    template.getProjectName().equals(GluonProject.DESKTOP_MULTIVIEWFXML.getType())) {
                steps.add(new GluonViewWizardStep(this, modulesProvider, false));
            }
        } else {
            icon = GluonIcons.GLUON_MOBILE;
            steps.add(new GluonModuleWizardStep(this, modulesProvider));
            if (template.getProjectName().equals(GluonProject.MOBILE_MULTIVIEW.getType()) ||
                    template.getProjectName().equals(GluonProject.MOBILE_MULTIVIEWFXML.getType()) ||
                    template.getProjectName().equals(GluonProject.MOBILE_MULTIVIEW_GAF.getType())) {
                steps.add(new GluonViewWizardStep(this, modulesProvider,
                        template.getProjectName().equals(GluonProject.MOBILE_MULTIVIEWFXML.getType()),
                        template.getProjectName().equals(GluonProject.MOBILE_MULTIVIEW_GAF.getType())));
            }
        }
        steps.add(new ChooseJavaSdkStep(wizardContext, this));
        return steps.toArray(new ModuleWizardStep[steps.size()]);
    }

    @Nullable
    @Override
    public ModuleWizardStep modifySettingsStep(@NotNull SettingsStep settingsStep) {
        if (template != null && template.getMetadata() != null) {
            String applicationName = template.getMetadata().getTitle();
            if (!applicationName.isEmpty() && settingsStep.getModuleNameField() != null) {
                settingsStep.getModuleNameField().setText(applicationName.replace(" ", ""));
            }
        }
        return null;
    }

    @Override
    public void setupRootModel(ModifiableRootModel rootModel) throws ConfigurationException {
        if (myJdk != null) {
            rootModel.setSdk(myJdk);
        } else {
            rootModel.inheritSdk();
        }

        final Project project = rootModel.getProject();
        StartupManager.getInstance(project).runWhenProjectIsInitialized(new DumbAwareRunnable() {
            @Override
            public void run() {
                DumbService.getInstance(project).smartInvokeLater(new Runnable() {
                    @Override
                    public void run() {
                        ApplicationManager.getApplication().runWriteAction(new Runnable() {
                            @Override
                            public void run() {
                                createProject(project);
                            }
                        });
                    }
                });
            }
        });
        StartupManager.getInstance(project).registerPostStartupActivity(new DumbAwareRunnable() {
            @Override
            public void run() {
                DumbService.getInstance(project).smartInvokeLater(new Runnable() {
                    @Override
                    public void run() {
                        linkGradleProject(project);
                    }
                });
            }
        });
    }

    public void updateParameter(String key, Object value) {
        parameters.put(key, value);
    }

    private void createProject(final Project project) {

        if (!OptInHelper.alreadyOptedIn()) {
            OptInHelper.persistOptIn((String) parameters.get(ProjectConstants.PARAM_USER_EMAIL),
                    Boolean.parseBoolean((String) parameters.get(ProjectConstants.PARAM_USER_UPTODATE)),
                    (String) parameters.get(ProjectConstants.PARAM_USER_LICENSE_MOBILE),
                    (String) parameters.get(ProjectConstants.PARAM_USER_LICENSE_DESKTOP));

            // only send once
            com.gluonhq.plugin.templates.OptInHelper.optIn((String) parameters.get(ProjectConstants.PARAM_USER_EMAIL),
                    Boolean.parseBoolean((String) parameters.get(ProjectConstants.PARAM_USER_UPTODATE)), "pluginidea",
                    (String) parameters.get(ProjectConstants.PARAM_USER_MAC_ADDRESS),
                    (String) parameters.get(ProjectConstants.PARAM_USER_PLUGIN_VERSION),
                    false);
        }

        parameters.put(ProjectConstants.PARAM_PROJECT_NAME, project.getName());
        parameters.put(ProjectConstants.PARAM_GLUON_DESKTOP_VERSION, ProjectConstants.getDesktopVersion());
        parameters.put(ProjectConstants.PARAM_GLUON_MOBILE_VERSION, ProjectConstants.getMobileVersion());
        parameters.put(ProjectConstants.PARAM_GLUON_MOBILE_GVM_VERSION, ProjectConstants.getMobileGvmVersion());
        parameters.put(ProjectConstants.PARAM_GLUON_DOWN_VERSION, ProjectConstants.getDownVersion());
        parameters.put(ProjectConstants.PARAM_GLUON_MOBILE_PLUGIN, ProjectConstants.getPluginVersion());
        parameters.put(ProjectConstants.PARAM_GLUON_MOBILE_GVM_PLUGIN, ProjectConstants.getPluginGvmVersion());
        parameters.put(ProjectConstants.PARAM_GLUON_GLISTEN_AFTERBURNER_VERSION, ProjectConstants.getGlistenAfterburnerVersion());

        final File projectRoot = new File(project.getBasePath());
        WriteCommandAction.runWriteCommandAction(project, new Runnable() {
            @Override
            public void run() {
                List<File> filesToOpen = new ArrayList<>();

                template.render(projectRoot, parameters);
                filesToOpen.addAll(template.getFilesToOpen());

                // create default source
                Template sourceTemplate = TemplateManager.getInstance().getSourceTemplate(template.getProjectName());
                if (sourceTemplate != null) {
                    sourceTemplate.render(projectRoot, parameters);
                    filesToOpen.addAll(sourceTemplate.getFilesToOpen());
                }

                if (!filesToOpen.isEmpty()) {
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
            }
        });
    }

    private void linkGradleProject(Project project) {
        File baseDir = VfsUtilCore.virtualToIoFile(project.getBaseDir());
        final File[] files = baseDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return FileUtil.namesEqual(GradleConstants.DEFAULT_SCRIPT_NAME, name);
            }
        });

        if (files != null && files.length != 0) {
            GradleProjectImportBuilder gradleProjectImportBuilder = new GradleProjectImportBuilder(ProjectDataManager.getInstance());
            final GradleProjectImportProvider gradleProjectImportProvider = new GradleProjectImportProvider(gradleProjectImportBuilder);
            AddModuleWizard wizard = new AddModuleWizard(project, files[0].getPath(), gradleProjectImportProvider);
            if ((wizard.getStepCount() <= 0 || wizard.showAndGet())) {
                ImportModuleAction.createFromWizard(project, wizard);
            }
        }
    }

    @Override
    protected ProjectType getProjectType() {
        return GluonConstants.PROJECT_TYPE;
    }

    @Override
    public Icon getNodeIcon() {
        return icon;
    }

    @Nullable
    @Override
    public String getBuilderId() {
        return builderId;
    }

}
