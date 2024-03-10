package net.serenitybdd.plugins.gradle

import org.gradle.api.tasks.TaskAction

import java.nio.file.Files

class ClearHistoryTask extends SerenityAbstractTask {

    @TaskAction
    void clearHistory() {
        SerenityAbstractTask.updateProperties(project)
        def historyDirectory = SerenityAbstractTask.prepareHistoryDirectory(project)
        Files.delete(historyDirectory)
    }
}
