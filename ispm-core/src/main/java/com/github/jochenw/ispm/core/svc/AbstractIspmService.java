package com.github.jochenw.ispm.core.svc;

import javax.inject.Inject;

import com.github.jochenw.afw.core.inject.IComponentFactory;
import com.github.jochenw.ispm.core.model.IConfiguration;
import com.github.jochenw.ispm.core.model.IInstance;
import com.softwareag.util.IDataMap;


public abstract class AbstractIspmService implements IsService {
	private @Inject IComponentFactory componentFactory;
	private @Inject IspmApplication ispmApplication;

	public IComponentFactory getComponentFactory() {
		return componentFactory;
	}

	@Override
	public Object[] run(IDataMap pMap) {
		// TODO Auto-generated method stub
		return null;
	}

	public IConfiguration getConfiguration() {
		return ispmApplication.getConfiguration();
	}

	public IInstance getLocalInstance() {
		return ispmApplication.getLocalInstance();
	}

	protected Object[] noResult() {
		return null;
	}

	protected Object[] result(Object... pValues) {
		return pValues;
	}
}
