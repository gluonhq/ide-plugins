package com.gluonhq.eclipse.plugin.wizard.mobile.multiviewfxml;

import com.gluonhq.eclipse.plugin.wizard.*;
import com.gluonhq.plugin.templates.GluonProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;

import java.util.HashMap;
import java.util.Map;

public class GluonProjectMultiViewFXMLWizard extends GluonProjectWizard {

	private final Map<String, Object> parameters = new HashMap<>();
	private ConfigureOptInProjectPage pageZero;
	private ConfigureGluonProjectPage pageOne;
	private ConfigureSampleClassPage pageTwo;
	private ConfigureViewsProjectPage pageThree;

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	public GluonProjectMultiViewFXMLWizard() {
		super(GluonProject.MOBILE_MULTIVIEWFXML);
	}

	@Override
	public void addPages() {
		super.addPages();

		if (!ProjectData.alreadyOptedIn()) {
			pageZero = new ConfigureOptInProjectPage(getProjectData());
			addPage(pageZero);
		}
		
		pageOne = new ConfigureGluonProjectPage(getProjectData());
		addPage(pageOne);
		
		pageTwo = new ConfigureSampleClassPage(getProjectData());
		addPage(pageTwo);
		
		pageThree = new ConfigureViewsProjectPage(getProjectData(), true, false);
		addPage(pageThree);
	}

	public void updateParameter(String key, Object value) {
		parameters.put(key, value);
	}

	@Override
	public boolean canFinish() {
		if (getContainer().getCurrentPage() == pageThree) {
			return true;
		}
		return false;
	}	
}
