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

	public void activatePackage(IInstance pInstance, String pPkgName) {
		final String mName = "activePackage";
		log.entering(mName, pInstance.getId(), pPkgName);
		if (invocator.isInvocable(pInstance)) {
			log.debug(mName, "Instance is invocable: " + pInstance.getId());
			final boolean packageActive = isPackageActive(pInstance, pPkgName);
			if (packageActive) {
				log.debug(mName, "Package is already active: " + pPkgName);
				log.info(mName, "Reloading package: " + pPkgName);
				invocator.invoke(pInstance, "wm.server.packages:packageReload", "package", pPkgName);
			} else {
				log.info(mName, "Activating package: " + pPkgName);
				invocator.invoke(pInstance, "wm.server.packages:packagesActivate", "package", pPkgName);
			}
		} else {
			final String msg = "Unable to acticate package " + pPkgName + " on instance " + pInstance.getId()
							+ " most likely due to insufficient configuration details.";
			log.error(mName, msg);
			throw new IllegalStateException(msg);
		}
		log.exiting(mName);
	}
}
