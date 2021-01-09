package com.github.jochenw.ispm.core.actions;

import java.util.Map;

import com.github.jochenw.afw.core.inject.LogInject;
import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.afw.core.util.Strings;
import com.github.jochenw.ispm.core.config.TIspmConfiguration.TPlugin;

public class AddPluginAction extends AbstractConfigUpdatingAction {
	private @LogInject ILog log;

	public Context addPlugin(String pClassName, String pScript, Map<String,String> pProperties) {
		final String mName = "addPlugin";
		return run(null, log, mName, (ctx, tIspmConfiguration) -> {
			ctx.action("Adding plugin: className=" + pClassName + ", scriptName=" + pScript);
			ctx.debug(log, mName, "Adding plugin: className=" + pClassName + ", scriptName=" + pScript);
			final String className = pClassName;
			final String script = pScript;
			if (Strings.isEmpty(className)  &&  Strings.isEmpty(script)) {
				throw new IllegalStateException("Either of the parameters className, and script, is required.");
			} else if (!Strings.isEmpty(className)  &&  !Strings.isEmpty(script)) {
				throw new IllegalStateException("The parameters className, and script, are mutually exclusive.");
			}
			final TPlugin plugin = new TPlugin(null, Strings.notNull(className, ""), Strings.notNull(script, ""));
			if (pProperties != null) {
				pProperties.forEach((k,v) -> {
					ctx.debug(log, mName, "Setting property: " + k + "=" + v);
					plugin.setProperty(k, v);
				});
			}
			tIspmConfiguration.addPlugin(plugin);
			ctx.debug(log, mName, "Added plugin");
			ctx.action("Added plugin");
		});
	}
}
