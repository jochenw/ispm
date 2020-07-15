package com.github.jochenw.ispm.core;

import com.github.jochenw.afw.core.inject.IComponentFactory;
import com.github.jochenw.afw.core.inject.simple.SimpleComponentFactoryBuilder;
import com.github.jochenw.afw.core.log.app.IAppLog;
import com.github.jochenw.ispm.core.config.IConfiguration;
import com.github.jochenw.ispm.core.plugins.IPluginRuntime;

public class MainPluginRuntime implements IPluginRuntime {
	private IComponentFactory componentFactory;

	public synchronized IComponentFactory getComponentFactory(IAppLog pAppLog, IConfiguration pConfiguration) {
		if (componentFactory == null) {
			componentFactory = newComponentFactory(pAppLog, pConfiguration);
		}
		return componentFactory;
	}

	@Override
	public synchronized IComponentFactory getComponentFactory() {
		if (componentFactory == null) {
			throw new IllegalStateException("Not initialized.");
		}
		return componentFactory;
	}
	
	protected IComponentFactory newComponentFactory(IAppLog pAppLog, IConfiguration pConfiguration) {
		final SimpleComponentFactoryBuilder scfb = new SimpleComponentFactoryBuilder();
		scfb.module((b) -> {
			b.bind(IAppLog.class).toInstance(pAppLog);
			b.bind(IConfiguration.class).toInstance(pConfiguration);
			b.bind(IPluginRuntime.class).toInstance(this);
		});
		return scfb.build();
	}
}
