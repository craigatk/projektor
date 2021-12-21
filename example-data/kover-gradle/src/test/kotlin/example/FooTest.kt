import strikt.api.expectThat
import strikt.assertions.*

import org.junit.jupiter.api.Test

class FooTest {
    @Test
    fun `should return bar`() {
        expectThat(foo()).isEqualTo("bar")
    }
}
