package com.github.jochenw.ispm.core.actions;

import java.nio.file.Path;

import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Functions.FailableConsumer;
import com.github.jochenw.ispm.core.config.IspmConfigWriter;
import com.github.jochenw.ispm.core.config.TIspmConfiguration;
import com.github.jochenw.ispm.core.svc.IspmApplication;


public abstract class AbstractConfigUpdatingAction extends AbstractAction {
	public void run(FailableConsumer<TIspmConfiguration,?> pConfigUpdater) {
		final IspmApplication ispmApplication = getComponentFactory().requireInstance(IspmApplication.class);
		final TIspmConfiguration tIspmConfiguration = ispmApplication.getTIspmConfiguration();
		try {
			pConfigUpdater.accept(tIspmConfiguration);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
		
		final Path configPath = ispmApplication.getTIspmConfigurationPath();
		IspmConfigWriter.write(tIspmConfiguration, configPath);
		ispmApplication.setIspmConfiguration(tIspmConfiguration, configPath);
	}
}
