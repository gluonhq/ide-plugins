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

import com.gluonhq.plugin.templates.TemplateUtils;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.Locale;

public class GluonViewPropertiesEditor implements DocumentListener {
    private JPanel contentPanel;
    private JLabel errorLabel;
    private JTextField primaryViewNameField;
    private JTextField secondaryViewNameField;
    private JCheckBox projectCheckBox;
    private JCheckBox primaryViewCheckBox;
    private JCheckBox secondaryViewCheckBox;
    private JCheckBox afterburnerCheckBox;
    private JLabel fxmlLabel;

    private final ModulesProvider modulesProvider;

    public GluonViewPropertiesEditor(String moduleName, ModulesProvider modulesProvider, boolean useFXML, boolean useGAf) {
        this.modulesProvider = modulesProvider;

        primaryViewNameField.setText("Primary");
        secondaryViewNameField.setText("Secondary");
        projectCheckBox.setSelected(true);
        primaryViewCheckBox.setSelected(true);
        secondaryViewCheckBox.setSelected(true);

        primaryViewNameField.selectAll();
        primaryViewNameField.getDocument().addDocumentListener(this);
        secondaryViewNameField.getDocument().addDocumentListener(this);

        afterburnerCheckBox.setVisible(useFXML && !useGAf);
        fxmlLabel.setVisible(useFXML && !useGAf);

        primaryViewCheckBox.setEnabled(!useGAf);
        secondaryViewCheckBox.setEnabled(!useGAf);
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }

    public JLabel getErrorLabel() {
        return errorLabel;
    }

    public JTextField getPrimaryViewNameField() {
        return primaryViewNameField;
    }

    public String getPrimaryViewName() {
        final String primary = TemplateUtils.getCorrectNameView(primaryViewNameField.getText().trim(), "Primary");
        return TemplateUtils.upperCaseWord(primary);
    }

    public String getSecondaryViewName() {
        final String secondary = TemplateUtils.getCorrectNameView(secondaryViewNameField.getText().trim(), "Secondary");
        return TemplateUtils.upperCaseWord(secondary);
    }

    public String getPrimaryViewCSS() {
        final String primary = TemplateUtils.getCorrectNameView(primaryViewNameField.getText().trim(), "Primary");
        return primary.toLowerCase(Locale.ROOT);
    }

    public String getSecondaryViewCSS() {
        final String secondary = TemplateUtils.getCorrectNameView(secondaryViewNameField.getText().trim(), "Secondary");
        return secondary.toLowerCase(Locale.ROOT);
    }

    public boolean isProjectSelected() {
        return projectCheckBox.isSelected();
    }

    public boolean isHomeViewSelected() {
        return primaryViewCheckBox.isSelected();
    }

    public boolean isSecondaryViewSelected() {
        return secondaryViewCheckBox.isSelected();
    }

    public boolean isAfterburnerSelected() { return afterburnerCheckBox.isSelected(); }

    public ModulesProvider getModulesProvider() {
        return modulesProvider;
    }

    private void validate() {
        String errorMessage = doValidate();
        errorLabel.setText(errorMessage);
    }

    private String doValidate() {
        if (!TemplateUtils.isValidNameView(primaryViewNameField.getText().trim())) {
            return primaryViewNameField.getText() + " is not a valid view name for the Primary View Name.";
        }

        if (!TemplateUtils.isValidNameView(secondaryViewNameField.getText().trim())) {
            return secondaryViewNameField.getText() + " is not a valid view name for the Secondary View Name.";
        }

        return "";
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        validate();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        validate();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        validate();
    }
    
    public boolean isValid() {
        return errorLabel.getText().isEmpty();
    }

}
