package net.serenitybdd.plugins.gradle

import net.serenitybdd.model.history.FileSystemTestOutcomeSummaryRecorder
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject
import java.nio.file.Path

abstract class HistoryTask extends SerenityAbstractTask {

    @InputDirectory
    abstract Path sourceDirectory;

    @Input
    abstract Property<Boolean> getDeletePreviousHistory()

    @OutputDirectory
    abstract Path historyDirectory;

    @Inject
    HistoryTask(ProjectLayout layout) {
        super(layout)
    }

    @TaskAction
    void history() {
        updateLayoutPaths()
        new FileSystemTestOutcomeSummaryRecorder(historyDirectory,
                getDeletePreviousHistory().get())
                .recordOutcomeSummariesFrom(sourceDirectory);

    }
}
