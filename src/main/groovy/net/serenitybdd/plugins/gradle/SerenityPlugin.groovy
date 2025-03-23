package net.serenitybdd.plugins.gradle

import net.serenitybdd.core.di.SerenityInfrastructure
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
        updateLayoutPaths(layout)
        def extension = project.extensions.create("serenity", SerenityPluginExtension)

        def aggregate = project.tasks.register('aggregate', AggregateTask) {
            updateLayoutPaths(layout)
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

            outputs.dir(reportDirectory)
            outputs.cacheIf( { false })
        }

        def reports = project.tasks.register('reports', ReportTask) {
            updateLayoutPaths(layout)
            group = 'Serenity BDD'
            description = 'Generates extended Serenity reports'

            projectKey = extension.projectKey ?: project.name
            reportDirectory = getReportDirectory(layout, extension)
            testRoot = extension.testRoot
            reports = extension.reports
        }

        def checkOutcomes = project.tasks.register('checkOutcomes', CheckOutcomesTask) {
            updateLayoutPaths(layout)
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

        reports.configure {
            mustRunAfter clearReports
        }

        aggregate.configure {
            mustRunAfter clearReports
        }

        checkOutcomes.configure {
            mustRunAfter aggregate
        }

        project.tasks.named('test').configure {
            // Intentionally removed automatic report aggregation after tests
            // finalizedBy aggregate
        }

        project.tasks.named('clean').configure {
            dependsOn clearReports
        }

        project.tasks.named('check').configure {
            dependsOn checkOutcomes
        }
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

    static void updateLayoutPaths(ProjectLayout layout) {
        def projectBuildDirectory = layout.projectDirectory.asFile.absolutePath
        System.properties['project.build.directory'] = projectBuildDirectory
        SystemPropertiesConfiguration configuration = SerenityInfrastructure.getConfiguration()
        configuration.getEnvironmentVariables().setProperty('project.build.directory', projectBuildDirectory)
        configuration.setProjectDirectory(layout.projectDirectory.asFile.toPath())
        configuration.setOutputDirectory(null)
        configuration.reloadOutputDirectory()
    }
}
