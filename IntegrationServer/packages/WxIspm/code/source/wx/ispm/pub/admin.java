package wx.ispm.pub;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import com.wm.lang.ns.NSPackage;
import com.wm.app.b2b.server.InvokeState;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import com.github.jochenw.afw.core.inject.IComponentFactory;
import com.github.jochenw.ispm.core.actions.ImportFromLocalRepoAction;
import com.github.jochenw.ispm.core.actions.ImportFromRemoteRepoAction;
import com.github.jochenw.ispm.core.actions.PackageCompilerAction;
import com.github.jochenw.ispm.core.actions.PackageCompilerAction.Option;
import com.github.jochenw.ispm.core.data.Data;
import com.github.jochenw.ispm.core.model.IspmConfiguration;
import com.github.jochenw.ispm.core.svc.IspmApplication;
import com.github.jochenw.ispm.core.svc.IspmApplicationSvc;
import com.softwareag.util.IDataMap;
// --- <<IS-END-IMPORTS>> ---

public final class admin

{
	// ---( internal utility methods )---

	final static admin _instance = new admin();

	static admin _newInstance() { return new admin(); }

	static admin _cast(Object o) { return (admin)o; }

	// ---( server methods )---




	public static final void compilePackages (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(compilePackages)>> ---
		// @sigtype java 3.5
		// [i] field:1:required packageNames
		// [i] field:0:optional noReload {"false","true"}
		// [i] record:0:required options
		// [i] - field:0:optional usingXlintDeprecation {"false","true"}
		// [i] - field:0:required noFailureOnWarnings
		// [o] recref:0:required result wx.ispm.docs:ActionResult
		final IDataMap map = new IDataMap(pipeline);
		final String[] packageNames = Data.requireStrings(map, "packageNames");
		final boolean noReload = Data.requireBoolean(map, "noReload");
		final IDataMap optionsMap = Data.getIDataMap(map, "options");
		final boolean usingXlintDeprecation;
		final boolean noFailureOnWarnings;
		if (optionsMap == null) {
			usingXlintDeprecation = false;
			noFailureOnWarnings = false;
		} else {
			usingXlintDeprecation = Data.requireBoolean(optionsMap, "usingXlintDeprecation", "options/usingXlintDeprecation");
		    noFailureOnWarnings = Data.requireBoolean(map, "noFailureOnWarnings", "options/noFailureOnWarnings");
		}
		final List<Option> options = new ArrayList<Option>();
		if (usingXlintDeprecation) {
			options.add(Option.USING_X_LINT_DEPRECATION);
		}
		if (noFailureOnWarnings) {
			options.add(Option.NO_FAILURE_ON_WARNINGS);
		}
		if (noReload) {
			options.add(Option.RELOAD);
		}
		final IComponentFactory cf = IspmApplicationSvc.getInstance().getApplication().getComponentFactory();
		final PackageCompilerAction action = cf.requireInstance(PackageCompilerAction.class);
		action.compile(packageNames, options.toArray(new Option[options.size()])).apply(map);
		// --- <<IS-END>> ---

                
	}



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
		final IspmApplication ispmApplication = IspmApplicationSvc.getInstance().getApplication();
		final IspmConfiguration ispmConfiguration = ispmApplication.getIspmConfiguration();
		final ImportFromLocalRepoAction action = ispmApplication.getComponentFactory().requireInstance(ImportFromLocalRepoAction.class);
		action.importFromLocalRepo(null, ispmConfiguration.requireInstance("local"),
				                   ispmConfiguration.requireLocalRepo(localRepoId), projectId).apply(map);
		// --- <<IS-END>> ---

                
	}



	public static final void importProjectFromRemoteRepository (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(importProjectFromRemoteRepository)>> ---
		// @sigtype java 3.5
		// [i] field:0:required projectId
		// [i] field:0:required localRepoId
		// [i] field:0:required remoteRepoId
		// [i] field:0:required deleteExistingProject {"false","true"}
		// [o] field:1:required messages
		final IDataMap map = new IDataMap(pipeline);
		final String projectId = Data.requireString(map, "projectId");
		final String localRepoId = Data.requireString(map, "localRepoId");
		final String remoteRepoId = Data.requireString(map, "remoteRepoId");
		final boolean deleteExistingProject = Data.requireBoolean(map, "deleteExistingProject");
		final IspmApplication ispmApplication = IspmApplicationSvc.getInstance().getApplication();
		final ImportFromRemoteRepoAction action = ispmApplication.getComponentFactory().requireInstance(ImportFromRemoteRepoAction.class);
		action.importFromRemoteRepo(projectId, "local", localRepoId, remoteRepoId, deleteExistingProject).apply(map);
			
		// --- <<IS-END>> ---

                
	}
}

