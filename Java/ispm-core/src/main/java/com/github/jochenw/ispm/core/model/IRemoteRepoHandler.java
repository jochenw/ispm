package com.github.jochenw.ispm.core.model;

import java.nio.file.Path;

import com.github.jochenw.afw.core.util.Functions.FailableConsumer;

public interface IRemoteRepoHandler {
	public interface IProject {
		public String getId();
		public String getUrl();
	}
	public void forEach(IRemoteRepo pRemoteRepo, FailableConsumer<IProject,?> pConsumer);
	public String getProjectUrl(IRemoteRepo pRemoteRepo, String pProjectId);
	public void cloneProjectTo(IRemoteRepo pRemoteRepo, String pProjectId, String pUrl, Path pLocalProjectDir);	
}
