package com.github.jochenw.ispm.core.impl;

import java.nio.file.Path;

import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.ispm.core.model.ILocalRepo;

public class DefaultLocalRepo implements ILocalRepo {
	private final boolean isDefault;
	private final String id, layout;
	private final Path baseDir;

	public DefaultLocalRepo(String pId, String pLayout, Path pBaseDir, boolean pDefault) {
		isDefault = pDefault;
		id = Objects.requireNonNull(pId, "Id");
		layout = Objects.requireNonNull(pLayout, "Layout");
		baseDir = Objects.requireNonNull(pBaseDir, "Base Directory");
	}

	@Override
	public boolean isDefault() {
		return isDefault;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getLayout() {
		return layout;
	}

	@Override
	public Path getBaseDir() {
		return baseDir;
	}

}
