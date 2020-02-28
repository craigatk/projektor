package projektor.plugin

class PublishResult {
    String reportUrl
    String publicId

    boolean isSuccessful() {
        reportUrl != null
    }
}
