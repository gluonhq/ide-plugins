/*
 * Copyright (c) 2017, 2020, Gluon Software
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
package com.gluonhq.eclipse.plugin.wizard;

import com.gluonhq.plugin.templates.GluonProject;
import com.gluonhq.plugin.templates.ProjectConstants;
import com.gluonhq.plugin.templates.TemplateUtils;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;

import java.util.HashMap;
import java.util.Map;

public class ProjectData {

	public GluonProject projectType;
	
	public String userEmail = "";
	public boolean userUptodate = true;
	public String userMobileLicense = "";
	public String userDesktopLicense = "";
	public String userMacAddress = "";
	public String userPluginVersion = "";
	
	public String projectName;
	public String projectLocation;
	public String packageName = "com.gluonapplication";
	public String mainClassName = "GluonApplication";
	public boolean androidSelected = true;
	public boolean iosSelected = true;
	public boolean desktopSelected = true;
	public boolean embeddedSelected = false;
	public String buildTool = "maven";
	
	public String primaryViewName = "Primary";
	public String secondaryViewName = "Secondary";
	public String primaryViewCSS = "primary";
	public String secondaryViewCSS = "secondary";
	public boolean projectSelected = true;
	public boolean primaryViewSelected = true;
	public boolean secondaryViewSelected = true;
	
	public boolean afterburnerSelected = false;
	
	private final static IEclipsePreferences PREFERENCES = ConfigurationScope.INSTANCE.getNode("com.gluonhq.eclipse.plugin");
    // Mac: /Applications/eclipse/Eclipse.app/Contents/Eclipse/configuration/.settings/com.gluonhq.plugin.eclipse.prefs
	// Windows: C:\Program Files\Eclipse\java-mars\eclipse\configuration\.settings\com.gluonhq.plugin.eclipse.prefs
	// sand-box Eclipse Application: /Users/<user>/Eclipse/workspace/.metadata/.plugins/org.eclipse.pde.core/Eclipse\ Application/.settings/com.gluonhq.plugin.eclipse.prefs
	
	public ProjectData(GluonProject projectType) {
		this.projectType = projectType;
		// default name
		this.projectName = projectType.getName();
		
		if (alreadyOptedIn()) {
			this.userEmail = PREFERENCES.get(ProjectConstants.PARAM_USER_EMAIL, "");
			this.userUptodate = PREFERENCES.getBoolean(ProjectConstants.PARAM_USER_UPTODATE, true);
			this.userMobileLicense = PREFERENCES.get(ProjectConstants.PARAM_USER_LICENSE_MOBILE, "");
			this.userDesktopLicense = PREFERENCES.get(ProjectConstants.PARAM_USER_LICENSE_DESKTOP, "");
			this.userMacAddress = PREFERENCES.get(ProjectConstants.PARAM_USER_MAC_ADDRESS, "");
			this.userPluginVersion = PREFERENCES.get(ProjectConstants.PARAM_USER_PLUGIN_VERSION, ProjectConstants.PLUGIN_VERSION);
		}
	}

	public Map<String, Object> asParameters() {
		Map<String, Object> parameters = new HashMap<>();
		
		parameters.put(ProjectConstants.PARAM_USER_EMAIL, userEmail);
		parameters.put(ProjectConstants.PARAM_USER_UPTODATE, userUptodate);
		parameters.put(ProjectConstants.PARAM_USER_LICENSE_MOBILE, userMobileLicense);
		parameters.put(ProjectConstants.PARAM_USER_LICENSE_DESKTOP, userDesktopLicense);
		parameters.put(ProjectConstants.PARAM_USER_MAC_ADDRESS, userMacAddress);
		parameters.put(ProjectConstants.PARAM_USER_PLUGIN_VERSION, userPluginVersion);
		
		parameters.put(ProjectConstants.PARAM_PACKAGE_NAME, packageName);
		parameters.put(ProjectConstants.PARAM_PACKAGE_FOLDER, packageName.replaceAll("\\.", "/"));
		parameters.put(ProjectConstants.PARAM_MAIN_CLASS, packageName + "." + mainClassName);
		parameters.put(ProjectConstants.PARAM_MAIN_CLASS_NAME, mainClassName);
		parameters.put(ProjectConstants.PARAM_ANDROID_ENABLED, androidSelected);
		parameters.put(ProjectConstants.PARAM_IOS_ENABLED, iosSelected);
		parameters.put(ProjectConstants.PARAM_DESKTOP_ENABLED, desktopSelected);
		parameters.put(ProjectConstants.PARAM_EMBEDDED_ENABLED, embeddedSelected);
		parameters.put(ProjectConstants.PARAM_BUILD_TOOL, buildTool);
		
		if (this.projectType.equals(GluonProject.DESKTOP_MULTIVIEW) || 
			this.projectType.equals(GluonProject.DESKTOP_MULTIVIEWFXML) || 
			this.projectType.equals(GluonProject.MOBILE_MULTIVIEW) || 
			this.projectType.equals(GluonProject.MOBILE_MULTIVIEWFXML) ||
			this.projectType.equals(GluonProject.MOBILE_MULTIVIEW_GAF)) {
			parameters.put(ProjectConstants.PARAM_PRIMARY_VIEW, primaryViewName);
	        parameters.put(ProjectConstants.PARAM_SECONDARY_VIEW, secondaryViewName);
	        parameters.put(ProjectConstants.PARAM_PRIMARY_CSS, primaryViewCSS);
	        parameters.put(ProjectConstants.PARAM_SECONDARY_CSS, secondaryViewCSS);
	        parameters.put(ProjectConstants.PARAM_PROJECT_CSS_ENABLED, projectSelected);
	        parameters.put(ProjectConstants.PARAM_PRIMARY_CSS_ENABLED, primaryViewSelected);
	        parameters.put(ProjectConstants.PARAM_SECONDARY_CSS_ENABLED, secondaryViewSelected);
		}
		
		if (this.projectType.equals(GluonProject.MOBILE_MULTIVIEWFXML)) {
			parameters.put(ProjectConstants.PARAM_AFTERBURNER_ENABLED, afterburnerSelected);
		}
		
		return parameters;
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
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}
	
}
