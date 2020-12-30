package com.github.jochenw.ispm.core.model;

import java.nio.file.Path;
import java.util.NoSuchElementException;

import com.github.jochenw.afw.core.util.Functions.FailableConsumer;

public interface ILocalRepoLayout {
	public interface IPkg {
		public String getName();
		public Path getPath();
	}
	public interface IProject {
		public String getId();
		public Path getPath();
	}
	public void forEach(ILocalRepo pRepository, FailableConsumer<IProject,?> pConsumer);
	public IProject getProject(ILocalRepo pRepository, String pId);
	public default IProject requireProject(ILocalRepo pRepository, String pId) throws NoSuchElementException {
		final IProject project = getProject(pRepository, pId);
		if (project == null) {
			throw new NoSuchElementException("Project not found in repository " + pRepository.getId() + ": " + pId);
		}
		return project;
	}
	public void forEach(ILocalRepo pRepository, IProject pProject, FailableConsumer<IPkg,?> pConsumer);
	public IPkg getPackage(ILocalRepo pRepository, IProject pProject, String pPkgId);
	public default IPkg requirePackage(ILocalRepo pRepository, IProject pProject, String pPkgId) {
		final IPkg pkg = getPackage(pRepository, pProject, pPkgId);
		if (pkg == null) {
			throw new NoSuchElementException("Package not found in repository " + pRepository.getId() + ", project " + pProject.getId()
			                                 + ": " + pPkgId);			
		}
		return pkg;
	}
}
