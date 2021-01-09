package com.github.jochenw.ispm.core.actions;

import java.util.Map;

import com.github.jochenw.afw.core.inject.LogInject;
import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.afw.core.util.Strings;
import com.github.jochenw.ispm.core.config.TIspmConfiguration.TRemoteRepo;
import com.github.jochenw.ispm.core.model.IRemoteRepoHandler;


public class AddRemoteRepoAction extends AbstractConfigUpdatingAction {
	private @LogInject ILog log;

	public Context addRemoteRepo(String pId, String pHandler, String pUrl, Map<String,String> pProperties) {
		final String mName = "addRemoteRepo";
		return super.run(null, log, mName, (ctx, tIspmConfiguration) -> {
			ctx.action("Adding remote repository: id=" + pId + ", handler=" + pHandler + ", url=" + pUrl);
			ctx.debug(log, mName, "Adding remote repository: id=" + pId + ", handler=" + pHandler + ", url=" + pUrl);
			final String id = Objects.requireNonNull(pId, "Id");
			String handler = Strings.notEmpty(pHandler, "default");
			final String url = Objects.requireNonNull(pUrl, "Url");
			if (getComponentFactory().getInstance(IRemoteRepoHandler.class, handler) == null) {
				throw new IllegalArgumentException("Invalid handler argument: " + handler + " (No such instance of IRemoteRepoHandler available.)");
			}

			final TRemoteRepo remoteRepo = new TRemoteRepo(null, id, url, handler);
			if (pProperties != null) {
				pProperties.forEach((k,v) -> remoteRepo.setProperty(k, v));
			}
			tIspmConfiguration.addRemoteRepo(remoteRepo);
			ctx.debug(log, mName, "Added remote repository " + pId);
			ctx.action("Added remote repository");
		});
	}
}
