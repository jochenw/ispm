package com.github.jochenw.ispm.core.svc;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.github.jochenw.ispm.core.Tests;

class IspmApplicationTest {
	@Test
	void testCreateApplication() {
		assertNotNull(Tests.getApplication());
	}
}
