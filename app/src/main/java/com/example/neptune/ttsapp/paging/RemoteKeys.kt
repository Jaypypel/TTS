package com.example.neptune.ttsapp.paging

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_keys")
data class RemoteKeys(
    @PrimaryKey val taskId: String,
    val prevKey: Int?,
    val nextKey: Int?
)
