package com.github.jochenw.ispm.core.actions;

import java.nio.file.Path;

import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Functions.FailableBiConsumer;
import com.github.jochenw.afw.core.util.Functions.FailableConsumer;
import com.github.jochenw.ispm.core.config.IspmConfigWriter;
import com.github.jochenw.ispm.core.config.TIspmConfiguration;
import com.github.jochenw.ispm.core.svc.IspmApplication;


public abstract class AbstractConfigUpdatingAction extends AbstractAction {
	public Context run(Context pCtx, ILog pLog, String pMName, FailableBiConsumer<Context,TIspmConfiguration,?> pConfigUpdater) {
		final FailableConsumer<Context, ?> consumer = (ctx) -> {
			final IspmApplication ispmApplication = getComponentFactory().requireInstance(IspmApplication.class);
			final TIspmConfiguration tIspmConfiguration = ispmApplication.getTIspmConfiguration();
			try {
				pConfigUpdater.accept(ctx, tIspmConfiguration);
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		
			final Path configPath = ispmApplication.getTIspmConfigurationPath();
			IspmConfigWriter.write(tIspmConfiguration, configPath);
			ispmApplication.setIspmConfiguration(tIspmConfiguration, configPath);
		};
		return run(pCtx, pMName, pLog, consumer);
	}
}
