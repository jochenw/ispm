package com.github.jochenw.ispm.core.compile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

	private Consumer<String> log;
	private Consumer<String> warnLogger;
	private boolean usingXlintDeprecation, failingOnWarnings;

	public boolean isFailingOnWarnings() {
		return failingOnWarnings;
	}

	public void setFailingOnWarnings(boolean pFailingOnWarninings) {
		failingOnWarnings = pFailingOnWarninings;
	}

	public boolean isUsingXlintDeprecation() {
		return usingXlintDeprecation;
	}

	public void setUsingXlintDeprecation(boolean pUsingXlintDeprecation) {
		usingXlintDeprecation = pUsingXlintDeprecation;
	}

	public Consumer<String> getLogger() {
		return log;
	}

	public void setLogger(Consumer<String> pLogger) {
		log = pLogger;
	}

	protected void log(String pMsg) {
		if (log != null) {
			log.accept(pMsg);
		}
	}

	protected void warn(String pMsg) {
		if (warnLogger != null) {
			warnLogger.accept(pMsg);
		}
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
			final Path codeSourceDir = pData.getCodeSourceDir();
			log("Looking for source files in " + codeSourceDir);
			Files.walk(codeSourceDir, Integer.MAX_VALUE).forEach((p) -> {
				log("Source dir entry: " + p);
				if (Files.isRegularFile(p)  &&  p.getFileName().toString().endsWith(".java")) {
					log("Adding java source file: " + p);
					pData.addJavaSourceFile(p);
				}
			});
			log("Done looking for source files");
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	protected String asLocalPath(Data pData, Path pPath) {
		final Path currentDir = pData.getInstanceDir();
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
			sb.append(asLocalPath(pData, path));
		}
		return sb.toString();
	}

	protected void runCompiler(Data pData) {
		JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
		final Path codeClassesDir = pData.getCodeClassesDir();
		final List<String> argList = new ArrayList<String>();
		if (isUsingXlintDeprecation()) {
			argList.add("-Xlint:deprecation");
		}
		argList.add("-g");
		argList.add("-classpath");
		argList.add(getClassPathString(pData));
		argList.add("-d");
		argList.add(asLocalPath(pData, codeClassesDir));
		for (Path sourceFile : pData.javaSourceFiles) {
			argList.add(asLocalPath(pData, sourceFile));
		}
		final int status;
		final ByteArrayOutputStream baos = newStdOutStream();
		final ByteArrayOutputStream baes = newStdErrStream();
		try {
			Files.createDirectories(codeClassesDir);
			final String[] args = argList.toArray(new String[argList.size()]);
			log("Compiler args: " + String.join(" ", args));
			status = javaCompiler.run((InputStream) null, baos, baes, args);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
		pData.setStandardOutput(baos.toByteArray());
		pData.setErrorOutput(baes.toByteArray());
		pData.setStatus(status);
		if (status != 0) {
			throw new IllegalStateException("Compiler exited with status=" + status + ", expected status=0");
		}
		if (isFailingOnWarnings()  &&  baes.size() > 0) {
			throw new IllegalStateException("Compiler exited with warnings, or error messages");
		}
	}

	protected ByteArrayOutputStream newStdOutStream() {
		return new ByteArrayOutputStream();
	}

	protected ByteArrayOutputStream newStdErrStream() {
		return new ByteArrayOutputStream();
	}

	protected Path getManifestFile(Path pPackageDir) {
		return pPackageDir.resolve("manifest.v3");
	}

	protected void collectClassPathDependencies(Data pData, String pPackageName) {
		if (pData.isPackageDependencyCollected(pPackageName)) {
			return; // This package has already been recognized, nothing to do.
		}
		log("Package dependency: " + pPackageName);
		final Path packageDir = getPackageDir(pData.getPackagesDir(), pPackageName);
		final String[] requiredPackages= parseManifestFile(pPackageName, packageDir); 
		final Path codeClassesDir = getCodeClassesDir(packageDir);
		if (Files.isDirectory(codeClassesDir)) {
			log("Classpath element: Classes directory " + codeClassesDir);
			pData.addClassPathDependency(codeClassesDir);
		} else {
			warnLogger.accept("code/classes directory not found for package: " + pPackageName + ". Ignoring classes from this package.");
		}
		collectPackageJarFiles(pData, pPackageName, packageDir);
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

	protected void collectPackageJarFiles(Data pData, String pPackageName, final Path packageDir) {
		final Path codeJarsDir = getCodeJarsDir(packageDir);
		if (Files.isDirectory(codeJarsDir)) {
			try {
				Files.walk(codeJarsDir, 1).forEach((p) -> {
					if (p.getFileName().toString().endsWith(".jar")  &&  Files.isRegularFile(p)) {
						log("Classpath element: Package jar file " + p);
						pData.addClassPathDependency(p);
					}
				});
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		} else {
			warnLogger.accept("code/jars directory not found for package: " + pPackageName + ". Ignoring jar files from this package.");
		}
	}

	protected Path getRootDir(Path pInstanceDir) {
		return pInstanceDir.resolve("../../..");
	}

	protected Path getCommonLibDir(Path pRootDir) {
		return pRootDir.resolve("common/lib");
	}

	protected Path getIsLibDir(Path pRootDir) {
		return pRootDir.resolve("IntegrationServer/lib");
	}

	protected void collectJarFiles(Data pData, Path pDir) {
		try {
			Files.walk(pDir,  1).forEach((p) -> {
				if (p.getFileName().toString().endsWith(".jar")  &&  Files.isRegularFile(p)) {
					log("Classpath element: Common jar file " + p);
					pData.addClassPathDependency(p);
				}
			});
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	protected void collectServerClassPathDependencies(Data pData) {
		final Path rootDir = getRootDir(pData.getInstanceDir());
		final Path isLibDir = getIsLibDir(rootDir);
		if (Files.isDirectory(isLibDir)) {
			collectJarFiles(pData, isLibDir);
		} else {
			warn("Directory <WM_HOME>/IntegrationServer/lib not found, ignoring jar files from that directory.");
		}
		final Path commonLibDir = getCommonLibDir(rootDir);
		final Path commonLibExtDir = commonLibDir.resolve("ext");
		if (Files.isDirectory(commonLibExtDir)) {
			collectJarFiles(pData, commonLibExtDir);
		} else {
			warn("Directory <WM_HOME>/common/lib/ext not found, ignoring jar files from that directory.");
		}
		final Path commonLibGfDir = commonLibDir.resolve("glassfish");
		if (Files.isDirectory(commonLibGfDir)) {
			collectJarFiles(pData, commonLibGfDir);
		} else {
			warn("Directory <WM_HOME>/common/lib/glassfish not found, ignoring jar files from that directory.");
		}
		if (Files.isDirectory(commonLibDir)) {
			collectJarFiles(pData, commonLibDir);
		} else {
			warn("Directory <WM_HOME>/common/lib not found, ignoring jar files from that directory.");
		}
	}

	protected void collectClassPathDependencies(Data pData) {
		collectClassPathDependencies(pData, pData.getPackageName());
		collectServerClassPathDependencies(pData);
	}

	public void compile(Path pInstanceDir, String pPackageName) throws CompilerStatusException {
		log("Compiling package " + pPackageName + " in directory " + pInstanceDir);
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
		log("Compiled package " + pPackageName);
	}
}
