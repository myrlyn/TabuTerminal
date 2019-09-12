package tabuterminal;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.kodedu.terminalfx.Terminal;
import com.kodedu.terminalfx.TerminalBuilder;
import com.kodedu.terminalfx.TerminalTab;
import com.kodedu.terminalfx.config.TabNameGenerator;
import com.kodedu.terminalfx.config.TerminalConfig;
import com.pty4j.PtyProcess;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class TabuTerminal extends Application
	{
		private static final String BACKGROUND_COLOR_LITERAL = "backgroundColor";
		private static final String FOREGROUND_COLOR_LITERAL = "foregroundColor";
		private static final String CURSOR_COLOR_LITERAL = "cursorColor";
		private static final String ENABLE_CLIPBOARD_NOTICE = "enableClipboardNotice";
		private static final String DEFAULT_CURSOR_COLOR = "#FF0000";
		private static final String SCROLL_BAR_VISIBLE = "scrollBarVisible";
		private static final String WINDOWS_TERMINAL_STARTER = "windowsTerminalStarter";
		private static final String USER_CSS = "userCss";
		private static final String DEFAULT_TERMINAL_CONFIG = "defaultTerminalConfig";
		private static final String USE_DEFAULT_WINDOW_COPY = "useDefaultWindowCopy";
		private static final String UNIX_TERMINAL_STARTER = "unixTerminalStarter";
		private static final String FONT_FAMILY = "fontFamily";
		private static final String SCROLL_WHELL_MOVE_MULTIPLIER = "scrollWhellMoveMultiplier";
		private static final String FONT_FAMILY_DEFAULT = "\"DejaVu Sans Mono\", \"Everson Mono\", FreeMono, \"Menlo\", \"Terminal\", monospace";
		private static final String CURSOR_BLINK = "cursorBlink";
		private static final String CTRL_V_PASTE = "ctrlVPaste";
		private static final String CTRL_C_COPY = "ctrlCCopy";
		private static final String COPY_ON_SELECT = "copyOnSelect";
		private static final String CLEAR_SELECTION_AFTER_COPY = "clearSelectionAfterCopy";
		private static final String BACKGROUND_COLOR = BACKGROUND_COLOR_LITERAL;
		private static final String CURSOR_COLOR = CURSOR_COLOR_LITERAL;
		private static final String DOT_TABU_TERMINAL_STRING = ".TabuTerminal";
		private static final String FOREGROUND_COLOR = FOREGROUND_COLOR_LITERAL;
		private static final String PLUGINS = "plugins";
		private static final String SSH_TERMINAL_CONFIG = "sshTerminalConfig";
		private static final String TEL_TERMINAL_CONFIG = "telTerminalConfig";
		private static final String USER_HOME = "user.home";
		private static final String USER_HOME_PROPERTY_NAME = USER_HOME;
		private Stage defaultTerminalSettingsStage = null;
		
		public static String getDotTabuTerminal()
			{
				return DOT_TABU_TERMINAL_STRING;
			}
			
		public static String getPlugins()
			{
				return PLUGINS;
			}
			
		public static String getUserHomePropertyName()
			{
				return USER_HOME_PROPERTY_NAME;
			}
			
		public static void main(String[] args)
			{
				launch(args);
			}
		private MenuItem applySettingsMenuItem = new MenuItem("Apply Settings");
		private MenuItem closeTabMenuItem = new MenuItem("Close Tab");
		private TerminalBuilder defaultTerminalBuilder = null;
		private String defaultTerminalCommand = "C:\\cygwin64\\bin\\bash.exe -i -l";
		private TerminalConfig defaultTerminalConfig = new TerminalConfig();
		private MenuItem exitAppMenuItem = new MenuItem("Exit");
		private Menu fileMenu = new Menu("File");
		private Logger logger = java.util.logging.Logger.getLogger(TabuTerminal.class.getName());
		private Stage mainWindow;
		private MenuBar menuBar = new MenuBar();
		private MenuItem newTabMenuItem = new MenuItem("New Default Terminal");
		private Map<String, TabuTerminalPlugin_V1> pluginMapV1 = new HashMap<>();
		private MenuItem renameTabMenuItem = new MenuItem("Rename Tab");
		private VBox rootBox = new VBox();
		private MenuItem saveSettingsMenuItem = new MenuItem("Save Settings");
		private Scene scene = null;
		private Map<String, Object> settings = new HashMap<>();
		private Menu settingsMenu = new Menu("Settings");
		private MenuItem sshTabItem = new MenuItem("New SSH Tab");
		private TerminalBuilder sshTerminalBuilder = null;
		private TerminalConfig sshTerminalConfig = new TerminalConfig();
		private Menu tabMenu = new Menu("Tab");
		private TabPane tabPane = new TabPane();
		private MenuItem telTabItem = new MenuItem("New Telnet Tab");
		private TerminalBuilder telTerminalBuilder = null;
		private TerminalConfig telTerminalConfig = null;
		private MenuItem defaultTerminalSettingsItem = new MenuItem("Default Terminal Settings");
		private MenuItem sshTerminalSettingsItem = new MenuItem("SSH Terminal Settings");
		private MenuItem telnetTerminalSettingsItem = new MenuItem("Telnet Terminal Settings");
		
		public void addSSHTab(Stage mainStage)
			{
				Stage dialog = new Stage();
				dialog.initModality(Modality.APPLICATION_MODAL);
				dialog.initOwner(mainStage);
				VBox dialogVBox = new VBox();
				String homeDir = System.getProperty(USER_HOME_PROPERTY_NAME);
				Text userText = new Text("UserName");
				String usernameVal = System.getProperty("user.name");
				if (settings.containsKey("ssh_user"))
					{
						usernameVal = settings.get("ssh_user").toString();
					}
				VBox.setVgrow(userText, Priority.ALWAYS);
				dialogVBox.getChildren().add(userText);
				TextField userNameField = new TextField();
				VBox.setVgrow(userNameField, Priority.ALWAYS);
				dialogVBox.getChildren().add(userNameField);
				userNameField.setText(usernameVal);
				dialogVBox.getChildren().add(new Text("Hostname"));
				TextField hostnameField = new TextField();
				dialogVBox.getChildren().add(hostnameField);
				dialogVBox.getChildren().add(new Text("Ssh Arguments"));
				TextField sshargsField = new TextField();
				dialogVBox.getChildren().add(sshargsField);
				String sshDir = homeDir + File.separator + ".ssh";
				String knownHosts = sshDir + File.separator + "known_hosts";
				dialogVBox.getChildren().add(new Text("Known Hosts File"));
				TextField knownHostsField = new TextField();
				dialogVBox.getChildren().add(knownHostsField);
				knownHostsField.setText(knownHosts);
				URL cfFile = TabuTerminal.class.getResource("ssh/bin/ssh_config");
				String cfFileName = cfFile.getFile();
				File cf = new File(cfFileName);
				String cfs = cf.getAbsolutePath();
				dialogVBox.getChildren().add(new Text("SSH Config File"));
				TextField confFileField = new TextField();
				dialogVBox.getChildren().add(confFileField);
				confFileField.setText(cfs);
				Button connectButton = new Button("Connect");
				connectButton.addEventHandler(ActionEvent.ANY, evt -> {
					String username = userNameField.getText();
					String hostname = hostnameField.getText();
					String args = sshargsField.getText();
					String conf = confFileField.getText();
					String knownHostsfl = knownHostsField.getText();
					URL sshFile = TabuTerminal.class.getResource("ssh/bin/ssh.exe");
					String sshFileName = sshFile.getFile();
					File f = new File(sshFileName);
					String cmd = f.getAbsolutePath();
					String ucmd = "/usr/bin/ssh";
					File sshDirFile = new File(homeDir + File.separator + ".ssh");
					if (!sshDirFile.exists())
						{
							sshDirFile.mkdirs();
						}
					cmd = cmd + " -o UserKnownHostsFile=" + knownHostsfl + " -F " + " " + conf + " " + args + " ";
					ucmd = ucmd + " -o UserKnownHostsFile=" + knownHostsfl + " -F " + " " + conf + " " + args + " ";
					if (!"".equalsIgnoreCase(username))
						{
							cmd += " " + username + "@";
							ucmd += " " + username + "@";
						}
					ucmd += hostname;
					cmd += hostname;
					sshTerminalBuilder.getTerminalConfig().setWindowsTerminalStarter(cmd);
					sshTerminalBuilder.getTerminalConfig().setUnixTerminalStarter(ucmd);
					TerminalTab sshTab = sshTerminalBuilder.newTerminal();
					tabPane.getTabs().add(sshTab);
					tabPane.getSelectionModel().select(sshTab);
					dialog.close();
				});
				dialogVBox.getChildren().add(connectButton);
				Scene dialogScene = new Scene(dialogVBox, 1115, 755);
				dialog.setScene(dialogScene);
				dialog.showAndWait();
			}
			
		public void addTelnetTab(Stage mainStage)
			{
				Stage dialog = new Stage();
				dialog.initModality(Modality.APPLICATION_MODAL);
				dialog.initOwner(mainStage);
				VBox dialogVBox = new VBox();
				Text hostText = new Text("Host");
				dialogVBox.getChildren().add(hostText);
				TextField hostnameField = new TextField();
				dialogVBox.getChildren().add(hostnameField);
				Text portText = new Text("Port");
				TextField portField = new TextField();
				dialogVBox.getChildren().add(portText);
				dialogVBox.getChildren().add(portField);
				portField.setText("23");
				Text argsText = new Text("Arguments");
				dialogVBox.getChildren().add(argsText);
				TextField argsField = new TextField();
				dialogVBox.getChildren().add(argsField);
				Scene dialogScene = new Scene(dialogVBox, 1115, 755);
				dialog.setScene(dialogScene);
				Button connectButton = new Button("Connect");
				connectButton.setOnAction(actev -> {
					URL telUrl = TabuTerminal.class.getResource("ssh/bin/telnet.exe");
					File telFile = new File(telUrl.getFile());
					String telnetcmd = telFile.getAbsolutePath();
					String utelnetcmd = "/usr/bin/telnet";
					telnetcmd = telnetcmd + " " + argsField.getText() + " " + hostnameField.getText() + " " + portField.getText();
					utelnetcmd = utelnetcmd + " " + argsField.getText() + " " + hostnameField.getText() + " " + portField.getText();
					telTerminalBuilder.getTerminalConfig().setWindowsTerminalStarter(telnetcmd);
					telTerminalBuilder.getTerminalConfig().setUnixTerminalStarter(utelnetcmd);
					TerminalTab telnetTab = telTerminalBuilder.newTerminal();
					tabPane.getTabs().add(telnetTab);
					tabPane.getSelectionModel().select(telnetTab);
					dialog.close();
				});
				dialogVBox.getChildren().add(connectButton);
				dialog.showAndWait();
			}
			
		public void addTerminalTab()
			{
				TerminalTab terminal = defaultTerminalBuilder.newTerminal();
				MenuItem contextRenameTab = new MenuItem("Rename Tab");
				contextRenameTab.setOnAction(new EventHandler<ActionEvent>()
					{
						Tab t = terminal;
						
						@Override
						public void handle(ActionEvent event)
							{
								renameTab(t);
							}
					});
				terminal.getContextMenu().getItems().add(contextRenameTab);
				tabPane.getTabs().add(terminal);
				tabPane.getSelectionModel().select(terminal);
			}
			
		public void applySettings()
			{
				TerminalConfig t = null;
				Object o = settings.get("defTerminalConfig");
				if (o instanceof TerminalConfig)
					{
						t = (TerminalConfig) o;
					}
				if (t != null)
					{
						this.setDefaultTerminalConfig(t);
					}
				TerminalConfig st = null;
				Object so = settings.get(SSH_TERMINAL_CONFIG);
				if (so instanceof TerminalConfig)
					{
						st = (TerminalConfig) so;
					}
				if (st != null)
					{
						this.setSshTerminalConfig(st);
					}
				TerminalConfig tt = null;
				Object to = settings.get(TEL_TERMINAL_CONFIG);
				if (to instanceof TerminalConfig)
					{
						tt = (TerminalConfig) to;
					}
				if (tt != null)
					{
						this.setTelTerminalConfig(tt);
					}
				if (settings.containsKey("log_level"))
					{
						setLogLevel((String) (settings.get("log_level")));
					}
				Set<String> pgSet = this.pluginMapV1.keySet();
				for (String pg : pgSet)
					{
						TabuTerminalPlugin_V1 plugin = pluginMapV1.get(pg);
						plugin.applySettings();
					}
			}
			
		public void closeAllChildren(PtyProcess proc)
			{
				try (Stream<ProcessHandle> chstream = proc.children();)
					{
						Iterator<ProcessHandle> childIterator = chstream.iterator();
						while (childIterator.hasNext())
							{
								ProcessHandle childproc = childIterator.next();
								try (Stream<ProcessHandle> proclist = childproc.children();)
									{
										closeAllChildren(proclist);
									}
								childproc.destroy();
							}
					}
			}
			
		public void closeAllChildren(Stream<ProcessHandle> children)
			{
				Iterator<ProcessHandle> childIterator = children.iterator();
				while (childIterator.hasNext())
					{
						ProcessHandle childproc = childIterator.next();
						closeAllChildren(childproc.children());
						childproc.destroy();
					}
			}
			
		public void closeAllChildrenOfTerminaTab(ActionEvent arg0, TerminalTab t)
			{
				Terminal tm = t.getTerminal();
				PtyProcess proc = tm.getProcess();
				closeAllChildren(proc);
				proc.destroy();
				t.closeTerminal(arg0);
			}
			
		public void closeAllTabs()
			{
				ObservableList<Tab> tablist = tabPane.getTabs();
				for (Tab tab : tablist)
					{
						if (tab instanceof TerminalTab)
							{
								((TerminalTab) tab).getProcess().destroy();
								((TerminalTab) tab).destroy();
							}
					}
			}
			
		public void closeAndExit()
			{
				ObservableList<Tab> tabList = tabPane.getTabs();
				for (Tab tab : tabList)
					{
						logger.fine("Close tab " + tab.getText());
						if (tab instanceof TerminalTab)
							{
								TerminalTab ttab = (TerminalTab) tab;
								PtyProcess proc = ttab.getProcess();
								logger.fine("Destroy Process " + proc.getPid());
								proc.destroyForcibly();
							}
					}
				logger.info("Goodbye");
				this.saveSettings();
				System.exit(0);
			}
			
		private void configureBackgroundColor(TerminalConfig tc, Map<String, Object> configMap)
			{
				if (configMap.containsKey(BACKGROUND_COLOR))
					{
						tc.setBackgroundColor(Color.web(configMap.get(BACKGROUND_COLOR).toString()));
						logger.log(Level.FINE, () -> "Set background color from settings: " + configMap.get(BACKGROUND_COLOR).toString());
					}
				else
					{
						tc.setBackgroundColor(Color.rgb(16, 16, 16));
					}
			}
			
		private void configureClearSlectionAfterCopy(TerminalConfig tc, Map<String, Object> configMap)
			{
				if (configMap.containsKey(CLEAR_SELECTION_AFTER_COPY))
					{
						Object o = configMap.get(CLEAR_SELECTION_AFTER_COPY);
						if (o instanceof Boolean)
							{
								tc.setClearSelectionAfterCopy((Boolean) o);
							}
					}
				else
					{
						tc.setClearSelectionAfterCopy(true);
					}
			}
			
		private void configureCopyOnSelect(TerminalConfig tc, Map<String, Object> configMap)
			{
				Boolean b = null;
				if (configMap.containsKey(COPY_ON_SELECT))
					{
						Object o = configMap.get(COPY_ON_SELECT);
						if (o instanceof Boolean)
							{
								b = (Boolean) o;
							}
						if (b != null)
							{
								tc.setCopyOnSelect(b);
							}
						else
							{
								tc.setCopyOnSelect(true);
							}
					}
			}
			
		private void configureCtrlCopy(TerminalConfig tc, Map<String, Object> configMap)
			{
				if (configMap.containsKey(CTRL_C_COPY))
					{
						Object o = configMap.get(CTRL_C_COPY);
						if (o instanceof Boolean)
							{
								tc.setCtrlCCopy((Boolean) o);
							}
					}
				else
					{
						tc.setCtrlCCopy(true);
					}
			}
			
		private void configureCtrlVPaste(TerminalConfig tc, Map<String, Object> configMap)
			{
				if (configMap.containsKey(CTRL_V_PASTE))
					{
						Object o = configMap.get(CTRL_V_PASTE);
						if (o instanceof Boolean)
							{
								tc.setCtrlVPaste((Boolean) o);
							}
					}
				else
					{
						tc.setCtrlVPaste(true);
					}
			}
			
		private void configureCursorBlink(TerminalConfig tc, Map<String, Object> configMap)
			{
				Object cb = configMap.get(CURSOR_BLINK);
				if (cb instanceof Boolean)
					{
						tc.setCursorBlink((Boolean) cb);
					}
				else
					{
						tc.setCursorBlink(true);
					}
			}
			
		private void configureCursorColor(TerminalConfig tc, Map<String, Object> configMap)
			{
				if (configMap.containsKey(CURSOR_COLOR))
					{
						Color webcolor = Color.web(configMap.get(CURSOR_COLOR).toString(), 0.5);
						tc.setCursorColor(webcolor);
						logger.log(Level.FINE, () -> "Set cursor color from settings: " + configMap.get(CURSOR_COLOR).toString());
					}
				else
					{
						tc.setCursorColor(Color.rgb(255, 0, 0, 0.5));
					}
			}
			
		private void configureEnableClipboardNotice(TerminalConfig tc, Map<String, Object> configMap)
			{
				Object cbn = configMap.get("enableCliboardNotice");
				if (cbn instanceof Boolean)
					{
						tc.setEnableClipboardNotice((Boolean) cbn);
					}
				else
					{
						tc.setEnableClipboardNotice(true);
					}
			}
			
		private void configureFontFamily(TerminalConfig tc, Map<String, Object> configMap)
			{
				String fontFamDef = FONT_FAMILY_DEFAULT;
				Object fontFam = configMap.get(FONT_FAMILY);
				if (fontFam != null)
					{
						tc.setFontFamily(fontFam.toString());
					}
				else
					{
						tc.setFontFamily(fontFamDef);
					}
			}
			
		private void configureFontSize(TerminalConfig tc, Map<String, Object> configMap)
			{
				Object fs = configMap.get("fontSize");
				if (fs instanceof Integer)
					{
						tc.setFontSize((Integer) fs);
					}
				else
					{
						tc.setFontSize(14);
					}
			}
			
		private void configureForegroundColor(TerminalConfig tc, Map<String, Object> configMap)
			{
				if (configMap.containsKey(FOREGROUND_COLOR))
					{
						tc.setForegroundColor(Color.web(configMap.get(FOREGROUND_COLOR).toString()));
						logger.log(Level.FINE, () -> "Set foreground color from settings: " + configMap.get(FOREGROUND_COLOR).toString());
					}
				else
					{
						tc.setForegroundColor(Color.rgb(240, 240, 240));
					}
			}
			
		private void configureScrollBarVisible(TerminalConfig tc, Map<String, Object> configMap)
			{
				Object sbv = configMap.get("scrollbarVisible");
				if (sbv instanceof Boolean)
					{
						tc.setScrollbarVisible((Boolean) sbv);
					}
				else
					{
						tc.setScrollbarVisible(true);
					}
			}
			
		private void configureScrollWhellMoveMultiplier(TerminalConfig tc, Map<String, Object> configMap)
			{
				Object swmm = configMap.get(SCROLL_WHELL_MOVE_MULTIPLIER);
				if (swmm instanceof Float)
					{
						tc.setScrollWhellMoveMultiplier((Float) swmm);
					}
				else
					{
						tc.setScrollWhellMoveMultiplier(0.1);
					}
			}
			
		private void configureTerminalConfig(TerminalConfig tc, String configKey)
			{
				logger.log(Level.INFO, () -> "Configure Terminal: " + configKey);
				this.readSettings();
				Object oMap = settings.get(configKey);
				if (!(Map.class.isAssignableFrom(oMap.getClass())))
					{
						logger.log(Level.SEVERE, () -> "misconfigured config element for " + configKey);
					}
				else
					{
						logger.log(Level.INFO, () -> "Loading from configured map");
						Map<String, Object> configMap = typeSafeGetConfigKey(configKey);
						configureBackgroundColor(tc, configMap);
						configureForegroundColor(tc, configMap);
						configureCursorColor(tc, configMap);
						configureWIndowsTerminalStarter(tc, configMap);
						configureCopyOnSelect(tc, configMap);
						configureUseDefaultWindowCopy(tc, configMap);
						configureClearSlectionAfterCopy(tc, configMap);
						configureCtrlCopy(tc, configMap);
						configureCtrlVPaste(tc, configMap);
						configureFontSize(tc, configMap);
						configureCursorBlink(tc, configMap);
						configureScrollBarVisible(tc, configMap);
						configureEnableClipboardNotice(tc, configMap);
						configureScrollWhellMoveMultiplier(tc, configMap);
						configureFontFamily(tc, configMap);
						configureUnixTerminalStarter(tc, configMap);
					}
			}
			
		private void configureUnixTerminalStarter(TerminalConfig tc, Map<String, Object> configMap)
			{
				configureUserCSS(tc, configMap);
				Object uts = configMap.get(UNIX_TERMINAL_STARTER);
				if (uts != null)
					{
						tc.setUnixTerminalStarter(uts.toString());
					}
				else
					{
						tc.setUnixTerminalStarter("/bin/bash -i");
					}
			}
			
		private void configureUseDefaultWindowCopy(TerminalConfig tc, Map<String, Object> configMap)
			{
				if (configMap.containsKey(USE_DEFAULT_WINDOW_COPY))
					{
						Object o = configMap.get(USE_DEFAULT_WINDOW_COPY);
						if (o instanceof Boolean)
							{
								tc.setUseDefaultWindowCopy((Boolean) o);
							}
					}
				else
					{
						tc.setUseDefaultWindowCopy(true);
					}
			}
			
		private void configureUserCSS(TerminalConfig tc, Map<String, Object> configMap)
			{
				Object ucss = configMap.get(USER_CSS);
				if (ucss != null)
					{
						tc.setUserCss(ucss.toString());
					}
				else
					{
						tc.setUserCss("data:text/plain;base64,eC1zY3JlZW4geyBjdXJzb3I6IGF1dG87IH0\\u003d");
					}
			}
			
		private void configureWIndowsTerminalStarter(TerminalConfig tc, Map<String, Object> configMap)
			{
				if (configMap.containsKey(WINDOWS_TERMINAL_STARTER))
					{
						tc.setWindowsTerminalStarter(configMap.get(WINDOWS_TERMINAL_STARTER).toString());
					}
				else
					{
						tc.setWindowsTerminalStarter(defaultTerminalCommand);
					}
			}
			
		public String findBashPrompt()
			{
				String cmdPrompt = "";
				for (String dirname : System.getenv("PATH").split(File.pathSeparator))
					{
						File file = new File(dirname, "bash.exe");
						if (file.isFile() && file.canExecute())
							{
								cmdPrompt = file.getAbsolutePath();
							}
					}
				return cmdPrompt + " -i -l";
			}
			
		public String findCmdPrompt()
			{
				String cmdPrompt = "";
				for (String dirname : System.getenv("PATH").split(File.pathSeparator))
					{
						File file = new File(dirname, "cmd.exe");
						if (file.isFile() && file.canExecute())
							{
								cmdPrompt = file.getAbsolutePath();
							}
					}
				return cmdPrompt;
			}
			
		public String findShPrompt()
			{
				String cmdPrompt = "";
				for (String dirname : System.getenv("PATH").split(File.pathSeparator))
					{
						File file = new File(dirname, "sh.exe");
						if (file.isFile() && file.canExecute())
							{
								cmdPrompt = file.getAbsolutePath();
							}
					}
				return cmdPrompt;
			}
			
		public MenuItem getApplySettingsMenuItem()
			{
				return applySettingsMenuItem;
			}
			
		public MenuItem getCloseTabMenuItem()
			{
				return closeTabMenuItem;
			}
			
		public TerminalBuilder getDefaultTerminalBuilder()
			{
				return defaultTerminalBuilder;
			}
			
		public String getDefaultTerminalCommand()
			{
				return defaultTerminalCommand;
			}
			
		public TerminalConfig getDefaultTerminalConfig()
			{
				return defaultTerminalConfig;
			}
			
		public MenuItem getExitAppMenuItem()
			{
				return exitAppMenuItem;
			}
			
		public Menu getFileMenu()
			{
				return fileMenu;
			}
			
		public Logger getLogger()
			{
				return logger;
			}
			
		public Stage getMainWindow()
			{
				return mainWindow;
			}
			
		public MenuBar getMenuBar()
			{
				return menuBar;
			}
			
		public MenuItem getNewTabMenuItem()
			{
				return newTabMenuItem;
			}
			
		public Map<String, TabuTerminalPlugin_V1> getPluginMapV1()
			{
				return pluginMapV1;
			}
			
		public MenuItem getRenameTabMenuItem()
			{
				return renameTabMenuItem;
			}
			
		public VBox getRootBox()
			{
				return rootBox;
			}
			
		public MenuItem getSaveSettingsMenuItem()
			{
				return saveSettingsMenuItem;
			}
			
		public Scene getScene()
			{
				return scene;
			}
			
		public Map<String, Object> getSettings()
			{
				return settings;
			}
			
		public Menu getSettingsMenu()
			{
				return settingsMenu;
			}
			
		public MenuItem getSshTabItem()
			{
				return sshTabItem;
			}
			
		public TerminalBuilder getSshTerminalBuilder()
			{
				return sshTerminalBuilder;
			}
			
		public TerminalConfig getSshTerminalConfig()
			{
				return sshTerminalConfig;
			}
			
		public Menu getTabMenu()
			{
				return tabMenu;
			}
			
		public TabPane getTabPane()
			{
				return tabPane;
			}
			
		public MenuItem getTelTabItem()
			{
				return telTabItem;
			}
			
		public TerminalBuilder getTelTerminalBuilder()
			{
				return telTerminalBuilder;
			}
			
		public TerminalConfig getTelTerminalConfig()
			{
				return telTerminalConfig;
			}
			
		public void loadAndInitializePlugin(String jar)
			{
				logger.log(Level.INFO, () -> "Initializ this jar: " + jar);
				try (
				    JarFile jf = new JarFile(System.getProperty(USER_HOME_PROPERTY_NAME) + File.separator + DOT_TABU_TERMINAL_STRING
				        + File.separator + PLUGINS + File.separator + jar);
				)
					{
						logger.log(Level.INFO, () -> "Check Jar File" + jf.toString());
						Enumeration<JarEntry> pluginEntries = jf.entries();
						URL[] urls =
							{ new URL("jar:file:" + System.getProperty(USER_HOME_PROPERTY_NAME) + File.separator + DOT_TABU_TERMINAL_STRING
							    + File.separator + PLUGINS + File.separator + jar + "!/") };
						logger.log(Level.INFO, () -> "Loader URL: " + urls[0]);
						try (URLClassLoader jarloader = URLClassLoader.newInstance(urls);)
							{
								List<Class<TabuTerminalPlugin_V1>> pluginsToInit;
								pluginsToInit = new LinkedList<>();
								while (pluginEntries.hasMoreElements())
									{
										JarEntry entry = pluginEntries.nextElement();
										if (!entry.isDirectory() && entry.getName().endsWith("class"))
											{
												// -6 because ".class".length == 6, so to strip '.class off the end of the
												// classname we want to load, we remove the last 6 characters
												// replace separators with dots to construct full classmame
												String className = entry.getName().substring(0, entry.getName().length() - 6).replace('/', '.');
												logger.log(Level.INFO, () -> "Class Name: " + className);
												Class<?> c = jarloader.loadClass(className);
												if (TabuTerminalPlugin_V1.class.isAssignableFrom(c))
													{
														@SuppressWarnings("unchecked") // I just checked the typesafety of this above...
														Class<TabuTerminalPlugin_V1> plugToInitTyped = (Class<TabuTerminalPlugin_V1>) c;
														pluginsToInit.add(plugToInitTyped);
														// SPOTBUGS complains, but the
														// type-checking her is done...
													}
											}
									}
								for (Class<TabuTerminalPlugin_V1> plugin : pluginsToInit)
									{
										Constructor<TabuTerminalPlugin_V1> pluginConstructor = plugin.getConstructor(TabuTerminal.class);
										TabuTerminalPlugin_V1 plug = pluginConstructor.newInstance(this);
										logger.info("Initialize this!  " + plug.getPluginName());
										plug.initialize(System.getProperty(USER_HOME_PROPERTY_NAME) + File.separator + DOT_TABU_TERMINAL_STRING + File.separator
										    + PLUGINS + File.separator + jar);
										this.pluginMapV1.put(plug.getPluginName(), plug);
									}
							}
					}
				catch (
				    IOException | ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException
				    | IllegalAccessException | IllegalArgumentException | InvocationTargetException e
				)
					{
						logger.log(Level.WARNING, "ERROR LOADING JAR PLUGIN", e);
					}
			}
			
		public void loadPlugins()
			{
				loadPlugins(System.getProperty(USER_HOME_PROPERTY_NAME) + File.separator + DOT_TABU_TERMINAL_STRING + File.separator + PLUGINS
				    + File.separator);
			}
			
		public void loadPlugins(String pluginsDir)
			{
				File pd = new File(pluginsDir);
				if (pd.exists())
					{
						if (pd.isDirectory())
							{
								String[] jars = pd.list((File dir, String name) -> name.toLowerCase().endsWith(".jar"));
								for (String jar : jars)
									{
										logger.log(Level.INFO, () -> "Found Jar " + jar);
										loadAndInitializePlugin(jar);
									}
							}
						else
							{
								logger.warning("plugin directory name refers to file, not directory!");
							}
					}
				else
					{
						pd.mkdirs();
					}
			}
			
		public Map<String, Object> readSettings()
			{
				Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().create();
				File settingsFile = new File(
				    System.getProperty(USER_HOME) + File.separator + DOT_TABU_TERMINAL_STRING + File.separator + "settings.json");
				if (settingsFile.exists())
					{
						try (JsonReader settingsReader = new JsonReader(new FileReader(settingsFile)))
							{
								typeSaveSettingsAssignment(gson, settingsReader);
							}
						catch (IOException e)
							{
								logger.log(Level.SEVERE, "Error reading settings", e);
							}
					}
				else
					{
						logger.info("settings file does not exist");
					}
				return settings;
			}
			
		public void renameTab()
			{
				Tab currentTab = this.tabPane.getSelectionModel().getSelectedItem();
				renameTab(currentTab);
			}
			
		public void renameTab(Tab currentTab)
			{
				TextInputDialog td = new TextInputDialog(currentTab.getText());
				Optional<String> newName = td.showAndWait();
				if (newName.isPresent())
					{
						currentTab.setText(newName.get());
					}
			}
			
		public void saveSettings()
			{
				Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().create();
				settings.put(DEFAULT_TERMINAL_CONFIG, this.getDefaultTerminalConfig());
				settings.put(SSH_TERMINAL_CONFIG, this.getSshTerminalConfig());
				settings.put(TEL_TERMINAL_CONFIG, this.getTelTerminalConfig());
				File settingsFile = new File(
				    System.getProperty(USER_HOME) + File.separator + DOT_TABU_TERMINAL_STRING + File.separator + "settings.json");
				Set<String> pgSet = this.pluginMapV1.keySet();
				for (String pg : pgSet)
					{
						TabuTerminalPlugin_V1 plugin = pluginMapV1.get(pg);
						plugin.saveSettings();
					}
				try (FileWriter settingsWriter = new FileWriter(settingsFile))
					{
						settingsWriter.write(gson.toJson(settings));
						settingsWriter.flush();
					}
				catch (IOException ioe)
					{
						logger.log(Level.SEVERE, "Error Writing to Settings File", ioe);
					}
			}
			
		public void setApplySettingsMenuItem(MenuItem applySettingsMenuItem)
			{
				this.applySettingsMenuItem = applySettingsMenuItem;
			}
			
		public void setCloseTab(MenuItem closeTab)
			{
				this.closeTabMenuItem = closeTab;
			}
			
		public void setCloseTabMenuItem(MenuItem closeTabMenuItem)
			{
				this.closeTabMenuItem = closeTabMenuItem;
			}
			
		public void setDefaultTerminalBuilder(TerminalBuilder defaultTerminalBuilder)
			{
				this.defaultTerminalBuilder = defaultTerminalBuilder;
			}
			
		public void setDefaultTerminalCommand(String defaultTerminalCommand)
			{
				this.defaultTerminalCommand = defaultTerminalCommand;
			}
			
		public void setDefaultTerminalConfig(TerminalConfig defaultTerminalConfig)
			{
				logger.info("SETTING TERMINAL CONFIG");
				this.defaultTerminalConfig = defaultTerminalConfig;
				this.getDefaultTerminalBuilder().setTerminalConfig(defaultTerminalConfig);
			}
			
		public void setExitApp(MenuItem exitApp)
			{
				this.exitAppMenuItem = exitApp;
			}
			
		public void setExitAppMenuItem(MenuItem exitAppMenuItem)
			{
				this.exitAppMenuItem = exitAppMenuItem;
			}
			
		public void setFileMenu(Menu fileMenu)
			{
				this.fileMenu = fileMenu;
			}
			
		public void setLogger(Logger logger)
			{
				this.logger = logger;
			}
			
		public void setLogLevel(String lv)
			{
				logger.log(Level.FINE, () -> "SET LOG LEVEL TO " + lv);
				switch (lv)
					{
						case "INFO":
							logger.setLevel(Level.INFO);
							break;
						case "CONFIG":
							logger.setLevel(Level.CONFIG);
							break;
						case "FINE":
							logger.setLevel(Level.FINE);
							break;
						case "FINER":
							logger.setLevel(Level.FINER);
							break;
						case "FINEST":
							logger.setLevel(Level.FINEST);
							break;
						case "OFF":
							logger.setLevel(Level.OFF);
							break;
						case "SEVERE":
							logger.setLevel(Level.SEVERE);
							break;
						case "WARNING":
							logger.setLevel(Level.WARNING);
							break;
						case "ALL":
							logger.setLevel(Level.ALL);
							break;
						default:
							logger.setLevel(Level.INFO);
					}
				logger.info(logger.getLevel().getName());
			}
			
		public void setMainWindow(Stage mainWindow)
			{
				this.mainWindow = mainWindow;
			}
			
		public void setMenuBar(MenuBar menuBar)
			{
				this.menuBar = menuBar;
			}
			
		public void setNewTab(MenuItem newTab)
			{
				this.newTabMenuItem = newTab;
			}
			
		public void setNewTabMenuItem(MenuItem newTabMenuItem)
			{
				this.newTabMenuItem = newTabMenuItem;
			}
			
		public void setPluginMapV1(Map<String, TabuTerminalPlugin_V1> pluginMapV1)
			{
				this.pluginMapV1 = pluginMapV1;
			}
			
		public void setRenameTabMenuItem(MenuItem renameTabMenuItem)
			{
				this.renameTabMenuItem = renameTabMenuItem;
			}
			
		public void setRootBox(VBox rootBox)
			{
				this.rootBox = rootBox;
			}
			
		public void setSaveSettingsMenuItem(MenuItem saveSettingsMenuItem)
			{
				this.saveSettingsMenuItem = saveSettingsMenuItem;
			}
			
		public void setScene(Scene scene)
			{
				this.scene = scene;
			}
			
		public void setSettings(Map<String, Object> settings)
			{
				this.settings = settings;
			}
			
		public void setSettingsMenu(Menu settingsMenu)
			{
				this.settingsMenu = settingsMenu;
			}
			
		public void setSshTabItem(MenuItem sshTabItem)
			{
				this.sshTabItem = sshTabItem;
			}
			
		public void setSshTerminalBuilder(TerminalBuilder sshTerminalBuilder)
			{
				this.sshTerminalBuilder = sshTerminalBuilder;
			}
			
		public void setSshTerminalConfig(TerminalConfig sshTerminalConfig)
			{
				this.sshTerminalConfig = sshTerminalConfig;
				this.getSshTerminalBuilder().setTerminalConfig(this.sshTerminalConfig);
			}
			
		public void setTabMenu(Menu tabMenu)
			{
				this.tabMenu = tabMenu;
			}
			
		public void setTabPane(TabPane tabPane)
			{
				this.tabPane = tabPane;
			}
			
		public void setTelTabItem(MenuItem telTabItem)
			{
				this.telTabItem = telTabItem;
			}
			
		public void setTelTerminalBuilder(TerminalBuilder telTerminalBuilder)
			{
				this.telTerminalBuilder = telTerminalBuilder;
			}
			
		public void setTelTerminalConfig(TerminalConfig telTerminalConfig)
			{
				this.telTerminalConfig = telTerminalConfig;
				this.getTelTerminalBuilder().setTerminalConfig(this.telTerminalConfig);
			}
			
		@Override
		public void start(Stage primaryStage)
		    throws Exception
			{
				this.setMainWindow(primaryStage);
				primaryStage.setOnCloseRequest((WindowEvent event) -> closeAndExit());
				configureTerminalConfig(defaultTerminalConfig, DEFAULT_TERMINAL_CONFIG);
				defaultTerminalBuilder = new TerminalBuilder(defaultTerminalConfig);
				sshTerminalConfig = new TerminalConfig();
				configureTerminalConfig(sshTerminalConfig, SSH_TERMINAL_CONFIG);
				sshTerminalBuilder = new TerminalBuilder(sshTerminalConfig);
				sshTerminalBuilder.setNameGenerator(new TabNameGenerator()
					{
						private long tabNumber = 1;
						
						@Override
						public String next()
							{
								return "SSH: " + tabNumber++;
							}
					});
				telTerminalConfig = new TerminalConfig();
				configureTerminalConfig(telTerminalConfig, TEL_TERMINAL_CONFIG);
				telTerminalBuilder = new TerminalBuilder(sshTerminalConfig);
				telTerminalBuilder.setNameGenerator(new TabNameGenerator()
					{
						private long tabNumber = 1;
						
						@Override
						public String next()
							{
								return "Telnet: " + tabNumber++;
							}
					});
				tabMenu.getItems().add(newTabMenuItem);
				tabMenu.getItems().add(closeTabMenuItem);
				fileMenu.getItems().add(exitAppMenuItem);
				menuBar.getMenus().add(fileMenu);
				menuBar.getMenus().add(tabMenu);
				newTabMenuItem.setOnAction((ActionEvent act) -> addTerminalTab());
				renameTabMenuItem.setOnAction((ActionEvent event) -> renameTab());
				closeTabMenuItem.setOnAction((ActionEvent evt) -> closeAndExit());
				exitAppMenuItem.setOnAction((ActionEvent evt) -> closeAndExit());
				tabMenu.getItems().add(sshTabItem);
				sshTabItem.setOnAction(evt -> this.addSSHTab(primaryStage));
				tabMenu.getItems().add(telTabItem);
				telTabItem.setOnAction(evt -> this.addTelnetTab(primaryStage));
				menuBar.getMenus().add(settingsMenu);
				settingsMenu.getItems().add(saveSettingsMenuItem);
				settingsMenu.getItems().add(applySettingsMenuItem);
				settingsMenu.getItems().add(defaultTerminalSettingsItem);
				settingsMenu.getItems().add(sshTerminalSettingsItem);
				settingsMenu.getItems().add(telnetTerminalSettingsItem);
				this.defaultTerminalSettingsItem.setOnAction(evt -> {
					Object omap =  settings.get(DEFAULT_TERMINAL_CONFIG);
					boolean typesafe = true;
					if (omap instanceof Map) {
						for (Entry<?,?> e : ((Map<?,?>)omap).entrySet()) {
							if (!((e.getKey() instanceof String) &&(e.getValue() instanceof Object))) {
								typesafe = false;
								break;
							}
						}
					}
					Map<String, Object> termMap = null;
					if (typesafe) {
						termMap = (Map<String,Object>)omap;
					}else {
						termMap = new HashMap<>();
					}
					this.defaultTerminalSettingsStage =getTerminalBuilderConfigurationWindow(this.defaultTerminalSettingsStage, termMap,DEFAULT_TERMINAL_CONFIG );
					this.defaultTerminalSettingsStage.showAndWait();
				});
				saveSettingsMenuItem.setOnAction(evt -> this.saveSettings());
				applySettingsMenuItem.setOnAction(evt -> this.applySettings());
				rootBox.getChildren().add(menuBar);
				VBox.setVgrow(tabPane, Priority.ALWAYS);
				rootBox.getChildren().add(tabPane);
				// gets a 132x43 character terminal, which is a standard size
				scene = new Scene(rootBox, 1115, 755);
				primaryStage.getIcons().add(new Image(TabuTerminal.class.getResourceAsStream("tabu.png")));
				primaryStage.setScene(scene);
				this.applySettings();
				loadPlugins();
				addTerminalTab();
				primaryStage.show();
			}
			
		private Stage getTerminalBuilderConfigurationWindow(Stage settingsWindow, Map<String,Object> termsettings, String settingsKey)
			{
				if (settingsWindow == null)
					{
						settingsWindow = new Stage();
						settingsWindow.initModality(Modality.APPLICATION_MODAL);
						settingsWindow.initOwner(this.getMainWindow());
						VBox dialogVBox = new VBox();
						// add line item for default window copy
						HBox useDefaultWindowCopyBox = new HBox();
						VBox.setVgrow(useDefaultWindowCopyBox, Priority.ALWAYS);
						Text useDefaultWindowCopyText = new Text(USE_DEFAULT_WINDOW_COPY);
						HBox.setHgrow(useDefaultWindowCopyText, Priority.ALWAYS);
						CheckBox useDefaultWindowCopyCheckBox = new CheckBox();
						HBox.setHgrow(useDefaultWindowCopyCheckBox, Priority.ALWAYS);
						Object useDefaultWindowCopyObject = termsettings.get(USE_DEFAULT_WINDOW_COPY);
						if (useDefaultWindowCopyObject instanceof Boolean)
							useDefaultWindowCopyCheckBox.setSelected((Boolean) useDefaultWindowCopyObject);
						else
							useDefaultWindowCopyCheckBox.setSelected(true);
						useDefaultWindowCopyBox.getChildren().add(useDefaultWindowCopyText);
						useDefaultWindowCopyBox.getChildren().add(useDefaultWindowCopyCheckBox);
						dialogVBox.getChildren().add(useDefaultWindowCopyBox);
						// same for clear selection on copy
						HBox clearSelectionAfterCopyBox = new HBox();
						VBox.setVgrow(clearSelectionAfterCopyBox, Priority.ALWAYS);
						Text clearSelectionAfterCopyText = new Text(CLEAR_SELECTION_AFTER_COPY);
						HBox.setHgrow(clearSelectionAfterCopyText, Priority.ALWAYS);
						CheckBox clearSelectionAfterCopyCheckBox = new CheckBox();
						HBox.setHgrow(clearSelectionAfterCopyCheckBox, Priority.ALWAYS);
						clearSelectionAfterCopyBox.getChildren().add(clearSelectionAfterCopyText);
						clearSelectionAfterCopyBox.getChildren().add(clearSelectionAfterCopyCheckBox);
						if (termsettings.get(CLEAR_SELECTION_AFTER_COPY) instanceof Boolean)
							clearSelectionAfterCopyCheckBox.setSelected((Boolean) termsettings.get(CLEAR_SELECTION_AFTER_COPY));
						else
							clearSelectionAfterCopyCheckBox.setSelected(true);
						dialogVBox.getChildren().add(clearSelectionAfterCopyBox);
						// same for copy on select
						// same for clear selection on copy
						HBox copyOnSelectBox = new HBox();
						VBox.setVgrow(copyOnSelectBox, Priority.ALWAYS);
						Text copyOnSelectText = new Text(COPY_ON_SELECT);
						HBox.setHgrow(copyOnSelectText, Priority.ALWAYS);
						CheckBox copyOnSelectCheckBox = new CheckBox();
						HBox.setHgrow(copyOnSelectCheckBox, Priority.ALWAYS);
						copyOnSelectBox.getChildren().add(copyOnSelectText);
						copyOnSelectBox.getChildren().add(copyOnSelectCheckBox);
						if (termsettings.get(COPY_ON_SELECT) instanceof Boolean)
							copyOnSelectCheckBox.setSelected((Boolean) termsettings.get(COPY_ON_SELECT));
						else
							copyOnSelectCheckBox.setSelected(true);
						dialogVBox.getChildren().add(copyOnSelectBox);
						// same for CTRLCCOPY
						HBox ctrlCCopyBox = new HBox();
						VBox.setVgrow(ctrlCCopyBox, Priority.ALWAYS);
						Text ctrlCCopyText = new Text(CTRL_C_COPY);
						HBox.setHgrow(ctrlCCopyText, Priority.ALWAYS);
						CheckBox ctrlCCopyCheckBox = new CheckBox();
						HBox.setHgrow(ctrlCCopyCheckBox, Priority.ALWAYS);
						ctrlCCopyBox.getChildren().add(ctrlCCopyText);
						ctrlCCopyBox.getChildren().add(ctrlCCopyCheckBox);
						if (termsettings.get(CTRL_C_COPY) instanceof Boolean)
							ctrlCCopyCheckBox.setSelected((Boolean) termsettings.get(CTRL_C_COPY));
						else
							ctrlCCopyCheckBox.setSelected(true);
						dialogVBox.getChildren().add(ctrlCCopyBox);
						// same for ctrlVPaste
						HBox ctrlVPasteBox = new HBox();
						VBox.setVgrow(ctrlVPasteBox, Priority.ALWAYS);
						Text ctrlVPasteText = new Text(CTRL_V_PASTE);
						HBox.setHgrow(ctrlVPasteText, Priority.ALWAYS);
						CheckBox ctrlVPasteCheckBox = new CheckBox();
						HBox.setHgrow(ctrlVPasteCheckBox, Priority.ALWAYS);
						ctrlVPasteBox.getChildren().add(ctrlVPasteText);
						ctrlVPasteBox.getChildren().add(ctrlVPasteCheckBox);
						if (termsettings.get(CTRL_V_PASTE) instanceof Boolean)
							ctrlVPasteCheckBox.setSelected((Boolean) termsettings.get(CTRL_V_PASTE));
						else
							ctrlVPasteCheckBox.setSelected(true);
						dialogVBox.getChildren().add(ctrlVPasteBox);
						// same for cursorColor
						// ColorPicker
						HBox cursorColorBox = new HBox();
						VBox.setVgrow(cursorColorBox, Priority.ALWAYS);
						Text cursorColorText = new Text(CTRL_V_PASTE);
						HBox.setHgrow(cursorColorText, Priority.ALWAYS);
						Color c = Color.web(DEFAULT_CURSOR_COLOR);
						ColorPicker cursorColorPicker = new ColorPicker(c);
				
						if (termsettings.get(CURSOR_COLOR_LITERAL) instanceof String)
							{
								c = Color.web(termsettings.get(CURSOR_COLOR_LITERAL).toString());
								cursorColorPicker.setValue(c);
							}
						HBox.setHgrow(cursorColorPicker, Priority.ALWAYS);
						cursorColorBox.getChildren().add(cursorColorText);
						cursorColorBox.getChildren().add(cursorColorPicker);
						dialogVBox.getChildren().add(cursorColorBox);
						// same for backgroundColor
						// ColorPicker
						HBox backgroundColorBox = new HBox();
						VBox.setVgrow(backgroundColorBox, Priority.ALWAYS);
						Text backgroundColorText = new Text(CTRL_V_PASTE);
						HBox.setHgrow(backgroundColorText, Priority.ALWAYS);
						Color cc = Color.web(DEFAULT_CURSOR_COLOR);
						ColorPicker backgroundColorPicker = new ColorPicker(cc);
						if (termsettings.get(BACKGROUND_COLOR_LITERAL) instanceof String)
							{
								c = Color.web(termsettings.get(BACKGROUND_COLOR_LITERAL).toString());
								backgroundColorPicker.setValue(cc);
							}
						HBox.setHgrow(backgroundColorPicker, Priority.ALWAYS);
						backgroundColorBox.getChildren().add(backgroundColorText);
						backgroundColorBox.getChildren().add(backgroundColorPicker);
						dialogVBox.getChildren().add(backgroundColorBox);
						// same for foregroundColor
						// ColorPicker
						HBox foregroundColorBox = new HBox();
						VBox.setVgrow(foregroundColorBox, Priority.ALWAYS);
						Text foregroundColorText = new Text(CTRL_V_PASTE);
						HBox.setHgrow(foregroundColorText, Priority.ALWAYS);
						Color cv = Color.web(DEFAULT_CURSOR_COLOR);
						ColorPicker foregroundColorPicker = new ColorPicker(cv);
						if (termsettings.get(FOREGROUND_COLOR_LITERAL) instanceof String)
							{
								Color.web(termsettings.get(FOREGROUND_COLOR_LITERAL).toString());
								foregroundColorPicker.setValue(cv);
							}
						HBox.setHgrow(foregroundColorPicker, Priority.ALWAYS);
						foregroundColorBox.getChildren().add(foregroundColorText);
						foregroundColorBox.getChildren().add(foregroundColorPicker);
						dialogVBox.getChildren().add(foregroundColorBox);
						// same for Font Size
						HBox fontSizeBox = new HBox();
						VBox.setVgrow(fontSizeBox, Priority.ALWAYS);
						Text fontSizeText = new Text("fontSize");
						HBox.setHgrow(fontSizeText, Priority.ALWAYS);
						Spinner<Integer> fontSizeSpinner = new Spinner<>();
						SpinnerValueFactory<Integer> fontSizeSpinnerFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(3, 128);
						fontSizeSpinner.setValueFactory(fontSizeSpinnerFactory);
						HBox.setHgrow(fontSizeSpinner, Priority.ALWAYS);
						fontSizeBox.getChildren().add(fontSizeText);
						fontSizeBox.getChildren().add(fontSizeSpinner);
						dialogVBox.getChildren().add(fontSizeBox);
						// same for cursor blink
						HBox cursorBlinkBox = new HBox();
						VBox.setVgrow(cursorBlinkBox, Priority.ALWAYS);
						Text cursorBlinkText = new Text(CURSOR_BLINK);
						HBox.setHgrow(cursorBlinkText, Priority.ALWAYS);
						CheckBox cursorBlinkCheckBox = new CheckBox();
						HBox.setHgrow(cursorBlinkCheckBox, Priority.ALWAYS);
						cursorBlinkBox.getChildren().add(cursorBlinkText);
						cursorBlinkBox.getChildren().add(cursorBlinkCheckBox);
						if (termsettings.get(CURSOR_BLINK) instanceof Boolean)
							cursorBlinkCheckBox.setSelected((Boolean) termsettings.get(CURSOR_BLINK));
						else
							cursorBlinkCheckBox.setSelected(true);
						dialogVBox.getChildren().add(cursorBlinkBox);
						// same for scrollBar visible
						HBox scrollBarVisibleBox = new HBox();
						VBox.setVgrow(scrollBarVisibleBox, Priority.ALWAYS);
						Text scrollBarVisibleText = new Text(SCROLL_BAR_VISIBLE);
						HBox.setHgrow(scrollBarVisibleText, Priority.ALWAYS);
						CheckBox scrollBarVisibleCheckBox = new CheckBox();
						HBox.setHgrow(scrollBarVisibleCheckBox, Priority.ALWAYS);
						scrollBarVisibleBox.getChildren().add(scrollBarVisibleText);
						scrollBarVisibleBox.getChildren().add(scrollBarVisibleCheckBox);
						if (termsettings.get(SCROLL_BAR_VISIBLE) instanceof Boolean)
							scrollBarVisibleCheckBox.setSelected((Boolean) termsettings.get(SCROLL_BAR_VISIBLE));
						else
							scrollBarVisibleCheckBox.setSelected(true);
						dialogVBox.getChildren().add(scrollBarVisibleBox);
						// same for enableClipboardNotice
						HBox enableClipboardNoticeBox = new HBox();
						VBox.setVgrow(enableClipboardNoticeBox, Priority.ALWAYS);
						Text enableClipboardNoticeText = new Text(ENABLE_CLIPBOARD_NOTICE);
						HBox.setHgrow(enableClipboardNoticeText, Priority.ALWAYS);
						CheckBox enableClipboardNoticeCheckBox = new CheckBox();
						HBox.setHgrow(enableClipboardNoticeCheckBox, Priority.ALWAYS);
						enableClipboardNoticeBox.getChildren().add(enableClipboardNoticeText);
						enableClipboardNoticeBox.getChildren().add(enableClipboardNoticeCheckBox);
						if (termsettings.get(ENABLE_CLIPBOARD_NOTICE) instanceof Boolean)
							enableClipboardNoticeCheckBox.setSelected((Boolean) termsettings.get(ENABLE_CLIPBOARD_NOTICE));
						else
							enableClipboardNoticeCheckBox.setSelected(true);
						dialogVBox.getChildren().add(enableClipboardNoticeBox);
						// same for scrollWhellMoveMultiplier
						HBox scrollWhellMoveMultiplierBox = new HBox();
						VBox.setVgrow(scrollWhellMoveMultiplierBox, Priority.ALWAYS);
						Text scrollWhellMoveMultiplierText = new Text(SCROLL_WHELL_MOVE_MULTIPLIER);
						HBox.setHgrow(scrollWhellMoveMultiplierText, Priority.ALWAYS);
						Spinner<Double> scrollWhellMoveMultiplierSpinner = new Spinner<>();
						Double initVal = 0.1;
						if (termsettings.get(SCROLL_WHELL_MOVE_MULTIPLIER) instanceof Double)
							{
								initVal = (Double) termsettings.get(SCROLL_WHELL_MOVE_MULTIPLIER);
							}
						SpinnerValueFactory<
						    Double> scrollWhellMoveMultiplierSpinnerFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0.1, 100.0, initVal);
						scrollWhellMoveMultiplierSpinner.setValueFactory(scrollWhellMoveMultiplierSpinnerFactory);
						HBox.setHgrow(scrollWhellMoveMultiplierSpinner, Priority.ALWAYS);
						scrollWhellMoveMultiplierBox.getChildren().add(scrollWhellMoveMultiplierText);
						scrollWhellMoveMultiplierBox.getChildren().add(scrollWhellMoveMultiplierSpinner);
						dialogVBox.getChildren().add(scrollWhellMoveMultiplierBox);
						// same for font family
						HBox fontFamilyBox = new HBox();
						VBox.setVgrow(fontFamilyBox, Priority.ALWAYS);
						Text fontFamilyText = new Text(FONT_FAMILY);
						HBox.setHgrow(fontFamilyText, Priority.ALWAYS);
						TextField fontFamilyTextField = new TextField();
						if (termsettings.get(FONT_FAMILY) instanceof String)
							{
								fontFamilyTextField.setText(termsettings.get(FONT_FAMILY).toString());
							}
						else
							{
								fontFamilyTextField.setText(FONT_FAMILY_DEFAULT);
							}
						HBox.setHgrow(fontFamilyTextField, Priority.ALWAYS);
						fontFamilyBox.getChildren().add(fontFamilyText);
						fontFamilyBox.getChildren().add(fontFamilyTextField);
						dialogVBox.getChildren().add(fontFamilyBox);
						// same for userCss
						HBox userCssBox = new HBox();
						VBox.setVgrow(userCssBox, Priority.ALWAYS);
						Text userCssText = new Text(USER_CSS);
						HBox.setHgrow(userCssText, Priority.ALWAYS);
						TextField userCssTextField = new TextField();
						if (termsettings.get(USER_CSS) instanceof String)
							{
								userCssTextField.setText(termsettings.get(USER_CSS).toString());
							}
						else
							{
								userCssTextField.setText("data:text/plain;base64,eC1zY3JlZW4geyBjdXJzb3I6IGF1dG87IH0\\u003d");
							}
						HBox.setHgrow(userCssTextField, Priority.ALWAYS);
						userCssBox.getChildren().add(userCssText);
						userCssBox.getChildren().add(userCssTextField);
						dialogVBox.getChildren().add(userCssBox);
						// same for windowsTerminalStarter
						HBox windowsTerminalStarterBox = new HBox();
						VBox.setVgrow(windowsTerminalStarterBox, Priority.ALWAYS);
						Text windowsTerminalStarterText = new Text(WINDOWS_TERMINAL_STARTER);
						HBox.setHgrow(windowsTerminalStarterText, Priority.ALWAYS);
						TextField windowsTerminalStarterTextField = new TextField();
						if (termsettings.get(WINDOWS_TERMINAL_STARTER) instanceof String)
							{
								windowsTerminalStarterTextField.setText(termsettings.get(WINDOWS_TERMINAL_STARTER).toString());
							}
						else
							{
								windowsTerminalStarterTextField
								    .setText(FONT_FAMILY_DEFAULT);
							}
						HBox.setHgrow(windowsTerminalStarterTextField, Priority.ALWAYS);
						windowsTerminalStarterBox.getChildren().add(windowsTerminalStarterText);
						windowsTerminalStarterBox.getChildren().add(windowsTerminalStarterTextField);
						dialogVBox.getChildren().add(windowsTerminalStarterBox);
						// same for unixTerminalStarter
						HBox unixTerminalStarterBox = new HBox();
						VBox.setVgrow(unixTerminalStarterBox, Priority.ALWAYS);
						Text unixTerminalStarterText = new Text(UNIX_TERMINAL_STARTER);
						HBox.setHgrow(unixTerminalStarterText, Priority.ALWAYS);
						TextField unixTerminalStarterTextField = new TextField();
						if (termsettings.get(UNIX_TERMINAL_STARTER) instanceof String)
							{
								unixTerminalStarterTextField.setText(termsettings.get(UNIX_TERMINAL_STARTER).toString());
							}
						else
							{
								unixTerminalStarterTextField
								    .setText(FONT_FAMILY_DEFAULT);
							}
						HBox.setHgrow(unixTerminalStarterTextField, Priority.ALWAYS);
						unixTerminalStarterBox.getChildren().add(unixTerminalStarterText);
						unixTerminalStarterBox.getChildren().add(unixTerminalStarterTextField);
						dialogVBox.getChildren().add(unixTerminalStarterBox);
						Button applyButton = new Button("Apply");
						dialogVBox.getChildren().add(applyButton);
						applyButton.setOnAction(evt -> {
							Map<String,Object> tmpTermSettings = new HashMap<>();
							tmpTermSettings.put(TabuTerminal.USE_DEFAULT_WINDOW_COPY, useDefaultWindowCopyCheckBox.isSelected());
							tmpTermSettings.put(TabuTerminal.CLEAR_SELECTION_AFTER_COPY, clearSelectionAfterCopyCheckBox.isSelected());
							tmpTermSettings.put(TabuTerminal.COPY_ON_SELECT, copyOnSelectCheckBox.isSelected());
							tmpTermSettings.put(TabuTerminal.CTRL_C_COPY, ctrlCCopyCheckBox.isSelected());
							tmpTermSettings.put(TabuTerminal.CTRL_V_PASTE, ctrlVPasteCheckBox.isSelected());
							tmpTermSettings.put(TabuTerminal.CURSOR_COLOR, cursorColorPicker.getValue());
							tmpTermSettings.put(TabuTerminal.BACKGROUND_COLOR, backgroundColorPicker.getValue());
							tmpTermSettings.put("fontSize", fontSizeSpinner.getValue());
							tmpTermSettings.put(TabuTerminal.FOREGROUND_COLOR_LITERAL, foregroundColorPicker.getValue());
							tmpTermSettings.put("cursorBlink",cursorBlinkCheckBox.isSelected());
							tmpTermSettings.put("scrollbarVisible",scrollBarVisibleCheckBox.isSelected());
							tmpTermSettings.put("enableClipboardNotice",enableClipboardNoticeCheckBox.isSelected());
							tmpTermSettings.put(TabuTerminal.SCROLL_WHELL_MOVE_MULTIPLIER, scrollWhellMoveMultiplierSpinner.getValue());
							tmpTermSettings.put(TabuTerminal.FONT_FAMILY,fontFamilyTextField.getText());
							tmpTermSettings.put("userCss",userCssTextField.getText());
							tmpTermSettings.put(TabuTerminal.WINDOWS_TERMINAL_STARTER,windowsTerminalStarterTextField.getText());
							tmpTermSettings.put(TabuTerminal.UNIX_TERMINAL_STARTER,unixTerminalStarterTextField.getText());
							
							//TODO FINISH BUTTON ACTION
						});
						Scene settingsScene = new Scene(dialogVBox);
						settingsWindow.setScene(settingsScene);
					}
				return settingsWindow;
			}
			
		private Map<String, Object> typeSafeGetConfigKey(String configKey)
			{
				Object o = settings.get(configKey);
				if (o instanceof Map)
					{
						@SuppressWarnings(
						  { "unchecked", "rawtypes" }
						)
						Set<Entry<?, ?>> smap = ((Map) o).entrySet();
						boolean typesafe = true;
						for (Entry<?, ?> e : smap)
							{
								if (!((e.getKey() instanceof String) && (e.getValue() instanceof Object)))
									{
										typesafe = false;
										break;
									}
							}
						if (typesafe)
							{
								@SuppressWarnings("unchecked")
								Map<String, Object> rval = (Map<String, Object>) settings.get(configKey);
								return rval;
							}
					}
				return new HashMap<>();
			}
			
		// rampant @SuppressWarnings -- there's not a *good* way to make this typesafe
		// so this method is a bad one...
		private void typeSaveSettingsAssignment(Gson gson, JsonReader settingsReader)
			{
				Object jso = gson.fromJson(settingsReader, HashMap.class);
				if (jso instanceof HashMap<?, ?>)
					{
						@SuppressWarnings("rawtypes")
						HashMap intermediateMap = (HashMap) jso;
						boolean typesafe = true;
						@SuppressWarnings(
						  { "unchecked", "rawtypes" }
						)
						Set<Entry> intermedetSet = intermediateMap.entrySet();
						for (@SuppressWarnings("rawtypes")
						Entry o : intermedetSet)
							{
								if ((!(String.class.isAssignableFrom(o.getKey().getClass()))) || (!(Object.class.isAssignableFrom(o.getKey().getClass()))))
									{
										typesafe = false;
										break;
									}
							}
						if (typesafe)
							{
								@SuppressWarnings("unchecked")
								HashMap<String, Object> tmp = (HashMap<String, Object>) jso;
								settings = tmp;
							}
						else
							{
								settings = new HashMap<>();
							}
					}
			}
	}
