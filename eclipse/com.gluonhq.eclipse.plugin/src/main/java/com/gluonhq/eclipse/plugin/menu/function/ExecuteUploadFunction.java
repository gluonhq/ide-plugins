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
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.configuration.BuildConfiguration;
import org.eclipse.buildship.core.internal.configuration.RunConfiguration;
import org.eclipse.buildship.core.internal.console.ProcessStreams;
import org.eclipse.buildship.core.internal.gradle.GradleProgressAttributes;
import org.eclipse.buildship.core.internal.util.progress.DelegatingProgressListener;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.CancellationToken;
import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProgressListener;

import com.gluonhq.eclipse.plugin.menu.ProjectUtils;
import com.gluonhq.plugin.function.UploadFunction;
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharStreams;

public class ExecuteUploadFunction {

    private final IContainer project;
    private final ProjectUtils utils;

    public ExecuteUploadFunction(IContainer project) {
        this.project = project;
        this.utils = new ProjectUtils(project);
    }

    public void execute() {
        BuildClass build = new BuildClass("GFBundle task");
        build.setUser(true);
        build.addJobChangeListener(new JobChangeAdapter() {
            @Override
            public void done(IJobChangeEvent event) {
                super.done(event);
                if (event.getResult().isOK()) {
                    File resource = new File(project.getRawLocationURI().getPath().concat("/build/distributions/gfBundle.zip"));
                    if (resource.exists()) {
                        Properties gradleProperties = getGradleProperties();
                        if (gradleProperties != null) {
                            try {
                                final String cloudLinkIdeKey = utils.getCloudLinkIdeKey();
                                UploadFunction.upload(cloudLinkIdeKey, gradleProperties.getProperty("gfName"),
                                gradleProperties.getProperty("gfEntrypoint"), resource);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });

        build.schedule();
    }
    	
    private Properties getGradleProperties() {
        Properties gradleProperties = new Properties();
        IResource resource = project.findMember("gradle.properties");
        if (resource != null && resource.exists()) {
            IFile gradlePropertiesFile = (IFile) resource;
            if (gradlePropertiesFile.exists()) {
                try (FileReader gradlePropertiesReader = new FileReader(new File(gradlePropertiesFile.getLocationURI()))) {
                    gradleProperties.load(gradlePropertiesReader);
                    return gradleProperties;
                } catch (IOException e) {
                    e.printStackTrace();
                } 
            }
        } 
        System.out.println("gradle resource not found");
        return null;
    }
    	
    private class BuildClass extends Job {
		
        public BuildClass(String name) {
            super(name);
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            BuildConfiguration buildConfig = CorePlugin.configurationManager()
                    .loadBuildConfiguration(new File(project.getRawLocationURI()));

            RunConfiguration runConfiguration = CorePlugin.configurationManager().createDefaultRunConfiguration(buildConfig);
            
            BuildLauncher launcher = CorePlugin.internalGradleWorkspace().getGradleBuild(buildConfig)
                            .newBuildLauncher(runConfiguration, 
                                            getGradleProgressAttributes(GradleConnector.newCancellationTokenSource(), monitor));
            launcher.forTasks(":" + project.getName() + ":gfBundle").run();
			
            return Status.OK_STATUS;
        }

        private GradleProgressAttributes getGradleProgressAttributes(CancellationTokenSource tokenSource, IProgressMonitor monitor) {
            return GradleProgressAttributes.builder(tokenSource, monitor)
            		.withFullProgress()
            		.build();
        }

    }
}
