package wx.ispm.impl.admin;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import com.github.jochenw.ispm.core.compile.PackageCompiler;
import com.github.jochenw.ispm.core.compile.PackageCompiler.CompilerStatusException;
import com.github.jochenw.ispm.core.data.Data;
import com.softwareag.util.IDataMap;
// --- <<IS-END-IMPORTS>> ---

public final class compiler

{
	// ---( internal utility methods )---

	final static compiler _instance = new compiler();

	static compiler _newInstance() { return new compiler(); }

	static compiler _cast(Object o) { return (compiler)o; }

	// ---( server methods )---




	public static final void compilePackage (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(compilePackage)>> ---
		// @sigtype java 3.5
		// [i] field:0:required package
		// [i] field:0:optional noException {"false","true"}
		// [i] field:0:optional logging {"false","true"}
		// [o] field:1:optional warnings
		// [o] field:1:optional logMessages
		// [o] field:0:optional exception
		// [o] field:0:optional standardOutput
		// [o] field:0:optional errorOutput
		// [o] field:0:optional status
		final IDataMap map = new IDataMap(pipeline);
		final boolean logging = map.getAsBoolean("logging", Boolean.FALSE).booleanValue();
		final boolean noException = map.getAsBoolean("noException", Boolean.FALSE).booleanValue();
		final List<String> warnings = new ArrayList<String>();
		final List<String> infos = new ArrayList<String>();
		final String packageName = Data.requireString(map, "package");
		final PackageCompiler compiler = new PackageCompiler(){
			@Override
			protected Path getCodeClassesDir(Path pPackageDir) {
				final Path p = super.getCodeClassesDir(pPackageDir);
				log("Target directory for " + pPackageDir + " is " + p);
				return super.getCodeClassesDir(pPackageDir);
			}
		};
		compiler.setWarnLogger((s) -> warnings.add(s));
		compiler.setLogger((s) -> { if (logging) { infos.add(s); } });
		try {
			final Path instanceDir = Paths.get(".");
			compiler.compile(instanceDir, packageName);
		} catch (CompilerStatusException cse) {
			if (noException) {
				final PackageCompiler.Data data = cse.getData();
				map.put("standardOutput", new String(data.getStandardOutput()));
				map.put("errorOutput", new String(data.getErrorOutput()));
				map.put("status", String.valueOf(data.getStatus()));
				final StringWriter sw = new StringWriter();
				final PrintWriter pw = new PrintWriter(sw);
				cse.printStackTrace(pw);
				pw.close();
				map.put("exception", sw.toString());
			} else {
				throw new ServiceException(cse);
			}
		} catch (Throwable t) {
			if (noException) {
				final StringWriter sw = new StringWriter();
				final PrintWriter pw = new PrintWriter(sw);
				t.printStackTrace(pw);
				pw.close();
				map.put("exception", sw.toString());
			} else {
				throw new ServiceException(t);
			}
		}
		if (!warnings.isEmpty()) {
			map.put("warnings", warnings.toArray(new String[warnings.size()]));
		}
		if (!infos.isEmpty()) {
			map.put("logMessages", infos.toArray(new String[infos.size()]));
		}
			
		// --- <<IS-END>> ---

                
	}
}

