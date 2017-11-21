package com.gluonhq.eclipse.plugin.wizard;

import com.gluonhq.plugin.templates.OptInHelper;
import com.gluonhq.plugin.templates.ProjectConstants;
import com.gluonhq.plugin.templates.Template;
import com.gluonhq.plugin.templates.TemplateManager;
import org.eclipse.core.runtime.IProgressMonitor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GluonProjectApplicationOperation {

	private final ProjectData projectData;

	private Template projectTemplate = null;
	private Template sourceTemplate = null;

	public GluonProjectApplicationOperation(ProjectData projectData) {
		this.projectData = projectData;

		TemplateManager templateManager = TemplateManager.getInstance();

		projectTemplate = templateManager.getProjectTemplate(projectData.projectType.getType());
		sourceTemplate = templateManager.getSourceTemplate(projectTemplate.getProjectName());
	}

	public void perform(IProgressMonitor mon, File projectDir) {
		createProjectContents(mon, projectDir);

		// OptIn
	    if (!ProjectData.alreadyOptedIn()) {
	    	ProjectData.persistOptIn(projectData.userEmail, projectData.userUptodate, 
	        		projectData.userMobileLicense, projectData.userDesktopLicense);
	    	
	    	// only send data once
	    	OptInHelper.optIn(projectData.userEmail, projectData.userUptodate, "plugineclipse",
	    			projectData.userMacAddress, projectData.userPluginVersion, false);
        
		}
	}

	private void createProjectContents(IProgressMonitor mon, File projectDir) {
		mon.beginTask("Create project contents", 1);
		try {
			Map<String, Object> parameters = projectData.asParameters();
			parameters.put(ProjectConstants.PARAM_PROJECT_NAME, projectData.projectName);
			
			parameters.put(ProjectConstants.PARAM_GLUON_DESKTOP_VERSION, ProjectConstants.getDesktopVersion());
			parameters.put(ProjectConstants.PARAM_GLUON_MOBILE_VERSION, ProjectConstants.getMobileVersion());
			parameters.put(ProjectConstants.PARAM_GLUON_DOWN_VERSION, ProjectConstants.getDownVersion());
			parameters.put(ProjectConstants.PARAM_GLUON_MOBILE_PLUGIN, ProjectConstants.getPluginVersion());
			parameters.put(ProjectConstants.PARAM_GLUON_GLISTEN_AFTERBURNER_VERSION, ProjectConstants.getGlistenAfterburnerVersion());

			List<File> filesToOpen = new ArrayList<>();
			if (projectTemplate != null) {
				projectTemplate.render(projectDir, parameters);
				filesToOpen.addAll(projectTemplate.getFilesToOpen());

				if (sourceTemplate != null) {
					sourceTemplate.render(projectDir, parameters);
					filesToOpen.addAll(sourceTemplate.getFilesToOpen());
				}
			}
			
			// TODO: open files
		} finally {
			mon.done();
		}
	}
	
	
}
