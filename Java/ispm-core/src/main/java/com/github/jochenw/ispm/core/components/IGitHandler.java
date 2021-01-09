package com.github.jochenw.ispm.core.components;

import java.nio.file.Path;
import java.util.List;

import com.github.jochenw.ispm.core.model.IRemoteRepo;

public interface IGitHandler {
	public void clone(IRemoteRepo pRemoteRepo, String pProjectUrl, Path pProjectDir);
	List<String> getBranches(IRemoteRepo pRepo, Path pProjectDir);
	void switchToBranch(IRemoteRepo pRepo, Path pProjectDir, String pBranchName);
}
