package com.github.jochenw.ispm.core.config;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public interface IConfiguration {
	public static enum WmVersion {
		wm99, // webMethods 9.9
		wm912, // webMethods 9.12
		wm103 //webMethods 10.3
	};
	public interface IIsInstance {
		public String getId();
		public Path getPath();
		public Path getPackageDir();
		public Path getConfigDir();
		public Path getIsHomeDir();
		public Path getWmHomeDir();
		public String getWmVersion();
		public static String[] getVersions() {
			final WmVersion[] versionObjects = WmVersion.values();
			final List<String> versionStrings = Arrays.asList(versionObjects).stream().map((e) -> e.name()).collect(Collectors.toList());
			return versionStrings.toArray(new String[versionStrings.size()]);
		}
		public static WmVersion asVersion(String pVersion) {
			return WmVersion.valueOf(pVersion.toLowerCase());
		}
	}

	public List<IIsInstance> getIsInstances();

	public IIsInstance getDefaultInstance();
}
