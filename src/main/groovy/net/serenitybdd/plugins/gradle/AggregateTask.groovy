package net.serenitybdd.plugins.gradle

import net.thucydides.core.configuration.SystemPropertiesConfiguration
import net.thucydides.core.guice.Injectors
import net.thucydides.core.reports.ResultChecker
import net.thucydides.core.reports.html.HtmlAggregateStoryReporter
import net.thucydides.core.requirements.DefaultRequirements
import net.thucydides.core.webdriver.Configuration
import org.gradle.api.tasks.TaskAction

import java.nio.file.Path

class AggregateTask extends SerenityAbstractTask {
    @TaskAction
    void aggregate() {
        updateProperties(project)
        Path reportDirectory = SerenityAbstractTask.prepareReportDirectory(project)

        if (!project.serenity.projectKey) {
            project.serenity.projectKey = project.name
        }
        logger.lifecycle("Generating Serenity Reports")
        String testRoot = project.serenity.testRoot

        if (project.serenity.testRoot) {
            logger.lifecycle("  - Test Root: ${project.serenity.testRoot}")
            System.properties['serenity.test.root'] = project.serenity.testRoot
        }
        URI mainReportPath = absolutePathOf(reportDirectory.resolve("index.html")).toUri()
        logger.lifecycle("  - Main report: $mainReportPath")
        logger.lifecycle("      - Test Root: ${project.serenity.testRoot}")
        logger.lifecycle("      - Requirements base directory: ${project.serenity.requirementsBaseDir}")

        System.properties['serenity.project.key'] = project.serenity.projectKey
        if (project.serenity.requirementsBaseDir) {
            System.properties['serenity.test.requirements.basedir'] = project.serenity.requirementsBaseDir
        }
        if (project.serenity.requirementsDir) {
            SystemPropertiesConfiguration configuration = (SystemPropertiesConfiguration) Injectors.getInjector().getProvider(Configuration.class).get()
            configuration.getEnvironmentVariables().setProperty('serenity.requirements.dir', project.serenity.requirementsDir)
        }

        def reporter

        def requirements = (project.serenity.testRoot) ? new DefaultRequirements(project.serenity.testRoot) : new DefaultRequirements()

        reporter = new HtmlAggregateStoryReporter(project.serenity.projectKey, requirements)
        reporter.outputDirectory = reportDirectory.toFile()
        reporter.testRoot = project.serenity.testRoot
        reporter.projectDirectory = project.projectDir.absolutePath
        reporter.issueTrackerUrl = project.serenity.issueTrackerUrl
        reporter.jiraUrl = project.serenity.jiraUrl
        reporter.jiraProject = project.serenity.jiraProject

        reporter.setGenerateTestOutcomeReports();
        reporter.generateReportsForTestResultsFrom(reporter.outputDirectory)
        new ResultChecker(reporter.outputDirectory).checkTestResults();
    }
}
