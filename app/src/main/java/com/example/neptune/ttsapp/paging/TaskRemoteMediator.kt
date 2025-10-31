//package com.example.neptune.ttsapp.paging
//
//import androidx.paging.ExperimentalPagingApi
//import androidx.paging.LoadType
//import androidx.paging.PagingState
//import androidx.paging.RemoteMediator
//import androidx.room.withTransaction
//import com.example.neptune.ttsapp.DTO.TaskSliceResponse
//import com.example.neptune.ttsapp.Network.TaskHandlerInterface
//import com.example.neptune.ttsapp.displayTaskToLearners.AppDatabase
//import com.example.neptune.ttsapp.displayTaskToLearners.TaskSummary
//import retrofit2.HttpException
//import java.io.IOException
//
//private const val STARTING_PAGE_INDEX = 0
//
//@OptIn(ExperimentalPagingApi::class)
//class TaskRemoteMediator(
//    private val taskApi: TaskHandlerInterface,
//    private val appDatabase: AppDatabase,
//    private val username: String,
//    private val status: String
//) : RemoteMediator<Int, TaskSummary>() {
//
//    override suspend fun load(loadType: LoadType, state: PagingState<Int, TaskSummary>): MediatorResult {
//        val page = when (loadType) {
//            LoadType.REFRESH -> STARTING_PAGE_INDEX
//            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
//            LoadType.APPEND -> {
//                val remoteKeys = getRemoteKeyForLastItem(state)
//                val nextKey = remoteKeys?.nextKey
//                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
//                nextKey
//            }
//        }
//
//        try {
//            val apiResponse: TaskSliceResponse = taskApi.getLearnerTasksWithPaging(
//                username = username,
//                status = status,
//                page = page,
//                size = state.config.pageSize
//            )
//
//            val tasks = apiResponse.content
//            val endOfPaginationReached = apiResponse.isLastPage
//
//            appDatabase.withTransaction {
//                if (loadType == LoadType.REFRESH) {
//                    appDatabase.remoteKeysDao().clearRemoteKeys()
//                    appDatabase.taskDao().clearAll()
//                }
//
//                val prevKey = if (page == STARTING_PAGE_INDEX) null else page - 1
//                val nextKey = if (endOfPaginationReached) null else page + 1
//                val keys = tasks.map {
//                    RemoteKeys(taskId = it.id, prevKey = prevKey, nextKey = nextKey)
//                }
//
//                appDatabase.remoteKeysDao().insertAll(keys)
//                appDatabase.taskDao().insertAll(tasks)
//            }
//            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
//        } catch (exception: IOException) {
//            return MediatorResult.Error(exception)
//        } catch (exception: HttpException) {
//            return MediatorResult.Error(exception)
//        }
//    }
//
//    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, TaskSummary>): RemoteKeys? {
//        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
//            ?.let { task ->
//                appDatabase.remoteKeysDao().remoteKeysByTaskId(task.id)
//            }
//    }
//}
