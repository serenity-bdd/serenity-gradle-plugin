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

public class WhenRunningClearHistoryTask {

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
    void without_configuration_deletes_default_history_directory() throws IOException {
        final var historyDirectory = testProjectDir.resolve("history");
        Files.createDirectories(historyDirectory);
        Files.createFile(historyDirectory.resolve("dummyfile"));

        var result = runClearHistoryTask();

        var outcome = result.task(":clearHistory").getOutcome();
        assertThat(outcome).isEqualTo(TaskOutcome.SUCCESS);
        assertThat(Files.exists(historyDirectory)).isFalse();
    }

    private BuildResult runClearHistoryTask() {
        return GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
                .withArguments("clearHistory")
                .withPluginClasspath()
                .build();
    }
}
