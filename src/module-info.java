module TabuTerminal
	{
		exports tabuterminal;
		requires transitive com.kodedu.terminalfx;
		requires transitive java.logging;
		requires transitive javafx.base;
		requires transitive javafx.controls;
		requires transitive javafx.graphics;
		requires transitive javafx.swing;
		//exports javafx.graphics/com.sun.javafx.scene;
		requires transitive pty4j;
		requires transitive com.fasterxml.jackson.annotation;
		requires transitive org.apache.commons.lang3;
		requires transitive com.fasterxml.jackson.databind;
		requires transitive java.sql;
		requires transitive gson;
		opens tabuterminal to gson;
	}