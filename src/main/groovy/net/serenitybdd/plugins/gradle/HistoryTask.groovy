package net.serenitybdd.plugins.gradle

import net.serenitybdd.model.history.FileSystemTestOutcomeSummaryRecorder
import org.gradle.api.tasks.TaskAction

class HistoryTask extends SerenityAbstractTask {

    @TaskAction
    void history() {
        SerenityAbstractTask.updateProperties(project)
        def historyDirectory = SerenityAbstractTask.prepareHistoryDirectory(project)

        new FileSystemTestOutcomeSummaryRecorder(historyDirectory,
                SerenityAbstractTask.deletePreviousHistory())
                .recordOutcomeSummariesFrom(project.serenity.sourceDirectory);

    }
}
