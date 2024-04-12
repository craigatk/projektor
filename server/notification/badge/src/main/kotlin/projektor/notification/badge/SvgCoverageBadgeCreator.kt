package projektor.notification.badge

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Reference:
 * http://shields.io/category/coverage
 * http://shields.io/#your-badge
 */
class SvgCoverageBadgeCreator(templateFileName: String = "coverage.template.svg") {
    private val svgTemplate = loadTextFromFile(templateFileName)

    fun createBadge(coveredPercentage: BigDecimal): String {
        val coverageLevel = CoverageLevel.of(coveredPercentage)
        val fillColor = coverageLevel.fillColor
        val roundedCoveredPercentage = coveredPercentage.setScale(0, RoundingMode.HALF_UP)

        val svgText =
            svgTemplate
                .replace("{coveredPercentage}", roundedCoveredPercentage.toPlainString())
                .replace("{fillColor}", fillColor)

        return svgText
    }

    private fun loadTextFromFile(filename: String) =
        javaClass
            .getResourceAsStream("/$filename")
            .bufferedReader()
            .readText()
}
