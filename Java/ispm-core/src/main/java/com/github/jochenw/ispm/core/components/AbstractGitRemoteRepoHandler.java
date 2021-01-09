package com.github.jochenw.ispm.core.components;

import java.nio.file.Path;

import javax.inject.Inject;

import com.github.jochenw.afw.core.inject.LogInject;
import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.ispm.core.model.IRemoteRepo;
import com.github.jochenw.ispm.core.model.IRemoteRepoHandler;

public abstract class AbstractGitRemoteRepoHandler implements IRemoteRepoHandler {
	private @LogInject ILog log;
	private @Inject IGitHandler gitHandler;

	@Override
	public void cloneProjectTo(IRemoteRepo pRemoteRepo, String pProjectId, String pUrl, Path pLocalProjectDir) {
		
	}
}
