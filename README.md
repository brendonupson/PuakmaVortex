# PuakmaVortex
Eclipse based IDE
----------------------------------------------


How to produce the eclipse plugin solution:
1.	Download Eclipse
2.	Setup workspace
3.	Install dependencies
4.	Compile
5.	Export Vortex Eclipse solution



# 1. Download Eclipse

In order to limit the number of dependencies to install, it is recommended to install the Eclipse Committers version. It can be found here: https://www.eclipse.org/downloads/packages/release/2020-09/r/eclipse-ide-eclipse-committers

This release is called Eclipse 4.17 and based on the official documentation; it has the following two notes:
1.	The latest release of Eclipse requires a 64-bit JVM, and does not support a 32-bit JVM.
2.	Current releases of Eclipse require Java 11 JRE/JDK or newer.
	If you are using Eclipse to do Java development, or are on macOS, install a JDK. In all cases, Eclipse requires a 64-bit JVM
	A Java Development Kit (JDK) includes many useful extras for Java developers including the source code for the standard Java libraries.
Note: other IDE versions might be fine too, but might require installation of more dependencies.


# 2. Setup workspace

Create a new workspace and clone the Git repository.
Now open the project. Go to File> Open Project from File System. Select the Puakma_Vortex folder from the Git repository.


# 3. Installing dependencies

The project won’t compile until all the dependencies are installed.
Go to Help>Install New Software to install the following components:
1.	GEF (MVC) SDK
2.	m2e-wtp - Maven Integration for WTP
3.	m2e-wtp - JSF configurator for WTP
4.	m2e – slf4j over logback logging
5.	JavaScript Development Tools
6.	Data Tools Platform SQL Development Tools
7.	Eclipse Java Web Developer Tools


# 4. Compile

Make sure project compiles. No errors, a few warnings.

# 5. Export Vortex Eclipse solution

Expand the Puakma_Vortex project in the Package Explorer. Open Vortex.product. Press the icon in the top-right corner called Export an Eclipse Product. Follow the wizard and select a <vortex-export-folder> to export the product.
The export might not export two versions of the same component, it normally exports the latest one. Puakma Vortex requires older versions of some components. For this reason, it is required to copy manually some components to the <vortex-export-folder>/eclipse/plugins folder. Alternatively copy the following script:
  
```bash
#Path to the plugins eclipse installation folder
ECLIPSE_INSTALLED_PATH="eclipse-committers-2020-09-R-win32-x86_64/eclipse/plugins/"
#Path to the new Vortex eclipse folder
ECLIPSE_NEW_BUILD="<vortex-export-folder>/eclipse/plugins"
cp "$ECLIPSE_INSTALLED_PATH/org.apache.lucene.analyzers-common_7.5.0.v20181003-1532.jar" "$ECLIPSE_NEW_BUILD"
cp "$ECLIPSE_INSTALLED_PATH/org.apache.lucene.core_7.5.0.v20181003-1532.jar" "$ECLIPSE_NEW_BUILD"
cp "$ECLIPSE_INSTALLED_PATH/org.apache.lucene.misc_7.5.0.v20181003-1532.jar" "$ECLIPSE_NEW_BUILD"
cp "$ECLIPSE_INSTALLED_PATH/org.apache.lucene.queryparser_7.5.0.v20181003-1532.jar" "$ECLIPSE_NEW_BUILD"
```

(optional & Incomplete – needs some research) This product is initially configured to produce a Windows output. To use Mac, I believe the minimum is to go to the Contents tab and click on Add Required Plug-ins. This will add the Plug-ins and Fragments needed to produce a Mac version. There might be more steps

