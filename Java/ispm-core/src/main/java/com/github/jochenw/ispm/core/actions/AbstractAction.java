package com.github.jochenw.ispm.core.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import com.github.jochenw.afw.core.inject.IComponentFactory;
import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.afw.core.util.Objects;
import com.softwareag.util.IDataMap;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;

public abstract class AbstractAction {
	public static class Context {
		private List<String> actionMessages, logMessages;

		public void action(String pMessage) {
			final String msg = Objects.requireNonNull(pMessage);
			if (actionMessages == null) {
				actionMessages = new ArrayList<>();
			}
			actionMessages.add(msg);
		}

		public void debug(ILog pLog, String pMName, String pMessage) {
			final String mName = Objects.requireNonNull(pMName);
			final String msg = Objects.requireNonNull(pMessage);
			if (pLog != null) {
				pLog.debug(mName, msg);
			}
			if (logMessages != null) {
				logMessages = new ArrayList<>();
			}
			logMessages.add("DEBUG " + msg);
		}

		public void trace(ILog pLog, String pMName, String pMessage) {
			final String mName = Objects.requireNonNull(pMName);
			final String msg = Objects.requireNonNull(pMessage);
			if (pLog != null) {
				pLog.debug(mName, msg);
			}
			if (logMessages != null) {
				logMessages = new ArrayList<>();
			}
			logMessages.add("TRACE " + msg);
		}

		public List<String> getActionMessages() { return actionMessages; }
		public List<String> getLogMessages() { return logMessages; }

		public void apply(IDataMap pPipeline) {
			final IData result = IDataFactory.create();
			final IDataCursor crsr = result.getCursor();
			
		}
	}

	private @Inject IComponentFactory componentFactory;

	public IComponentFactory getComponentFactory() { return componentFactory; }
}
