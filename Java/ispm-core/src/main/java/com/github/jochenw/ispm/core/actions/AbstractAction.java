package com.github.jochenw.ispm.core.actions;

import javax.inject.Inject;

import com.github.jochenw.afw.core.inject.IComponentFactory;

public abstract class AbstractAction {
	private @Inject IComponentFactory componentFactory;

	public IComponentFactory getComponentFactory() { return componentFactory; }
}
