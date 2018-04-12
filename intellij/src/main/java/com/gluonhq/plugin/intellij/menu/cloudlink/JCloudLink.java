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
package com.gluonhq.plugin.intellij.menu.cloudlink;

import com.gluonhq.plugin.cloudlink.AccountFX;
import com.gluonhq.plugin.cloudlink.ApplicationsFX;
import com.gluonhq.plugin.cloudlink.Credentials;
import com.gluonhq.plugin.down.PluginsFX;
import com.gluonhq.plugin.intellij.menu.ProjectUtils;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.gluonhq.plugin.intellij.menu.ProjectUtils.CLOUDLINK_CONFIG_FILE;

public class JCloudLink extends JFrame {

    private final ProjectUtils utils;
    private final boolean allowDisableApply;

    public JCloudLink(ProjectUtils utils) {
        this(utils, true);
    }
    public JCloudLink(ProjectUtils utils, boolean allowDisableApply) {
        this.utils = utils;
        this.allowDisableApply = allowDisableApply;

        Path jsonPath = Paths.get(utils.getCloudLinkFile().getPath());

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
                            finalJsonPath = Paths.get(jsonPath.toFile().toString(), CLOUDLINK_CONFIG_FILE);
                        }
                        try {
                            Files.write(finalJsonPath, cloudLinkText.getBytes(StandardCharsets.UTF_8));

                            File fileJsonConfig = new File(finalJsonPath.toString());
                            ProjectUtils.runSafe(() -> {
                                VirtualFile file = VfsUtil.findFileByIoFile(fileJsonConfig, true);
                                if (file == null) {
                                    return;
                                }
                                file.refresh(false, false);
                                try {
                                    dispose();
                                } catch (Exception ex) { }
                            });
                        } catch (IOException ex) {
                            PluginsFX.showError("Error writing json file: " + ex);
                            dispose();
                        }
                    } else {
                        dispose();
                    }
                }
            });

            final Scene scene = new Scene(applicationsFX);
            fxPanel.setScene(scene);

            applicationsFX.loadApplications(ProjectUtils.getCloudLinkConfig(utils.getCloudLinkFile()));
        });
        return fxPanel;
    }
}
