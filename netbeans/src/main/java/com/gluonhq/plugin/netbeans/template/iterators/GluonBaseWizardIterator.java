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
package com.gluonhq.plugin.netbeans.template.iterators;

import com.gluonhq.plugin.netbeans.template.OptInHelper;
import com.gluonhq.plugin.templates.ProjectConstants;
import com.gluonhq.plugin.templates.Template;
import com.gluonhq.plugin.templates.TemplateManager;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.*;

public abstract class GluonBaseWizardIterator implements WizardDescriptor.InstantiatingIterator {
    
    private final String typeProject;
    private int index;
    private WizardDescriptor.Panel[] panels;
    private WizardDescriptor wiz;

    public GluonBaseWizardIterator(String typeProject) {
        this.typeProject = typeProject;
    }

    @Override
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;
        panels = createPanels();
        // Make sure list of steps is accurate.
        String[] steps = createSteps();
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                // TODO if using org.openide.dialogs >= 7.8, can use WizardDescriptor.PROP_*:
                jc.putClientProperty("WizardPanel_contentSelectedIndex", i);
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps);
            }
        }
    }
    
    @Override
    public Set/*<FileObject>*/ instantiate(/*ProgressHandle handle*/) throws IOException {
        if (!OptInHelper.alreadyOptedIn()) {
            OptInHelper.persistOptIn((String) wiz.getProperty(ProjectConstants.PARAM_USER_EMAIL), 
                    (Boolean) wiz.getProperty(ProjectConstants.PARAM_USER_UPTODATE), 
                    (String) wiz.getProperty(ProjectConstants.PARAM_USER_LICENSE_MOBILE), 
                    (String) wiz.getProperty(ProjectConstants.PARAM_USER_LICENSE_DESKTOP));
            
            // only send once
            com.gluonhq.plugin.templates.OptInHelper.optIn((String) wiz.getProperty(ProjectConstants.PARAM_USER_EMAIL), 
                (Boolean) wiz.getProperty(ProjectConstants.PARAM_USER_UPTODATE), "pluginnetbeans", 
                (String) wiz.getProperty(ProjectConstants.PARAM_USER_MAC_ADDRESS),
                (String) wiz.getProperty(ProjectConstants.PARAM_USER_PLUGIN_VERSION), false);
        }
        
        wiz.putProperty(ProjectConstants.PARAM_IDE, ProjectConstants.IDE_NETBEANS);
        wiz.putProperty(ProjectConstants.PARAM_JAVAFX_VERSION, ProjectConstants.getJavaFXVersion());
        wiz.putProperty(ProjectConstants.PARAM_JAVAFX_MAVEN_PLUGIN, ProjectConstants.getJavaFXMavenPluginVersion());
        wiz.putProperty(ProjectConstants.PARAM_JAVAFX_GRADLE_PLUGIN, ProjectConstants.getJavaFXGradlePluginVersion());
        wiz.putProperty(ProjectConstants.PARAM_GLUON_DESKTOP_VERSION, ProjectConstants.getDesktopVersion());
        wiz.putProperty(ProjectConstants.PARAM_GLUON_DESKTOP_VERSION, ProjectConstants.getDesktopVersion());
        wiz.putProperty(ProjectConstants.PARAM_GLUON_MOBILE_VERSION, ProjectConstants.getMobileVersion());
        wiz.putProperty(ProjectConstants.PARAM_GLUON_ATTACH_VERSION, ProjectConstants.getAttachVersion());
        wiz.putProperty(ProjectConstants.PARAM_GLUON_CLIENT_MAVEN_PLUGIN, ProjectConstants.getClientMavenPluginVersion());
        wiz.putProperty(ProjectConstants.PARAM_GLUON_CLIENT_GRADLE_PLUGIN, ProjectConstants.getClientGradlePluginVersion());
        wiz.putProperty(ProjectConstants.PARAM_GLUON_GLISTEN_AFTERBURNER_VERSION, ProjectConstants.getGlistenAfterburnerVersion());
        
        Set<FileObject> resultSet = new LinkedHashSet<>();
        File dirF = FileUtil.normalizeFile((File) wiz.getProperty(ProjectConstants.PARAM_PROJECT_DIR));
        dirF.mkdirs();

        List<File> filesToOpen = new ArrayList<>();
        TemplateManager templateManager = TemplateManager.getInstance();
        Template template = templateManager.getProjectTemplate(typeProject);

        template.render(dirF, wiz.getProperties());
        filesToOpen.addAll(template.getFilesToOpen());

        FileObject dir = FileUtil.toFileObject(dirF);

        // Always open top dir as a project:
        resultSet.add(dir);
        // Look for nested projects to open as well:
        Enumeration<? extends FileObject> e = dir.getFolders(true);
        while (e.hasMoreElements()) {
            FileObject subfolder = e.nextElement();
            if (ProjectManager.getDefault().isProject(subfolder)) {
                resultSet.add(subfolder);
            }
        }

        File parent = dirF.getParentFile();
        if (parent != null && parent.exists()) {
            ProjectChooser.setProjectsFolder(parent);
        }

        // create template sources
        Template sourceTemplate = templateManager.getSourceTemplate(template.getProjectName());
        if (sourceTemplate != null) {
            sourceTemplate.render(dirF, wiz.getProperties());
            filesToOpen.addAll(sourceTemplate.getFilesToOpen());
        }

        if (!filesToOpen.isEmpty()) {
            for (File file : filesToOpen) {
                if (file.exists()) {
                    try {
                        DataObject.find(FileUtil.toFileObject(FileUtil.normalizeFile(file))).getLookup().lookup(OpenCookie.class).open();
                    } catch (DataObjectNotFoundException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
        
        return resultSet;
    }
    
    protected void restoreOptIn() {
        OptInHelper.restoreOptIn(wiz);
    }
    
    protected abstract WizardDescriptor.Panel[] createPanels();
    
    protected abstract String[] createSteps();

    @Override
    public void uninitialize(WizardDescriptor wiz) {
        this.wiz.putProperty(ProjectConstants.PARAM_PROJECT_DIR, null);
        this.wiz.putProperty(ProjectConstants.PARAM_PROJECT_NAME, null);
        this.wiz = null;
        panels = null;
    }

    @Override
    public String name() {
        return MessageFormat.format("{0} of {1}",
                new Object[]{index + 1, panels.length});
    }

    @Override
    public boolean hasNext() {
        return index < panels.length - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    @Override
    public WizardDescriptor.Panel current() {
        return panels[index];
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    @Override
    public final void addChangeListener(ChangeListener l) {
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
    }
    
}
