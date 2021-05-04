package projektor.plugin

import static projektor.plugin.EnvironmentResolver.findFirstEnvironmentValue

class MetadataResolver {
    static String findBuildNumber(Map<String, String> environment, ProjektorPublishPluginExtension extension) {
        return findFirstEnvironmentValue(environment, extension.buildNumberEnvironmentVariables)
    }

    static boolean isCI(Map<String, String> environment, ProjektorPublishPluginExtension extension) {
        return extension.ciEnvironmentVariables.any {envVariable ->
            environment.get(envVariable) != null && environment.get(envVariable) != "false"
        }
    }
}
