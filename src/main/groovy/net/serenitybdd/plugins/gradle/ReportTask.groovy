package net.serenitybdd.plugins.gradle

import net.thucydides.core.reports.ExtendedReport
import net.thucydides.core.reports.ExtendedReports
import net.thucydides.model.reports.ResultChecker
import org.gradle.api.tasks.TaskAction

class ReportTask extends SerenityAbstractTask {

    @TaskAction
    void report() {
        SerenityAbstractTask.updateProperties(project)
        def reportDirectory = SerenityAbstractTask.prepareReportDirectory(project)
        if (!project.serenity.projectKey) {
            project.serenity.projectKey = project.name
        }

        logger.lifecycle("Generating Additional Serenity Reports for ${project.serenity.projectKey} to directory $reportDirectory")
        System.properties['serenity.project.key'] = project.serenity.projectKey
        if (project.serenity.testRoot) {
            System.properties['serenity.test.root'] = project.serenity.testRoot
        }
        if (project.serenity.requirementsBaseDir) {
            System.properties['serenity.test.requirements.basedir'] = project.serenity.requirementsBaseDir
        }
        List<String> extendedReportTypes = project.serenity.reports
        if (extendedReportTypes) {
            for (ExtendedReport report : ExtendedReports.named(extendedReportTypes)) {
                report.sourceDirectory = reportDirectory
                report.outputDirectory = reportDirectory
                URI reportPath = SerenityAbstractTask.absolutePathOf(report.generateReport()).toUri()
                logger.lifecycle("  - ${report.description}: ${reportPath}")
            }
        }

        ResultChecker resultChecker = new ResultChecker(reportDirectory.toFile())
        resultChecker.checkTestResults()
    }
}
