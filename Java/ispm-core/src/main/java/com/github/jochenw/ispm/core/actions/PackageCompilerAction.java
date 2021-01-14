package com.github.jochenw.ispm.core.actions;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;

import javax.inject.Inject;

import com.github.jochenw.afw.core.inject.IComponentFactory;
import com.github.jochenw.afw.core.inject.LogInject;
import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.ispm.core.compile.PackageCompiler;
import com.github.jochenw.ispm.core.model.IInstance;
import com.github.jochenw.ispm.core.model.IspmConfiguration;

public class PackageCompilerAction extends AbstractAction {
	private @LogInject ILog log;
	private @Inject IspmConfiguration config;
	private @Inject IComponentFactory componentFactory;

	public enum Option {
		NO_FAILURE_ON_WARNINGS,
		RELOAD,
		USING_X_LINT_DEPRECATION,
	}

	private boolean hasOption(Option pOption, Option[] pOptions) {
		if (pOptions != null) {
			for (Option o : pOptions) {
				if (pOption == o) {
					return true;
				}
			}
		}
		return false;
	}

	public Context compile(String[] pPackages, Option... pOptions) {
		final String mName = "compile";
		final String[] packages = Objects.requireAllNonNull(Objects.requireNonNull(pPackages, "Packages"), "Package");
		return run(null, "compile", log, (ctx) -> {
			if (packages.length == 0) {
				ctx.action("No packages given, nothing to do.");
			} else {
				final IInstance instance = config.requireInstance("local");
				final IoCatcher ioCatcher = Objects.requireNonNull(ctx.getIoCatcher());
				final PackageCompiler pc = new PackageCompiler() {
					@Override
					protected Path getPackagesDir(Path pInstanceDir) {
						return instance.getPackagesDir();
					}

					@Override
					protected ByteArrayOutputStream newStdOutStream() {
						return ioCatcher.getStandardOut();
					}

					@Override
					protected ByteArrayOutputStream newStdErrStream() {
						return ioCatcher.getStandardErr();
					}
					
				};
				pc.setLogger((s) -> {
					ctx.debug(log, "compile", s);
				});
				pc.setWarnLogger((s) -> {
					ctx.warn(log, "compile", s);
				});
				pc.setUsingXlintDeprecation(hasOption(Option.USING_X_LINT_DEPRECATION, pOptions));
				pc.setFailingOnWarnings(!hasOption(Option.NO_FAILURE_ON_WARNINGS, pOptions));
				final PackageReloadAction reloadAction = componentFactory.requireInstance(PackageReloadAction.class);
				for (String pkg : packages) {
					ctx.action("Compiling package: " + pkg);
					ctx.info(log, mName, "Compiling package: " + pkg);
					pc.compile(instance.getBaseDir(), pkg);
					ctx.info(log, mName, "Compiled package: " + pkg);
					ctx.action("Compiled package: " + pkg);
					if (hasOption(Option.RELOAD, pOptions)) {
						reloadAction.reloadPkg(ctx, instance, pkg);
					}
				}
			}
		});
	}
}
