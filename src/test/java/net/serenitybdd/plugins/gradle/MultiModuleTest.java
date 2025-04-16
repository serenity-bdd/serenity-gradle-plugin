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
                                                 id "net.serenity-bdd.serenity-gradle-plugin"
                                                 id 'java'
                                             }
                    
                                             defaultTasks 'clean', 'test', 'aggregate'
                    
                                             repositories {
                                                 mavenCentral()
                                             }
                    
                                             sourceCompatibility = 17
                                             targetCompatibility = 17
                    
                                             ext {
                                                 slf4jVersion = '1.7.7'
                                                 serenityCoreVersion = '4.2.22'
                                                 junitVersion = '5.11.4'
                                                 assertJVersion = '3.23.1'
                                                 lombokVersion = '1.18.24'
                                                 logbackVersion = '1.2.11'
                                             }
                    
                                             dependencies {
                                                 testImplementation "net.serenity-bdd:serenity-core:${serenityCoreVersion}",
                                                         "net.serenity-bdd:serenity-junit5:${serenityCoreVersion}",
                                                         "net.serenity-bdd:serenity-screenplay:${serenityCoreVersion}",
                                                         "net.serenity-bdd:serenity-screenplay-webdriver:${serenityCoreVersion}",
                                                         "net.serenity-bdd:serenity-ensure:${serenityCoreVersion}",
                                                         "net.serenity-bdd:serenity-junit:${serenityCoreVersion}",
                                                         "org.junit.jupiter:junit-jupiter-api:${junitVersion}",
                                                         "org.junit.jupiter:junit-jupiter-engine:${junitVersion}",
                                                         "org.assertj:assertj-core:${assertJVersion}",
                                                         "org.projectlombok:lombok:${lombokVersion}",
                                                         "ch.qos.logback:logback-classic:${logbackVersion}"
                                             }
                    
                                             test {
                                                 useJUnitPlatform()
                                                 testLogging.showStandardStreams = true
                                                 systemProperties System.getProperties()
                                             }
                    
                                             gradle.startParameter.continueOnFailure = true
                    
                                             serenity {
                                                 reports = ["single-page-html"]
                    
                                                 // // Specify the root package of any JUnit acceptance tests
                                                 testRoot="starter"
                                             }
                    
                                             test.finalizedBy(aggregate)
                    
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
        for (var path : subprojectPaths) {
            Path report = path.resolve("target/site/serenity/index.html");
            assertThat(report).exists();
        }
        assertThat(result.getOutput()).contains("BUILD SUCCESSFUL");
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
