package projektor.plugin

class TestDirectoryGroup implements TestGroup {
    private final File resultsDirectory

    TestDirectoryGroup(File resultsDirectory) {
        this.resultsDirectory = resultsDirectory
    }

    @Override
    File getResultsDir() {
        return this.resultsDirectory
    }

    @Override
    String getName() {
        return this.resultsDirectory.name
    }

    @Override
    String getLabel() {
        return this.resultsDirectory.name
    }

    static List<TestDirectoryGroup> listFromDirPaths(File projectDir, List<String> dirPaths) {
        return dirPaths.collect { path -> new File(projectDir, path) }
                .findAll { it.exists() }
                .collect { new TestDirectoryGroup(it) }
    }
}
