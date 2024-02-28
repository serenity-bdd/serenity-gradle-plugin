package net.serenitybdd.plugins.gradle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

public class WhenRunningReportTask {

    @TempDir
    Path testProjectDir;
    Path buildFile;

    @BeforeEach
    void setup() throws IOException {
        buildFile = testProjectDir.resolve("build.gradle");
        Files.writeString(buildFile, """
            plugins {
                id 'net.serenity-bdd.serenity-gradle-plugin'
            }
        """);
    }

    @Test
    void without_configuration_logs_null_test_root() {
        var result = runAggregateTask();

        var output = result.getOutput();
        assertThat(output).contains("Generating Additional Serenity Reports for ",
                " to directory ",
                Path.of("target", "site", "serenity").toString());
    }

    private BuildResult runAggregateTask() {
        return GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
                .withArguments("report")
                .withPluginClasspath()
                .build();
    }
}
