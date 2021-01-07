package wx.ispm.pub;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import com.github.jochenw.afw.core.util.Strings;
import com.github.jochenw.ispm.core.actions.AddInstanceAction;
import com.github.jochenw.ispm.core.actions.AddLocalRepoAction;
import com.github.jochenw.ispm.core.actions.AddPluginAction;
import com.github.jochenw.ispm.core.actions.AddRemoteRepoAction;
import com.github.jochenw.ispm.core.data.Data;
import com.github.jochenw.ispm.core.svc.IspmApplication;
import com.softwareag.util.IDataMap;
// --- <<IS-END-IMPORTS>> ---

public final class config

{
	// ---( internal utility methods )---

	final static config _instance = new config();

	static config _newInstance() { return new config(); }

	static config _cast(Object o) { return (config)o; }

	// ---( server methods )---




	public static final void addInstance (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(addInstance)>> ---
		// @sigtype java 3.5
		// [i] field:0:required id
		// [i] field:0:required baseDir
		// [i] record:0:optional properties
		// [i] - field:0:optional is.admin.url
		// [i] - field:0:optional is.admin.user
		// [i] - field:0:optional is.admin.pass
		// [i] field:0:optional wmHomeDir
		// [i] field:0:optional packagesDir
		// [i] field:0:optional configDir
		// [i] field:0:optional logsDir
		final IDataMap map = new IDataMap(pipeline);
		final String id = Data.requireString(map, "id");
		final String baseDir = Data.requireString(map, "baseDir");
		final Path baseDirPath = Paths.get(baseDir);
		if (!Files.isDirectory(baseDirPath)) {
			throw new IllegalArgumentException("Invalid value for parameter baseDir: " + baseDir + " (No such directory)");
		}
		final IData properties = Data.getIData(map, "properties");
		final String wmHomeDir = Data.getString(map, "wmHomeDir");
		final String packagesDir = Data.getString(map, "packagesDir");
		final String configDir = Data.getString(map, "configDir");
		final String logsDir = Data.getString(map, "logsDir");
		final Map<String,String> propertyMap = asPropertyMap(properties);
		
		final IspmApplication ispmApplication = IspmApplication.getInstance();
		final AddInstanceAction action = ispmApplication.getComponentFactory().requireInstance(AddInstanceAction.class);
		action.addInstance(id, baseDirPath, propertyMap, wmHomeDir, packagesDir, configDir, logsDir);
		// --- <<IS-END>> ---

                
	}



	public static final void addLocalRepository (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(addLocalRepository)>> ---
		// @sigtype java 3.5
		// [i] field:0:required id
		// [i] field:0:required dir
		// [i] field:0:optional layout
		// [i] record:0:optional properties
		final IDataMap map = new IDataMap(pipeline);
		final String id = Data.requireString(map, "id");
		final String dir = Data.requireString(map, "dir");
		final Path path = Paths.get(dir);
		if (!Files.isDirectory(path)) {
			throw new IllegalArgumentException("Invalid value for parameter dir: " + dir + " (No such directory)");
		}
		final String layout = Data.getString(map, "layout");
		final IData propertiesData = Data.getIData(map, "properties");
		final IspmApplication ispmApplication = IspmApplication.getInstance();
		final AddLocalRepoAction action = ispmApplication.getComponentFactory().requireInstance(AddLocalRepoAction.class);
		action.addLocalRepo(id, layout, path, asPropertyMap(propertiesData));
		// --- <<IS-END>> ---

                
	}



	public static final void addPlugin (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(addPlugin)>> ---
		// @sigtype java 3.5
		// [i] field:0:required className
		final IDataMap map = new IDataMap(pipeline);
		final String className = Strings.notNull(Data.getString(map, "className"));
		final String scriptName = Strings.notNull(Data.getString(map, "scriptName"));
		if (Strings.isEmpty(className)  &&  Strings.isEmpty(scriptName)) {
			throw new IllegalArgumentException("Either of the parameters className, and scriptName is required.");
		}
		if (!Strings.isEmpty(className)  &&  !Strings.isEmpty(scriptName)) {
			throw new IllegalArgumentException("The parameters className, and scriptName are mutually exclusive.");
		}
		final IData properties = Data.getIData(map, "properties");
		final IspmApplication ispmApplication = IspmApplication.getInstance();
		final AddPluginAction action = ispmApplication.getComponentFactory().requireInstance(AddPluginAction.class);
		action.addPlugin(className, scriptName, asPropertyMap(properties));
		// --- <<IS-END>> ---

                
	}



	public static final void addRemoteRepository (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(addRemoteRepository)>> ---
		// @sigtype java 3.5
		// [i] field:0:required id
		// [i] field:0:required url
		// [i] field:0:optional handler
		// [i] record:0:optional properties
		// [i] - field:0:optional git.user.name
		// [i] - field:0:required git.user.email
		// [i] - field:0:optional remote.auth.userName
		// [i] - field:0:optional remote.auth.password
		final IDataMap map = new IDataMap(pipeline);
		final String id = Data.requireString(map, "id");
		final String url = Data.requireString(map, "url");
		final String handler = Data.getString(map, "handler");
		final IData propertiesData = Data.getIData(map, "properties");
		final IspmApplication ispmApplication = IspmApplication.getInstance();
		final AddRemoteRepoAction action = ispmApplication.getComponentFactory().requireInstance(AddRemoteRepoAction.class);
		action.addRemoteRepo(id, handler, url, asPropertyMap(propertiesData));
			
		// --- <<IS-END>> ---

                
	}

	// --- <<IS-START-SHARED>> ---
	private static Map<String,String> asPropertyMap(IData pData) {
		if (pData == null) {
			return null;
		} else {
			final IDataCursor crsr = pData.getCursor();
			if (crsr.first()) {
				final Map<String,String> map = new HashMap<>();
				do {
					final String key = crsr.getKey();
					final Object value = crsr.getValue();
					if (value == null) {
						throw new IllegalArgumentException("The value for property " + key + " must not be null.");
					} else if (value instanceof String) {
						map.put(key, (String) value);
					} else {
						throw new IllegalArgumentException("The value for property " + key
								                           + " must be a string, not an instance of " + value.getClass().getName());
					}
				} while (crsr.next());
				crsr.destroy();
				return map;
			} else {
				crsr.destroy();
				return null;
			}
		}
	}
	// --- <<IS-END-SHARED>> ---
}

