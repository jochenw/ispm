package com.github.jochenw.ispm.core.svc;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Stack;

import com.wm.app.b2b.server.InvokeState;
import com.wm.lang.ns.NSService;

public class IspmApplicationSvc {
	private static final IspmApplicationSvc instance = new IspmApplicationSvc();

	public static IspmApplicationSvc getInstance() {
		return instance;
	}

	private IspmApplication application;

	public synchronized IspmApplication getApplication() {
		if (application == null) {
			Trace.log("Creating application");
			final Stack<NSService> stack = (Stack<NSService>) InvokeState.getCurrentState().getCallStack();
			if (stack.isEmpty()) {
				throw new IllegalStateException("The call stack is empty.");
			}
			final NSService svc = stack.lastElement();
			final String pkgName = svc.getPackage().getName();
			Path currentDir = Paths.get(".");
			Trace.log("Package name is " + pkgName + ", path is " + currentDir.toAbsolutePath());
			application = new IspmApplication(currentDir, pkgName);
		} else {
			Trace.log("Using existing application");
		}
		return application;
	}
}
