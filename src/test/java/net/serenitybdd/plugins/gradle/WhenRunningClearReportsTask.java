package net.serenitybdd.plugins.gradle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

public class WhenRunningClearReportsTask {

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
    void without_configuration_deletes_default_reports_directory() throws IOException {
        final var reportsDirectory = testProjectDir.resolve("target/site/serenity");
        Files.createDirectories(reportsDirectory);
        Files.createFile(reportsDirectory.resolve("dummyfile"));

        var result = runClearReportsTask();

        var outcome = result.task(":clearReports").getOutcome();
        assertThat(outcome).isEqualTo(TaskOutcome.SUCCESS);
        assertThat(Files.exists(reportsDirectory)).isFalse();
    }

    private BuildResult runClearReportsTask() {
        return GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
                .withArguments("clearReports")
                .withPluginClasspath()
                .build();
    }
}

