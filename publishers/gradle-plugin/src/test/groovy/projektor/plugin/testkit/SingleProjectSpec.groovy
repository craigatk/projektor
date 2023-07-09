package projektor.plugin.testkit

import projektor.plugin.BuildFileWriter
import projektor.plugin.ProjectBuildFileConfig

abstract class SingleProjectSpec extends ProjectSpec {
    File buildFile

    def setup() {
        buildFile = BuildFileWriter.createProjectBuildFile(
                projectRootDir,
                new ProjectBuildFileConfig(
                        includeProjektorPlugin: true,
                        includeJacocoPlugin: includeJacocoPlugin(),
                        includeKoverPlugin: includeKoverPlugin(),
                        includeCodeNarcPlugin: includeCodenarcPlugin()
                )
        )
    }
}
