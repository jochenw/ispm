package com.github.jochenw.ispm.core.impl;

import java.nio.file.Path;

import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.ispm.core.model.IInstance;

public class DefaultInstance implements IInstance {
	private final String id;
	private final Path baseDir;
	private final Path packagesDir, configDir, logDir;
	private final boolean isDefault;

	public DefaultInstance(String pId, Path pBaseDir, boolean pDefault) {
		this(Objects.requireNonNull(pId, "Id"),
			 Objects.requireNonNull(pBaseDir, "Base Directory"),
			 pBaseDir.resolve("packages"),
			 pBaseDir.resolve("config"),
			 pBaseDir.resolve("logs"),
			 pDefault);
	}

	public DefaultInstance(String pId, Path pBaseDir, Path pPackagesDir, Path pConfigDir, Path pLogDir,
			               boolean pDefault) {
		id = Objects.requireNonNull(pId, "Id");
		baseDir = Objects.requireNonNull(pBaseDir, "Base Directory");
		packagesDir = Objects.requireNonNull(pPackagesDir, "Packages Directory");
		configDir = Objects.requireNonNull(pConfigDir, "Config Directory");
		logDir = Objects.requireNonNull(pLogDir, "Log Directory");
		isDefault = pDefault;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public Path getBaseDir() {
		return baseDir;
	}

	@Override
	public Path getPackagesDir() {
		return packagesDir;
	}

	@Override
	public Path getConfigDir() {
		return configDir;
	}

	@Override
	public Path getLogDir() {
		return logDir;
	}

	@Override
	public boolean isDefault() {
		return isDefault;
	}
}
