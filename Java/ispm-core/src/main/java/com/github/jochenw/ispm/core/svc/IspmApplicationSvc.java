package com.github.jochenw.ispm.core.svc;

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
			final Stack<NSService> stack = (Stack<NSService>) InvokeState.getCurrentState().getCallStack();
			if (stack.isEmpty()) {
				throw new IllegalStateException("The call stack is empty.");
			}
			final NSService svc = stack.lastElement();
			final String pkgName = svc.getPackage().getName();
			application = new IspmApplication(Paths.get("."), pkgName);
		}
		return application;
	}
}
