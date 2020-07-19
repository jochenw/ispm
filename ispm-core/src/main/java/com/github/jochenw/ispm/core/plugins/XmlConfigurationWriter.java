package com.github.jochenw.ispm.core.plugins;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

import com.github.jochenw.afw.core.util.Functions.FailableBiConsumer;
import com.github.jochenw.ispm.core.plugins.XmlConfiguration.IsInstance;
import com.github.jochenw.ispm.core.plugins.XmlConfiguration.TLocalRepository;
import com.github.jochenw.ispm.core.plugins.XmlConfiguration.TPlugin;
import com.github.jochenw.ispm.core.plugins.XmlConfiguration.TRemoteRepository;
import com.github.jochenw.ispm.core.sax.AbstractContentHandler;
import com.github.jochenw.ispm.core.sax.ContentHandlerProxy;


public class XmlConfigurationWriter {
	public static final Attributes NO_ATTRS = new AttributesImpl();

	public void write(XmlConfiguration pConfiguration, OutputStream pOut) throws SAXException, TransformerException {
		final SAXTransformerFactory stf = (SAXTransformerFactory) TransformerFactory.newInstance();
		final TransformerHandler th = stf.newTransformerHandler();
		final Transformer t = th.getTransformer();
		t.setOutputProperty(OutputKeys.INDENT, "yes");
		t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		th.setResult(new StreamResult(pOut));
		write(pConfiguration, th);
	}

	private void write(XmlConfiguration pConfiguration, final ContentHandler pHandler) throws SAXException {
		pHandler.startDocument();
		final AttributesImpl rootAttrs = new AttributesImpl();
		rootAttrs.addAttribute(XMLConstants.XML_NS_URI, "xmlns", "xmlns:xsi", "CDATA", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
		rootAttrs.addAttribute(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "schemaLocation", "xsi:schemaLocation", "CDATA", "ispm-config.xsd");
		pHandler.startElement(XmlConfigurationHandler.NS, "ispm-config", "ispm-config", rootAttrs);
		final List<TLocalRepository> localRepos = pConfiguration.getLocalRepositories();
		final List<TRemoteRepository> remoteRepos = pConfiguration.getRemoteRepositories();
		final List<IsInstance> isInstances = pConfiguration.getIsInstanceDirs();
		final String NS = XmlConfigurationHandler.NS;
		final FailableBiConsumer<TPlugin,String,SAXException> pluginWriter = (p,s) -> {
			write(pHandler, p, s);
		};
		if (localRepos != null  &&  !localRepos.isEmpty()) {
			pHandler.startElement(NS, "localRepositories", "localRepositories", NO_ATTRS);
			for (TLocalRepository localRepo : localRepos) {
				pluginWriter.accept(localRepo, "localRepository");
			}
			pHandler.endElement(NS, "localRepositories", "localRepositories");
		}
		if (remoteRepos != null  &&  !remoteRepos.isEmpty()) {
			pHandler.startElement(NS, "remoteRepositories", "remoteRepositories", NO_ATTRS);
			for (TRemoteRepository remoteRepo : remoteRepos) {
				pluginWriter.accept(remoteRepo, "remoteRepository");
			}
			pHandler.endElement(NS, "remoteRepositories", "remoteRepositories");
		}
		if (isInstances != null  &&  !isInstances.isEmpty()) {
			pHandler.startElement(NS, "isInstanceDirs", "isInstanceDirs", NO_ATTRS);
			for (IsInstance isInstance : isInstances) {
				write(pHandler, isInstance);
			}
			pHandler.endElement(NS, "isInstanceDirs", "isInstanceDirs");
		}
		pHandler.endElement(XmlConfigurationHandler.NS, "ispm-config", "ispm-config");
		pHandler.endDocument();
	}

	private void write(final ContentHandler pHandler, IsInstance pIsInstance) throws SAXException {
		final String NS = XmlConfigurationHandler.NS;
		final AttributesImpl instAttrs = new AttributesImpl();
		instAttrs.addAttribute("", "dir", "dir", "CDATA", pIsInstance.getDir());
		instAttrs.addAttribute("", "id", "id", "CDATA", pIsInstance.getId());
		if (pIsInstance.isDefault()) {
			instAttrs.addAttribute("", "default", "default", "CDATA", "true");
		}
		if (pIsInstance.getWmVersion() != null) {
			instAttrs.addAttribute("", "wmVersion", "wmVersion", "CDATA", pIsInstance.getWmVersion());
		}
		if (pIsInstance.getWmHomeDir() != null  &&  !"../../..".equals(pIsInstance.getWmHomeDir())) {
			instAttrs.addAttribute("", "wmHomeDir", "wmHomeDir", "CDATA", pIsInstance.getWmHomeDir());
		}
		if (pIsInstance.getIsHomeDir() != null  &&  !"../..".equals(pIsInstance.getIsHomeDir())) {
			instAttrs.addAttribute("", "isHomeDir", "isHomeDir", "CDATA", pIsInstance.getIsHomeDir());
		}
		if (pIsInstance.getPackageDir() != null  &&  !"packages".equals(pIsInstance.getPackageDir())) {
			instAttrs.addAttribute("", "packageDir", "packageDir", "CDATA", pIsInstance.getPackageDir());
		}
		if (pIsInstance.getConfigDir() != null  &&  !"config".equals(pIsInstance.getConfigDir())) {
			instAttrs.addAttribute("", "configDir", "configDir", "CDATA", pIsInstance.getConfigDir());
		}
		pHandler.startElement(NS, "isInstanceDir", "isInstanceDir", instAttrs);
		pHandler.endElement(NS, "isInstanceDir", "isInstanceDir");
	}

	public void write(ContentHandler pHandler, TLocalRepository pLocalRepository) throws SAXException {
		write(pHandler, pLocalRepository, "localRepository");
	}

	public void write(ContentHandler pHandler, TRemoteRepository pRemoteRepository) throws SAXException {
		write(pHandler, pRemoteRepository, "remoteRepository");
	}

	private void write(final ContentHandler pHandler, TPlugin pPlugin, String pElementName) throws SAXException {
		final String NS = XmlConfigurationHandler.NS;
		final AttributesImpl repoAttrs = new AttributesImpl();
		repoAttrs.addAttribute("", "id", "id", "CDATA", pPlugin.getId());
		repoAttrs.addAttribute("", "type", "type", "CDATA", pPlugin.getType());
		pHandler.startElement(NS, pElementName, pElementName, repoAttrs);
		final Map<String,String> properties = pPlugin.getProperties();
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
					pHandler.startElement(NS, "property", "property", propAttrs);
					if (useElement) {
						final char[] chars = value.toCharArray();
						pHandler.characters(chars, 0, chars.length);
					}
					pHandler.endElement(NS, "property", "property");
				}
			}
		}
		pHandler.endElement(NS, pElementName, pElementName);
	}


	protected void copy(final InputSource pSource, final ContentHandlerProxy ch) throws ParserConfigurationException,
			SAXException, SAXNotRecognizedException, SAXNotSupportedException, IOException {
		final SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setValidating(false);
		spf.setNamespaceAware(true);
		final SAXParser sp = spf.newSAXParser();
		final XMLReader xr = sp.getXMLReader();
		xr.setContentHandler(ch);
		if (ch instanceof LexicalHandler) {
			xr.setProperty("http://xml.org/sax/properties/lexical-handler", ch);
		}
		xr.parse(pSource);
	}
}
