package com.github.jochenw.ispm.core.config;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Strings;
import com.github.jochenw.ispm.core.config.TIspmConfiguration.TPropertiesContainer;

public class IspmConfigWriter {
	public static void write(TIspmConfiguration pConfiguration, Path pConfigPath) {
		try (OutputStream os = Files.newOutputStream(pConfigPath)) {
			write(pConfiguration, new StreamResult(os));
		} catch (IOException e) {
			throw Exceptions.show(e);
		}
	}

	public static void write(TIspmConfiguration pConfiguration, Result pResult) {
		final IspmConfigWriter icw = new IspmConfigWriter(newContentHandler(pResult));
		icw.write(pConfiguration);
	}

	private final ContentHandler ch;

	private IspmConfigWriter(ContentHandler pContentHandler) {
		ch = pContentHandler;
	}

	protected void startElement(String pElement, String... pAttributes) throws SAXException {
		final AttributesImpl attrs = new AttributesImpl();
		if (pAttributes != null) {
			for (int i = 0;  i < pAttributes.length;  i += 2) {
				final String value = pAttributes[i+1];
				final String name = pAttributes[i];
				if (value != null) {
					attrs.addAttribute(XMLConstants.NULL_NS_URI, name, name, "CDATA", value);
				}
			}
		}
		ch.startElement(IspmConfigParser.NS, pElement, pElement, attrs);
	}

	protected void endElement(String pElement) throws SAXException {
		ch.endElement(IspmConfigParser.NS, pElement, pElement);
	}

	private static TransformerHandler newContentHandler(Result pResult) {
		try {
			final SAXTransformerFactory stf = (SAXTransformerFactory) TransformerFactory.newInstance();
			final TransformerHandler th = stf.newTransformerHandler();
			th.setResult(pResult);
			th.getTransformer().setOutputProperty(OutputKeys.INDENT, "yes");
			th.getTransformer().setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			return th;
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	protected void writeProperties(TPropertiesContainer pContainer) {
		pContainer.getProperties().forEach((key, value) -> {
			if (value != null) {
				try {
					if (value.length() < 20  &&  value.indexOf('\n') == -1) {
						startElement("property", "key", key, "value", value);
						endElement("property");
					} else {
						startElement("property", "key", key);
						final char[] chars = value.toCharArray();
						ch.characters(chars, 0, chars.length);
						endElement("property");
					}
				} catch (Throwable t) {
					throw Exceptions.show(t);
				}
			}
		});
	}

	protected String nullIfDefault(String pValue, String pDefault) {
		if (pValue.equals(pDefault)) {
			return null;
		} else {
			return pValue;
		}
	}

	public void write(TIspmConfiguration pConfiguration) {
		try {
			ch.startDocument();
			startElement("ispm-config");
			if (!pConfiguration.getPlugins().isEmpty()) {
				startElement("plugins");
				pConfiguration.forEachPlugin((plugin) -> {
					startElement("plugin", "class", Strings.notEmpty(plugin.getClassName(), null),
							               "script", Strings.notEmpty(plugin.getScript(), null));
					writeProperties(plugin);
					endElement("plugin");
				});
				endElement("plugins");
			}
			if (!pConfiguration.getLocalRepos().isEmpty()) {
				startElement("localRepos");
				pConfiguration.forEachLocalRepo((id, localRepo) -> {
					startElement("localRepo",
							     "id", localRepo.getId(),
							     "dir", localRepo.getBaseDir(),
							     "layout", nullIfDefault(localRepo.getLayout(), "default"));
					writeProperties(localRepo);
					endElement("localRepo");
				});
				endElement("localRepos");
			}
			if (!pConfiguration.getRemoteRepos().isEmpty()) {
				startElement("remoteRepos");
				pConfiguration.forEachRemoteRepo((id, remoteRepo) -> {
					startElement("remoteRepo",
							     "id", remoteRepo.getId(),
							     "url", remoteRepo.getUrl(),
							     "handler", nullIfDefault(remoteRepo.getHandler(), "default"));
					writeProperties(remoteRepo);
					endElement("remoteRepo");
				});
				endElement("remoteRepos");
			}
			if (!pConfiguration.getInstances().isEmpty()) {
				startElement("instances");
				pConfiguration.forEachInstance((id, inst) -> {
					startElement("instance", "id", inst.getId(), "baseDir", inst.getBaseDir(),
							     "wmHomeDir", nullIfDefault(inst.getWmHomeDir(), "${baseDir}/../../.."),
							     "packagesDir", nullIfDefault(inst.getPackagesDir(), "${baseDir}/packages"),
							     "configDir", nullIfDefault(inst.getConfigDir(), "${baseDir}/config"),
							     "logsDir", nullIfDefault(inst.getLogsDir(), "${baseDir}/logs"));
					writeProperties(inst);
					endElement("instance");
				});
				endElement("instances");
			}
			endElement("ispm-config");
			ch.endDocument();
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}
}
