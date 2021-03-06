package com.github.jochenw.ispm.core.components;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import javax.inject.Inject;
import javax.inject.Named;

import com.github.jochenw.afw.core.inject.LogInject;
import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.afw.core.props.IPropertyFactory;
import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Executor;
import com.github.jochenw.afw.core.util.Streams;
import com.github.jochenw.afw.core.util.Strings;
import com.github.jochenw.afw.core.util.Systems;
import com.github.jochenw.ispm.core.actions.IoCatcher;
import com.github.jochenw.ispm.core.model.IRemoteRepo;



public class GitExeGitHandler implements IGitHandler {
	private @LogInject ILog log;
	private @Inject IPropertyFactory propertyFactory;
	private @Inject @Named(value="git") Executor executor;

	protected String getGitProperty(IRemoteRepo pRemoteRepo, String pKey) {
		final String value = pRemoteRepo.getProperties().get(pKey);
		if (value == null) {
			return propertyFactory.getPropertyValue(pKey);
		} else {
			return value;
		}
	}

	protected String getGitExecutable(IRemoteRepo pRemoteRepo) {
		final String gitLocationStr = getGitProperty(pRemoteRepo, "git.location");
		if (gitLocationStr == null  ||  gitLocationStr.length() == 0) {
			return Systems.isWindows() ? "git.exe" : "git";
		} else {
			return gitLocationStr;
		}
	}

	protected boolean isGitVerbose(IRemoteRepo pRemoteRepo) {
		return Boolean.parseBoolean(getGitProperty(pRemoteRepo, "git.verbose"));
	}
	
	protected boolean isGitCloneVerbose(IRemoteRepo pRemoteRepo) {
		return Boolean.parseBoolean(getGitProperty(pRemoteRepo, "git.clone.verbose"));
	}
	
	@Override
	public void clone(IRemoteRepo pRemoteRepo, String pProjectUrl, Path pRepoDir) {
		final String mName = "clone";
		try {
			log.entering(mName, "Cloning", pRemoteRepo.getId(), pProjectUrl, pRepoDir);
			final Path parentDir = pRepoDir.toAbsolutePath().getParent();
			if (parentDir != null) {
				log.trace(mName, "Creating directory", parentDir.toString());
				Files.createDirectories(parentDir);
			}
			final List<String> cmdList = new ArrayList<>();
	        cmdList.add(getGitExecutable(pRemoteRepo));
	        cmdList.add("clone");
	        if (isGitVerbose(pRemoteRepo)) {
	        	cmdList.add("--verbose");
	        }
	        if (!isGitCloneVerbose(pRemoteRepo)) {
	        	cmdList.add("-q");
	        }
	        cmdList.add(pProjectUrl);
	        cmdList.add(pRepoDir.getFileName().toString());
	        final String[] cmd = cmdList.toArray(new String[cmdList.size()]);
			run(pRemoteRepo, parentDir, cmd);
			log.exiting(mName);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	private static final IntConsumer DEFAULT_STATUS_HANDLER = new IntConsumer() {
		public void accept(int pStatus) {
			if (pStatus != 0) {
				throw new IllegalStateException("Status: " + pStatus); };
		}
	};

	@Override
	public List<String> getBranches(IRemoteRepo pRepo, Path pRepoDir) {
		final String mName = "clone";
		try {
			log.entering(mName, "Getting branches", pRepo.getId(), pRepoDir);
			final String[] cmd = new String[] {
					getGitExecutable(pRepo),
					"branch", "-r"
			};
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			final ByteArrayOutputStream err = new ByteArrayOutputStream();
			final Consumer<InputStream> cOut = (in) -> Streams.copy(in, out);
			final Consumer<InputStream> cErr = (in) -> Streams.copy(in, err);

			executor.run(pRepoDir, cmd, getEnv(pRepo), cOut, cErr, DEFAULT_STATUS_HANDLER);
			final List<String> lines = parseBranches(out.toByteArray());
			log.exiting(mName, Strings.join(", ", lines));
			return lines;
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	@Override
	public void configure(IRemoteRepo pRemoteRepo, Path pRepoDir, String pGitProperty, String pValue) {
		final String mName = "configure";
		try {
			log.entering(mName, pGitProperty, pValue);
			final String[] cmd = new String[] { getGitExecutable(pRemoteRepo), "config", pGitProperty, pValue };
			run(pRemoteRepo, pRepoDir, cmd);
			log.exiting(mName);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	protected List<String> parseBranches(byte[] pOutput) {
		final String s = new String(pOutput);
		final List<String> branches = new ArrayList<String>();
		try (StringReader sr = new StringReader(s);
			 BufferedReader br = new BufferedReader(sr)) {
			for (;;) {
				final String line = br.readLine();
				if (line == null) {
					break;
				} else if (line.contains("->")) {
					continue;
				} else {
					final String branch = getReleaseNumber(line.trim());
					if (branch != null) {
						branches.add(branch);
					}
				}
			}
		} catch (IOException e) {
			throw Exceptions.show(e);
		}
		return branches;
	}

	protected String getReleaseNumber(String pBranchName) {
		final int offset = pBranchName.indexOf("Releases/");
		if (offset != -1) {
			return pBranchName.substring(offset);
		} else {
			return null;
		}
	}

	@Override
	public void switchToBranch(IRemoteRepo pRepo, Path pRepoDir, String pBranch) {
		final String mName = "switchToBranch";
		try {
			log.entering(mName, "Switching branch:", pRepo.getId(), pRepoDir, pBranch);
			final String[] cmd = new String[] {
					getGitExecutable(pRepo),
					"checkout", "--track", pBranch
			};
			run(pRepo, pRepoDir, cmd);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	protected Consumer<InputStream> loggingConsumer(String pId) {
		final IoCatcher ioc = IoCatcher.get();
		if (ioc == null) {
			return (in) -> {
				log.debug("loggingConsumer", "Discarding output", pId);
				Streams.read(in);
			};
		} else {
			if ("STDOUT".equals(pId)) {
				return (in) -> {
					log.debug("loggingConsumer", "Catching output", pId);
					Streams.copy(in, ioc.getStandardOut());
				};
			} else if ("STDERR".equals(pId)) {
				return (in) -> {
					log.debug("loggingConsumer", "Catching output", pId);
					Streams.copy(in, ioc.getStandardErr());
				};
			} else {
				throw new IllegalArgumentException("Invalid id: " + pId);
			}
		}
	}

	protected void run(IRemoteRepo pRepo, Path pDir, String... pCmd) {
		final String[] env = getEnv(pRepo);
		log.debug("run", "Running git: ", pDir, pCmd, env);
		executor.run(pDir, pCmd, env, loggingConsumer("STDOUT"), loggingConsumer("STDERR"),
					 DEFAULT_STATUS_HANDLER);
	}

	private String[] getEnv(IRemoteRepo pRepo) {
		return null;
	}
}
