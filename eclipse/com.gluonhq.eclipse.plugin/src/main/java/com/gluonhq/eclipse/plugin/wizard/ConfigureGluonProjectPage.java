/**
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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.TextProcessor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

//import org.eclipse.jface.dialogs.IDialogSettings;
//import org.springsource.ide.eclipse.gradle.ui.GradleUI;

public class ConfigureGluonProjectPage extends WizardPage {

	public static final ImageDescriptor WIZBAN_IMAGE = ImageDescriptor.createFromURL(ConfigureGluonProjectPage.class.getClassLoader().getResource("icons/wizban/basic_wizard.png"));
	private static final String SAVED_LOCATION_ATTR = "OUTSIDE_LOCATION"; //$NON-NLS-1$

	private Text projectName;
	private Button useDefaultsButton;
	private Label projectLocationLabel;
	private Text projectLocation;
	private Button browseButton;
	private String previousExternalLocation = "";

	private final ProjectData projectData;

	public ConfigureGluonProjectPage(ProjectData projectData) {
		super(projectData.projectType.getName(), "Name and Location", WIZBAN_IMAGE);

		this.projectData = projectData;
		this.projectData.projectLocation = getDefaultPathDisplayString();
	}

	private void updateTexts() {
		if (useDefaultsButton.getSelection()) {
			updateLocationField(getDefaultPathDisplayString());
		}
	}

	private void validate() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();

		// check whether the project name is empty
		String projectName = projectData.projectName;
		if (projectName == null || projectName.trim().isEmpty()) {
			setErrorMessage("Project name must not be empty");
			setPageComplete(false);
			return;
		}

		// check whether the project name is valid
		final IStatus projectNameStatus = workspace.validateName(projectName, IResource.PROJECT);
		if (!projectNameStatus.isOK()) {
			setErrorMessage(projectNameStatus.getMessage());
			setPageComplete(false);
			return;
		}

		// check whether the project with the name already exists
		IProject handle = workspace.getRoot().getProject(projectData.projectName);
		if (handle.exists()) {
			setErrorMessage("A project with name '" + projectData.projectName + "' already exists in the workspace");
			setPageComplete(false);
			return;
		}

		// check whether the location is empty
		String location = projectData.projectLocation;
		if (location == null || location.isEmpty()) {
			setErrorMessage("Enter a location for the project.");
			setPageComplete(false);
			return;
		}

		// check whether the location is a syntactically correct path
		if (!Path.EMPTY.isValidPath(location)) {
			setErrorMessage("Invalid project contents directory");
			setPageComplete(false);
			return;
		}

		IPath projectPath = null;
		if (!useDefaultsButton.getSelection()) {
			projectPath = Path.fromOSString(location);
			if (!projectPath.toFile().exists()) {
				if (!canCreate(projectPath.toFile())) {
					setErrorMessage("Cannot create project content at the given external location.");
					setPageComplete(false);
					return;
				}
			}
		}

		// check whether the location is valid
		final IStatus locationStatus = workspace.validateProjectLocation(handle, projectPath);
		if (!locationStatus.isOK()) {
			setErrorMessage(locationStatus.getMessage());
			setPageComplete(false);
			return;
		}

		setErrorMessage(null);
		setPageComplete(true);
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite( parent, SWT.NONE );
		container.setLayout( new GridLayout( 3, false ) );

		{
			Label l = new Label(container, SWT.NONE);
			l.setText("Project Name:");
			
			projectName = new Text(container, SWT.BORDER);
			GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
			layoutData.horizontalSpan = 2;
			projectName.setLayoutData(layoutData);
			projectName.setText(projectData.projectName);
			projectName.addModifyListener(new ModifyListener() {
				
				@Override
				public void modifyText(ModifyEvent e) {
					projectData.projectName = projectName.getText();
					updateTexts();
					validate();
				}
			});
		}

		{
			useDefaultsButton = new Button(container, SWT.CHECK | SWT.RIGHT);
			useDefaultsButton.setText("Use default location");
			useDefaultsButton.setSelection(true);
			GridData gridData = new GridData();
			gridData.horizontalSpan = 3;
			useDefaultsButton.setLayoutData(gridData);

			projectLocationLabel = new Label(container, SWT.NONE);
			projectLocationLabel.setText("Location:");

			projectLocation = new Text(container, SWT.BORDER);
			updateLocationField(projectData.projectLocation);
			projectLocation.addModifyListener(new ModifyListener() {
				
				@Override
				public void modifyText(ModifyEvent e) {
					projectData.projectLocation = projectLocation.getText();
					validate();
				}
			});

			browseButton = new Button(container, SWT.PUSH);
			browseButton.setText("Browse");
			browseButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					handleLocationBrowseButtonSelected();
				}				
			});

			useDefaultsButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					boolean useDefaults = useDefaultsButton.getSelection();
					if (useDefaults) {
						previousExternalLocation = projectLocation.getText();
						updateLocationField(getDefaultPathDisplayString());
					} else {
						projectLocation.setText(previousExternalLocation);
					}
					setUserAreaEnabled(!useDefaults);
				}
			});
		}

		setUserAreaEnabled(false);

		setControl(container);

		updateTexts();
		validate();
	}
	
	

	private String getDefaultPathDisplayString() {
		if (projectData.projectName != null) {
			return Platform.getLocation().append(projectData.projectName).toOSString();
		} else {
			return Platform.getLocation().toOSString();
		}
	}

	private void setUserAreaEnabled(boolean enabled) {
		projectLocationLabel.setEnabled(enabled);
		projectLocation.setEnabled(enabled);
		browseButton.setEnabled(enabled);
	}

	private void handleLocationBrowseButtonSelected() {
		String selectedDirectory = null;
		String dirName = getPathFromLocationField();

		if (dirName != null && !dirName.equals("")) {
			File dir = new File(dirName);
			if (!dir.exists()) {
				dirName = "";
			}
		}
		if (dirName == null || dirName.equals("")) {
			String value = getDialogSettings().get(SAVED_LOCATION_ATTR);
			if (value != null) {
				dirName = value;
			}
		}

		DirectoryDialog dialog = new DirectoryDialog(projectLocation.getShell(), SWT.SHEET);
		dialog.setMessage("Select the location directory");
		dialog.setFilterPath(dirName);
		selectedDirectory = dialog.open();
		if (selectedDirectory != null) {
			updateLocationField(selectedDirectory);
			getDialogSettings().put(SAVED_LOCATION_ATTR, selectedDirectory);
		}
	}

	private void updateLocationField(String location) {
		projectLocation.setText(TextProcessor.process(location));
	}

	private String getPathFromLocationField() {
		URI fieldURI;
		try {
			fieldURI = new URI(projectLocation.getText());
		} catch (URISyntaxException e) {
			return projectLocation.getText();
		}
		String path = fieldURI.getPath();
		return path != null ? path : projectLocation.getText();
	}

    private boolean canCreate(File file) {
    	while (!file.exists()) {
    		file = file.getParentFile();
    		if (file == null) {
    			return false;
    		}
    	}
    	return file.canWrite();
    }

}
