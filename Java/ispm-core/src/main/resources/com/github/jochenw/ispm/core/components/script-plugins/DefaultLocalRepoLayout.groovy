import java.nio.file.Files;
import java.nio.file.Path;

import com.github.jochenw.afw.core.inject.IComponentFactory;
import com.github.jochenw.afw.core.inject.IComponentFactoryAware;
import com.github.jochenw.afw.core.inject.ComponentFactoryBuilder.Binder;
import com.github.jochenw.afw.core.inject.ComponentFactoryBuilder.Module;
import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.afw.core.log.ILogFactory;
import com.github.jochenw.afw.core.util.Functions.FailableConsumer;
import com.github.jochenw.ispm.core.model.ILocalRepo;
import com.github.jochenw.ispm.core.model.ILocalRepoLayout;
import com.github.jochenw.ispm.core.model.ILocalRepoLayout.IPkg
import com.github.jochenw.ispm.core.model.ILocalRepoLayout.IProject;

// Create the ILocalRepoLayout
def localRepoLayout = new ILocalRepoLayout() {
    private ILog log;
    public void init(IComponentFactory pComponentFactory) {
        def logFactory = pComponentFactory.requireInstance(ILogFactory.class);
        log = logFactory.getLog(getClass());
    }
    private boolean isPackageDir(Path pFile) {
        return java.nio.file.Files.isDirectory(pFile);
    }
    // Example: Find all directories in "F:\GIT-DEV103", and report them to the consumer as projects.
	public void forEach(ILocalRepo pRepository, FailableConsumer<IProject,?> pConsumer) {
	    final Path baseDir = pRepository.getDir();
	    new File(baseDir.toFile()).eachDir({
	        String name = it.name;
	        Path path = i;
	        pConsumer.accept(new IProject(){
	            public String getId() { return name; }
	            public Path getPath() { return path; }
	        });
	    });
	}
    // Example: Given a project id, like "ESB-LCL", find the corresponding project in "F:\GIT-DEV103",
    // and return the project, if it exists, or null.
	public IProject getProject(ILocalRepo pRepository, String pId) {
		log.entering("getProject", pRepository.getId(), pId);
	    Path baseDir = pRepository.getDir();
	    Path projectDir = baseDir.resolve(pId);
	    log.debug("getProject", "baseDir=" + baseDir, "projectDir=" + projectDir);
	    if (java.nio.file.Files.isDirectory(projectDir)) {
	        return new IProject(){
	            public String getId() { return pId; }
	            public Path getPath() { return projectDir; }
	        };
	    } else {
	        return null;
	    }
	}
	// Example: Given a project, find all packages in the project, and report them to the consumer.
	public void forEach(ILocalRepo pRepository, ILocalRepoLayout.IProject pProject, FailableConsumer<ILocalRepoLayout.IPkg,?> pConsumer) {
	    final Path projectDir = pProject.getPath();
	    final Path packagesDir = projectDir.resolve("IntegrationServer/packages");
	    final File pkgsDir = packagesDir.toFile();
	    pkgsDir.eachDir({
	        final String name = it.name;
	        final Path path = it.toPath();
	        if (isPackageDir(path)) {
	            pConsumer.accept(new IPkg(){
	                public String getName() { return name; }
	                public Path getPath() { return path; }
	            });
	        }
	    });
	}
	public ILocalRepoLayout.IPkg getPackage(ILocalRepo pRepository, ILocalRepoLayout.IProject pProject, String pPkgId) {
	}
};
// Create, and return the module.
return new Module(){
    public void configure(Binder pBinder) {
        pBinder.bind(ILocalRepoLayout.class, "default").toInstance(localRepoLayout);
        pBinder.addFinalizer(new java.util.function.Consumer<IComponentFactory>(){
            public void accept(IComponentFactory pComponentFactory) {
                final ILogFactory = pComponentFactory.requireInstance(ILogFactory.class);
                localRepoLayout.init(pComponentFactory);
            }
        });
    }
};

