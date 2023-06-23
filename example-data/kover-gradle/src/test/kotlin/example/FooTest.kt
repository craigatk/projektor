package example

import io.kotest.core.spec.style.StringSpec
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class FooTest : StringSpec() {
    init {
        "should return bar" {
            expectThat(foo()).isEqualTo("bar")
        }
    }
}
