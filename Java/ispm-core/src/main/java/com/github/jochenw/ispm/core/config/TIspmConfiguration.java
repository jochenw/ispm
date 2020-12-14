package com.github.jochenw.ispm.core.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.Locator;

import com.github.jochenw.afw.core.util.DomHelper;
import com.github.jochenw.afw.core.util.Functions;
import com.github.jochenw.afw.core.util.Functions.FailableBiConsumer;
import com.github.jochenw.afw.core.util.Functions.FailableConsumer;
import com.github.jochenw.afw.core.util.LocalizableDocument;
import com.github.jochenw.afw.core.util.Objects;

public class TIspmConfiguration {
	public static class TPropertiesContainer implements Serializable {
		private static final long serialVersionUID = -5964170036565905562L;
		private final Locator locator;
		private final Map<String,String> properties = new HashMap<>();

		public TPropertiesContainer(Locator pLocator) {
			locator = Objects.requireNonNull(pLocator, "Locator");
		}

		public Locator getLocator() { return locator; }
		public String setProperty(String pKey, String pValue) {
			final String key = Objects.requireNonNull(pKey, "Key");
			final String value = Objects.requireNonNull(pValue, "Value");
			return properties.put(key, value);
		}
		public String getProperty(String pKey) { return properties.get(pKey); }
		public Map<String,String> getProperties() { return Collections.unmodifiableMap(properties); }
	}
	public static class TIdentifiable extends TPropertiesContainer {
		private static final long serialVersionUID = 3662439613280402574L;
		private final String id;
		public TIdentifiable(Locator pLocator, String pId) {
			super(pLocator);
			id = Objects.requireNonNull(pId, "Id");
		}
		public String getId() { return id; }
	}
	public static class TPlugin extends TPropertiesContainer {
		private static final long serialVersionUID = 8332489482266907307L;
		private final String className, script;
		public TPlugin(Locator pLocator, String pClass, String pScript) {
			super(pLocator);
			className = Objects.requireNonNull(pClass, "Class");
			script = Objects.requireNonNull(pScript, "Script");
		}
		public String getClassName() { return className; }
		public String getScript() { return script; }
	}
	public static class TLocalRepo extends TIdentifiable {
		private static final long serialVersionUID = 997874883022903837L;
		private final String baseDir, layout;
		public TLocalRepo(Locator pLocator, String pId, String pBaseDir, String pLayout) {
			super(pLocator, pId);
			baseDir = Objects.requireNonNull(pBaseDir, "BaseDir");
			layout = pLayout;
		}
		public String getBaseDir() { return baseDir; }
		public String getLayout() { return layout; }
	}
	public static class TRemoteRepo extends TIdentifiable {
		private static final long serialVersionUID = 5161122019885069740L;
		private final String url, handler;
		public TRemoteRepo(Locator pLocator, String pId, String pUrl, String pHandler) {
			super(pLocator, pId);
			url = pUrl;
			handler = pHandler;
		}
		public String getUrl() { return url; }
		public String getHandler() { return handler; }
	}
	public static class TInstance extends TIdentifiable {
		private static final long serialVersionUID = -4696274153408001765L;
		private final String baseDir, wmHomeDir, packagesDir, configDir, logsDir;
		public TInstance(Locator pLocator, String pId, String pBaseDir, String pWmHomeDir, String pPackagesDir,
				         String pConfigDir, String pLogsDir) {
			super(pLocator, pId);
			baseDir = Objects.requireNonNull(pBaseDir, "BaseDir");
			wmHomeDir = Objects.requireNonNull(pWmHomeDir, "WmHomeDir");
			packagesDir = Objects.requireNonNull(pPackagesDir, "PackagesDir");
			configDir = Objects.requireNonNull(pConfigDir, "ConfigDir");
			logsDir = Objects.requireNonNull(pLogsDir, "LogsDir");
		}
		public String getBaseDir() { return baseDir; }
		public String getWmHomeDir() { return wmHomeDir; }
		public String getPackagesDir() { return packagesDir; }
		public String getConfigDir() { return configDir; }
		public String getLogsDir() { return logsDir; }
	}

	private final List<TPlugin> plugins = new ArrayList<>();
	/* We're intentionally using linked maps, not hash maps, because we want to preserve the order of elements when writing the configuration
	 * to a file.
	 */
	private final Map<String,TLocalRepo> localRepos = new LinkedHashMap<>();
	private final Map<String,TRemoteRepo> remoteRepos = new LinkedHashMap<>();
	private final Map<String,TInstance> instances = new LinkedHashMap<>();

	public void forEach(FailableConsumer<TPlugin,?> pConsumer) { plugins.forEach(Functions.asConsumer(pConsumer)); }
	public void forEachLocalRepo(FailableBiConsumer<String,TLocalRepo,?> pConsumer) { localRepos.forEach(Functions.asBiConsumer(pConsumer)); }
	public void forEachRemoteRepo(FailableBiConsumer<String,TRemoteRepo,?> pConsumer) { remoteRepos.forEach(Functions.asBiConsumer(pConsumer)); }
	public void forEachInstance(FailableBiConsumer<String,TInstance,?> pConsumer) { instances.forEach(Functions.asBiConsumer(pConsumer)); }
	public void addPlugin(TPlugin pPlugin) { plugins.add(pPlugin); }
	public void addLocalRepo(TLocalRepo pLocalRepo) {
		final TLocalRepo localRepo = Objects.requireNonNull(pLocalRepo, "LocalRepo");
		if (localRepos.putIfAbsent(localRepo.getId(), localRepo) != null) {
			throw new DomHelper.LocalizableException(localRepo.getLocator(), "Duplicate id for localRepo: " + localRepo.getId());
		}
	}
	public void addRemoteRepo(TRemoteRepo pRemoteRepo) {
		final TRemoteRepo remoteRepo = Objects.requireNonNull(pRemoteRepo, "RemoteRepo");
		if (remoteRepos.putIfAbsent(remoteRepo.getId(), remoteRepo) != null) {
			throw new DomHelper.LocalizableException(remoteRepo.getLocator(), "Duplicate id for remoteRepo: " + remoteRepo.getId());
		}
	}
	public void addInstance(TInstance pInstance) {
		final TInstance instance = Objects.requireNonNull(pInstance, "Instance");
		if ("local".equals(instance.getId())) {
			throw new DomHelper.LocalizableException(instance.getLocator(), "Invalid id for instance: 'local' is reserved.");
		}
		if (instances.putIfAbsent(instance.getId(), instance) != null) {
			throw new DomHelper.LocalizableException(instance.getLocator(), "Duplicate id for instance: " + instance.getId());
		}
	}
	public TLocalRepo getLocalRepo(String pId) {
		return localRepos.get(pId);
	}
	public TRemoteRepo getRemoteRepo(String pId) {
		return remoteRepos.get(pId);
	}
	public TInstance getInstance(String pId) {
		return instances.get(pId);
	}
	public List<TPlugin> getPlugins() { return Collections.unmodifiableList(plugins); }
	public Map<String,TLocalRepo> getLocalRepos() { return Collections.unmodifiableMap(localRepos); }
	public Map<String,TRemoteRepo> getRemoteRepos() { return Collections.unmodifiableMap(remoteRepos); }
	public Map<String,TInstance> getInstances() { return Collections.unmodifiableMap(instances); }
}
	