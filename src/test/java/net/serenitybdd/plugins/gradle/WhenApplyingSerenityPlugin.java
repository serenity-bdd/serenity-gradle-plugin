package net.serenitybdd.plugins.gradle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;

class WhenApplyingSerenityPlugin {

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

    @ParameterizedTest
    @ValueSource(strings = {"aggregate", "reports", "checkOutcomes", "history", "clearReports", "clearHistory" })
    void it_succesfully_runs_all_tasks_without_configuration(final String taskName) throws IOException {
        Files.createDirectories(testProjectDir.resolve("history"));
        Files.createDirectories(testProjectDir.resolve("target/site/serenity"));

        var result = runTask(taskName);

        assertEquals(SUCCESS, result.task(":" + taskName).getOutcome());
    }

    private BuildResult runTask(final String taskName) {
        return GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
                .withArguments(taskName, "-i")
                .withPluginClasspath()
                .build();
    }
}

