package com.github.jochenw.ispm.core.actions;

import java.nio.file.Path;
import java.util.Map;

import com.github.jochenw.afw.core.util.Files;
import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.ispm.core.config.TIspmConfiguration.TInstance;


public class AddInstanceAction extends AbstractConfigUpdatingAction {
	public void addInstance(String pId, Path pBaseDir, Map<String,String> pProperties,
			                String pWmHomeDir, String pPackagesDir, String pConfigDir,
			                String pLogsDir) {
		final String id = Objects.requireNonNull(pId, "Id");
		final Path baseDir = Objects.requireNonNull(pBaseDir);
		if (!Files.isDirectory(baseDir)) {
			throw new IllegalArgumentException("The base directory doesn't exist, or is not a directory: " + pBaseDir.toAbsolutePath());
		}
		super.run((tIspmConfiguration) -> {
			final TInstance inst = new TInstance(null, id, baseDir.toString(), pWmHomeDir,
                    pPackagesDir, pConfigDir, pLogsDir);
			if (pProperties != null) {
				pProperties.forEach((k,v) -> inst.setProperty(k, v));
			}
			tIspmConfiguration.addInstance(inst);
		});
	}
}
