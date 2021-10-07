/*
 * Copyright (c) 2017, 2021, Gluon Software
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
package com.gluonhq.eclipse.plugin.preferences;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.gluonhq.eclipse.plugin.wizard.ProjectData;
import com.gluonhq.plugin.templates.ProjectConstants;
import com.gluonhq.plugin.templates.TemplateUtils;

import org.eclipse.ui.IWorkbench;


public class OptionsPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private final static IEclipsePreferences PREFERENCES = ConfigurationScope.INSTANCE.getNode("com.gluonhq.eclipse.plugin");
	private Text emailName;
	private Button uptodateCheckBox;
	private Text gluonLicense;
	private Label error;
	
	public String userEmail = "";
	public boolean userUptodate = true;
	public String userGluonLicense = "";

	private final org.eclipse.swt.graphics.Color color;
	
	public OptionsPage() {
		super(GRID);
		setDescription("Gluon Settings");
		if (ProjectData.alreadyOptedIn()) {
			this.userEmail = PREFERENCES.get(ProjectConstants.PARAM_USER_EMAIL, "");
			this.userUptodate = PREFERENCES.getBoolean(ProjectConstants.PARAM_USER_UPTODATE, true);
			this.userGluonLicense = PREFERENCES.get(ProjectConstants.PARAM_USER_LICENSE, "");
		}
		color = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
	}
	
	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
	    composite.setLayout(new GridLayout(2, false));
	    
	    {
	    	Label l = new Label(composite, SWT.NONE );
			l.setText("Email address:");
			
			emailName = new Text(composite, SWT.BORDER);
		    emailName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		    emailName.setText(PREFERENCES.get(ProjectConstants.PARAM_USER_EMAIL, ""));
    		emailName.addModifyListener(new ModifyListener() {
				
				@Override
				public void modifyText(ModifyEvent e) {
					error.setVisible(!TemplateUtils.isValidEmail(emailName.getText()));
				}
			});
	    }
	    {
	    	Label l = new Label(composite, SWT.NONE );
			l.setText("Keep up-to-date:");
			
			uptodateCheckBox = new Button(composite, SWT.CHECK);
			uptodateCheckBox.setText("");
			GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
			layoutData.grabExcessHorizontalSpace = true;
			uptodateCheckBox.setLayoutData(layoutData);
			uptodateCheckBox.setSelection(PREFERENCES.getBoolean(ProjectConstants.PARAM_USER_UPTODATE, true));
	    }
	    {
	    	Label l = new Label(composite, SWT.NONE);
			l.setText("Gluon License Key:");
			
			gluonLicense = new Text(composite, SWT.BORDER);
			GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
			layoutData.grabExcessHorizontalSpace = true;
			gluonLicense.setLayoutData(layoutData);
			gluonLicense.setText(PREFERENCES.get(ProjectConstants.PARAM_USER_LICENSE, ""));
		}
	    {
	    	new Label(composite, SWT.NONE);
	    	new Label(composite, SWT.NONE);
	    }
	    {
	    	error = new Label(composite, SWT.NONE);
		    error.setText("A valid email address is required");
		    error.setForeground(color);
		    error.setVisible(false);
	    }
	    return composite;		
	}
	
	public void init(IWorkbench workbench) {
	}
	
	protected void performDefaults() {
		emailName.setText(userEmail);
		uptodateCheckBox.setSelection(userUptodate);
		gluonLicense.setText(userGluonLicense);
	}
	
	@Override
	protected void performApply() {
		super.performApply();
	}
	
	@Override
	public boolean performOk() {
		boolean changed = false;
		
		if (!emailName.getText().equals(userEmail) ||
			uptodateCheckBox.getSelection() != userUptodate ||
			!gluonLicense.getText().equals(userGluonLicense) ||
			PREFERENCES.get(ProjectConstants.PARAM_USER_MAC_ADDRESS, "").isEmpty() ||
			PREFERENCES.get(ProjectConstants.PARAM_USER_PLUGIN_VERSION, "").isEmpty()) {
			changed = true;
		}
		
        // store modified settings
        ProjectData.persistOptIn(emailName.getText(), 
				uptodateCheckBox.getSelection(), 
				gluonLicense.getText());

        if (changed) {
			// send modified settings
	        com.gluonhq.plugin.templates.OptInHelper.optIn(emailName.getText(), 
	            	uptodateCheckBox.getSelection(), "plugineclipse", 
	                PREFERENCES.get(ProjectConstants.PARAM_USER_MAC_ADDRESS, ""),
	                PREFERENCES.get(ProjectConstants.PARAM_USER_PLUGIN_VERSION, ""), true);
        }
        
		this.userEmail = PREFERENCES.get(ProjectConstants.PARAM_USER_EMAIL, "");
		this.userUptodate = PREFERENCES.getBoolean(ProjectConstants.PARAM_USER_UPTODATE, true);
		this.userGluonLicense = PREFERENCES.get(ProjectConstants.PARAM_USER_LICENSE, "");

		return super.performOk();
	}

	@Override
	protected void createFieldEditors() {
		// TODO Auto-generated method stub	
	}
	
}