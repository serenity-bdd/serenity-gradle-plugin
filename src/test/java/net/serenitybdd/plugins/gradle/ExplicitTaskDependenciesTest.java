package net.serenitybdd.plugins.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class ExplicitTaskDependenciesTest {

    @TempDir(cleanup = CleanupMode.NEVER)
    Path testProjectDir;
    Path buildFile;

    @BeforeEach
    void setup() throws IOException {
        buildFile = testProjectDir.resolve("build.gradle");
        Files.writeString(buildFile, """
            plugins {
                id 'java'
                id 'net.serenity-bdd.serenity-gradle-plugin'
            }
            repositories {
                mavenCentral()
            }
            test {
                useJUnitPlatform {}
            }
            ext {
                serenityCoreVersion = '4.2.22'
                junitVersion = '5.10.+'
            }
                    
            dependencies {
                testImplementation "net.serenity-bdd:serenity-core:${serenityCoreVersion}",
                        "net.serenity-bdd:serenity-junit5:${serenityCoreVersion}",
                        "org.junit.jupiter:junit-jupiter-api:${junitVersion}",
                        "org.junit.jupiter:junit-jupiter-engine:${junitVersion}"
            }
        """);
        var javaRoot = testProjectDir.resolve("src/test/java");
        Files.createDirectories(javaRoot);
        Files.writeString(javaRoot.resolve("NoopTest.java"), """
            import org.junit.jupiter.api.Test;
            import net.serenitybdd.junit5.SerenityJUnit5Extension;
            import org.junit.jupiter.api.Test;
            import org.junit.jupiter.api.extension.ExtendWith;
            
            @ExtendWith(SerenityJUnit5Extension.class)
            class NoopTest {
                @Test
                void noop() {
                }
            }
        """);
    }

    @Test
    void explicitDependencyBetweenClearReportsAndCheckOutcomes() {
        var result = runTasks("test", "checkOutcomes", "clearReports", "-i");
        assertThat(result.getOutput()).contains("BUILD SUCCESSFUL");
    }

    private BuildResult runTasks(String... args) {
        return GradleRunner.create()
                .withGradleVersion("8.5")
                .withProjectDir(testProjectDir.toFile())
                .withArguments(args)
                .withPluginClasspath()
                .build();
    }
}
