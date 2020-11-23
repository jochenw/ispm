package com.github.jochenw.ispm.core.model;

import java.nio.file.Path;

public interface ILocalRepo {
	public boolean isDefault();
	public String getId();
	public String getLayout();
	public Path getBaseDir();
}
