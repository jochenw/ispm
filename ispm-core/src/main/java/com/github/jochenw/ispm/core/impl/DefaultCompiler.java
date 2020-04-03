package com.github.jochenw.ispm.core.impl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import com.github.jochenw.afw.core.inject.LogInject;
import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Sax;
import com.github.jochenw.afw.core.util.Strings;
import com.github.jochenw.afw.core.util.Systems;
import com.github.jochenw.ispm.core.api.Data;
import com.github.jochenw.ispm.core.api.IIspmConfiguration;
import com.github.jochenw.ispm.core.api.IIspmConfiguration.IIsInstance;
import com.github.jochenw.ispm.core.sax.ManifestParser;
import com.github.jochenw.ispm.core.sax.ManifestParser.Listener;
import com.wm.util.EncUtil;

public class DefaultCompiler implements ICompiler {
	@LogInject private ILog log;
	@Inject IIspmConfiguration ispmConfig;
	@Inject @Named(value="java.compiler.fileEncoding") private String fileEncoding;
	@Inject @Named(value="java.compiler.pathSeparator") private String pathSeparator;

	@Override
	public void compile(Data pData) {
		final String isInstanceId = pData.getIsInstanceId();
		final String packageName = pData.getPackageName();
		log.enteringf("compile", "IsInstanceId=%s, Package=%s", isInstanceId, packageName);
		if (packageName == null) {
			throw new NullPointerException("The package name is null.");
		}
		final IIsInstance isInstance = ispmConfig.getIsInstance(isInstanceId);
		if (isInstance == null) {
			throw new IllegalArgumentException("Unknown IS instance: " + isInstanceId);
		}
		final Path packageDir = isInstance.getPackagesDir().resolve(packageName);
		if (!Files.isDirectory(packageDir)) {
			throw new IllegalArgumentException("Package " + packageName
					+ " not found in IS instance " + isInstanceId
					+ " (Directory " + packageDir.toAbsolutePath() + " doesn't exist.)");
		}
		final Path classesDir = packageDir.resolve("code").resolve("classes");
		try {
			Files.createDirectories(classesDir);
		} catch (IOException e) {
			throw Exceptions.show(e);
		}
		final Path[] packageDependencies = getPackageDependencies(packageDir);
		final String pathSeparator = Strings.notEmpty(this.pathSeparator, File.pathSeparator);
		final StringBuilder sb = new StringBuilder();
		for (Path p : packageDependencies) {
			if (sb.length() > 0) {
				sb.append(pathSeparator);
			}
			sb.append(p);
		}
		final String classPath = sb.toString();
		final String encoding = Strings.notEmpty(fileEncoding, "Unicode");
		final String[] args = new String[] {
			"-g", "-encoding", encoding, "-classpath", classPath, "-d", classesDir.toString()
		};

		int status;
		final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		final ByteArrayOutputStream errStream = new ByteArrayOutputStream();
		final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			throw new IllegalStateException("JRE doesn't provide a Java compiler: "
					+ System.getProperty("java.home"));
		}
		log.debug("compile", "Starting compiler", (Object[]) args);
		status = compiler.run(null, outStream, errStream, args);
		for (String s : getLines(errStream)) {
			log.error("compile", s);
		}
		for (String s : getLines(outStream)) {
			log.debug("compile", s);
		}
		if (status != 0) {
			throw new IllegalStateException("Java compiler failed: Status=" + status);
		}
	}

	protected Iterable<String> getLines(ByteArrayOutputStream pOs) {
		final byte[] bytes = pOs.toByteArray();
		final List<String> lines = new ArrayList<String>();
		try (InputStream in = new ByteArrayInputStream(bytes);
			 Reader reader = new InputStreamReader(in);
			 BufferedReader br = new BufferedReader(reader)) {
			for (;;) {
				final String line = br.readLine();
				if (line == null) {
					break;
				} else {
					lines.add(line);
				}
			}
		} catch (IOException e) {
			throw Exceptions.show(e);
		}
		return lines;
	}

	protected Path[] getPackageDependencies(Path pPackageDir) {
		final PackageDependency[] requiredPackages = findPackageDependencies(pPackageDir);
		final List<Path> jarFiles = new ArrayList<>();
		for (PackageDependency pd : requiredPackages) {
			jarFiles.addAll(pd.getJarFiles());
		}
		return jarFiles.toArray(new Path[jarFiles.size()]);
	}

	public static class PackageDependency {
		private final int level;
		private final String packageName;
		private final Path packageDir;
		private final List<Path> jarFiles = new ArrayList<>();

		public PackageDependency(int pLevel, String pPackageName, Path pPackageDir) {
			level = pLevel;
			packageName = pPackageName;
			packageDir = pPackageDir;
		}

		public int getLevel() {
			return level;
		}

		public String getPackageName() {
			return packageName;
		}

		public Path getPackageDir() {
			return packageDir;
		}

		public void addJarFile(Path pJarFile) {
			jarFiles.add(pJarFile);
		}

		public List<Path> getJarFiles() {
			return jarFiles;
		}
	}
	
	protected PackageDependency[] findPackageDependencies(Path pPackageDir) {
		final Map<String,PackageDependency> includedPackages = new HashMap<>();
		addPackageDependency(includedPackages, pPackageDir, 0);
		final List<PackageDependency> dependencies = new ArrayList<>(includedPackages.values());
		dependencies.sort((pd1, pd2) -> Integer.signum(pd1.getLevel() - pd2.getLevel()));
		return dependencies.toArray(new PackageDependency[dependencies.size()]);
	}

	protected void addPackageDependency(Map<String,PackageDependency> pIncludedPackages,
			                            Path pPackageDir, int pLevel) {
		final String packageName = pPackageDir.getFileName().toString();
		if (pIncludedPackages.containsKey(packageName)) {
			return;
		}
		final PackageDependency pd = new PackageDependency(pLevel, packageName, pPackageDir);
		pIncludedPackages.put(packageName, pd);
		final Path codeJarsDir = pPackageDir.resolve("code").resolve("jars");
		final Path codeJarsStaticDir = codeJarsDir.resolve("static");
		if (Files.isDirectory(codeJarsStaticDir)) {
			addJarFiles(pd, codeJarsStaticDir);
		}
		if (Files.isDirectory(codeJarsDir)) {
			addJarFiles(pd, codeJarsDir);
		}
		final String[] requiredPackageNames = getRequiredPackageNames(pPackageDir);
		for (String requiredPackage : requiredPackageNames) {
			final Path requiredPackageDir = pPackageDir.getParent().resolve(requiredPackage);
			if (Files.isDirectory(requiredPackageDir)) {
				addPackageDependency(pIncludedPackages, requiredPackageDir, pLevel+1);
			} else {
				log.warnf("addPackageDependency",
						   "Package %s (Dependency of %s) not found.",
						   requiredPackage, packageName);
			}
		}
	}

	protected void addJarFiles(PackageDependency pPackageDependency, Path pDir) {
		try {
			Files.walk(pDir, 1)
			    .filter((p) -> p.getFileName().toString().toLowerCase().endsWith(".jar"))
			    .forEach((p) -> pPackageDependency.addJarFile(p));
		} catch (IOException e) {
			throw Exceptions.show(e);
		}
	}

	protected String[] getRequiredPackageNames(Path pPackageDir) {
		final Path manifestFile = pPackageDir.resolve("manifest.v3");
		final List<String> names = new ArrayList<String>();
		final ManifestParser mp = new ManifestParser(new ManifestParser.Listener() {
			@Override
			public void version(String pVersion) {
				// Does nothing.
			}

			@Override
			public void startupService(String pService) {
				// Does nothing.
			}

			@Override
			public void shutdownService(String pService) {
				// Does nothing.
			}

			@Override
			public void requires(String pPackageName, String pVersion) {
				names.add(pPackageName);
			}
		});
		Sax.parse(manifestFile, mp);
		return names.toArray(new String[names.size()]);
	}
}
