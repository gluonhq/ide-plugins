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
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class GluonDesktopWizardStep extends ModuleWizardStep {

    private final GluonDesktopPropertiesEditor desktopPropertiesEditor;
    private final GluonTemplateModuleBuilder moduleBuilder;

    private JPanel panel;
    private boolean initialized;

    public GluonDesktopWizardStep(@NotNull GluonTemplateModuleBuilder moduleBuilder,
                                  ModulesProvider modulesProvider) {
        super();

        this.moduleBuilder = moduleBuilder;

        desktopPropertiesEditor = new GluonDesktopPropertiesEditor(moduleBuilder.getName(), modulesProvider);

        panel = new JPanel();
        panel.setLayout(new OverlayLayout(panel));
        panel.add(desktopPropertiesEditor.getContentPanel());
    }

    @Override
    public void updateStep() {
        if (!initialized) {
            initialized = true;
            initialize();
        }
    }

    @Override
    public boolean validate() throws ConfigurationException {
        return desktopPropertiesEditor.isValid();
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return desktopPropertiesEditor.getPackageNameField();
    }

    private void initialize() {
        final String moduleName = moduleBuilder.getName();

        if (moduleName != null) {
            desktopPropertiesEditor.getMainClassNameField().setText(moduleName);
            desktopPropertiesEditor.getPackageNameField().setText(ProjectConstants.DEFAULT_PACKAGE_NAME + "." + moduleName);
        }
    }

    @Override
    public void updateDataModel() {
        moduleBuilder.updateParameter(ProjectConstants.PARAM_PACKAGE_NAME, desktopPropertiesEditor.getPackageName());
        moduleBuilder.updateParameter(ProjectConstants.PARAM_PACKAGE_FOLDER, desktopPropertiesEditor.getPackageName().replaceAll("\\.", "/"));
        moduleBuilder.updateParameter(ProjectConstants.PARAM_MAIN_CLASS, desktopPropertiesEditor.getMainClass());
        moduleBuilder.updateParameter(ProjectConstants.PARAM_MAIN_CLASS_NAME, desktopPropertiesEditor.getMainClassName());
    }

}
