import java.nio.file.Path

import com.github.jochenw.afw.core.inject.IComponentFactory;
import com.github.jochenw.afw.core.inject.ComponentFactoryBuilder.Binder;
import com.github.jochenw.afw.core.inject.ComponentFactoryBuilder.Module;
import com.github.jochenw.afw.core.util.Functions.FailableConsumer
import com.github.jochenw.ispm.core.model.ILocalRepo
import com.github.jochenw.ispm.core.model.IRemoteRepo
import com.github.jochenw.ispm.core.model.IRemoteRepoHandler;
import com.github.jochenw.ispm.core.model.IRemoteRepoHandler.IProject


def remoteRepoHandler = new IRemoteRepoHandler() {
	private void init(IComponentFactory pComponentFactory) {
		
	}
	public void forEach(IRemoteRepo pRemoteRepo, FailableConsumer<IProject,?> pConsumer) {
		throw new IllegalStateException("Not implemented!");
		
	}
	public String getProjectUrl(IRemoteRepo pRemoteRepo, String pProjectId) {
		throw new IllegalStateException("Not implemented!");
    }
    public void cloneProjectTo(IRemoteRepo pRemoteRepo, String pProjectId, String pUrl, Path pLocalProjectDir) {
		throw new IllegalStateException("Not implemented!");
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
