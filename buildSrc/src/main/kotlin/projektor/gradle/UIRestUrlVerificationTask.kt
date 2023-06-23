package projektor.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.VerificationTask
import java.io.File

open class UIRestUrlVerificationTask : DefaultTask(), VerificationTask {
    private var ignoreFailures = false

    override fun setIgnoreFailures(ignoreFailures: Boolean) {
        this.ignoreFailures = ignoreFailures
    }


    override fun getIgnoreFailures(): Boolean =
        this.ignoreFailures

    @TaskAction
    fun run() {
        val jsDistDir = File(project.projectDir, "dist/")

        val jsBundleFile = jsDistDir.walk().filter { it.extension == "js" }.firstOrNull()

        if (jsBundleFile != null) {
            logger.error("Verifying JS bundle file [${jsBundleFile.path}] does not contain localhost")

            val jsBundleFileContents = jsBundleFile.readText()

            if (jsBundleFileContents.contains("http://localhost:8080/")) {
                throw GradleException("JS production bundle contains localhost URL. File ${jsBundleFile.absolutePath}")
            }
        } else {
            throw GradleException("Could not find JS bundle file in directory ${jsDistDir.absolutePath}")
        }
    }
}
