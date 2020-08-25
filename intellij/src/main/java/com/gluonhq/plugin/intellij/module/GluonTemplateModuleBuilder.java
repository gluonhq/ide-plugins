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
import com.intellij.ide.util.projectWizard.JavaModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
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
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.projectImport.ProjectImportProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.model.MavenConstants;
import org.jetbrains.plugins.gradle.util.GradleConstants;

import javax.swing.*;
import java.io.File;
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
                template.getProjectName().equals(GluonProject.DESKTOP_MULTIVIEWFXML.getType())) {
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
    public void setupRootModel(@NotNull ModifiableRootModel rootModel) throws ConfigurationException {
        if (myJdk != null) {
            rootModel.setSdk(myJdk);
        } else {
            rootModel.inheritSdk();
        }

        final Project project = rootModel.getProject();
        ApplicationManager.getApplication().runWriteAction(() -> createProject(project));
        StartupManager.getInstance(project).runWhenProjectIsInitialized((DumbAwareRunnable)() -> DumbService.getInstance(project).smartInvokeLater(() -> {
            importProject(project);
            openProjectFiles(project);
        }));
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

        parameters.put(ProjectConstants.PARAM_IDE, ProjectConstants.IDE_INTELLIJ);
        parameters.put(ProjectConstants.PARAM_PROJECT_NAME, project.getName());
        parameters.put(ProjectConstants.PARAM_JAVAFX_VERSION, ProjectConstants.getJavaFXVersion());
        parameters.put(ProjectConstants.PARAM_JAVAFX_MAVEN_PLUGIN, ProjectConstants.getJavaFXMavenPluginVersion());
        parameters.put(ProjectConstants.PARAM_JAVAFX_GRADLE_PLUGIN, ProjectConstants.getJavaFXGradlePluginVersion());
        parameters.put(ProjectConstants.PARAM_GLUON_DESKTOP_VERSION, ProjectConstants.getDesktopVersion());
        parameters.put(ProjectConstants.PARAM_GLUON_MOBILE_VERSION, ProjectConstants.getMobileVersion());
        parameters.put(ProjectConstants.PARAM_GLUON_ATTACH_VERSION, ProjectConstants.getAttachVersion());
        parameters.put(ProjectConstants.PARAM_GLUON_CLIENT_MAVEN_PLUGIN, ProjectConstants.getClientMavenPluginVersion());
        parameters.put(ProjectConstants.PARAM_GLUON_CLIENT_GRADLE_PLUGIN, ProjectConstants.getClientGradlePluginVersion());
        parameters.put(ProjectConstants.PARAM_GLUON_GLISTEN_AFTERBURNER_VERSION, ProjectConstants.getGlistenAfterburnerVersion());

        final File projectRoot = new File(project.getBasePath());
        WriteCommandAction.runWriteCommandAction(project, () -> template.render(projectRoot, parameters));
    }

    private void openProjectFiles(Project project) {
        final File projectRoot = new File(project.getBasePath());
        List<File> filesToOpen = new ArrayList<>(template.getFilesToOpen());

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

    private void importProject(Project project) {
        File baseDir = VfsUtilCore.virtualToIoFile(project.getBaseDir());
        String baseFile = null;
        if (parameters.get(ProjectConstants.PARAM_BUILD_TOOL).equals("maven")) {
            final File[] files = baseDir.listFiles((dir, name) -> FileUtil.namesEqual(MavenConstants.POM_XML, name));
            if (files != null && files.length != 0) {
                baseFile = files[0].getPath();
            }
        } else if (parameters.get(ProjectConstants.PARAM_BUILD_TOOL).equals("gradle")) {
            final File[] files = baseDir.listFiles((dir, name) -> FileUtil.namesEqual(GradleConstants.DEFAULT_SCRIPT_NAME, name));
            if (files != null && files.length != 0) {
                baseFile = files[0].getPath();
            }
        }
        if (baseFile != null) {
            VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(baseFile);
            final List<ProjectImportProvider> providers = ImportModuleAction.getProviders(project);
            final ProjectImportProvider[] providersArray = providers.toArray(new ProjectImportProvider[0]);
            final AddModuleWizard importWizard = ImportModuleAction.createImportWizard(project, null, virtualFile, providersArray);
            ImportModuleAction.createFromWizard(project, importWizard);
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
