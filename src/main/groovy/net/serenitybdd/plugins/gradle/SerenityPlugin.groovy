package net.serenitybdd.plugins.gradle

import net.serenitybdd.core.history.FileSystemTestOutcomeSummaryRecorder
import net.thucydides.core.ThucydidesSystemProperty
import net.thucydides.core.configuration.SystemPropertiesConfiguration
import net.thucydides.core.guice.Injectors
import net.thucydides.core.reports.ExtendedReport
import net.thucydides.core.reports.ExtendedReports
import net.thucydides.core.reports.ResultChecker
import net.thucydides.core.reports.html.HtmlAggregateStoryReporter
import net.thucydides.core.requirements.DefaultRequirements
import net.thucydides.core.webdriver.Configuration
import org.apache.commons.io.FileUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class SerenityPlugin implements Plugin<Project> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    void apply(Project project) {
        updateSystemPath(project)
        project.pluginManager.apply(JavaPlugin.class)
        project.extensions.create("serenity", SerenityPluginExtension)

        def aggregateTask = project.tasks.register('aggregate') {
            group = 'Serenity BDD'
            description = 'Generates aggregated Serenity reports'
            doLast {
                updateProperties(project)
                Path reportDirectory = prepareReportDirectory(project)

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

                if (project.serenity.generateOutcomes) {
                    reporter.setGenerateTestOutcomeReports();
                }
                reporter.generateReportsForTestResultsFrom(reporter.outputDirectory)
                new ResultChecker(reporter.outputDirectory).checkTestResults();
            }
        }

        def reportsTask = project.tasks.register('reports') {
            group = 'Serenity BDD'
            description = 'Generates extended Serenity reports'
            doLast {
                updateProperties(project)
                def reportDirectory = prepareReportDirectory(project)
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
                        URI reportPath = absolutePathOf(report.generateReport()).toUri()
                        logger.lifecycle("  - ${report.description}: ${reportPath}")
                    }
                }

                ResultChecker resultChecker = new ResultChecker(reportDirectory.toFile())
                resultChecker.checkTestResults()
            }
        }

        def checkOutcomesTask = project.tasks.register('checkOutcomes') {
            group = 'Serenity BDD'
            description = "Checks the Serenity reports and fails the build if there are test failures (run automatically with 'check')"

            Path reportDirectory = prepareReportDirectory(project)

            inputs.files(project.fileTree(reportDirectory))

            doLast {
                updateProperties(project)
                logger.lifecycle("Checking serenity results for ${project.serenity.projectKey} in directory $reportDirectory")
                if (reportDirectory.toFile().exists()) {
                    def checker = new ResultChecker(reportDirectory.toFile())
                    checker.checkTestResults()
                }
            }
        }

        def clearReportsTask = project.tasks.register('clearReports') {
            group = 'Serenity BDD'
            description = "Deletes the Serenity output directory (run automatically with 'clean')"

            doLast {
                updateProperties(project)
                def reportDirectory = prepareReportDirectory(project)
                FileUtils.deleteDirectory(reportDirectory.toFile())
            }
        }

        def clearHistoryTask = project.tasks.register('clearHistory') {
            group = 'Serenity BDD'
            description = "Deletes the Serenity history directory"

            doLast {
                updateProperties(project)
                def historyDirectory = prepareHistoryDirectory(project)
                Files.delete(historyDirectory)
            }
        }

        def historyTask = project.tasks.register('history') {
            group = 'Serenity BDD'
            description = "Records a summary of test outcomes to be used for comparison in the next test run"

            doLast {
                updateProperties(project)
                def historyDirectory = prepareHistoryDirectory(project)

                new FileSystemTestOutcomeSummaryRecorder(historyDirectory,
                        deletePreviousHistory())
                        .recordOutcomeSummariesFrom(project.serenity.sourceDirectory);

            }
        }

        project.tasks.named('checkOutcomes').configure {
            mustRunAfter aggregateTask
        }

        project.tasks.named('test').configure {
            finalizedBy aggregateTask
        }

        project.tasks.named('clean').configure {
            dependsOn clearReportsTask
        }

        project.tasks.named('check').configure {
            dependsOn checkOutcomesTask
        }
    }

    static Path absolutePathOf(Path path) {
        return Paths.get(System.getProperty("user.dir")).resolve(path)
    }

    static Path prepareReportDirectory(Project project) {
        Path outputDir = Paths.get(project.serenity.outputDirectory)
        if (!outputDir.isAbsolute()) {
            outputDir = project.projectDir.toPath().resolve(outputDir)
        }
        return outputDir
    }

    static Path prepareHistoryDirectory(Project project) {
        def outputDir = Paths.get(project.serenity.historyDirectory)
        if (!outputDir.isAbsolute()) {
            outputDir = project.projectDir.toPath().resolve(outputDir)
        }
        return outputDir
    }

    static def updateSystemPath(Project project) {
        System.properties['project.build.directory'] = project.projectDir.getAbsolutePath()
        SystemPropertiesConfiguration configuration = (SystemPropertiesConfiguration) Injectors.getInjector().getProvider(Configuration.class).get()
        configuration.getEnvironmentVariables().setProperty('project.build.directory', project.projectDir.getAbsolutePath())
        configuration.reloadOutputDirectory()
    }

    static Boolean deletePreviousHistory() {
        SystemPropertiesConfiguration configuration = (SystemPropertiesConfiguration) Injectors.getInjector().getProvider(Configuration.class).get()
        return ThucydidesSystemProperty.DELETE_HISTORY_DIRECTORY.booleanFrom(configuration.environmentVariables, true);
//        return configuration.environmentVariables.getPropertyAsBoolean(ThucydidesSystemProperty.DELETE_HISTORY_DIRECTORY, true)
    }


    static def updateProperties(Project project) {
        updateSystemPath(project)
        def config = Injectors.getInjector().getProvider(Configuration.class).get()
        project.serenity.outputDirectory = config.getOutputDirectory().toPath()
        project.serenity.sourceDirectory = config.getOutputDirectory().toPath()
    }

}
