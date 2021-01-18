import java.nio.file.Path

import javax.json.JsonObject;

import com.github.jochenw.afw.core.inject.IComponentFactory;
import com.github.jochenw.afw.core.inject.ComponentFactoryBuilder.Binder;
import com.github.jochenw.afw.core.inject.ComponentFactoryBuilder.Module;
import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.afw.core.log.ILogFactory;
import com.github.jochenw.afw.core.util.Functions.FailableConsumer;
import com.github.jochenw.afw.core.util.Functions.FailableFunction;
import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Holder;
import com.github.jochenw.ispm.core.components.IJsonHandler;
import com.github.jochenw.ispm.core.model.ILocalRepo
import com.github.jochenw.ispm.core.model.IRemoteRepo
import com.github.jochenw.ispm.core.model.IRemoteRepoHandler;
import com.github.jochenw.ispm.core.model.IRemoteRepoHandler.IProject


def remoteRepoHandler = new com.github.jochenw.ispm.core.components.AbstractGitRemoteRepoHandler() {
    private ILog log;
	private IJsonHandler jsonHandler;

	private void init(IComponentFactory pComponentFactory) {
	    def logFactory = pComponentFactory.requireInstance(ILogFactory.class);
		jsonHandler = pComponentFactory.requireInstance(IJsonHandler.class);
        log = logFactory.getLog(getClass());
	}
	public void forEach(IRemoteRepo pRemoteRepo, FailableConsumer<IProject,?> pConsumer) {
		try {
			final String u = pRemoteRepo.getUrl();
			final String uri;
			if (u.endsWith("/")) {
				uri = u + "_apis/git/repositories";
			} else {
				uri = u + "/_apis/git/repositories";
			}
			final FailableFunction<InputStream,JsonObject,?> function = (in) -> {
                return jsonHandler.parse(in);
			};
			final JsonObject repos = read(uri, pRemoteRepo, function);
			repos.getJsonArray("value").forEach((o) -> {
				final String id = o.getJsonString("name").getString();
				final String url = o.getJsonString("remoteUrl").getString();
                final IProject project = new IProject(){
					public String getId() { return id; }
					public String getUrl() { return url; }
				};
				pConsumer.accept(project);
			});
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}
	public String getProjectUrl(IRemoteRepo pRemoteRepo, String pProjectId) {
	    log.entering("getProjectUrl", "Repo = " + pRemoteRepo.getId() + ", project = " + pProjectId);
	    final Holder<String> holder = new Holder<>();
	    forEach(pRemoteRepo, (prj) -> {
	        if (pProjectId.equals(prj.getId())) {
	            holder.set(prj.getUrl());
	        }
	    });
	    final String uri = holder.get();
	    if (uri == null) {
	        throw new IllegalArgumentException("Project not found in remote repo: " + pProjectId);
	    }
		log.exiting("getProjectUrl", uri);
		return uri;
    }
};


// Create, and return the module.
return new Module(){
	public void configure(Binder pBinder) {
		// Bind the handler as "default".
		pBinder.bind(IRemoteRepoHandler.class, "default").toInstance(remoteRepoHandler);
		// Bind the same handler as "azure".
		pBinder.bind(IRemoteRepoHandler.class, "azure").toInstance(remoteRepoHandler);
		pBinder.addFinalizer(new java.util.function.Consumer<IComponentFactory>(){
			public void accept(IComponentFactory pComponentFactory) {
				remoteRepoHandler.init(pComponentFactory);
			}
		});
	}
};
