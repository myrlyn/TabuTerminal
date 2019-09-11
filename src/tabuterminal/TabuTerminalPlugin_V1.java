package tabuterminal;

import java.util.logging.Level;

public abstract class TabuTerminalPlugin_V1
	{
		private TabuTerminal mainTerminalWindow;

		public TabuTerminalPlugin_V1(TabuTerminal jtt)
			{
				this.mainTerminalWindow = jtt;
			}

		public void applySettings()
			{
				mainTerminalWindow.getLogger().log(Level.INFO, () -> this.getPluginName() + " does not implement any settings");
			}

		public abstract String getPluginName();

		public TabuTerminal getTerminalWindow()
			{
				return mainTerminalWindow;
			}

		public abstract void initialize(String jf);

		public abstract void removePlugin();

		public void saveSettings()
			{
				mainTerminalWindow.getLogger().log(Level.INFO, () -> this.getPluginName() + " does not implement any settings");
			}

		public void setTerminalWindow(TabuTerminal terminalWindow)
			{
				mainTerminalWindow = terminalWindow;
			}
	}
