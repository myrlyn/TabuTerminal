TabuTerminal
============

execute from command line
---------
mvn javafx:run

build .exe
----------
mvn jfx:native


Development
===========
requires 'lib' folder one dir above JAVA_HOME containing ant-javafx.jar for mvn jfx:* commands

JAVA_HOME must refer to JDK11+ JDK



PLUGINS
===========
Plugins must implement the `` TabuTerminalPlugin_V1 `` abstract class. 

Plugins are given a reference to the main TabuTerminal object to modify at need.

Plugins must implement a single-parameter constructor that takes the TabuTerminal object as the parameter.
   
Plugins should implement the removePlugin method that undoes the changes to the TabuTerminal object.  

LICENSE
=========
Licensed under the MIT license for compatibility with underlying components. 
