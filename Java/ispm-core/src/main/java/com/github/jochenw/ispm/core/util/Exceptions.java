package com.github.jochenw.ispm.core.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Objects;

public class Exceptions {
	public static RuntimeException show(Throwable pTh) {
		final Throwable th = Objects.requireNonNull(pTh, "Throwable");
		if (th instanceof RuntimeException) {
			throw (RuntimeException) th;
		} else if (th instanceof Error) {
			throw (Error) th;
		} else if (th instanceof IOException) {
			throw new UncheckedIOException((IOException) th);
		} else {
			throw new UndeclaredThrowableException(th);
		}
	}

	public static <E extends Throwable> RuntimeException show(Throwable pTh, Class<E> pType) throws E {
		final Throwable th = Objects.requireNonNull(pTh, "Throwable");
		if (th instanceof RuntimeException) {
			throw (RuntimeException) th;
		} else if (th instanceof Error) {
			throw (Error) th;
		} else if (pType.isAssignableFrom(th.getClass())) {
			throw pType.cast(th);
		} else if (th instanceof IOException) {
			throw new UncheckedIOException((IOException) th);
		} else {
			throw new UndeclaredThrowableException(th);
		}
	}
}
