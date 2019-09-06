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
	
	private TerminalConfig plinkTerminalConfig = new TerminalConfig();

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
	
	private TerminalBuilder plinkTerminalBuilder = null;

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

	}
	private void addPlinkTab(Stage mainStage) {
		Stage dialog = new Stage();
		dialog.initModality(Modality.APPLICATION_MODAL);
		dialog.initOwner(mainStage);
		VBox dialogVBox = new VBox();
		
		
		
		Text userText = new Text("UserName");
		userText.toFront();
		VBox.setVgrow(userText, Priority.ALWAYS);
		dialogVBox.getChildren().add(userText);
		TextField userNameField = new TextField ();
		VBox.setVgrow(userNameField, Priority.ALWAYS);
		dialogVBox.getChildren().add(userNameField);
		
		Text passwordText = new Text("Password");
		dialogVBox.getChildren().add(passwordText);
		PasswordField passwordField = new PasswordField ();
		dialogVBox.getChildren().add(passwordField);
		
		dialogVBox.getChildren().add(new Text("Hostname"));
		TextField hostnameField = new TextField ();
		dialogVBox.getChildren().add(hostnameField);
		
		Button connectButton = new Button("Connect");
		connectButton.addEventHandler(ActionEvent.ANY, evt -> {
			String username = userNameField.getText();
			String password = passwordField.getText();
			String hostname = hostnameField.getText();
			URL plinkFile = TabuTerminal.class.getResource("plink.exe");
			String plinkFileName = plinkFile.getFile();
			String cmd = plinkFileName+" -l "+username+" -pw "+password+" "+hostname;
			plinkTerminalBuilder.getTerminalConfig().setWindowsTerminalStarter(cmd);
			TerminalTab plinkTab = plinkTerminalBuilder.newTerminal();
			tabPane.getTabs().add(plinkTab);
			dialog.close();
		});
		dialogVBox.getChildren().add(connectButton);
		Scene dialogScene = new Scene(dialogVBox);
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
		try (JarFile jf = new JarFile(jar);) {

			Enumeration<JarEntry> pluginEntries = jf.entries();
			URL[] urls = { new URL("jar:file:" + jar + "!/") };

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
																// name
						Class<?> c = jarloader.loadClass(className);
						if (c.isAssignableFrom(TabuTerminalPlugin_V1.class)) {
							pluginsToInit.add((Class<TabuTerminalPlugin_V1>) c);
						}
					}
				}
				for (Class<TabuTerminalPlugin_V1> plugin : pluginsToInit) {
					Constructor<TabuTerminalPlugin_V1> pluginConstructor = plugin.getConstructor(TabuTerminal.class);
					TabuTerminalPlugin_V1 plug = pluginConstructor.newInstance(this);
					plug.initalize();
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
					proc.destroy();
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
		String cygflags = " -i -l";
		String cmdDefault = "C:\\WINDOWS\\system32\\cmd.exe";
		if (new File(cygbash).canExecute()) {
			logger.info("Cygwin bash found, using cygwin as default shell");
			this.setDefaultTerminalCommand(cygbash + cygflags);
		} else if (new File(cmdDefault).canExecute()) {
			logger.info("Cygwin bash not found, using cmd.exe as default shell");
			this.setDefaultTerminalCommand("C:\\WINDOWS\\system32\\cmd.exe");
		} else {
			String cmdPrompt = "";
			for (String dirname : System.getenv("PATH").split(File.pathSeparator)) {
				File file = new File(dirname, "cmd.exe");
				if (file.isFile() && file.canExecute()) {
					cmdPrompt = file.getAbsolutePath();
				}
			}
			this.setDefaultTerminalCommand(cmdPrompt);
		}
		defaultTerminalConfig.setBackgroundColor(Color.rgb(16, 16, 16));
		defaultTerminalConfig.setForegroundColor(Color.rgb(240, 240, 240));
		defaultTerminalConfig.setCursorColor(Color.rgb(255, 0, 0, 0.5));
		defaultTerminalConfig.setWindowsTerminalStarter(defaultTerminalCommand);
		defaultTerminalConfig.setCopyOnSelect(true);
		defaultTerminalBuilder = new TerminalBuilder(defaultTerminalConfig);
		
		plinkTerminalConfig.setBackgroundColor(Color.rgb(16, 16, 16));
		plinkTerminalConfig.setForegroundColor(Color.rgb(240, 240, 240));
		plinkTerminalConfig.setCursorColor(Color.rgb(255, 0, 0, 0.5));
		plinkTerminalConfig.setWindowsTerminalStarter(defaultTerminalCommand);
		plinkTerminalConfig.setCopyOnSelect(true);
		plinkTerminalBuilder = new TerminalBuilder(plinkTerminalConfig);
		plinkTerminalBuilder.setNameGenerator(new TabNameGenerator() {
			private long tabNumber = 1;
			@Override
			public String next() {
				// TODO Auto-generated method stub
				return "SSH: "+tabNumber++;
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
		addTerminalTab();
		addPlinkTab(primaryStage);
		rootBox.getChildren().add(menuBar);
		VBox.setVgrow(tabPane, Priority.ALWAYS);
		rootBox.getChildren().add(tabPane);
		// gets a 132x43 character terminal, which is a standard size
		scene = new Scene(rootBox, 1115, 755);
		loadPlugins();
		primaryStage.getIcons().add(new Image(TabuTerminal.class.getResourceAsStream("tabu.png")));
		primaryStage.setScene(scene);
		primaryStage.show();
	}
}
