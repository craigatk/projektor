package projektor.plugin

class EnvironmentResolver {
    String findFirstEnvironmentValue(List<String> variableNames) {
        return variableNames
                .collect { System.getenv(it) }
                .find { it != null}
    }

    static String findFirstEnvironmentValue(Map<String, String> environment, List<String> variableNames) {
        return variableNames
                .collect { environment.get(it) }
                .find { it != null}
    }
}
