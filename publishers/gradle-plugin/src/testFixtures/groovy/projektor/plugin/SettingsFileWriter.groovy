package projektor.plugin

class SettingsFileWriter {
    static File createSettingsFile(TempDirectory projectDir, String projectName) {
        File settingsFile = projectDir.newFile('settings.gradle')

        settingsFile.text = "rootProject.name = '${projectName}'"

        return settingsFile
    }
}
