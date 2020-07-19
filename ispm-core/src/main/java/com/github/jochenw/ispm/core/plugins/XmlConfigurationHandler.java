package com.github.jochenw.ispm.core.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.LocatorImpl;

import com.github.jochenw.afw.core.log.app.IAppLog;
import com.github.jochenw.afw.core.util.Sax;
import com.github.jochenw.afw.core.util.Sax.AbstractContentHandler;
import com.github.jochenw.ispm.core.plugins.XmlConfiguration.IsInstance;
import com.github.jochenw.ispm.core.plugins.XmlConfiguration.TLocalRepository;
import com.github.jochenw.ispm.core.plugins.XmlConfiguration.TRemoteRepository;

class XmlConfigurationHandler extends AbstractContentHandler {
	public static final String NS = "http://namespaces.github.com/jochenw/ispm/core/1.0.0";
	private final List<IsInstance> instances = new ArrayList<>();
	private final List<TLocalRepository> localRepositories = new ArrayList<>();
	private final List<TRemoteRepository> remoteRepositories = new ArrayList<>();
	private final Map<String,String> properties = new HashMap<>();
	private final IAppLog appLog;
	private boolean inIsInstances, inLocalRepositories, inRemoteRepositories;
	private boolean parsed;
	private boolean inLocalRepository, inRemoteRepository, inIsInstance;
	private String id, type;
	private Locator locator;

	protected XmlConfigurationHandler(IAppLog pAppLog) {
		appLog = pAppLog;
	}
	
	@Override
	public void startDocument() throws SAXException {
		instances.clear();
		localRepositories.clear();
		remoteRepositories.clear();
		properties.clear();
		inIsInstances = false;
		inLocalRepositories = false;
		inRemoteRepositories = false;
		parsed = false;
		inLocalRepository = false;
		inRemoteRepository = false;
		inIsInstance = false;
		id = null;
		type = null;
	}

	@Override
	public void endDocument() throws SAXException {
		parsed = true;
	}

	protected String requireAttribute(Attributes pAttrs, String pName) throws SAXException {
		final String value = requireAttribute(pAttrs, pName, null);
		if (value == null) {
			throw error("Missing attribute: @" + pName);
		}
		return value;
	}

	protected String requireAttribute(Attributes pAttrs, String pName, String pDefault) throws SAXException {
		final String value = pAttrs.getValue(pName);
		if (value == null) {
			return pDefault;
		}
		if (value.length() == 0) {
			throw error("Empty attribute: @" + pName);
		}
		return value;
	}

	protected void assertElement(String pExpectedLocalName, String pUri,
			                     String pLocalName) throws SAXException {
		if (!NS.equals(pUri)  ||  !pExpectedLocalName.equals(pLocalName)) {
			throw error("Expected " + Sax.asQName(NS, pExpectedLocalName)
				+ ", got " + Sax.asQName(pUri, pLocalName));
		}
	}

	protected boolean isElement(String pExpectedLocalName, String pUri,
            String pLocalName) throws SAXException {
		return NS.equals(pUri)  &&  pExpectedLocalName.equals(pLocalName);
	}

	@Override
	public void startElement(String pUri, String pLocalName, String pQName, Attributes pAttrs) throws SAXException {
		if (!NS.equals(pUri)) {
			throw error("Expected namespace " + NS + ", got " + pUri);
		}
		switch (incLevel()) {
		case 1:
			assertElement("ispm-config", pUri, pLocalName);
			break;
		case 2:
			if (isElement("localRepositories", pUri, pLocalName)) {
				inLocalRepositories = true;
			} else if (isElement("remoteRepositories", pUri, pLocalName)) {
				inRemoteRepositories = true;
			} else if (isElement("isInstanceDirs", pUri, pLocalName)) {
				inIsInstances = true;
			} else {
				throw error("Expected localRepositories|remoteRepositories|isInstanceDirs, got "
						+ Sax.asQName(pUri, pLocalName));
			}
			break;
		case 3:
		    if (inLocalRepositories) {
		    	assertElement("localRepository", pUri, pLocalName);
		    	locator = getLocator();
		    	id = requireAttribute(pAttrs, "id");
		    	type = requireAttribute(pAttrs, "type");
		    	inLocalRepository = true;
		    } else if (inRemoteRepositories) {
		    	assertElement("remoteRepository", pUri, pLocalName);
		    	locator = getLocator();
		    	id = requireAttribute(pAttrs, "id");
		    	type = requireAttribute(pAttrs, "type");
		    	inRemoteRepository = true;
		    } else if (inIsInstances) {
		    	assertElement("isInstanceDir", pUri, pLocalName);
		    	final String id = requireAttribute(pAttrs, "id");
		    	final boolean isDefault = Boolean.parseBoolean(pAttrs.getValue("default"));
		    	final String dir = requireAttribute(pAttrs, "dir");
		    	final String wmVersion = pAttrs.getValue("wmVersion");
		    	final String isHomeDir = pAttrs.getValue("isHomeDir");
		    	final String wmHomeDir = pAttrs.getValue("wmHomeDir");
		    	final String packageDir = pAttrs.getValue("packageDir");
		    	final String configDir = pAttrs.getValue("configDir");
		    	instances.add(new IsInstance(getLocator(), isDefault, id, dir, wmVersion, wmHomeDir, isHomeDir, packageDir, configDir));
		    	inIsInstance = true;
		    } else {
		    	throw error("Expected inLocalRepositories || inRemoteRepositories || inIsInstances");
		    }
		    break;
		  case 4:
			assertElement("property", pUri, pLocalName);
			final String key = requireAttribute(pAttrs, "key");
			if (properties.containsKey(key)) {
				throw error("Duplicate property key: " + key);
			}
			final boolean empty = Boolean.parseBoolean(pAttrs.getValue("empty"));
			String value = pAttrs.getValue("value");
			if (value == null) {
				final Locator loc = getLocator();
				startTextElement((s) -> {
					if ("".equals(s)) {
						if (!empty) {
							final String msg = Sax.asLocalizedMessage(loc, "Property value is empty.");
							appLog.warn(msg);
						}
					}
					properties.put(key, s);
				});
			} else {
				properties.put(key, value);
			}
		}
	}

	private Locator getLocator() {
		final Locator lc = getDocumentLocator();
		final Locator loc = lc == null ? null : new LocatorImpl(lc);
		return loc;
	}

	@Override
	public void endElement(String pUri, String pLocalName, String pQName) throws SAXException {
		if (!NS.equals(pUri)) {
			throw error("Expected namespace " + NS + ", got " + pUri);
		}
		final int level = getLevel();
		decLevel();
		switch (level) {
		  case 1:
		    assertElement("ispm-config", pUri, pLocalName);
			break;
		  case 2:
			if (inLocalRepositories) {
				inLocalRepositories = false;
			} else if (inRemoteRepositories) {
				inRemoteRepositories = false;
			} else if (inIsInstances) {
				inIsInstances = false;
			} else {
		    	throw error("Expected inLocalRepositories || inRemoteRepositories || inIsInstances");
			}
			break;
		  case 3:
			if (inLocalRepository) {
				localRepositories.add(new TLocalRepository(locator, id, type, properties));
				inLocalRepository = false;
				id = null;
				type = null;
				properties.clear();
			} else if (inRemoteRepository) {
				remoteRepositories.add(new TRemoteRepository(locator, id, type, properties));
				inRemoteRepository = false;
				id = null;
				type = null;
				properties.clear();
			} else if (inIsInstance) {
				inIsInstance = false;
			}
			break;
		  case 4:
			assertElement("property", pUri, pLocalName);
			break;
	      default:
	    	throw error("Unexpected element level: " + getLevel());
		}
	}

	public XmlConfiguration getXmlConfiguration() {
		if (!parsed) {
			throw new IllegalStateException("Configuration is only available after parsing.");
		}
		return new XmlConfiguration(instances, localRepositories, remoteRepositories);
	}
}