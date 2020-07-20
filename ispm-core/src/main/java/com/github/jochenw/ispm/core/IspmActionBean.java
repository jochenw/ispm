package com.github.jochenw.ispm.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import com.github.jochenw.afw.core.inject.IComponentFactory;
import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.ispm.core.config.IConfiguration;
import com.github.jochenw.ispm.core.config.IConfiguration.IIsInstance;
import com.github.jochenw.ispm.core.plugins.ILocalRepositoryPlugin;
import com.github.jochenw.ispm.core.plugins.IRemoteRepositoryPlugin;


public class IspmActionBean {
	/** Instances of this class are produced by the {@link #list(Consumer)} method.
	 */
	public static interface IsPkgInfo {
		/**
		 * @return the packageName
		 */
		public String getPackageName();

		/**
		 * @return the localRepositoryId
		 */
		public String getLocalRepositoryId();

		/**
		 * @return the remoteRepositoryId
		 */
		public String getRemoteRepositoryId();
	}

	public static interface IsInstanceInfo {
		/**
		 * @return the id
		 */
		public String getId();

		/**
		 * @return the dir
		 */
		public Path getDir();
	}

	private IComponentFactory componentFactory;
	private String isInstanceId;

	/**
	 * @return the isInstanceId
	 */
	public String getIsInstanceId() {
		return isInstanceId;
	}

	/**
	 * @param pIsInstanceId the isInstanceId to set
	 */
	public void setIsInstanceId(String pIsInstanceId) {
		isInstanceId = pIsInstanceId;
	}

	/**
	 * @return the componentFactory
	 */
	public IComponentFactory getComponentFactory() {
		return componentFactory;
	}

	/**
	 * @param pComponentFactory the componentFactory to set
	 */
	public void setComponentFactory(IComponentFactory pComponentFactory) {
		componentFactory = pComponentFactory;
	}

	public @Nonnull IIsInstance getIsInstance() {
		if (isInstanceId == null) {
			final IIsInstance defaultInstance = getConfiguration().getDefaultInstance();
			if (defaultInstance == null) {
				throw new IllegalStateException("Configuration doesn't specify a default instance.");
			}
			return defaultInstance;
		} else {
			final List<IIsInstance> instances = getConfiguration().getIsInstances();
			for (IIsInstance instance : instances) {
				if (isInstanceId.equals(instance.getId())) {
					return instance;
				}
			}
			throw new IllegalStateException("Configuration doesn't specify an instance with this id: " + isInstanceId);
		}
	}

	public IConfiguration getConfiguration() {
		return componentFactory.requireInstance(IConfiguration.class);
	}

	public List<ILocalRepositoryPlugin> getLocalRepositoryPlugins() {
		@SuppressWarnings("unchecked")
		final List<ILocalRepositoryPlugin> list = componentFactory.requireInstance(List.class, ILocalRepositoryPlugin.class.getName());
		return list;
	}

	public List<IRemoteRepositoryPlugin> getRemoteRepositoryPlugins() {
		@SuppressWarnings("unchecked")
		final List<IRemoteRepositoryPlugin> list = componentFactory.requireInstance(List.class, IRemoteRepositoryPlugin.class.getName());
		return list;
	}
	
	public void list(Consumer<IsPkgInfo> pConsumer) {
		final List<ILocalRepositoryPlugin> localRepositoryPlugins = getLocalRepositoryPlugins();
		final List<IRemoteRepositoryPlugin> remoteRepositoryPlugins = getRemoteRepositoryPlugins();
		final Path packageDir = getIsInstance().getPackageDir();
		try {
			Files.walk(packageDir, 1).forEach((p) -> {
				if (!packageDir.equals(p)) {
					if (Files.isDirectory(p)) {
						if (Files.isRegularFile(p.resolve("manifest.v3"))) {
							Path lrpPath = null;
							String lrpId = null;
							for (ILocalRepositoryPlugin lrp : localRepositoryPlugins) {
								final Path lrpp = lrp.getLocalRepositoryPathOf(p);
								if (lrpp != null) {
									lrpPath = lrpp;
									lrpId = lrp.getId();
									break;
								}
							}
							final String localRepoId;
							final Path localRepoPath;
							if (lrpPath == null) {
								localRepoPath = null;
								localRepoId = "none";
							} else {
								localRepoId = lrpId;
								localRepoPath = lrpPath;
							}
							final String remoteRepoId;
							String rrpId = null;
							for (IRemoteRepositoryPlugin rrp : remoteRepositoryPlugins) {
								final String rrpp = rrp.getRemoteRepositoryUrl(localRepoPath);
								if (rrpp != null) {
									rrpId = rrp.getId();
									break;
								}
							}
							if (rrpId == null) {
								remoteRepoId = rrpId;
							} else {
								remoteRepoId = rrpId;
							}
							final IsPkgInfo pkInfo = new IsPkgInfo() {
								@Override
								public String getRemoteRepositoryId() {
									return remoteRepoId;
								}

								@Override
								public String getPackageName() {
									return p.getFileName().toString();
								}

								@Override
								public String getLocalRepositoryId() {
									return localRepoId;
								}
							};
							pConsumer.accept(pkInfo);
						}
					}
				}
			});
		} catch (IOException e) {
			throw Exceptions.show(e);
		}
	}

	public void listInstances(Consumer<IsInstanceInfo> pConsumer) {
		final IConfiguration config = getConfiguration();
		for (IIsInstance instance : config.getIsInstances()) {
			final IsInstanceInfo info = new IsInstanceInfo() {
				@Override
				public String getId() {
					return instance.getId();
				}

				@Override
				public Path getDir() {
					return instance.getPath();
				}
			};
			pConsumer.accept(info);
		}
	}
}
