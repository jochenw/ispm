package com.github.jochenw.ispm.core.actions;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.github.jochenw.afw.core.inject.ComponentFactoryBuilder.Module;
import com.github.jochenw.afw.core.inject.simple.SimpleComponentFactoryBuilder;
import com.github.jochenw.afw.core.io.IReadable;
import com.github.jochenw.afw.core.scripts.IScriptEngine.Script;
import com.github.jochenw.afw.core.util.Scripts;

class ScriptPluginsTest {
	@Test
	void testDefaultLocalRepoLayout() {
		final Path path = Paths.get("src/main/resources/com/github/jochenw/ispm/core/components/script-plugins/DefaultLocalRepoLayout.groovy");
		final Script script = Scripts.compile(IReadable.of(path), null);
		final Module module = script.call(Collections.emptyMap());
		assertNotNull(module);
		new SimpleComponentFactoryBuilder().module(module).build();
	}

}
