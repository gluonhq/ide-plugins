/*
 * Copyright (c) 2017, Gluon Software
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

import com.gluonhq.plugin.templates.OptInHelper;
import com.gluonhq.plugin.templates.ProjectConstants;
import com.gluonhq.plugin.templates.Template;
import com.gluonhq.plugin.templates.TemplateManager;

public class GluonProjectApplicationOperation  {

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
			parameters.put(ProjectConstants.PARAM_GLUON_MOBILE_GVM_VERSION, ProjectConstants.getMobileGvmVersion());
			parameters.put(ProjectConstants.PARAM_GLUON_DOWN_VERSION, ProjectConstants.getDownVersion());
			parameters.put(ProjectConstants.PARAM_GLUON_MOBILE_PLUGIN, ProjectConstants.getPluginVersion());
			parameters.put(ProjectConstants.PARAM_GLUON_MOBILE_GVM_PLUGIN, ProjectConstants.getPluginGvmVersion());
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
