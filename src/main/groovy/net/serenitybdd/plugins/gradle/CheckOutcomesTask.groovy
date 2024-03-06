package net.serenitybdd.plugins.gradle

import net.thucydides.model.reports.ResultChecker
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject
import java.nio.file.Files
import java.nio.file.Path

abstract class CheckOutcomesTask extends SerenityAbstractTask {

    @InputDirectory
    abstract Path reportDirectory;

    @Input @Optional
    abstract Property<String> getProjectKey();

    @Inject
    CheckOutcomesTask(ProjectLayout layout) {
        super(layout)
    }

    @TaskAction
    void checkOutcomes() {
        updateLayoutPaths()
        logger.lifecycle("Checking serenity results for ${getProjectKey().get()} in directory $reportDirectory")
        if (Files.exists(reportDirectory)) {
            def checker = new ResultChecker(reportDirectory.toFile())
            checker.checkTestResults()
        }
    }
}
