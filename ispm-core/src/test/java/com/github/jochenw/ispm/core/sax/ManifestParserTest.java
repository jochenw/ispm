package com.github.jochenw.ispm.core.sax;

import static org.junit.Assert.assertEquals;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.xml.sax.InputSource;

import com.github.jochenw.afw.core.util.Holder;
import com.github.jochenw.afw.core.util.Sax;
import com.github.jochenw.ispm.core.sax.ManifestParser.Listener;

import org.junit.Assert;


public class ManifestParserTest {
	private static final String NO_RESULTS_MANIFEST =
			"<?xml version='1.0' encoding='UTF-8'?>\r\n" + 
			"\r\n" + 
			"<Values version='2.0'>\r\n" + 
			"  <value name='enabled'>yes</value>\r\n" + 
			"  <value name='system_package'>no</value>\r\n" + 
			"  <value name='version'>1.0</value>\r\n" + 
			"  <null name='startup_services'/>\r\n" + 
			"  <null name='shutdown_services'/>\r\n" + 
			"  <null name='replication_services'/>\r\n" + 
			"  <null name='requires'/>\r\n" + 
			"  <null name='listACL'/>\r\n" + 
			"  <value name='webappLoad'>yes</value>\r\n" + 
			"</Values>\r\n";

	@Test
	public void testNoResult() {
		final Holder<String> versionHolder = new Holder<String>();
		final Listener listener = new Listener() {
			@Override
			public void startupService(String pService) {
				throw new IllegalStateException("Unexpected startup service: " + pService);
			}

			@Override
			public void shutdownService(String pService) {
				throw new IllegalStateException("Unexpected shutdown service: " + pService);
			}

			@Override
			public void version(String pVersion) {
				versionHolder.set(pVersion);
			}

			@Override
			public void requires(String pPackageName, String pVersion) {
				throw new IllegalStateException("Unexpected dependeny: " + pPackageName);
			}

			
		};
		final InputSource isource = new InputSource(new StringReader(NO_RESULTS_MANIFEST));
		final ManifestParser parser = new ManifestParser(listener);
		Sax.parse(isource, parser);
		assertEquals("1.0", versionHolder.get());
	}

	private static final String SOME_RESULTS_MANIFEST =
			"<?xml version=\"1.0\" encoding=\"utf-8\"?><Values version=\"2.0\">\r\n" + 
			"  <value name=\"enabled\">yes</value>\r\n" + 
			"  <value name=\"system_package\">no</value>\r\n" + 
			"  <value name=\"version\">2.7.4</value>\r\n" + 
			"  <record name=\"startup_services\" javaclass=\"com.wm.util.Values\">\r\n" + 
			"    <null name=\"com.foo.mypkg.admin:startup\"/>\r\n" + 
			"  </record>\r\n" + 
			"  <null name=\"shutdown_services\"/>\r\n" + 
			"  <null name=\"replication_services\"/>\r\n" + 
			"  <record name=\"requires\" javaclass=\"com.wm.util.Values\">\r\n" + 
			"    <value name=\"WxConfig\">1.7</value>\r\n" + 
			"    <value name=\"Pkg0\">2.2</value>\r\n" + 
			"    <value name=\"Pkg1\">2.2</value>\r\n" + 
			"    <value name=\"Pkg2\">2.1</value>\r\n" + 
			"    <value name=\"Pkg3\">2.2</value>\r\n" + 
			"  </record>\r\n" + 
			"  <value name=\"listACL\">Default</value>\r\n" + 
			"  <value name=\"webappLoad\">yes</value>\r\n" + 
			"</Values>";

	@Test
	public void testSomeResults() throws Exception {
		final Holder<String> versionHolder = new Holder<String>();
		final Reader r = new StringReader(SOME_RESULTS_MANIFEST);
		final List<String> startupServices = new ArrayList<>();
		final List<String> shutdownServices = new ArrayList<>();
		final List<String> requires = new ArrayList<String>();
		final Listener listener = new Listener() {
			@Override
			public void version(String pVersion) {
				if (versionHolder.get() != null) {
					throw new IllegalStateException("Expected only one version string");
				}
				versionHolder.set(pVersion);
			}

			@Override
			public void startupService(String pService) {
				startupServices.add(pService);
			}

			@Override
			public void shutdownService(String pService) {
				shutdownServices.add(pService);
			}

			@Override
			public void requires(String pPackageName, String pVersion) {
				requires.add(pPackageName);
				requires.add(pVersion);
			}
			
		};
		final ManifestParser parser = new ManifestParser(listener);
		Sax.parse(new InputSource(r), parser);
		Assert.assertEquals(1, startupServices.size());
		Assert.assertTrue(shutdownServices.isEmpty());
		Assert.assertEquals("com.foo.mypkg.admin:startup", startupServices.get(0));
		Assert.assertEquals(10, requires.size());
		Assert.assertEquals("WxConfig", requires.get(0));
		Assert.assertEquals("1.7", requires.get(1));
		Assert.assertEquals("Pkg0", requires.get(2));
		Assert.assertEquals("2.2", requires.get(3));
		Assert.assertEquals("Pkg1", requires.get(4));
		Assert.assertEquals("2.2", requires.get(5));
		Assert.assertEquals("Pkg2", requires.get(6));
		Assert.assertEquals("2.1", requires.get(7));
		Assert.assertEquals("Pkg3", requires.get(8));
		Assert.assertEquals("2.2", requires.get(9));
	}
}
