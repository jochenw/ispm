package wx.ispm.impl.util;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.github.jochenw.ispm.core.data.Data;
import com.softwareag.util.IDataMap;
// --- <<IS-END-IMPORTS>> ---

public final class lists

{
	// ---( internal utility methods )---

	final static lists _instance = new lists();

	static lists _newInstance() { return new lists(); }

	static lists _cast(Object o) { return (lists)o; }

	// ---( server methods )---




	public static final void addToDocumentList (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(addToDocumentList)>> ---
		// @sigtype java 3.5
		// [i] object:0:required documentList
		// [i] record:0:required document
		// [o] object:0:required documentList
		final IDataMap map = new IDataMap(pipeline);
		final Object documentListObject = map.get("documentList");
		final List<IData> documentList;
		if (documentListObject == null) {
			documentList = new ArrayList<>();
		} else {
			if (!(documentListObject instanceof List)) {
				throw new IllegalArgumentException("Expected document list for parameter documentList, got "
						+ documentListObject.getClass().getName());
			}
			@SuppressWarnings("unchecked")
			final List<IData> lst = (List<IData>) documentListObject;
			documentList = lst;
		}
		final IData document = Data.requireIData(map, "document");
		documentList.add(document);
		map.put("documentList", documentList);
			
		// --- <<IS-END>> ---

                
	}



	public static final void addToStringList (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(addToStringList)>> ---
		// @sigtype java 3.5
		// [i] object:0:required stringList
		// [i] field:0:required string
		// [o] object:0:required stringList
		final IDataMap map = new IDataMap(pipeline);
		final String string = Data.getString(map, "string");
		if (string == null) {
			throw new NullPointerException("Missing parameter: string");
		}
		final Object stringListObject = map.get("stringList");
		final List<String> stringList;
		if (stringListObject == null) {
			stringList = new ArrayList<>();
		} else {
			if (stringListObject instanceof List) {
				@SuppressWarnings("unchecked")
				final List<String> lst = (List<String>) stringListObject;
				stringList = lst;
			} else {
				throw new IllegalArgumentException("Expected string list for parameter stringList, got "
						+ stringListObject.getClass().getName());
			}
		}
		stringList.add(string);
		map.put("stringList", stringList);
		
			
		// --- <<IS-END>> ---

                
	}



	public static final void createDocumentList (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(createDocumentList)>> ---
		// @sigtype java 3.5
		// [o] object:0:required documentList
		final IDataMap map = new IDataMap(pipeline);
		map.put("documentList", new ArrayList<>());
		// --- <<IS-END>> ---

                
	}



	public static final void createStringList (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(createStringList)>> ---
		// @sigtype java 3.5
		// [o] object:0:required stringList
		final IDataMap map = new IDataMap(pipeline);
		map.put("stringList", new ArrayList<>());
		// --- <<IS-END>> ---

                
	}



	public static final void toIsDocumentList (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(toIsDocumentList)>> ---
		// @sigtype java 3.5
		// [i] object:0:required documentList
		// [o] record:1:required documents
		final IDataMap map = new IDataMap(pipeline);
		final Object documentListObject = map.get("documentList");
		final List<IData> documentList;
		if (documentListObject == null) {
			documentList = Collections.emptyList();
		} else {
			if (!(documentListObject instanceof List)) {
				throw new IllegalArgumentException("Expected document list for parameter documentList, got "
						+ documentListObject.getClass().getName());
			}
			@SuppressWarnings("unchecked")
			final List<IData> lst = (List<IData>) documentListObject;
			documentList = lst;
		}
		map.put("documents", documentList.toArray(new IData[documentList.size()]));		
			
		// --- <<IS-END>> ---

                
	}



	public static final void toIsStringList (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(toIsStringList)>> ---
		// @sigtype java 3.5
		// [i] object:0:required stringList
		// [o] field:1:required strings
		final IDataMap map = new IDataMap(pipeline);
		final Object stringListObject = map.get("stringList");
		final List<String> stringList;
		if (stringListObject == null) {
			stringList = Collections.emptyList();
		} else {
			if (!(stringListObject instanceof List)) {
				throw new IllegalArgumentException("Expected string list for parameter stringList, got "
						+ stringListObject.getClass().getName());
			}
			@SuppressWarnings("unchecked")
			final List<String> lst = (List<String>) stringListObject;
			stringList = lst;
		}
		map.put("strings", stringList.toArray(new String[stringList.size()]));		
			
		// --- <<IS-END>> ---

                
	}

	// --- <<IS-START-SHARED>> ---


	
	// --- <<IS-END-SHARED>> ---
}

