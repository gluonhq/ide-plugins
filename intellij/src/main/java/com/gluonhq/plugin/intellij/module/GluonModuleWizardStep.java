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

import com.gluonhq.plugin.templates.ProjectConstants;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class GluonModuleWizardStep extends ModuleWizardStep {

    private final GluonAppPropertiesEditor appPropertiesEditor;
    private final GluonTemplateModuleBuilder moduleBuilder;

    private JPanel panel;
    private boolean initialized;

    public GluonModuleWizardStep(@NotNull GluonTemplateModuleBuilder moduleBuilder,
                                 ModulesProvider modulesProvider) {
        super();

        this.moduleBuilder = moduleBuilder;

        appPropertiesEditor = new GluonAppPropertiesEditor(moduleBuilder.getName(), modulesProvider);

        panel = new JPanel();
        panel.setLayout(new OverlayLayout(panel));
        panel.add(appPropertiesEditor.getContentPanel());
    }

    @Override
    public void updateStep() {
        if (!initialized) {
            initialized = true;
            initialize();
        }
    }

    @Override
    public boolean validate() {
        return appPropertiesEditor.isValid();
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return appPropertiesEditor.getPackageNameField();
    }

    private void initialize() {
        final String moduleName = moduleBuilder.getName();

        if (moduleName != null) {
            appPropertiesEditor.getMainClassNameField().setText(moduleName);
            appPropertiesEditor.getPackageNameField().setText(ProjectConstants.DEFAULT_PACKAGE_NAME + "." + moduleName);
        }
    }

    @Override
    public void updateDataModel() {
        moduleBuilder.updateParameter(ProjectConstants.PARAM_PACKAGE_NAME, appPropertiesEditor.getPackageName());
        moduleBuilder.updateParameter(ProjectConstants.PARAM_PACKAGE_FOLDER, appPropertiesEditor.getPackageName().replaceAll("\\.", "/"));
        moduleBuilder.updateParameter(ProjectConstants.PARAM_MAIN_CLASS, appPropertiesEditor.getMainClass());
        moduleBuilder.updateParameter(ProjectConstants.PARAM_MAIN_CLASS_NAME, appPropertiesEditor.getMainClassName());
        moduleBuilder.updateParameter(ProjectConstants.PARAM_ANDROID_ENABLED, appPropertiesEditor.isAndroidSelected());
        moduleBuilder.updateParameter(ProjectConstants.PARAM_IOS_ENABLED, appPropertiesEditor.isIosSelected());
        moduleBuilder.updateParameter(ProjectConstants.PARAM_DESKTOP_ENABLED, appPropertiesEditor.isDesktopSelected());
        moduleBuilder.updateParameter(ProjectConstants.PARAM_EMBEDDED_ENABLED, appPropertiesEditor.isEmbeddedSelected());
        moduleBuilder.updateParameter(ProjectConstants.PARAM_BUILD_TOOL, appPropertiesEditor.getBuildTool());
    }
}
