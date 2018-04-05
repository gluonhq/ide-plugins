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
package com.gluonhq.plugin.netbeans.template;

import com.gluonhq.plugin.templates.ProjectConstants;
import com.gluonhq.plugin.templates.TemplateUtils;
import org.openide.WizardDescriptor;
import org.openide.util.Exceptions;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class OptInHelper {
    private final static Preferences PREFERENCES = Preferences.userRoot().node("com.gluonhq.plugin.netbeans");
    // On Mac: /Users/<user>/Library/Preferences/com.apple.java.util.prefs.plist
    // On Windows: HKEY_CURRENT_USER\Software\JavaSoft\Prefs\com.gluonhq.plugin.netbeans

    public static void restoreOptIn(WizardDescriptor wiz) {
        if (alreadyOptedIn()) {
            wiz.putProperty(ProjectConstants.PARAM_USER_EMAIL, PREFERENCES.get(ProjectConstants.PARAM_USER_EMAIL, ""));
            wiz.putProperty(ProjectConstants.PARAM_USER_UPTODATE, PREFERENCES.getBoolean(ProjectConstants.PARAM_USER_UPTODATE, true));
            wiz.putProperty(ProjectConstants.PARAM_USER_LICENSE_MOBILE, PREFERENCES.get(ProjectConstants.PARAM_USER_LICENSE_MOBILE, ""));
            wiz.putProperty(ProjectConstants.PARAM_USER_LICENSE_DESKTOP, PREFERENCES.get(ProjectConstants.PARAM_USER_LICENSE_DESKTOP, ""));
            wiz.putProperty(ProjectConstants.PARAM_USER_MAC_ADDRESS, PREFERENCES.get(ProjectConstants.PARAM_USER_MAC_ADDRESS, ""));
            wiz.putProperty(ProjectConstants.PARAM_USER_PLUGIN_VERSION, PREFERENCES.get(ProjectConstants.PARAM_USER_PLUGIN_VERSION, ProjectConstants.PLUGIN_VERSION));
        }
    }

    public static boolean alreadyOptedIn() {
        return "true".equals(PREFERENCES.get(ProjectConstants.PARAM_USER_IDE_OPTIN, ""));
    }

    public static void persistOptIn(String email, boolean uptodate, String mobileLicense, String desktopLicense) {
        PREFERENCES.put(ProjectConstants.PARAM_USER_IDE_OPTIN, "true");
        PREFERENCES.put(ProjectConstants.PARAM_USER_EMAIL, email);
        PREFERENCES.putBoolean(ProjectConstants.PARAM_USER_UPTODATE, uptodate);
        PREFERENCES.put(ProjectConstants.PARAM_USER_LICENSE_MOBILE, mobileLicense);
        PREFERENCES.put(ProjectConstants.PARAM_USER_LICENSE_DESKTOP, desktopLicense);
        PREFERENCES.put(ProjectConstants.PARAM_USER_MAC_ADDRESS, TemplateUtils.getMacAddress());
        PREFERENCES.put(ProjectConstants.PARAM_USER_PLUGIN_VERSION, ProjectConstants.PLUGIN_VERSION);
        try {
            PREFERENCES.flush();
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
