package projektor.plugin

import org.junit.rules.TemporaryFolder

class SettingsFileWriter {
    static File createSettingsFile(TemporaryFolder projectDir, String projectName) {
        File settingsFile = projectDir.newFile('settings.gradle')

        settingsFile.text = "rootProject.name = '${projectName}'"

        return settingsFile
    }
}
