package com.github.jochenw.ispm.core.actions;

import java.util.Map;

import com.github.jochenw.afw.core.util.Strings;
import com.github.jochenw.ispm.core.config.TIspmConfiguration.TPlugin;

public class AddPluginAction extends AbstractConfigUpdatingAction {
	public void addPlugin(String pClassName, String pScript, Map<String,String> pProperties) {
		final String className = pClassName;
		final String script = pScript;
		if (Strings.isEmpty(className)  &&  Strings.isEmpty(script)) {
			throw new IllegalStateException("Either of the parameters className, and script, is required.");
		} else if (!Strings.isEmpty(className)  &&  !Strings.isEmpty(script)) {
			throw new IllegalStateException("The parameters className, and script, are mutually exclusive.");
		}
		run((tIspmConfiguration) -> {
			final TPlugin plugin = new TPlugin(null, Strings.notNull(className, ""), Strings.notNull(script, ""));
			if (pProperties != null) {
				pProperties.forEach((k,v) -> plugin.setProperty(k, v));
			}
			tIspmConfiguration.addPlugin(plugin);
		});
	}
}
