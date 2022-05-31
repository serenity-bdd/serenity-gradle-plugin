package net.serenitybdd.plugins.gradle


import net.thucydides.core.guice.Injectors
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin

class SerenityPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        Injectors.setDefaultModule(new SerenityPluginModule())

        SerenityAbstractTask.updateSystemPath(project)
        project.pluginManager.apply(JavaPlugin.class)
        project.extensions.create("serenity", SerenityPluginExtension)

        def aggregate = project.tasks.register('aggregate', AggregateTask) {
            group = 'Serenity BDD'
            description = 'Generates aggregated Serenity reports'
        }

        def reports = project.tasks.register('reports', ReportTask) {
            group = 'Serenity BDD'
            description = 'Generates extended Serenity reports'
        }

        def checkOutcomes = project.tasks.register('checkOutcomes', CheckOutcomesTask) {
            group = 'Serenity BDD'
            description = "Checks the Serenity reports and fails the build if there are test failures (run automatically with 'check')"
        }

        def clearReports = project.tasks.register('clearReports', ClearReportsTask) {
            group = 'Serenity BDD'
            description = "Deletes the Serenity output directory (run automatically with 'clean')"
        }

        def clearHistory = project.tasks.register('clearHistory', ClearHistoryTask) {
            group = 'Serenity BDD'
            description = "Deletes the Serenity history directory"
        }

        def history = project.tasks.register('history', HistoryTask) {
            group = 'Serenity BDD'
            description = "Records a summary of test outcomes to be used for comparison in the next test run"
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
}
