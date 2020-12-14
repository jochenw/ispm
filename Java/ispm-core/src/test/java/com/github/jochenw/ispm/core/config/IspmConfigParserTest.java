package com.github.jochenw.ispm.core.config;

import static org.junit.jupiter.api.Assertions.*;

import java.io.StringReader;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

import com.github.jochenw.ispm.core.config.TIspmConfiguration.TInstance;
import com.github.jochenw.ispm.core.config.TIspmConfiguration.TPlugin;

class IspmConfigParserTest {

	@Test
	void testEmptyConfig1() {
		final String XML = "<ispm-config xmlns='" + IspmConfigParser.NS + "'/>";
		final InputSource isource = new InputSource(new StringReader(XML));
		isource.setSystemId("testEmptyConfig1");
		final TIspmConfiguration config = IspmConfigParser.parse(isource);
		assertNotNull(config);
		assertTrue(config.getPlugins().isEmpty());
		assertTrue(config.getLocalRepos().isEmpty());
		assertTrue(config.getRemoteRepos().isEmpty());
		assertTrue(config.getInstances().isEmpty());
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
	void testInstances() {
		final String XML0 = "<ispm-config xmlns='" + IspmConfigParser.NS + "'>\n"
		          + "  <instances>\n"
		          + "    <instance id='inst0' baseDir='SomeDir'/>\n"
		          + "    <instance id='inst1' baseDir='OtherDir' wmHomeDir='f:\\SoftwareAG\\webMethods103' configDir='SomeConfigDir'"
		          + "              packagesDir='SomePackagesDir' logsDir='SomeLogsDir'/>\n"
		          + "  </instances>\n"
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
		final TInstance inst1 = instances.get("inst1");
		assertEquals("inst1", inst1.getId());
		assertEquals("OtherDir", inst1.getBaseDir());
		assertEquals("SomeConfigDir", inst1.getConfigDir());
		assertEquals("SomePackagesDir", inst1.getPackagesDir());
		assertEquals("SomeLogsDir", inst1.getLogsDir());
		assertEquals("f:\\SoftwareAG\\webMethods103", inst1.getWmHomeDir());
	}
}
