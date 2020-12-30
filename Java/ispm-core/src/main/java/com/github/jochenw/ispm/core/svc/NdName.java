package com.github.jochenw.ispm.core.svc;

public class NdName {
	private final String qName;
	private final String namespace;
	private final String localName;

	private NdName(String pQName, String pNamespace, String pLocalName) {
		super();
		qName = pQName;
		namespace = pNamespace;
		localName = pLocalName;
	}

	public static NdName of(String pSvcName) {
		final int offset = pSvcName.indexOf(':');
		if (offset == -1) {
			throw new IllegalArgumentException("Invalid service name (Missing ':' character): " + pSvcName);
		}
		final String namespace = pSvcName.substring(0, offset);
		final String name = pSvcName.substring(offset+1);
		return new NdName(pSvcName, namespace, name);
	}

	public String getNamespace() { return namespace; }
	public String getLocalName() { return localName; }
	public String getQName() { return qName; }
}
