# Gluon Plugin for Eclipse

The repository contains the source code of [Gluon Plugin for Eclipse IDE](https://marketplace.eclipse.org/content/gluon-plugin).

## How to build the plugin

We use Maven Tycho plugin to build the Eclipse plugin. The project contains 3 Maven sub-projects:

1. Eclipse Plugin
2. Eclipse Feature
3. Eclipse Site

To simply create a local repository for the plugin, run:

```
mvn clean verify
```

The repository is created in `com.gluonhq.eclipse.site\target`.

### Eclipse Plugin

The project is packaged as 'eclipse-plugin' and is the base project of the plugin.
The project dependents on the Gluon IDE Templates. The dependency and its other transitive dependencies is managed via Maven
and is downloaded into the lib directory when a Maven build is executed. All these dependencies are added to the classpath via the
Bundle-ClassPath in MANIFEST.MF.

### Eclipse Feature

Feature project of the Gluon Eclipse plugin. This project also contains the plugin's license.

### Eclipse Site

Repository project which aggregats plugin content into a p2 repository (aka "update site").

## Run/Debug

To run or debug the plugin, import the project into Eclipse and run `mvn clean verify`.
This will download all the third-party dependencies required by the project.

Right click on `com.gluonhq.eclipse.plugin` and select "Run As" or "Debug As" -> "Eclipse Application".

## Release

To release the plugin to your local repository, run the following command:

```
mvn release:perform -Prelease -Dgoals="clean install"
```

The project uses `maven-jarsigner-plugin` to sign the jars so as to avoid Eclipse to show a warning while installing the plugin.
`maven-jarsigner-plugin` needs to be properly setup before running the release goal.

In order to test a release, run the following command:

```
mvn release:prepare -Prelease
```