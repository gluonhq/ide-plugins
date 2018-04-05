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
package com.gluonhq.plugin.intellij.menu;

import com.gluonhq.plugin.intellij.menu.cloudlink.JCloudLink;
import com.gluonhq.plugin.templates.ProjectConstants;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public class ProjectUtils {

    public static final String CLOUDLINK_CONFIG_FILE = "gluoncloudlink_config.json";
    private final static Preferences PREFERENCES = Preferences.userRoot().node("com.gluonhq.plugin.intellij");
    private final static String PATH = "Path";
    private Preferences PROJECT_PREFERENCES;

    private final VirtualFile project;
    private VirtualFile rootProject;
    private VirtualFile mobileProject;
    private boolean gluonMobileProject;
    private boolean gluonFunctionProject;

    public ProjectUtils(VirtualFile project) {
        this.project = project;

        if (project != null && project.isDirectory() && getGradleBuildFile(project) != null) {
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

    public VirtualFile getRootProject() {
        return rootProject;
    }

    public VirtualFile getMobileProject() {
        return mobileProject;
    }

    public VirtualFile getCloudLinkFile() {
        if (mobileProject != null && getGradleBuildFile(mobileProject) != null) {
            // find cloudLink config file
            VirtualFile cloudLinkFile = mobileProject.findFileByRelativePath("src/main/resources/" + CLOUDLINK_CONFIG_FILE);
            if (cloudLinkFile == null) {
                // file doesn't exist, sets the folder where it can be created
                cloudLinkFile = mobileProject.findFileByRelativePath("src/main/resources");
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
            VirtualFile parentProject = project.getParent();
            if (parentProject != null) {
                rootProject = parentProject;
            } else {
                rootProject = project;
            }
        } else if (isGluonFunctionProject(project)) {
            gluonFunctionProject = true;
            VirtualFile parentProject = project.getParent();
            if (parentProject != null) {
                rootProject = parentProject;
            } else {
                rootProject = project;
            }
        }

        if (mobileProject == null && rootProject != project) {
            VirtualFile projectToInspect = rootProject != null ? rootProject : project;
            if (getGradleBuildFile(projectToInspect) != null && getGradleSettingsFile(projectToInspect) != null) {
                for (VirtualFile file : projectToInspect.getChildren()) {
                    if (file != null && file.isDirectory()) {
                        if (file != null && isGluonMobileProject(file)) {
                            mobileProject = file;
                            rootProject = projectToInspect;
                            break;
                        }
                    }
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
                    if (rootProject.getPath().equals(nodePath)) {
                        PROJECT_PREFERENCES = pref;
                        break;
                    }
                }
            }

            if (PROJECT_PREFERENCES == null) {
                final String key = generatePreferencesKey(rootProject.getName());
                PROJECT_PREFERENCES = PREFERENCES.node(key);
                PROJECT_PREFERENCES.put(PATH, rootProject.getPath());
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

    public static VirtualFile getGradleBuildFile(VirtualFile project) {
        return project.findChild("build.gradle");
    }

    public static VirtualFile getGradleSettingsFile(VirtualFile project) {
        return project.findChild("settings.gradle");
    }

    public static File getProjectFile(VirtualFile project) {
        if (project == null) {
            return null;
        }
        return new File(project.getPath());
    }

    public static void refreshProject(VirtualFile project) {
        if (project == null) {
            return;
        }
        project.refresh(true, true);
    }

    public static String getCloudLinkConfig(VirtualFile cloudLinkConfig) {
        if (cloudLinkConfig != null && !cloudLinkConfig.isDirectory()) {
            try {
                Path jsonPath = Paths.get(cloudLinkConfig.getPath());
                return Files.lines(jsonPath).collect(Collectors.joining("\n"));
            } catch (IOException ex) { }
        }
        return null;
    }

    // check if build.gradle uses the jfxmobile plugin 1.1.0+ (downConfig)
    private static boolean isGluonMobileProject(VirtualFile project) {
        VirtualFile buildFile = getGradleBuildFile(project);
        if (buildFile != null) {
            try {
                Document document = FileDocumentManager.getInstance().getDocument(buildFile);
                return StringUtil.indexOf(document.getCharsSequence(), "downConfig") > -1;
            } catch (IllegalArgumentException ex) {}
        }
        return false;
    }

    // check if build.gradle contains the gfBundle task
    private static boolean isGluonFunctionProject(VirtualFile project) {
        VirtualFile buildFile = getGradleBuildFile(project);
        if (buildFile != null) {
            try {
                Document document = FileDocumentManager.getInstance().getDocument(buildFile);
                return StringUtil.indexOf(document.getCharsSequence(), "task gfBundle") > -1;
            } catch (IllegalArgumentException ex) {}
        }
        return false;
    }

    public boolean cloudLinkSignIn() {
        VirtualFile cloudLinkConfig = getCloudLinkFile();
        if (getCloudLinkUserKey() == null || getCloudLinkConfig(cloudLinkConfig) == null || getCloudLinkIdeKey() == null) {
            final JCloudLink jCloudLink = new JCloudLink(this, getCloudLinkIdeKey() != null);
            showDialog(jCloudLink);

            cloudLinkConfig = getCloudLinkFile();
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

    public static void runSafe(Runnable runnable) {
        final Application application = ApplicationManager.getApplication();
        if (application.isDispatchThread()) {
            application.invokeLater(runnable, ModalityState.NON_MODAL);
        }
    }
}
