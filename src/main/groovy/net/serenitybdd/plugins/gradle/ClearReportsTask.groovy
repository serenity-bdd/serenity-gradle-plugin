package net.serenitybdd.plugins.gradle

import org.apache.commons.io.FileUtils
import org.gradle.api.tasks.TaskAction

class ClearReportsTask extends SerenityAbstractTask {

    @TaskAction
    void clearReports() {
        SerenityAbstractTask.updateProperties(project)
        def reportDirectory = SerenityAbstractTask.prepareReportDirectory(project)
        FileUtils.deleteDirectory(reportDirectory.toFile())
    }

}
