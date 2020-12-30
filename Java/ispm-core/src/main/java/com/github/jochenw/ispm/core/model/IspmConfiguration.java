package com.github.jochenw.ispm.core.model;

import java.util.NoSuchElementException;

import com.github.jochenw.afw.core.util.Functions.FailableConsumer;

public interface IspmConfiguration {
	public void forEachLocalRepo(FailableConsumer<ILocalRepo,?> pConsumer);
	public void forEachRemoteRepo(FailableConsumer<IRemoteRepo,?> pConsumer);
	public void forEachInstance(FailableConsumer<IInstance,?> pConsumer);
	public ILocalRepo getLocalRepo(String pId);
	public IRemoteRepo getRemoteRepo(String pId);
	public IInstance getInstance(String pId);
	public default ILocalRepo requireLocalRepo(String pId) throws NoSuchElementException {
		final ILocalRepo localRepo = getLocalRepo(pId);
		if (localRepo == null) {
			throw new NoSuchElementException("Local repository not found: " + pId);
		}
		return localRepo;
	}
	public default IRemoteRepo requireRemoteRepo(String pId) throws NoSuchElementException {
		final IRemoteRepo remoteRepo = getRemoteRepo(pId);
		if (remoteRepo == null) {
			throw new NoSuchElementException("Remote repository not found: " + pId);
		}
		return remoteRepo;
	}
	public default IInstance requireInstance(String pId) throws NoSuchElementException {
		final IInstance instance = getInstance(pId);
		if (instance == null) {
			throw new NoSuchElementException("Instance not found: " + pId);
		}
		return instance;
	}
}
