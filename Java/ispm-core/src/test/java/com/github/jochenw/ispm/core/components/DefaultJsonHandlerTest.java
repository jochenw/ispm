package com.github.jochenw.ispm.core.components;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.json.JsonArray;
import javax.json.JsonObject;

import org.junit.jupiter.api.Test;

import com.github.jochenw.afw.core.util.Streams;

class DefaultJsonHandlerTest {

	@Test
	void testParse() throws Exception {
		final JsonObject object = readGitRepos();
		assertNotNull(object);
		assertEquals(2, object.getJsonNumber("count").intValue());
		final JsonArray array = object.getJsonArray("value");
		assertNotNull(array);
		assertEquals(2, array.size());
		final JsonObject repo0 = (JsonObject) array.get(0);
		assertNotNull(repo0);
		final String customerId = "d19a6f8f-a2c3-49c6-9321-14d2d819cc01";
		final String id0 = "4832145c-96d2-4810-a6a3-009d1413a407";
		final String name0 = "Foo-INT-Repo1";
		assertEquals(id0, repo0.getJsonString("id").getString());
		assertEquals(name0, repo0.getJsonString("name").getString());
		assertEquals("https://dev.azure.com/customer/" + customerId + "/_apis/git/repositories/" + id0, repo0.getJsonString("url").getString());
		final String id1 = "410dd5e3-2ec7-4b63-9c84-04f2aeae4987";
		final String name1 = "Foo-INT-Repo2";
		final JsonObject repo1 = (JsonObject) array.get(1);
		assertEquals(id1, repo1.getJsonString("id").getString());
		assertEquals(name1, repo1.getJsonString("name").getString());
		assertEquals("https://dev.azure.com/customer/" + customerId + "/_apis/git/repositories/" + id1, repo1.getJsonString("url").getString());
		assertNotNull(repo1);
		assertEquals("Foo-INT-Repo2", repo1.getJsonString("name").getString());
		
	}

	private JsonObject readGitRepos() throws IOException {
		final DefaultJsonHandler djh = new DefaultJsonHandler();
		final URL url = getClass().getResource("git-repos.json");
		final JsonObject object;
		try (InputStream in = url.openStream()) {
			object = djh.parse(in);
		}
		return object;
	}

	@Test
	void testWrite() throws Exception {
		final JsonObject object = readGitRepos();
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		new DefaultJsonHandler().write(object, baos);
		final String got = baos.toString(StandardCharsets.UTF_8.name());
		final URL url = getClass().getResource("git-repos-short.json");
		final String JSON;
		try (InputStream in = url.openStream()) {
			JSON = Streams.read(in, StandardCharsets.UTF_8);
		}
		assertEquals(JSON, got);
	}

}
