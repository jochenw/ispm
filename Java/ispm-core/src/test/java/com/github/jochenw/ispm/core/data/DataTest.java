package com.github.jochenw.ispm.core.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.softwareag.util.IDataMap;


class DataTest {
	@Test
	void testGetValueMapString() {
		final IDataMap dataMap = newDataMap();
		assertEquals(42, ((Integer) Data.getValue(dataMap, "answer")).intValue());
		assertEquals("bar", Data.getValue(dataMap, "foo"));
		assertSame(Boolean.TRUE, Data.getValue(dataMap, "check"));
		assertNull(Data.getValue(dataMap, "noSuchValue"));
	}

	protected IDataMap newDataMap() {
		final IDataMap map = new IDataMap();
		map.put("answer", Integer.valueOf(42));
		map.put("foo", "bar");
		map.put("check", Boolean.TRUE);
		return map;
	}
}
