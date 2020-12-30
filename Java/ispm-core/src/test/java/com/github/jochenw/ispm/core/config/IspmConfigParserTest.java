package com.github.jochenw.ispm.core.config;

import static org.junit.jupiter.api.Assertions.*;

import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.junit.jupiter.api.Test;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

import com.github.jochenw.ispm.core.config.TIspmConfiguration.TInstance;
import com.github.jochenw.ispm.core.config.TIspmConfiguration.TLocalRepo;
import com.github.jochenw.ispm.core.config.TIspmConfiguration.TPlugin;
import com.github.jochenw.ispm.core.config.TIspmConfiguration.TRemoteRepo;

class IspmConfigParserTest {

	@Test
	void testEmptyConfig1() throws Exception {
		final String XML = "<ispm-config xmlns='" + IspmConfigParser.NS + "'/>";
		final InputSource isource = new InputSource(new StringReader(XML));
		isource.setSystemId("testEmptyConfig1");
		final TIspmConfiguration config = IspmConfigParser.parse(isource);
		assertNotNull(config);
		assertTrue(config.getPlugins().isEmpty());
		assertTrue(config.getLocalRepos().isEmpty());
		assertTrue(config.getRemoteRepos().isEmpty());
		assertTrue(config.getInstances().isEmpty());
		assertEquals(XML, asXml(config));
	}

	protected String asXml(TIspmConfiguration pConfiguration) throws Exception {
		final StringWriter sw = new StringWriter();
		final Result result = new StreamResult(sw);
		final Constructor<IspmConfigWriter> constructor = IspmConfigWriter.class.getDeclaredConstructor(ContentHandler.class);
		final SAXTransformerFactory stf = (SAXTransformerFactory) TransformerFactory.newInstance();
		final TransformerHandler th = stf.newTransformerHandler();
		th.setResult(result);
		th.getTransformer().setOutputProperty(OutputKeys.INDENT, "no");
		th.getTransformer().setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		th.getTransformer().setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		constructor.setAccessible(true);
		final IspmConfigWriter icw = constructor.newInstance(th);
		icw.write(pConfiguration);
		final String xml = sw.toString().replace('\"', '\'');
		final Pattern pattern = Pattern.compile("^\\<\\?.*\\?\\>(\\<ispm-config.*)$");
		final Matcher matcher = pattern.matcher(xml);
		if (matcher.matches()) {
			return matcher.group(1);
		} else {
			return xml;
		}
	}
	
	@Test
	void testEmptyConfig2() {
		for (String element : Arrays.asList("plugins", "localRepos", "remoteRepos", "instances")) {
			final String XML = "<ispm-config xmlns='" + IspmConfigParser.NS + "'><" + element + "/></ispm-config>";
			final InputSource isource = new InputSource(new StringReader(XML));
			isource.setSystemId("testEmptyConfig2" + element);
			final TIspmConfiguration config = IspmConfigParser.parse(isource);
			assertNotNull(config);
			assertTrue(config.getPlugins().isEmpty());
			assertTrue(config.getLocalRepos().isEmpty());
			assertTrue(config.getRemoteRepos().isEmpty());
			assertTrue(config.getInstances().isEmpty());
		}
	}

	@Test
	void testEmptyConfig3() {
		final String XML = "<ispm-config xmlns='" + IspmConfigParser.NS + "'><plugins/><localRepos/><remoteRepos/><instances/></ispm-config>";
		final InputSource isource = new InputSource(new StringReader(XML));
		isource.setSystemId("testEmptyConfig3");
		final TIspmConfiguration config = IspmConfigParser.parse(isource);
		assertNotNull(config);
		assertTrue(config.getPlugins().isEmpty());
		assertTrue(config.getLocalRepos().isEmpty());
		assertTrue(config.getRemoteRepos().isEmpty());
		assertTrue(config.getInstances().isEmpty());
	}

	@Test
	void testPlugin() {
		final String XML0 = "<ispm-config xmlns='" + IspmConfigParser.NS + "'>\n"
				          + "  <plugins>\n"
				          + "    <plugin class='SomeClass'/>\n"
				          + "    <plugin script='SomeScript'/>\n"
				          + "  </plugins>\n"
				          + "</ispm-config>";
		final InputSource isource0 = new InputSource(new StringReader(XML0));
		isource0.setSystemId("testPlugin0");
		final TIspmConfiguration config0 = IspmConfigParser.parse(isource0);
		final List<TPlugin> plugins = config0.getPlugins();
		assertEquals(2, plugins.size());
		final TPlugin pl0 = plugins.get(0);
		assertEquals("SomeClass", pl0.getClassName());
		assertEquals("", pl0.getScript());
		assertTrue(pl0.getProperties().isEmpty());
		final TPlugin pl1 = plugins.get(1);
		assertEquals("", pl1.getClassName());
		assertEquals("SomeScript", pl1.getScript());
		assertTrue(pl1.getProperties().isEmpty());

		final String XML1 = "<ispm-config xmlns='" + IspmConfigParser.NS + "'>\n"
		          + "  <plugins>\n"
		          + "    <plugin/>\n"
		          + "  </plugins>\n"
		          + "</ispm-config>";
		final InputSource isource1 = new InputSource(new StringReader(XML1));
		isource1.setSystemId("testPlugin1");
		try {
			IspmConfigParser.parse(isource1);
			fail("Expected Exception");
		} catch (UndeclaredThrowableException ute) {
			final SAXParseException spe = (SAXParseException) ute.getCause();
			assertTrue(spe.getMessage().endsWith("Either of the attributes plugin/@class, or plugin/@script is required."));
		}

		final String XML2 = "<ispm-config xmlns='" + IspmConfigParser.NS + "'>\n"
		          + "  <plugins>\n"
		          + "    <plugin class='SomeClass' script='SomeScript'/>\n"
		          + "  </plugins>\n"
		          + "</ispm-config>";
		final InputSource isource2 = new InputSource(new StringReader(XML2));
		isource1.setSystemId("testPlugin2");
		try {
			IspmConfigParser.parse(isource2);
			fail("Expected Exception");
		} catch (UndeclaredThrowableException ute) {
			final SAXParseException spe = (SAXParseException) ute.getCause();
			assertTrue(spe.getMessage().endsWith("The attributes plugin/@class, and plugin/@script are mutually exclusive."));
		}
	}

	@Test
	void testPluginProperties() {
		final String XML0 = "<ispm-config xmlns='" + IspmConfigParser.NS + "'>\n"
		          + "  <plugins>\n"
		          + "    <plugin class='SomeClass'>\n"
		          + "      <property key='prop0' value='value0'/>\n"
		          + "      <property key='prop1'>value1</property>\n"
		          + "    </plugin>"
		          + "    <plugin script='SomeScript'/>\n"
		          + "  </plugins>\n"
		          + "</ispm-config>";
		final InputSource isource0 = new InputSource(new StringReader(XML0));
		isource0.setSystemId("testProperties0");
		final TIspmConfiguration config0 = IspmConfigParser.parse(isource0);
		final List<TPlugin> plugins = config0.getPlugins();
		assertEquals(2, plugins.size());
		final TPlugin pl0 = plugins.get(0);
		assertEquals("SomeClass", pl0.getClassName());
		assertEquals("", pl0.getScript());
		assertEquals(2, pl0.getProperties().size());
		assertEquals("value0", pl0.getProperty("prop0"));
		final TPlugin pl1 = plugins.get(1);
		assertEquals("", pl1.getClassName());
		assertEquals("SomeScript", pl1.getScript());
		assertTrue(pl1.getProperties().isEmpty());
	}

	@Test
	void testInstances() throws Exception {
		final String XML0 = "<ispm-config xmlns='" + IspmConfigParser.NS + "'>"
		          +   "<instances>"
		          +     "<instance id='inst0' baseDir='SomeDir'/>"
		          +     "<instance id='inst1' baseDir='OtherDir' wmHomeDir='f:\\SoftwareAG\\webMethods103'"
		                       + " packagesDir='SomePackagesDir' configDir='SomeConfigDir' logsDir='SomeLogsDir'/>"
		          +   "</instances>"
		          + "</ispm-config>";
		final InputSource isource0 = new InputSource(new StringReader(XML0));
		isource0.setSystemId("testProperties0");
		final TIspmConfiguration config0 = IspmConfigParser.parse(isource0);
		final Map<String,TInstance> instances = config0.getInstances();
		assertEquals(2, instances.size());
		final TInstance inst0 = instances.get("inst0");
		assertEquals("inst0", inst0.getId());
		assertEquals("SomeDir", inst0.getBaseDir());
		assertEquals("${baseDir}/../../..", inst0.getWmHomeDir());
		assertEquals("${baseDir}/packages", inst0.getPackagesDir());
		assertEquals("${baseDir}/config", inst0.getConfigDir());
		assertEquals("${baseDir}/logs", inst0.getLogsDir());
		assertEquals(XML0, asXml(config0));
		final TInstance inst1 = instances.get("inst1");
		assertEquals("inst1", inst1.getId());
		assertEquals("OtherDir", inst1.getBaseDir());
		assertEquals("SomeConfigDir", inst1.getConfigDir());
		assertEquals("SomePackagesDir", inst1.getPackagesDir());
		assertEquals("SomeLogsDir", inst1.getLogsDir());
		assertEquals("f:\\SoftwareAG\\webMethods103", inst1.getWmHomeDir());
	}

	@Test
	void testLocalRepos() throws Exception {
		final String XML = "<ispm-config xmlns='" + IspmConfigParser.NS + "'>"
				           + "<localRepos>"
				             + "<localRepo id='wm99' dir='F:/GIT-DEV99'/>"
				             + "<localRepo id='wm103' dir='F:/GIT-DEV103' layout='other'/>"
				           + "</localRepos>"
				         + "</ispm-config>";
		final InputSource isource0 = new InputSource(new StringReader(XML));
		isource0.setSystemId("testLocalRepos");
		final TIspmConfiguration config = IspmConfigParser.parse(isource0);
		assertEquals(2, config.getLocalRepos().size());
		final TLocalRepo repo0 = config.getLocalRepos().get("wm99");
		assertEquals("wm99", repo0.getId());
		assertEquals("F:/GIT-DEV99", repo0.getBaseDir());
		assertEquals("default", repo0.getLayout());
		final TLocalRepo repo1 = config.getLocalRepo("wm103");
		assertEquals("wm103", repo1.getId());
		assertEquals("F:/GIT-DEV103", repo1.getBaseDir());
		assertEquals("other", repo1.getLayout());
		assertEquals(XML, asXml(config));
	}

	@Test
	void testRemoteRepos() throws Exception {
		final String XML = "<ispm-config xmlns='" + IspmConfigParser.NS + "'>"
		                     + "<remoteRepos>"
		                       + "<remoteRepo id='azure' url='https://dev.azure.com/'/>"
		                       + "<remoteRepo id='github' url='https://github.com/' handler='gh'/>"
		                     + "</remoteRepos>"
		                   + "</ispm-config>";
        final InputSource isource0 = new InputSource(new StringReader(XML));
        isource0.setSystemId("testRemoteRepos");
        final TIspmConfiguration config = IspmConfigParser.parse(isource0);
        assertEquals(2, config.getRemoteRepos().size());
        final TRemoteRepo repo0 = config.getRemoteRepo("azure");
        assertEquals("azure", repo0.getId());
        assertEquals("https://dev.azure.com/", repo0.getUrl());
        assertEquals("default", repo0.getHandler());
        final TRemoteRepo repo1 = config.getRemoteRepo("github");
        assertEquals("github", repo1.getId());
        assertEquals("https://github.com/", repo1.getUrl());
        assertEquals("gh", repo1.getHandler());
        assertEquals(XML, asXml(config));
	}
}
