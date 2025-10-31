package com.example.neptune.ttsapp.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.neptune.ttsapp.DTO.MentorTaskItem
import com.example.neptune.ttsapp.Network.TaskHandlerInterface
import com.example.neptune.ttsapp.paging.MentorTasksPagingSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This is a NEW and INDEPENDENT repository for the mentor task list feature.
 * It does not interfere with any existing repositories.
 */
@Singleton
class MentorRepository @Inject constructor(
    private val taskApi: TaskHandlerInterface
) {

    /**
     * This method creates the final data stream for the UI.
     * It sets up a Pager which uses our MentorTasksPagingSource to fetch data.
     */
    fun getMentorTasksStream(username: String): Flow<PagingData<MentorTaskItem>> {
        return Pager(
            // PagingConfig has been updated to explicitly control the initial load size.
            config = PagingConfig(
                pageSize = 10, 
                enablePlaceholders = false,
                // By default, initialLoadSize is 3 * pageSize. We are overriding it here.
                initialLoadSize = 10,
                prefetchDistance = 3
            ),
            // This provides a new instance of our PagingSource, which knows how to fetch the data.
            pagingSourceFactory = { MentorTasksPagingSource(taskApi, username) }
        ).flow
    }
}
