package net.serenitybdd.plugins.gradle

import net.thucydides.core.reports.ResultChecker
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

import java.nio.file.Path

class CheckOutcomesTask extends SerenityAbstractTask {
    @InputFiles
    FileCollection getReportFiles() {
        Path reportDirectory = SerenityAbstractTask.prepareReportDirectory(project)

        return project.fileTree(reportDirectory)
    }

    @TaskAction
    void checkOutcomes() {
        Path reportDirectory = SerenityAbstractTask.prepareReportDirectory(project)

        SerenityAbstractTask.updateProperties(project)
        logger.lifecycle("Checking serenity results for ${project.serenity.projectKey} in directory $reportDirectory")
        if (reportDirectory.toFile().exists()) {
            def checker = new ResultChecker(reportDirectory.toFile())
            checker.checkTestResults()
        }
    }
}
