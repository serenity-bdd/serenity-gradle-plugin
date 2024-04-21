Serenity-Gradle-Plugin
======================

Serenity-Gradle-Plugin adding tasks to generate reports from serenity test-results.   

Requirements
============

* gradle 7.0+

Gradle Tasks
============

* `gradle aggregate` Generates aggregated Serenity reports
* `gradle reports` Generates extended Serenity reports
* `gradle checkOutcomes ` Checks the Serenity reports and fails the build if there are test failures (run automatically with 'check')
* `gradle clearReports` Deletes the Serenity output directory (run automatically with 'clean')

Example 'build.gradle'
======================

```
plugins {    
    id 'net.serenity-bdd.serenity-gradle-plugin' version '4.1.9'
}

serenity {
    outputDirectory = null      // String, report-directory
    projectKey = null           // String, overwrites 'serenity.project.key'
    issueTrackerUrl = null      // String, base URL for the issue tracking system to be referred to in the reports
    jiraUrl = null              // String, If the base JIRA URL is defined, Serenity will build the issue tracker url using the standard JIRA form
    jiraProject = null          // String, ff defined, the JIRA project id will be prepended to issue numbers
    requirementsBaseDir = null  // String, overwrites 'serenity.test.requirements.basedir'
    requirementsDir = null      // String, overwrites 'serenity.requirements.dir'             
    testRoot = null             // String, overwrites 'serenity.test.root' 
    generateOutcomes = true     // generate report
    reports = []                // extended report types for reports-task    
}
```
