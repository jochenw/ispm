package com.github.jochenw.ispm.core.sax;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

public class ContentHandlerProxy implements ContentHandler, LexicalHandler {
	private final @Nonnull ContentHandler ch;
	private final @Nullable LexicalHandler lh;

	public ContentHandlerProxy(@Nonnull ContentHandler pCh) {
		ch = pCh;
		if (pCh instanceof LexicalHandler) {
			lh = (LexicalHandler) pCh;
		} else {
			lh = null;
		}
	}
	
	/**
	 * @param pName
	 * @param pPublicId
	 * @param pSystemId
	 * @throws SAXException
	 * @see org.xml.sax.ext.LexicalHandler#startDTD(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void startDTD(String pName, String pPublicId, String pSystemId) throws SAXException {
		if (lh != null) {
			lh.startDTD(pName, pPublicId, pSystemId);
		}
	}

	/**
	 * @throws SAXException
	 * @see org.xml.sax.ext.LexicalHandler#endDTD()
	 */
	public void endDTD() throws SAXException {
		if (lh != null) {
			lh.endDTD();
		}
	}

	/**
	 * @param pName
	 * @throws SAXException
	 * @see org.xml.sax.ext.LexicalHandler#startEntity(java.lang.String)
	 */
	public void startEntity(String pName) throws SAXException {
		if (lh != null) {
			lh.startEntity(pName);
		}
	}

	/**
	 * @param pName
	 * @throws SAXException
	 * @see org.xml.sax.ext.LexicalHandler#endEntity(java.lang.String)
	 */
	public void endEntity(String pName) throws SAXException {
		if (lh != null) {
			lh.endEntity(pName);
		}
	}

	/**
	 * @throws SAXException
	 * @see org.xml.sax.ext.LexicalHandler#startCDATA()
	 */
	public void startCDATA() throws SAXException {
		if (lh != null) {
			lh.startCDATA();
		}
	}

	/**
	 * @throws SAXException
	 * @see org.xml.sax.ext.LexicalHandler#endCDATA()
	 */
	public void endCDATA() throws SAXException {
		if (lh != null) {
			lh.endCDATA();
		}
	}

	/**
	 * @param pCh
	 * @param pStart
	 * @param pLength
	 * @throws SAXException
	 * @see org.xml.sax.ext.LexicalHandler#comment(char[], int, int)
	 */
	public void comment(char[] pCh, int pStart, int pLength) throws SAXException {
		if (lh != null) {
			lh.comment(pCh, pStart, pLength);
		}
	}

	/**
	 * @param pLocator
	 * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
	 */
	public void setDocumentLocator(Locator pLocator) {
		ch.setDocumentLocator(pLocator);
	}

	/**
	 * @throws SAXException
	 * @see org.xml.sax.ContentHandler#startDocument()
	 */
	public void startDocument() throws SAXException {
		ch.startDocument();
	}

	/**
	 * @throws SAXException
	 * @see org.xml.sax.ContentHandler#endDocument()
	 */
	public void endDocument() throws SAXException {
		ch.endDocument();
	}

	/**
	 * @param pPrefix
	 * @param pUri
	 * @throws SAXException
	 * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String, java.lang.String)
	 */
	public void startPrefixMapping(String pPrefix, String pUri) throws SAXException {
		ch.startPrefixMapping(pPrefix, pUri);
	}

	/**
	 * @param pPrefix
	 * @throws SAXException
	 * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
	 */
	public void endPrefixMapping(String pPrefix) throws SAXException {
		ch.endPrefixMapping(pPrefix);
	}

	/**
	 * @param pUri
	 * @param pLocalName
	 * @param pQName
	 * @param pAtts
	 * @throws SAXException
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String pUri, String pLocalName, String pQName, Attributes pAtts) throws SAXException {
		ch.startElement(pUri, pLocalName, pQName, pAtts);
	}

	/**
	 * @param pUri
	 * @param pLocalName
	 * @param pQName
	 * @throws SAXException
	 * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void endElement(String pUri, String pLocalName, String pQName) throws SAXException {
		ch.endElement(pUri, pLocalName, pQName);
	}

	/**
	 * @param pCh
	 * @param pStart
	 * @param pLength
	 * @throws SAXException
	 * @see org.xml.sax.ContentHandler#characters(char[], int, int)
	 */
	public void characters(char[] pCh, int pStart, int pLength) throws SAXException {
		ch.characters(pCh, pStart, pLength);
	}

	/**
	 * @param pCh
	 * @param pStart
	 * @param pLength
	 * @throws SAXException
	 * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
	 */
	public void ignorableWhitespace(char[] pCh, int pStart, int pLength) throws SAXException {
		ch.ignorableWhitespace(pCh, pStart, pLength);
	}

	/**
	 * @param pTarget
	 * @param pData
	 * @throws SAXException
	 * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String, java.lang.String)
	 */
	public void processingInstruction(String pTarget, String pData) throws SAXException {
		ch.processingInstruction(pTarget, pData);
	}

	/**
	 * @param pName
	 * @throws SAXException
	 * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
	 */
	public void skippedEntity(String pName) throws SAXException {
		ch.skippedEntity(pName);
	}
}
