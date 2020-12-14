package com.github.jochenw.ispm.core.config;

import java.net.URL;
import java.nio.file.Path;

import org.apache.logging.log4j.util.Strings;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.github.jochenw.afw.core.util.Sax;
import com.github.jochenw.ispm.core.compile.AbstractContentHandler;
import com.github.jochenw.ispm.core.config.TIspmConfiguration.TInstance;
import com.github.jochenw.ispm.core.config.TIspmConfiguration.TLocalRepo;
import com.github.jochenw.ispm.core.config.TIspmConfiguration.TPropertiesContainer;
import com.github.jochenw.ispm.core.config.TIspmConfiguration.TPlugin;
import com.github.jochenw.ispm.core.config.TIspmConfiguration.TRemoteRepo;


public class IspmConfigParser {
	public static String NS = "http://namespaces.github.com/jochenw/ispm/core/config/1.0.0";

	public static class Handler extends AbstractContentHandler {
		private TIspmConfiguration configuration, config;
		private boolean inPlugins, inLocalRepos, inRemoteRepos, inInstances;
		private TPropertiesContainer currentContainer = null;
		private String attributeKey, attributeValueAttr, attributeValue;

		@Override
		public void startDocument() throws SAXException {
			super.startDocument();
			config = new TIspmConfiguration();
			currentContainer = null;
			inPlugins = inLocalRepos = inRemoteRepos = inInstances = false;
			attributeKey = attributeValue = attributeValueAttr = null;
		}

		@Override
		public void endDocument() throws SAXException {
			super.endDocument();
			configuration = config;
		}

		protected String getAttribute(Attributes pAttrs, String pName) {
			return pAttrs.getValue(pName);
		}

		protected String getAttribute(Attributes pAttrs, String pName, String pDefault) {
			final String value = pAttrs.getValue(pName);
			if (value == null) {
				return pDefault;
			} else {
				return value;
			}
		}

		protected String requireAttribute(Attributes pAttrs, String pName) throws SAXException {
			final String value = getAttribute(pAttrs, pName);
			if (value == null  ||  value.length() == 0) {
				throw error("The attribute @" + pName + " is missing, or empty.");
			}
			return value;
		}

		@Override
		public void startElement(String pUri, String pLocalName, String pQName, Attributes pAttrs) throws SAXException {
			super.startElement(pUri, pLocalName, pQName, pAttrs);
			switch (getLevel()) {
			  case 1:
				  assertElement(NS, "ispm-config", pUri, pLocalName);
				  break;
			  case 2:
				  if (isElement(NS, "plugins", pUri, pLocalName)) {
					  inPlugins = true;
				  } else if (isElement(NS, "localRepos", pUri, pLocalName)) {
					  inLocalRepos = true;
				  } else if (isElement(NS, "remoteRepos", pUri, pLocalName)) {
					  inRemoteRepos = true;
				  } else if (isElement(NS, "instances", pUri, pLocalName)) {
					  inInstances = true;
				  } else {
					  throw error("Expected " + asQName(NS, "plugins|localRepos|remoteRepos|instances")
				                  + " at level 1, got " + asQName(pUri, pLocalName));
				  }
				  break;
			  case 3:
				  if (inPlugins) {
					  if (isElement(NS, "plugin", pUri, pLocalName)) {
						  final String className = getAttribute(pAttrs, "class", "");
						  final String script = getAttribute(pAttrs, "script", "");
						  if (Strings.isEmpty(className)  &&  Strings.isEmpty(script)) {
							  throw error("Either of the attributes plugin/@class, or plugin/@script is required.");
						  }
						  if (!Strings.isBlank(className)  &&  !Strings.isEmpty(script)) {
							  throw error("The attributes plugin/@class, and plugin/@script are mutually exclusive.");
						  }
						  currentContainer = new TPlugin(getDocumentLocator(), className, script);
					  } else {
						  throw error("Expected element " + asQName(NS, "plugin") + ", got " + asQName(pUri, pLocalName));
					  }
				  } else if (inLocalRepos) {
					  if (isElement(NS, "localRepo", pUri, pLocalName)) {
						  final String id = requireAttribute(pAttrs, "id");
						  final String dir = requireAttribute(pAttrs, "dir");
						  final String layout = getAttribute(pAttrs, "layout", "default");
						  currentContainer = new TLocalRepo(getDocumentLocator(), id, dir, layout);
					  } else {
						  throw error("Expected element " + asQName(NS, "localRepo") + ", got " + asQName(pUri, pLocalName));
					  }
				  } else if (inRemoteRepos) {
					  if (isElement(NS, "remoteRepo", pUri, pLocalName)) {
						  final String id = requireAttribute(pAttrs, "id");
						  final String url = requireAttribute(pAttrs, "url");
						  final String handler = requireAttribute(pAttrs, "handler");
						  currentContainer = new TRemoteRepo(getDocumentLocator(), id, url, handler);
					  } else {
						  throw error("Expected element " + asQName(NS, "remoteRepo") + ", got " + asQName(pUri, pLocalName));
					  }
				  } else if (inInstances) {
					  if (isElement(NS, "instance", pUri, pLocalName)) {
						  final String id = requireAttribute(pAttrs, "id");
						  final String baseDir = requireAttribute(pAttrs, "baseDir");
						  final String wmHomeDir = getAttribute(pAttrs, "wmHomeDir", "${baseDir}/../../..");
						  final String packagesDir = getAttribute(pAttrs, "packagesDir", "${baseDir}/packages");
						  final String configDir = getAttribute(pAttrs, "configDir", "${baseDir}/config");
						  final String logsDir = getAttribute(pAttrs, "logsDir", "${baseDir}/logs");
						  currentContainer = new TInstance(getDocumentLocator(), id, baseDir, wmHomeDir, packagesDir, configDir, logsDir);
					  } else {
						  throw error("Expected element " + asQName(NS, "instance") + ", got " + asQName(pUri, pLocalName));
					  }
				  } else {
					  throw new IllegalStateException("Expected inPlugins|inLocalRepos|inRemoteRepos|inInstances=true, got all false.");
				  }
				  break;
			  case 4:
				  if (isElement(NS, "property", pUri, pLocalName)) {
					  attributeKey = requireAttribute(pAttrs, "key");
					  attributeValueAttr = getAttribute(pAttrs, "value");
					  attributeValue = null;
					  startCollecting(3, (s) -> attributeValue = s);
				  } else {
					  throw error("Expected element " + asQName(NS, "property") + ", got " + asQName(pUri, pLocalName));
				  }
				  break;
			  default:
				  throw error("Unexpected element level: " + getLevel());
			}
		}

		@Override
		public void endElement(String pUri, String pLocalName, String pQName) throws SAXException {
			super.endElement(pUri, pLocalName, pQName);
			switch(getLevel()) {
			case 3:
				if (attributeValueAttr == null) {
					if (Strings.isEmpty(attributeValue)) {
						throw error("A property element must either have an attribute property/@value, or a non-empty body.");
					}
				} else {
					if (!Strings.isEmpty(attributeValue)) {
						throw error("A property element's attribute property/@value, and a non-empty body are mutually exclusive.");
					}
					attributeValue = attributeValueAttr;
				}
				if (currentContainer.setProperty(attributeKey, attributeValue) != null) {
					throw error("Duplicate property key: " + attributeKey);
				}
				attributeKey = attributeValue = attributeValueAttr = null;
				break;
			case 2:
				if (currentContainer == null) {
					throw new IllegalStateException("Expected currentContainer != null");
				}
				if (currentContainer instanceof TPlugin) {
					config.addPlugin((TPlugin) currentContainer);
				} else if (currentContainer instanceof TLocalRepo) {
					config.addLocalRepo((TLocalRepo) currentContainer);
				} else if (currentContainer instanceof TRemoteRepo) {
					config.addRemoteRepo((TRemoteRepo) currentContainer);
				} else if (currentContainer instanceof TInstance) {
					config.addInstance((TInstance) currentContainer);
				} else {
					throw new IllegalStateException("Invalid container type: " + currentContainer.getClass().getName());
				}
				break;
			case 1:
				inPlugins = inLocalRepos = inRemoteRepos = inInstances = false;
				break;
			case 0:
				break;
			default:
				  throw error("Unexpected element level: " + getLevel());
			}
		}

		protected TIspmConfiguration getConfiguration() {
			if (configuration == null) {
				throw new NullPointerException("Configuration is not yet available. Did you parse?");
			}
			return configuration;
		}
		
	}

	public static TIspmConfiguration parse(InputSource pSource) {
		final Handler handler = new Handler();
		Sax.parse(pSource, handler);
		return handler.getConfiguration();
	}

	public static TIspmConfiguration parse(Path pPath) {
		final Handler handler = new Handler();
		Sax.parse(pPath, handler);
		return handler.getConfiguration();
	}

	public static TIspmConfiguration parse(URL pUrl) {
		final Handler handler = new Handler();
		Sax.parse(pUrl, handler);
		return handler.getConfiguration();
	}
}
