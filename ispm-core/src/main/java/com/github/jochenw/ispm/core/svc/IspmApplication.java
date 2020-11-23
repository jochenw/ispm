package com.github.jochenw.ispm.core.svc;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.github.jochenw.afw.core.ILifecycleController;
import com.github.jochenw.afw.core.ILifecycleController.Listener;
import com.github.jochenw.afw.core.impl.DefaultLifecycleController;
import com.github.jochenw.afw.core.inject.ComponentFactoryBuilder.Module;
import com.github.jochenw.afw.core.inject.IComponentFactory;
import com.github.jochenw.afw.core.inject.Scopes;
import com.github.jochenw.afw.core.inject.simple.SimpleComponentFactoryBuilder;
import com.github.jochenw.afw.core.log.ILogFactory;
import com.github.jochenw.afw.core.log.log4j.Log4j2LogFactory;
import com.github.jochenw.afw.core.props.DefaultPropertyFactory;
import com.github.jochenw.afw.core.props.IPropertyFactory;
import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Files;
import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.ispm.core.config.IspmConfigParser;
import com.github.jochenw.ispm.core.config.IspmConfigParser.ParsedConfiguration;
import com.github.jochenw.ispm.core.impl.DefaultConfiguration;
import com.github.jochenw.ispm.core.model.IConfiguration;
import com.github.jochenw.ispm.core.model.IInstance;

public class IspmApplication {
	private static IspmApplication instance = new IspmApplication();

	public static synchronized IspmApplication getInstance() {
		return instance;
	}

	public static synchronized void setInstance(IspmApplication instance) {
		IspmApplication.instance = instance;
	}

	private IComponentFactory componentFacory;
	private final List<Module> modules;
	private ILogFactory logFactory;
	private IPropertyFactory propertyFactory;
	private ParsedConfiguration parsedConfiguration;
	private DefaultConfiguration configuration;
	private IInstance localInstance;
	private ClassLoader classLoader;

	public IspmApplication(List<Module> pModules) {
		modules = pModules;
	}

	public IspmApplication() {
		modules = null;
	}

	public synchronized IInstance getLocalInstance() {
		if (localInstance == null) {
			localInstance = newLocalInstance();
		}
		return localInstance;
	}

	public synchronized ClassLoader getClassLoader() {
		if (classLoader == null) {
			classLoader = Objects.requireNonNull(newClassLoader());
		}
		return classLoader;
	}

	protected ClassLoader newClassLoader() {
		return Thread.currentThread().getContextClassLoader();
	}

	public synchronized IConfiguration getConfiguration() {
		if (configuration == null) {
			configuration = Objects.requireNonNull(newConfiguration());
		}
		return configuration;
	}

	public synchronized ParsedConfiguration getParsedConfiguration() {
		if (parsedConfiguration == null) {
			parsedConfiguration = Objects.requireNonNull(newParsedConfiguration());
		}
		return parsedConfiguration;
	}

	public synchronized IPropertyFactory getPropertyFactory() {
		if (propertyFactory == null) {
			propertyFactory = Objects.requireNonNull(newPropertyFactory());
		}
		return propertyFactory;
	}

	public synchronized ILogFactory getLogFactory() {
		if (logFactory == null) {
			logFactory = Objects.requireNonNull(newLogFactory());
		}
		return logFactory;
	}

	public synchronized IComponentFactory getComponentFactory() {
		if (componentFacory == null) {
			componentFacory = Objects.requireNonNull(newComponentFactory());
		}
		return componentFacory;
	}

	protected IComponentFactory newComponentFactory() {
		final SimpleComponentFactoryBuilder scfb = new SimpleComponentFactoryBuilder();
		final ILifecycleController lc = new DefaultLifecycleController();
		final ILogFactory lf = getLogFactory();
		if (lf instanceof Listener) {
			lc.addListener((Listener) lf);
		}
		final IPropertyFactory pf = getPropertyFactory();
		if (pf instanceof Listener) {
			lc.addListener((Listener) pf);
		}
		scfb.module((b) -> {
			b.bind(ILifecycleController.class).toInstance(lc);
			b.bind(ILogFactory.class).toInstance(lf);
			b.bind(IPropertyFactory.class).toInstance(pf);
			b.bind(ParsedConfiguration.class).toSupplier(() -> getParsedConfiguration()).in(Scopes.NO_SCOPE);
			b.bind(IConfiguration.class).toSupplier(() -> getConfiguration()).in(Scopes.NO_SCOPE);
			b.bind(ClassLoader.class).toInstance(getClassLoader());
			b.bind(IspmConfigParser.class).in(Scopes.NO_SCOPE);
			b.bind(IInstance.class).toInstance(getLocalInstance());
		});
		if (modules != null) {
			scfb.modules(modules);
		}
		return scfb.build();
	}

	protected ILogFactory newLogFactory() {
		return new Log4j2LogFactory() {
			@Override
			protected URL getUrl() {
				return requireUrl("./config/WxIspm/log4j2.xml", "./packages/WxIspm/config/log4j2.xml");
			}
		};
	}

	protected IPropertyFactory newPropertyFactory() {
		final URL factoryPropertyUrl = requireUrl("./packages/WxIspm/config/ispm-factory.properties");
		final URL instanceUrl = requireUrl("./config/WxIspm/ispm.properties",
				                           "./packages/WxIspm/config/ispm.properties");
		return new DefaultPropertyFactory(factoryPropertyUrl, instanceUrl);
	}

	protected ParsedConfiguration newParsedConfiguration() {
		final URL configUrl = requireUrl("./config/WxIspm/ispm-config.xml",
				                         "./packages/WxIspm/config/ispm-config.xml");
		return getComponentFactory().requireInstance(IspmConfigParser.class).parse(configUrl);
	}

	protected DefaultConfiguration newConfiguration() {
		throw new IllegalStateException("TODO: Implement me!");
	}

	protected URL requireUrl(String... pUris) {
		final URL url = getUrl(pUris);
		if (url == null) {
			throw new IllegalStateException("Unable to locate either of the folllowing URI's: "
					+ String.join(", ", pUris));
		}
		return url;
	}

	protected URL getUrl(String... pUris) {
		for (String uri : pUris) {
			final Path path = Paths.get(uri);
			if (Files.isRegularFile(path)) {
				try {
					return path.toUri().toURL();
				} catch (MalformedURLException e) {
					throw Exceptions.show(e);
				}
			}
		}
		for (String uri : pUris) {
			URL url = classLoader.getResource(uri);
			if (url != null) {
				return null;
			}
			return url;
		}
		return null;
	}

	protected IInstance newLocalInstance() {
		final Path packagesDir = Paths.get("packages");
		final Path logsDir = Paths.get("logs");
		final Path configDir = Paths.get("config");
		final Path baseDir = Paths.get("");
		return new IInstance() {
			@Override
			public boolean isDefault() {
				return true;
			}
			
			@Override
			public Path getPackagesDir() {
				return packagesDir;
			}
			
			@Override
			public Path getLogDir() {
				return logsDir;
			}
			
			@Override
			public String getId() {
				return "default";
			}
			
			@Override
			public Path getConfigDir() {
				return configDir;
			}
			
			@Override
			public Path getBaseDir() {
				return baseDir;
			}
		};
	}
}
