package com.github.jochenw.ispm.core.actions;

import java.nio.file.Path;

import com.github.jochenw.afw.core.util.Files;
import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.ispm.core.config.IspmConfigParser;
import com.github.jochenw.ispm.core.config.IspmConfigWriter;
import com.github.jochenw.ispm.core.config.TIspmConfiguration;
import com.github.jochenw.ispm.core.config.TIspmConfiguration.TLocalRepo;
import com.github.jochenw.ispm.core.model.ILocalRepoLayout;
import com.github.jochenw.ispm.core.svc.IspmApplication;

public class AddLocalRepoAction extends AbstractAction {
	public void addLocalRepo(String pId, String pLayout, Path pDir) {
		final String id = Objects.requireNonNull(pId, "Id");
		final String layout = Objects.requireNonNull(pLayout, "Layout");
		final Path path = Objects.requireNonNull(pDir, "Dir");
		if (getComponentFactory().getInstance(ILocalRepoLayout.class, layout) == null) {
			throw new IllegalArgumentException("Invalid layout argument: " + layout + " (No such instance of ILocalRepoLayout available.)");
		}
		if (!Files.isDirectory(path)) {
			throw new IllegalArgumentException("Invalid dir argument: " + path + " (No such directory.)");
		}

		final IspmApplication ispmApplication = getComponentFactory().requireInstance(IspmApplication.class);
		final TIspmConfiguration tIspmConfiguration = ispmApplication.getTIspmConfiguration();
		tIspmConfiguration.addLocalRepo(new TLocalRepo(null, id, path.toString(), layout));
		final Path configPath = ispmApplication.getTIspmConfigurationPath();
		IspmConfigWriter.write(tIspmConfiguration, configPath);
		ispmApplication.setIspmConfiguration(tIspmConfiguration, configPath);
	}
}
