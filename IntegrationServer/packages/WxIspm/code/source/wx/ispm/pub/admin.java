package wx.ispm.pub;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import com.github.jochenw.ispm.core.actions.ImportFromLocalRepoAction;
import com.github.jochenw.ispm.core.data.Data;
import com.github.jochenw.ispm.core.model.IspmConfiguration;
import com.github.jochenw.ispm.core.svc.IspmApplication;
import com.softwareag.util.IDataMap;
// --- <<IS-END-IMPORTS>> ---

public final class admin

{
	// ---( internal utility methods )---

	final static admin _instance = new admin();

	static admin _newInstance() { return new admin(); }

	static admin _cast(Object o) { return (admin)o; }

	// ---( server methods )---




	public static final void importProjectFromLocalRepository (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(importProjectFromLocalRepository)>> ---
		// @sigtype java 3.5
		// [i] field:0:required projectId
		// [i] field:0:required localRepoId
		// [o] field:1:required messages
		final IDataMap map = new IDataMap(pipeline);
		final String projectId = Data.requireString(map, "projectId");
		final String localRepoId = Data.requireString(map, "localRepoId");
		final IspmApplication ispmApplication = IspmApplication.getInstance();
		final IspmConfiguration ispmConfiguration = ispmApplication.getIspmConfiguration();
		final ImportFromLocalRepoAction action = ispmApplication.getComponentFactory().requireInstance(ImportFromLocalRepoAction.class);
		action.run(ispmConfiguration.requireInstance("local"),
				   ispmConfiguration.requireLocalRepo(localRepoId),
				   projectId);
			
		// --- <<IS-END>> ---

                
	}



	public static final void importProjectFromRemoteRepository (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(importProjectFromRemoteRepository)>> ---
		// @sigtype java 3.5
		// [i] field:0:required projectId
		// [i] field:0:required localRepoId
		// [o] field:1:required messages
		final IDataMap map = new IDataMap(pipeline);
		final String projectId = Data.requireString(map, "projectId");
		final String localRepoId = Data.requireString(map, "localRepoId");
		final IspmApplication ispmApplication = IspmApplication.getInstance();
		final IspmConfiguration ispmConfiguration = ispmApplication.getIspmConfiguration();
		final ImportFromLocalRepoAction action = ispmApplication.getComponentFactory().requireInstance(ImportFromLocalRepoAction.class);
		action.run(ispmConfiguration.requireInstance("local"),
				   ispmConfiguration.requireLocalRepo(localRepoId),
				   projectId);
			
		// --- <<IS-END>> ---

                
	}
}

