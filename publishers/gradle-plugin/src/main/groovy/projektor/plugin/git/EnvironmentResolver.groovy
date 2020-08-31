package projektor.plugin.git

class EnvironmentResolver {
    String findFirstEnvironmentValue(List<String> variableNames) {
        return variableNames
                .collect { System.getenv(it) }
                .find { it != null}
    }
}
