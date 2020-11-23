package com.github.jochenw.ispm.core.svc;

import javax.inject.Inject;

import org.apache.logging.log4j.util.Strings;

import com.github.jochenw.afw.core.inject.LogInject;
import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.ispm.core.action.ImporterFromLocalRepo;
import com.github.jochenw.ispm.core.model.IConfiguration;
import com.github.jochenw.ispm.core.model.IInstance;
import com.github.jochenw.ispm.core.model.ILocalRepo;
import com.softwareag.util.IDataMap;

/**
 * This service is being invoked to import a project from a local repository
 * to the IS package directory.
 */
public class ImportFromLocalRepoSvc extends AbstractIspmService {
	private @LogInject ILog log;
	private @Inject ImporterFromLocalRepo importerFromLocalRepo;

	@Override
	public Object[] run(IDataMap pMap) {
		final String localRepoId = Data.getString(pMap, "localRepoId");
		final String projectId = Data.requireString(pMap, "projectId");
		final String targetInstanceId = Data.getString(pMap, "targetInstanceId");
		IConfiguration configuration = getConfiguration();
		final ILocalRepo localRepo = configuration.requireLocalRepo(localRepoId);
		final IInstance instance = Strings.isEmpty(targetInstanceId) ? getLocalInstance() : configuration.requireInstance(targetInstanceId);
		importerFromLocalRepo.importProject(localRepo, projectId, instance);
		return result();
	}
}
