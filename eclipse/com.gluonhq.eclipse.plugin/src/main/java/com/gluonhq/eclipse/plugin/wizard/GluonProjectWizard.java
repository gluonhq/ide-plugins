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

import org.eclipse.buildship.core.internal.workspace.NewProjectHandler;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;

import com.gluonhq.eclipse.plugin.HelpContext;
import com.gluonhq.eclipse.plugin.UiPlugin;
import com.gluonhq.plugin.templates.GluonProject;

public abstract class GluonProjectWizard extends Wizard implements INewWizard, HelpContextIdProvider {

	/**
     * The section name declaration for {@link org.eclipse.jface.dialogs.DialogSettings} where the import wizard stores its
     * preferences.
     *
     * @see org.eclipse.jface.dialogs.DialogSettings#getOrCreateSection(IDialogSettings, String)
     */
    private static final String WIZARD_ID = "com.gluonhq.eclipse.plugin.wizard"; //$NON-NLS-1$
	
	private final ProjectData projectData;
	
	// the controllers that contain the wizard logic
	private final ProjectImportWizardController importController;

	public GluonProjectWizard(GluonProject gluonProject) {
		
		// store the dialog settings on the wizard and use them to retrieve / persist the most
        // recent values entered by the user
        setDialogSettings(getOrCreateDialogSection(UiPlugin.getInstance().getDialogSettings()));
        
		projectData = new ProjectData(gluonProject);
		
		// instantiate the controllers for this wizard
		this.importController = new ProjectImportWizardController(this);
	}

	@Override
	public String getWindowTitle() {
		return "Create a new Gluon Project";// TODO
	}

	@Override
	public String getHelpContextId() {
		return HelpContext.PROJECT_CREATION;
	}

	@Override
	public void addPages() {
		// show progress bar when getContainer().run() is called
		setNeedsProgressMonitor(true);

		// disable help on all wizard pages
		setHelpAvailable(false);
	}

	@Override
	public boolean performFinish() {
		importController.getConfiguration().setProjectDir(new File(projectData.projectLocation));
		return this.importController.performImportProject(getContainer(), projectData, NewProjectHandler.IMPORT_AND_MERGE);
	}

	protected ProjectData getProjectData() {
		return projectData;
	}
	
	private static IDialogSettings getOrCreateDialogSection(IDialogSettings dialogSettings) {
        // in Eclipse 3.6 the method DialogSettings#getOrCreateSection does not exist
        IDialogSettings section = dialogSettings.getSection(WIZARD_ID);
        if (section == null) {
            section = dialogSettings.addNewSection(WIZARD_ID);
        }
        return section;
    }
}
