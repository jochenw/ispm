package com.github.jochenw.ispm.core.components;

import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.ispm.core.model.IInstance;
import com.github.jochenw.ispm.core.svc.NdName;
import com.softwareag.util.IDataMap;
import com.wm.app.b2b.server.Service;
import com.wm.data.IData;

public class DefaultServiceInvocator implements IServiceInvocator {
	@Override
	public IDataMap invoke(IInstance pInstance, String pService, IDataMap pInput) {
		final NdName nodeName = NdName.of(pService);
		IData output;
		try {
			output = Service.doInvoke(nodeName.getNamespace(), nodeName.getLocalName(), pInput.getIData());
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
		return new IDataMap(output);
	}

	@Override
	public boolean isInvocable(IInstance pInstance) {
		return "local".equals(pInstance.getId());
	}
}
