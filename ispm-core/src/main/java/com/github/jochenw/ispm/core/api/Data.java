package com.github.jochenw.ispm.core.api;

public class Data {
	private String action, packageName, isInstanceId;

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getIsInstanceId() {
		return isInstanceId;
	}

	public void setIsInstanceId(String wmHomeId) {
		this.isInstanceId = wmHomeId;
	}
}