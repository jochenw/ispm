package com.github.jochenw.ispm.core.actions;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import com.github.jochenw.afw.core.inject.ComponentFactoryBuilder.Module;
import com.github.jochenw.afw.core.inject.IComponentFactory;
import com.github.jochenw.afw.core.inject.simple.SimpleComponentFactory;
import com.github.jochenw.afw.core.inject.simple.SimpleComponentFactoryBuilder;
import com.github.jochenw.afw.core.io.IReadable;
import com.github.jochenw.afw.core.log.ILogFactory;
import com.github.jochenw.afw.core.log.simple.SimpleLogFactory;
import com.github.jochenw.afw.core.props.DefaultPropertyFactory;
import com.github.jochenw.afw.core.props.IPropertyFactory;
import com.github.jochenw.afw.core.scripts.IScriptEngine.Script;
import com.github.jochenw.afw.core.util.Executor;
import com.github.jochenw.afw.core.util.HttpConnector;
import com.github.jochenw.afw.core.util.Scripts;
import com.github.jochenw.ispm.core.components.DefaultBranchSelector;
import com.github.jochenw.ispm.core.components.DefaultJsonHandler;
import com.github.jochenw.ispm.core.components.GitExeGitHandler;
import com.github.jochenw.ispm.core.components.IBranchSelector;
import com.github.jochenw.ispm.core.components.IGitHandler;
import com.github.jochenw.ispm.core.components.IJsonHandler;
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
			b.bind(IJsonHandler.class).to(DefaultJsonHandler.class);
			b.bind(IGitHandler.class).to(GitExeGitHandler.class);
			b.bind(IBranchSelector.class).to(DefaultBranchSelector.class);
			b.bind(IPropertyFactory.class).toInstance(new DefaultPropertyFactory(new Properties()));
			b.bind(HttpConnector.class);
			b.bind(Executor.class);
			b.bind(Executor.class, "git");
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
