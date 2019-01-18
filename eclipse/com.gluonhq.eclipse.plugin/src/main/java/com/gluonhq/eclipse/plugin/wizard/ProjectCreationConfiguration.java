/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package com.gluonhq.eclipse.plugin.wizard;

import com.google.common.base.Preconditions;

import java.io.File;

/**
 * Serves as the extra data model of the project creation wizard.
 */
public final class ProjectCreationConfiguration {

    private String projectName;
    private Boolean useDefaultLocation;
    private File customLocation;
    private File targetProjectDir;

    public ProjectCreationConfiguration(String projectName, Boolean useDefaultLocation, File customLocation, File targetProjectDir) {
        this.projectName = Preconditions.checkNotNull(projectName);
        this.useDefaultLocation = Preconditions.checkNotNull(useDefaultLocation);
        this.customLocation = Preconditions.checkNotNull(customLocation);
        this.targetProjectDir = Preconditions.checkNotNull(targetProjectDir);
    }

    public String getProjectName() {
        return this.projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Boolean getUseDefaultLocation() {
        return this.useDefaultLocation;
    }

    public void setUseDefaultLocation(boolean useDefaultLocation) {
        this.useDefaultLocation = useDefaultLocation;
    }

    public File getCustomLocation() {
        return this.customLocation;
    }

    public void setCustomLocation(File customLocation) {
        this.customLocation = customLocation;
    }

    public File getTargetProjectDir() {
        return this.targetProjectDir;
    }

    public void setTargetProjectDir(File targetProjectDir) {
        this.targetProjectDir = targetProjectDir;
    }

}
