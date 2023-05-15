package net.serenitybdd.plugins.gradle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.gradle.testkit.runner.TaskOutcome.SKIPPED;
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE;

class WhenApplyingSerenityPluginWithConfigurationCache {
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

        var runner = GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
                .withPluginClasspath();

        // run tasks twice to use configuration cache
        // see https://docs.gradle.org/current/userguide/configuration_cache.html#config_cache:testing
        var firstResult = runner.withArguments("--configuration-cache", taskName)
                .build();
        assertThat(firstResult.task(":" + taskName).getOutcome()).isIn(SKIPPED, SUCCESS);

        var secondResult = runner.withArguments("--configuration-cache", taskName)
                .build();
        assertThat(secondResult.task(":" + taskName).getOutcome()).isIn(SKIPPED, SUCCESS, UP_TO_DATE);
    }

}

