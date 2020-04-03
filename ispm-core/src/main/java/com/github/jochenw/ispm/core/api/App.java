package com.github.jochenw.ispm.core.api;

import java.net.URL;

import com.github.jochenw.afw.core.inject.ComponentFactoryBuilder.Module;
import com.github.jochenw.afw.core.inject.IComponentFactory;
import com.github.jochenw.afw.core.inject.simple.SimpleComponentFactoryBuilder;
import com.github.jochenw.afw.core.log.ILog.Level;
import com.github.jochenw.afw.core.log.ILogFactory;
import com.github.jochenw.afw.core.log.simple.SimpleLogFactory;
import com.github.jochenw.afw.core.props.DefaultPropertyFactory;
import com.github.jochenw.afw.core.props.IPropertyFactory;
import com.github.jochenw.ispm.core.impl.DefaultCompiler;
import com.github.jochenw.ispm.core.impl.DefaultExecutor;
import com.github.jochenw.ispm.core.impl.ICompiler;

public class App {
	private ILogFactory logFactory;
	private IPropertyFactory propertyFactory;
	private IComponentFactory componentFactory;

	public synchronized IComponentFactory getComponentFactory() {
		if (componentFactory == null) {
			componentFactory = newComponentFactory();
		}
		return componentFactory;
	}

	public synchronized void setComponentFactory(IComponentFactory pComponentFactory) {
		componentFactory = pComponentFactory;
	}

	protected IComponentFactory newComponentFactory() {
		return new SimpleComponentFactoryBuilder().module(newModule()).build();
	}

	protected Module newModule() {
		final App app = this;
		return (b) -> {
			b.bind(ILogFactory.class).toInstance(getLogFactory());
			b.bind(IPropertyFactory.class).toInstance(getPropertyFactory());
			b.bind(App.class).toInstance(app);
			b.bind(IExecutor.class).to(DefaultExecutor.class);
			b.bind(ICompiler.class).to(DefaultCompiler.class);
		};
	}
	
	public synchronized ILogFactory getLogFactory() {
		if (logFactory == null) {
			logFactory = newLogFactory();
		}
		return logFactory;
	}

	public synchronized void setLogFactory(ILogFactory pLogFactory) {
		logFactory = pLogFactory;
	}
	
	protected ILogFactory newLogFactory() {
		final SimpleLogFactory slf = new SimpleLogFactory();
		slf.setLevel(Level.INFO);
		return slf;
	}

	public synchronized IPropertyFactory getPropertyFactory() {
		if (propertyFactory == null) {
			propertyFactory = newPropertyFactory();
		}
		return propertyFactory;
	}

	public synchronized void setPropertyFactory(IPropertyFactory pPropertyFactory) {
		propertyFactory = pPropertyFactory;
	}

	protected IPropertyFactory newPropertyFactory() {
		return new DefaultPropertyFactory(getInstancePropertyUrl(), getFactoryPropertyUrl());
	}

	protected URL getInstancePropertyUrl() {
		return null;
	}

	protected URL getFactoryPropertyUrl() {
		String factoryPropertyUri = "/ispm-core-factory.properties";
		final URL factoryPropertyUrl = Thread.currentThread().getContextClassLoader().getResource(factoryPropertyUri);
		if (factoryPropertyUrl == null) {
			throw new NullPointerException("Resource not found: " + factoryPropertyUri);
		}
		return factoryPropertyUrl;
	}
}
