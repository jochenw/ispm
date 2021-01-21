package com.github.jochenw.ispm.core.compile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;

import com.github.jochenw.afw.core.cli.Args;
import com.github.jochenw.afw.core.cli.Args.Context;
import com.github.jochenw.afw.core.util.Files;

/** A command line interface for the package compiler.
 */
public class Main {
	public static class Options {
		private String[] packageNames;
		private Path instanceDir;
	}

	public static Options parse(String[] pArgs) {
		final Options options = new Options();
		final Args.Listener listener = new Args.Listener() {
			@Override
			public void option(Context pCtx, String pName) {
				switch (pName) {
				  case "?":
				  case "h":
				  case "help":
					  throw usage(null);
				  case "id":
				  case "instDir":
				  case "instanceDirectory":
					  final Path p = pCtx.getSinglePathValue();
					  if (!Files.isDirectory(p)) {
						  throw usage("Invalid argument for option " + pName + ": " + p
								      + " (Does not exist, or is not a directory)");
					  }
					  options.instanceDir = p;
					  break;
				}
			}
		};
		options.packageNames = Args.parse(listener, pArgs);
		if (options.packageNames == null  ||  options.packageNames.length == 0) {
			throw usage("At least one package name must be given.");
		}
		return options;
	}

	public static void run(Options pOptions) {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final ByteArrayOutputStream baes = new ByteArrayOutputStream();

		final PackageCompiler pc = new PackageCompiler() {
			@Override
			protected void log(String pMsg) {
				System.out.println(pMsg);
			}

			@Override
			protected void warn(String pMsg) {
				System.err.println("Warning: " + pMsg);
			}

			@Override
			protected ByteArrayOutputStream newStdOutStream() {
				return baos;
			}

			@Override
			protected ByteArrayOutputStream newStdErrStream() {
				return baes;
			}
		};
		for (String pkg : pOptions.packageNames) {
			try {
				pc.log("Compiling package: " + pkg);
				System.setProperty("user.dir", pOptions.instanceDir.toAbsolutePath().toString());
				pc.compile(pOptions.instanceDir, pkg);
				baos.writeTo(System.out);
				System.out.flush();
				baes.writeTo(System.err);
				System.err.flush();
				pc.log("Compiled package: " + pkg);
			} catch (PackageCompiler.CompilerStatusException|IllegalStateException e) {
				try {
					baos.writeTo(System.out);
					baes.writeTo(System.err);
				} catch (IOException ex) {
					throw new UncheckedIOException(ex);
				}
				System.out.flush();
				System.err.flush();
				pc.warn("Failed to compile package " + pkg + ": " + e.getMessage());
				System.exit(1);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}

	public static void main(String[] pArgs) throws Exception {
		final Options options = parse(pArgs);
		run(options);
	}

	public static RuntimeException usage(String pMsg) {
		final PrintStream ps = System.err;
		if (pMsg != null) {
			ps.println(pMsg);
			ps.println();
		}
		ps.println("Usage: java " + Main.class.getName() + " <options> -- Package1 Package2 ...");
		ps.println("Possible options are:");
		ps.println("  -id|-instDir|-instanceDirectory <DIR> Sets the IS instance directory to <DIR>.");
		ps.println("                                        This option is required.");
		ps.println("  -?|-h|-help        Prints this help message, and exits with error status.");
		System.exit(1);
		return null;
	}
}
