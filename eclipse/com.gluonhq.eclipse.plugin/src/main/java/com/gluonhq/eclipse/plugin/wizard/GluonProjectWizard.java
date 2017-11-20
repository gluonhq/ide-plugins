package com.gluonhq.eclipse.plugin.wizard;

import java.io.File;

import org.eclipse.buildship.core.util.progress.AsyncHandler;
import org.eclipse.buildship.core.workspace.NewProjectHandler;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.gradle.tooling.CancellationToken;

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
	private final GluonProjectApplicationOperation operation;

	// the controllers that contain the wizard logic
	private final ProjectImportWizardController importController;

	public GluonProjectWizard(GluonProject gluonProject) {
		
		// store the dialog settings on the wizard and use them to retrieve / persist the most
        // recent values entered by the user
        setDialogSettings(getOrCreateDialogSection(UiPlugin.getInstance().getDialogSettings()));
        
		projectData = new ProjectData(gluonProject);
		operation = new GluonProjectApplicationOperation(projectData);

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
		return this.importController.performImportProject(new NewGluonProjectInitializer(), NewProjectHandler.IMPORT_AND_MERGE);
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
	
	/**
	 * Initializes a new Gluon project from the given configuration.
	 */
	private final class NewGluonProjectInitializer implements AsyncHandler {

		@Override
		public void run(IProgressMonitor monitor, CancellationToken token) {
			monitor.beginTask("Init Gluon project", IProgressMonitor.UNKNOWN);
			try {
				File projectDir = new File(projectData.projectLocation);
				if (!projectDir.exists()) {
					if (projectDir.mkdir()) {
						operation.perform(monitor, projectDir);
					}
				}
			} finally {
				monitor.done();
			}
		}
	}
}
