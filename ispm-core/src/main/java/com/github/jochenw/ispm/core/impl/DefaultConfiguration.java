package com.github.jochenw.ispm.core.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.jochenw.afw.core.util.Holder;
import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.ispm.core.model.IConfiguration;
import com.github.jochenw.ispm.core.model.IInstance;
import com.github.jochenw.ispm.core.model.ILocalRepo;


public class DefaultConfiguration implements IConfiguration {
	private Map<String,IInstance> instances = new ConcurrentHashMap<>();
	private Map<String,ILocalRepo> localRepos = new ConcurrentHashMap<>();

	@Override
	public ILocalRepo getLocalRepo(String pLocalRepoId) {
		final String repoId = Objects.requireNonNull(pLocalRepoId, "Repository Id");
		return localRepos.get(repoId);
	}

	@Override
	public ILocalRepo getDefaultLocalRepo() {
		final Holder<ILocalRepo> holder = new Holder<>();
		localRepos.forEach((id, repo) -> {
			if (repo.isDefault()) {
				if (holder.get() == null) {
					holder.set(repo);
				} else {
					throw new IllegalStateException("Multiple default repositories detected: "
							+ holder.get().getId() + ", and " + id);
				}
			}
		});
		return holder.get();
	}

	@Override
	public IInstance getInstance(String pInstanceId) {
		final String instId = Objects.requireNonNull(pInstanceId, "Instance Id");
		return instances.get(instId);
	}

	@Override
	public IInstance getDefaultInstance() {
		final Holder<IInstance> holder = new Holder<>();
		instances.forEach((id, inst) -> {
			if (inst.isDefault()) {
				if (holder.get() == null) {
					holder.set(inst);
				} else {
					throw new IllegalStateException("Multiple instances detected: "
							+ holder.get().getId() + ", and " + id);
				}
			}
		});
		return holder.get();
	}

	public void addInstance(IInstance pInstance) {
		final IInstance inst = Objects.requireNonNull(pInstance);
		final String id = Objects.requireNonNull(inst.getId(), "Instance Id");
		if (inst.isDefault()) {
			final IInstance defaultInstance = getDefaultInstance();
			if (defaultInstance != null) {
				throw new IllegalStateException("Unable to register instance id " + id
						+ " as default, because there already is a default instance with id "
						+ defaultInstance.getId());
			}
		}
		if (instances.putIfAbsent(id, inst) != null) {
			throw new IllegalArgumentException("Duplicate instance id: " + id);
		}
	}

	public void addLocalRepo(ILocalRepo pLocalRepo) {
		final ILocalRepo repo = Objects.requireNonNull(pLocalRepo);
		final String id = Objects.requireNonNull(repo.getId(), "Repository Id");
		if (repo.isDefault()) {
			final ILocalRepo defaultLocalRepo = getDefaultLocalRepo();
			if (defaultLocalRepo != null) {
				throw new IllegalStateException("Unable to register local repository id " + id
						+ " as default, because there already is a default local repository with id "
						+ defaultLocalRepo.getId());
			}
		}
		if (localRepos.putIfAbsent(id, repo) != null) {
			throw new IllegalArgumentException("Duplicate local repository id: " + id);
		}
	}
}
