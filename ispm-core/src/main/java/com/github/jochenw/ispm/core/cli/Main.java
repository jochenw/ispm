package com.github.jochenw.ispm.core.cli;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.github.jochenw.afw.core.cli.Options;
import com.github.jochenw.afw.core.util.Holder;
import com.github.jochenw.afw.core.util.MutableBoolean;
import com.github.jochenw.ispm.core.api.Data;


public class Main {
	public interface Handler {
		void accept(String pOption, Supplier<String> pValue);
		RuntimeException error(String pMsg);
	}

	public static void process(String[] pArgs, Handler pHandler) {
		List<String> args = new ArrayList<>(Arrays.asList(pArgs));
		final Holder<String> valueHolder = new Holder<String>();
		final MutableBoolean valueGiven = new MutableBoolean();
		while (args.isEmpty()) {
			final String arg = args.remove(0);
			String option;
			if (arg.startsWith("--")) {
				option = arg.substring(2);
			} else if (arg.startsWith("-")  ||  arg.startsWith("/")) {
				option = arg.substring(1);
			} else {
				throw pHandler.error("Invalid option: " + arg);
			}
			final int offset = option.indexOf('=');
			if (offset == -1) {
				valueHolder.set(null);
			} else if (offset == 0) {
				throw pHandler.error("Invalid option: " + arg);
			} else {
				valueHolder.set(option.substring(offset+1));
				option = option.substring(0, offset);
			}
			final String opt = option;
			valueGiven.setValue(false);
			final Supplier<String> valueSupplier = () -> {
				if (valueGiven.isSet()) {
					throw new IllegalStateException("Only one value may be requested.");
				}
				valueGiven.set();
				String v = valueHolder.get();
				if (v == null) {
					if (!args.isEmpty()) {
						v = args.remove(0);
					} else {
						throw pHandler.error("Option " + opt + " requires an argument.");
					}
				}
				return v;
			};
			pHandler.accept(opt, valueSupplier);
		}
	}

	public static void main(String[] pArgs) {
		final Data data = new Data();
		final Consumer<String> actionConsumer = (pAction) -> {
			if (data.getAction() != null) {
				throw usage("The options -compile, -list, -show, and -show-config are mutually exclusive.");
			}
			data.setAction(pAction);
		};
		final Handler handler = new Handler() {
			@Override
			public void accept(String pOption, Supplier<String> pValue) {
				switch (pOption) {
				case "compile":
					actionConsumer.accept("compile");
					data.setPackageName(pValue.get());
					break;
				case "list":
					actionConsumer.accept("list");
					break;
				case "show":
					actionConsumer.accept("show");
					data.setPackageName(pValue.get());
					break;
				case "show-config":
					actionConsumer.accept("show-config");
					break;
				case "wmHome":
					data.setIsInstanceId(pValue.get());
					break;
				case "h":
				case "?":
				case "help":
					throw usage(null);
				default:
					throw usage("Unknown option: " + pOption);
				}
			}

			@Override
			public RuntimeException error(String pMsg) {
				return usage(pMsg);
			}			
		};
		process(pArgs, handler);
	}

	private static RuntimeException usage(String pMsg) {
		final PrintStream ps = System.err;
		if (pMsg != null) {
			ps.println(pMsg);
			ps.println();
		}
		/*
		 * In what follows, we'll restrict ourselves to 76 characters per line.
		 * The following scale should help with that:
		 * 
		 *          0         1         2         3         4         5         6         7
		 *          0123456789012345678901234567890123456789012345678901234567890123456789012345
		 */
		ps.println("Usage: java " + Main.class.getName() + " [<ACTION>] <OPTIONS>");
		ps.println();
		ps.println("Possible actions are:");
		ps.println("  --compile <PKG>   Compiles the given package.");
		ps.println("  --list            List all packages in the packages directory.");
		ps.println("                    This action is the default.");
		ps.println("  --show <PACKAGE>  Show details for the given package.");
		ps.println("  --show-config     Show the configuration file ${HOME}/.ispm/config.xml");
		ps.println();
		ps.println("Other options are:");
		ps.println("  -h|/?|-help          Show this message, and exit with error status.");
		ps.println("  --wmHome <ID>     Sets the webMethods home directory.");
		System.exit(1);
		return null;
	}
}
