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
		
		if (this.projectType.equals(GluonProject.DESKTOP_MULTIVIEW) || 
			this.projectType.equals(GluonProject.DESKTOP_MULTIVIEWFXML) || 
			this.projectType.equals(GluonProject.MOBILE_MULTIVIEW) || 
			this.projectType.equals(GluonProject.MOBILE_MULTIVIEWFXML)) {
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
