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
package com.gluonhq.plugin.netbeans.menu.down;

import com.gluonhq.plugin.down.PluginsBean;
import com.gluonhq.plugin.down.PluginsFX;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import org.openide.filesystems.FileObject;

import javax.swing.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class JPlugins extends JFrame {

    public JPlugins(FileObject buildFile) {
        final JFXPanel fxPanel = new JFXPanel();
        Platform.setImplicitExit(false);
        Platform.runLater(() -> {
            List<String> lines = null;
            try {
                lines = buildFile.asLines();
            } catch (IOException ex) {
                PluginsFX.showError("Error reading build.gradle: " + ex);
            }  
            PluginsFX pluginsFX = new PluginsFX();
            PluginsBean pluginsBean = pluginsFX.loadBuildLines(lines);
            pluginsBean.addPropertyChangeListener(e -> {
                if (e.getNewValue() != null) {
                    try {
                        final List<String> editedBuild = pluginsBean.savePlugins();
                        if (editedBuild != null && !editedBuild.isEmpty()) {
                            Files.write(Paths.get(buildFile.getPath()), editedBuild, StandardCharsets.UTF_8);
                        } 
                        buildFile.refresh();
                    } catch (IOException ex) {
                        PluginsFX.showError("Error writing build.gradle: " + ex);
                    }
                }
                dispose();
            });
            final Scene scene = new Scene(pluginsFX);
            fxPanel.setScene(scene);
        });
        
        getContentPane().add(fxPanel);
    }
}
