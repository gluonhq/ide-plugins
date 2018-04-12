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

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.roots.ui.configuration.JdkComboBox;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel;
import com.intellij.openapi.util.Condition;

import javax.swing.*;

public class ChooseJavaSdkStep extends ModuleWizardStep {

    private JdkComboBox myJdkComboBox;
    private JButton myNewButton;
    private JPanel myPanel;
    private WizardContext myWizardContext;
    private ModuleBuilder myModuleBuilder;
    private ProjectSdksModel mySdksModel;

    public ChooseJavaSdkStep(WizardContext myWizardContext, ModuleBuilder myModuleBuilder) {
        this.myWizardContext = myWizardContext;
        this.myModuleBuilder = myModuleBuilder;

        myJdkComboBox.setSetupButton(myNewButton, null, mySdksModel, new JdkComboBox.NoneJdkComboBoxItem(), null, false);
    }

    @Override
    public JComponent getComponent() {
        return myPanel;
    }

    @Override
    public void updateDataModel() {
        Project project = myWizardContext.getProject();
        if (project == null) {
            myWizardContext.setProjectJdk(myJdkComboBox.getSelectedJdk());
        } else {
            myModuleBuilder.setModuleJdk(myJdkComboBox.getSelectedJdk());
        }
    }

    private void createUIComponents() {
        mySdksModel = new ProjectSdksModel();
        mySdksModel.reset(null);
        myJdkComboBox = new JdkComboBox(mySdksModel, new Condition<SdkTypeId>() {
            @Override
            public boolean value(SdkTypeId id) {
                return JavaSdk.getInstance().equals(id);
            }
        });
    }
}
