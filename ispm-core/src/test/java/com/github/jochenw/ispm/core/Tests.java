package com.github.jochenw.ispm.core;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.jochenw.afw.core.inject.ComponentFactoryBuilder.Module;
import com.github.jochenw.afw.core.log.ILogFactory;
import com.github.jochenw.afw.core.log.ILog.Level;
import com.github.jochenw.afw.core.log.simple.SimpleLogFactory;
import com.github.jochenw.afw.core.props.DefaultPropertyFactory;
import com.github.jochenw.afw.core.props.IPropertyFactory;
import com.github.jochenw.afw.core.util.Streams;
import com.github.jochenw.ispm.core.svc.IspmApplication;

public class Tests {
	public static IspmApplication getApplication() {
		return getApplication((Module[]) null);
	}
	public static IspmApplication getApplication(Module... pModules) {
		final List<Module> moduleList = new ArrayList<>();
		if (pModules != null) {
			moduleList.addAll(Arrays.asList(pModules));
		}
		final IspmApplication application = new IspmApplication(moduleList) {
			@Override
			protected ILogFactory newLogFactory() {
				final SimpleLogFactory slf = new SimpleLogFactory(System.out);
				slf.setLevel(Level.TRACE);
				return slf;
			}

			@Override
			protected IPropertyFactory newPropertyFactory() {
				final URL url = Thread.currentThread().getContextClassLoader().getResource("ispm-test.properties");
				assertNotNull(url);
				return new DefaultPropertyFactory(Streams.load(url));
			}
		};
		IspmApplication.setInstance(application);
		return application;
	}
}
