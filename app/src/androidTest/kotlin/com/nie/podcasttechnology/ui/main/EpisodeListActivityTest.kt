package com.nie.podcasttechnology.ui.main


import android.util.Log
import android.view.View
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.util.HumanReadables
import androidx.test.espresso.util.TreeIterables
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.nie.podcasttechnology.R
import com.nie.podcasttechnology.util.Constant.TAG
import org.hamcrest.core.StringContains.containsString
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeoutException
import java.util.logging.Logger



fun waitForView(viewId: Int, timeout: Long): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): org.hamcrest.Matcher<View>? {
            return isRoot()
        }

        override fun getDescription(): String {
            return "wait for a specific view with id $viewId; during $timeout millis."
        }

        override fun perform(uiController: UiController, rootView: View) {
            uiController.loopMainThreadUntilIdle()
            val startTime = System.currentTimeMillis()
            val endTime = startTime + timeout
            val viewMatcher = withId(viewId)

            do {
                // Iterate through all views on the screen and see if the view we are looking for is there already
                for (child in TreeIterables.breadthFirstViewTraversal(rootView)) {
                    // found view with required ID
                    if (viewMatcher.matches(child)) {
                        return
                    }
                }
                // Loops the main thread for a specified period of time.
                // Control may not return immediately, instead it'll return after the provided delay has passed and the queue is in an idle state again.
                uiController.loopMainThreadForAtLeast(100)
            } while (System.currentTimeMillis() < endTime) // in case of a timeout we throw an exception -> test fails
            throw PerformException.Builder()
                .withCause(TimeoutException())
                .withActionDescription(this.description)
                .withViewDescription(HumanReadables.describe(rootView))
                .build()
        }
    }
}

fun waitForApp(){
    Log.d(TAG, "[Step] waitForApp")
    onView(isRoot()).perform(waitForView(R.id.recyclerViewEpisodes, 5000))
    Thread.sleep(5000)
}

fun checkElementExist(text: String){
    Log.d(TAG, "[Step] checkElementExist: $text")
    onView(allOf(withText(text), isDisplayed())).check(matches(withText(text)))
}

fun checkContainsText(text: String){
    Log.d(TAG, "[Step] checkContainsText: $text")
    onView(withText(containsString("text")))
}

fun clickEpisode(positionIndex: Int){
    Log.d(TAG, "[Step] clickEpisode: position index -> $positionIndex")
    onView(withId(R.id.recyclerViewEpisodes))
        .perform(
            RecyclerViewActions.actionOnItemAtPosition<EpisodeAdapter.PodcastViewHolder>(positionIndex,
                ViewActions.click()
            ))
    Thread.sleep(5000)
}
fun back(){
    Log.d(TAG, "[Step] back")
    Espresso.pressBack()
}

fun scrollToPosition(positionIndex: Int){
    Log.d(TAG, "[Step] scrollToPosition: position index -> $positionIndex")
    onView(withId(R.id.recyclerViewEpisodes))
        .perform(
            RecyclerViewActions.actionOnItemAtPosition<EpisodeAdapter.PodcastViewHolder>(positionIndex,
                ViewActions.scrollTo()
            ))
}

@LargeTest
@RunWith(AndroidJUnit4::class)
class EpisodeListActivityTest {

    @Rule
    @JvmField
    var mActivityScenarioRule = ActivityScenarioRule(EpisodeListActivity::class.java)
    val mScreenshotTestRule = ScreenshotTestRule()

    @Test
    fun checkFirstEpisodeDisplay() {
        waitForApp()
        checkElementExist("SP. 科技島讀請回答")
        checkElementExist("2021/06/06")
    }

    @Test
    fun checkClickEpisodeSectionRedirect() {
        waitForApp()
        clickEpisode(0)
        checkElementExist("科技島讀")
        checkElementExist("SP. 科技島讀請回答")
        checkContainsText("科技島讀podcast歷時4年")
        back()
        checkElementExist("SP. 科技島讀請回答")
    }

    @Test
    fun checkScrollToSpecificEpisodeSection() {
        waitForApp()
        scrollToPosition(11)
        checkElementExist("Ep. 135 流動的資金盛宴")
        checkElementExist("2021/03/07")
    }
}
