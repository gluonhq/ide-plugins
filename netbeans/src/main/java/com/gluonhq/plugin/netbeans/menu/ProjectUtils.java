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
package com.gluonhq.plugin.netbeans.menu;

import com.gluonhq.plugin.netbeans.menu.cloudlink.JCloudLink;
import com.gluonhq.plugin.templates.ProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.UUID;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class ProjectUtils {

    public static final String CLOUDLINK_CONFIG_FILE = "gluoncloudlink_config.json";

    private final static Preferences PREFERENCES = Preferences.userRoot().node("com.gluonhq.plugin.netbeans");
    private final static String PATH = "Path";
    private Preferences PROJECT_PREFERENCES;

    private final Project project;
    private Project rootProject;
    private Project mobileProject;
    private boolean gluonMobileProject;
    private boolean gluonFunctionProject;

    public ProjectUtils(Project project) {
        this.project = project;

        if (project != null) {
            configure();
        }
    }

    public boolean isGluonProject() {
        return rootProject != null;
    }

    public boolean isGluonMobileProject() {
        return gluonMobileProject;
    }

    public boolean isGluonFunctionProject() {
        return gluonFunctionProject;
    }

    public Project getRootProject() {
        return rootProject;
    }

    public Project getMobileProject() {
        return mobileProject;
    }

    public FileObject getCloudLinkFile() {
        if (mobileProject != null && getGradleBuildFile(mobileProject) != null) {
            // find cloudLink config file
            FileObject cloudLinkFile = mobileProject.getProjectDirectory().getFileObject("src/main/resources/" + CLOUDLINK_CONFIG_FILE);
            if (cloudLinkFile == null) {
                // file doesn't exist, sets the folder where it can be created
                cloudLinkFile = mobileProject.getProjectDirectory().getFileObject("src/main/resources");
            }
            return cloudLinkFile;
        }
        return null;
    }

    public String getCloudLinkUserKey() {
        return PREFERENCES.get(ProjectConstants.PARAM_GLUON_CLOUDLINK_USER_KEY, null);
    }

    public void setCloudLinkUserKey(String userKey) {
        PREFERENCES.put(ProjectConstants.PARAM_GLUON_CLOUDLINK_USER_KEY, userKey);
    }

    public void removeCloudLinkUserKey() {
        PREFERENCES.remove(ProjectConstants.PARAM_GLUON_CLOUDLINK_USER_KEY);
    }

    public String getCloudLinkIdeKey() {
        if (isGluonProject()) {
            return PROJECT_PREFERENCES.get(ProjectConstants.PARAM_GLUON_CLOUDLINK_IDE_KEY, null);
        }
        return null;
    }

    public void setCloudLinkIdeKey(String ideKey) {
        if (isGluonProject()) {
            PROJECT_PREFERENCES.put(ProjectConstants.PARAM_GLUON_CLOUDLINK_IDE_KEY, ideKey);
        }
    }

    public void removeCloudLinkIdeKey() {
        if (isGluonProject()) {
            PROJECT_PREFERENCES.remove(ProjectConstants.PARAM_GLUON_CLOUDLINK_IDE_KEY);
        }
    }

    private void configure() {
        loadProjectConfiguration();

        loadProjectPreferences();
    }

    private void loadProjectConfiguration() {
        gluonMobileProject = isGluonMobileProject(project);

        if (gluonMobileProject) {
            mobileProject = project;
            try {
                Project parentProject = ProjectManager.getDefault()
                        .findProject(project.getProjectDirectory().getParent());
                if (parentProject != null) {
                    rootProject = parentProject;
                } else {
                    rootProject = project;
                }
            } catch (IOException e) {}
        } else if (isGluonFunctionProject(project)) {
            gluonFunctionProject = true;
            try {
                Project parentProject = ProjectManager.getDefault()
                        .findProject(project.getProjectDirectory().getParent());
                if (parentProject != null) {
                    rootProject = parentProject;
                } else {
                    rootProject = project;
                }
            } catch (IOException e) {}
        }

        if (mobileProject == null && rootProject != project) {
            Project projectToInspect = rootProject != null ? rootProject : project;
            if (getGradleBuildFile(projectToInspect) != null && getGradleSettingsFile(projectToInspect) != null) {
                final Enumeration<? extends FileObject> folders = projectToInspect.getProjectDirectory().getFolders(true);
                while (folders.hasMoreElements()) {
                    FileObject folder = folders.nextElement();
                    try {
                        Project subproject = ProjectManager.getDefault().findProject(folder);
                        if (subproject != null && isGluonMobileProject(subproject)) {
                            mobileProject = subproject;
                            rootProject = projectToInspect;
                            break;
                        }
                    } catch (IOException e) {}
                }
            }
        }
    }

    private void loadProjectPreferences() {
        if (! isGluonProject()) {
            return;
        }
        try {
            for (String child : PREFERENCES.childrenNames()) {
                final Preferences pref = PREFERENCES.node(child);
                final String nodePath = pref.get(PATH, null);
                if (nodePath != null && ! nodePath.isEmpty()) {
                    if (rootProject.getProjectDirectory().getPath().equals(nodePath)) {
                        PROJECT_PREFERENCES = pref;
                        break;
                    }
                }
            }

            if (PROJECT_PREFERENCES == null) {
                final String key = generatePreferencesKey(rootProject.getProjectDirectory().getName());
                PROJECT_PREFERENCES = PREFERENCES.node(key);
                PROJECT_PREFERENCES.put(PATH, rootProject.getProjectDirectory().getPath());
            }
        } catch (BackingStoreException bse) {}
    }

    private String generatePreferencesKey(String name) throws BackingStoreException {
        String key = UUID.randomUUID().toString() + name;
        if (key.length() > Preferences.MAX_KEY_LENGTH) {
            key = key.substring(0, Preferences.MAX_KEY_LENGTH);
        }
        return key;
    }

    public static FileObject getGradleBuildFile(Project project) {
        return project.getProjectDirectory().getFileObject("build.gradle");
    }

    public static FileObject getGradleSettingsFile(Project project) {
        return project.getProjectDirectory().getFileObject("settings.gradle");
    }

    public static File getProjectFile(Project project) {
        if (project == null) {
            return null;
        }
        return FileUtil.toFile(project.getProjectDirectory());
    }

    public static void refreshProject(Project project) {
        if (project == null) {
            return;
        }
        project.getProjectDirectory().refresh(true);
    }

    public static String getCloudLinkConfig(FileObject cloudLinkConfig) {
        if (cloudLinkConfig != null && !cloudLinkConfig.isFolder()) {
            try {
                return cloudLinkConfig.asText();
            } catch (IOException e) {}
        }
        return null;
    }

    // check if build.gradle uses the jfxmobile plugin 1.1.0+ (downConfig)
    private static boolean isGluonMobileProject(Project project) {
        FileObject buildFile = getGradleBuildFile(project);
        if (buildFile != null) {
            try {
                return buildFile.asLines()
                        .stream()
                        .anyMatch(line -> line.contains("downConfig"));
            } catch (IOException | IllegalArgumentException ex) {}
        }
        return false;
    }

    // check if build.gradle contains the gfBundle task
    private static boolean isGluonFunctionProject(Project project) {
        FileObject buildFile = getGradleBuildFile(project);
        if (buildFile != null) {
            try {
                return buildFile.asLines()
                        .stream()
                        .anyMatch(line -> line.contains("task gfBundle"));
            } catch (IOException | IllegalArgumentException ex) {}
        }
        return false;
    }
    
    public boolean cloudLinkSignIn() {
        FileObject cloudLinkConfig = getCloudLinkFile();
        if (getCloudLinkUserKey() == null || getCloudLinkConfig(cloudLinkConfig) == null || getCloudLinkIdeKey() == null) {
            final JCloudLink jCloudLink = new JCloudLink(this, getCloudLinkIdeKey() != null);
            final JRootPane rootPane = jCloudLink.getRootPane();

            final JDialog dialog = new JDialog(jCloudLink, Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setContentPane(rootPane);
            dialog.setMinimumSize(new Dimension(600, 372));
            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);

            return !(getCloudLinkUserKey() == null || getCloudLinkConfig(cloudLinkConfig) == null || getCloudLinkIdeKey() == null);
        }
        return true;
    }

    public void showDialog(JFrame frame) {
        showDialog(frame, 600, 372);
    }
    
    public void showDialog(JFrame frame, int width, int height ) {
        final JDialog dialog = new JDialog(frame, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setContentPane(frame.getRootPane());
        dialog.setMinimumSize(new Dimension(width, height));
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }
}
