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
package org.gradle.plugins.nbm

import org.gradle.api.internal.ConventionTask
import org.gradle.api.tasks.TaskAction

class MergePropertiesTask extends ConventionTask {

    private NbmPluginExtension netbeansExt() {
        project.extensions.nbm
    }

    MergePropertiesTask() {
        configure { ConventionTask it ->
            def generatedClasses = "${project.buildDir}/generated-resources/main"
            def generatedResources = "${project.buildDir}/generated-resources/resources"
            // def generatedOutput = "${project.buildDir}/generated-resources/output"
            outputs.dir(new File(project.buildDir, '/generated-resources/output'))
            inputs.dir generatedClasses
            inputs.dir generatedResources
            dependsOn project.tasks.findByName('compileJava')
            dependsOn project.tasks.findByName('processResources')
        }
    }

    @TaskAction
    void generate() {
        def generatedClasses = "${project.buildDir}/generated-resources/main"
        def generatedResources = "${project.buildDir}/generated-resources/resources"
        def generatedOutput = "${project.buildDir}/generated-resources/output"
        new File(generatedOutput).mkdirs()

        Set<String> paths = new HashSet<>()
        def genProperties = project.fileTree(dir: generatedClasses)
        def userProperties = project.fileTree(dir: generatedResources)

        genProperties.visit { if (!it.file.isDirectory()) paths.add(it.relativePath.pathString) }
        userProperties.visit { if (!it.file.isDirectory()) paths.add(it.relativePath.pathString) }
        paths.each { String path ->
            // if in both merge else copy
            def dest = new File(generatedOutput, path).parentFile
            dest.mkdirs()
            if (!new File(generatedClasses, path).exists()) {
                project.copy {
                    from new File(generatedResources, path)
                    into dest
                }
            } else if (!new File(generatedResources, path).exists()) {
                project.copy {
                    from new File(generatedClasses, path)
                    into dest
                }
            } else {
                def destFile = new File(generatedOutput, path)
                destFile << new File(generatedClasses, path).text +
                        '\n' +
                        new File(generatedResources, path).text
            }
        }
    }
}
