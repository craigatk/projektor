package projektor.plugin.file

class DirectoryUtil {
    static String findSubDirectoryPath(File baseDir, File subDirectory) {
        int baseDirPathLengthToSkip = baseDir.absolutePath.size() + 1
        String subDirectoryAbsolutePath = subDirectory.absolutePath

        if (subDirectoryAbsolutePath.size() > baseDirPathLengthToSkip) {
            return subDirectoryAbsolutePath.substring(baseDirPathLengthToSkip).replaceAll("\\\\", "/")
        } else {
            return ""
        }

    }
}
