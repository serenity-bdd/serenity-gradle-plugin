package net.serenitybdd.plugins.gradle

import net.serenitybdd.core.di.SerenityInfrastructure
import net.thucydides.core.reports.html.HtmlAggregateStoryReporter
import net.thucydides.model.configuration.SystemPropertiesConfiguration
import net.thucydides.model.reports.ResultChecker
import net.thucydides.model.requirements.DefaultRequirements
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject
import java.nio.file.Path

abstract class AggregateTask extends SerenityAbstractTask {

    @Input
    abstract Property<String> getProjectKey()

    @Optional @Input
    abstract Property<String> getTestRoot()

    @Optional @Input
    abstract Property<String> getRequirementsBaseDir()

    @Optional @Input
    abstract Property<String> getRequirementsDir()

    @Optional @Input
    abstract Property<String> getIssueTrackerUrl()

    @InputFiles
    abstract Collection testResults

    @Optional @Input
    abstract Property<String> getJiraUrl()

    @Optional @Input
    abstract Property<String> getJiraProject()

    @Input
    abstract Property<Boolean> getGenerateOutcomes()

    @OutputDirectory
    abstract Path reportDirectory

    @Inject
    AggregateTask(ProjectLayout layout) {
        super(layout)
    }

    @TaskAction
    void aggregate() {
        updateLayoutPaths()
        def testRoot = getTestRoot().getOrNull()
        logger.lifecycle("Generating Serenity Reports")

        if (testRoot) {
            logger.lifecycle("  - Test Root: ${testRoot}")
            System.properties['serenity.test.root'] = testRoot
        }
        URI mainReportPath = absolutePathOf(reportDirectory.resolve("index.html")).toUri()
        def requirementsBaseDir = getRequirementsBaseDir().getOrNull()
        logger.lifecycle("  - Main report: $mainReportPath")
        logger.lifecycle("      - Test Root: ${testRoot}")
        logger.lifecycle("      - Requirements base directory: ${requirementsBaseDir}")

        System.properties['serenity.project.key'] = getProjectKey()
        if (requirementsBaseDir) {
            System.properties['serenity.test.requirements.basedir'] = requirementsBaseDir
        }
        def requirementsDir = getRequirementsDir().getOrNull()
        if (requirementsDir) {

            SystemPropertiesConfiguration configuration = SerenityInfrastructure.getConfiguration() as SystemPropertiesConfiguration
            configuration.getEnvironmentVariables().setProperty('serenity.requirements.dir', requirementsDir)
        }

        def requirements = (testRoot) ? new DefaultRequirements(testRoot) : new DefaultRequirements()

        def reporter = new HtmlAggregateStoryReporter(getProjectKey().get(), requirements)
        reporter.outputDirectory = reportDirectory.toFile()
        reporter.testRoot = testRoot
        reporter.projectDirectory = layout.projectDirectory.asFile.absolutePath
        reporter.issueTrackerUrl = getIssueTrackerUrl().getOrNull()
        reporter.jiraUrl = getJiraUrl().getOrNull()
        reporter.jiraProject = getJiraProject().getOrNull()

        if (getGenerateOutcomes().get()) {
            reporter.setGenerateTestOutcomeReports();
        }
        reporter.generateReportsForTestResultsFrom(reporter.outputDirectory)
        new ResultChecker(reporter.outputDirectory).checkTestResults()
    }
}
