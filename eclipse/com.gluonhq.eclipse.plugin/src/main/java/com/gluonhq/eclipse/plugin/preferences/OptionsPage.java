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
	private Text mobileLicense;
	private Text desktopLicense;
	private Label error;
	
	public String userEmail = "";
	public boolean userUptodate = true;
	public String userMobileLicense = "";
	public String userDesktopLicense = "";
	
	private final org.eclipse.swt.graphics.Color color;
	
	public OptionsPage() {
		super(GRID);
		setDescription("Gluon Settings");
		if (ProjectData.alreadyOptedIn()) {
			this.userEmail = PREFERENCES.get(ProjectConstants.PARAM_USER_EMAIL, "");
			this.userUptodate = PREFERENCES.getBoolean(ProjectConstants.PARAM_USER_UPTODATE, true);
			this.userMobileLicense = PREFERENCES.get(ProjectConstants.PARAM_USER_LICENSE_MOBILE, "");
			this.userDesktopLicense = PREFERENCES.get(ProjectConstants.PARAM_USER_LICENSE_DESKTOP, "");
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
			l.setText("Mobile License Key:");
			
			mobileLicense = new Text(composite, SWT.BORDER);
			GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
			layoutData.grabExcessHorizontalSpace = true;
			mobileLicense.setLayoutData(layoutData);
			mobileLicense.setText(PREFERENCES.get(ProjectConstants.PARAM_USER_LICENSE_MOBILE, ""));
		}
	    {
			Label l = new Label(composite, SWT.NONE);
			l.setText("Desktop License Key:");
			
			desktopLicense = new Text(composite, SWT.BORDER);
			GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
			layoutData.grabExcessHorizontalSpace = true;
			desktopLicense.setLayoutData(layoutData);
			desktopLicense.setText(PREFERENCES.get(ProjectConstants.PARAM_USER_LICENSE_DESKTOP, ""));
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
		mobileLicense.setText(userMobileLicense);
		desktopLicense.setText(userDesktopLicense);
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
			!mobileLicense.getText().equals(userMobileLicense) ||
			!desktopLicense.getText().equals(userDesktopLicense) ||
			PREFERENCES.get(ProjectConstants.PARAM_USER_MAC_ADDRESS, "").isEmpty() || 
			PREFERENCES.get(ProjectConstants.PARAM_USER_PLUGIN_VERSION, "").isEmpty()) {
			changed = true;
		}
		
        // store modified settings
        ProjectData.persistOptIn(emailName.getText(), 
				uptodateCheckBox.getSelection(), 
				mobileLicense.getText(), 
				desktopLicense.getText());

        if (changed) {
			// send modified settings
	        com.gluonhq.plugin.templates.OptInHelper.optIn(emailName.getText(), 
	            	uptodateCheckBox.getSelection(), "plugineclipse", 
	                PREFERENCES.get(ProjectConstants.PARAM_USER_MAC_ADDRESS, ""),
	                PREFERENCES.get(ProjectConstants.PARAM_USER_PLUGIN_VERSION, ""), true);
        }
        
		this.userEmail = PREFERENCES.get(ProjectConstants.PARAM_USER_EMAIL, "");
		this.userUptodate = PREFERENCES.getBoolean(ProjectConstants.PARAM_USER_UPTODATE, true);
		this.userMobileLicense = PREFERENCES.get(ProjectConstants.PARAM_USER_LICENSE_MOBILE, "");
		this.userDesktopLicense = PREFERENCES.get(ProjectConstants.PARAM_USER_LICENSE_DESKTOP, "");

		return super.performOk();
	}

	@Override
	protected void createFieldEditors() {
		// TODO Auto-generated method stub	
	}
	
}