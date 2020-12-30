package com.github.jochenw.ispm.core.svc;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import com.github.jochenw.afw.core.inject.ComponentFactoryBuilder.Module;
import com.github.jochenw.afw.core.inject.IComponentFactory;
import com.github.jochenw.afw.core.util.Files;
import com.github.jochenw.afw.core.util.Tests;
import com.github.jochenw.ispm.core.actions.AddLocalRepoAction;
import com.github.jochenw.ispm.core.config.TIspmConfiguration;
import com.github.jochenw.ispm.core.model.IspmConfiguration;

public class IspmApplicationTest {
	public static IspmApplication setup(Class<?> pTestClass) {
		return setup(pTestClass, (Module[]) null);
	}

	public static IspmApplication setup(Class<?> pTestClass, Module... pModules) {
		final String dirName = pTestClass.getSimpleName();
		final Path sourceDir = Paths.get("src/test/resources/IspmApplicationTest");
		final Path testDir = Tests.setupTestDirectory(pTestClass, sourceDir);
		final Path instanceDir = testDir.resolve("wmHomeDir/IntegrationServer/instances/default");
		final Path repoDir = testDir.resolve("repo");
		assertTrue(Files.isDirectory(repoDir));
		final Path projectsDir = repoDir.resolve("projects");
		assertTrue(Files.isDirectory(projectsDir));
		final IspmApplication ispmApp = new IspmApplication(instanceDir, "WxIspm") {
			@Override
			protected Path getBuiltinPluginsDir() {
				return Paths.get("src/main/resources/com/github/jochenw/ispm/core/components/script-plugins");
			}
			
		};
		IspmApplication.setInstance(ispmApp);
		final IComponentFactory componentFactory = ispmApp.getComponentFactory();
		final IspmConfiguration ispmConfiguration = ispmApp.getIspmConfiguration();
		assertNotNull(ispmConfiguration);
		assertNull(ispmConfiguration.getLocalRepo("repo"));
		final TIspmConfiguration tIspmConfiguration = ispmApp.getTIspmConfiguration();
		final AddLocalRepoAction action = componentFactory.requireInstance(AddLocalRepoAction.class);
		action.addLocalRepo("test", "default", projectsDir.toAbsolutePath());
		final IComponentFactory componentFactory2 = ispmApp.getComponentFactory();
		assertNotSame(componentFactory, componentFactory2);
		final IspmConfiguration ispmConfiguration2 = ispmApp.getIspmConfiguration();
		assertNotSame(ispmConfiguration, ispmConfiguration2);
		assertNotNull(ispmConfiguration2.getLocalRepo("test"));
		assertSame(tIspmConfiguration, ispmApp.getTIspmConfiguration());
		return ispmApp;
	}

	@Test
	void testCreateInstance() {
		final IspmApplication ispmApp = setup(getClass());
		final IComponentFactory componentFactory = IspmApplication.getInstance().getComponentFactory();
		assertNotNull(componentFactory);
		assertSame(componentFactory, componentFactory.requireInstance(IComponentFactory.class));
		assertSame(ispmApp, componentFactory.requireInstance(IspmApplication.class));
	}

}
