package net.serenitybdd.plugins.gradle

import org.apache.commons.io.FileUtils
import org.gradle.api.file.ProjectLayout
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject
import java.nio.file.Path

abstract class ClearReportsTask extends SerenityAbstractTask {

    @OutputDirectory
    abstract Path reportDirectory;

    @Inject
    ClearReportsTask(ProjectLayout layout) {
        super(layout)
    }

    @TaskAction
    void clearReports() {
        FileUtils.deleteDirectory(reportDirectory.toFile())
    }

}
