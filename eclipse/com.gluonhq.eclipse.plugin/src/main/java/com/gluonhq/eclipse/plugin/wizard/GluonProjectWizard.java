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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.buildship.core.GradleCore;
import org.eclipse.buildship.core.GradleDistribution;
import org.eclipse.buildship.core.SynchronizationResult;
import org.eclipse.buildship.core.internal.DefaultGradleBuild;
import org.eclipse.buildship.core.internal.configuration.BuildConfiguration;
import org.eclipse.buildship.core.internal.operation.ToolingApiStatus;
import org.eclipse.buildship.core.internal.operation.ToolingApiStatus.ToolingApiStatusType;
import org.eclipse.buildship.core.internal.workspace.NewProjectHandler;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.UIPlugin;
import org.gradle.tooling.GradleConnector;

import com.gluonhq.eclipse.plugin.HelpContext;
import com.gluonhq.eclipse.plugin.UiPlugin;
import com.gluonhq.plugin.templates.GluonProject;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

public abstract class GluonProjectWizard extends Wizard implements INewWizard, HelpContextIdProvider {

	/**
     * The section name declaration for {@link org.eclipse.jface.dialogs.DialogSettings} where the import wizard stores its
     * preferences.
     *
     * @see org.eclipse.jface.dialogs.DialogSettings#getOrCreateSection(IDialogSettings, String)
     */
    private static final String WIZARD_ID = "com.gluonhq.eclipse.plugin.wizard"; //$NON-NLS-1$
	
	private final ProjectData projectData;
	private final ProjectImportConfiguration configuration;
	
	public GluonProjectWizard(GluonProject gluonProject) {
		try {
			// store the dialog settings on the wizard and use them to retrieve / persist the most
	        // recent values entered by the user
	        setDialogSettings(getOrCreateDialogSection(UiPlugin.getInstance().getDialogSettings()));
	        
			projectData = new ProjectData(gluonProject);	
			
			this.configuration = new ProjectImportConfiguration();
		} catch (Throwable t) {
			// log exception to workspace/.metadata/.log
			UIPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, UiPlugin.PLUGIN_ID, 
					"Error project " + gluonProject + " :: " + t.getMessage(), t));
			throw t;
		}
      
	}

	@Override
	public String getWindowTitle() {
		return "Create a new Gluon Project";
	}

	@Override
	public String getHelpContextId() {
		return HelpContext.PROJECT_CREATION;
	}

	@Override
	public void addPages() {
		// show progress bar when getContainer().run() is called
		setNeedsProgressMonitor(true);

		// disable help on all wizard pages
		setHelpAvailable(false);
	}

	@Override
	public boolean performFinish() {
		configuration.setProjectDir(new File(projectData.projectLocation));
		configuration.setOverwriteWorkspaceSettings(false);
		
		IDialogSettings dialogSettings = getDialogSettings();
		String gradleDistributionString = dialogSettings.get("gradle_distribution");
        Optional<File> gradleUserHome = getAbsoluteFile(dialogSettings.get("gradle_user_home"));
        Optional<File> javaHome = getAbsoluteFile(dialogSettings.get("java_home"));
        GradleDistribution distribution;
        try {
            distribution = GradleDistribution.fromString(gradleDistributionString);
        } catch (RuntimeException ignore) {
            distribution = GradleDistribution.fromBuild();
        }
        boolean applyWorkingSets = dialogSettings.get("apply_working_sets") != null && dialogSettings.getBoolean("apply_working_sets");
        List<String> workingSets = ImmutableList.copyOf(nullToEmpty(dialogSettings.getArray("working_sets")));
        boolean buildScansEnabled = dialogSettings.getBoolean("build_scans");
        boolean offlineMode = dialogSettings.getBoolean("offline_mode");
        boolean autoSync = dialogSettings.getBoolean("auto_sync");
        List<String> arguments = ImmutableList.copyOf(nullToEmpty(dialogSettings.getArray("arguments")));
        List<String> jvmArguments = ImmutableList.copyOf(nullToEmpty(dialogSettings.getArray("jvm_arguments")));
        boolean showConsoleView = dialogSettings.getBoolean("show_console_view");
        boolean showExecutionsView = dialogSettings.getBoolean("show_executions_view");

        configuration.setDistribution(distribution);
        configuration.setGradleUserHome(gradleUserHome.orNull());
        configuration.setJavaHomeHome(javaHome.orNull());
        configuration.setApplyWorkingSets(applyWorkingSets);
        configuration.setWorkingSets(workingSets);
        configuration.setBuildScansEnabled(buildScansEnabled);
        configuration.setOfflineMode(offlineMode);
        configuration.setAutoSync(autoSync);
        configuration.setArguments(arguments);
        configuration.setJvmArguments(jvmArguments);
        configuration.setShowConsoleView(showConsoleView);
        configuration.setShowExecutionsView(showExecutionsView);

		try {
			getContainer().run(true, true, new IRunnableWithProgress() {

                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    BuildConfiguration internalBuildConfiguration = configuration.toInternalBuildConfiguration();
                    boolean showExecutionsView = internalBuildConfiguration.getWorkspaceConfiguration().isShowExecutionsView();
                    org.eclipse.buildship.core.BuildConfiguration buildConfiguration = configuration.toBuildConfiguration();
                    org.eclipse.buildship.core.GradleBuild gradleBuild = GradleCore.getWorkspace().createBuild(buildConfiguration);

                    ImportWizardNewProjectHandler workingSetsAddingNewProjectHandler = new ImportWizardNewProjectHandler(NewProjectHandler.IMPORT_AND_MERGE, 
                    		configuration, showExecutionsView);
                    
                	GluonProjectApplicationOperation operation = new GluonProjectApplicationOperation(projectData);
                	operation.perform(monitor, buildConfiguration.getRootProjectDirectory().getAbsoluteFile());

                    SynchronizationResult result = ((DefaultGradleBuild)gradleBuild).synchronize(workingSetsAddingNewProjectHandler, GradleConnector.newCancellationTokenSource(), monitor);
                    if (!result.getStatus().isOK()) {
                        throw new InvocationTargetException(new CoreException(result.getStatus()));
                    }
                }
            });
        } catch (InvocationTargetException e) {
            ToolingApiStatus status = containerExceptionToToolingApiStatus(e);
            status.log();
        	return !ToolingApiStatusType.IMPORT_ROOT_DIR_FAILED.matches(status);
        } catch (InterruptedException ignored) {
            return false;
        }

        return true;
    }

    private ToolingApiStatus containerExceptionToToolingApiStatus(InvocationTargetException exception) {
        Throwable target = exception.getTargetException() == null ? exception : exception.getTargetException();
        if (target instanceof CoreException && ((CoreException) target).getStatus() instanceof ToolingApiStatus) {
           return (ToolingApiStatus) ((CoreException) target).getStatus();
        } else {
            return ToolingApiStatus.from("Project import", target);
        }
    }

	protected ProjectData getProjectData() {
		return projectData;
	}
	
	private static IDialogSettings getOrCreateDialogSection(IDialogSettings dialogSettings) {
        // in Eclipse 3.6 the method DialogSettings#getOrCreateSection does not exist
        IDialogSettings section = dialogSettings.getSection(WIZARD_ID);
        if (section == null) {
            section = dialogSettings.addNewSection(WIZARD_ID);
        }
        return section;
    }
	
	private static Optional<File> getAbsoluteFile(String path) {
        if (Strings.isNullOrEmpty(path)) {
            return Optional.absent();
        } else {
            return Optional.of(new File(path.trim()).getAbsoluteFile());
        }
    }
	
	private static String[] nullToEmpty(String[] array) {
        return array == null ? new String[0] : array;
    }
	
	/**
     * A delegating {@link NewProjectHandler} which adds workingsets to the imported projects and
     * ensures that the Gradle views are visible.
     *
     * @author Stefan Oehme
     */
    private static final class ImportWizardNewProjectHandler implements NewProjectHandler {

        private final ProjectImportConfiguration configuration;
        private final NewProjectHandler importedBuildDelegate;
        private final boolean showExecutionsView;

        private volatile boolean gradleViewsVisible;

        private ImportWizardNewProjectHandler(NewProjectHandler delegate, ProjectImportConfiguration configuration, boolean showExecutionsView) {
            this.importedBuildDelegate = delegate;
            this.configuration = configuration;
            this.showExecutionsView = showExecutionsView;
        }

        @Override
        public boolean shouldImportNewProjects() {
            return this.importedBuildDelegate.shouldImportNewProjects();
        }

        @Override
        public void afterProjectImported(IProject project) {
            this.importedBuildDelegate.afterProjectImported(project);
            addWorkingSets(project);
            ensureGradleViewsAreVisible();
        }

        private void addWorkingSets(IProject project) {
            List<String> workingSetNames = this.configuration.getApplyWorkingSets() ? ImmutableList.copyOf(this.configuration.getWorkingSets())
                    : ImmutableList.<String> of();
            IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
            IWorkingSet[] workingSets = toWorkingSets(workingSetNames);
            workingSetManager.addToWorkingSets(project, workingSets);
        }

        private void ensureGradleViewsAreVisible() {
            PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

                @Override
                public void run() {
                    if (!gradleViewsVisible) {
                        gradleViewsVisible = true;
                        showView("org.eclipse.buildship.ui.views.taskview", null, IWorkbenchPage.VIEW_ACTIVATE);
                        if (showExecutionsView) {
                            showView("org.eclipse.buildship.ui.views.executionview", null, IWorkbenchPage.VIEW_VISIBLE);
                        }
                    }
                }
            });
        }
        
        private static <T extends IViewPart> T showView(String viewId, String secondaryId, int mode) {
            IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            try {
                @SuppressWarnings("unchecked")
                T view = (T) activeWorkbenchWindow.getActivePage().showView(viewId, secondaryId, mode);
                return view;
            } catch (PartInitException e) {
                throw new RuntimeException(String.format("Cannot show view with id %s and secondary id %s.", viewId, secondaryId), e);
            }
        }
        
        private static IWorkingSet[] toWorkingSets(List<String> workingSetNames) {
            final IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
            return FluentIterable.from(workingSetNames).transform(new Function<String, IWorkingSet>() {

                @Override
                public IWorkingSet apply(String name) {
                    return workingSetManager.getWorkingSet(name);
                }
            }).filter(Predicates.notNull()).toArray(IWorkingSet.class);
        }
    }
}
