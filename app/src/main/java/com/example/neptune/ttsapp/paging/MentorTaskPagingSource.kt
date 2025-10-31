//package com.example.neptune.ttsapp.paging
//
//import androidx.paging.PagingSource
//import androidx.paging.PagingState
//import com.example.neptune.ttsapp.DTO.MentorTask
//import com.example.neptune.ttsapp.Network.TaskHandlerInterface
//import retrofit2.HttpException
//import java.io.IOException
//
//private const val STARTING_PAGE_INDEX = 0
//
//class MentorTaskPagingSource(
//    private val taskApi: TaskHandlerInterface,
//    private val username: String
//) : PagingSource<Int, MentorTask>() {
//
//    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MentorTask> {
//        val page = params.key ?: STARTING_PAGE_INDEX
//        return try {
//            val response = taskApi.getTaskAssignedToLearnerByMentor(username, page, params.loadSize).execute()
//            val tasks = response.body()?.content ?: emptyList()
//
//            LoadResult.Page(
//                data = tasks,
//                prevKey = if (page == STARTING_PAGE_INDEX) null else page - 1,
//                nextKey = if (tasks.isEmpty() || response.body()?.isLastPage == true) null else page + 1
//            )
//        } catch (exception: IOException) {
//            return LoadResult.Error(exception)
//        } catch (exception: HttpException) {
//            return LoadResult.Error(exception)
//        }
//    }
//
//    override fun getRefreshKey(state: PagingState<Int, MentorTask>): Int? {
//        return state.anchorPosition?.let {
//            state.closestPageToPosition(it)?.prevKey?.plus(1)
//                ?: state.closestPageToPosition(it)?.nextKey?.minus(1)
//        }
//    }
//}