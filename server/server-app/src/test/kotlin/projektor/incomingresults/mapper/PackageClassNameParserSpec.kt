package projektor.incomingresults.mapper

import io.kotlintest.data.forall
import io.kotlintest.specs.StringSpec
import io.kotlintest.tables.row
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class PackageClassNameParserSpec : StringSpec({
    "should parse package and class name" {
        forall(row("example.Spec", "example", "Spec"),
                row("com.example.sub.package.MyTestSpec", "com.example.sub.package", "MyTestSpec"),
                row("Spec", null, "Spec")
        ) { classAndPackage, expectedPackage, expectedClass ->
            val packageAndClass = parsePackageAndClassName(classAndPackage)

            expectThat(packageAndClass) {
                get { first }.isEqualTo(expectedPackage)
                get { second }.isEqualTo(expectedClass)
            }
        }
    }
})
