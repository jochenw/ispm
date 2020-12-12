package com.github.jochenw.ispm.core.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import com.wm.data.IData;


public class DocumentList extends ArrayList<IData> implements Serializable {
	private static final long serialVersionUID = 3245574913236690065L;

	public DocumentList() {
		super();
	}

	public DocumentList(Collection<? extends IData> arg0) {
		super(arg0);
	}

	public DocumentList(int arg0) {
		super(arg0);
	}
}
