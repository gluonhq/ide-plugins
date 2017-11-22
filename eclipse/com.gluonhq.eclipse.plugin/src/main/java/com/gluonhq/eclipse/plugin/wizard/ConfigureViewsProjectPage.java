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

import java.util.Locale;

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

import com.gluonhq.plugin.templates.TemplateUtils;

public class ConfigureViewsProjectPage extends WizardPage {

	public static final ImageDescriptor WIZBAN_IMAGE = ImageDescriptor.createFromURL(ConfigureViewsProjectPage.class.getClassLoader().getResource("icons/wizban/basic_wizard.png"));

	private Text primaryViewNameText;
	private Text secondaryViewNameText;
	private Button projectCheckBox;
	private Button primaryViewCheckBox;
	private Button secondaryViewCheckBox;
	private Button afterburnerCheckBox;
	
	private final ProjectData projectData;
	private final boolean useFXML;

	public ConfigureViewsProjectPage(ProjectData projectData, boolean useFXML) {
		super("GluonApplicationSettings", "Name of Views", WIZBAN_IMAGE);

		this.projectData = projectData;
		this.useFXML = useFXML;
	}

	private void validate() {
		if (!TemplateUtils.isValidNameView(projectData.primaryViewName.trim())) {
			setErrorMessage(projectData.primaryViewName + " is not a valid view name for the Primary View Name.");
        	setPageComplete(false);
			return;
		}
		
		if (!TemplateUtils.isValidNameView(projectData.secondaryViewName.trim())) {
			setErrorMessage(projectData.secondaryViewName + " is not a valid view name for the Secondary View Name.");
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
			l.setText( "Primary View Name:" );
			
			primaryViewNameText = new Text(container,SWT.BORDER);
			GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
			layoutData.grabExcessHorizontalSpace = true;
			primaryViewNameText.setLayoutData(layoutData);
			primaryViewNameText.setText(projectData.primaryViewName);
			primaryViewNameText.addModifyListener(new ModifyListener() {
				
				@Override
				public void modifyText(ModifyEvent e) {
					if (!TemplateUtils.isValidNameView(primaryViewNameText.getText().trim())) {
						setErrorMessage(primaryViewNameText.getText() + " is not a valid view name for the Primary View Name.");
			        	setPageComplete(false);
						return;
					} else {
						final String primary = TemplateUtils.getCorrectNameView(primaryViewNameText.getText().trim(), "Primary");
				        projectData.primaryViewName = TemplateUtils.upperCaseWord(primary);
						projectData.primaryViewCSS = primary.toLowerCase(Locale.ROOT);
						validate();
					}
				}
			});
		}

		{
			Label l = new Label( container, SWT.NONE );
			l.setText( "Secondary View Name:" );
			
			secondaryViewNameText = new Text(container,SWT.BORDER);
			GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
			layoutData.grabExcessHorizontalSpace = true;
			secondaryViewNameText.setLayoutData(layoutData);
			secondaryViewNameText.setText(projectData.secondaryViewName);
			secondaryViewNameText.addModifyListener(new ModifyListener() {
				
				@Override
				public void modifyText(ModifyEvent e) {
					if (!TemplateUtils.isValidNameView(projectData.secondaryViewName.trim())) {
						setErrorMessage(projectData.secondaryViewName + " is not a valid view name for the Secondary View Name.");
						setPageComplete(false);
						return;
					} else {
						final String secondary = TemplateUtils.getCorrectNameView(secondaryViewNameText.getText().trim(), "Secondary");
						projectData.secondaryViewName = TemplateUtils.upperCaseWord(secondary);
						projectData.secondaryViewCSS = secondary.toLowerCase(Locale.ROOT);
						validate();
					}
			        
				}
			});
		}

		{
			Label l = new Label( container, SWT.NONE );
			l.setText( "CSS files:" );
			GridData gridData = new GridData(GridData.BEGINNING, GridData.CENTER, false, false);
			gridData.verticalSpan = 3;
			l.setLayoutData(gridData);

			projectCheckBox = new Button(container, SWT.CHECK);
			projectCheckBox.setText(" Project ");
			GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
			layoutData.grabExcessHorizontalSpace = true;
			projectCheckBox.setLayoutData(layoutData);
			projectCheckBox.setSelection(true);
			projectCheckBox.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}

				@Override
				public void widgetSelected(SelectionEvent arg0) {
					projectData.projectSelected = projectCheckBox.getSelection();
					validate();
				}
			});

			primaryViewCheckBox = new Button(container, SWT.CHECK);
			primaryViewCheckBox.setText(" Primary View ");
			primaryViewCheckBox.setLayoutData(layoutData);
			primaryViewCheckBox.setSelection(true);
			primaryViewCheckBox.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}

				@Override
				public void widgetSelected(SelectionEvent arg0) {
					projectData.primaryViewSelected = primaryViewCheckBox.getSelection();
					validate();
				}
			});

			secondaryViewCheckBox = new Button(container, SWT.CHECK);
			secondaryViewCheckBox.setText(" Secondary View ");
			secondaryViewCheckBox.setLayoutData(layoutData);
			secondaryViewCheckBox.setSelection(true);
			secondaryViewCheckBox.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}

				@Override
				public void widgetSelected(SelectionEvent arg0) {
					projectData.secondaryViewSelected = secondaryViewCheckBox.getSelection();
					validate();
				}
			});
			
		}
		if (useFXML) {
			{
				Label l = new Label( container, SWT.NONE );
				l.setText( "" );
				GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
				layoutData.grabExcessHorizontalSpace = true;
				layoutData.horizontalSpan = 2;
				l.setLayoutData(layoutData);
			}
			
			{
				Label l = new Label( container, SWT.NONE );
				l.setText( "FXML:" );
				
				afterburnerCheckBox = new Button(container, SWT.CHECK);
				afterburnerCheckBox.setText(" Use Afterburner ");
				GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
				layoutData.grabExcessHorizontalSpace = true;
				afterburnerCheckBox.setLayoutData(layoutData);
				afterburnerCheckBox.setSelection(false);
				afterburnerCheckBox.addSelectionListener(new SelectionListener() {
					
					@Override
					public void widgetDefaultSelected(SelectionEvent arg0) {
					}
	
					@Override
					public void widgetSelected(SelectionEvent arg0) {
						projectData.afterburnerSelected = afterburnerCheckBox.getSelection();
						validate();
					}
				});
			}
		}
		setControl(container);
	}

}
