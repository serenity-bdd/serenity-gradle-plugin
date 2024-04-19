package net.serenitybdd.plugins.gradle

import org.apache.commons.io.FileUtils
import org.gradle.api.file.ProjectLayout
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject
import java.nio.file.Path

class ClearReportsTask extends SerenityAbstractTask {

    @Inject
    ClearReportsTask(ProjectLayout layout) {
        super(layout)
    }

    @InputDirectory
    abstract Path reportDirectory;

    @TaskAction
    void clearReports() {
        FileUtils.deleteDirectory(reportDirectory.toFile())
    }

}
