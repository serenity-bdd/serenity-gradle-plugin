package net.serenitybdd.plugins.gradle

import net.serenitybdd.core.di.SerenityInfrastructure
import net.thucydides.model.configuration.SystemPropertiesConfiguration
import net.thucydides.model.webdriver.Configuration;

class SerenityPluginExtension {
    SerenityPluginExtension() {
        // needs SerenityPlugin.updateLayoutPaths()
        def configuration = SerenityInfrastructure.getConfiguration()
        outputDirectory = configuration.getOutputDirectory()
        historyDirectory = configuration.getHistoryDirectory()
        println("!!!!   " + outputDirectory)
    }
    String outputDirectory
    String historyDirectory
    String projectKey
    String issueTrackerUrl
    String jiraUrl
    String jiraProject
    String sourceDirectory = outputDirectory
    String requirementsBaseDir
    String requirementsDir
    String testRoot
    boolean generateOutcomes
    List<String> reports
}
