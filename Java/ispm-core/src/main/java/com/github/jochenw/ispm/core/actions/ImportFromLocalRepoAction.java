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

	public void run(IInstance pInstance, ILocalRepo pLocalRepo, String pProjectId) {
		final String mName = "run";
		log.entering(mName);
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
					log.info(mName, "Package " + p.getName() + " is already linked to project " + project.getId()
					    + ", nothing to do.");
					return;
				} else if (overwriteExisting) {
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
			activatePackageAction.activatePackage(pInstance, p.getName());
		});
	}
}
