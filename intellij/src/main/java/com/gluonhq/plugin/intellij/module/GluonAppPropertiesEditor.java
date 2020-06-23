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
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

public class GluonAppPropertiesEditor implements ChangeListener, DocumentListener {

    private JTextField packageNameField;
    private JPanel contentPanel;
    private JTextField mainClassNameField;
    private JTextField mainClassField;
    private JCheckBox androidCheckBox;
    private JCheckBox iOSCheckBox;
    private JCheckBox desktopCheckBox;
    private JCheckBox embeddedCheckBox;
    private JLabel errorLabel;
    private JRadioButton mavenRadioButton;
    private JRadioButton gradleRadioButton;
    private ButtonGroup buildTool;

    private ModulesProvider modulesProvider;

    public GluonAppPropertiesEditor(String moduleName, ModulesProvider modulesProvider) {
        this.modulesProvider = modulesProvider;

        mainClassNameField.getDocument().addDocumentListener(this);
        packageNameField.getDocument().addDocumentListener(this);
        androidCheckBox.addChangeListener(this);

        String defaultAppName = moduleName != null ? moduleName : ProjectConstants.DEFAULT_PROJECT_NAME;
        mainClassNameField.setText(defaultAppName);
        mainClassNameField.selectAll();
        packageNameField.setText(Utils.getDefaultPackageNameByModuleName(defaultAppName.toLowerCase()));

        mavenRadioButton.setActionCommand("maven");
        gradleRadioButton.setActionCommand("gradle");
        buildTool = new ButtonGroup();
        buildTool.add(mavenRadioButton);
        buildTool.add(gradleRadioButton);
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

    public boolean isAndroidSelected() {
        return androidCheckBox.isSelected();
    }

    public boolean isIosSelected() {
        return iOSCheckBox.isSelected();
    }

    public boolean isDesktopSelected() {
        return desktopCheckBox.isSelected();
    }

    public boolean isEmbeddedSelected() {
        return embeddedCheckBox.isSelected();
    }

    public String getBuildTool() {
        return buildTool.getSelection().getActionCommand();
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

        if (androidCheckBox.isSelected()) {
            String errorMessage = isValidAndroidPackageName(packageName);
            if (errorMessage != null) {
                return errorMessage;
            }
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

    private String isValidAndroidPackageName(String candidate) {
        String packageManagerCheck = validateName(candidate, true);
        if (packageManagerCheck != null) {
            return packageManagerCheck;
        }

        return null;
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

    @Nullable
    private static String validateName(String name, boolean requiresSeparator) {
        final int N = name.length();
        boolean hasSep = false;
        boolean front = true;
        for (int i=0; i<N; i++) {
            final char c = name.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                front = false;
                continue;
            }
            if ((c >= '0' && c <= '9') || c == '_') {
                if (!front) {
                    continue;
                } else {
                    if (c == '_') {
                        return "The character '_' cannot be the first character in a package segment";
                    } else {
                        return "A digit cannot be the first character in a package segment";
                    }
                }
            }
            if (c == '.') {
                hasSep = true;
                front = true;
                continue;
            }
            return "The character '" + c + "' is not allowed in Android application package names";
        }

        if (!hasSep && requiresSeparator) {
            return "The package must have at least one '.' separator";
        }

        if (hasSep && requiresSeparator && name.startsWith(".")) {
            return "The character '.' cannot be the first character in a package segment";
        }

        if (hasSep && requiresSeparator && name.endsWith(".")) {
            return "The character '.' cannot be the last character in a package segment";
        }

        return null;
    }

    public boolean isValid() {
        return errorLabel.getText().isEmpty();
    }
}
