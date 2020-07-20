package com.github.jochenw.ispm.core;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.github.jochenw.afw.core.cli.Args;
import com.github.jochenw.afw.core.inject.IComponentFactory;
import com.github.jochenw.afw.core.log.app.DefaultAppLog;
import com.github.jochenw.afw.core.log.app.IAppLog;
import com.github.jochenw.afw.core.log.app.SystemOutAppLog;
import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.afw.core.util.Strings;
import com.github.jochenw.afw.core.util.Systems;
import com.github.jochenw.ispm.core.config.IConfiguration;
import com.github.jochenw.ispm.core.config.IConfiguration.IIsInstance;
import com.github.jochenw.ispm.core.config.IConfiguration.WmVersion;
import com.github.jochenw.ispm.core.plugins.XmlConfiguration;
import com.github.jochenw.ispm.core.plugins.XmlConfiguration.IsInstance;
import com.github.jochenw.ispm.core.plugins.XmlConfiguration.TLocalRepository;
import com.github.jochenw.ispm.core.plugins.XmlConfiguration.TRemoteRepository;


public class Main {
	public static class OptionsBean {
		String action;
		IAppLog.Level logLevel;
		Path logFile;
		Path configFile;
		final Map<String,String> properties = new HashMap<>();
		String type;
		String id, isInstanceId;
		Path dir;
		Boolean isDefault;
		WmVersion wmVersion;
		Path wmHomeDir, isHomeDir, packageDir, configDir;
	}

	public OptionsBean parseOptions(String[] pArgs) {
		final OptionsBean options = new OptionsBean();
		final Args.Listener listener = new Args.Listener() {
			@Override
			public void option(Args.Context pCtx, String pName) {
				String actionStr = null;
				switch (pName) {
				  case "add-instance":
					actionStr = pName;
					options.id = pCtx.getValue();
					break;
				  case "add-local-repo":
					actionStr = pName;
					options.id = pCtx.getValue();
					break;
				  case "add-remote-repo":
					actionStr = pName;
					options.id = pCtx.getValue();
					break;
				  case "configFile":
				  {
					if (options.configFile != null) {
						throw error("Option " + pName + " may be used only once.");
					}
					final Path p = Paths.get(pCtx.getValue());
					if (!Files.isRegularFile(p)) {
						throw error("Invalid argument for option " + pName + ": " + p + " (Not a file, or doesn't exist.)");
					}
					options.configFile = p;
					break;
				  }
				  case "default":
				  {
					  if (options.isDefault != null) {
						  throw error("Option " + pName + " may be used only once.");
					  }
					  final Boolean b;
					  if (pCtx.isValueAvailable()) {
						  b = Boolean.valueOf(pCtx.getValue());
					  } else {
						  b = Boolean.TRUE;
					  }
					  options.isDefault = b;
					  break;
				  }
				  case "dir":
				  {
					if (options.dir != null) {
						throw error("Option " + pName + " may be used only once.");
					}
					final Path p = Paths.get(pCtx.getValue());
					if (!Files.isDirectory(p)) {
						throw error("Invalid argument for option " + pName + ": " + p + " (Not a directory, or doesn't exist)");
					}
					options.dir = p;
					break;
				  }
				  case "list-instances":
					actionStr = pName;
					break;
				  case "list":
					actionStr = pName;
					break;
				  case "isInstance":
				    if (options.isInstanceId != null) {
				    	throw error("Option " + pName + " may be used only once.");
				    }
				    options.isInstanceId = pCtx.getValue();
				    return;
				  case "logFile":
					if (options.logFile != null) {
						throw error("Option " + pName + " may be used only once.");
					}
					final Path path = Paths.get(pCtx.getValue());
					final Path dir = path.getParent();
					if (dir != null  &&  !Files.isDirectory(dir)) {
						throw error("Invalid value for option "
					                + pName + ": Directory doesn't exist, or is no directory.");
					}
					options.logFile = path;
					return;
				  case "logLevel":
					if (options.logLevel != null) {
						throw error("Option " + pName + " may be used only once.");
					}
					final IAppLog.Level level = IAppLog.Level.valueOf(pCtx.getValue().toUpperCase());
					options.logLevel = level;
					return;
				  case "wmVersion":
					if (options.wmVersion != null) {
						throw error("Option " + pName + " may be used only once.");
					}
					final String wmvStr = pCtx.getValue();
					final WmVersion wmv;
					try {
						wmv = IIsInstance.asVersion(wmvStr);
					} catch (IllegalArgumentException e) {
						throw error("Invalid value for option " + pName + ": " + wmvStr + ", permitted values are "
								    + Strings.join(",", IIsInstance.getVersions()));
					}
					options.wmVersion = wmv;
					break;
				  default:
					throw error("Unknown option: " + pName);
				}
				if (actionStr != null) {
					if (options.action != null) {
						throw error("The options list, and list-instances are mutually exclusive.");
					}
					options.action = actionStr;
				}
			}

			@Override
			public @Nullable RuntimeException error(String pMsg) {
				usage(pMsg);
				return null;
			}
		};
		Args.parse(listener, pArgs);
		return options;
	}

	private IAppLog getAppLog(OptionsBean pOptions) {
		final IAppLog appLog;
		if (pOptions.logFile == null) {
			appLog = new SystemOutAppLog();
		} else {
			final Path logDir = pOptions.logFile.getParent();
			if (logDir != null  &&  !Files.isDirectory(logDir)) {
				throw usage("Directory " + logDir + " for log file doesn't exist, or is not a directory.");
			}
			try {
				appLog = new DefaultAppLog(Files.newOutputStream(pOptions.logFile));
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
		final IAppLog.Level level = Objects.notNull(pOptions.logLevel, IAppLog.Level.INFO);
		appLog.setLevel(level);
		return appLog;
	}

	private XmlConfiguration getXmlConfiguration(OptionsBean pOptions, IAppLog pAppLog) {
		if (pOptions.configFile == null) {
			final Path configFilePath = getDefaultConfigFile();
			if (configFilePath == null) {
				throw new IllegalStateException("Unable to determine path of default configuration file.");
			}
			if (Files.isRegularFile(configFilePath)) {
				return XmlConfiguration.parse(configFilePath, pAppLog);
			} else {
				final Path configDir = configFilePath.getParent();
				if (configDir != null) {
					try {
						Files.createDirectories(configDir);
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				}
				final XmlConfiguration config = new XmlConfiguration();
				config.save(configFilePath);
				return XmlConfiguration.parse(configFilePath, pAppLog);
			}
		} else {
			return XmlConfiguration.parse(pOptions.configFile, pAppLog);
		}
	}

	public static void main(String[] pArgs) throws Exception {
		final Main main = new Main();
		final OptionsBean options = main.parseOptions(pArgs);
		if (options.action == null) {
			throw usage("No action given.");
		}

		final IAppLog appLog = main.getAppLog(options);
		final XmlConfiguration xmlConfiguration = main.getXmlConfiguration(options, appLog);
		final IConfiguration configuration = xmlConfiguration.asConfiguration();
		final MainPluginRuntime pluginRuntime = new MainPluginRuntime();
		final IspmActionBean bean = new IspmActionBean();
		final IComponentFactory componentFactory = pluginRuntime.getComponentFactory(appLog, configuration);
		bean.setComponentFactory(componentFactory);
		switch (options.action) {
		case "add-is-instance":
		{
			main.addIsInstance(options, xmlConfiguration);
			break;
		}
		case "add-local-repo":
		{
			main.addLocalRepo(options, xmlConfiguration);
			break;
		}
		case "add-remote-repo":
		{
			main.addRemoteRepo(options, xmlConfiguration);
			break;
		}
		case "list-instances":
			main.listInstances(bean);
			break;
		case "list":
			main.list(bean);
		}
	}

	protected void list(final IspmActionBean pBean) {
		final List<IspmActionBean.IsPkgInfo> packages = new ArrayList<>();
		pBean.list((pkg) -> packages.add(pkg));
		if (packages.isEmpty()) {
			System.out.println("No packages found.");
		} else {
			packages.sort((p1, p2) -> p1.getPackageName().compareToIgnoreCase(p2.getPackageName()));
			show(packages, new String[] {"Name", "Local repo", "Remote Repo"},
					(pkg) -> pkg.getPackageName(),
					(pkg) -> pkg.getLocalRepositoryId(),
					(pkg) -> pkg.getRemoteRepositoryId());
		}
	}

	protected void listInstances(final IspmActionBean pBean) {
		final List<IspmActionBean.IsInstanceInfo> instances = new ArrayList<>();
		pBean.listInstances((inst) -> instances.add(inst));
		if (instances.isEmpty()) {
			System.out.println("No instances found.");
		} else {
			instances.sort((i1, i2) -> 
				i1.getId().compareToIgnoreCase(i2.getId())
			);
			show(instances, new String[] {"Id", "Directory"},
				 (inst) -> inst.getId(),
				 (inst) -> inst.getDir().toString());
		}
	}

	protected void addIsInstance(OptionsBean pOptions, XmlConfiguration pXmlConfiguration) {
		final String instanceId = pOptions.id;
		if (instanceId == null) {
			throw usage("No instance Id given.");
		}
		final Path instanceDir = pOptions.dir;
		if (instanceDir == null) {
			throw usage("Action add-is-instance requires option --dir");
		}
		final WmVersion instanceWmVersion = pOptions.wmVersion;
		if (instanceWmVersion == null) {
			throw usage("Action add-is-instance requires option --wmVersion");
		}
		
		final boolean isDefaultInstance = pOptions.isDefault != null  &&  pOptions.isDefault.booleanValue();
		final List<IsInstance> instances = new ArrayList<IsInstance>();
		for (IsInstance inst : pXmlConfiguration.getIsInstanceDirs()) {
			final boolean instIsDefault = inst.isDefault()  &&  !isDefaultInstance;
			if (instanceId.equals(inst.getId())) {
				throw usage("Duplicate instance Id: " + instanceId);
			}
			instances.add(new IsInstance(inst.getLocator(), instIsDefault, inst.getId(), inst.getDir(),
					                     inst.getWmVersion(), inst.getWmHomeDir(), inst.getIsHomeDir(),
					                     inst.getPackageDir(), inst.getConfigDir()));
		}
		final IsInstance isInstance = new IsInstance(null, isDefaultInstance, instanceId, instanceDir.toString(),
													 instanceWmVersion.name(),
				                                     pOptions.wmHomeDir == null ? null : pOptions.wmHomeDir.toString(),
				                                     pOptions.isHomeDir == null ? null : pOptions.isHomeDir.toString(),
				                                     pOptions.packageDir == null ? null : pOptions.packageDir.toString(),
				                                     pOptions.configDir == null ? null : pOptions.configDir.toString());
		instances.add(isInstance);
		final XmlConfiguration newConfiguration = new XmlConfiguration(instances, pXmlConfiguration.getLocalRepositories(),
				                                                       pXmlConfiguration.getRemoteRepositories());
		newConfiguration.save(pXmlConfiguration.getPath());
	}

	protected void addLocalRepo(OptionsBean pOptions, XmlConfiguration pXmlConfiguration) {
		final String localRepoId = pOptions.id;
		if (localRepoId == null) {
			throw usage("No local repo Id given.");
		}
		final String localRepoType = pOptions.type;
		if (localRepoType == null) {
			throw usage("No local repo type given.");
		}
		final List<TLocalRepository> localRepos = new ArrayList<>();
		for (TLocalRepository lr : pXmlConfiguration.getLocalRepositories()) {
			if (localRepoId.equals(lr.getId())) {
				throw usage("Duplicate local repository Id: " + localRepoId);
			}
			localRepos.add(lr);
		}
		localRepos.add(new TLocalRepository(null, localRepoId, localRepoType, pOptions.properties));
		final XmlConfiguration newConfiguration = new XmlConfiguration(pXmlConfiguration.getIsInstanceDirs(), localRepos,
				                                                       pXmlConfiguration.getRemoteRepositories());
		newConfiguration.save(pXmlConfiguration.getPath());
	}

	protected void addRemoteRepo(OptionsBean pOptions, XmlConfiguration pXmlConfiguration) {
		final String remoteRepoId = pOptions.id;
		if (remoteRepoId == null) {
			throw usage("No remote repo Id given.");
		}
		final String remoteRepoType = pOptions.type;
		if (remoteRepoType == null) {
			throw usage("No remote repo type given.");
		}
		final List<TRemoteRepository> remoteRepos = new ArrayList<>();
		for (TRemoteRepository rr : pXmlConfiguration.getRemoteRepositories()) {
			if (remoteRepoId.equals(rr.getId())) {
				throw usage("Duplicate remote repository Id: " + remoteRepoId);
			}
			remoteRepos.add(rr);
		}
		remoteRepos.add(new TRemoteRepository(null, remoteRepoId, remoteRepoType, pOptions.properties));
		final XmlConfiguration newConfiguration = new XmlConfiguration(pXmlConfiguration.getIsInstanceDirs(),
				                                                       pXmlConfiguration.getLocalRepositories(),
				                                                       remoteRepos);
		newConfiguration.save(pXmlConfiguration.getPath());
	}
	
	@SafeVarargs
	private static <O> void show(List<O> pList, String[] pColumnTitles, Function<O,String>... pColumnSuppliers) {
		final int[] columnLengths = new int[pColumnTitles.length];
		for (int i = 0;  i < columnLengths.length;  i++) {
			columnLengths[i] = pColumnTitles[i].length();
		}
		for (int i = 0;  i < pList.size();  i++) {
			final O o = pList.get(i);
			for (int j = 0;  j < columnLengths.length;  j++) {
				final int len = pColumnSuppliers[j].apply(o).length();
				if (len > columnLengths[j]) {
					columnLengths[j] = len;
				}
			}
		}
		for (int i = 0;  i < columnLengths[i];  i++) {
			show(pColumnTitles[i], columnLengths[i], i > 0);
			System.out.println();
		}
		for (int i = 0;  i < pList.size();  i++) {
			final O o = pList.get(i);
			for (int j = 0;  j < columnLengths.length;  j++) {
				show(pColumnSuppliers[j].apply(o), columnLengths[j], j > 0);
			}
			System.out.println();
		}
	}

	private static void show(String pValue, int pColumnLength, boolean pShowSeparator) {
		final StringBuilder sb = new StringBuilder();
		if (pShowSeparator) {
			sb.append(' ');
		}
		sb.append(pValue);
		for (int i = pValue.length();  i < pColumnLength;  i++) {
			sb.append(' ');
		}
		System.out.print(sb);
	}

	public static RuntimeException usage(String pMsg) {
		final PrintStream ps = System.err;
		if (pMsg != null) {
			ps.println(pMsg);
			ps.println();
		}
		// Text should be restricted to 80 characters:
		//          0         1         2         3         4         5         6         7
		//          01234567890123456789012345678901234567890123456789012345678901234567890123456789
		ps.println("Usage: java " + Main.class.getName() + " <ACTION> [OPTIONS]");
		ps.println();
		ps.println("Possible actions are:");
		ps.println("  --add-instance=<ID> Adds a new IS instance directory to the configuration.");
		ps.println("                      Required options: --dir, --wmVersion");
		ps.println("                      Other options: --configDir, --configFile, --default, ");
		ps.println("                      --isHome, --packageDir, and --wmHome");
		ps.println("  --add-local-repo=<ID> Adds a new local repository to the configuration.");
		ps.println("                      Required options are: --type");
		ps.println("                      Other options are: --configFile, and --property");
		ps.println("  --add-remote-repo=<ID> Adds a new remote repository to the configuration.");
		ps.println("                      Required options are: --type");
		ps.println("                      Other options are: --configFile, and --property");
		ps.println("  --list              List the packages in the IS directory.");
		ps.println("                      Possible options are: --isInstance");
		ps.println("  --list-instances    List the IS instance directories.");
		ps.println("                      No relevant options.");
		ps.println();
		ps.println("Available options are:");
		ps.println("  --configDir=<DIR> Sets the IS instances config directory for --add-instance.");
		ps.println("                    Defaults to ${instanceDir}/config");
		ps.println("  --configFile <F>  Sets the config file to use. Defaults to ");
		ps.println("                    " + getDefaultConfigFile());
		ps.println("  --default         Used in conjunction with --add-instance to create");
		ps.println("                    a default instance.");
		ps.println("  --dir=<DIR>       Sets the IS instance directory for --add-instance.");
		ps.println("  --isHome=<DIR>    Sets the IS home directory for --add-instance.");
		ps.println("                    Defaults to ${instanceDir}/../.., where ${instanceDir}");
		ps.println("                    is given by --dir.");
		ps.println("  --isInstance <ID> Set the IS instance directory to the given ID.");
		ps.println("  --logFile <F>     Sets the log file (Defaults to System.out).");
		ps.println("  --logLevel <LVL>  Sets the log level (TRACE,DEBUG,INFO,WARN,ERROR; default is INFO).");
		ps.println("  --packageDir=<DIR> Sets the IS instances package directory for --add-instance.");
		ps.println("                    Defaults to ${instanceDir}/packages");
		ps.println("  --property=<KEY>=<VALUE> Sets a repository property for --add-local-repo, ");
		ps.println("                    and --add-remote-repo.");
		ps.println("  --type=<TYPE>     Sets the repository type for --add-local-repo, and");
		ps.println("                    --add-remote-repo");
		ps.println("  --wmHome=<DIR>    Sets the webMethods home directory for --add-instance.");
		ps.println("                    Defaults to ${instanceDir}/../../.., where ${instanceDir}");
		ps.println("                    is given by --dir.");
		ps.println("  --wmVersion=<V>   Sets the webMethods version for --add-instance.");
		ps.println("                    Supported version numbers are " + String.join(",", IIsInstance.getVersions()) + ".");
		System.exit(1);
		return null;
	}

	public static Path getDefaultConfigFile() {
		String userDir = System.getProperty("user.home");
		if (userDir == null) {
			final String ud;
			if (Systems.isLinuxOrUnix()) {
				ud = System.getenv("HOME");
			} else if (Systems.isWindows()) {
				ud = System.getenv("HOMEPATH");
			} else {
				ud = null;
			}
			if (ud == null) {
				return null;
			} else {
				userDir = ud;
			}
		}
		return Paths.get(userDir).resolve(".ispm/config.xml");
	}
}
