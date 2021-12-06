package projektor.plugin.testkit

import projektor.plugin.BuildFileWriter

abstract class SingleProjectSpec extends ProjectSpec {
    File buildFile

    def setup() {
        buildFile = BuildFileWriter.createProjectBuildFile(
                projectRootDir,
                true,
                includeJacocoPlugin(),
                includeKoverPlugin(),
                includeCodenarcPlugin()
        )
    }
}
