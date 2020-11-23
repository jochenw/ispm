package com.github.jochenw.ispm.core.model;

import java.util.NoSuchElementException;

public interface IConfiguration {
	public ILocalRepo getLocalRepo(String pLocalRepoId);
	public ILocalRepo getDefaultLocalRepo();
	public IInstance getInstance(String pInstanceId);
	public IInstance getDefaultInstance();

	public default ILocalRepo requireLocalRepo(String pLocalRepoId) {
		final ILocalRepo localRepo = getLocalRepo(pLocalRepoId);
		if (localRepo == null) {
			throw new NoSuchElementException("Local repository not found: " + pLocalRepoId);
		}
		return localRepo;
	}

	public default IInstance requireInstance(String pInstanceId) {
		final IInstance inst = getInstance(pInstanceId);
		if (inst == null) {
			throw new NoSuchElementException("Instance not found: " + pInstanceId);
		}
		return inst;
	}
}
