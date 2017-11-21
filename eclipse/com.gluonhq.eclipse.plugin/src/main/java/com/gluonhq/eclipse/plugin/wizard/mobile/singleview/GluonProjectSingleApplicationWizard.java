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
package com.gluonhq.eclipse.plugin.wizard.mobile.singleview;

import com.gluonhq.eclipse.plugin.wizard.*;
import com.gluonhq.plugin.templates.GluonProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;

import java.util.HashMap;
import java.util.Map;

public class GluonProjectSingleApplicationWizard extends GluonProjectWizard {

	private final Map<String, Object> parameters = new HashMap<>();
	private ConfigureOptInProjectPage pageZero;
	private ConfigureGluonProjectPage pageOne;
	private ConfigureSampleClassPage pageTwo;

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	public GluonProjectSingleApplicationWizard() {
		super(GluonProject.MOBILE_SINGLE);
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
	}

	public void updateParameter(String key, Object value) {
		parameters.put(key, value);
	}

	@Override
	public boolean canFinish() {
		if (getContainer().getCurrentPage() == pageTwo) {
			return true;
		}
		return false;
	}
}
