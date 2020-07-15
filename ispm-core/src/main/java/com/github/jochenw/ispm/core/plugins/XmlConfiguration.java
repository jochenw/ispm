package com.github.jochenw.ispm.core.plugins;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiConsumer;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.AttributesImpl;

import com.github.jochenw.afw.core.inject.IComponentFactory;
import com.github.jochenw.afw.core.log.app.IAppLog;
import com.github.jochenw.afw.core.util.DomHelper.LocalizableException;
import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Functions.FailableBiConsumer;
import com.github.jochenw.afw.core.util.Sax;
import com.github.jochenw.ispm.core.config.IConfiguration;

public class XmlConfiguration {
	public static class IsInstance {
		private final Locator locator;
		private final String dir, wmVersion, wmHomeDir, isHomeDir, packageDir, configDir;

		/**
		 * @param pDir
		 * @param pWmVersion
		 * @param pWmHomeDir
		 * @param pPackageDir
		 * @param pConfigDir
		 */
		public IsInstance(Locator pLocator, String pDir, String pWmVersion, String pWmHomeDir, String pIsHomeDir, String pPackageDir, String pConfigDir) {
			locator = pLocator;
			dir = pDir;
			wmVersion = pWmVersion;
			wmHomeDir = pWmHomeDir;
			isHomeDir = pIsHomeDir;
			packageDir = pPackageDir;
			configDir = pConfigDir;
		}

		/**
		 * @return the dir
		 */
		public String getDir() {
			return dir;
		}

		/**
		 * @return the wmVersion
		 */
		public String getWmVersion() {
			return wmVersion;
		}

		/**
		 * @return the isHomeDir
		 */
		public String getIsHomeDir() {
			return isHomeDir;
		}

		/**
		 * @return the wmHomeDir
		 */
		public String getWmHomeDir() {
			return wmHomeDir;
		}

		/**
		 * @return the packageDir
		 */
		public String getPackageDir() {
			return packageDir;
		}

		/**
		 * @return the configDir
		 */
		public String getConfigDir() {
			return configDir;
		}

		/** Returns the plugins locator.
		 */
		public Locator getLocator() {
			return locator;
		}
	}

	public static class TPlugin {
		private final Map<String,String> properties = new HashMap<String,String>();
		private final Locator locator;
		private final String id, type;

		/**
		 * @param pId
		 * @param pType
		 */
		public TPlugin(Locator pLocator, String pId, String pType) {
			locator = pLocator;
			id = pId;
			type = pType;
		}

		/**
		 * @return the properties
		 */
		public Map<String, String> getProperties() {
			return properties;
		}

		/**
		 * @return the id
		 */
		public String getId() {
			return id;
		}

		/**
		 * @return the type
		 */
		public String getType() {
			return type;
		}

		/** Returns the plugins locator.
		 */
		public Locator getLocator() {
			return locator;
		}
	}

	public static class TLocalRepository extends TPlugin {
		public TLocalRepository(Locator pLocator, String pId, String pType,
				                Map<String,String> pProperties) {
			super(pLocator, pId, pType);
			if (pProperties != null) {
				getProperties().putAll(pProperties);
			}
		}
	}

	public static class TRemoteRepository extends TPlugin {
		public TRemoteRepository(Locator pLocator, String pId, String pType,
				                 Map<String,String> pProperties) {
			super(pLocator, pId, pType);
			if (pProperties != null) {
				getProperties().putAll(pProperties);
			}
		}
	}

	private final List<IsInstance> instances;
	private final List<TLocalRepository> localRepositories;
	private final List<TRemoteRepository> remoteRepositories;
	private @Inject IAppLog appLog;
	private @Inject IComponentFactory componentFactory;

	
	/**
	 * @param pInstances
	 * @param pLocalRepositories
	 * @param pRemoteRepositories
	 */
	public XmlConfiguration() {
		this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
	}

	/**
	 * @param pInstances
	 * @param pLocalRepositories
	 * @param pRemoteRepositories
	 */
	public XmlConfiguration(List<IsInstance> pInstances, List<TLocalRepository> pLocalRepositories,
			List<TRemoteRepository> pRemoteRepositories) {
		super();
		instances = pInstances;
		localRepositories = pLocalRepositories;
		remoteRepositories = pRemoteRepositories;
	}

	protected IConfiguration asConfiguration() throws LocalizableException {
		throw new IllegalStateException("Not implemented");
	}

	public IConfiguration parse(Path pConfigFilePath) {
		try {
			final XmlConfigurationHandler h = new XmlConfigurationHandler(appLog);
			Sax.parse(pConfigFilePath, h);
			final XmlConfiguration xmlConfiguration = h.getXmlConfiguration();
			return xmlConfiguration.asConfiguration();
		} catch (UndeclaredThrowableException ute) {
			final Throwable t = ute.getCause();
			if (t != null  &&  t instanceof SAXParseException) {
				final SAXParseException e = (SAXParseException) t;
				final String msg = Sax.asLocalizedMessage(e.getMessage(), e.getSystemId(), e.getLineNumber(), e.getColumnNumber());
				throw new IllegalStateException(msg, e);
			} else {
				throw ute;
			}
		} catch (LocalizableException e) {
			final String msg = Sax.asLocalizedMessage(e.getLocator(), e.getMessage());
			throw new IllegalStateException(msg, e);
		}
	}

	public XmlConfiguration getDefault() {
		return new XmlConfiguration();
	}

	public void save(XmlConfiguration pDefault, Path pConfigFilePath) {
		try (OutputStream os = Files.newOutputStream(pConfigFilePath);
			 BufferedOutputStream bos = new BufferedOutputStream(os)) {
			save(bos);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	public void save(OutputStream pOs)
			throws TransformerException, SAXException {
		final SAXTransformerFactory stf = (SAXTransformerFactory) TransformerFactory.newInstance();
		final TransformerHandler th = stf.newTransformerHandler();
		final Transformer t = th.getTransformer();
		t.setOutputProperty(OutputKeys.INDENT, "yes");
		t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		th.setResult(new StreamResult(pOs));
		th.startDocument();
		final Attributes NO_ATTRS = new AttributesImpl();
		final AttributesImpl rootAttrs = new AttributesImpl();
		rootAttrs.addAttribute(XMLConstants.XML_NS_URI, "xmlns", "xmlns:xsi", "CDATA", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
		rootAttrs.addAttribute(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "schemaLocation", "xsi:schemaLocation", "CDATA", "ispm-config.xsd");
		th.startElement(XmlConfigurationHandler.NS, "ispm-config", "ispm-config", rootAttrs);
		final List<TLocalRepository> localRepos = localRepositories;
		final List<TRemoteRepository> remoteRepos = remoteRepositories;
		final List<IsInstance> isInstances = instances;
		final String NS = XmlConfigurationHandler.NS;
		final FailableBiConsumer<TPlugin,String,SAXException> pluginWriter = (p,s) -> {
			final AttributesImpl repoAttrs = new AttributesImpl();
			repoAttrs.addAttribute("", "id", "id", "CDATA", p.getId());
			repoAttrs.addAttribute("", "type", "type", "CDATA", p.getType());
			th.startElement(NS, s, s, repoAttrs);
			final Map<String,String> properties = p.getProperties();
			if (properties != null  &&  !properties.isEmpty()) {
				for (Map.Entry<String,String> en : properties.entrySet()) {
					final String key = en.getKey();
					final String value = en.getValue();
					if (value != null) {
						final AttributesImpl propAttrs = new AttributesImpl();
						propAttrs.addAttribute("", "key", "key", "CDATA", key);
						final boolean useElement;
						if (value.length() == 0) {
							propAttrs.addAttribute("", "empty", "empty", "CDATA", "true");
							propAttrs.addAttribute("", "value", "value", "CDATA", "");
							useElement = false;
						} else if (value.length() > 40 ||  value.indexOf(10)!= -1  ||  value.indexOf(13) != -1) {
							useElement = true;
						} else {
							propAttrs.addAttribute("", "value", "value", "CDATA", value);
							useElement = false;
						}
						th.startElement(NS, "property", "property", propAttrs);
						if (useElement) {
							final char[] chars = value.toCharArray();
							th.characters(chars, 0, chars.length);
						}
						th.endElement(NS, "property", "property");
					}
				}
			}
			th.endElement(NS, s, s);
		};
		if (localRepos != null  &&  !localRepos.isEmpty()) {
			th.startElement(NS, "localRepositories", "localRepositories", NO_ATTRS);
			for (TLocalRepository localRepo : localRepos) {
				pluginWriter.accept(localRepo, "localRepository");
			}
			th.endElement(NS, "localRepositories", "localRepositories");
		}
		if (remoteRepos != null  &&  !remoteRepos.isEmpty()) {
			th.startElement(NS, "remoteRepositories", "remoteRepositories", NO_ATTRS);
			for (TRemoteRepository remoteRepo : remoteRepos) {
				pluginWriter.accept(remoteRepo, "remoteRepository");
			}
			th.endElement(NS, "remoteRepositories", "remoteRepositories");
		}
		if (isInstances != null  &&  !isInstances.isEmpty()) {
			th.startElement(NS, "isInstanceDirs", "isInstanceDirs", NO_ATTRS);
			for (IsInstance isInstance : isInstances) {
				final AttributesImpl instAttrs = new AttributesImpl();
				instAttrs.addAttribute("", "dir", "dir", "CDATA", isInstance.getDir());
				if (isInstance.getWmVersion() != null) {
					instAttrs.addAttribute("", "wmVersion", "wmVersion", "CDATA", isInstance.getWmVersion());
				}
				if (isInstance.getWmHomeDir() != null  &&  !"../../..".equals(isInstance.getWmHomeDir())) {
					instAttrs.addAttribute("", "wmHomeDir", "wmHomeDir", "CDATA", isInstance.getWmHomeDir());
				}
				if (isInstance.getIsHomeDir() != null  &&  !"../..".equals(isInstance.getIsHomeDir())) {
					instAttrs.addAttribute("", "isHomeDir", "isHomeDir", "CDATA", isInstance.getIsHomeDir());
				}
				if (isInstance.getPackageDir() != null  &&  !"packages".equals(isInstance.getPackageDir())) {
					instAttrs.addAttribute("", "packageDir", "packageDir", "CDATA", isInstance.getPackageDir());
				}
				if (isInstance.getConfigDir() != null  &&  !"config".equals(isInstance.getConfigDir())) {
					instAttrs.addAttribute("", "configDir", "configDir", "CDATA", isInstance.getConfigDir());
				}
				th.startElement(NS, "isInstanceDir", "isInstanceDir", instAttrs);
				th.endElement(NS, "isInstanceDir", "isInstanceDir");
			}
			th.endElement(NS, "isInstanceDirs", "isInstanceDirs");
		}
		th.endElement(XmlConfigurationHandler.NS, "ispm-config", "ispm-config");
		th.endDocument();
	}

	public void validate(InputStream pIn) {
		try {
			final URL url = getClass().getResource("ispm-config.xsd");
			if (url == null) {
				throw new IllegalStateException("Unable to locate resource: ispm-config.xsd");
			}
			final SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			final Schema schema = sf.newSchema(url);
			schema.newValidator().validate(new StreamSource(pIn));
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	public @Nonnull List<TLocalRepository> getLocalRepositories() {
		if (localRepositories == null) {
			return Collections.emptyList();
		} else {
			return localRepositories;
		}
	}

	public @Nonnull List<TRemoteRepository> getRemoteRepositories() {
		if (remoteRepositories == null) {
			return Collections.emptyList();
		} else {
			return remoteRepositories;
		}
	}

	public @Nonnull List<IsInstance> getIsInstanceDirs() {
		if (instances == null) {
			return Collections.emptyList();
		} else {
			return instances;
		}
	}
}
