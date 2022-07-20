package com.nie.podcasttechnology

import com.nie.podcasttechnology.data.remote.model.*
import com.nie.podcasttechnology.domain.EpisodeListUseCase
import com.nie.podcasttechnology.repository.DatabaseRepository
import com.nie.podcasttechnology.repository.EpisodeListRepository
import io.mockk.*
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

@FlowPreview
class EpisodeListViewModelTest {
    private lateinit var episodeListUseCase: EpisodeListUseCase

    private val episodeListRepository = mockk<EpisodeListRepository>(relaxed = true)
    private val databaseRepository = mockk<DatabaseRepository>(relaxed = true)

    private val image = Image(
        "imageUrl",
        "title",
        "link"
    )

    private val enclosure = Enclosure(
        "type",
        "audioUrl"
    )

    private val podcastItem = EpisodeItem(
        "title",
        "Sat, 24 Apr 2010 14:01:00 GMT",
        "description",
        "duration",
        enclosure,
        image
    )

    private val channel = Channel(
        "title",
        "Sat, 24 Apr 2010 14:01:00 GMT",
        "description",
        listOf(image),
        listOf(podcastItem)
    )

    private val rss = Rss(channel)

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        episodeListUseCase = EpisodeListUseCase(episodeListRepository, databaseRepository)
    }

    @Test
    fun fetchEpisodes() = runBlocking {
        every { databaseRepository.clearAllDatabaseTables() } returns flowOf(true)
        every { episodeListRepository.fetchEpisodes() } returns flowOf(rss)
        every { databaseRepository.insertEpisodes(rss.channel.items) } returns flowOf(true)

        episodeListUseCase.fetchEpisodes()
            .collect()

        verifyOrder {
            databaseRepository.clearAllDatabaseTables()
            episodeListRepository.fetchEpisodes()
            databaseRepository.insertEpisodes(rss.channel.items)
        }
    }

    @Test
    fun fetchEpisodesFailed() = runBlocking {
        every { databaseRepository.clearAllDatabaseTables() } returns flowOf(true)
        every { episodeListRepository.fetchEpisodes() } returns flow { throw Exception() }
        every { databaseRepository.insertEpisodes(rss.channel.items) } returns flowOf(true)

        var isError = false

        episodeListUseCase.fetchEpisodes()
            .catch { e ->
                isError = true
            }
            .collect()

        assert(isError)
    }

    //測試fetchEpisodes執行function的流程
    @Test
    fun fetchEpisodesFlow() = runBlocking {
        every { databaseRepository.clearAllDatabaseTables() } returns flowOf(true)
        every { episodeListRepository.fetchEpisodes() } returns flowOf(rss)
        every { databaseRepository.insertEpisodes(rss.channel.items) } returns flowOf(true)

        episodeListUseCase.fetchEpisodes()
            .collect()

        verifySequence {
            databaseRepository.clearAllDatabaseTables()
            episodeListRepository.fetchEpisodes()
            databaseRepository.insertEpisodes(rss.channel.items)
        }
    }

    @Test
    fun fetchEpisodeContent() = runBlocking {
        every {databaseRepository.clearAllDatabaseTables()} returns flowOf(true)
        every {databaseRepository.insertEpisodes(rss.channel.items)} returns flowOf(true)

        episodeListUseCase.fetchEpisodes().collect {

        }
    }
}