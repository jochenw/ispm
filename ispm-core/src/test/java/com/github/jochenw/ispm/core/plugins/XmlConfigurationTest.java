package com.github.jochenw.ispm.core.plugins;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.github.jochenw.afw.core.log.app.IAppLog;
import com.github.jochenw.afw.core.log.app.SystemOutAppLog;
import com.github.jochenw.afw.core.util.Sax;
import com.github.jochenw.ispm.core.plugins.XmlConfiguration.IsInstance;
import com.github.jochenw.ispm.core.plugins.XmlConfiguration.TLocalRepository;
import com.github.jochenw.ispm.core.plugins.XmlConfiguration.TPlugin;
import com.github.jochenw.ispm.core.plugins.XmlConfiguration.TRemoteRepository;

public class XmlConfigurationTest {
	@Test
	public void testEmptyConfiguration() throws Exception {
		final XmlConfiguration xmlConfiguration = new XmlConfiguration();
		parseAndValidate(xmlConfiguration, false);
	}

	protected void parseAndValidate(final XmlConfiguration pExpect, boolean pShow) throws TransformerException, SAXException, IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		pExpect.save(baos);
		if (pShow) {
			baos.writeTo(System.out);
		}
		final XmlConfiguration xmlConfiguration2 = parse(new ByteArrayInputStream(baos.toByteArray()));
		assertEqual(pExpect, xmlConfiguration2);
	}

	protected XmlConfiguration parse(InputStream pIn) {
		final SystemOutAppLog appLog = new SystemOutAppLog();
		appLog.setLevel(IAppLog.Level.TRACE);
		final XmlConfigurationHandler xfh = new XmlConfigurationHandler(appLog);
		Sax.parse(pIn, xfh);
		return xfh.getXmlConfiguration();
	}
	
	@Test
	public void testEsbConfiguration() throws Exception {
		final IsInstance wm99Instance = new IsInstance(null, true, "wm99", "F:/SoftwareAG/webMethods99", null, null, null, null, null);
		final IsInstance wm912Instance = new IsInstance(null, false, "wm912", "F:/SoftwareAG/webMethods912", null, null, null, null, null);
		final IsInstance wm103Instance = new IsInstance(null, false, "wm103", "F:/SoftwareAG/webMethods103", null, null, null, null, null);
		final List<IsInstance> instanceDirs = Arrays.asList(wm99Instance, wm912Instance, wm103Instance);
		final TLocalRepository gitDev99Repo = new TLocalRepository(null, "gd99", "default", new HashMap<>());
		gitDev99Repo.getProperties().put("dir", "f:/GIT-DEV99");
		gitDev99Repo.getProperties().put("wmVersion", "wm99");
		final TLocalRepository gitDev912Repo = new TLocalRepository(null, "gd912", "default", new HashMap<>());
		gitDev912Repo.getProperties().put("dir", "f:/GIT-DEV912");
		gitDev912Repo.getProperties().put("wmVersion", "wm912");
		final TLocalRepository gitDev103Repo = new TLocalRepository(null, "gd912", "default", new HashMap<>());
		gitDev103Repo.getProperties().put("dir", "f:/GIT-DEV103");
		gitDev103Repo.getProperties().put("wmVersion", "wm103");
		final List<TLocalRepository> localRepos = Arrays.asList(gitDev99Repo, gitDev912Repo, gitDev103Repo);
		final TRemoteRepository azureRepo = new TRemoteRepository(null, "azure", "azure", new HashMap<>());
		final List<TRemoteRepository> remoteRepos = Collections.singletonList(azureRepo);
		final XmlConfiguration xmlConfiguration = new XmlConfiguration(instanceDirs, localRepos, remoteRepos);
		parseAndValidate(xmlConfiguration, false);
	}

	@Test
	public void testAddLocalRepository() throws Exception {
		final XmlConfiguration emptyConfiguration = new XmlConfiguration();
		final TLocalRepository gitDev99Repo = new TLocalRepository(null, "gd99", "default", new HashMap<>());
		gitDev99Repo.getProperties().put("dir", "f:/GIT-DEV99");
		gitDev99Repo.getProperties().put("wmVersion", "wm99");
		final XmlConfiguration resultConfiguration = emptyConfiguration.add(gitDev99Repo);
		final XmlConfiguration expectConfiguration = new XmlConfiguration(Collections.emptyList(),
				                                                          Collections.singletonList(gitDev99Repo),
				                                                          Collections.emptyList());
		assertEqual(expectConfiguration, resultConfiguration);
	}

	@Test
	public void testAddRemoteRepository() throws Exception {
		final XmlConfiguration emptyConfiguration = new XmlConfiguration();
		final TRemoteRepository ghRepo = new TRemoteRepository(null, "gh", "default", new HashMap<>());
		ghRepo.getProperties().put("url", "https://github.com/acmeCorporation");
		final XmlConfiguration resultConfiguration = emptyConfiguration.add(ghRepo);
		final XmlConfiguration expectConfiguration = new XmlConfiguration(Collections.emptyList(),
				                                                          Collections.emptyList(),
				                                                          Collections.singletonList(ghRepo));
		assertEqual(expectConfiguration, resultConfiguration);
	}

	protected void validate(InputStream pIn) {
		new XmlConfiguration().validate(pIn);
	}

	protected void assertEqual(XmlConfiguration pExpect, XmlConfiguration pGot) {
		final List<TLocalRepository> expectLocalRepositories = pExpect.getLocalRepositories();
		final List<TLocalRepository> gotLocalRepositories = pGot.getLocalRepositories();
		final List<TRemoteRepository> expectRemoteRepositories = pExpect.getRemoteRepositories();
		final List<TRemoteRepository> gotRemoteRepositories = pGot.getRemoteRepositories();
		final List<IsInstance> expectInstances = pExpect.getIsInstanceDirs();
		final List<IsInstance> gotInstances = pGot.getIsInstanceDirs();
		assertEquals(expectLocalRepositories.size(), gotLocalRepositories.size());
		assertEquals(pExpect.getRemoteRepositories().size(), pGot.getRemoteRepositories().size());
		assertEquals(pExpect.getIsInstanceDirs().size(), pGot.getIsInstanceDirs().size());
		if (!expectLocalRepositories.isEmpty()) {
			for (int i = 0;  i < expectLocalRepositories.size();  i++) {
				assertEqual(expectLocalRepositories.get(i), gotLocalRepositories.get(i));
			}
		}
		if (!expectRemoteRepositories.isEmpty()) {
			for (int i = 0;  i < expectRemoteRepositories.size();  i++) {
				assertEqual(expectRemoteRepositories.get(i), gotRemoteRepositories.get(i));
			}
		}
		if (!expectInstances.isEmpty()) {
			for (int i = 0;  i < expectInstances.size();  i++) {
				assertEqual(expectInstances.get(i), gotInstances.get(i));
			}
		}
	}

	protected void assertEqual(TLocalRepository pExpect, TLocalRepository pGot) {
		assertEqual((TPlugin) pExpect, (TPlugin) pGot);
	}

	protected void assertEqual(TRemoteRepository pExpect, TRemoteRepository pGot) {
		assertEqual((TPlugin) pExpect, (TPlugin) pGot);
	}

	protected void assertEqual(IsInstance pExpect, IsInstance pGot) {
		assertEqual(pExpect.getId(), pGot.getId());
		assertEquals(pExpect.isDefault(), pGot.isDefault());
		assertEquals(pExpect.getDir(), pGot.getDir());
		assertEqual(pExpect.getWmVersion(), pGot.getWmVersion());
		assertEqual(pExpect.getWmHomeDir(), pGot.getWmHomeDir());
		assertEqual(pExpect.getIsHomeDir(), pGot.getIsHomeDir());
		assertEqual(pExpect.getPackageDir(), pGot.getPackageDir());
		assertEqual(pExpect.getConfigDir(), pGot.getConfigDir());
	}

	protected void assertEqual(String pExpect, String pGot) {
		if (pExpect == null) {
			assertNull(pGot);
		} else {
			assertEquals(pExpect, pGot);
		}
	}

	protected void assertEqual(TPlugin pExpect, TPlugin pGot) {
		assertEquals(pExpect.getId(), pGot.getId());
		assertEquals(pExpect.getType(), pGot.getType());
		final Map<String,String> expectProperties = pExpect.getProperties();
		final Map<String,String> gotProperties = pGot.getProperties();
		for (Map.Entry<String,String> en : expectProperties.entrySet()) {
			final String key = en.getKey();
			final String value = en.getValue();
			assertEquals(value, gotProperties.get(key));
		}
		assertEquals(expectProperties.size(), gotProperties.size());
	}
}
