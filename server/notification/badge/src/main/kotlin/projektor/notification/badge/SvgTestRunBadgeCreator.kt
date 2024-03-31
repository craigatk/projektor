package projektor.notification.badge

/**
 * Reference:
 * http://shields.io/#your-badge
 */
class SvgTestRunBadgeCreator(templateFileName: String = "test_run.template.svg") {
    private val svgTemplate = loadTextFromFile(templateFileName)

    fun createBadge(testRunPassed: Boolean): String {
        val buildLabel = BuildLabel.of(testRunPassed)
        val fillColor = buildLabel.fillColor

        val displayText = if (testRunPassed) {
            "passing"
        } else {
            "failing"
        }

        val svgText = svgTemplate
            .replace("{displayText}", displayText)
            .replace("{fillColor}", fillColor)

        return svgText
    }

    private fun loadTextFromFile(filename: String) = javaClass
        .getResourceAsStream("/$filename")
        .bufferedReader()
        .readText()
}
