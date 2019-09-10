package tabuterminal;

import java.util.logging.Level;

public abstract class TabuTerminalPlugin_V1 {
	private TabuTerminal TerminalWindow;
	public TabuTerminal getTerminalWindow() {
		return TerminalWindow;
	}
	public void setTerminalWindow(TabuTerminal terminalWindow) {
		TerminalWindow = terminalWindow;
	}
	public TabuTerminalPlugin_V1(TabuTerminal jtt) {
		this.TerminalWindow = jtt;
	}
	public abstract void initialize(String jf);
	public abstract void removePlugin();
	public abstract String getPluginName();
	public void applySettings() {
		TerminalWindow.getLogger().log(Level.INFO,()->{return this.getPluginName()+" does not implement any settings";});
	}
	public void saveSettings() {
		TerminalWindow.getLogger().log(Level.INFO,()->{return this.getPluginName()+" does not implement any settings";});
	}
}
