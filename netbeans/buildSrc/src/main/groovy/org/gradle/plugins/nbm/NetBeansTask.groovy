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
import org.apache.tools.ant.types.FileSet
import org.apache.tools.ant.types.Path
import org.gradle.api.file.CopySpec
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTreeElement
import org.gradle.api.internal.ConventionTask
import org.gradle.api.tasks.*

import java.util.jar.Attributes
import java.util.jar.JarFile

class NetBeansTask extends ConventionTask {
    public static final String TEST_USER_DIR_NAME = 'testuserdir'

    private FileCollection classpath

    @OutputDirectory
    File moduleBuildDir

    public NetBeansTask() {
        outputs.upToDateWhen { false }
    }

    private NbmPluginExtension netbeansExt() {
        project.extensions.nbm
    }

    @Input
    File getInputModuleJarFile() {
        project.tasks.jar.archivePath
    }

    @InputFiles @Optional
    FileCollection getClasspath() {
        return classpath
    }

    /**
     * Sets the classpath to include in the module content.
     *
     * @param classpath The classpath. Must not be null.
     */
    void setClasspath(Object classpath) {
        this.classpath = project.files(classpath)
    }

    /**
     * Adds files to the classpath to include in the module content.
     *
     * @param classpath The files to add. These are evaluated as per {@link org.gradle.api.Project#files(Object [])}
     */
    void classpath(Object... classpath) {
        FileCollection oldClasspath = getClasspath()
        this.classpath = project.files(oldClasspath ?: [], classpath)
    }

    private File getCacheDir() {
        File result = project.buildDir
        result = new File(result, TEST_USER_DIR_NAME)
        result = new File(result, 'var')
        result = new File(result, 'cache')
        return result
    }

    @TaskAction
    void generate() {
        project.logger.info "NetBeansTask running"
        // nbmFile.write "Version: ${getVersion()}"
        def moduleDir = getModuleBuildDir()
        if (!moduleDir.isDirectory()) {
            moduleDir.mkdirs()
        }
        // TODO handle eager/autoload
        def modulesDir = new File(moduleDir, 'modules')
        def modulesExtDir = new File(modulesDir, 'ext')

        project.delete(getCacheDir());

        def moduleJarName = netbeansExt().moduleName.replace('.', '-') + '.jar'
        project.copy { CopySpec it ->
            it.from(inputModuleJarFile)
            it.into(modulesDir)
            it.rename('.*\\.jar', moduleJarName)
        }
        project.copy { CopySpec it ->
            it.from(classpath)
            it.into(modulesExtDir)
            it.exclude { FileTreeElement fte ->
                if (fte.directory) return true
                if (!fte.name.endsWith('jar')) return true

                JarFile jar = new JarFile(fte.file)
                def attrs = jar.manifest.mainAttributes
                def attrValue = attrs.getValue(new Attributes.Name('OpenIDE-Module'))
                attrValue != null
            }
        }

        AntBuilder antBuilder = antBuilder()
        def moduleXmlTask = antBuilder.antProject.createTask('module-xml')
        moduleXmlTask.xmldir = new File(moduleDir, 'config' + File.separator + 'Modules')
        FileSet moduleFileSet = new FileSet()
        moduleFileSet.setDir(moduleDir)
        moduleFileSet.setIncludes('modules' + File.separator + moduleJarName)
        moduleXmlTask.addEnabled(moduleFileSet)
        moduleXmlTask.execute()

        def nbTask = antBuilder.antProject.createTask('genlist')
        nbTask.outputfiledir = moduleDir
        nbTask.module = 'modules' + File.separator + moduleJarName
        FileSet fs = nbTask.createFileSet()
        fs.dir = moduleDir
        fs.setIncludes('**')
        nbTask.execute()
    }

    private AntBuilder antBuilder() {
        def antProject = ant.antProject
        ant.project.getBuildListeners().firstElement().setMessageOutputLevel(3)
        Taskdef taskdef = antProject.createTask("taskdef")
        taskdef.classname = "org.netbeans.nbbuild.MakeListOfNBM"
        taskdef.name = "genlist"
        taskdef.classpath = new Path(antProject, netbeansExt().harnessConfiguration.asPath)
        taskdef.execute()
        Taskdef taskdef2 = antProject.createTask("taskdef")
        taskdef2.classname = "org.netbeans.nbbuild.CreateModuleXML"
        taskdef2.name = "module-xml"
        taskdef2.classpath = new Path(antProject, netbeansExt().harnessConfiguration.asPath)
        taskdef2.execute()
        return getAnt();
    }
}

