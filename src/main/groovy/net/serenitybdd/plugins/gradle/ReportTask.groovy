package net.serenitybdd.plugins.gradle

import net.thucydides.core.reports.ExtendedReport
import net.thucydides.core.reports.ExtendedReports
import net.thucydides.model.reports.ResultChecker
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject
import java.nio.file.Path

abstract class ReportTask extends SerenityAbstractTask {

    @Input
    abstract Property<String> getProjectKey()

    @Optional @Input
    abstract Property<String> getRequirementsBaseDir()

    @Optional @Input
    abstract Property<String> getTestRoot()

    @Optional @Input
    abstract ListProperty<String> getReports()

    @OutputDirectory
    abstract Path reportDirectory;

    @Inject
    ReportTask(ProjectLayout layout) {
        super(layout)
    }

    @TaskAction
    void report() {
        updateLayoutPaths()
        logger.lifecycle("Generating Additional Serenity Reports for ${getProjectKey().get()} to directory $reportDirectory")
        System.properties['serenity.project.key'] = getProjectKey()
        if (getTestRoot().isPresent()) {
            System.properties['serenity.test.root'] = getTestRoot().get()
        }
        if (getRequirementsBaseDir().isPresent()) {
            System.properties['serenity.test.requirements.basedir'] = getRequirementsBaseDir()
        }
        List<String> extendedReportTypes = getReports().getOrElse(Collections.emptyList())
        for (ExtendedReport report : ExtendedReports.named(extendedReportTypes)) {
            report.sourceDirectory = reportDirectory
            report.outputDirectory = reportDirectory
            URI reportPath = absolutePathOf(report.generateReport()).toUri()
            logger.lifecycle("  - ${report.description}: ${reportPath}")
        }

        ResultChecker resultChecker = new ResultChecker(reportDirectory.toFile())
        resultChecker.checkTestResults()
    }
}
