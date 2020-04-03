package com.github.jochenw.ispm.core.impl;

import javax.inject.Inject;

import com.github.jochenw.afw.core.inject.LogInject;
import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.ispm.core.api.Data;
import com.github.jochenw.ispm.core.api.IExecutor;


public class DefaultExecutor implements IExecutor {
	private @LogInject ILog log;
	private @Inject ICompiler compiler;

	@Override
	public void execute(Data pData) {
		log.enteringf("execute", "Action=%s", pData.getAction());
		try {
			switch (pData.getAction()) {
			case "compile":
				compile(pData);
				break;
			case "list":
				list(pData);
				break;
			case "show":
				compile(pData);
				break;
			case "show-config":
				showConfig(pData);
				break;
			
			}
			log.exiting("execute");
		} catch (Throwable t) {
			log.error("execute", t);
		}
	}

	protected void compile(Data pData) {
		log.enteringf("compile", "WmHomeId=%s, Package=%s", pData.getIsInstanceId(), pData.getPackageName());
		compiler.compile(pData);
		log.exiting("compile");
	}

	protected void list(Data pData) {
		log.enteringf("list", "WmHomeId=%s", pData.getIsInstanceId());
		log.exiting("list");
	}

	protected void show(Data pData) {
		log.enteringf("show", "WmHomeId=%s, Package=%s", pData.getIsInstanceId(), pData.getPackageName());
		log.exiting("show");
	}

	protected void showConfig(Data pData) {
		log.entering("showConfig");
		log.exiting("showConfig");
	}

}
