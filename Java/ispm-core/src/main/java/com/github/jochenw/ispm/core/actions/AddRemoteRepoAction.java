package com.github.jochenw.ispm.core.actions;

import java.util.Map;

import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.afw.core.util.Strings;
import com.github.jochenw.ispm.core.config.TIspmConfiguration.TRemoteRepo;
import com.github.jochenw.ispm.core.model.IRemoteRepoHandler;


public class AddRemoteRepoAction extends AbstractConfigUpdatingAction {
	public void addRemoteRepo(String pId, String pHandler, String pUrl, Map<String,String> pProperties) {
		final String id = Objects.requireNonNull(pId, "Id");
		String handler = Strings.notEmpty(pHandler, "default");
		final String url = Objects.requireNonNull(pUrl, "Url");
		if (getComponentFactory().getInstance(IRemoteRepoHandler.class, handler) == null) {
			throw new IllegalArgumentException("Invalid handler argument: " + handler + " (No such instance of IRemoteRepoHandler available.)");
		}

		super.run((tIspmConfiguration) -> {
			final TRemoteRepo remoteRepo = new TRemoteRepo(null, id, url, handler);
			if (pProperties != null) {
				pProperties.forEach((k,v) -> remoteRepo.setProperty(k, v));
			}
			tIspmConfiguration.addRemoteRepo(remoteRepo);
		});
	}
}
