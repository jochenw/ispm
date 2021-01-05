package com.github.jochenw.ispm.core.model;

import java.nio.file.Path;

import com.github.jochenw.afw.core.util.Functions.FailableConsumer;

public interface IRemoteRepoHandler {
	public interface IProject {
		public String getId();
		public String getUrl();
	}
	public void forEach(IRemoteRepo pRemoteRepo, FailableConsumer<IProject,?> pConsumer);
	public void clone(IRemoteRepo pRemoteRepo, String pProjectId, ILocalRepo pLocalRepo, Path pTargetDir);
}
