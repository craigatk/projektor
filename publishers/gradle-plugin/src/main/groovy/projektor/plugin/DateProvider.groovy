package projektor.plugin

import java.time.LocalDateTime

class DateProvider {
    LocalDateTime now() {
        return LocalDateTime.now()
    }
}
