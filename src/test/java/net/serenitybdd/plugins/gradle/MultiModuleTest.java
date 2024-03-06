package net.serenitybdd.plugins.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class MultiModuleTest {

    @TempDir(cleanup = CleanupMode.NEVER)
    Path testProjectDir;

    List<String> subprojectNames;
    List<Path> subprojectPaths;

    @BeforeEach
    void setup() throws IOException {
        var ids = IntStream.rangeClosed(1, 2).boxed().toList();
        subprojectNames = ids.stream().map(i -> "subproject-" + i).toList();
        subprojectPaths = subprojectNames.stream().map(testProjectDir::resolve).toList();
        var settingsFile = testProjectDir.resolve("settings.gradle");
        var settingsContent = subprojectNames.stream().map(s -> "include '" + s + "'").reduce((a, b) -> a + "\n" + b).orElse("");
        Files.writeString(settingsFile, settingsContent);
        for (var s : subprojectNames) {
            Files.createDirectories(testProjectDir.resolve(s).resolve("src/test/java"));
            Files.writeString(testProjectDir.resolve(s).resolve("build.gradle"), """
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
                            serenityCoreVersion = '4.0.46'
                            junitVersion = '5.10.+'
                        }
                                
                        dependencies {
                            testImplementation "net.serenity-bdd:serenity-core:${serenityCoreVersion}",
                                    "net.serenity-bdd:serenity-junit5:${serenityCoreVersion}",
                                    "org.junit.jupiter:junit-jupiter-api:${junitVersion}",
                                    "org.junit.jupiter:junit-jupiter-engine:${junitVersion}"
                        }
                    """);
            Files.writeString(testProjectDir.resolve(s).resolve("src/test/java/NoopTest.java"), """
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
    }

    @Test
    void multiModulesAggregateTheReportInTheRightLocation() throws IOException {
        var result = runTasks("test", "-i");
        System.out.println(result.getOutput());
        for (var path : subprojectPaths) {
            Path report = path.resolve("target/site/serenity/index.html");
            assertThat(report).exists();
        }
        assertThat(result.getOutput().contains("BUILD SUCCESSFUL"));
    }

    private BuildResult runTasks(String... args) {
        return GradleRunner.create()
                .withGradleVersion("8.5")
                .withProjectDir(testProjectDir.toFile())
                .withArguments(args)
                .withPluginClasspath()
                .forwardOutput()
                .build();
    }
}
