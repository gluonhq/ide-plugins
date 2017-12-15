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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import com.gluonhq.eclipse.plugin.menu.ProjectUtils;
import com.gluonhq.plugin.cloudlink.AccountSWT;
import com.gluonhq.plugin.cloudlink.ApplicationsSWT;
import com.gluonhq.plugin.cloudlink.Credentials;

public class JCloudLink {

    private final ProjectUtils utils;
    private final boolean allowDisableApply;
    private final IResource cloudLinkFile;
    private final Runnable runnable;

    public JCloudLink(ProjectUtils utils) {
        this(utils, true, null);
    }
    
    public JCloudLink(ProjectUtils utils, boolean allowDisableApply, Runnable runnable) {
        this.utils = utils;
        this.allowDisableApply = allowDisableApply;
        this.runnable = runnable;

        cloudLinkFile = utils.getCloudLinkFile();
        Path jsonPath = Paths.get(cloudLinkFile.getLocation().toOSString());
        final String userKey = utils.getCloudLinkUserKey();
        if (userKey == null) {
            runLogin(jsonPath);
        } else {
            runApplications(userKey, true, jsonPath);
        }
    }

    private void runLogin(final Path jsonPath) {
        AccountSWT account = new AccountSWT(null);
        Credentials credentials = account.getCredentials();
        
        account.open();
        
        String userKey = credentials.getUserKey();
        if (userKey != null) {
            if (credentials.isKeepLogged()) {
                utils.setCloudLinkUserKey(userKey);
            } else {
                utils.removeCloudLinkUserKey();
            }
            runApplications(userKey, credentials.isKeepLogged(), jsonPath);
        }
    }

    private void runApplications(final String userKey, final boolean keepLogged, final Path jsonPath) {
        ApplicationsSWT applications = new ApplicationsSWT(null, userKey, keepLogged, ProjectUtils.getCloudLinkConfig(cloudLinkFile), allowDisableApply);
        Credentials credentials = applications.getCredentials();
        
        applications.open();
        
        if (credentials.getCredentials() != null) {
            utils.setCloudLinkIdeKey(credentials.getIdeKey());
            Path finalJsonPath = jsonPath;
            if (jsonPath.toFile().isDirectory()) {
                finalJsonPath = Paths.get(jsonPath.toFile().toString(), ProjectUtils.CLOUDLINK_CONFIG_FILE);
            }
            String cloudLinkText = credentials.getCredentials();
            try {
                Files.write(finalJsonPath, cloudLinkText.getBytes(StandardCharsets.UTF_8));
                cloudLinkFile.refreshLocal(IResource.DEPTH_ONE, null);
            } catch (IOException | CoreException ex) {
                MessageDialog.openError(new Shell(), "Error", "Error writing json file: " + ex);
                ex.printStackTrace();
            }
        } 
        if (credentials.getUserKey() == null) {
            utils.removeCloudLinkUserKey();
            return;
        } 
    
        if (credentials.getCredentials() != null && runnable != null) {
            runnable.run();
        }
    }

}
