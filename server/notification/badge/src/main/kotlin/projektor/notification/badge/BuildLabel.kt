package projektor.notification.badge

enum class BuildLabel(val fillColor: String) {
    PASS("#4c1"),
    FAIL("#e05d44");

    companion object {
        fun of(testRunPassed: Boolean): BuildLabel =
            if (testRunPassed) {
                PASS
            } else {
                FAIL
            }
    }
}
