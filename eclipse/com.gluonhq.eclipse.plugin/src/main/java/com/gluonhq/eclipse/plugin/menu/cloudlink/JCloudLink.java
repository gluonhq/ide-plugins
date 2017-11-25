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
package com.gluonhq.eclipse.plugin.menu.cloudlink;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JFrame;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.gluonhq.eclipse.plugin.menu.ProjectUtils;
import com.gluonhq.plugin.cloudlink.AccountFX;
import com.gluonhq.plugin.cloudlink.ApplicationsFX;
import com.gluonhq.plugin.cloudlink.Credentials;
import com.gluonhq.plugin.down.PluginsFX;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;

public class JCloudLink extends JFrame {

    private static final long serialVersionUID = 4225983627721313247L;
	
	private final ProjectUtils utils;
    private final boolean allowDisableApply;
    private final IResource cloudLinkFile;

    public JCloudLink(ProjectUtils utils) {
        this(utils, true);
    }
    public JCloudLink(ProjectUtils utils, boolean allowDisableApply) {
        this.utils = utils;
        this.allowDisableApply = allowDisableApply;

        cloudLinkFile = utils.getCloudLinkFile();
		Path jsonPath = Paths.get(cloudLinkFile.getLocation().toOSString());
        final String userKey = utils.getCloudLinkUserKey();
        if (userKey == null) {
            getContentPane().add(runLogin(jsonPath));
        } else {
            getContentPane().add(runApplicationsFX(userKey, true, jsonPath));
        }
    }

    private JFXPanel runLogin(final Path jsonPath) {
        final JFXPanel fxPanel = new JFXPanel();
        Platform.setImplicitExit(false);
        Platform.runLater(() -> {
            AccountFX accountFX = new AccountFX();
            Credentials credentials = accountFX.getCredentials();
            credentials.addPropertyChangeListener(e -> {
                if (Credentials.USERKEY_PROPERTY.equals(e.getPropertyName())) {
                    String userKey = (String) e.getNewValue();
                    if (userKey != null) {
                        if (credentials.isKeepLogged()) {
                            utils.setCloudLinkUserKey(userKey);
                        } else {
                            utils.removeCloudLinkUserKey();
                        }
                        getContentPane().remove(fxPanel);
                        getContentPane().add(runApplicationsFX(userKey, credentials.isKeepLogged(), jsonPath));
                    } else {
                        dispose();
                    }
                }
            });
            final Scene scene = new Scene(accountFX);
            fxPanel.setScene(scene);
        });

        return fxPanel;
    }

    private JFXPanel runApplicationsFX(final String userKey, final boolean keepLogged, final Path jsonPath) {
        final JFXPanel fxPanel = new JFXPanel();
        Platform.setImplicitExit(false);
        Platform.runLater(() -> {
            ApplicationsFX applicationsFX = new ApplicationsFX(userKey, keepLogged, allowDisableApply);
            Credentials credentials = applicationsFX.getCredentials();
            credentials.addPropertyChangeListener(e -> {
                if (Credentials.USERKEY_PROPERTY.equals(e.getPropertyName())) {
                    utils.removeCloudLinkUserKey();
                    dispose();
                } else if (Credentials.CREDENTIALS_PROPERTY.equals(e.getPropertyName())) {
                    String cloudLinkText = (String) e.getNewValue();
                    if (cloudLinkText != null) {
                        utils.setCloudLinkIdeKey(credentials.getIdeKey());
                        Path finalJsonPath = jsonPath;
                        if (jsonPath.toFile().isDirectory()) {
                            finalJsonPath = Paths.get(jsonPath.toFile().toString(), ProjectUtils.CLOUDLINK_CONFIG_FILE);
                        }
                        try {
                            Files.write(finalJsonPath, cloudLinkText.getBytes(StandardCharsets.UTF_8));
                            cloudLinkFile.refreshLocal(IResource.DEPTH_ONE, null);
                        } catch (IOException | CoreException ex) {
                            Platform.runLater(() -> PluginsFX.showError("Error writing json file: " + ex));
                            ex.printStackTrace();
						}
                        dispose();
                    } else {
                        dispose();
                    }
                }
            });

            final Scene scene = new Scene(applicationsFX);
            fxPanel.setScene(scene);

            applicationsFX.loadApplications(ProjectUtils.getCloudLinkConfig(cloudLinkFile));
        });
        return fxPanel;
    }

}
