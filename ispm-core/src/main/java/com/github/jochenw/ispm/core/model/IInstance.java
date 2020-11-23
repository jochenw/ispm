package com.github.jochenw.ispm.core.model;

import java.nio.file.Path;

public interface IInstance {
	public String getId();
	public Path getBaseDir();
	public Path getPackagesDir();
	public Path getConfigDir();
	public Path getLogDir();
	public boolean isDefault();
}
