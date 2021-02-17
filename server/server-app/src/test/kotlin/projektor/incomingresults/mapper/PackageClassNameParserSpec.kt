package projektor.incomingresults.mapper

import io.kotest.core.spec.style.StringSpec
import io.kotest.data.forAll
import io.kotest.data.row
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class PackageClassNameParserSpec : StringSpec({
    "should parse package and class name" {
        forAll(
            row("example.Spec", "example", "Spec"),
            row("com.example.sub.package.MyTestSpec", "com.example.sub.package", "MyTestSpec"),
            row("Spec", null, "Spec"),
            row("/k6/test.js", "k6/test.js", "test"),
            row("src\\TestOutput\\__tests__\\TestSuiteSystemErr.spec.tsx", "src/TestOutput/__tests__/TestSuiteSystemErr.spec.tsx", "TestSuiteSystemErr"),
            row("src/TestOutput/__tests__/TestSuiteSystemErr.spec.tsx", "src/TestOutput/__tests__/TestSuiteSystemErr.spec.tsx", "TestSuiteSystemErr"),
            row("Text description / with period . and another slash /", null, "Text description / with period . and another slash /"),
            row("No_spaces/but./_dots/", null, "No_spaces/but./_dots/")
        ) { classAndPackage, expectedPackage, expectedClass ->
            val packageAndClass = parsePackageAndClassName(classAndPackage)

            expectThat(packageAndClass) {
                get { first }.isEqualTo(expectedPackage)
                get { second }.isEqualTo(expectedClass)
            }
        }
    }
})
