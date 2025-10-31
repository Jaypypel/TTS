package com.example.neptune.ttsapp.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.neptune.ttsapp.DTO.MentorTaskItem
import com.example.neptune.ttsapp.Network.TaskHandlerInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/**
 * This is the core data-fetching class for pagination. It knows how to fetch one page of data
 * from the new mentor tasks endpoint.
 */
class MentorTasksPagingSource(
    private val taskApi: TaskHandlerInterface,
    private val username: String
) : PagingSource<Int, MentorTaskItem>() {

    /**
     * This function is called by the Paging library to fetch a page of data.
     */
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MentorTaskItem> {
        // If params.key is null, it's the first load, so we start at page 0.
        val pageNumber = params.key ?: 0

        return try {
            // Execute the network request. The `execute()` method makes it a synchronous call
            // inside this coroutine.
            val response = withContext(Dispatchers.IO){
                taskApi.getTasksToLearnersByMentor(username, pageNumber, params.loadSize).execute()
            }
//                taskApi.getTasksToLearnersByMentor(username, pageNumber, params.loadSize).execute()
            val mentorTaskResponse = response.body()

            if (response.isSuccessful && mentorTaskResponse != null) {
                val tasks = mentorTaskResponse.content
                val isLastPage = mentorTaskResponse.isLastPage

                // If it's the last page, the next key will be null, stopping pagination.
                val nextKey = if (isLastPage) null else pageNumber + 1
                // The previous key is simply the page before, or null if we are on the first page.
                val prevKey = if (pageNumber == 0) null else pageNumber - 1

                // Return a successful page of data.
                LoadResult.Page(
                    data = tasks,
                    prevKey = prevKey,
                    nextKey = nextKey
                )
            } else {
                // Handle cases where the response was not successful (e.g., 404, 500).
                LoadResult.Error(HttpException(response))
            }
        } catch (e: IOException) {
            // Handle network errors (e.g., no internet connection).
            LoadResult.Error(e)
        } catch (e: HttpException) {
            // Handle other HTTP errors.
            LoadResult.Error(e)
        }
    }

    /**
     * This function helps the Paging library determine which page to load when the data is refreshed.
     * It's not critical for basic functionality but good to have.
     */
    override fun getRefreshKey(state: PagingState<Int, MentorTaskItem>): Int? {
        return state
            .anchorPosition
            ?.let { state
            .closestPageToPosition(it)
            ?.prevKey?.plus(1)
            ?: state
                .closestPageToPosition(it)
                ?.nextKey
                ?.minus(1)
        }
    }
}
