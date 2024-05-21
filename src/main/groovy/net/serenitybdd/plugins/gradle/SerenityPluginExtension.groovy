package net.serenitybdd.plugins.gradle

import net.serenitybdd.core.di.SerenityInfrastructure
import net.thucydides.model.configuration.SystemPropertiesConfiguration
import net.thucydides.model.webdriver.Configuration;

class SerenityPluginExtension {
    SerenityPluginExtension() {
        // needs SerenityPlugin.updateLayoutPaths()
        def configuration = SerenityInfrastructure.getConfiguration()
        outputDirectory = configuration.getOutputDirectory()
    }
    String outputDirectory
    String projectKey
    String issueTrackerUrl
    String jiraUrl
    String jiraProject
    String requirementsBaseDir
    String requirementsDir
    String testRoot
    List<String> reports
}
