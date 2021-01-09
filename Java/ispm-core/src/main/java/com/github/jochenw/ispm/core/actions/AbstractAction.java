package com.github.jochenw.ispm.core.actions;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.github.jochenw.afw.core.inject.IComponentFactory;
import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Functions.FailableConsumer;
import com.github.jochenw.afw.core.util.Objects;
import com.softwareag.util.IDataMap;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;

public abstract class AbstractAction {
	public static class Context {
		private List<String> actionMessages, logMessages;
		private Throwable error;
		private String standardOutput, errorOutput;

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
			if (logMessages == null) {
				logMessages = new ArrayList<>();
			}
			logMessages.add("DEBUG " + msg);
		}

		public void info(ILog pLog, String pMName, String pMessage) {
			final String mName = Objects.requireNonNull(pMName);
			final String msg = Objects.requireNonNull(pMessage);
			if (pLog != null) {
				pLog.info(mName, msg);
			}
			if (logMessages == null) {
				logMessages = new ArrayList<>();
			}
			logMessages.add("INFO  " + msg);
		}

		public void trace(ILog pLog, String pMName, String pMessage) {
			final String mName = Objects.requireNonNull(pMName);
			final String msg = Objects.requireNonNull(pMessage);
			if (pLog != null) {
				pLog.debug(mName, msg);
			}
			if (logMessages == null) {
				logMessages = new ArrayList<>();
			}
			logMessages.add("TRACE " + msg);
		}

		public void setError(Throwable pTh) {
			error = pTh;
		}
		public void setStandardOutput(String pStandardOutput) {
			standardOutput = pStandardOutput;
		}
		public void setErrorOutput(String pErrorOutput) {
			errorOutput = pErrorOutput;
		}
		public List<String> getActionMessages() { return actionMessages; }
		public List<String> getLogMessages() { return logMessages; }

		public void apply(IDataMap pPipeline) {
			final IData result = IDataFactory.create();
			final IDataCursor crsr = result.getCursor();
			if (error == null) {
				IDataUtil.put(crsr, "success", "true");
			} else {
				IDataUtil.put(crsr, "success", "false");
				IDataUtil.put(crsr, "errorMessage", error.getMessage());
				IDataUtil.put(crsr, "errorType", error.getClass().getName());
				IDataUtil.put(crsr, "errorDetails", Exceptions.toString(error));
			}
			if (standardOutput != null) {
				IDataUtil.put(crsr, "standardOutput", standardOutput);
			}
			if (errorOutput != null) {
				IDataUtil.put(crsr, "errorOutput", errorOutput);
			}
			if (actionMessages != null  &&  !actionMessages.isEmpty()) {
				IDataUtil.put(crsr, "actionMessages", actionMessages.toArray(new String[actionMessages.size()]));
			}
			if (logMessages != null  &&  !logMessages.isEmpty()) {
				IDataUtil.put(crsr, "logMessages", logMessages.toArray(new String[logMessages.size()]));
			}
			pPipeline.put("actionResult", result);
		}

		public Throwable getError() {
			return error;
		}
	}

	protected Context run(Context pCtx, String pMName, ILog pLog, FailableConsumer<Context,?> pConsumer) {
		final Context ctx = Objects.notNull(pCtx, () -> new Context());
		try {
			pConsumer.accept(ctx);
		} catch (Throwable t) {
			ctx.setError(t);
			pLog.error(pMName, t);
		}
		return ctx;
	}

	private @Inject IComponentFactory componentFactory;

	public IComponentFactory getComponentFactory() { return componentFactory; }
}
