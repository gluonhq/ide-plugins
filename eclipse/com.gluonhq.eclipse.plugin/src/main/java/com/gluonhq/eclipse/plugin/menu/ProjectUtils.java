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
package com.gluonhq.eclipse.plugin.menu;

import java.awt.Dialog;
import java.awt.Dimension;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import com.gluonhq.eclipse.plugin.menu.cloudlink.JCloudLink;
import com.gluonhq.plugin.templates.ProjectConstants;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;

public class ProjectUtils {
	
	public static final String CLOUDLINK_CONFIG_FILE = "gluoncloudlink_config.json";
	private final static Preferences PREFERENCES = Preferences.userRoot().node("com.gluonhq.plugin.eclipse");
    private final static String PATH = "Path";
    private Preferences PROJECT_PREFERENCES;
    
    private final IContainer project;
    private IContainer rootProject;
    private IContainer mobileProject;
    private boolean gluonMobileProject;
    private boolean gluonFunctionProject;

    public ProjectUtils(IContainer project) {
        this.project = project;

        if (project != null && getGradleBuildFile(project) != null) {
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

    public IContainer getRootProject() {
        return rootProject;
    }

    public IContainer getMobileProject() {
        return mobileProject;
    }
    
    public IResource getCloudLinkFile() {
        if (mobileProject != null && getGradleBuildFile(mobileProject) != null) {
            // find cloudLink config file
            IResource path = mobileProject.findMember("src/main/resources/" + CLOUDLINK_CONFIG_FILE);
            if (path == null) {
                // file doesn't exist, sets the folder where it can be created
                path = mobileProject.findMember("src/main/resources");
            }
            return path;
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
            IContainer parentProject = getParentProject(project);
            if (parentProject != null) {
                rootProject = parentProject;
            } else {
                rootProject = project;
            }
        } else if (isGluonFunctionProject(project)) {
            gluonFunctionProject = true;
            IContainer parentProject = getParentProject(project);
            if (parentProject != null) {
                rootProject = parentProject;
            } else {
                rootProject = project;
            }
        }

        if (mobileProject == null && rootProject != project) {
            IContainer projectToInspect = rootProject != null ? rootProject : project;
            if (getGradleBuildFile(projectToInspect) != null && 
            		getGradleSettingsFile(projectToInspect) != null) {
                try {
					for (IResource file : projectToInspect.members()) {
					    if (file != null && file instanceof IContainer) {
					        if (file != null && isGluonMobileProject((IContainer) file)) {
					            mobileProject = (IContainer) file;
					            rootProject = projectToInspect;
					            break;
					        }
					    }
					}
				} catch (CoreException e) {
					e.printStackTrace();
				}
            }
        }
    }
    
    private IContainer getParentProject(IContainer project) {
        IPath location = project.getRawLocation();
        if (location != null && location.segmentCount() > 1) {
            String parentName = location.removeLastSegments(1).lastSegment();
            if (parentName != null && ! parentName.isEmpty()) {
                return Stream.of(ResourcesPlugin.getWorkspace().getRoot().getProjects())
                                .filter(p -> p.getName().equals(parentName))
                                .map(IContainer.class::cast)
                                .findFirst().orElse(null);
            }
            return null;
        }
        return null;
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
                    if (rootProject.getLocation().toOSString().equals(nodePath)) {
                        PROJECT_PREFERENCES = pref;
                        break;
                    }
                }
            }

            if (PROJECT_PREFERENCES == null) {
                final String key = generatePreferencesKey(rootProject.getName());
                PROJECT_PREFERENCES = PREFERENCES.node(key);
                PROJECT_PREFERENCES.put(PATH, rootProject.getLocation().toOSString());
            }
        } catch (BackingStoreException bse) {}
    }

    private String generatePreferencesKey(String name) throws BackingStoreException {
        String key = UUID.randomUUID().toString() + name;
        if (key.length() > java.util.prefs.Preferences.MAX_KEY_LENGTH) {
            key = key.substring(0, java.util.prefs.Preferences.MAX_KEY_LENGTH);
        }
        return key;
    }
    
    public static IFile getGradleBuildFile(IContainer project) {
        IResource resource = project.findMember("build.gradle");
		if (resource != null && resource.exists()) {
			return (IFile) resource;
		}
		return null;
    }
    
    public static IFile getGradleSettingsFile(IContainer project) {
        IResource resource = project.findMember("settings.gradle");
		if (resource != null && resource.exists()) {
			return (IFile) resource;
		}
		return null;
	}
    
    public static void refreshProject(IContainer project) {
        if (project == null) {
            return;
        }
        try {
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
    }

    public static String getCloudLinkConfig(IResource cloudLinkConfig) {
        if (cloudLinkConfig != null && cloudLinkConfig.getType() == IResource.FILE) {
            try {
                Path jsonPath = Paths.get(cloudLinkConfig.getLocationURI());
                return Files.lines(jsonPath).collect(Collectors.joining("\n"));
            } catch (IOException ex) { }
        }
        return null;
    }
    
    // check if build.gradle uses the jfxmobile plugin 1.1.0+ (downConfig)
    private static boolean isGluonMobileProject(IContainer project) {
        IFile iFile = getGradleBuildFile(project);
        if (iFile != null) {
        		try {
                return Files.lines(Paths.get(iFile.getLocationURI()))
                        .anyMatch(line -> line.contains("downConfig"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }  
        }
        return false;
    }
    
    // check if build.gradle contains the gfBundle task
    private static boolean isGluonFunctionProject(IContainer project) {
        IFile iFile = getGradleBuildFile(project);
        if (iFile != null) {
            try {
	            return Files.lines(Paths.get(iFile.getLocationURI()))
	                    .anyMatch(line -> line.contains("task gfBundle"));
	        } catch (IOException ex) {
	            ex.printStackTrace();
	        } 
        }
        return false;
    }
    
    private final ReadOnlyBooleanWrapper cloudLinkSignedIn = new ReadOnlyBooleanWrapper();
    
    private void setCloudLinkSignedIn() {
        IResource cloudLinkConfig = getCloudLinkFile();
        cloudLinkSignedIn.set(! (getCloudLinkUserKey() == null || getCloudLinkConfig(cloudLinkConfig) == null || getCloudLinkIdeKey() == null));
    }
    
    public final boolean isCloudLinkSignedIn() {
        setCloudLinkSignedIn();
        return cloudLinkSignedIn.get();
    }
    
    public final ReadOnlyBooleanProperty cloudLinkSignedInProperty() { return cloudLinkSignedIn.getReadOnlyProperty(); }
    
    public void cloudLinkSignIn() {
        final JCloudLink jCloudLink = new JCloudLink(this, getCloudLinkIdeKey() != null);
        showDialog(jCloudLink, 600, 372, () -> setCloudLinkSignedIn());
    }
    
    public void showDialog(JFrame frame) {
        showDialog(frame, 600, 372);
    }

    public void showDialog(JFrame frame, int width, int height) {
    		showDialog(frame, width, height, null);
    }
    
    private void showDialog(JFrame frame, int width, int height, Runnable runnable) {
        SwingUtilities.invokeLater(() -> {
            final JDialog dialog = new JDialog(frame, Dialog.ModalityType.APPLICATION_MODAL);
	        dialog.setContentPane(frame.getRootPane());
	        dialog.setMinimumSize(new Dimension(width, height));
	        dialog.pack();
	        dialog.setLocationRelativeTo(null);
	        dialog.setVisible(true);
	        
	        if (runnable != null) {
	        		runnable.run();
	        }
        });
    }

}
