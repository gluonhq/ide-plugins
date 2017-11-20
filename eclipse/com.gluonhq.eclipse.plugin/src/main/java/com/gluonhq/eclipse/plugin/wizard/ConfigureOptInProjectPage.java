package com.gluonhq.eclipse.plugin.wizard;

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

import com.gluonhq.plugin.templates.ProjectConstants;
import com.gluonhq.plugin.templates.TemplateUtils;

public class ConfigureOptInProjectPage extends WizardPage {

	public static final ImageDescriptor WIZBAN_IMAGE = ImageDescriptor.createFromURL(ConfigureOptInProjectPage.class.getClassLoader().getResource("icons/wizban/basic_wizard.png"));

	private Text emailName;
	private Button uptodateCheckBox;
	private Text mobileLicense;
	private Text desktopLicense;
	
	private final ProjectData projectData;

	public ConfigureOptInProjectPage(ProjectData projectData) {
		super("GluonApplicationSettings", "Register with Gluon", WIZBAN_IMAGE);

		this.projectData = projectData;
	}

	private void validate() {
		if (!TemplateUtils.isValidEmail(projectData.userEmail)) {
			setErrorMessage(projectData.userEmail + " is not a valid email address.");
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
			l.setText( "This plugin is completely free of charge. We only ask you to enter your email address" );
			GridData gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
			gridData.horizontalSpan = 2;
			l.setLayoutData(gridData);
			
		}
		
		{
			Label l = new Label( container, SWT.NONE );
			l.setText( "Email address:" );
			
			emailName = new Text(container, SWT.BORDER);
			GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
			layoutData.grabExcessHorizontalSpace = true;
			emailName.setLayoutData(layoutData);
			emailName.setText(projectData.userEmail);
			emailName.addModifyListener(new ModifyListener() {
				
				@Override
				public void modifyText(ModifyEvent e) {
					projectData.userEmail = emailName.getText();
					validate();
				}
			});
		}

		{
			Label l = new Label( container, SWT.NONE );
			l.setText( "" );
			
			uptodateCheckBox = new Button(container, SWT.CHECK);
			uptodateCheckBox.setText(" Keep me up-to-date ");
			GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
			layoutData.grabExcessHorizontalSpace = true;
			uptodateCheckBox.setLayoutData(layoutData);
			uptodateCheckBox.setSelection(projectData.userUptodate);
			uptodateCheckBox.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}

				@Override
				public void widgetSelected(SelectionEvent arg0) {
					projectData.userUptodate = uptodateCheckBox.getSelection();
					validate();
				}
			});
			
		}
		
		{
			Label l = new Label(container, SWT.NONE);
			l.setText("Enter your Gluon Mobile and/or Gluon Desktop license keys. (OPTIONAL)");
			GridData gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
			gridData.horizontalSpan = 2;
			l.setLayoutData(gridData);
			
		}
		
		{
			Label l = new Label(container, SWT.NONE);
			l.setText("Mobile License Key:");
			
			mobileLicense = new Text(container, SWT.BORDER);
			GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
			layoutData.grabExcessHorizontalSpace = true;
			mobileLicense.setLayoutData(layoutData);
			mobileLicense.setText(projectData.userMobileLicense);
			mobileLicense.addModifyListener(new ModifyListener() {
				
				@Override
				public void modifyText(ModifyEvent e) {
					projectData.userMobileLicense = mobileLicense.getText();
					validate();
				}
			});
		}

		{
			Label l = new Label(container, SWT.NONE);
			l.setText("Desktop License Key:");
			
			desktopLicense = new Text(container, SWT.BORDER);
			GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
			layoutData.grabExcessHorizontalSpace = true;
			desktopLicense.setLayoutData(layoutData);
			desktopLicense.setText(projectData.userDesktopLicense);
			desktopLicense.addModifyListener(new ModifyListener() {
				
				@Override
				public void modifyText(ModifyEvent e) {
					projectData.userDesktopLicense = desktopLicense.getText();
					validate();
				}
			});
		}

		projectData.userMacAddress = TemplateUtils.getMacAddress();
		projectData.userPluginVersion = ProjectConstants.PLUGIN_VERSION;
		
		setControl(container);
		// without valid email
		setPageComplete(false);
	}

}
