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

import org.netbeans.api.project.Project;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.ResourceBundle;

@ActionID(
        category = "Gluon",
        id = "com.gluonhq.plugin.netbeans.menu.GluonAction"
)
@ActionRegistration(
        displayName = "#CTL_Gluon", lazy=false
)
@ActionReference(path = "Projects/Actions", position = 1000, separatorBefore = 800)
@NbBundle.Messages("CTL_Gluon=Gluon")
public class GluonAction extends AbstractAction implements ContextAwareAction {

    private static final long serialVersionUID = 1L;

    @Override
    public void actionPerformed(ActionEvent ev) {
    }

    @Override
    public Action createContextAwareInstance(Lookup context) {
        return new ContextAction(context);
    }

    private static final class ContextAction extends AbstractAction implements Presenter.Popup {

        private final Project project;
        private final ProjectUtils projectUtils;

        public ContextAction(Lookup context) {
            project = context.lookup(Project.class);
            projectUtils = new ProjectUtils(project);

            putValue(NAME, ResourceBundle.getBundle("com.gluonhq.plugin.plugin").getString("plugin.menu.root"));
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
            setEnabled(projectUtils.isGluonProject());
        }

        @Override
        public JMenuItem getPopupPresenter() {
            JMenu main = new JMenu(ResourceBundle.getBundle("com.gluonhq.plugin.plugin").getString("plugin.menu.root"));
            if (projectUtils.isGluonProject()) {
                List<? extends Action> actionsForPath = Utilities.actionsForPath("Actions/Gluon/AddGluonFunction");
                actionsForPath.forEach(a -> {
                    a.putValue("Option_AddGluonFunction", ResourceBundle.getBundle("com.gluonhq.plugin.plugin").getString("plugin.menu.submenu.add_gluon_function"));
                    main.add(a);
                });
                if (projectUtils.isGluonFunctionProject()) {
                    actionsForPath = Utilities.actionsForPath("Actions/Gluon/UploadGluonFunction");
                    actionsForPath.forEach(a -> {
                        a.putValue("Option_UploadGluonFunction", ResourceBundle.getBundle("com.gluonhq.plugin.plugin").getString("plugin.menu.submenu.upload_gluon_function"));
                        main.add(a);
                    });
                }

                main.addSeparator();

                actionsForPath = Utilities.actionsForPath("Actions/Gluon/MobileSettings");
                actionsForPath.forEach(a -> {
                    a.putValue("Option_MobileSettings", ResourceBundle.getBundle("com.gluonhq.plugin.plugin").getString("plugin.menu.submenu.mobile_settings"));
                    main.add(a);
                });
                actionsForPath = Utilities.actionsForPath("Actions/Gluon/CloudLinkSettings");
                actionsForPath.forEach(a -> {
                    a.putValue("Option_CloudLinkSettings", ResourceBundle.getBundle("com.gluonhq.plugin.plugin").getString("plugin.menu.submenu.cloudlink_settings"));
                    main.add(a);
                });
            }

            main.setEnabled(projectUtils.isGluonProject());
            main.putClientProperty(DynamicMenuContent.HIDE_WHEN_DISABLED, true);

            return main;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // no-op
        }
    }
}
