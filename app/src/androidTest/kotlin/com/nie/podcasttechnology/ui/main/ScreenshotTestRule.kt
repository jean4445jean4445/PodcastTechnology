package com.nie.podcasttechnology.ui.main

import androidx.test.espresso.Espresso
import androidx.test.espresso.base.DefaultFailureHandler
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import java.util.concurrent.atomic.AtomicBoolean
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * TestRule used to run all test methods try count 1 time. Take screenshots on failure.
 */
open class ScreenshotTestRule : TestRule {
    // Note: Data seeding must happen before we run a test. As a result, retrying failed tests
    //       at the JUnit level doesn"t make sense because we can"t run data seeeding.
    //
    // Run all test methods tryCount times. Take screenshots on failure.
    // A method rule would allow targeting specific (method.getAnnotation(Retry.class))
    private val tryCount = 1

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                var error: Throwable? = null

                val errorHandled = AtomicBoolean(false)

                // Espresso failure handler will capture accurate UI screenshots.
                // if we wait for `try { base.evaluate() } catch ()` then the UI will be in a different state
                //
                // Only espresso failures trigger the espresso failure handlers. For JUnit assert errors,
                // those must be captured in `try { base.evaluate() } catch ()`
                Espresso.setFailureHandler { throwable, matcher ->
                    EspressoScreenshot.takeScreenshot(description)
                    errorHandled.set(true)
                    val targetContext = getInstrumentation().targetContext
                    DefaultFailureHandler(targetContext).handle(throwable, matcher)
                }

                for (i in 0 until tryCount) {
                    errorHandled.set(false)
                    try {
                        base.evaluate()
                        return
                    } catch (t: Throwable) {
                        if (!errorHandled.get()) {
                            EspressoScreenshot.takeScreenshot(description)
                        }
                        error = t
                    }
                }

                if (error != null) throw error
            }
        }
    }
}