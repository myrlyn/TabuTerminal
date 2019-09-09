package tabuterminal;

import java.io.File;
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
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;

public class TabuTerminal extends Application {
	public static void main(String[] args) {
		launch(args);
	}

	private Stage mainWindow = null;

	private String defaultTerminalCommand = "C:\\cygwin64\\bin\\bash.exe -i -l";

	private TerminalConfig sshTerminalConfig = new TerminalConfig();

	private TerminalConfig defaultTerminalConfig = new TerminalConfig();

	private VBox rootBox = new VBox();

	private MenuBar menuBar = new MenuBar();

	private Menu tabMenu = new Menu("Tab");

	private MenuItem newTab = new MenuItem("New Default Terminal");

	private MenuItem closeTab = new MenuItem("Close Tab");

	private TerminalBuilder defaultTerminalBuilder = null;

	private TabPane tabPane = new TabPane();

	private Scene scene = null;

	private Logger logger = java.util.logging.Logger.getLogger(TabuTerminal.class.getName());

	private Menu fileMenu = new Menu("File");

	private MenuItem exitApp = new MenuItem("Exit");

	private Map<String, TabuTerminalPlugin_V1> pluginMapV1 = new HashMap<>();

	private TerminalBuilder sshTerminalBuilder = null;

	private TerminalBuilder telTerminalBuilder = null;

	private TerminalConfig telTerminalConfig = null;

	private void addTerminalTab() {
		TerminalTab terminal = defaultTerminalBuilder.newTerminal();
		MenuItem contextRenameTab = new MenuItem("Rename Tab");
		contextRenameTab.setOnAction(new EventHandler<ActionEvent>() {
			Tab t = terminal;

			@Override
			public void handle(ActionEvent event) {
				renameTab(t);

			}

		});
		terminal.getContextMenu().getItems().add(contextRenameTab);
		tabPane.getTabs().add(terminal);
		tabPane.getSelectionModel().select(terminal);
	}

	private void addTelnetTab(Stage mainStage) {
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
			telnetcmd = telnetcmd + " " + argsField.getText() + " " + hostnameField.getText() + " "	+ portField.getText();
			telTerminalBuilder.getTerminalConfig().setWindowsTerminalStarter(telnetcmd);
			TerminalTab telnetTab = telTerminalBuilder.newTerminal();
			tabPane.getTabs().add(telnetTab);
			tabPane.getSelectionModel().select(telnetTab);
			dialog.close();
		});
		dialogVBox.getChildren().add(connectButton);
		
		dialog.showAndWait();
	}

	private void addSSHTab(Stage mainStage) {
		Stage dialog = new Stage();
		dialog.initModality(Modality.APPLICATION_MODAL);
		dialog.initOwner(mainStage);
		VBox dialogVBox = new VBox();
		String homeDir = System.getProperty("user.home");
		Text userText = new Text("UserName");

		String usernameVal = System.getProperty("user.name");

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
			File sshDirFile = new File(homeDir + File.separator + ".ssh");
			if (!sshDirFile.exists()) {
				sshDirFile.mkdirs();
			}

			cmd = cmd + " -o UserKnownHostsFile=" + knownHostsfl + " -F " + " " + conf + " " + args + " ";
			if (!"".equalsIgnoreCase(username))
				cmd += " " + username + "@";
			cmd += hostname;
			sshTerminalBuilder.getTerminalConfig().setWindowsTerminalStarter(cmd);
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

	public MenuItem getCloseTab() {
		return closeTab;
	}

	public TerminalBuilder getDefaultTerminalBuilder() {
		return defaultTerminalBuilder;
	}

	public String getDefaultTerminalCommand() {
		return defaultTerminalCommand;
	}

	public TerminalConfig getDefaultTerminalConfig() {
		return defaultTerminalConfig;
	}

	public MenuItem getExitApp() {
		return exitApp;
	}

	public Menu getFileMenu() {
		return fileMenu;
	}

	public Logger getLogger() {
		return logger;
	}

	public MenuBar getMenuBar() {
		return menuBar;
	}

	public MenuItem getNewTab() {
		return newTab;
	}

	public Map<String, TabuTerminalPlugin_V1> getPluginMapV1() {
		return pluginMapV1;
	}

	public VBox getRootBox() {
		return rootBox;
	}

	public Scene getScene() {
		return scene;
	}

	public Menu getTabMenu() {
		return tabMenu;
	}

	public TabPane getTabPane() {
		return tabPane;
	}

	public void loadPlugins() {
		loadPlugins(System.getProperty("user.home") + File.separator + ".TabuTerminal" + File.separator + "plugins"
				+ File.separator);
	}

	public void loadPlugins(String pluginsDir) {
		File pd = new File(pluginsDir);
		if (pd.exists()) {
			if (pd.isDirectory()) {
				String[] jars = pd.list((File dir, String name) -> name.toLowerCase().endsWith(".jar"));
				for (String jar : jars) {
					logger.info("Found Jar "+jar);
					loadAndInitializePlugin(jar);
				}
			} else {
				logger.warning("plugin directory name refers to file, not directory!");
			}
		} else {
			pd.mkdirs();
		}
	}

	private void loadAndInitializePlugin(String jar) {
		logger.info("Initializ this jar: "+jar.toString());
		try (JarFile jf = new JarFile( System.getProperty("user.home") + File.separator + ".TabuTerminal" + File.separator + "plugins"
				+ File.separator + jar);) {
			logger.info("Check Jar File"+jf.toString());
			Enumeration<JarEntry> pluginEntries = jf.entries();
			URL[] urls = { new URL("jar:file:" + System.getProperty("user.home") + File.separator + ".TabuTerminal" + File.separator + "plugins"
					+ File.separator + jar + "!/") };
			logger.info("Loader URL: "+urls[0]);
			try (URLClassLoader jarloader = URLClassLoader.newInstance(urls);) {
				List<Class<TabuTerminalPlugin_V1>> pluginsToInit;
				pluginsToInit = new LinkedList<>();
				while (pluginEntries.hasMoreElements()) {
					JarEntry entry = pluginEntries.nextElement();
					if (!entry.isDirectory() && entry.getName().endsWith("class")) {
						// -6 because ".class".length == 6, so to strip '.class off the end of the
						// classname we want to load, we remove the last 6 characters
						String className = entry.getName().substring(0, entry.getName().length() - 6);
						className = className.replace('/', '.');// replace separators with dots to construct full class
						logger.info("Class Name: "+className);										// name
						Class<?> c = jarloader.loadClass(className);
						if (TabuTerminalPlugin_V1.class.isAssignableFrom(c)) {
							pluginsToInit.add((Class<TabuTerminalPlugin_V1>) c);
						}
					}
				}
				for (Class<TabuTerminalPlugin_V1> plugin : pluginsToInit) {
					Constructor<TabuTerminalPlugin_V1> pluginConstructor = plugin.getConstructor(TabuTerminal.class);
					TabuTerminalPlugin_V1 plug = pluginConstructor.newInstance(this);
					logger.info("Initialize this!  "+plug.getPluginName());
					plug.initialize(System.getProperty("user.home") + File.separator + ".TabuTerminal" + File.separator + "plugins"
							+ File.separator + jar );
					this.pluginMapV1.put(plug.getPluginName(), plug);
				}
			}
		} catch (IOException | ClassNotFoundException | NoSuchMethodException | SecurityException
				| InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			logger.log(Level.WARNING, "ERROR LOADING JAR PLUGIN", e);
		}
	}

	public void setCloseTab(MenuItem closeTab) {
		this.closeTab = closeTab;
	}

	public void setDefaultTerminalBuilder(TerminalBuilder defaultTerminalBuilder) {
		this.defaultTerminalBuilder = defaultTerminalBuilder;
	}

	public void setDefaultTerminalCommand(String defaultTerminalCommand) {
		this.defaultTerminalCommand = defaultTerminalCommand;
	}

	public void setDefaultTerminalConfig(TerminalConfig defaultTerminalConfig) {
		this.defaultTerminalConfig = defaultTerminalConfig;
	}

	public void setExitApp(MenuItem exitApp) {
		this.exitApp = exitApp;
	}

	public void setFileMenu(Menu fileMenu) {
		this.fileMenu = fileMenu;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public void setMenuBar(MenuBar menuBar) {
		this.menuBar = menuBar;
	}

	public void setNewTab(MenuItem newTab) {
		this.newTab = newTab;
	}

	public void setPluginMapV1(Map<String, TabuTerminalPlugin_V1> pluginMapV1) {
		this.pluginMapV1 = pluginMapV1;
	}

	public void setRootBox(VBox rootBox) {
		this.rootBox = rootBox;
	}

	public void setScene(Scene scene) {
		this.scene = scene;
	}

	public void setTabMenu(Menu tabMenu) {
		this.tabMenu = tabMenu;
	}

	public void setTabPane(TabPane tabPane) {
		this.tabPane = tabPane;
	}

	public void renameTab() {
		Tab currentTab = this.tabPane.getSelectionModel().getSelectedItem();
		renameTab(currentTab);
	}

	public void renameTab(Tab currentTab) {
		TextInputDialog td = new TextInputDialog(currentTab.getText());
		Optional<String> newName = td.showAndWait();
		if (newName.isPresent()) {
			currentTab.setText(newName.get());
		}
	}

	public void closeAllTabs() {
		ObservableList<Tab> tablist = tabPane.getTabs();
		for (Tab tab : tablist) {
			if (tab instanceof TerminalTab) {
				((TerminalTab) tab).getProcess().destroy();
				((TerminalTab) tab).destroy();

			}
		}
	}

	public void closeAndExit() {
		ObservableList<Tab> tabList = tabPane.getTabs();
		for (Tab tab : tabList) {
			if (tab instanceof TerminalTab) {
				TerminalTab ttab = (TerminalTab) tab;
				PtyProcess proc = ttab.getProcess();
				if (proc != null)
					proc.destroyForcibly();
			}
		}
		logger.info("Goodbye");
		System.exit(0);
	}

	public void closeAllChildrenOfTerminaTab(ActionEvent arg0, TerminalTab t) {
		Terminal tm = t.getTerminal();
		PtyProcess proc = tm.getProcess();
		closeAllChildren(proc);
		proc.destroy();
		t.closeTerminal(arg0);
	}

	private void closeAllChildren(PtyProcess proc) {
		try (Stream<ProcessHandle> chstream = proc.children();) {
			Iterator<ProcessHandle> childIterator = chstream.iterator();
			while (childIterator.hasNext()) {
				ProcessHandle childproc = childIterator.next();
				try (Stream<ProcessHandle> proclist = childproc.children();) {
					closeAllChildren(proclist);
				}
				childproc.destroy();
			}
		}
	}

	private void closeAllChildren(Stream<ProcessHandle> children) {

		Iterator<ProcessHandle> childIterator = children.iterator();
		while (childIterator.hasNext()) {
			ProcessHandle childproc = childIterator.next();
			closeAllChildren(childproc.children());
			childproc.destroy();
		}
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		this.mainWindow = primaryStage;
		primaryStage.setOnCloseRequest((WindowEvent event) -> closeAndExit());
		String cygbash = "C:\\cygwin64\\bin\\bash.exe";
		String bashflags = " -i -l";
		String cmdPrompt = "";
		if (new File(cygbash).canExecute()) { // start cygwin if it is installed
			logger.info("Cygwin bash found, using cygwin as default shell");
			this.setDefaultTerminalCommand(cygbash + bashflags);
		} else { // otherwise, find a command interpreter
			cmdPrompt = findBashPrompt();
			if (cmdPrompt.equalsIgnoreCase("")) {
				cmdPrompt = findShPrompt();
				if (cmdPrompt.equalsIgnoreCase("")) {
					cmdPrompt = findCmdPrompt();
				}
			}

			else
				cmdPrompt = cmdPrompt + bashflags;
			this.setDefaultTerminalCommand(cmdPrompt);
		}

		defaultTerminalConfig.setBackgroundColor(Color.rgb(16, 16, 16));
		defaultTerminalConfig.setForegroundColor(Color.rgb(240, 240, 240));
		defaultTerminalConfig.setCursorColor(Color.rgb(255, 0, 0, 0.5));
		defaultTerminalConfig.setWindowsTerminalStarter(defaultTerminalCommand);
		defaultTerminalConfig.setCopyOnSelect(true);
		defaultTerminalBuilder = new TerminalBuilder(defaultTerminalConfig);

		sshTerminalConfig.setBackgroundColor(Color.rgb(16, 16, 16));
		sshTerminalConfig.setForegroundColor(Color.rgb(240, 240, 240));
		sshTerminalConfig.setCursorColor(Color.rgb(255, 0, 0, 0.5));
		sshTerminalConfig.setWindowsTerminalStarter(defaultTerminalCommand);
		sshTerminalConfig.setCopyOnSelect(true);
		sshTerminalBuilder = new TerminalBuilder(sshTerminalConfig);
		sshTerminalBuilder.setNameGenerator(new TabNameGenerator() {

			private long tabNumber = 1;

			@Override
			public String next() {
				// TODO Auto-generated method stub
				return "SSH: " + tabNumber++;
			}

		});
		telTerminalConfig = new TerminalConfig();
		telTerminalConfig.setBackgroundColor(Color.rgb(16, 16, 16));
		telTerminalConfig.setForegroundColor(Color.rgb(240, 240, 240));
		telTerminalConfig.setCursorColor(Color.rgb(255, 0, 0, 0.5));
		telTerminalConfig.setWindowsTerminalStarter(defaultTerminalCommand);
		telTerminalConfig.setCopyOnSelect(true);
		telTerminalBuilder = new TerminalBuilder(sshTerminalConfig);
		telTerminalBuilder.setNameGenerator(new TabNameGenerator() {

			private long tabNumber = 1;

			@Override
			public String next() {
				// TODO Auto-generated method stub
				return "Telnet: " + tabNumber++;
			}

		});
		tabMenu.getItems().add(newTab);
		tabMenu.getItems().add(closeTab);
		fileMenu.getItems().add(exitApp);
		menuBar.getMenus().add(fileMenu);
		menuBar.getMenus().add(tabMenu);
		newTab.setOnAction((ActionEvent act) -> addTerminalTab());
		
		MenuItem renameTabMenuItem = new MenuItem("Rename Tab");
		renameTabMenuItem.setOnAction((ActionEvent event) -> renameTab());
		closeTab.setOnAction((ActionEvent evt) -> closeAndExit());
		exitApp.setOnAction((ActionEvent evt) -> closeAndExit());
		
		MenuItem sshTabItem = new MenuItem("New SSH Tab");
		tabMenu.getItems().add(sshTabItem);
		sshTabItem.setOnAction(evt -> 
			this.addSSHTab(primaryStage)
		);
	
		MenuItem telTabItem = new MenuItem("New Telnet Tab");
		tabMenu.getItems().add(telTabItem);
		telTabItem.setOnAction(evt -> 
			this.addTelnetTab(primaryStage)
		);
		
		rootBox.getChildren().add(menuBar);
		VBox.setVgrow(tabPane, Priority.ALWAYS);
		rootBox.getChildren().add(tabPane);
		// gets a 132x43 character terminal, which is a standard size
		scene = new Scene(rootBox, 1115, 755);
		loadPlugins();
		addTerminalTab();
		primaryStage.getIcons().add(new Image(TabuTerminal.class.getResourceAsStream("tabu.png")));
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private String findCmdPrompt() {
		String cmdPrompt = "";
		for (String dirname : System.getenv("PATH").split(File.pathSeparator)) {
			File file = new File(dirname, "cmd.exe");
			if (file.isFile() && file.canExecute()) {
				cmdPrompt = file.getAbsolutePath();
			}
		}
		return cmdPrompt;
	}

	private String findBashPrompt() {
		String cmdPrompt = "";
		for (String dirname : System.getenv("PATH").split(File.pathSeparator)) {
			File file = new File(dirname, "bash.exe");
			if (file.isFile() && file.canExecute()) {
				cmdPrompt = file.getAbsolutePath();
			}
		}
		return cmdPrompt + " -i -l";
	}

	private String findShPrompt() {
		String cmdPrompt = "";
		for (String dirname : System.getenv("PATH").split(File.pathSeparator)) {
			File file = new File(dirname, "sh.exe");
			if (file.isFile() && file.canExecute()) {
				cmdPrompt = file.getAbsolutePath();
			}
		}
		return cmdPrompt;
	}
}
