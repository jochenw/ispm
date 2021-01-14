package com.github.jochenw.ispm.core.actions;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import com.github.jochenw.afw.core.util.Objects;

public class IoCatcher {
	private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
	private final ByteArrayOutputStream baes = new ByteArrayOutputStream();

	public ByteArrayOutputStream getStandardOut() {
		return baos;
	}
	public ByteArrayOutputStream getStandardErr() {
		return baes;
	}

	private static final ThreadLocal<IoCatcher> theIoCatcher = new ThreadLocal<>();
	public static IoCatcher reset() {
		final IoCatcher ioc = new IoCatcher();
		theIoCatcher.set(ioc);
		return ioc;
	}
	public static IoCatcher get() {
		return Objects.requireNonNull(theIoCatcher.get());
	}
	public static void clear() {
		theIoCatcher.set(null);
	}
}
