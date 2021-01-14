package com.github.jochenw.ispm.core.components;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import com.github.jochenw.afw.core.inject.LogInject;
import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.afw.core.props.IPropertyFactory;
import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.ispm.core.model.IRemoteRepo;
import com.github.jochenw.ispm.core.model.IRemoteRepoHandler;

public abstract class AbstractGitRemoteRepoHandler implements IRemoteRepoHandler {
	private @LogInject ILog log;
	private @Inject IGitHandler gitHandler;
	private @Inject IBranchSelector branchSelector;
	private @Inject IPropertyFactory propertyFactory;

	@Override
	public void cloneProjectTo(IRemoteRepo pRemoteRepo, String pProjectId, String pUrl, Path pLocalProjectDir) {
		final String mName = "cloneProjectTo";
		final IRemoteRepo remoteRepo = Objects.requireNonNull(pRemoteRepo, "Remote repository");
		log.entering(mName, remoteRepo.getId(), pProjectId, pUrl, pLocalProjectDir);
		gitHandler.clone(remoteRepo, pProjectId, pLocalProjectDir);
		final List<String> branches = gitHandler.getBranches(remoteRepo, pLocalProjectDir);
		final String latestBranch = branchSelector.getBranch(branches);
		if (latestBranch == null) {
			log.warn(mName, "Latest branch not found in branch list, keeping master.", branches);
		} else {
			log.info(mName, "Switching to latest branch", latestBranch);
			gitHandler.switchToBranch(remoteRepo, pLocalProjectDir, latestBranch);
		}
		for (String gitProperty : Arrays.asList("user.name", "user.email")) {
			final String key = "git." + gitProperty;
			String value = remoteRepo.getProperties().get(key);
			if (value == null) {
				value = propertyFactory.getPropertyValue(key);
			}
			if (value == null) {
				log.warn(mName, "No value found for git property, ignoring.", key);
			} else {
				log.info(mName, "Configuring git property ", gitProperty, value);
				gitHandler.configure(remoteRepo, pLocalProjectDir, gitProperty, value);
			}
		}
		log.exiting(mName);
	}
}
