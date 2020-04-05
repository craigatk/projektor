package projektor.plugin

class PluginOutput {
    static String extractTestId(String output, String projektorServerUrl) {
        String reportMessage = "View Projektor report at: ${projektorServerUrl}/tests/"
        assert output.contains(reportMessage)
        int startingIndex = output.indexOf(reportMessage) + reportMessage.size()

        return output.substring(startingIndex, startingIndex + 12)
    }

    static void verifyOutputContainsReportLink(String buildOutput, String serverUrl, String publicId) {
        assert !buildOutput.contains("Projektor plugin enabled but no server specified")
        assert buildOutput.contains("View Projektor report at: ${serverUrl}/tests/${publicId}")
    }

    static void verifyOutputDoesNotContainReportLink(String buildOutput) {
        assert !buildOutput.contains("View Projektor report at")
    }
}
