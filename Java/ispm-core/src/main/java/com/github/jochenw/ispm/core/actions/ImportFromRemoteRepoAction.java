package com.github.jochenw.ispm.core.actions;

import java.net.URL;
import java.nio.file.Path;

import javax.inject.Inject;

import com.github.jochenw.afw.core.inject.IComponentFactory;
import com.github.jochenw.afw.core.inject.LogInject;
import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.afw.core.util.Files;
import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.ispm.core.model.IInstance;
import com.github.jochenw.ispm.core.model.ILocalRepo;
import com.github.jochenw.ispm.core.model.ILocalRepoLayout;
import com.github.jochenw.ispm.core.model.IRemoteRepo;
import com.github.jochenw.ispm.core.model.IRemoteRepoHandler;
import com.github.jochenw.ispm.core.model.IspmConfiguration;

public class ImportFromRemoteRepoAction extends AbstractAction {
	private @Inject IComponentFactory componentFactory;
	private @Inject IspmConfiguration configuration;
	private @LogInject ILog log;

	public Context importFromRemoteRepo(String pProjectId, String pInstanceId, String pLocalRepoId, String pRemoteRepoId,
			                            boolean pDeleteExistingProject) {
		final String mName = "importFromRemoteRepo";
		return super.run(null, mName, log, (ctx) -> {
			final String msg = "Importing remote project: projectId=" + pProjectId + ", instanceId=" + pInstanceId
						   + ", localRepoId=" + pLocalRepoId + ", remoteRepoId=" + pRemoteRepoId;
			ctx.action(msg);
			ctx.debug(log, mName, msg);
			final String projectId = Objects.requireNonNull(pProjectId, "Project Id");
			final ILocalRepo localRepo = configuration.requireLocalRepo(Objects.requireNonNull(pLocalRepoId, "Local Repo Id"));
			final IRemoteRepo remoteRepo = configuration.requireRemoteRepo(Objects.requireNonNull(pRemoteRepoId, "Remote Repo Id"));
			final IInstance instance = configuration.requireInstance(pInstanceId);
			final ILocalRepoLayout layout = configuration.requireLocalRepoLayout(componentFactory, localRepo);
			final IRemoteRepoHandler handler = configuration.requireRemoteRepoHandler(componentFactory, remoteRepo);
			final Path localProjectDir = layout.getProjectDir(localRepo, projectId);
			if (java.nio.file.Files.exists(localProjectDir)) {
				if (java.nio.file.Files.isDirectory(localProjectDir)) {
					if (pDeleteExistingProject) {
						ctx.action("Permission to delete existing project directory given, deleting " + localProjectDir);
						ctx.info(log, mName, "Permission to delete existing project directory given, deleting " + localProjectDir);
						Files.removeDirectory(localProjectDir);
					} else {
						throw new IllegalStateException("Permission to delete existing directory "
								+ localProjectDir + " not given");
					}
				} else {
					throw new IllegalStateException("Local project directory " + localProjectDir
							   + " is invalid, file or other object with the same name already exists.");
				}
			}
			final URL url = handler.getProjectUrl(remoteRepo, projectId);
			handler.cloneProjectTo(remoteRepo, projectId, url, localProjectDir);
		});
	}
}
