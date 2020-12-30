package com.github.jochenw.ispm.core.components;

import com.github.jochenw.ispm.core.data.Data;
import com.github.jochenw.ispm.core.model.IInstance;
import com.softwareag.util.IDataMap;
import com.wm.data.IData;

public interface IServiceInvocator {
	public IDataMap invoke(IInstance pInstance, String pService, IDataMap pInput);
	public default IDataMap invoke(IInstance pInstance, String pService, IData pInput) {
		return invoke(pInstance, pService, new IDataMap(pInput));
	}
	public default IDataMap invoke(IInstance pInstance, String pService, Object... pInput) {
		return invoke(pInstance, pService, Data.asIDataMap(pInput));
	}
	public boolean isInvocable(IInstance pInstance);
}
