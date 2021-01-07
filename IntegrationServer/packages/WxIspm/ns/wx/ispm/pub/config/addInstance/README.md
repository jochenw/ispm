# The service addInstance

The service **wx.ispm.pub.config:addInstance** updates the WxIspm configuration by adding a new instance to the config file.

## Input Parameters

- id The instance id. (Required) Must be unique with respect to instances. (It's fine, to have the same id for different object types, for example an instance, and a local repository.)
- baseDir The instances base directory. (Required) Example: *F:\SoftwareAG\webMethods103\IntegrationServer\instances\default*
- properties An optional set of key/value pairs, that are being used as properties. For example, the properties **is.admin.url**, **is.admin.user**, **is.admin.pass** must be specified, if remote access to the instance is required. (Which is usually the case.)
- wmHomeDir The webMethods installation directory. This parameter is not required, and the default value is *${baseDir}/../../..*.
- packagesDir The instances package directory. This parameter is not required, and the default value is *${baseDir}/packages*.
- configDir The instances config directory. This parameter is not required, and the default value is *${baseDir}/config*.
- logsDir The instances logs directory. This parameter is not required, and the default value is *${baseDir}/logs*.

## See also

- The service wx.ispm.pub.config:addLocalRepository
- The service wx.ispm.pub.config:addRemoteRepository
