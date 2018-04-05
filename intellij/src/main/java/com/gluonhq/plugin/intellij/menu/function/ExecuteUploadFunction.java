/*
 * Copyright (c) 2018, Gluon Software
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
package com.gluonhq.plugin.intellij.menu.function;

import com.gluonhq.plugin.function.UploadFunction;
import com.gluonhq.plugin.intellij.menu.ProjectUtils;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.externalSystem.model.execution.ExternalSystemTaskExecutionSettings;
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId;
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListenerAdapter;
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode;
import com.intellij.openapi.externalSystem.service.notification.ExternalSystemProgressNotificationManager;
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.gradle.util.GradleConstants;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Properties;

public class ExecuteUploadFunction {

    private final VirtualFile functionProjectFile;
    private final String ideKey;
    private final Properties gradleProperties;
    private final Project project;

    public ExecuteUploadFunction(Project project, VirtualFile file) {
        this.project = project;
        this.functionProjectFile = file;

        ProjectUtils utils = new ProjectUtils(functionProjectFile);
        ideKey = utils.getCloudLinkIdeKey();

        gradleProperties = new Properties();

        VirtualFile gradlePropertiesFile = functionProjectFile.findFileByRelativePath("gradle.properties");
        if (gradlePropertiesFile != null && gradlePropertiesFile.exists()) {
            try (FileReader gradlePropertiesReader = new FileReader(new File(gradlePropertiesFile.getPath()))) {
                gradleProperties.load(gradlePropertiesReader);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Properties file not found");
        }
    }

    public void execute() {
        ExternalSystemTaskExecutionSettings settings = new ExternalSystemTaskExecutionSettings();
        settings.setExternalProjectPath(functionProjectFile.getPath());
        settings.setTaskNames(Collections.singletonList(":" + functionProjectFile.getName() + ":gfBundle"));
        settings.setExternalSystemIdString(GradleConstants.SYSTEM_ID.getId());
        ExternalSystemProgressNotificationManager notificationManager = ServiceManager.getService(ExternalSystemProgressNotificationManager.class);

        ExternalSystemTaskNotificationListenerAdapter listener = new ExternalSystemTaskNotificationListenerAdapter() {

            @Override
            public void onTaskOutput(@NotNull ExternalSystemTaskId id, @NotNull String text, boolean stdOut) {
                System.out.print(text);
            }

            @Override
            public void onEnd(@NotNull ExternalSystemTaskId id) {
                super.onEnd(id);
                notificationManager.removeNotificationListener(this);
                System.out.println(":gfBundle: uploading gfBundle.zip");
                File zip = new File(functionProjectFile.getPath() + "/build/distributions/gfBundle.zip");
                if (zip != null && zip.exists()) {
                    try {
                        UploadFunction.upload(ideKey, gradleProperties.getProperty("gfName"),
                                gradleProperties.getProperty("gfEntrypoint"), zip);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Zip file not found");
                }
            }
        };
        notificationManager.addNotificationListener(listener);
        ExternalSystemUtil.runTask(settings, DefaultRunExecutor.EXECUTOR_ID, project, GradleConstants.SYSTEM_ID, null, ProgressExecutionMode.START_IN_FOREGROUND_ASYNC);
    }
}
