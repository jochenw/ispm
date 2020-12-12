package wx.ispm.impl.util;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import com.wm.app.b2b.server.Manifest;
import com.wm.app.b2b.server.Package;
import com.wm.app.b2b.server.PackageManager;
import com.wm.lang.ns.NSName;
import java.util.HashMap;
import java.util.Map;
import com.github.jochenw.ispm.core.data.Data;
import com.github.jochenw.ispm.core.util.Exceptions;
import com.softwareag.util.IDataMap;
// --- <<IS-END-IMPORTS>> ---

public final class admin

{
	// ---( internal utility methods )---

	final static admin _instance = new admin();

	static admin _newInstance() { return new admin(); }

	static admin _cast(Object o) { return (admin)o; }

	// ---( server methods )---




	public static final void runServiceWithDependenciesDisabled (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(runServiceWithDependenciesDisabled)>> ---
		// @sigtype java 3.5
		// [i] field:0:required service
		// [i] record:0:required inputPipeline
		// [i] field:0:required packageName
		final IDataMap map = new IDataMap(pipeline);
		final String serviceName = Data.requireString(map, "service");
		
		final String packageName = Data.requireString(map, "packageName");
		final IData inputPipeline = Data.requireIData(map, "inputPipeline");
		
		// Remove the dependencies on package "packageName", and store them in a Map, so that we can
		// restore it later.
		final Map<String, String> removedDependencies = new HashMap<>();
		for (Package pkg : PackageManager.getAllPackages()) {
			// Ignore builtin packages, and Wx packages
			if (pkg.getName().startsWith("Wm")  ||  pkg.getName().startsWith("Wx")) {
				continue;
			}
			boolean packageChanged = false;
			final Manifest manifest = pkg.getManifest();
			final String[] requires = manifest.getRequiresAsStr();
			if (requires != null) {
				for (String require : requires) {
					if (require.contains(packageName)) {
						manifest.delDependency(require);
						removedDependencies.put(pkg.getName(), require);
						packageChanged = true;
					}
				}
			}
			if (packageChanged) {
				pkg.setManifest(manifest);
			}
		}
		// Call the actual service.
		final IData output;
		try {
			output = Service.doInvoke(NSName.create(serviceName), inputPipeline);
		} catch (Throwable t) {
			throw Exceptions.show(t, ServiceException.class);
		}
		// Merge the service output into the current pipeline.
		if (output != null) {
			final IDataCursor crsr = output.getCursor();
			if (crsr.first()) {
				do {
					final String key = crsr.getKey();
					final Object value = crsr.getValue();
					if (value != null) {
						map.put(key, value);
					}
				} while (crsr.next());
			}
		}
		// Restore the original dependencies.
		for (Map.Entry<String,String> en : removedDependencies.entrySet()) {
			final String pkgName = en.getKey();
			final String require = en.getValue();
			Package pkg = PackageManager.getPackage(pkgName);
			if (pkg == null) {
				throw new IllegalStateException("Package not found: " + pkgName);
			}
			pkg.getManifest().addDependency(pkgName, require, null);
		}
			
		// --- <<IS-END>> ---

                
	}
}

