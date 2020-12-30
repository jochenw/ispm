package com.github.jochenw.ispm.core.model;

import java.nio.file.Path;
import java.util.Map;

public interface ILocalRepo {
	public String getId();
	public Path getDir();
	public String getLayout();
	public Map<String,String> getProperties();
}
