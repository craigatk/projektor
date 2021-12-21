package example

import strikt.api.expectThat
import strikt.assertions.*

import io.kotest.core.spec.style.StringSpec

class FooTest : StringSpec() {
    init {
        "should return bar" {
            expectThat(foo()).isEqualTo("bar")
        }
    }
}
