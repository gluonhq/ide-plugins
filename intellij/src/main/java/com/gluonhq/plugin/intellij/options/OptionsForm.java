/*
 * Copyright (c) 2018, 2021, Gluon Software
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
package com.gluonhq.plugin.intellij.options;

import com.gluonhq.plugin.intellij.module.OptInHelper;
import com.gluonhq.plugin.templates.ProjectConstants;
import com.gluonhq.plugin.templates.TemplateUtils;
import com.intellij.ide.util.PropertiesComponent;

import javax.swing.*;
import java.util.prefs.Preferences;

public class OptionsForm {
    private JTextField jTextEmailAddress;
    private JCheckBox jCheckUpToDate;
    private JPanel contentPanel;
    private JLabel errorLabel;
    private JTextField jTextGluonLicense;

    private final static Preferences PREFERENCES = Preferences.userRoot().node("com.gluonhq.plugin.intellij");
    private final static PropertiesComponent LEGACY_PREFERENCES = PropertiesComponent.getInstance();

    private String iniEmailAddress;
    private boolean iniCheck;
    private String iniGluonLicenseKey;

    public OptionsForm() {
        errorLabel.setVisible(false);
        init();
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }

    public boolean isModified() {
        errorLabel.setVisible(!isValid());
        return (!jTextEmailAddress.getText().equals(iniEmailAddress) && isValid()) ||
                jCheckUpToDate.isSelected() != iniCheck ||
                !jTextGluonLicense.getText().equals(iniGluonLicenseKey);
    }

    public boolean isValid() {
        return TemplateUtils.isValidEmail(jTextEmailAddress.getText());
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    public void apply() {
        boolean changed = false;
        if (isModified() ||
                PREFERENCES.get(ProjectConstants.PARAM_USER_MAC_ADDRESS, "").isEmpty() ||
                PREFERENCES.get(ProjectConstants.PARAM_USER_PLUGIN_VERSION, "").isEmpty()) {
            changed = true;
        }

        // store modified settings
        OptInHelper.persistOptIn(jTextEmailAddress.getText(),
                jCheckUpToDate.isSelected(),
                jTextGluonLicense.getText());
    
        if (changed) {
            // send modified settings
            com.gluonhq.plugin.templates.OptInHelper.optIn(jTextEmailAddress.getText(),
                    jCheckUpToDate.isSelected(), "pluginidea",
                    PREFERENCES.get(ProjectConstants.PARAM_USER_MAC_ADDRESS, ""),
                    PREFERENCES.get(ProjectConstants.PARAM_USER_PLUGIN_VERSION, ""), true);
        }

        init();
    }

    public void init() {

        if ("true".equals(LEGACY_PREFERENCES.getValue(ProjectConstants.PARAM_USER_IDE_OPTIN, ""))) {
            OptInHelper.persistOptIn(LEGACY_PREFERENCES.getValue(ProjectConstants.PARAM_USER_EMAIL, ""),
                    LEGACY_PREFERENCES.getBoolean(ProjectConstants.PARAM_USER_UPTODATE),
                    LEGACY_PREFERENCES.getValue(ProjectConstants.PARAM_USER_LICENSE, ""));

            LEGACY_PREFERENCES.unsetValue(ProjectConstants.PARAM_USER_IDE_OPTIN);
            LEGACY_PREFERENCES.unsetValue(ProjectConstants.PARAM_USER_EMAIL);
            LEGACY_PREFERENCES.unsetValue(ProjectConstants.PARAM_USER_UPTODATE);
            LEGACY_PREFERENCES.unsetValue(ProjectConstants.PARAM_USER_LICENSE);
            LEGACY_PREFERENCES.unsetValue(ProjectConstants.PARAM_USER_MAC_ADDRESS);
            LEGACY_PREFERENCES.unsetValue(ProjectConstants.PARAM_USER_PLUGIN_VERSION);
        }

        iniEmailAddress = PREFERENCES.get(ProjectConstants.PARAM_USER_EMAIL, "");
        iniCheck = PREFERENCES.getBoolean(ProjectConstants.PARAM_USER_UPTODATE, true);
        iniGluonLicenseKey = PREFERENCES.get(ProjectConstants.PARAM_USER_LICENSE, "");

        jTextEmailAddress.setText(iniEmailAddress);
        jCheckUpToDate.setSelected(iniCheck);
        jTextGluonLicense.setText(iniGluonLicenseKey);
    }
}
