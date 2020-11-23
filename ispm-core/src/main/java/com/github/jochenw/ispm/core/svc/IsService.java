package com.github.jochenw.ispm.core.svc;

import java.util.Objects;

import com.github.jochenw.afw.core.util.Exceptions;
import com.softwareag.util.IDataMap;
import com.wm.app.b2b.server.mon.ISService;
import com.wm.data.IData;

public interface IsService {
	public Object[] run(IDataMap pMap);

	public default void run(IData pPipeline) {
		final IDataMap map = new IDataMap(Objects.requireNonNull(pPipeline));
		final Object[] output = run(map);
		if (output != null) {
			for (int i = 0;  i < output.length;  i += 2) {
				String name = (String) output[i];
				Object value = output[i+1];
				if (value != null) {
					map.put(name, value);
				}
			}
		}
	}

	public static void run(IData pPipeline, Class<? extends ISService> pType) {
		final IsService svc;
		try {
			svc = (IsService) pType.getConstructor().newInstance();
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
		IspmApplication.getInstance().getComponentFactory().init(svc);
		svc.run(pPipeline);
	}
}
