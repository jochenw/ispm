package com.github.jochenw.ispm.core.actions;

import javax.inject.Inject;

import com.github.jochenw.afw.core.inject.LogInject;
import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.ispm.core.components.IServiceInvocator;
import com.github.jochenw.ispm.core.model.IInstance;
import com.softwareag.util.IDataMap;

public class ActivatePackageAction extends AbstractAction {
	private @Inject IServiceInvocator invocator;
	private @LogInject ILog log;

	protected boolean isPackageActive(IInstance pInstance, String pPkgName) {
		final IDataMap output = invocator.invoke(pInstance, "wm.server.packages:packageListInactive");
		final String[] inactivePackages = output.getAsStringArray("inactive");
		if (inactivePackages != null) {
			for (String inactivePkg : inactivePackages) {
				if (inactivePkg.equals(pPkgName)) {
					return false;
				}
			}
		}
		return true;
	}

	public Context activatePackage(Context pContext, IInstance pInstance, String pPkgName) {
		final String mName = "activePackage";
		return super.run(pContext, mName, log, (ctx) -> {
			ctx.action("Activating package " + pPkgName + "in instance " + pInstance.getId());
			log.entering(mName, pInstance.getId(), pPkgName);
			if (invocator.isInvocable(pInstance)) {
				ctx.debug(log, mName, "Instance is invocable: " + pInstance.getId());
				final boolean packageActive = isPackageActive(pInstance, pPkgName);
				if (packageActive) {
					ctx.info(log, mName, "Package is already active: " + pPkgName + ", reloading");
					invocator.invoke(pInstance, "wm.server.packages:packageReload", "package", pPkgName);
				} else {
					ctx.info(log, mName, "Package is not yet active, activating: " + pPkgName);
					invocator.invoke(pInstance, "wm.server.packages:packagesActivate", "package", pPkgName);
				}
			} else {
				final String msg = "Unable to acticate package " + pPkgName + " on instance " + pInstance.getId()
				+ " most likely due to insufficient configuration details.";
				throw new IllegalStateException(msg);
			}
			log.exiting(mName);
			ctx.action("Activated package " + pPkgName);
		});
	}
}
