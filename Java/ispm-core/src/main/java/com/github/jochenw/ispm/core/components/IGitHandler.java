package com.github.jochenw.ispm.core.components;

import java.nio.file.Path;
import java.util.List;

import com.github.jochenw.ispm.core.model.IRemoteRepo;

public interface IGitHandler {
	public void clone(IRemoteRepo pRemoteRepo, String pProjectUrl, Path pRepoDir);
	public List<String> getBranches(IRemoteRepo pRepo, Path pRepoDir);
	public void switchToBranch(IRemoteRepo pRepo, Path pRepoDir, String pBranchName);
	public void configure(IRemoteRepo pRemoteRepo, Path pRepoDir, String pGitProperty, String pValue);
}
