package com.example.neptune.ttsapp.displayTaskToLearners

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

//@Dao
//interface TaskDao {
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    fun insertAll(tasks: List<TaskSummary>)
//
//    @Query("SELECT * FROM task_summary")
//    fun pagingSource(): PagingSource<Int, TaskSummary>
//
//    @Query("DELETE FROM task_summary")
//    fun clearAll()
//}
