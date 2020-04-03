package com.github.jochenw.ispm.core.api;

import java.nio.file.Path;

import javax.annotation.Nullable;

public interface IIspmConfiguration {
	public interface IIsInstance {
		public Path getPath();
		public Path getPackagesDir();
	}
	public @Nullable IIsInstance getIsInstance(@Nullable String pId);
}
