package com.github.jochenw.ispm.core.compile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.github.jochenw.ispm.core.util.Exceptions;

public class PackageCompiler {
	public static class CompilerStatusException extends Exception {
		private static final long serialVersionUID = 6498551044559613520L;
		private final Data data;

		public CompilerStatusException(Data pData, String pMessage, Throwable pCause) {
			super(pMessage, pCause);
			data = pData;
		}

		public CompilerStatusException(Data pData, String pMessage) {
			super(pMessage);
			data = pData;
		}

		public CompilerStatusException(Data pData, Throwable cause) {
			super(cause);
			data = pData;
		}
		public Data getData() { return data; }
	}
	
	public static class Data {
		private final Path instanceDir, packagesDir, packageDir, codeSourceDir, codeClassesDir;
		private final String packageName;
		private final List<Path> javaSourceFiles = new ArrayList<>();
		private final List<Path> classPathDependencies = new ArrayList<>();
		private final Set<String> collectedPackageDependencies = new HashSet<String>();
		private byte[] standardOutput, errorOutput;
		private int status;
		public Data(Path pInstanceDir, Path pPackagesDir, Path pPackageDir, Path pCodeSourceDir, Path pCodeClassesDir,
				    String pPackageName) {
			instanceDir = pInstanceDir;
			packagesDir = pPackagesDir;
			packageDir = pPackageDir;
			codeSourceDir = pCodeSourceDir;
			codeClassesDir = pCodeClassesDir;
			packageName = pPackageName;
		}
		public Path getInstanceDir() { return instanceDir; }
		public Path getPackagesDir() { return packagesDir; }
		public Path getPackageDir() { return packageDir; }
		public Path getCodeSourceDir() { return codeSourceDir; }
		public Path getCodeClassesDir() { return codeClassesDir; }
		public String getPackageName() { return packageName; }
		public void addJavaSourceFile(Path pPath) {
			javaSourceFiles.add(pPath);
		}
		public void addClassPathDependency(Path pPath) {
			classPathDependencies.add(pPath);
		}
		public boolean isPackageDependencyCollected(String pPackageName) { return collectedPackageDependencies.contains(pPackageName); }
		public void addCollectedPackageDependencyCollected(String pPackageName) { collectedPackageDependencies.add(pPackageName); }
		public void setStandardOutput(byte[] pBytes) {
			standardOutput = pBytes;
		}
		public void setErrorOutput(byte[] pBytes) {
			errorOutput = pBytes;
		}
		public void setStatus(int pStatus) {
			status = pStatus;
		}
		public byte[] getStandardOutput() {
			return standardOutput;
		}
		public byte[] getErrorOutput() {
			return errorOutput;
		}
		public int getStatus() {
			return status;
		}
	}

	private Consumer<String> logger;
	private Consumer<String> warnLogger;

	public Consumer<String> getLogger() {
		return logger;
	}

	public void setLogger(Consumer<String> pLogger) {
		logger = pLogger;
	}

	public Consumer<String> getWarnLogger() {
		return warnLogger;
	}

	public void setWarnLogger(Consumer<String> pLogger) {
		warnLogger = pLogger;
	}

	protected Path getPackagesDir(Path pInstanceDir) {
		return pInstanceDir.resolve("packages");
	}

	protected Path getPackageDir(Path pPackagesDir, String pPackageName) {
		return pPackagesDir.resolve(pPackageName);
	}

	protected Path getCodeSourceDir(Path pPackageDir) {
		return pPackageDir.resolve("code/source");
	}

	protected Path getCodeClassesDir(Path pPackageDir) {
		return pPackageDir.resolve("code/classes");
	}

	protected Path getCodeJarsDir(Path pPackageDir) {
		return pPackageDir.resolve("code/jars");
	}

	protected void collectJavaSourceFiles(Data pData) {
		try {
			Files.walk(pData.getCodeSourceDir(), 0).forEach((p) -> {
				if (Files.isRegularFile(p)  &&  p.getFileName().toString().endsWith(".java")) {
					logger.accept("Adding java source file: " + p);
					pData.addJavaSourceFile(p);
				}
			});
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	protected String getClassPathElement(Path pPath) {
		final Path currentDir = Paths.get("");
		final String relativePath = currentDir.relativize(pPath).toString();
		final String absolutePath = pPath.toAbsolutePath().toString();
		if (relativePath.length() < absolutePath.length()) {
			return relativePath;
		} else {
			return absolutePath;
		}
	}

	protected String getClassPathString(Data pData) {
		final StringBuilder sb = new StringBuilder();
		for (Path path : pData.classPathDependencies) {
			if (sb.length() > 0) {
				sb.append(File.pathSeparator);
			}
			sb.append(getClassPathElement(path));
		}
		return sb.toString();
	}

	protected void runCompiler(Data pData) {
		JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
		final Path codeClassesDir = pData.getCodeClassesDir();
		final List<String> argList = new ArrayList<String>();
		argList.add("-g");
		argList.add("-classpath");
		argList.add(getClassPathString(pData));
		argList.add("-d");
		argList.add(getClassPathElement(codeClassesDir));
		final int status;
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final ByteArrayOutputStream baes = new ByteArrayOutputStream();
		try {
			Files.createDirectories(codeClassesDir);
			final String[] args = argList.toArray(new String[argList.size()]);
			status = javaCompiler.run((InputStream) null, baos, baes, args);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
		pData.setStandardOutput(baos.toByteArray());
		pData.setErrorOutput(baes.toByteArray());
		pData.setStatus(status);
		if (status != 0) {
			
		}
	}

	protected Path getManifestFile(Path pPackageDir) {
		return pPackageDir.resolve("manifest.v3");
	}

	protected void collectClassPathDependencies(Data pData, String pPackageName) {
		if (pData.isPackageDependencyCollected(pPackageName)) {
			return; // This package has already been recognized, nothing to do.
		}
		logger.accept("Package dependency: " + pPackageName);
		final Path packageDir = getPackageDir(pData.getPackagesDir(), pPackageName);
		final String[] requiredPackages= parseManifestFile(pPackageName, packageDir); 
		final Path codeClassesDir = getCodeClassesDir(packageDir);
		if (Files.isDirectory(codeClassesDir)) {
			logger.accept("Classpath element: Classes directory " + codeClassesDir);
			pData.addClassPathDependency(codeClassesDir);
		} else {
			warnLogger.accept("code/classes directory not found for package: " + pPackageName + ". Ignoring classes from this package.");
		}
		collectPackageJarFiles(pPackageName, packageDir);
		for (String requiredPackage : requiredPackages) {
			collectClassPathDependencies(pData, requiredPackage);
		}
	}

	protected String[] parseManifestFile(String pPackageName, final Path packageDir) {
		final List<String> requiredPackages = new ArrayList<String>();
		final ManifestParser.Listener listener = new ManifestParser.Listener() {
			@Override
			public void requires(String pPackageName, String pVersion) {
				requiredPackages.add(pPackageName);
			}
		};
		final Path manifestFile = getManifestFile(packageDir);
		if (Files.isRegularFile(manifestFile)) {
			final ManifestParser manifestParser = new ManifestParser(listener);
			try {
				final SAXParserFactory spf = SAXParserFactory.newInstance();
				spf.setNamespaceAware(true);
				spf.setValidating(false);
				final XMLReader xr = spf.newSAXParser().getXMLReader();
				xr.setContentHandler(manifestParser);
				try (InputStream in = Files.newInputStream(manifestFile)) {
					final InputSource isource = new InputSource(in);
					isource.setSystemId(manifestFile.toString());
					xr.parse(isource);
				}
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		} else {
			warnLogger.accept("Manifest file not found for package " + pPackageName + ". Ignoring this packages dependencies.");
		}
		return requiredPackages.toArray(new String[requiredPackages.size()]);
	}

	protected void collectPackageJarFiles(String pPackageName, final Path packageDir) {
		final Path codeJarsDir = getCodeJarsDir(packageDir);
		if (Files.isDirectory(codeJarsDir)) {
			try {
				Files.walk(codeJarsDir, 1).forEach((p) -> {
					if (p.getFileName().toString().endsWith(".jar")  &&  Files.isRegularFile(p)) {
						logger.accept("Classpath element: Package jar file " + p);
					}
				});
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		} else {
			warnLogger.accept("code/jars directory not found for package: " + pPackageName + ". Ignoring jar files from this package.");
		}
	}

	protected void collectClassPathDependencies(Data pData) {
		collectClassPathDependencies(pData, pData.getPackageName());
	}

	public void compile(Path pInstanceDir, String pPackageName) throws CompilerStatusException {
		final Path instanceDir = Objects.requireNonNull(pInstanceDir, "Instance Directory");
		final String packageName = Objects.requireNonNull(pPackageName, "Package Name");
		final Path packagesDir = Objects.requireNonNull(getPackagesDir(instanceDir));
		final Path packageDir = Objects.requireNonNull(getPackageDir(packagesDir, packageName));
		if (!Files.isDirectory(packageDir)) {
			throw new IllegalStateException("Package directory not found: " + packageDir);
		}
		final Path codeSourceDir = Objects.requireNonNull(getCodeSourceDir(packageDir));
		final Path codeClassesDir = Objects.requireNonNull(getCodeClassesDir(packageDir));
		final Data data = new Data(instanceDir, packagesDir, packageDir, codeSourceDir, codeClassesDir, packageName);
		collectJavaSourceFiles(data);
		collectClassPathDependencies(data);
		runCompiler(data);
		if (data.status != 0) {
			throw new CompilerStatusException(data, "Invalid compiler status: " + data.status);
		}
	}
}
