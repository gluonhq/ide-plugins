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
package com.gluonhq.plugin.netbeans.menu.function;

public class ExecuteUploadFunction {

    /*private final Project project;
    private final GradleCommandExecutor executor;

    public ExecuteUploadFunction(Project project) {
        this.project = project;
        this.executor = project.getLookup().lookup(GradleCommandExecutor.class);
    }

    public void execute() {
        ProjectUtils projectUtils = new ProjectUtils(project);
        if (projectUtils.isGluonFunctionProject() && projectUtils.getMobileProject() != null) {
            GradleCommandTemplate.Builder command
                    = new GradleCommandTemplate.Builder("", Arrays.asList(":" + project.getProjectDirectory().getName() + ":gfBundle"));

            CustomCommandActions.Builder custom
                    = new CustomCommandActions.Builder(TaskKind.OTHER);
            custom.setCommandCompleteListener(new CommandCompleteListener() {
                @Override
                public void onComplete(@Nullable Throwable error) {
                    if (error != null) {
                        error.printStackTrace();
                    } else {
                        Path gfBundle = FileUtil.toFile(project.getProjectDirectory()).toPath().resolve("build/distributions/gfBundle.zip");
                        if (Files.exists(gfBundle)) {
                            Path gradlePropertiesFile = FileUtil.toFile(project.getProjectDirectory()).toPath().resolve("gradle.properties");
                            if (Files.exists(gradlePropertiesFile)) {
                                Properties gradleProperties = new Properties();
                                try (FileReader gradlePropertiesReader = new FileReader(gradlePropertiesFile.toFile())) {
                                    gradleProperties.load(gradlePropertiesReader);

                                    UploadFunction.upload(projectUtils.getCloudLinkIdeKey(), gradleProperties.getProperty("gfName"),
                                            gradleProperties.getProperty("gfEntrypoint"),
                                            gfBundle.toFile());
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                    // TODO: show error to user??
                                }
                            }
                        }
                    }
                }
            });

            executor.executeCommand(command.create(), custom.create());
        }
    }*/
}
