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

import com.gluonhq.plugin.intellij.util.GluonBundle;
import com.gluonhq.plugin.intellij.util.Utils;
import com.gluonhq.plugin.templates.ProjectConstants;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

public class GluonDesktopPropertiesEditor implements ChangeListener, DocumentListener {

    private JTextField packageNameField;
    private JTextField mainClassNameField;
    private JTextField mainClassField;
    private JPanel contentPanel;
    private JLabel errorLabel;

    private ModulesProvider modulesProvider;

    public GluonDesktopPropertiesEditor(String moduleName, ModulesProvider modulesProvider) {
        this.modulesProvider = modulesProvider;

        mainClassNameField.getDocument().addDocumentListener(this);
        packageNameField.getDocument().addDocumentListener(this);

        String defaultAppName = moduleName != null ? moduleName : ProjectConstants.DEFAULT_PROJECT_NAME;
        mainClassNameField.setText(defaultAppName);
        mainClassNameField.selectAll();
        packageNameField.setText(Utils.getDefaultPackageNameByModuleName(defaultAppName.toLowerCase()));
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }

    public String getMainClassName() {
        return mainClassNameField.getText().trim();
    }

    public String getPackageName() {
        return packageNameField.getText().trim();
    }

    public String getMainClass() {
        return mainClassField.getText().trim();
    }

    public JTextField getMainClassNameField() {
        return mainClassNameField;
    }

    public JTextField getPackageNameField() {
        return packageNameField;
    }

    private void updateTexts(DocumentEvent e) {
        Document doc = e.getDocument();
        if (doc == packageNameField.getDocument() || doc == mainClassNameField.getDocument()) {
            String packageName = packageNameField.getText();
            String mainClassName = mainClassNameField.getText();

            if (packageName.isEmpty()) {
                mainClassField.setText(mainClassName);
            } else if (mainClassName.isEmpty()) {
                mainClassField.setText(packageName);
            } else {
                mainClassField.setText(packageName + '.' + mainClassName);
            }
        }

        validate();
    }

    private void validate() {
        String errorMessage = doValidate();
        errorLabel.setText(errorMessage);
    }

    private String doValidate() {
        String packageName = packageNameField.getText();
        if (packageName.length() == 0) {
            return GluonBundle.message("provide.valid.package.name.error");
        }

        if (!Utils.isValidPackageName(packageName)) {
            return GluonBundle.message("package.name.is.invalid.error");
        }

        String mainClassName = mainClassNameField.getText();
        if (mainClassName.length() == 0) {
            return GluonBundle.message("provide.valid.class.name.error");
        }
        if (!Utils.isJavaIdentifier(mainClassName)) {
            return GluonBundle.message("class.name.is.invalid.error");
        }

        return "";
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        validate();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        updateTexts(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        updateTexts(e);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        updateTexts(e);
    }

    public boolean isValid() {
        return errorLabel.getText().isEmpty();
    }

}
