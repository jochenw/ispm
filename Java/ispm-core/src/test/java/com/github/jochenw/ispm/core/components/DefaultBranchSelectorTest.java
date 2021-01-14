package com.github.jochenw.ispm.core.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.github.jochenw.afw.core.inject.IComponentFactory;
import com.github.jochenw.afw.core.inject.simple.SimpleComponentFactoryBuilder;
import com.github.jochenw.afw.core.log.ILogFactory;
import com.github.jochenw.afw.core.log.simple.SimpleLogFactory;

class DefaultBranchSelectorTest {
	@Test
	void testValid() {
		final DefaultBranchSelector dbs = newBranchSelector();
		final String latestBranch = dbs.getBranch(Arrays.asList("remotes/origin/Releases/1.0", "remotes/origin/Releases/1.1", "remotes/origin/releases/2.0", "remotes/origin/Features/3.0"));
		assertEquals("remotes/origin/releases/2.0", latestBranch);
	}

	private DefaultBranchSelector newBranchSelector() {
		final IComponentFactory scf = new SimpleComponentFactoryBuilder()
				.module((b) -> {
					b.bind(ILogFactory.class).toInstance(new SimpleLogFactory(System.out));
					b.bind(IBranchSelector.class).to(DefaultBranchSelector.class);
				})
				.build();
		final DefaultBranchSelector dbs = (DefaultBranchSelector) scf.requireInstance(IBranchSelector.class);
		return dbs;
	}

	@Test
	void testInvalid() {
		final DefaultBranchSelector dbs = newBranchSelector();
		final String latestBranch = dbs.getBranch(Arrays.asList("remotes/origin/Features/3.0"));
		assertNull(latestBranch);
	}

}
