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
    private JTextField jTextMobileLicense;
    private JTextField jTextDesktopLicense;

    private final static Preferences PREFERENCES = Preferences.userRoot().node("com.gluonhq.plugin.intellij");
    private final static PropertiesComponent LEGACY_PREFERENCES = PropertiesComponent.getInstance();

    private String iniEmailAddress;
    private boolean iniCheck;
    private String iniMobileLicenseKey, iniDesktopLicenseKey;

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
                !jTextMobileLicense.getText().equals(iniMobileLicenseKey) ||
                !jTextDesktopLicense.getText().equals(iniDesktopLicenseKey);
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
                jTextMobileLicense.getText(),
                jTextDesktopLicense.getText());
    
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
                    LEGACY_PREFERENCES.getValue(ProjectConstants.PARAM_USER_LICENSE_MOBILE, ""),
                    LEGACY_PREFERENCES.getValue(ProjectConstants.PARAM_USER_LICENSE_DESKTOP, ""));

            LEGACY_PREFERENCES.unsetValue(ProjectConstants.PARAM_USER_IDE_OPTIN);
            LEGACY_PREFERENCES.unsetValue(ProjectConstants.PARAM_USER_EMAIL);
            LEGACY_PREFERENCES.unsetValue(ProjectConstants.PARAM_USER_UPTODATE);
            LEGACY_PREFERENCES.unsetValue(ProjectConstants.PARAM_USER_LICENSE_MOBILE);
            LEGACY_PREFERENCES.unsetValue(ProjectConstants.PARAM_USER_LICENSE_DESKTOP);
            LEGACY_PREFERENCES.unsetValue(ProjectConstants.PARAM_USER_MAC_ADDRESS);
            LEGACY_PREFERENCES.unsetValue(ProjectConstants.PARAM_USER_PLUGIN_VERSION);
        }

        iniEmailAddress = PREFERENCES.get(ProjectConstants.PARAM_USER_EMAIL, "");
        iniCheck = PREFERENCES.getBoolean(ProjectConstants.PARAM_USER_UPTODATE, true);
        iniMobileLicenseKey = PREFERENCES.get(ProjectConstants.PARAM_USER_LICENSE_MOBILE, "");
        iniDesktopLicenseKey = PREFERENCES.get(ProjectConstants.PARAM_USER_LICENSE_DESKTOP, "");

        jTextEmailAddress.setText(iniEmailAddress);
        jCheckUpToDate.setSelected(iniCheck);
        jTextMobileLicense.setText(iniMobileLicenseKey);
        jTextDesktopLicense.setText(iniDesktopLicenseKey);
    }
}
