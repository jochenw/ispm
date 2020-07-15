package com.github.jochenw.ispm.core.plugins;

import java.nio.file.Path;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IRemoteRepositoryPlugin {
	public @Nonnull String getId();
	public @Nullable String getRemoteRepositoryUrl(Path pLocalRepoPath);
}
