package com.github.jochenw.ispm.core.actions;

import javax.inject.Inject;

import com.github.jochenw.afw.core.inject.LogInject;
import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.ispm.core.components.IServiceInvocator;
import com.github.jochenw.ispm.core.data.Data;
import com.github.jochenw.ispm.core.model.IInstance;
import com.github.jochenw.ispm.core.model.IspmConfiguration;

public class PackageReloadAction extends AbstractAction {
	private @LogInject ILog log;
	private @Inject IspmConfiguration ispmConfiguration;
	private @Inject IServiceInvocator invocator;

	public Context reload(String[] pPackages) {
		final String[] packages = Objects.requireAllNonNull(Objects.requireNonNull(pPackages, "Packages"), "Package");
		final IInstance instance = ispmConfiguration.requireInstance("local");
		final String mName = "reload";
		return run(null, mName, log, (ctx) -> {
			for (String pkg : packages) {
				reloadPkg(ctx, instance, pkg);
			}
		});
	}

	protected void reloadPkg(Context pCtx, IInstance pInstance, String pPackage) {
		final String mName = "reloadPkg";
		pCtx.action("Reloading package: " + pPackage);
		pCtx.debug(log, mName, "Reloading package: " + pPackage);
		invocator.invoke(pInstance, "wx.ispm.impl.util.admin:runServiceWithDependenciesDisabled",
				         "service", "wm.server.packages:packageReload",
				         "packageName", pPackage,
				         "inputPipeline", Data.asIData("package", pPackage));
		pCtx.debug(log, mName, "Reloaded package: " + pPackage);
		pCtx.action("Reloaded package: " + pPackage);
	}
}
