package com.github.jochenw.ispm.core.model;

import java.util.Map;

public interface IRemoteRepo {
	public String getId();
	public String getUrl();
	public String getHandler();
	public Map<String,String> getProperties();
}
