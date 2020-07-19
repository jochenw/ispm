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
import com.github.jochenw.afw.core.log.app.DefaultAppLog;
import com.github.jochenw.afw.core.log.app.IAppLog;
import com.github.jochenw.afw.core.log.app.SystemOutAppLog;
import com.github.jochenw.afw.core.util.Holder;
import com.github.jochenw.afw.core.util.Systems;
import com.github.jochenw.ispm.core.config.IConfiguration;
import com.github.jochenw.ispm.core.plugins.XmlConfiguration;


public class Main {
	public static void main(String[] pArgs) throws Exception {
		final Holder<String> action = new Holder<String>();
		final Holder<IAppLog.Level> logLevel = new Holder<IAppLog.Level>();
		final Holder<Path> logFile = new Holder<Path>();
		final Holder<Path> configFile = new Holder<Path>();
		final IspmActionBean bean = new IspmActionBean();
		final Map<String,String> properties = new HashMap<>();
		final Holder<String> type = new Holder<String>();
		final Holder<String> id = new Holder<String>();
		final Holder<Path> dir = new Holder<Path>();
		final Args.Listener listener = new Args.Listener() {
			@Override
			public void option(Args.Context pCtx, String pName) {
				String actionStr = null;
				switch (pName) {
				  case "add-is-instance":
					actionStr = pName;
					id.set(pCtx.getValue());
					break;
				  case "add-local-repo":
					actionStr = pName;
					id.set(pCtx.getValue());
					break;
				  case "add-remote-repo":
					actionStr = pName;
					id.set(pCtx.getValue());
					break;
				  case "dir":
					if (dir.get() != null) {
						throw error("Option " + pName + " may be used only once.");
					}
					final Path p = Paths.get(pCtx.getValue());
					if (!Files.isDirectory(p)) {
						throw error("Invalid argument for option " + pName + ": " + p + " (Not  directory, or doesn't exist)");
					}
					dir.set(p);
					break;
				  case "list-instances":
					actionStr = pName;
					break;
				  case "list":
					actionStr = pName;
					break;
				  case "isInstance":
				    if (bean.getIsInstanceId() != null) {
				    	throw error("Option " + pName + " may be used only once.");
				    }
				    bean.setIsInstanceId(pCtx.getValue());
				    return;
				  case "logFile":
					if (logFile.get() != null) {
						throw error("Option " + pName + " may be used only once.");
					}
					final Path path = Paths.get(pCtx.getValue());
					final Path dir = path.getParent();
					if (dir != null  &&  !Files.isDirectory(dir)) {
						throw error("Invalid value for option "
					                + pName + ": Directory doesn't exist, or is no directory.");
					}
					logFile.set(path);
					return;
				  case "logLevel":
					if (logLevel.get() != null) {
						throw error("Option " + pName + " may be used only once.");
					}
					final IAppLog.Level level = IAppLog.Level.valueOf(pCtx.getValue().toUpperCase());
					logLevel.set(level);
					return;
				  default:
					throw error("Unknown option: " + pName);
				}
				if (action.get() != null) {
					throw error("The options list, and list-instances are mutually exclusive.");
				}
				action.set(actionStr);
			}

			@Override
			public @Nullable RuntimeException error(String pMsg) {
				usage(pMsg);
				return null;
			}
		};
		Args.parse(listener, pArgs);

		if (action.get() == null) {
			throw listener.error("No action given.");
		}
		
		final IAppLog appLog;
		if (logFile.get() == null) {
			appLog = new SystemOutAppLog();
		} else {
			appLog = new DefaultAppLog(Files.newOutputStream(logFile.get()));
		}
		if (logLevel.get() == null) {
			appLog.setLevel(IAppLog.Level.INFO);
		} else {
			appLog.setLevel(logLevel.get());
		}
		final IConfiguration configuration;
		final XmlConfiguration xmlConfiguration = new XmlConfiguration();
		if (configFile.get() == null) {
			final Path configFilePath = getDefaultConfigFile();
			if (configFilePath == null) {
				throw new IllegalStateException("Unable to determine path of default configuration file.");
			}
			if (Files.isRegularFile(configFilePath)) {
				configuration = xmlConfiguration.parse(configFilePath);
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
				configuration = xmlConfiguration.parse(configFilePath);
			}
		} else {
			configuration = xmlConfiguration.parse(configFile.get());
		}
		bean.setComponentFactory(new MainPluginRuntime().getComponentFactory(appLog, configuration));
		switch (action.get()) {
		case "list-instances":
			final List<IspmActionBean.IsInstanceInfo> instances = new ArrayList<>();
			bean.listInstances((inst) -> instances.add(inst));
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
			break;
		case "list":
			final List<IspmActionBean.IsPkgInfo> packages = new ArrayList<>();
			bean.list((pkg) -> packages.add(pkg));
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
		ps.println("Usage: java " + Main.class.getName() + " <ACTION> [OPTIONS]");
		ps.println();
		ps.println("Possible actions are:");
		ps.println("  --add-instance=<ID> Adds a new IS instance directory to the configuration.");
		ps.println("                      Use the options --dir, and --property to");
		ps.println("                      configure the instance details.");
		ps.println("  --list            List the packages in the IS directory.");
		ps.println("  --list-instances  List the IS instance directories.");
		ps.println();
		ps.println("Other opions are:");
		ps.println("  --configFile <F>  Sets the config file to use. Defaults to "
				   + getDefaultConfigFile());
		ps.println("  --dir=<DIR>       Sets the IS instance directory for --add-instance.");
		ps.println("  --isInstance <ID> Set the IS instance directory to the given ID.");
		ps.println("  --logFile <F>     Sets the log file (Defaults to System.out).");
		ps.println("  --logLevel <LVL>  Sets the log level (TRACE,DEBUG,INFO,WARN,ERROR; default is INFO).");
		ps.println("  --property=<KEY>=<VALUE> Sets a repository property for --add-local-repo, ");
		ps.println("                    and --add-remote-repo.");
		ps.println("  --type=<TYPE>     Sets the repository type for --add-local-repo, and");
		ps.println("                    --add-remote-repo");
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
