package com.github.jochenw.ispm.core.components;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import javax.inject.Inject;

import com.github.jochenw.afw.core.inject.LogInject;
import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.afw.core.props.IPropertyFactory;
import com.github.jochenw.afw.core.util.Functions.FailableFunction;
import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.HttpConnector;
import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.ispm.core.model.IRemoteRepo;
import com.github.jochenw.ispm.core.model.IRemoteRepoHandler;

public abstract class AbstractGitRemoteRepoHandler implements IRemoteRepoHandler {
	private @LogInject ILog log;
	private @Inject IGitHandler gitHandler;
	private @Inject IBranchSelector branchSelector;
	private @Inject IPropertyFactory propertyFactory;
	private @Inject HttpConnector httpConnector;

	protected String getProperty(IRemoteRepo pContainer, String pKey) {
		return Objects.notNull(pContainer.getProperties().get(pKey), propertyFactory.getPropertyMap().get(pKey));
	}
	protected String requireProperty(IRemoteRepo pContainer, String pKey) {
		final String value = getProperty(pContainer, pKey);
		if (value == null) {
			throw new NullPointerException("Missing property: " + pKey);
		}
		if (value.length() == 0) {
			throw new IllegalArgumentException("Empty property: " + pKey);
		}
		return value;
	}
	protected <O> O read(String pUrl, IRemoteRepo pRemoteRepo, FailableFunction<InputStream,O,?> pConsumer) {
		final String mName = "read";
		log.entering(mName, "Reading", pUrl, pRemoteRepo.getId());
		try (final HttpConnector.HttpConnection conn = httpConnector.connect(new URL(pUrl))) {
			final HttpURLConnection huc = conn.getUrlConnection();
			huc.setRequestMethod("GET");
			final String userName = getProperty(pRemoteRepo, "remote.auth.userName");
			final String password = getProperty(pRemoteRepo, "remote.auth.password");
			if (userName != null  ||  password != null) {
				log.debug(mName, "Authentication: userName=" + userName);
				if (password == null) {
					log.warn(mName, "Authentication: password is null");
				} else if (password.length() == 0) {
					log.warn(mName, "Authentication: passwordis empty.");
				} else if (log.isTraceEnabled()) {
					log.trace(mName, "Authentication: password=" + password);
				} else {
					log.debug(mName, "Authentication: password=<NOT_LOGGED>");
				}
				final String authStr = Base64.getMimeEncoder(0, new byte[] {'\n'}).encodeToString((userName + ":" + password).getBytes(StandardCharsets.UTF_8));
				huc.setRequestProperty("Authorization", "basic " + authStr.trim());
			}
			try (InputStream in = huc.getInputStream()) {
				return pConsumer.apply(in);
			}
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
			
	}
	@Override
	public void cloneProjectTo(IRemoteRepo pRemoteRepo, String pProjectId, String pUrl, Path pLocalProjectDir) {
		final String mName = "cloneProjectTo";
		final IRemoteRepo remoteRepo = Objects.requireNonNull(pRemoteRepo, "Remote repository");
		final IGitHandler gh = Objects.requireNonNull(gitHandler, "GitHandler");
		log.entering(mName, remoteRepo.getId(), pProjectId, pUrl, pLocalProjectDir);
		gh.clone(remoteRepo, pUrl, pLocalProjectDir);
		final List<String> branches = gitHandler.getBranches(remoteRepo, pLocalProjectDir);
		final String latestBranch = branchSelector.getBranch(branches);
		if (latestBranch == null) {
			log.warn(mName, "Latest branch not found in branch list, keeping master.", branches);
		} else {
			log.info(mName, "Switching to latest branch", latestBranch);
			gitHandler.switchToBranch(remoteRepo, pLocalProjectDir, latestBranch);
		}
		for (String gitProperty : Arrays.asList("user.name", "user.email")) {
			final String key = "git." + gitProperty;
			String value = remoteRepo.getProperties().get(key);
			if (value == null) {
				value = propertyFactory.getPropertyValue(key);
			}
			if (value == null) {
				log.warn(mName, "No value found for git property, ignoring.", key);
			} else {
				log.info(mName, "Configuring git property ", gitProperty, value);
				gitHandler.configure(remoteRepo, pLocalProjectDir, gitProperty, value);
			}
		}
		log.exiting(mName);
	}
}
