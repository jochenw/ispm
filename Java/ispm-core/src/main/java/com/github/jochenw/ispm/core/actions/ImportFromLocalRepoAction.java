package com.github.jochenw.ispm.core.actions;

import java.nio.file.Files;
import java.nio.file.Path;

import javax.inject.Inject;

import com.github.jochenw.afw.core.components.ISymbolicLinksHandler;
import com.github.jochenw.afw.core.inject.IComponentFactory;
import com.github.jochenw.afw.core.inject.LogInject;
import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.ispm.core.model.IInstance;
import com.github.jochenw.ispm.core.model.ILocalRepo;
import com.github.jochenw.ispm.core.model.ILocalRepoLayout;
import com.github.jochenw.ispm.core.model.ILocalRepoLayout.IProject;


public class ImportFromLocalRepoAction extends AbstractAction {
	private @Inject ISymbolicLinksHandler symLinkHandler;
	private @LogInject ILog log;
	private boolean overwriteExisting;

	public Context importFromLocalRepo(Context pCtx, IInstance pInstance, ILocalRepo pLocalRepo, String pProjectId) {
		final String mName = "importFromLocalRepo";
		return super.run(pCtx, mName, log, (ctx) -> {
			ctx.action("Importing project from local repository: projectId=" + pProjectId + ", localRepoId=" + pLocalRepo.getId());
			ctx.debug(log, mName, "Importing project from local repository: projectId=" + pProjectId + ", localRepoId=" + pLocalRepo.getId() + ", instanceId=" + pInstance.getId());
			final IComponentFactory componentFactory = getComponentFactory();
			final ILocalRepoLayout localRepoLayout = componentFactory.requireInstance(ILocalRepoLayout.class, pLocalRepo.getLayout());
			final IProject project = localRepoLayout.requireProject(pLocalRepo, pProjectId);
			final Path packagesDir = pInstance.getPackagesDir();
			localRepoLayout.forEach(pLocalRepo, project, (p) -> {
				final Path packageSourceDir = p.getPath();
				final Path packageTargetDir = packagesDir.resolve(p.getName());
				if (Files.exists(packageTargetDir)) {
					final Path symLinkSource = symLinkHandler.checkLink(packageTargetDir);
					if (symLinkSource == null) {
						final String msg = "Package " + p.getName() + " is already present in instance " + pInstance.getId()
						+ ", and not overwritable.";
						log.error(mName, msg);
						throw new IllegalStateException(msg);
					} else if (symLinkSource.equals(packageSourceDir)) {
						ctx.info(log, mName, "Package " + p.getName() + " is already linked to project " + project.getId()
								 + ", nothing to do.");
						return;
					} else if (overwriteExisting) {
						ctx.info(log, mName, "Package " + p.getName() + " is already present in instance " + project.getId()
						                     + ", but pointing to another location. Permission to overwrite is given, overwriting");
						symLinkHandler.removeLink(packageTargetDir);
					} else {
						final String msg = "Package " + p.getName() + " is already present in instance " + pInstance.getId()
										+ ", and overwritable, but permission to overwrite has not been given.";
						log.error(mName, msg);
						throw new IllegalStateException(msg);
					}
				}
				symLinkHandler.createLink(packageSourceDir.toAbsolutePath(), packageTargetDir.toAbsolutePath());
				final ActivatePackageAction activatePackageAction = componentFactory.requireInstance(ActivatePackageAction.class);
				activatePackageAction.activatePackage(null, pInstance, p.getName());
			});
			ctx.debug(log, mName, "Imported project from local repository: projectId=" + pProjectId);
			ctx.action("Imported project from local repository: projectId=" + pProjectId);
		});
	}
}
