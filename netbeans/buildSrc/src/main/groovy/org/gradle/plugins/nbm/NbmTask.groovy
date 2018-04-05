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

import org.apache.tools.ant.taskdefs.Taskdef
import org.apache.tools.ant.types.Path
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.ConventionTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

class NbmTask extends ConventionTask {
    public NbmTask() {
        outputs.upToDateWhen { false }
    }

    @OutputFile
    File getOutputFile() {
        def moduleJarName = netbeansExt().moduleName.replace('.', '-')
        new File(getNbmBuildDir(), moduleJarName + '.nbm')
    }

    @OutputDirectory
    File nbmBuildDir

    @InputFiles
    FileCollection getModuleFiles() {
        project.files(project.tasks.netbeans.getModuleBuildDir()).builtBy(project.tasks.netbeans)
    }

    private NbmPluginExtension netbeansExt() {
        project.extensions.nbm
    }

    @TaskAction
    void generate() {
        project.logger.info "NbmTask running"
        def nbmFile = getOutputFile()
        def nbmDir = getNbmBuildDir()
        if (!nbmDir.isDirectory()) {
            nbmDir.mkdirs()
        }

        NbmPluginExtension nbm = netbeansExt();

        def moduleJarName = nbm.moduleName.replace('.', '-')

        def makenbm = antBuilder().antProject.createTask("makenbm")
        makenbm.productDir = project.tasks.netbeans.getModuleBuildDir()
        makenbm.file = nbmFile
        makenbm.module = "modules" + File.separator + moduleJarName + ".jar"
        makenbm.homepage = 'http://gluonhq.com/products/tools/'
        makenbm.moduleauthor = 'Gluon <support@gluonhq.com>'
        makenbm.createLicense().file = new File(project.projectDir, 'license.txt')

        NbmKeyStoreDef keyStore = nbm.keyStore
        def keyStoreFile = EvaluateUtils.asPath(keyStore.keyStoreFile)
        if (keyStoreFile != null) {
            def signature = makenbm.createSignature()
            signature.keystore = keyStoreFile.toFile()
            signature.alias = keyStore.username
            signature.storepass = keyStore.password
        }
        makenbm.execute()
    }

    private AntBuilder antBuilder() {
        def antProject = ant.antProject
        ant.project.getBuildListeners().firstElement().setMessageOutputLevel(3)
        Taskdef taskdef = antProject.createTask("taskdef")
        taskdef.classname = "org.netbeans.nbbuild.MakeNBM"
        taskdef.name = "makenbm"
        taskdef.classpath = new Path(antProject, netbeansExt().harnessConfiguration.asPath)
        taskdef.execute()
        return getAnt();
    }
}

