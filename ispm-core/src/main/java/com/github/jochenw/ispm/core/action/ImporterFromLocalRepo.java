package com.github.jochenw.ispm.core.action;

import com.github.jochenw.ispm.core.model.IInstance;
import com.github.jochenw.ispm.core.model.ILocalRepo;

public interface ImporterFromLocalRepo {
	public void importProject(ILocalRepo localRepo, String projectId, IInstance instance);
}
