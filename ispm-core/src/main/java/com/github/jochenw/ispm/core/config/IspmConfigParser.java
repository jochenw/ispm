package com.github.jochenw.ispm.core.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;

import com.github.jochenw.afw.core.util.DomHelper;
import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.LocalizableDocument;
import com.github.jochenw.afw.core.util.LocalizableDocument.Localizable;
import com.github.jochenw.afw.core.util.Sax;


public class IspmConfigParser {
	public static class ParsedConfiguration {
		private final Map<String,LocalRepo> localRepos = new HashMap<>();
		private final Map<String,Instance> instances = new HashMap<>();
	}
	public static class LocalRepo extends Localizable {
		private final String id, layout;
		private final boolean isDefault;
		private final Path baseDir;
		public LocalRepo(Locator pLocator, String pId, String pLayout, boolean pDefault, Path pBaseDir) {
			super(pLocator);
			id = pId;
			layout = pLayout;
			isDefault = pDefault;
			baseDir = pBaseDir;
		}
		public String getId() { return id; }
		public String getLayout() { return layout; }
		public boolean isDefault() { return isDefault; }
		public Path getBaseDir() { return baseDir; }
	}
	public static class Instance extends Localizable {
		private final String id;
		private final boolean isDefault;
		private final Path baseDir, packagesDir, configDir, logsDir;
		
		public Instance(Locator pLocator, String pId, boolean pDefault, Path pBaseDir,
				        Path pPackagesDir, Path pConfigDir, Path pLogsDir) {
			super(pLocator);
			id = pId;
			isDefault = pDefault;
			baseDir = pBaseDir;
			packagesDir = pPackagesDir;
			configDir = pConfigDir;
			logsDir = pLogsDir;
		}
		public String getId() { return id; }
		public boolean isDefault() { return isDefault; }
		public Path getBaseDir() { return baseDir; }
		public Path getPackagesDir() { return packagesDir; }
		public Path getConfigDir() { return configDir; }
		public Path getLogsDir() { return logsDir; }
	}
	public static String NS = "http://namespaces.github.com/jochenw/ispm/core/config/1.0.0";
	private final LocalizableDocument ldoc;
	private final DomHelper helper;
	private final ParsedConfiguration config = new ParsedConfiguration();

	protected IspmConfigParser(LocalizableDocument pDocument) {
		ldoc = pDocument;
		helper = pDocument.getDomHelper();
		helper.setDefaultNamespaceUri(NS);
	}

	protected ParsedConfiguration parse(Document pDocument) {
		final Element rootElement = pDocument.getDocumentElement();
		helper.assertElementNS(rootElement, NS, "ispm-config");
		for (Element e : helper.getChildren(rootElement)) {
			if (!NS.equals(e.getNamespaceURI())) {
				throw helper.error(e, "Expected element namespace " + NS + ", got "
						+ Sax.asQName(e.getNamespaceURI(), e.getLocalName()));
			}
			if (helper.isElementNS(e, NS, "localRepo")) {
				final LocalRepo localRepo = parseLocalRepo(e);
				if (config.localRepos.put(localRepo.getId(), localRepo) != null) {
					throw helper.error(e, "Duplicate local repository id: " + localRepo.getId());
				}
			} else if (helper.isElementNS(e, NS, "instance")) {
				final Instance instance = parseInstance(e);
				if (config.instances.put(instance.getId(), instance) != null) {
					throw helper.error(e, "Duplicate instance id: " + instance.getId());
				}
			} else {
				throw helper.error(e, "Expected element localRepo, or instance, got" + e.getLocalName());
			}
		}
		return config;
	}

	protected LocalRepo parseLocalRepo(Element pElement) {
		final String id = helper.requireAttribute(pElement, "id");
		final String layout = helper.requireAttribute(pElement, "layout", "default");
		final boolean isDefault = Boolean.valueOf(helper.requireAttribute(pElement, "default", "false"));
		final Path baseDirPath = helper.requirePathElement(pElement, "baseDir", Files::isDirectory,
				                       (s) -> "The element baseDir contains an invalid path: "
					                          + s + " (No such directory)");
		return new LocalRepo(ldoc.getLocator(pElement), id, layout, isDefault, baseDirPath);
	}

	protected Instance parseInstance(Element pElement) {
		final String id = helper.requireAttribute(pElement, "id");
		final boolean isDefault = Boolean.valueOf(helper.requireAttribute(pElement, "default", "false"));
		final Path baseDirPath = helper.requirePathElement(pElement, "baseDir", Files::isDirectory,
				                       (s) -> "The element baseDir contains an invalid path: "
					                          + s + " (No such directory)");
		final Path configDirPath = helper.getPathElement(pElement, "configDir", Files::isDirectory,
                (s) -> "The element configDir contains an invalid path: "
                       + s + " (No such directory)");
		final Path packagesDirPath = helper.getPathElement(pElement, "packagesDir", Files::isDirectory,
                (s) -> "The element packagesDir contains an invalid path: "
                       + s + " (No such directory)");
		final Path logsDirPath = helper.getPathElement(pElement, "logsDir", Files::isDirectory,
				(s) -> "The element logsDir contains an invalid path: "
	                       + s + " (No such directory)");
		return new Instance(ldoc.getLocator(pElement), id, isDefault, baseDirPath,
				            configDirPath, packagesDirPath, logsDirPath);
		
	}

	public static ParsedConfiguration parse(Path pConfigFile) {
		try (InputStream in = Files.newInputStream(pConfigFile)) {
			final InputSource isource = new InputSource(in);
			isource.setSystemId(pConfigFile.toString());
			final LocalizableDocument ldoc = LocalizableDocument.parse(isource);
			return new IspmConfigParser(ldoc).parse(ldoc.getDocument());
		} catch (IOException e) {
			throw Exceptions.show(e);
		}
	}

	public ParsedConfiguration parse(URL pUrl) {
		try (InputStream in = pUrl.openStream()) {
			final InputSource isource = new InputSource(in);
			isource.setSystemId(pUrl.toExternalForm());
			final LocalizableDocument ldoc = LocalizableDocument.parse(isource);
			return new IspmConfigParser(ldoc).parse(ldoc.getDocument());
		} catch (IOException e) {
			throw Exceptions.show(e);
		}
	}
}
