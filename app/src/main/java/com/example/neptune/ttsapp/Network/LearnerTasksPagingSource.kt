package com.example.neptune.ttsapp.Network;

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.neptune.ttsapp.displayTaskToLearners.TaskSummary

class LearnerTasksPagingSource : PagingSource<Int, TaskSummary>(){



    override fun getRefreshKey(state: PagingState<Int, TaskSummary>): Int? {
        TODO("Not yet implemented")
    }



    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, TaskSummary> {
        TODO("Not yet implemented")
    }

}
