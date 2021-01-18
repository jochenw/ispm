package com.github.jochenw.ispm.core.svc;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Strings;

public class Trace {
	private static final boolean tracing = false;

	public static void log(String pMsg) {
		if (tracing) {
			synchronized(Trace.class) {
				try (OutputStream out = new FileOutputStream("c:/tmp/trace.log", true)) {
					out.write((pMsg + System.lineSeparator()).getBytes(StandardCharsets.UTF_8));
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			}
		}
	}

	public static void log(String... pMsgs) {
		if (tracing) {
			final String msg = Strings.join("", pMsgs);
			log(pMsgs);
		}
	}

	public static void log(Throwable pTh) {
		if (tracing) {
			final String msg = Exceptions.toString(pTh);
			final StringBuilder sb = new StringBuilder();
			boolean first = true;
			try (StringReader sr = new StringReader(msg);
					BufferedReader br = new BufferedReader(sr)) {
				for (;;) {
					final String line = br.readLine();
					if (line == null) {
						break;
					} else {
						if (first) {
							first = false;
						} else {
							sb.append(System.lineSeparator());
						}
						sb.append(line);
					}
				}
				log(sb.toString());
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}
}
