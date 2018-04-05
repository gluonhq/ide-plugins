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

import com.gluonhq.plugin.netbeans.menu.ProjectUtils;
import org.netbeans.api.project.Project;
import org.openide.LifecycleManager;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@ActionID(
        category = "Gluon/MobileSettings",
        id = "com.gluonhq.plugin.netbeans.menu.down.GluonMobileSettingsAction"
)
@ActionRegistration(
        displayName = "#CTL_MobileSettings"
)
@Messages("CTL_MobileSettings=Gluon Mobile Settings")
public final class GluonMobileSettingsAction implements ActionListener {

    private static final long serialVersionUID = 1L;
    
    private final FileObject buildFile;
    
    private final ProjectUtils projectUtils;

    public GluonMobileSettingsAction(Project project) {
        projectUtils = new ProjectUtils(project);
        this.buildFile = ProjectUtils.getGradleBuildFile(projectUtils.getMobileProject());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        LifecycleManager.getDefault().saveAll();
        
        projectUtils.showDialog(new JPlugins(buildFile), 800, 600);
    }
    
}
