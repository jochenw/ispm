package com.github.jochenw.ispm.core.svc;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

import org.apache.logging.log4j.util.Strings;
import org.codehaus.groovy.ast.ASTNode;

import com.github.jochenw.afw.core.ILifecycleController;
import com.github.jochenw.afw.core.components.DefaultSymbolicLinksHandler;
import com.github.jochenw.afw.core.components.ISymbolicLinksHandler;
import com.github.jochenw.afw.core.impl.DefaultLifecycleController;
import com.github.jochenw.afw.core.inject.ComponentFactoryBuilder.Module;
import com.github.jochenw.afw.core.inject.IComponentFactory;
import com.github.jochenw.afw.core.inject.Scopes;
import com.github.jochenw.afw.core.inject.simple.SimpleComponentFactoryBuilder;
import com.github.jochenw.afw.core.io.IReadable;
import com.github.jochenw.afw.core.log.ILogFactory;
import com.github.jochenw.afw.core.log.log4j.Log4j2LogFactory;
import com.github.jochenw.afw.core.props.DefaultPropertyFactory;
import com.github.jochenw.afw.core.props.IPropertyFactory;
import com.github.jochenw.afw.core.scripts.IScriptEngine.Script;
import com.github.jochenw.afw.core.util.DomHelper;
import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Functions;
import com.github.jochenw.afw.core.util.Functions.FailableConsumer;
import com.github.jochenw.afw.core.util.Functions.FailableFunction;
import com.github.jochenw.afw.core.util.Scripts;
import com.github.jochenw.afw.core.util.Tupel;
import com.github.jochenw.ispm.core.actions.ActivatePackageAction;
import com.github.jochenw.ispm.core.actions.AddInstanceAction;
import com.github.jochenw.ispm.core.actions.AddLocalRepoAction;
import com.github.jochenw.ispm.core.actions.AddPluginAction;
import com.github.jochenw.ispm.core.actions.AddRemoteRepoAction;
import com.github.jochenw.ispm.core.actions.ImportFromLocalRepoAction;
import com.github.jochenw.ispm.core.actions.PackageCompilerAction;
import com.github.jochenw.ispm.core.actions.PackageReloadAction;
import com.github.jochenw.ispm.core.components.DefaultServiceInvocator;
import com.github.jochenw.ispm.core.components.IServiceInvocator;
import com.github.jochenw.ispm.core.config.IspmConfigParser;
import com.github.jochenw.ispm.core.config.TIspmConfiguration;
import com.github.jochenw.ispm.core.config.TIspmConfiguration.TPlugin;
import com.github.jochenw.ispm.core.model.IspmConfiguration;
import com.github.jochenw.ispm.core.model.IspmConfigurationBuilder;

import groovy.lang.GroovyRuntimeException;


public class IspmApplication {
	private static IspmApplication instance;

	public static synchronized IspmApplication getInstance() {
		return instance;
	}

	public static synchronized void setInstance(IspmApplication pInstance) {
		instance = pInstance;
	}

	/* Core fields: If either of these change, then the other fields must be reset to null,
	 * forcing a reinitialization.
	 */
	private static class CoreData {
		private TIspmConfiguration tIspmConfiguration;
		private Path ispmConfigurationPath;
		private ILogFactory logFactory;
		private IPropertyFactory propertyFactory;
	}

	public IspmApplication(Path pCurrentDir, String pIspmPackageName, Module... pModules) {
		currentDir = pCurrentDir;
		ispmPackageName = pIspmPackageName;
		modules = pModules;
	}
	public IspmApplication(Path pCurrentDir, String pIspmPackageName) {
		this(pCurrentDir, pIspmPackageName, (Module[]) null);
	}

	private final String ispmPackageName;
	private final Module[] modules;
	private final Path currentDir;
	private final CoreData coreData = new CoreData();
	private static final class ExtData {
		private IspmConfiguration ispmConfiguration;
		private IComponentFactory componentFactory;
	}
	private ExtData extData;

	public TIspmConfiguration getTIspmConfiguration() {
		return useCoreData(cd -> {
			if (cd.tIspmConfiguration == null) {
				final Tupel<TIspmConfiguration,Path> tupel = newTIspmConfiguration();
				cd.tIspmConfiguration = tupel.getAttribute1();
				cd.ispmConfigurationPath = tupel.getAttribute2();
			}
			return cd.tIspmConfiguration;
		});
	}
	public void setIspmConfiguration(TIspmConfiguration pIspmConfiguration, Path pPath) {
		synchronized(this) {
			synchronized (coreData) {
				coreData.tIspmConfiguration = pIspmConfiguration;
				coreData.ispmConfigurationPath = pPath;
			}
			extData = null;
		}
	}

	public ILogFactory getLogFactory() {
		return useCoreData(cd -> {
			if (cd.logFactory == null) {
				cd.logFactory = newLogFactory();
			}
			return cd.logFactory;
		});
	}
	public void setLogFactory(ILogFactory pLogFactory) {
		synchronized (this) {
			synchronized (coreData) {
				coreData.logFactory = pLogFactory;
			}
			extData = null;
		}
	}

	public IPropertyFactory getPropertyFactory() {
		return useCoreData(cd -> {
			if (cd.propertyFactory == null) {
				cd.propertyFactory = newPropertyFactory();
			}
			return cd.propertyFactory;
		});
	}
	public void setPropertyFactory(IPropertyFactory pPropertyFactory) {
		synchronized (this) {
			synchronized (coreData) {
				coreData.propertyFactory = pPropertyFactory;
			}
			extData = null;
		}
	}

	protected synchronized ExtData getExtData() {
		if (extData == null) {
			extData = new ExtData();
		}
		return extData;
	}

	public IspmConfiguration getIspmConfiguration() {
		final ExtData extData = getExtData();
		synchronized (extData) {
			if (extData.ispmConfiguration == null) {
				extData.ispmConfiguration = newIspmConfiguration();
			}
			return extData.ispmConfiguration;
		}
	}
	public IComponentFactory getComponentFactory() {
		final ExtData extData = getExtData();
		synchronized (extData) {
			if (extData.componentFactory == null) {
				extData.componentFactory = newComponentFactory();
			}
			return extData.componentFactory;
		}
	}
	protected void useCoreData(FailableConsumer<CoreData,?> pConsumer) {
		synchronized (coreData) {
			Functions.accept(pConsumer, coreData);
		}
	}

	protected <O> O useCoreData(FailableFunction<CoreData,O,?> pCallable) {
		synchronized (coreData) {
			return Functions.apply(pCallable, coreData);
		}
	}

	protected Path findFile(String... pPaths) {
		for (String s : pPaths) {
			Path p = Paths.get(s);
			if (!p.isAbsolute()) {
				p = currentDir.resolve(s);
			}
			if (Files.isRegularFile(p)) {
				return p;
			}
		}
		return null;
	}

	protected Path requireFile(String... pPaths) {
		final List<String> absolutePaths = new ArrayList<>();
		for (String s : pPaths) {
			Path p = Paths.get(s);
			if (!p.isAbsolute()) {
				p = currentDir.resolve(s);
			}
			if (Files.isRegularFile(p)) {
				return p;
			} else {
				absolutePaths.add(p.toAbsolutePath().toString());
			}
		}
		throw new IllegalStateException("Neither of the following files found: " + String.join(", ", absolutePaths));
	}

	protected Tupel<TIspmConfiguration,Path> newTIspmConfiguration() {
		final Path path = requireFile("./config/packages/" + ispmPackageName + "/ispm-configuration.xml",
				                      "./packages/" + ispmPackageName + "/config/ispm-configuration.xml");
		return new Tupel<TIspmConfiguration,Path>(IspmConfigParser.parse(path), path);
	}

	protected ILogFactory newLogFactory() {
		final Path path = requireFile("./config/packages/" + ispmPackageName + "/log4j2.xml",
				                      "./packages/" + ispmPackageName + "/config/log4j2.xml");
		final Log4j2LogFactory lf = new Log4j2LogFactory() {
			@Override
			protected URL getUrl() {
				try {
					return path.toFile().toURI().toURL();
				} catch (Throwable t) {
					throw Exceptions.show(t);
				}
			}
		};
		lf.start();
		return lf;
	}

	protected IPropertyFactory newPropertyFactory() {
		final Path customPropertiesPath = findFile("./config/packages/" + ispmPackageName + "/ispm.properties",
                "./packages/" + ispmPackageName + "/config/ispm.properties");
		final Path factoryPropertiesPath = requireFile("./packages/" + ispmPackageName + "/config/ispm-factory.properties");
		final Properties factoryProps = new Properties();
		try (InputStream in = Files.newInputStream(factoryPropertiesPath)) {
			factoryProps.load(in);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
		final Properties props = new Properties();
		props.putAll(factoryProps);
		if (customPropertiesPath != null) {
			final Properties customProperties = new Properties();
			try (InputStream in = Files.newInputStream(customPropertiesPath)) {
				customProperties.load(in);
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
			props.putAll(customProperties);
		}
		return new DefaultPropertyFactory(props);
	}

	protected IspmConfiguration newIspmConfiguration() {
		return new IspmConfigurationBuilder().build(getTIspmConfiguration(), currentDir);
	}

	protected IComponentFactory newComponentFactory() {
		final SimpleComponentFactoryBuilder scfb = new SimpleComponentFactoryBuilder().module(newModule());
		if (modules != null) {
			scfb.modules(modules);
		}
		try {
			return scfb.build();
		} catch (GroovyRuntimeException gre) {
			final ASTNode node = gre.getNode();
			if (node == null) {
				throw gre;
			} else {
				final String text = node.getText();
				final int lineNumber = node.getLineNumber();
				final int columnNumber = node.getColumnNumber();
				throw gre;
			}
		}
	}

	protected Module newModule() {
		final TIspmConfiguration tIspmConfiguration = getTIspmConfiguration();
		final ILogFactory logFactory = getLogFactory();
		final IPropertyFactory propertyFactory = getPropertyFactory();
		final IspmConfiguration ispmConfiguration = getIspmConfiguration();
		final ILifecycleController lifecycleController = new DefaultLifecycleController();
		return (b) -> {
			b.bind(ILifecycleController.class).toInstance(lifecycleController);
			b.bind(TIspmConfiguration.class).toInstance(tIspmConfiguration);
			b.bind(ILogFactory.class).toInstance(logFactory);
			b.bind(IPropertyFactory.class).toInstance(propertyFactory);
			b.bind(IspmConfiguration.class).toInstance(ispmConfiguration);
			b.bind(IspmApplication.class).toInstance(IspmApplication.this);
			b.bind(Path.class, "currentDir").toInstance(currentDir);
			b.bind(ImportFromLocalRepoAction.class).in(Scopes.NO_SCOPE);
			b.bind(AddLocalRepoAction.class).in(Scopes.NO_SCOPE);
			b.bind(ActivatePackageAction.class).in(Scopes.NO_SCOPE);
			b.bind(AddInstanceAction.class).in(Scopes.NO_SCOPE);
			b.bind(AddLocalRepoAction.class).in(Scopes.NO_SCOPE);
			b.bind(AddPluginAction.class).in(Scopes.NO_SCOPE);
			b.bind(AddRemoteRepoAction.class).in(Scopes.NO_SCOPE);
			b.bind(ISymbolicLinksHandler.class).toClass(DefaultSymbolicLinksHandler.class);
			b.bind(IServiceInvocator.class).toClass(DefaultServiceInvocator.class);
			b.bind(PackageCompilerAction.class).in(Scopes.NO_SCOPE);
			b.bind(PackageReloadAction.class).in(Scopes.NO_SCOPE);
			findBuiltinPlugins((Module m) -> m.configure(b));
			tIspmConfiguration.forEachPlugin((tp) -> {
				final Module module = newModule(tp);
				module.configure(b);
			});
		};
	}

	protected void findBuiltinPlugins(Consumer<Module> pConsumer) {
		final Path pluginsDir = getBuiltinPluginsDir();
		if (Files.isDirectory(pluginsDir)) {
			final List<Path> plugins = new ArrayList<Path>();
			try {
				Files.walk(pluginsDir, 1).forEach((p) -> {
					if (Files.isRegularFile(p)  &&  Scripts.isScriptFile(p)) {
						plugins.add(p);
					}
				});
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
			Collections.sort(plugins, (p1, p2) -> p1.getFileName().toString().compareToIgnoreCase(p2.getFileName().toString()));
			for (Path p : plugins) {
				final Script script = Scripts.compile(IReadable.of(p), null);
				final Module module = executePluginScript(script);
				pConsumer.accept(module);
			}
		}
	}

	protected Path getBuiltinPluginsDir() {
		final Path pluginsDir = Paths.get("./packages/" + ispmPackageName + "/config/plugins");
		return pluginsDir;
	}
	protected Module newModule(TPlugin pPlugin) {
		final String className = pPlugin.getClassName();
		if (Strings.isEmpty(className)) {
			final String script = pPlugin.getScript();
			if (Strings.isEmpty(script)) {
				throw new IllegalStateException("A plugin must either have a class name, or a script.");
			} else {
				final Path p1 = Paths.get("./packages/" + ispmPackageName + "/config/scripts", script);
				final Path p2 = Paths.get("./config/packages/" + ispmPackageName + "/config/scripts", script);
				final Path p;
				if (Files.isRegularFile(p2)) {
					p = p2;
				} else if (Files.isRegularFile(p1)) {
					p = p1;
				} else {
					throw new DomHelper.LocalizableException(pPlugin.getLocator(),
							"Script file " + script + " not found for plugin");
				}
				final Script scrpt = Scripts.compile(IReadable.of(p), StandardCharsets.UTF_8);
				return executePluginScript(scrpt, "properties", pPlugin.getProperties());
			}
		} else {
			final Class<Module> cl;
			try {
				@SuppressWarnings("unchecked")
				final Class<Module> clazz = (Class<Module>) Thread.currentThread().getContextClassLoader().loadClass(className);
				cl = clazz;
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
			final Module module;
			try {
				Constructor<Module> cons = cl.getDeclaredConstructor();
				cons.setAccessible(true);
				module = cons.newInstance();
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
			return module;
		}
	}

	protected Module executePluginScript(Script pScript, Object... pAttributes) {
		final Map<String,Object> map = new HashMap<>();
		if (pAttributes != null) {
			for (int i = 0;  i < pAttributes.length;  i += 2) {
				map.put((String) pAttributes[i], pAttributes[i+1]);
			}
		}
		return pScript.call(map);
	}

	public Path getTIspmConfigurationPath() {
		return useCoreData((cd) -> {
			return cd.ispmConfigurationPath;
		});
	}
}
