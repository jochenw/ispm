# WxIspm

The IS Project Manager (or ispm, for short) is a utility, which aims to provide valuable help in managing a, possibly large, set of projects in the environment of the [webMethods Integration Server](https://en.wikipedia.org/wiki/WebMethods_Integration_Server).

The ispm grew out of a customer project, in which we had to develop, and maintain, hundreds of IS projects, so-called packages. By using a properly configured ispm, you will be able to clone an IS project from your hard drive, build, and install it into your local development IS within seconds.

## Goals (What does it do?)

It provides services, that an IS developer maay use, for example:

  - A service for reloading an IS package, that has been modified on disk.
    Of course, the same functionality is available from within the IS administration
    UI, or from the Designers "Package Explorer". However, the WxIspm service offers to
    do the same with dependencies disabled. This will greatly reduce the time, that the
    service requires for running.
  - A service for compiling an IS packages Java services. This is supposed to work, even if running the
    equivalent IS internal functionality (**wx.server.package:packageCompile**) fails, and reports
    "Unable to locate or start compiler", an error message, which is usually, at best, misleading.
    The WxIspm service however, provides explicit diagnostics, which should help to quickly resolve
    any problems, like compiler errors, or the like.
  - A service, that combines compile/reload into a single service invocation. (Typically, developers need
    to do an explicit reload after the compile.)
  - **Testing stage**: Several services for managing IS sets of development projects (so-called local
    repositories),
    including checkout/clone, etc. from external sources (so-called remote repositories). WxIspm is heavily tied to the "local development mode".
  - **Planning stage**: A service for running ABE/Deployer on a package, or a set of packages. This is not
    intended as a replacement for, or even as a tool for use within a CI/CD chain. Instead, it aims
    to help in a situation, where local testing is not possible, and you wish to ensure, that the
    tested version is identical with the one you are developing locally.
  - It is a test bed for working on new ideas in the area of development, building, and the like. Example: The WxIspm services are documented in markdown files. At build time, these files are being incorporated into the services as comments.

## Non-Goals (What it doesn't, and won't do?)

   - WxIspm is not intended to provide anything beyond the development stage.

   - It should never leave the developers workstation, unless for the purpose of distributing itself.

   - It is not intended for deployment in environments like production, quality assessment, integration. Not even in testing!

     
## Concepts

This section intends to introduce some concepts, that are used overall in WxIspm:

### Instance

An instance is an IS server, that is actually, or (at least) potentially running. An instance is mainly identified by it's installation directory. For example, if the webMethods suite has been installed in *F:\SoftwareAG\webMethods10.3* with typical options, then an instance would be *F:\SoftwareAG\webMethods10.3\IntegrationServer\instances\default*.

A webMethods installation can contain multiple instances, typically other directories in *F:\SoftwareAG\webMethods10.3\IntegrationServer\instances*.

#### The "local" instance

A special instance is the so-called "local" instance. It is automatically created by WxIspm, and needs no explicit configuration. The "local" instance refers to the IS, in which WxIspm is currently running.

#### Other instances

Other instances are only present, if they have been configured in the WxIspm config file. For example, the following would configure another instance named "central":

        <instance id="central"
            baseDir="F:\SAG\wM10.3\IntegrationServer\inst\central"/>

So, in essence, an instance is given by it's Id, and it's base directory. It does have some other attributes, but these are usually covered by reasonable defaults:

| Attribute    | Default value        |
|------------- | -------------------- |
| wmHomeDir    | ${baseDir}/../../..  |
| packagesDir  | ${baseDir}/packages  |
| configDir    | ${baseDir}/config    |
| logsDir      | ${baseDir}/logs      |

**Note**: It is quite possible, to have two instances, which are referring to the same IS. For example, if you have an instance "wm103" with the base directory *F:\SoftwareAG\webMethods10.3\IntegrationServer\instances\default*, referring to the IS, in which WxIspm is running, then the instances "local", and "wm103" would both identify the same IS.



##### Instance properties

An instance can have properties. Basically, apart from the "local" instance, it should have properties:

| Property      | Example                 |Description |
| --------      | ----------------------- | ---------- |
| is.admin.url  | http://127.0.0.1:10555/ | URL of the instances IS administration UI |
| is.admin.user  | Administrator          | Admin user name |
| is.admin.pass  | manage                 | Admin users password. |

A configuration snipped specifying these properties could be:

    <instance id="central"
        baseDir="F:\SAG\wM10.3\IntegrationServer\inst\central">
      <property key="is.admin.url" value="http://127.0.0.1:10555/"/>
      <property key="is.admin.user" value="Administrator"/>
      <property key="is.admin.pass" value="manage"/>
    </instance>

#### Creating instances

To create a new instance, the following possibilities are available:

1. Change the Ispm config file accordingly, and reload the WxIspm package.
2. Invoke the service **wx.ispm.pub.config:addInstance** with the appropriate parameters.

### Local Repositories

A local repository is a directory, that contains projects. Within WxIspm, a project is meant to be a directory, that contains one, or more packages, on which the developer is working.

In local development mode, the local repository is, where you checkout, or clone your source code to.

#### The "internal" local repository

Like the "local" instance, WxIspm creates a local repository automatically: The "internal" local repository is identical with the instances "packages" directory. Every directory in that directory is assumed to be a project, which contains exactly one package. This is intended to be useful in cases, where you are not using "local development mode".

#### Other local repositories

Apart from the "internal" local repository, the WxIspm config file can be used to specify local repositories, like this:



      <localRepo id="wm103" dir="F:\GIT-DEV103">
        <!-- Configure values for git's user.name,
             and user.email settings. -->
        <property key="git.user.name"
               value="Jochen Wiedmann"/>
        <property key="git.user.email"
               value="jochen.wiedmann@gmail.com"
      </localRepo>

#### Creating local repositories

Obviously, you don't need to create the "internal" repository.

To create another repository, there are the following options:



1. Create an entry in the ispm config file, and reload the WxIspm package.
2. Invoke the service **wx.ispm.pub.config:addLocalRepository**.

### Remote repositories

A remote repository is the analogon of a source code repository, where you're projects are stored by the team. 

No obvious idea of a default remote repository provides itself,, so there is no "local", or "internal" remote repository. In other words: All remote repositories are specified in the ispm config file, like so:

      <remoteRepo id="azure"
                 url="https://dev.azure.com/customer/projectGroup"
             handler="azure">
        <property key="azure.userName" value="jwi@softwareag.com"/>
        <property key="azure.password" value="552a....8fc5"/>
      </remoteRepo>

#### Creating remote repositories

Like [instances](#creating-instances), or [local repositories](#creating-local-repositories), there are two options for creating a remote repository:

1. Create a "remoteRepo" element in the ispm config file, and reload the WxIspm package.
2. Invoke the service **wx.ispm.pub.config:addRemoteRepository**.

### Local repository layouts

Any project will typically have its own conventions on organizing projects. To deal with the differences, WxIspm has introduced the concept of a "local repository layout". The layout is a Java object, which implements the interface [**com.github.jochenw.ispm.core.model.ILocalRepoLayout**](https://github.com/jochenw/ispm/blob/master/Java/ispm-core/src/main/java/com/github/jochenw/ispm/core/model/ILocalRepoLayout.java). By default, a local repository has the "default" layout, which is based on the following assumptions:

1. Every subdirectory of the (not subdirectories of subdirectories) is a project.
2. Every project has a directory *IntegrationServer/packages*.
3. The projects packages are the subdirectories of *IntegrationServer/packages*. (Again, only direct subdirectories count, not indirect subdirectories.)

As an example, take the WxIspm project itself, which you can view [here](https://github.com/jochenw/ispm/tree/master/IntegrationServer/packages).

To use an alternative layout, set the attribute "layout" in the ispm config files "localRepo" element, like this

      <localRepo id="wm103" dir="F:\GIT-DEV103"/>
      <localRepo id="wm99" dir="F:\GIT-DEV99" layout="legacy">

In the example, the local repository "wm103" uses the layout "default", because it has no "layout" attribute. The local repository "wm99", however, uses a layout "legacy".

As of this writing, WxIspm includes only the "default" layout. The author is ready to implement other layouts on demand. Apart from that, custom layouts can be implemented as a [plugin](#plugins).


### Remote repository handlers

Remote repository handlers are to remote repositories, what local repository layouts are to local repositories: They adapt WxIspm to the projects conventions. Additionally, they provide the interface to the respective version control system (Git, Subversion, etc.).

WxIspm comes with a default handler called "default": That handler is, in fact, an adapter to [Azure DevOps](https://azure.microsoft.com/services/devops/). The same handler is available, if you use the Id "azure". Suggest the following remote repository definitions:

      <remoteRepo id="azure"
                 url="https://dev.azure.com/customer/projectGroup"/>
      <remoteRepo id="azure2"
                 url="https://dev.azure.com/otherCustomer/otherProjectGroup"
             handler="azure"/>

Both remote repositories would use the same handler "azure": The first one, because it doesn't have a "handler" attribute, thus uses the "default" handler, which is the same than the "azure" handler. The second one specifies the "azure" handler explicitly.

As of this writing, WxIspm includes only the "azure" layout, which is available under the id's "azure", and/or "default". This handler assumes, that you are using the [Git](https://git-scm.com/) vcs (version control system). The author is ready to implement other handlers on demand. Apart from that, custom handlers can be implemented as a [plugin](#plugins).


### Plugins

A plugin is a WxIspm extension, that provides additional, custom components, for example local repository layouts, or remote repository handlers. Plugins can either be implemented as Java classes, or as [Groovy](http://groovy.apache.org/) scripts.

#### Java-Plugins

A Java plugin is a Java class, that implements a [module](#modules). The class must have a public default constructor.

The plugin class is typically located in a Jar file, that's placed in *<IS_INST_DIR>/packages/WxIspm/config/plugins/lib*, or *<IS_INST_DIR>/config/packages/WxIspm/plugins/lib*.  (The former location is preferred, if you are distributing a custom version of WxIspm, in which your plugins should be integrated. The latter location has the advantage, that it survives a redeployment of WxIspm. It is therefore better suited, if you use the standard version of WxIspm.)

A Java plugin is specified in the ispm config file, like this:4

      <plugin class="fully.qualified.plugin.className"/>

#### Groovy-Plugins

A Groovy-Plugin is a Groovy-Script, that returns a [module](#modules). Groovy scripts have the obvious advantage, that they are easier to maintain, and change. The WxIspm author expects, that custom plugins will be mostly written in Groovy. To help in the implementation of plugins, and as a test bed for the Groovy plugins, WxIspm comes with a set of plugin implementations:

1. The local repository handler "default" is implemented as a [Groovy plugin](https://github.com/jochenw/ispm/blob/master/Java/ispm-core/src/main/resources/com/github/jochenw/ispm/core/components/script-plugins/DefaultLocalRepoLayout.groovy).
2. The remote repository handler "azure" is implemented as [another Groovy plugin](https://github.com/jochenw/ispm/blob/master/Java/ispm-core/src/main/resources/com/github/jochenw/ispm/core/components/script-plugins/DefaultRemoteRepoHandler.groovy)


