package com.github.jochenw.ispm.core.components;

import java.io.InputStream;
import java.io.OutputStream;

import javax.json.Json;
import javax.json.JsonObject;

public class DefaultJsonHandler implements IJsonHandler {
	@Override
	public JsonObject parse(InputStream pIn) {
		return Json.createReader(pIn).readObject();
	}

	@Override
	public void write(JsonObject pObject, OutputStream pOut) {
		Json.createWriter(pOut).writeObject(pObject);
	}
}
