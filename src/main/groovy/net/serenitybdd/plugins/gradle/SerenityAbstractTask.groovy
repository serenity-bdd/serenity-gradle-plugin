package net.serenitybdd.plugins.gradle

import net.serenitybdd.core.di.SerenityInfrastructure
import net.thucydides.model.configuration.SystemPropertiesConfiguration
import org.gradle.api.DefaultTask
import org.gradle.api.file.ProjectLayout

import javax.inject.Inject
import java.nio.file.Path
import java.nio.file.Paths

class SerenityAbstractTask extends DefaultTask {

    protected final ProjectLayout layout;

    @Inject
    SerenityAbstractTask(ProjectLayout layout) {
        this.layout = layout
    }

    static Path absolutePathOf(Path path) {
        return Paths.get(System.getProperty("user.dir")).resolve(path)
    }

    void updateSystemPath() {
        def projectBuildDirectory = layout.projectDirectory.asFile.absolutePath
        System.properties['project.build.directory'] = projectBuildDirectory
        SystemPropertiesConfiguration configuration = SerenityInfrastructure.getConfiguration()
        configuration.getEnvironmentVariables().setProperty('project.build.directory', projectBuildDirectory)
        configuration.reloadOutputDirectory()
    }

}
