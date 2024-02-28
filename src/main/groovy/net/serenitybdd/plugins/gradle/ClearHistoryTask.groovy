package net.serenitybdd.plugins.gradle

import org.apache.commons.io.FileUtils
import org.gradle.api.file.ProjectLayout
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject
import java.nio.file.Path

class ClearHistoryTask extends SerenityAbstractTask {

    @OutputDirectory
    abstract Path historyDirectory;

    @Inject
    ClearHistoryTask(ProjectLayout layout) {
        super(layout)
    }

    @TaskAction
    void clearHistory() {
        FileUtils.deleteDirectory(historyDirectory.toFile())
    }
}
