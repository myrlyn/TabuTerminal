package tabuterminal;

import java.util.logging.Level;

public abstract class TabuTerminalPlugin_V1 {
	private TabuTerminal mainTerminalWindow;
	public TabuTerminal getTerminalWindow() {
		return mainTerminalWindow;
	}
	public void setTerminalWindow(TabuTerminal terminalWindow) {
		mainTerminalWindow = terminalWindow;
	}
	public TabuTerminalPlugin_V1(TabuTerminal jtt) {
		this.mainTerminalWindow = jtt;
	}
	public abstract void initialize(String jf);
	public abstract void removePlugin();
	public abstract String getPluginName();
	public void applySettings() {
		mainTerminalWindow.getLogger().log(Level.INFO,()->this.getPluginName()+" does not implement any settings");
	}
	public void saveSettings() {
		mainTerminalWindow.getLogger().log(Level.INFO,()->this.getPluginName()+" does not implement any settings");
	}
}
