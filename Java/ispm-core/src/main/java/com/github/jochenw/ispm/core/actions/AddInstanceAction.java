package com.github.jochenw.ispm.core.actions;

import java.nio.file.Path;
import java.util.Map;

import com.github.jochenw.afw.core.inject.LogInject;
import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.afw.core.util.Files;
import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.ispm.core.config.TIspmConfiguration.TInstance;


public class AddInstanceAction extends AbstractConfigUpdatingAction {
	private @LogInject ILog log;

	public void addInstance(String pId, Path pBaseDir, Map<String,String> pProperties,
			                String pWmHomeDir, String pPackagesDir, String pConfigDir,
			                String pLogsDir) {
		super.run(null, log, "addInstance", (ctx, tIspmConfiguration) -> {
			ctx.action("Adding instance " + pId + ", baseDir=" + pBaseDir);
			final String id = Objects.requireNonNull(pId, "Id");
			final Path baseDir = Objects.requireNonNull(pBaseDir);
			if (!Files.isDirectory(baseDir)) {
				throw new IllegalArgumentException("The base directory doesn't exist, or is not a directory: " + pBaseDir.toAbsolutePath());
			}
			ctx.debug(log, "addInstance", "Adding instance: id=" + id + ", baseDir=" + baseDir);
			final TInstance inst = new TInstance(null, id, baseDir.toString(), pWmHomeDir,
                    pPackagesDir, pConfigDir, pLogsDir);
			if (pProperties != null) {
				pProperties.forEach((k,v) -> {
					ctx.debug(log, "addInstance", "Instance property: " + k + "=" + v);
					inst.setProperty(k, v);
				});
			}
			tIspmConfiguration.addInstance(inst);
			ctx.action("Added instance " + id);
		});
	}
}
