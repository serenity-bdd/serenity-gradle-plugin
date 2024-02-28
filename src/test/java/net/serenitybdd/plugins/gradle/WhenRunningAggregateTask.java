package net.serenitybdd.plugins.gradle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

public class WhenRunningAggregateTask {

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
        assertThat(output).contains(" - Test Root: null");
    }

    @Test
    void with_defined_testRoot_logs_it() throws IOException {
        Files.writeString(buildFile, """
            serenity {
                testRoot = 'something'
            }
        """, StandardOpenOption.APPEND);
        var result = runAggregateTask();

        var output = result.getOutput();
        assertThat(output).contains(" - Test Root: something");
    }

    @Test
    void without_configuration_logs_null_requirements_basedir() {
        var result = runAggregateTask();

        var output = result.getOutput();
        assertThat(output).contains(" - Requirements base directory: null");
    }

    @Test
    void with_defined_requirements_basedir_logs_it() throws IOException {
        Files.writeString(buildFile, """
            serenity {
                requirementsBaseDir = 'something'
            }
        """, StandardOpenOption.APPEND);
        var result = runAggregateTask();

        var output = result.getOutput();
        assertThat(output).contains(" - Requirements base directory: something");
    }

    private BuildResult runAggregateTask() {
        return GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
                .withArguments("aggregate")
                .withPluginClasspath()
                .build();
    }
}
