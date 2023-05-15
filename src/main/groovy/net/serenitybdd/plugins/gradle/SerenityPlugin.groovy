package net.serenitybdd.plugins.gradle

import net.serenitybdd.core.di.SerenityInfrastructure
import net.serenitybdd.model.di.ModelInfrastructure
import net.thucydides.model.ThucydidesSystemProperty
import net.thucydides.model.configuration.SystemPropertiesConfiguration
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ProjectLayout
import org.gradle.api.plugins.JavaPlugin

import java.nio.file.Files
import java.nio.file.Path

class SerenityPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.pluginManager.apply(JavaPlugin.class)
        def layout = project.layout
        updateReportDirectory(layout)
        def extension = project.extensions.create("serenity", SerenityPluginExtension)

        def aggregate = project.tasks.register('aggregate', AggregateTask) {
            group = 'Serenity BDD'
            description = 'Generates aggregated Serenity reports'

            projectKey = extension.projectKey ?: project.name
            reportDirectory = getReportDirectory(layout, extension)
            testRoot = extension.testRoot
            requirementsBaseDir = extension.requirementsBaseDir
            requirementsDir = extension.requirementsDir
            issueTrackerUrl = extension.issueTrackerUrl
            jiraUrl = extension.jiraUrl
            jiraProject = extension.jiraProject
            generateOutcomes = extension.generateOutcomes

            outputs.cacheIf( { false })
        }

        project.tasks.register('reports', ReportTask) {
            group = 'Serenity BDD'
            description = 'Generates extended Serenity reports'

            projectKey = extension.projectKey ?: project.name
            reportDirectory = getReportDirectory(layout, extension)
            testRoot = extension.testRoot
            reports = extension.reports
        }

        def checkOutcomes = project.tasks.register('checkOutcomes', CheckOutcomesTask) {
            group = 'Serenity BDD'
            description = "Checks the Serenity reports and fails the build if there are test failures (run automatically with 'check')"

            def extensionReportDirectory = getReportDirectory(layout, extension)
            reportDirectory = extensionReportDirectory
            projectKey = extension.projectKey ?: project.name

            onlyIf { Files.exists(extensionReportDirectory) }
        }

        def clearReports = project.tasks.register('clearReports', ClearReportsTask) {
            group = 'Serenity BDD'
            description = "Deletes the Serenity output directory (run automatically with 'clean')"

            def extensionReportDirectory = getReportDirectory(layout, extension)

            reportDirectory = extensionReportDirectory

            onlyIf { Files.exists(extensionReportDirectory) }
        }

        project.tasks.register('clearHistory', ClearHistoryTask) {
            group = 'Serenity BDD'
            description = "Deletes the Serenity history directory"

            def extensionHistoryDirectory = getHistoryDirectory(layout, extension)

            historyDirectory = extensionHistoryDirectory

            onlyIf { Files.exists(extensionHistoryDirectory) }
        }

        project.tasks.register('history', HistoryTask) {
            group = 'Serenity BDD'
            description = "Records a summary of test outcomes to be used for comparison in the next test run"

            def extensionSourceDirectory = getSourceDirectory(layout, extension)

            historyDirectory = getHistoryDirectory(layout, extension)
            sourceDirectory = extensionSourceDirectory
            deletePreviousHistory = deletePreviousHistory()

            onlyIf { Files.exists(extensionSourceDirectory) }
        }

        project.tasks.named('checkOutcomes').configure {
            mustRunAfter aggregate
        }

        project.tasks.named('test').configure {
            finalizedBy aggregate
        }

        project.tasks.named('clean').configure {
            dependsOn clearReports
        }

        project.tasks.named('check').configure {
            dependsOn checkOutcomes
        }
    }

    static void updateReportDirectory(ProjectLayout layout) {
        // Set the project directory for use in the reporting tasks
        ModelInfrastructure.configuration.setProjectDirectory(layout.getProjectDirectory().getAsFile().toPath())
    }

    static Path getHistoryDirectory(ProjectLayout layout, SerenityPluginExtension extension) {
        return toAbsolute(new File(extension.historyDirectory), layout)
    }

    static Path getReportDirectory(ProjectLayout layout, SerenityPluginExtension extension) {
        return toAbsolute(new File(extension.outputDirectory), layout)
    }

    static Path getSourceDirectory(ProjectLayout layout, SerenityPluginExtension extension) {
        return toAbsolute(new File(extension.outputDirectory), layout)
    }

    static Path toAbsolute(File file, ProjectLayout layout) {
        Path path = file.toPath()
        if (!path.isAbsolute()) {
            return layout.projectDirectory.dir(file.toString()).asFile.toPath()
        }
        return path
    }

    static Boolean deletePreviousHistory() {
        SystemPropertiesConfiguration configuration = SerenityInfrastructure.getConfiguration()
        return ThucydidesSystemProperty.DELETE_HISTORY_DIRECTORY.booleanFrom(configuration.environmentVariables, true);
    }

}
