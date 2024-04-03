package net.serenitybdd.plugins.gradle


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

    void updateLayoutPaths() {
        SerenityPlugin.updateLayoutPaths(layout)
    }
}
