# Gluon Plugin for NetBeans

The repository contains the source code of [Gluon Plugin for NetBeans IDE](http://plugins.netbeans.org/plugin/57602/gluon-plugin).

## How to build the plugin

We use Gradle build tool along with the [Gradle NBM plugin](https://github.com/radimk/gradle-nbm-plugin) to build the plugin/NetBeans module.

The following Gradle tasks can be used to build and run the module in an instance of NetBeans:

Build the module:

```
./gradlew netbeans
```

Run NetBeans with this module:

```
./gradlew netBeansRun
```