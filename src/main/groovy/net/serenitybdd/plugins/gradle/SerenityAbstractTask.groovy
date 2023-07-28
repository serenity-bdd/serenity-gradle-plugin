package net.serenitybdd.plugins.gradle

import net.thucydides.core.ThucydidesSystemProperty
import net.thucydides.core.configuration.SystemPropertiesConfiguration
import net.serenitybdd.core.di.SerenityInfrastructure;
import net.thucydides.core.webdriver.Configuration
import org.gradle.api.DefaultTask
import org.gradle.api.Project

import java.nio.file.Path
import java.nio.file.Paths

class SerenityAbstractTask extends DefaultTask {
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
        SystemPropertiesConfiguration configuration = SerenityInfrastructure.getConfiguration()
        configuration.getEnvironmentVariables().setProperty('project.build.directory', project.projectDir.getAbsolutePath())
        configuration.reloadOutputDirectory()
    }

    static Boolean deletePreviousHistory() {
        SystemPropertiesConfiguration configuration = SerenityInfrastructure.getConfiguration()
        return ThucydidesSystemProperty.DELETE_HISTORY_DIRECTORY.booleanFrom(configuration.environmentVariables, true);
//        return configuration.environmentVariables.getPropertyAsBoolean(ThucydidesSystemProperty.DELETE_HISTORY_DIRECTORY, true)
    }

    static def updateProperties(Project project) {
        updateSystemPath(project)
        def config = SerenityInfrastructure.getConfiguration()
        project.serenity.outputDirectory = config.getOutputDirectory().toPath()
        project.serenity.sourceDirectory = config.getOutputDirectory().toPath()
    }
}
