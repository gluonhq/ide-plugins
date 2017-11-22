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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ConfigureDesktopClassPage extends WizardPage {

	public static final ImageDescriptor WIZBAN_IMAGE = ImageDescriptor.createFromURL(ConfigureDesktopClassPage.class.getClassLoader().getResource("icons/wizban/basic_wizard.png"));

	private Text packageName;
	private Text mainClassName;
	private Text mainClass;

	private final ProjectData projectData;

	public ConfigureDesktopClassPage(ProjectData projectData) {
		super("GluonApplicationSettings", "Gluon Application Settings", WIZBAN_IMAGE);

		this.projectData = projectData;
	}

	private void updateTexts() {
		if (projectData.packageName.isEmpty()) {
			mainClass.setText(projectData.mainClassName);
		} else if (projectData.mainClassName.isEmpty()) {
			mainClass.setText(projectData.packageName);
		} else {
			mainClass.setText(projectData.packageName + "." + projectData.mainClassName);
		}
	}

	private void validate() {
		if (projectData.packageName.trim().isEmpty()) {
			setErrorMessage("Package name must not be empty");
			setPageComplete(false);
			return;
		}

		IStatus validPackageName = JavaConventions.validatePackageName(projectData.packageName, "1.5", "1.5");
		if (!validPackageName.isOK()) {
			setErrorMessage("Package name is invalid");
			setPageComplete(false);
			return;
		}

		
		if (projectData.mainClassName.trim().isEmpty()) {
			setErrorMessage("Main class name must not be empty");
			setPageComplete(false);
			return;
		}

		IStatus validClassName = JavaConventions.validateJavaTypeName(projectData.mainClassName, "1.5", "1.5");
		if (!validClassName.isOK()) {
			setErrorMessage("Main class name is invalid");
			setPageComplete(false);
			return;
		}

		setErrorMessage(null);
		setPageComplete(true);
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite( parent, SWT.NONE );
		container.setLayout( new GridLayout( 2, false ) );

		{
			Label l = new Label( container, SWT.NONE );
			l.setText( "Package Name:" );
			
			packageName = new Text(container,SWT.BORDER);
			GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
			layoutData.grabExcessHorizontalSpace = true;
			packageName.setLayoutData(layoutData);
			packageName.setText(projectData.packageName);
			packageName.addModifyListener(new ModifyListener() {
				
				@Override
				public void modifyText(ModifyEvent e) {
					projectData.packageName = packageName.getText();
					updateTexts();
					validate();
				}
			});
		}

		{
			Label l = new Label( container, SWT.NONE );
			l.setText( "Main Class Name:" );
			
			mainClassName = new Text(container,SWT.BORDER);
			GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
			layoutData.grabExcessHorizontalSpace = true;
			mainClassName.setLayoutData(layoutData);
			mainClassName.setText(projectData.mainClassName);
			mainClassName.addModifyListener(new ModifyListener() {
				
				@Override
				public void modifyText(ModifyEvent e) {
					projectData.mainClassName = mainClassName.getText();
					updateTexts();
					validate();
				}
			});
		}

		{
			Label l = new Label( container, SWT.NONE );
			l.setText( "Main Class:" );
			
			mainClass = new Text(container,SWT.BORDER);
			GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
			layoutData.grabExcessHorizontalSpace = true;
			mainClass.setLayoutData(layoutData);
			mainClass.setEnabled(false);
		}

		updateTexts();

		setControl(container);
	}

}
