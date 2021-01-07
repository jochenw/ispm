package com.github.jochenw.ispm.core.actions;

import java.nio.file.Path;
import java.util.Map;

import com.github.jochenw.afw.core.util.Files;
import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.ispm.core.config.TIspmConfiguration.TLocalRepo;
import com.github.jochenw.ispm.core.model.ILocalRepoLayout;


public class AddLocalRepoAction extends AbstractConfigUpdatingAction {
	public void addLocalRepo(String pId, String pLayout, Path pDir, Map<String,String> pProperties) {
		final String id = Objects.requireNonNull(pId, "Id");
		final String layout = Objects.requireNonNull(pLayout, "Layout");
		final Path path = Objects.requireNonNull(pDir, "Dir");
		if (getComponentFactory().getInstance(ILocalRepoLayout.class, layout) == null) {
			throw new IllegalArgumentException("Invalid layout argument: " + layout + " (No such instance of ILocalRepoLayout available.)");
		}
		if (!Files.isDirectory(path)) {
			throw new IllegalArgumentException("Invalid dir argument: " + path + " (No such directory.)");
		}
		super.run((tIspmConfiguration) -> {
			final TLocalRepo localRepo = new TLocalRepo(null, id, path.toString(), layout);
			if (pProperties != null) {
				pProperties.forEach((k,v) -> localRepo.setProperty(k, v));
			}
			tIspmConfiguration.addLocalRepo(localRepo);
		});
	}
}
