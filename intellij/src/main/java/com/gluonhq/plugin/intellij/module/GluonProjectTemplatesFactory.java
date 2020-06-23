/*
 * Copyright (c) 2018, 2020, Gluon Software
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
package com.gluonhq.plugin.intellij.module;

import com.gluonhq.plugin.intellij.util.GluonIcons;
import com.gluonhq.plugin.templates.GluonProjectTarget;
import com.gluonhq.plugin.templates.Template;
import com.gluonhq.plugin.templates.TemplateManager;
import com.intellij.ide.util.projectWizard.JavaModuleBuilder;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.platform.ProjectTemplate;
import com.intellij.platform.ProjectTemplatesFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class GluonProjectTemplatesFactory extends ProjectTemplatesFactory {

    private static final Logger LOG = Logger.getInstance("GluonPlugin");

    public static final String PARENT_GROUP = "Gluon";
    public static final String GLUON = "Gluon";

    @NotNull
    @Override
    public String[] getGroups() {
        return new String[] { GLUON };
    }

    @Override
    public Icon getGroupIcon(String group) {
        return GluonIcons.GLUON_MODULE;
    }

    @Override
    public String getParentGroup(String group) {
        return PARENT_GROUP;
    }

    @Override
    public int getGroupWeight(String group) {
        return JavaModuleBuilder.JAVA_MOBILE_WEIGHT;
    }

    @NotNull
    @Override
    public ProjectTemplate[] createTemplates(@Nullable String group, WizardContext context) {
        Project project = context.getProject();

        List<ProjectTemplate> projectTemplates = new ArrayList<>();
        if (project == null) {
            TemplateManager templateManager = TemplateManager.getInstance();
            List<Template> templates = templateManager.getProjectTemplates(GluonProjectTarget.IDE);
            for (Template template : templates) {
                LOG.info("Template: " + template);
                if (!(template.getProjectName().contains("Function") || template.getProjectName().contains("Desktop"))) {
                    projectTemplates.add(new GluonProjectTemplate(template));
                }
            }
        }
        return projectTemplates.toArray(new ProjectTemplate[projectTemplates.size()]);
    }
}
