package com.github.jochenw.ispm.core.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.jochenw.ispm.core.actions.AbstractAction.Context;
import com.github.jochenw.ispm.core.components.IServiceInvocator;
import com.github.jochenw.ispm.core.model.IInstance;
import com.github.jochenw.ispm.core.model.ILocalRepo;
import com.github.jochenw.ispm.core.model.IspmConfiguration;
import com.github.jochenw.ispm.core.svc.IspmApplication;
import com.github.jochenw.ispm.core.svc.IspmApplicationTest;
import com.softwareag.util.IDataMap;
import com.wm.data.IData;

class ImportFromLocalRepoActionTest {
	private static class MockServiceInvocator implements IServiceInvocator {
		private static class Invocation {
			private final IInstance iInstance;
			private final String service;
			private final IDataMap input;

			Invocation(IInstance pInstance, String pService, IDataMap pInput) {
				iInstance = pInstance;
				service = pService;
				input = clone(pInput);
			}

			public IInstance getInstance() { return iInstance; }
			public String getService() { return service; }
			public IDataMap getInput() { return input; }
			private IDataMap clone(IDataMap pInput) {
				final IDataMap map = new IDataMap();
				for (Map.Entry<String,Object> en : pInput.entrySet()) {
					final String key = en.getKey();
					final Object value = en.getValue();
					final Object val;
					if (value == null) {
						val = null;
					} else if (value instanceof String
							   ||  value instanceof Boolean
							   ||  value instanceof Number) {
						val = value;
					} else if (value instanceof IData) {
						val = clone(new IDataMap((IData) value)).getIData();
					} else {
						throw new IllegalStateException("Invalid value type: " + value.getClass().getName());
					}
					map.put(key, val);
				}
				return map;
			}
		}

		private final List<Invocation> invocations = new ArrayList<>();

		@Override
		public IDataMap invoke(IInstance pInstance, String pService, IDataMap pInput) {
			invocations.add(new Invocation(pInstance, pService, pInput));
			return pInput;
		}

		@Override
		public boolean isInvocable(IInstance pInstance) {
			return true;
		}
		
	}

	@Test
	void testImport() {
		final MockServiceInvocator serviceInvocator = new MockServiceInvocator();
		final IspmApplication ispmApp = IspmApplicationTest.setup(getClass(), (b) -> {
			b.bind(IServiceInvocator.class).toInstance(serviceInvocator);
		});
		final ImportFromLocalRepoAction action = ispmApp.getComponentFactory().requireInstance(ImportFromLocalRepoAction.class);
		assertNotNull(action);
		final IspmConfiguration configuration = ispmApp.getComponentFactory().requireInstance(IspmConfiguration.class);
		assertNotNull(configuration);
		final IInstance instance = configuration.requireInstance("local");
		assertNotNull(instance);
		final ILocalRepo localRepo = configuration.requireLocalRepo("test");
		assertNotNull(localRepo);
		assertTrue(serviceInvocator.invocations.isEmpty());
		final Context ctx = action.importFromLocalRepo(null, instance, localRepo, "WxIspm");
		assertNotNull(ctx);
		final Throwable t = ctx.getError();
		assertNotNull(t);
		assertTrue(t instanceof IllegalStateException);
		assertEquals("Package WxIspm is already present in instance local, and not overwritable.", t.getMessage());
		assertTrue(serviceInvocator.invocations.isEmpty());
	}

}
