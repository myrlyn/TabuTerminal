package tabuterminal;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import com.kodedu.terminalfx.Terminal;
import com.kodedu.terminalfx.TerminalBuilder;
import com.kodedu.terminalfx.TerminalTab;
import com.kodedu.terminalfx.config.TabNameGenerator;
import com.kodedu.terminalfx.config.TerminalConfig;
import com.pty4j.PtyProcess;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;

public class TabuTerminal extends Application
	{
		private static final String CURSOR_COLOR = "cursorColor";
		private static final String BACKGROUND_COLOR = "backgroundColor";
		private static final String FOREGROUND_COLOR = "foregroundColor";
		private static final String TEL_TERMINAL_CONFIG = "telTerminalConfig";
		private static final String SSH_TERMINAL_CONFIG = "sshTerminalConfig";
		private static final String DOT_TABU_TERMINAL_STRING = ".TabuTerminal";
		private static final String USER_HOME = "user.home";
		private static final String DOT_TABU_TERMINAL = DOT_TABU_TERMINAL_STRING;
		private static final String PLUGINS = "plugins";
		private static final String USER_HOME_PROPERTY_NAME = USER_HOME;

		public static String getDotTabuTerminal()
			{
				return DOT_TABU_TERMINAL;
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
						{cmd += " " + username + "@";
					ucmd += " " + username +"@";
					}
					ucmd+=hostname;
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

		public void setLogLevel(String lv)
			{
				logger.log(Level.FINE, () -> "SET LOG LEVEL TO "+lv);
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
						logger.fine("Close tab "+tab.getText());
						
						if (tab instanceof TerminalTab)
							{
								TerminalTab ttab = (TerminalTab) tab;
								
								PtyProcess proc = ttab.getProcess();
								logger.fine("Destroy Process "+proc.getPid());
									proc.destroyForcibly();
							}
					}
				logger.info("Goodbye");
				this.saveSettings();
				System.exit(0);
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
						JarFile jf = new JarFile(System.getProperty(USER_HOME_PROPERTY_NAME) + File.separator + DOT_TABU_TERMINAL
								+ File.separator + PLUGINS + File.separator + jar);
				)
					{
						logger.log(Level.INFO, () -> "Check Jar File" + jf.toString());
						Enumeration<JarEntry> pluginEntries = jf.entries();
						URL[] urls =
							{ new URL("jar:file:" + System.getProperty(USER_HOME_PROPERTY_NAME) + File.separator + DOT_TABU_TERMINAL
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
														pluginsToInit.add((Class<TabuTerminalPlugin_V1>) c);
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
										plug.initialize(System.getProperty(USER_HOME_PROPERTY_NAME) + File.separator + DOT_TABU_TERMINAL
												+ File.separator + PLUGINS + File.separator + jar);
										this.pluginMapV1.put(plug.getPluginName(), plug);
									}
							}
					} catch (
							IOException | ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException
							| IllegalAccessException | IllegalArgumentException | InvocationTargetException e
					)
					{
						logger.log(Level.WARNING, "ERROR LOADING JAR PLUGIN", e);
					}
			}

		public void loadPlugins()
			{
				loadPlugins(System.getProperty(USER_HOME_PROPERTY_NAME) + File.separator + DOT_TABU_TERMINAL + File.separator
						+ PLUGINS + File.separator);
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
							} else
							{
								logger.warning("plugin directory name refers to file, not directory!");
							}
					} else
					{
						pd.mkdirs();
					}
			}

		public Map<String, Object> readSettings()
			{
				Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().create();
				File settingsFile = new File(System.getProperty(USER_HOME) + File.separator + DOT_TABU_TERMINAL_STRING
						+ File.separator + "settings.json");
				if (settingsFile.exists())
					{
						try (JsonReader settingsReader = new JsonReader(new FileReader(settingsFile)))
							{
								Object jso = gson.fromJson(settingsReader, HashMap.class);
								if (jso instanceof HashMap<?, ?>)
									{
										settings = (HashMap<String, Object>) jso;
									}
							} catch (IOException e)
							{
								logger.log(Level.SEVERE, "Error reading settings", e);
							}
					} else
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
				settings.put("defaultTerminalConfig", this.getDefaultTerminalConfig());
				settings.put(SSH_TERMINAL_CONFIG, this.getSshTerminalConfig());
				settings.put(TEL_TERMINAL_CONFIG, this.getTelTerminalConfig());
				File settingsFile = new File(System.getProperty(USER_HOME) + File.separator + DOT_TABU_TERMINAL_STRING
						+ File.separator + "settings.json");
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
					} catch (IOException ioe)
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
				configureTerminalConfig(defaultTerminalConfig, "defaultTerminalConfig");
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
				this.menuBar.getMenus().add(settingsMenu);
				this.settingsMenu.getItems().add(saveSettingsMenuItem);
				this.settingsMenu.getItems().add(applySettingsMenuItem);
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

		private void configureTerminalConfig(TerminalConfig tc, String configKey)
			{
				logger.log(Level.INFO, () -> "Configure Terminal: " + configKey);
				this.readSettings();
				Object oMap = settings.get(configKey);
				if (!(Map.class.isAssignableFrom(oMap.getClass())))
					{
						logger.log(Level.SEVERE, () -> "misconfigured config element for " + configKey);
					} else
					{
						logger.log(Level.INFO, () -> "Loading from configured map");
						Map<String, Object> configMap = (Map<String, Object>) settings.get(configKey);
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
				Object uts = configMap.get("unixTerminalStarter");
				if (uts != null)
					{
						tc.setUnixTerminalStarter(uts.toString());
					} else
					{
						tc.setUnixTerminalStarter("/bin/bash -i");
					}
			}

		private void configureUserCSS(TerminalConfig tc, Map<String, Object> configMap)
			{
				Object ucss = configMap.get("userCss");
				if (ucss != null)
					{
						tc.setUserCss(ucss.toString());
					} else
					{
						tc.setUserCss("data:text/plain;base64,eC1zY3JlZW4geyBjdXJzb3I6IGF1dG87IH0\\u003d");
					}
			}

		private void configureFontFamily(TerminalConfig tc, Map<String, Object> configMap)
			{
				String fontFamDef = "\"DejaVu Sans Mono\", \"Everson Mono\", FreeMono, \"Menlo\", \"Terminal\", monospace";
				Object fontFam = configMap.get("fontFamily");
				if (fontFam != null)
					{
						tc.setFontFamily(fontFam.toString());
					} else
					{
						tc.setFontFamily(fontFamDef);
					}
			}

		private void configureScrollWhellMoveMultiplier(TerminalConfig tc, Map<String, Object> configMap)
			{
				Object swmm = configMap.get("scrollWhellMoveMultiplier");
				if (swmm instanceof Float)
					{
						tc.setScrollWhellMoveMultiplier((Float) swmm);
					} else
					{
						tc.setScrollWhellMoveMultiplier(0.1);
					}
			}

		private void configureEnableClipboardNotice(TerminalConfig tc, Map<String, Object> configMap)
			{
				Object cbn = configMap.get("enableCliboardNotice");
				if (cbn instanceof Boolean)
					{
						tc.setEnableClipboardNotice((Boolean) cbn);
					} else
					{
						tc.setEnableClipboardNotice(true);
					}
			}

		private void configureScrollBarVisible(TerminalConfig tc, Map<String, Object> configMap)
			{
				Object sbv = configMap.get("scrollbarVisible");
				if (sbv instanceof Boolean)
					{
						tc.setScrollbarVisible((Boolean) sbv);
					} else
					{
						tc.setScrollbarVisible(true);
					}
			}

		private void configureCursorBlink(TerminalConfig tc, Map<String, Object> configMap)
			{
				Object cb = configMap.get("cursorBlink");
				if (cb instanceof Boolean)
					{
						tc.setCursorBlink((Boolean) cb);
					} else
					{
						tc.setCursorBlink(true);
					}
			}

		private void configureFontSize(TerminalConfig tc, Map<String, Object> configMap)
			{
				Object fs = configMap.get("fontSize");
				if (fs instanceof Integer)
					{
						tc.setFontSize((Integer) fs);
					} else
					{
						tc.setFontSize(14);
					}
			}

		private void configureCtrlVPaste(TerminalConfig tc, Map<String, Object> configMap)
			{
				if (configMap.containsKey("ctrlVPaste"))
					{
						Object o = configMap.get("ctrlVPaste");
						if (o instanceof Boolean)
							{
								tc.setCtrlVPaste((Boolean) o);
							}
					} else
					{
						tc.setCtrlVPaste(true);
					}
			}

		private void configureCtrlCopy(TerminalConfig tc, Map<String, Object> configMap)
			{
				if (configMap.containsKey("ctrlCCopy"))
					{
						Object o = configMap.get("ctrlCCopy");
						if (o instanceof Boolean)
							{
								tc.setCtrlCCopy((Boolean) o);
							}
					} else
					{
						tc.setCtrlCCopy(true);
					}
			}

		private void configureClearSlectionAfterCopy(TerminalConfig tc, Map<String, Object> configMap)
			{
				if (configMap.containsKey("clearSelectionAfterCopy"))
					{
						Object o = configMap.get("clearSelectionAfterCopy");
						if (o instanceof Boolean)
							{
								tc.setClearSelectionAfterCopy((Boolean) o);
							}
					} else
					{
						tc.setClearSelectionAfterCopy(true);
					}
			}

		private void configureUseDefaultWindowCopy(TerminalConfig tc, Map<String, Object> configMap)
			{
				if (configMap.containsKey("useDefaultWindowCopy"))
					{
						Object o = configMap.get("useDefaultWindowCopy");
						if (o instanceof Boolean)
							{
								tc.setUseDefaultWindowCopy((Boolean) o);
							}
					} else
					{
						tc.setUseDefaultWindowCopy(true);
					}
			}

		private void configureCopyOnSelect(TerminalConfig tc, Map<String, Object> configMap)
			{
				Boolean b = null;
				if (configMap.containsKey("copyOnSelect"))
					{
						Object o = configMap.get("copyOnSelect");
						if (o instanceof Boolean)
							{
								b = (Boolean) o;
							}
						if (b != null)
							{
								tc.setCopyOnSelect(b);
							} else
							{
								tc.setCopyOnSelect(true);
							}
					}
			}

		private void configureWIndowsTerminalStarter(TerminalConfig tc, Map<String, Object> configMap)
			{
				if (configMap.containsKey("windowsTerminalStarter"))
					{
						tc.setWindowsTerminalStarter(configMap.get("windowsTerminalStarter").toString());
					} else
					{
						tc.setWindowsTerminalStarter(defaultTerminalCommand);
					}
			}

		private void configureCursorColor(TerminalConfig tc, Map<String, Object> configMap)
			{
				if (configMap.containsKey(CURSOR_COLOR))
					{
						Color webcolor = Color.web(configMap.get(CURSOR_COLOR).toString(), 0.5);
						tc.setCursorColor(webcolor);
						logger.log(Level.FINE, () -> "Set cursor color from settings: " + configMap.get(CURSOR_COLOR).toString());
					} else
					{
						tc.setCursorColor(Color.rgb(255, 0, 0, 0.5));
					}
			}

		private void configureForegroundColor(TerminalConfig tc, Map<String, Object> configMap)
			{
				if (configMap.containsKey(FOREGROUND_COLOR))
					{
						tc.setForegroundColor(Color.web(configMap.get(FOREGROUND_COLOR).toString()));
						logger.log(Level.FINE,
								() -> "Set foreground color from settings: " + configMap.get(FOREGROUND_COLOR).toString());
					} else
					{
						tc.setForegroundColor(Color.rgb(240, 240, 240));
					}
			}

		private void configureBackgroundColor(TerminalConfig tc, Map<String, Object> configMap)
			{
				if (configMap.containsKey(BACKGROUND_COLOR))
					{
						tc.setBackgroundColor(Color.web(configMap.get(BACKGROUND_COLOR).toString()));
						logger.log(Level.FINE,
								() -> "Set background color from settings: " + configMap.get(BACKGROUND_COLOR).toString());
					} else
					{
						tc.setBackgroundColor(Color.rgb(16, 16, 16));
					}
			}
	}
