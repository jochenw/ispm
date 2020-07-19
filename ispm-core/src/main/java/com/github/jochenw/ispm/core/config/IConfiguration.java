package com.github.jochenw.ispm.core.config;

import java.nio.file.Path;
import java.util.List;

public interface IConfiguration {
	public interface IIsInstance {
		public String getId();
		public Path getPath();
		public Path getPackageDir();
		public Path getConfigDir();
		public Path getIsHomeDir();
		public Path getWmHomeDir();
		public String getWmVersion();
	}

	public List<IIsInstance> getIsInstances();

	public IIsInstance getDefaultInstance();
}
