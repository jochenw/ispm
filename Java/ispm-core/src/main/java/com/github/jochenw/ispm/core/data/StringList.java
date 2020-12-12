package com.github.jochenw.ispm.core.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class StringList extends ArrayList<String> implements Serializable {
	private static final long serialVersionUID = -5729312034254940815L;

	public StringList() {
		super();
	}

	public StringList(Collection<? extends String> c) {
		super(c);
	}

	public StringList(int initialCapacity) {
		super(initialCapacity);
	}
}
