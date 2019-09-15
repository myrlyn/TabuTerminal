TabuTerminal
============
A tabbed terminal emulator for Windows.  Mostly just a wrapper to add some useful menus to the pre-existing terminalfx project.  This was a quick-and-dirty project written to fill a need at work, that took on a little bit of a life of its own.  

compile from command line
---------
mvn javafx:compile

execute from command line
---------
mvn javafx:run

build .exe
----------
mvn jfx:native or mvn package

other build considerations
--------------------------
JDK11+ must be your JAVA_HOME (test builds were all done with JDK12)

If JAVAFX11+ is already in your classpath and modulepath (on the machine where you want to run TabuTerminal), you should be able to change the scope of the JAVAFX entries to "provided" to get a smaller distributed package.  

Configuration
---------------
The config file is stored in ~\.TabuTerminal\settings.json

It is JSON formatted, but allows comments. 

It will be overwritten by the current settings upon applclose.  

The default setting elements are :
* defaultTerminalConfig
* sshTerminalConfig
* telTerminalConfig

Which are JSON representations of the parameters of a TerminalConfig object.  

NOTE - windowsTerminalStarter will be ignored for sshTerminalConfig and telTerminalConfig, as it is programatically replaced at runtime. unixTerminalStarter will be replaced similarly, on UNIX systems (this is currently untested).    

```json

  "defaultTerminalConfig": {
    "useDefaultWindowCopy": true,
    "clearSelectionAfterCopy": true,
    "copyOnSelect": false,
    "ctrlCCopy": true,
    "ctrlVPaste": true,
    "cursorColor": "#FF0000",
    "backgroundColor": "#101010",
    "fontSize": 14,
    "foregroundColor": "#F0F0F0",
    "cursorBlink": true,
    "scrollbarVisible": true,
    "enableClipboardNotice": true,
    "scrollWhellMoveMultiplier": 0.1,
    "fontFamily": "\"DejaVu Sans Mono\", \"Everson Mono\", FreeMono, \"Menlo\", \"Terminal\", monospace",
    "userCss": "data:text/plain;base64,eC1zY3JlZW4geyBjdXJzb3I6IGF1dG87IH0\\u003d",
    "windowsTerminalStarter": "C:\\cygwin64\\bin\\bash.exe -i -l",
    "unixTerminalStarter": "/bin/bash -i"
  },
  "log_level": "FINE",
  "ssh_user": "myusername"
```
The only other standard config entities are "log_level" (Which takes java.util.Logging.Level names - e.g. INFO) and "ssh_user" which specifies the default user to use for ssh terminal sessions.  

If Cygwin is not installed, or you just want to use CMD for your default terminal, change windowsTerminalStarter to "C:\\WINDOWS\\system32\\cmd.exe" (or the path to your preferred command line shell, complete with arguments).  

If Cygwin is installed anywhere other than C:\\cygwin64, adjust the path accordingly.  

NOTE THAT THE FINAL LINE OF THE FILE MUST NOT END WITH A COMMA

Development
===========
requires 'lib' folder at the same level as JAVA_HOME containing ant-javafx.jar for mvn jfx:* commands in order for the "jfx" and "package" maven targets to function.  This is distributed with java 1.8 JDKs.    

For instance of JAVA_HOME is C:\java\jdk12

You must have C:\java\lib\ant-javafx.jar

javafx:run will execute the application

MSI installer requires wix (wix.sf.net) to be in the execution path.  Licensed under Microsoft Reciprocal License (MS-RL). //installer not tested

EXE installer requires iscc in the execution path (https://github.com/jrsoftware/issrc/blob/master/license.txt) // BUILD NOT TESTED

JAVA_HOME must refer to a JDK12+ JDK

Feel free to report bugs/make feature requests, but I wrote this 100% on a lark so I don't know when I will get to them.  I literally wrote this during a week of lunch breaks.  

TODO
========
In order of priority
1. ~~Configuration UI/menu (Possibly as a plugin)~~
2. make SSH and TELNET locations configurable on LINUX
3. Enable saved sessions for ssh and telnet. (Possibly as a plugin)
4. Ship bash and busybox as built-in executables, so that we don't have to fall back to CMD if cygwin is not installed.  
5. Implement a sane plugin system, with versioning, security, and a plugin manager. (Maybe OSGI)
    - protect against class collisions
    - versioning
    - plugin manager (possibly as a plugin)
    - basic security
    - allow plugins to easily and safely register new UI and config elements. 
    - runtime plugin loading, unloading, reloading
    - Deprecate and disable V1 plugins. 
6. Test MSI installer. 
7. Build and test EXE installer. 
8. Fix JNLP launcher.  


PLUGINS
===========
Plugins must implement the `` TabuTerminalPlugin_V1 `` abstract class. 

Plugins are given a reference to the main TabuTerminal object to modify at need.

Plugins must implement a single-parameter constructor that takes the TabuTerminal object as the parameter.
   
Plugins should implement the removePlugin method that undoes the changes to the TabuTerminal object.

Jar file for the plugin should be placed in ``${HOME}/.TabuTerminal/plugins``  

LICENSE
=========
Licensed under the GPLV2 license.  

JavaFX licensed under BSD 

SSH Components from git-for-windows licensed under GPLV2

PTy4J library under EPL from IntelliJ repository

JACKSON Libraries under APL

TELNET Components from Cygwin licensed under GPLV2

TerminalFX licensed under the MIT license



PLUGIN DEVELOPMENT
==================
Install the TabuTerminal jar in your local maven repo ``git clone ${tabuterminal-git-repo} ; cd TabuTerminal ; mvn clean package install

create a new POM for your plugin -- for example 
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>tabuterminal</groupId>
  <artifactId>cygwinTcshPlugin</artifactId>
  <version>0.0.1-SNAPSHOT</version>
  <build>
      <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.8.1</version>
        <configuration>
          <source>12</source>
          <target>12</target>
        </configuration>
      </plugin>
    </plugins>
  </build>
  <dependencies>
  	<dependency>
  		<groupId>tabuterminal</groupId>
  		<artifactId>TabuTerminal</artifactId>
  		<version>0.0.2</version>
  		<scope>provided</scope>
  	</dependency>
  </dependencies>
</project>
```

Create your source file -- extending the example above...

```java
package cygwinTcshPlugin;

import java.io.File;
import java.util.logging.Level;

import com.kodedu.terminalfx.TerminalBuilder;
import com.kodedu.terminalfx.TerminalTab;
import com.kodedu.terminalfx.config.TabNameGenerator;
import com.kodedu.terminalfx.config.TerminalConfig;

import javafx.scene.control.MenuItem;
import javafx.scene.paint.Color;
import tabuterminal.TabuTerminal;
import tabuterminal.TabuTerminalPlugin_V1;

public class CygwinTCSHPlugin extends TabuTerminalPlugin_V1 {
	TerminalConfig tconf = new TerminalConfig();
	TerminalBuilder tbuild = null;
	MenuItem TCSHTabItem = new MenuItem("New TCSH Tab");
	String defaultTerminalCommand = "C:\\cygwin64\\bin\\tcsh.exe -l";
	public CygwinTCSHPlugin(TabuTerminal jtt) {
		super(jtt);
	}

	@Override
	public void initialize(String jf) {
		tconf.setBackgroundColor(Color.rgb(16, 16, 16));
		tconf.setForegroundColor(Color.rgb(240, 240, 240));
		tconf.setCursorColor(Color.rgb(255, 0, 0, 0.5));
		tconf.setWindowsTerminalStarter(defaultTerminalCommand);
		tconf.setCopyOnSelect(true);
		tbuild = new TerminalBuilder(tconf);
		if (!(new File("C:\\cygwin64\\bin\\tcsh.exe").canExecute())){
			this.getTerminalWindow().getLogger().log(Level.SEVERE,"CANNOT EXECUTE C:\\cygwin64\\bin\\tcsh.exe!  Is cygwin tcsh properly installed?");
			return;
		}
		tbuild.setNameGenerator(new TabNameGenerator() {
			
			private long tabNumber = 1;

			public String next() {
				// TODO Auto-generated method stub
				return "TCSH: "+tabNumber;
			}

			

		});
		this.getTerminalWindow().getTabMenu().getItems().add(TCSHTabItem);
		TCSHTabItem.setOnAction(evt -> {
			TerminalTab  tab = tbuild.newTerminal();
			this.getTerminalWindow().getTabPane().getTabs().add(tab);
			this.getTerminalWindow().getTabPane().getSelectionModel().select(tab);
		});
	
	}

	@Override
	public void removePlugin() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getPluginName() {
		// TODO Auto-generated method stub
		return null;
	}

}
```

The plugin extends the abstract class `TabuTerminalPlugin_V1` and implements the relevant Constructor and methods.  

Build the jar, and place it in the plugins dir. 

No prevention for class collisions between plugins is implemented, and no protection of the main UI from the plugins is written.  Only load plugins from trusted sources and use at your own risk.  With this in mind, it may be best to be as parsimonious as possible with the classes you choose to include.  

As the plugin classes are not loaded as part of the default classloader, getting resources out of them can be kind of a pain.  For this reason,  the full path to the JAR file for the plugin is passed to initialize().  If necessary, this can be used to extract any files in the jar using ZIP methods rather than the generic `getResource()` and `getResourceAsStream()` methods from the class/classloader.  

The entire JavaFX Application (of type TabuTerminal) is passed in to the constructor of the plugin, and is available via `this.getTerminalWindow()`.  There are a host of accessor methods (getter/setters) defined in the source code, the most relevant for most plugins being
*  `getMenuBar()` -- useful when you want to and a new menu to the window
* `getFileMenu()` and `getTabMenu()` -- useful when you want to add new menu items to the existing menus. 
* `getTabPane()` -- useful when adding, deleting, etc. tabs to the window.  Added tabs *should* only be or extend TerminalTab (from terminalfx).  
* `getDefaultTerminalBuilder()`, `getDefaultTerminalConfig()`, `getSshTerminalBuilder()`, `getSshTerminalConfig()`, `getTelTerminalBuilder`, `getTelTerminalConfig()` -- useful when you want to change the behavior of default, ssh and telnet tabs respectively.  
* `getPluginMap()` , `setPluginMap()` `loadPlugins()`, `loadPlugins(String)` -- all useful if you wanted to write a real plugin manager as a plugin.  