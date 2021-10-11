# Gluon Plugin for NetBeans

The repository contains the source code of [Gluon Plugin for NetBeans IDE](http://plugins.netbeans.org/plugin/57602/gluon-plugin).

## How to build the plugin

We use Maven build tool to build the plugin/NetBeans module.

The following Maven goals can be used to build and run the module in an instance of NetBeans:

Build the module:

```
mvn install nbm:nbm
```

Run NetBeans with this module:

```
mvn nbm:run-ide
```