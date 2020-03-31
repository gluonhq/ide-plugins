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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ConfigureSampleClassPage extends WizardPage {

	public static final ImageDescriptor WIZBAN_IMAGE = ImageDescriptor.createFromURL(ConfigureSampleClassPage.class.getClassLoader().getResource("icons/wizban/basic_wizard.png"));

	private Text packageName;
	private Text mainClassName;
	private Text mainClass;
	private Button androidCheckBox;
	private Button iosCheckBox;
	private Button desktopCheckBox;
	private Button embeddedCheckBox;
	private Button mavenRadioButton;
	private Button gradleRadioButton;

	private final ProjectData projectData;

	public ConfigureSampleClassPage(ProjectData projectData) {
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

		if (projectData.androidSelected) {
			String androidValidation = validateAndroidPackageName(projectData.packageName);
			if (androidValidation != null) {
				setErrorMessage(androidValidation);
				setPageComplete(false);
				return;
			}
		}

		if (projectData.mainClassName.trim().isEmpty()) {
			setErrorMessage("Main class name must not be empty");
			setPageComplete(false);
			return;
		}

        if (projectData.buildTool.trim().isEmpty()) {
            setErrorMessage("Build tool must be defined");
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
		container.setLayout( new GridLayout( 3, false ) );

		{
			Label l = new Label( container, SWT.NONE );
			l.setText( "Package Name:" );

			packageName = new Text(container,SWT.BORDER);
			GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
			layoutData.grabExcessHorizontalSpace = true;
			layoutData.horizontalSpan = 2;
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
			layoutData.horizontalSpan = 2;
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
			layoutData.horizontalSpan = 2;
			mainClass.setLayoutData(layoutData);
			mainClass.setEnabled(false);
		}

		{
			Label l = new Label( container, SWT.NONE );
			l.setText( "Platforms:" );
			GridData gridData = new GridData(GridData.BEGINNING, GridData.CENTER, false, false);
			gridData.verticalSpan = 4;
			l.setLayoutData(gridData);

			androidCheckBox = new Button(container, SWT.CHECK);
			androidCheckBox.setText(" Android ");
			GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
			layoutData.grabExcessHorizontalSpace = true;
			layoutData.horizontalSpan = 2;
			androidCheckBox.setLayoutData(layoutData);
			androidCheckBox.setSelection(true);
			androidCheckBox.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}

				@Override
				public void widgetSelected(SelectionEvent arg0) {
					projectData.androidSelected = androidCheckBox.getSelection();
					validate();
				}
			});

			iosCheckBox = new Button(container, SWT.CHECK);
			iosCheckBox.setText(" iOS ");
			iosCheckBox.setLayoutData(layoutData);
			iosCheckBox.setSelection(true);
			iosCheckBox.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}

				@Override
				public void widgetSelected(SelectionEvent arg0) {
					projectData.iosSelected = iosCheckBox.getSelection();
					validate();
				}
			});

			desktopCheckBox = new Button(container, SWT.CHECK);
			desktopCheckBox.setText(" Desktop ");
			desktopCheckBox.setLayoutData(layoutData);
			desktopCheckBox.setSelection(true);
			desktopCheckBox.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}

				@Override
				public void widgetSelected(SelectionEvent arg0) {
					projectData.desktopSelected = desktopCheckBox.getSelection();
					validate();
				}
			});

			embeddedCheckBox = new Button(container, SWT.CHECK);
			embeddedCheckBox.setText(" Embedded ");
			embeddedCheckBox.setLayoutData(layoutData);
			embeddedCheckBox.setSelection(false);
			embeddedCheckBox.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}

				@Override
				public void widgetSelected(SelectionEvent arg0) {
					projectData.embeddedSelected = embeddedCheckBox.getSelection();
					validate();
				}
			});
		}
		{
			Label l = new Label( container, SWT.NONE );
			l.setText( "Build Tool:" );
			
			mavenRadioButton = new Button(container, SWT.RADIO);
			mavenRadioButton.setText(" Maven ");
			mavenRadioButton.setLayoutData(new GridData());
			mavenRadioButton.setSelection(true);
			mavenRadioButton.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}

				@Override
				public void widgetSelected(SelectionEvent arg0) {
					projectData.buildTool = mavenRadioButton.getText().toLowerCase().trim();
					validate();
				}
			});
			
			gradleRadioButton = new Button(container, SWT.RADIO);
			gradleRadioButton.setText(" Gradle ");
			gradleRadioButton.setLayoutData(new GridData());
			gradleRadioButton.setSelection(false);
			gradleRadioButton.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}

				@Override
				public void widgetSelected(SelectionEvent arg0) {
					projectData.buildTool = gradleRadioButton.getText().toLowerCase().trim();
				}
			});
		}

		updateTexts();

		setControl(container);
	}

    /**
     * Validate if the provided package name is valid to be used as
     * an Android package name in the Android manifest file.
     * @param name
     * @return <code>null</code> when it is a valid name, or an error message
     * when it is not
     */
    private static String validateAndroidPackageName(String name) {
        final int N = name.length();
        boolean hasSep = false;
        boolean front = true;
        for (int i=0; i<N; i++) {
            final char c = name.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                front = false;
                continue;
            }
            if ((c >= '0' && c <= '9') || c == '_') {
                if (!front) {
                    continue;
                } else {
                    if (c == '_') {
                        return "The character '_' cannot be the first character in a package segment";
                    } else {
                        return "A digit cannot be the first character in a package segment";
                    }
                }
            }
            if (c == '.') {
                hasSep = true;
                front = true;
                continue;
            }
            return "The character '" + c + "' is not allowed in Android application package names";
        }
        return hasSep ? null : "The package must have at least one '.' separator";
    }
}
