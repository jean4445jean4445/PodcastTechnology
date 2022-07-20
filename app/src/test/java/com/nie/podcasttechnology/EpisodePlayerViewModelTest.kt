package com.nie.podcasttechnology

import com.nie.podcasttechnology.data.ui.ViewEpisode
import com.nie.podcasttechnology.domain.EpisodePlayerUseCase
import com.nie.podcasttechnology.repository.DatabaseRepository
import io.mockk.MockKAnnotations
import io.mockk.*
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.*

@FlowPreview
class EpisodePlayerViewModelTest {
    private lateinit var episodePlayerUseCase: EpisodePlayerUseCase

    private val databaseRepository = mockk<DatabaseRepository>(relaxed = true)

    private val viewEpisodeList = listOf<ViewEpisode>(ViewEpisode(Date(2022, 6,6), "title test", "description test", "type test", "audio url test", "image url test"))

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        episodePlayerUseCase = EpisodePlayerUseCase(databaseRepository)
    }

    //測試getNextEpisode取得的內容
    @Test
    fun getNextEpisodeContent() = runBlocking {
        every {databaseRepository.getNextEpisode(Date(2022,4,24))} returns flowOf(viewEpisodeList)

        var date = ""
        var title = ""
        var description = ""
        var type = ""
        var audioUrl = ""
        var imageUrl = ""

        episodePlayerUseCase.getNextEpisode(Date(2022, 4, 24)).collect {
            date = it.first().pubDate.toString()
            title = it.first().title
            description = it.first().description
            type = it.first().type
            audioUrl = it.first().audioUrl
            imageUrl = it.first().imageUrl
        }

        Assert.assertEquals("Thu Jul 06 00:00:00 CST 3922", date)
        Assert.assertEquals("title test", title)
        Assert.assertEquals("description test", description)
        Assert.assertEquals("type test", type)
        Assert.assertEquals("audio url test", audioUrl)
        Assert.assertEquals("image url test", imageUrl)
    }

    //測試getLatestEpisode取得的內容
    @Test
    fun getLatestEpisodeContent() = runBlocking {
        every {databaseRepository.getLatestEpisode()} returns flowOf(viewEpisodeList)

        var title = ""
        var date = ""
        var description = ""
        var type = ""
        var audioUrl = ""
        var imageUrl = ""

        episodePlayerUseCase.getLatestEpisode().collect {
            title = it.first().title
            date = it.first().pubDate.toString()
            description = it.first().description
            type = it.first().type
            audioUrl = it.first().audioUrl
            imageUrl = it.first().imageUrl
        }

        Assert.assertEquals("title test", title)
        Assert.assertEquals("Thu Jul 06 00:00:00 CST 3922", date)
        Assert.assertEquals("description test", description)
        Assert.assertEquals("type test", type)
        Assert.assertEquals("audio url test", audioUrl)
        Assert.assertEquals("image url test", imageUrl)
    }
}