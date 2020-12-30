package com.github.jochenw.ispm.core.model;

import java.nio.file.Path;
import java.util.Map;

public interface IInstance {
	public String getId();
	public Map<String,String> getProperties();
	public Path getBaseDir();
	public Path getWmHomeDir();
	public Path getPackagesDir();
	public Path getConfigDir();
	public Path getLogsDir();
	public boolean isLocal();
}
