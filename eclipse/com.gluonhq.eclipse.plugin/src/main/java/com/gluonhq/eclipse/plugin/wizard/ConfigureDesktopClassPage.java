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
