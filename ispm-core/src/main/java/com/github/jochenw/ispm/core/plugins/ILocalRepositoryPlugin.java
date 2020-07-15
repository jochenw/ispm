package com.github.jochenw.ispm.core.plugins;

import java.nio.file.Path;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ILocalRepositoryPlugin {
	public @Nonnull String getId();
	public @Nullable Path getLocalRepositoryPathOf(@Nonnull Path pP);
}
