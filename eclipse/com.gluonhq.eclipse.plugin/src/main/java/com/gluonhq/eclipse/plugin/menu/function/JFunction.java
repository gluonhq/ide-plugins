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
package com.gluonhq.eclipse.plugin.menu.function;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.JFrame;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ISetSelectionTarget;

import com.gluonhq.eclipse.plugin.menu.ProjectUtils;
import com.gluonhq.plugin.function.Function;
import com.gluonhq.plugin.function.FunctionFX;
import com.gluonhq.plugin.templates.GluonProject;
import com.gluonhq.plugin.templates.ProjectConstants;
import com.gluonhq.plugin.templates.Template;
import com.gluonhq.plugin.templates.TemplateManager;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;

public class JFunction extends JFrame {
	
	private static final long serialVersionUID = 3225873452245298682L;
	
	private final ProjectUtils utils;
    private final IContainer project;
    private List<String> lines = null;

    public JFunction(ProjectUtils utils, IContainer project) {
        this.utils = utils;
        this.project = project;

        getContentPane().add(runFunctionFX());
    }

    private JFXPanel runFunctionFX() {
        final JFXPanel fxPanel = new JFXPanel();
        Platform.setImplicitExit(false);
        Platform.runLater(() -> {
            FunctionFX functionFX = new FunctionFX();
            Function function = functionFX.getFunction();
            function.addPropertyChangeListener(e -> {
                if (function.getFunctionName() != null && function.getPackageName() != null) {
					GluonSubProject gluonSubProject = new GluonSubProject("Gluon Function", function);
					gluonSubProject.setUser(true);
					gluonSubProject.schedule();
                }
                dispose();
            });

            final Scene scene = new Scene(functionFX);
            fxPanel.setScene(scene);
        });
        return fxPanel;
    }
    
    /**
	 * Initializes a new Gluon subproject 
	 */
	private final class GluonSubProject extends Job {
		
		private final Function function;
		
		public GluonSubProject(String jobName, Function function) {
			super(jobName);
			this.function = function;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask("Add Gluon subProject", 100);
			try {
				File rootFile = new File(utils.getRootProject().getLocationURI());
				final String fnProjectName = rootFile.getName() + function.getFunctionName();
				monitor.worked(10);

				// Create and open project

				IWorkspace workspace = ResourcesPlugin.getWorkspace();
		        IProjectDescription projectDescription = workspace.newProjectDescription(fnProjectName);
	            projectDescription.setLocation(utils.getRootProject().getLocation().append(File.separator).append(fnProjectName));
	            projectDescription.setComment(String.format("Gluon subproject %s", fnProjectName));
	            IProject fnProject = workspace.getRoot().getProject(fnProjectName);
	            try {
					fnProject.create(projectDescription, monitor);
					fnProject.open(IResource.BACKGROUND_REFRESH, monitor);
				} catch (CoreException e) {
					e.printStackTrace();
				}
	            monitor.worked(20);
		    	
				// Run Template
		        
		        Map<String, Object> parameters = new HashMap<>();
		        parameters.put(ProjectConstants.PARAM_GLUON_FUNCTION_NAME, function.getFunctionName());
		        parameters.put(ProjectConstants.PARAM_GLUON_FUNCTION_METHOD_NAME, function.getMethodName());
		        parameters.put(ProjectConstants.PARAM_PACKAGE_NAME, function.getPackageName());
		        parameters.put(ProjectConstants.PARAM_PACKAGE_FOLDER, function.getPackageName().replaceAll("\\.", "/"));
		        parameters.put(ProjectConstants.PARAM_GLUON_FUNCTION_PROJECT_NAME, fnProjectName);
		    
		        List<File> filesToOpen = new ArrayList<>();
		        TemplateManager templateManager = TemplateManager.getInstance();
		        Template template = templateManager.getProjectTemplate(GluonProject.FUNCTION.getType());
		
		        template.render(rootFile, parameters);
		        filesToOpen.addAll(template.getFilesToOpen());
		
		        monitor.worked(30);
		    	
		        // create template sources
		        Template sourceTemplate = templateManager.getSourceTemplate(template.getProjectName());
		        if (sourceTemplate != null) {
		            sourceTemplate.render(rootFile, parameters);
		            filesToOpen.addAll(sourceTemplate.getFilesToOpen());
		        }
		        monitor.worked(45);
		    	
		        // Add include to settings.build
		
		        final IFile settingsFile = ProjectUtils.getGradleSettingsFile(utils.getRootProject());
		        Path path = null;
		        if (settingsFile != null) {
					try {
			            path = Paths.get(settingsFile.getLocationURI());
						lines = Files.readAllLines(path);
			        } catch (IOException ex) {
			            ex.printStackTrace();
			        }  
			    }
		        
		        if (path != null && lines != null && !lines.isEmpty()) {
		            int includeLines = (int) IntStream.range(0, lines.size())
		                    .filter(i -> lines.get(i).startsWith("include"))
		                    .count();
		
		            List<String> newList = IntStream.range(0, includeLines)
		                    .mapToObj(lines::get)
		                    .collect(Collectors.toList());
		            newList.add("include '" +  fnProjectName + "'");
		            newList.addAll(IntStream.range(includeLines + 1, lines.size())
		                    .mapToObj(lines::get)
		                    .collect(Collectors.toList()));
		            try {
		                Files.write(path, newList, StandardCharsets.UTF_8);
						settingsFile.refreshLocal(IResource.DEPTH_ZERO, null);
					} catch (IOException | CoreException e) {
						e.printStackTrace();
					}
		            monitor.worked(55);
		        }
		        
		        ProjectUtils.refreshProject(project);
		        monitor.worked(65);
		        
		        // Open function class
		        
		        if (! filesToOpen.isEmpty()) {
					Display.getDefault().asyncExec(() -> {
						IWorkbench workbench = PlatformUI.getWorkbench();
						IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
						if (activeWorkbenchWindow != null) {
							IWorkbenchPage page = activeWorkbenchWindow.getActivePage();

							filesToOpen.stream()
								.filter(File::exists)
								.map(file -> org.eclipse.core.runtime.Path.fromOSString(file.getAbsolutePath()))
								.map(iPath -> workspace.getRoot().getFileForLocation(iPath))
								.forEach(iFile -> {
									try {
										iFile.refreshLocal(IResource.DEPTH_ZERO, null);
										// open file
										IEditorDescriptor fileDescriptor = workbench.getEditorRegistry().getDefaultEditor(iFile.getName());
										IDE.openEditor(page, iFile, fileDescriptor == null ?  "org.eclipse.ui.DefaultTextEditor" : fileDescriptor.getId(), false);
									} catch (CoreException e) {
										e.printStackTrace();
									}
									// expand project explorer
									IViewPart view = page.findView(IPageLayout.ID_PROJECT_EXPLORER);
									if (view != null && view instanceof ISetSelectionTarget) {
										((ISetSelectionTarget) view).selectReveal(new StructuredSelection(iFile));
									}
								});
						}
					});
		        }
		        
		        monitor.worked(85);
		    	
		        
		        // TODO: build and upload gluon function
	            //ExecuteUploadFunction executeUpload = new ExecuteUploadFunction(fnProject);
	            //executeUpload.execute();
		        
		        monitor.worked(100);
		    	
			} finally {
				monitor.done();
			}
			return Status.OK_STATUS;
		}
	}
}
