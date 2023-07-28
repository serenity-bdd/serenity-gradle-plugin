package net.serenitybdd.plugins.gradle

import net.serenitybdd.core.di.SerenityInfrastructure;

class SerenityPluginExtension {
    private final def configuration = SerenityInfrastructure.getConfiguration()
    String outputDirectory = configuration.getOutputDirectory()
    String historyDirectory = configuration.getHistoryDirectory()
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
