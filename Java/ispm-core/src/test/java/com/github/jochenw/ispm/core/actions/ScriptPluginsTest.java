package com.github.jochenw.ispm.core.actions;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.github.jochenw.afw.core.inject.ComponentFactoryBuilder.Module;
import com.github.jochenw.afw.core.inject.IComponentFactory;
import com.github.jochenw.afw.core.inject.simple.SimpleComponentFactory;
import com.github.jochenw.afw.core.inject.simple.SimpleComponentFactoryBuilder;
import com.github.jochenw.afw.core.io.IReadable;
import com.github.jochenw.afw.core.log.ILogFactory;
import com.github.jochenw.afw.core.log.simple.SimpleLogFactory;
import com.github.jochenw.afw.core.scripts.IScriptEngine.Script;
import com.github.jochenw.afw.core.util.Scripts;
import com.github.jochenw.ispm.core.model.ILocalRepoLayout;
import com.github.jochenw.ispm.core.model.IRemoteRepoHandler;

class ScriptPluginsTest {
	@Test
	void testDefaultLocalRepoLayout() {
		final IComponentFactory scf = newComponentFactory("DefaultLocalRepoLayout.groovy");
		scf.requireInstance(ILocalRepoLayout.class, "default");
	}

	protected IComponentFactory newComponentFactory(String pScript) {
		final Path path = Paths.get("src/main/resources/com/github/jochenw/ispm/core/components/script-plugins/" + pScript);
		final Script script = Scripts.compile(IReadable.of(path), null);
		final Module module0 = (b) -> {
			b.bind(ILogFactory.class).toInstance(new SimpleLogFactory(System.out));
		};
		final Module module1 = script.call(Collections.emptyMap());
		assertNotNull(module1);
		final IComponentFactory scf = new SimpleComponentFactoryBuilder().modules(module0, module1).build();
		return scf;
	}

	@Test
	void testDefaultRemoteRepoLayout() {
		final IComponentFactory scf = newComponentFactory("DefaultRemoteRepoHandler.groovy");
		final IRemoteRepoHandler remoteRepoHandler = scf.requireInstance(IRemoteRepoHandler.class, "default");
		assertSame(remoteRepoHandler, scf.requireInstance(IRemoteRepoHandler.class, "azure"));
	}
}
