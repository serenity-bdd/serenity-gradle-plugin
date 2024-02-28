package net.serenitybdd.plugins.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.assertj.core.api.Assertions.assertThat;

public class WhenRunningCheckOutcomesTask {

    // We have to use the same build file for all tests, because serenity caches the project-directory
    // in net.serenitybdd.model.di.ModelInfrastructure.configuration
    @TempDir
    static Path testProjectDir;
    static Path buildFile;

    @BeforeEach
    void setup() throws IOException {
        buildFile = testProjectDir.resolve("build.gradle");
        Files.writeString(buildFile, """
            plugins {
                id 'net.serenity-bdd.serenity-gradle-plugin'
            }
        """, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    @Test
    void without_configuration_logs_null_project_key() throws IOException {
        Files.createDirectories(testProjectDir.resolve("target/site/serenity"));

        var result = runCheckOutcomesTask();

        var outcome = result.task(":checkOutcomes").getOutcome();
        assertThat(outcome).isEqualTo(TaskOutcome.SUCCESS);

        var output = result.getOutput();
        assertThat(output).contains("Checking serenity results for ", testProjectDir.getFileName().toString(), " in directory ");
    }

    @Test
    void with_project_key_configuration_logs_it() throws IOException {
        Files.writeString(buildFile, """
            serenity {
                projectKey = 'something'
            }
        """, StandardOpenOption.APPEND);
        Files.createDirectories(testProjectDir.resolve("target/site/serenity"));

        var result = runCheckOutcomesTask();

        var outcome = result.task(":checkOutcomes").getOutcome();
        assertThat(outcome).isEqualTo(TaskOutcome.SUCCESS);

        var output = result.getOutput();
        assertThat(output).contains("Checking serenity results for something in directory ");
    }

    @Test
    void without_report_directory_skips_it() throws IOException {
        Files.deleteIfExists(testProjectDir.resolve("target/site/serenity"));
        Files.writeString(buildFile, """
            serenity {
                projectKey = 'something'
            }
        """, StandardOpenOption.APPEND);

        var result = runCheckOutcomesTask();

        var outcome = result.task(":checkOutcomes").getOutcome();
        assertThat(outcome).isEqualTo(TaskOutcome.SKIPPED);
    }

    private BuildResult runCheckOutcomesTask() {
        return GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
                .withArguments("checkOutcomes")
                .withPluginClasspath()
                .build();
    }

}
