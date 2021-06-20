package com.malinskiy.marathon

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.api.TestVariant
import com.malinskiy.marathon.config.AppType
import com.malinskiy.marathon.exceptions.ExceptionsReporter
import com.malinskiy.marathon.exceptions.ExceptionsReporterFactory
import com.malinskiy.marathon.extensions.executeGradleCompat
import com.malinskiy.marathon.log.MarathonLogging
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.kotlin.dsl.closureOf
import java.io.File

private val log = MarathonLogging.logger {}

class MarathonPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        log.info { "Applying marathon plugin" }

        val extension: MarathonExtension = project.extensions.create("marathon", MarathonExtension::class.java)

        project.afterEvaluate {
            val exceptionsReporter = ExceptionsReporterFactory.get(extension.bugsnag != false)
            exceptionsReporter.start(AppType.GRADLE_PLUGIN)

            val appPlugin = project.plugins.findPlugin(AppPlugin::class.java)
            val libraryPlugin = project.plugins.findPlugin(LibraryPlugin::class.java)

            if (appPlugin == null && libraryPlugin == null) {
                throw IllegalStateException("Android plugin is not found")
            }

            val marathonTask: Task = project.task(TASK_PREFIX, closureOf<Task> {
                group = JavaBasePlugin.VERIFICATION_GROUP
                description = "Runs all the instrumentation test variations on all the connected devices"
            })

            val appExtension = extensions.findByType(AppExtension::class.java)
            val libraryExtension = extensions.findByType(LibraryExtension::class.java)

            if (appExtension == null && libraryExtension == null) {
                throw IllegalStateException("No TestedExtension is found")
            }
            val testedExtension = appExtension ?: libraryExtension

            val conf = extensions.getByName("marathon") as? MarathonExtension ?: MarathonExtension()

            testedExtension!!.testVariants.all {
                log.info { "Applying marathon for ${this.baseName}" }
                val testTaskForVariant = createTask(this, project, conf, testedExtension.sdkDirectory, exceptionsReporter)
                marathonTask.dependsOn(testTaskForVariant)
            }
        }
    }

    companion object {
        private fun createTask(
            variant: TestVariant,
            project: Project,
            config: MarathonExtension,
            sdkDirectory: File,
            exceptionsReporter: ExceptionsReporter
        ): MarathonRunTask {
            checkTestVariants(variant)

            val marathonTask = project.tasks.create("$TASK_PREFIX${variant.name.capitalize()}", MarathonRunTask::class.java)

            variant.testedVariant.outputs.all {
                val testedOutput = this
                log.info { "Processing output $testedOutput" }

                checkTestedVariants(testedOutput)
                marathonTask.configure(closureOf<MarathonRunTask> {
                    group = JavaBasePlugin.VERIFICATION_GROUP
                    description = "Runs instrumentation tests on all the connected devices for '${variant.name}' " +
                        "variation and generates a report with screenshots"
                    flavorName.set(variant.name)
                    applicationVariant.set(variant.testedVariant)
                    testVariant.set(variant)
                    marathonExtension.set(config)
                    sdk.set(sdkDirectory)
                    outputs.upToDateWhen { false }
                    exceptionsTracker.set(exceptionsReporter)
                    executeGradleCompat(
                        exec = {
                            dependsOn(variant.testedVariant.assembleProvider, variant.assembleProvider)
                        },
                        fallbacks = listOf {
                            @Suppress("DEPRECATION")
                            dependsOn(variant.testedVariant.assemble, variant.assemble)
                        }
                    )
                })
            }

            return marathonTask
        }

        private fun checkTestVariants(testVariant: TestVariant) {
            if (testVariant.outputs.size > 1) {
                throw UnsupportedOperationException("The Marathon plugin does not support abi/density splits for test APKs")
            }

        }

        /**
         * Checks that if the base variant contains more than one outputs (and has therefore splits), it is the universal APK.
         * Otherwise, we can test the single output. This is a workaround until Fork supports test & app splits properly.
         *
         * @param baseVariant the tested variant
         */
        private fun checkTestedVariants(baseVariantOutput: BaseVariantOutput) {
            if (baseVariantOutput.outputs.size > 1) {
                throw UnsupportedOperationException(
                    "The Marathon plugin does not support abi splits for app APKs, " +
                        "but supports testing via a universal APK. "
                        + "Add the flag \"universalApk true\" in the android.splits.abi configuration."
                )
            }

        }

        /**
         * Task name prefix.
         */
        private const val TASK_PREFIX = "marathon"
    }
}
