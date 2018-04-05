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

public class OptInStep  implements ChangeListener, DocumentListener {
    private JTextField emailAddress;
    private JCheckBox keepMeUpToCheckBox;
    private JPanel contentPanel;
    private JLabel emailAddressLabel;
    private JLabel label1;
    private JLabel errorLabel;
    private JTextField mobileLicenseTextField;
    private JTextField desktopLicenseTextField;
    private ModulesProvider modulesProvider;

    public OptInStep(String moduleName, ModulesProvider modulesProvider) {
        this.modulesProvider = modulesProvider;
        errorLabel.setVisible(false);
        emailAddress.getDocument().addDocumentListener(this);
        isValid();
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }

    public JTextField getEmailAddress() {
        return emailAddress;
    }

    public JTextField getMobileLicense() { return mobileLicenseTextField; }

    public JTextField getDesktopLicense() { return desktopLicenseTextField; }

    public boolean getSubscribe() {
        return keepMeUpToCheckBox.isSelected();
    }

    @Override
    public void stateChanged(ChangeEvent e) {

    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        isValid();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        isValid();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        isValid();
    }

    public boolean isValid() {

        boolean valid = TemplateUtils.isValidEmail(emailAddress.getText());
        errorLabel.setVisible(!valid);
        return valid;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
