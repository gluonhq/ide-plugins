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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.buildship.core.GradleDistribution;
import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.configuration.BuildConfiguration;

/**
 * Serves as the data model of the project import wizard.
 */
public class ProjectImportConfiguration {

    private File projectDir;
    private Boolean overwriteWorkspaceSettings;
    private GradleDistribution distribution;
    private File gradleUserHome;
    private File javaHome;
    private Boolean applyWorkingSets;
    private List<String> workingSets;
    private Boolean buildScansEnabled;
    private Boolean offlineMode;
    private Boolean autoSync;
    private List<String> arguments;
    private List<String> jvmArguments;
    private Boolean showConsoleView;
    private Boolean showExecutionsView;

    public ProjectImportConfiguration() {
    	this(null,  null,  null,  null, false, new ArrayList<>());
    }
    
    public ProjectImportConfiguration(File projectDir, GradleDistribution distribution,
    		File gradleUserHome, File javaHome, boolean applyWorkingSets, List<String> workingSets) {
        this.projectDir = projectDir;
        this.distribution = distribution;
        this.gradleUserHome = gradleUserHome;
        this.javaHome = javaHome;
        this.applyWorkingSets = applyWorkingSets;
        this.workingSets = workingSets;
        this.arguments = new ArrayList<>();
        this.jvmArguments = new ArrayList<>();
    }

    public File getProjectDir() {
        return this.projectDir;
    }

    public void setProjectDir(File projectDir) {
        this.projectDir = projectDir;
    }


    public Boolean getOverrideWorkspaceConfiguration() {
        return this.overwriteWorkspaceSettings;
    }

    public void setOverwriteWorkspaceSettings(boolean overwriteWorkspaceSettings) {
        this.overwriteWorkspaceSettings = Boolean.valueOf(overwriteWorkspaceSettings);
    }

    public GradleDistribution getDistribution() {
        return this.distribution;
    }

    public void setDistribution(GradleDistribution distribution) {
        this.distribution = distribution;
    }

    public File getGradleUserHome() {
        return this.gradleUserHome;
    }

    public void setGradleUserHome(File gradleUserHome) {
        this.gradleUserHome = gradleUserHome;
    }

    public File getJavaHome() {
        return this.javaHome;
    }

    public void setJavaHomeHome(File javaHome) {
        this.javaHome = javaHome;
    }

    public Boolean getApplyWorkingSets() {
        return this.applyWorkingSets;
    }

    public void setApplyWorkingSets(Boolean applyWorkingSets) {
        this.applyWorkingSets = applyWorkingSets;
    }

    public List<String> getWorkingSets() {
        return this.workingSets;
    }

    public void setWorkingSets(List<String> workingSets) {
        this.workingSets = workingSets;
    }

    public Boolean getBuildScansEnabled() {
        return this.buildScansEnabled;
    }

    public void setBuildScansEnabled(boolean buildScansEnabled) {
        this.buildScansEnabled = Boolean.valueOf(buildScansEnabled);
    }

    public Boolean getOfflineMode() {
        return this.offlineMode;
    }

    public void setOfflineMode(boolean offlineMode) {
        this.offlineMode = Boolean.valueOf(offlineMode);
    }

    public Boolean getAutoSync() {
        return this.autoSync;
    }

    public void setAutoSync(boolean autoSync) {
        this.autoSync = Boolean.valueOf(autoSync);
    }

    public List<String> getArguments() {
        return this.arguments;
    }

    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    public List<String> getJvmArguments() {
        return this.jvmArguments;
    }

    public void setJvmArguments(List<String> jvmArguments) {
        this.jvmArguments = jvmArguments;
    }

    public Boolean getShowConsoleView() {
        return this.showConsoleView;
    }

    public void setShowConsoleView(boolean showConsoleView) {
        this.showConsoleView = Boolean.valueOf(showConsoleView);
    }

    public Boolean getShowExecutionsView() {
        return this.showExecutionsView;
    }

    public void setShowExecutionsView(boolean showExecutionsView) {
        this.showExecutionsView = Boolean.valueOf(showExecutionsView);
    }

    public BuildConfiguration toInternalBuildConfiguration() {
        return CorePlugin.configurationManager().createBuildConfiguration(getProjectDir(),
                getOverrideWorkspaceConfiguration(),
                getDistribution(),
                getGradleUserHome(),
                getJavaHome(),
                getBuildScansEnabled(),
                getOfflineMode(),
                getAutoSync(),
                getArguments(),
                getJvmArguments(),
                getShowConsoleView(),
                getShowExecutionsView());
    }

    public org.eclipse.buildship.core.BuildConfiguration toBuildConfiguration() {
        return org.eclipse.buildship.core.BuildConfiguration.forRootProjectDirectory(getProjectDir())
                .overrideWorkspaceConfiguration(getOverrideWorkspaceConfiguration())
                .gradleDistribution(getDistribution())
                .gradleUserHome(getGradleUserHome())
                .javaHome(getJavaHome())
                .buildScansEnabled(getBuildScansEnabled())
                .offlineMode(getOfflineMode())
                .autoSync(getAutoSync())
                .arguments(getArguments())
                .jvmArguments(getJvmArguments())
                .showConsoleView(getShowConsoleView())
                .showExecutionsView(getShowExecutionsView())
                .build();
    }
}