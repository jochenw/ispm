package com.github.jochenw.ispm.core.components;

import java.io.InputStream;
import java.io.OutputStream;

import javax.json.JsonObject;

public interface IJsonHandler {
	public JsonObject parse(InputStream pIn);
	public void write(JsonObject pObject, OutputStream pOut);

}
