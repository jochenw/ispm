package com.github.jochenw.ispm.core.model;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import com.github.jochenw.afw.core.inject.LogInject;
import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.afw.core.log.ILogFactory;
import com.github.jochenw.afw.core.util.DomHelper;
import com.github.jochenw.afw.core.util.Files;
import com.github.jochenw.afw.core.util.Functions;
import com.github.jochenw.afw.core.util.Functions.FailableConsumer;
import com.github.jochenw.afw.core.util.Strings;
import com.github.jochenw.ispm.core.config.TIspmConfiguration;
import com.github.jochenw.ispm.core.config.TIspmConfiguration.TInstance;
import com.github.jochenw.ispm.core.config.TIspmConfiguration.TLocalRepo;
import com.github.jochenw.ispm.core.config.TIspmConfiguration.TRemoteRepo;

public class IspmConfigurationBuilder {
	private final ILog log;

	public IspmConfigurationBuilder(ILogFactory pLogFactory) {
		log = pLogFactory.getLog(IspmConfigurationBuilder.class);
	}

	public IspmConfiguration build(TIspmConfiguration pIspmConfiguration, Path pBaseDir) {
		final String mName = "build";
		log.entering(mName, "Path", pBaseDir);
		final Map<String,ILocalRepo> localRepos = new HashMap<>();
		final Map<String,IRemoteRepo> remoteRepos = new HashMap<>();
		final Map<String,IInstance> instances = new HashMap<>();
		pIspmConfiguration.forEachInstance((id,inst) -> {
			instances.put(id, newInstance(inst));
		});
		if (pBaseDir != null) {
			instances.put("local", newLocalInstance(pBaseDir));
		}
		pIspmConfiguration.forEachLocalRepo((id,repo) -> {
			localRepos.put(id, newLocalRepo(repo));
		});
		pIspmConfiguration.forEachRemoteRepo((id,repo) -> {
			remoteRepos.put(id, newRemoteRepo(repo));
		});
		log.exiting(mName, "Sets", Strings.toString(instances.keySet()),
				    Strings.toString(localRepos.keySet()), Strings.toString(remoteRepos.keySet()));
		return new IspmConfiguration() {
			@Override
			public IRemoteRepo getRemoteRepo(String pId) {
				return remoteRepos.get(pId);
			}
			
			@Override
			public ILocalRepo getLocalRepo(String pId) {
				return localRepos.get(pId);
			}
			
			@Override
			public IInstance getInstance(String pId) {
				return instances.get(pId);
			}
			
			@Override
			public void forEachRemoteRepo(FailableConsumer<IRemoteRepo, ?> pConsumer) {
				remoteRepos.forEach((id, repo) -> Functions.accept(pConsumer, repo));
			}
			
			@Override
			public void forEachLocalRepo(FailableConsumer<ILocalRepo, ?> pConsumer) {
				localRepos.forEach((id, repo) -> Functions.accept(pConsumer, repo));
			}
			
			@Override
			public void forEachInstance(FailableConsumer<IInstance, ?> pConsumer) {
				instances.forEach((id, inst) -> Functions.accept(pConsumer, inst));
			}
		};
	}

	protected IRemoteRepo newRemoteRepo(TRemoteRepo pRepo) {
		final String id = pRepo.getId();
		final String url = pRepo.getUrl();
		final String handler = pRepo.getHandler();
		final Map<String,String> properties = pRepo.getProperties();
		return new IRemoteRepo() {
			@Override
			public String getId() {
				return id;
			}

			@Override
			public String getUrl() {
				return url;
			}

			@Override
			public String getHandler() {
				return handler;
			}

			@Override
			public Map<String, String> getProperties() {
				return properties;
			}
			
		};
	}

	protected ILocalRepo newLocalRepo(TLocalRepo pRepo) {
		final String id = pRepo.getId();
		final Path dir = Paths.get(pRepo.getBaseDir());
		if (!Files.isDirectory(dir)) {
			throw new DomHelper.LocalizableException(pRepo.getLocator(), "Directory not found for local repository " + id + ": " + dir);
		}
		final String layout = pRepo.getLayout();
		final Map<String,String> properties = pRepo.getProperties();
		return new ILocalRepo() {
			@Override
			public String getId() {
				return id;
			}

			@Override
			public Path getDir() {
				return dir;
			}

			@Override
			public String getLayout() {
				return layout;
			}

			@Override
			public Map<String, String> getProperties() {
				return properties;
			}
		};
	}

	protected IInstance newInstance(TInstance pInstance) {
		final String id = pInstance.getId();
		final Map<String,String> properties = pInstance.getProperties();
		final String baseDirStr = pInstance.getBaseDir();
		final Path baseDir = Paths.get(baseDirStr);
		if (!Files.isDirectory(baseDir)) {
			throw new DomHelper.LocalizableException(pInstance.getLocator(),
					                                 "Base directory not found for instance " + pInstance.getId());
		}
		final BiFunction<String,String,Path> resolver = (s,msg) -> {
			final Path p = Paths.get(s.replace("${baseDir}", baseDirStr));
			if (!Files.isDirectory(p)) {
				throw new DomHelper.LocalizableException(pInstance.getLocator(),
						msg + " not found for instance " + pInstance.getId() + ": " + p);
			}
			return p;
		};
		final Path wmHomeDir = resolver.apply(pInstance.getWmHomeDir(), "Wm Home directory");
		final Path packagesDir = resolver.apply(pInstance.getPackagesDir(), "Packages directory");
		final Path configDir = resolver.apply(pInstance.getConfigDir(), "Config directory");
		final Path logsDir = resolver.apply(pInstance.getLogsDir(), "Logs directory");
		return new IInstance() {
			@Override
			public String getId() {
				return id;
			}

			@Override
			public Map<String, String> getProperties() {
				return properties;
			}

			@Override
			public Path getBaseDir() {
				return baseDir;
			}

			@Override
			public Path getWmHomeDir() {
				return wmHomeDir;
			}

			@Override
			public Path getPackagesDir() {
				return packagesDir;
			}

			@Override
			public Path getConfigDir() {
				return configDir;
			}

			@Override
			public Path getLogsDir() {
				return logsDir;
			}

			@Override
			public boolean isLocal() {
				return false;
			}
			
		};
	}

	protected IInstance newLocalInstance(Path pBaseDir) {
		final Map<String,String> properties = Collections.emptyMap();
		return new IInstance() {
			@Override
			public String getId() {
				return "local";
			}

			@Override
			public Map<String, String> getProperties() {
				return properties;
			}

			@Override
			public Path getBaseDir() {
				return pBaseDir;
			}

			@Override
			public Path getWmHomeDir() {
				return pBaseDir.resolve("../../..");
			}

			@Override
			public Path getPackagesDir() {
				return pBaseDir.resolve("packages");
			}

			@Override
			public Path getConfigDir() {
				return pBaseDir.resolve("config");
			}

			@Override
			public Path getLogsDir() {
				return pBaseDir.resolve("logs");
			}

			@Override
			public boolean isLocal() {
				return true;
			}
		};
	}
}
