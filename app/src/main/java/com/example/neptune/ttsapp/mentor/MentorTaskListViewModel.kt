package com.example.neptune.ttsapp.mentor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.neptune.ttsapp.DTO.MentorTaskItem
import com.example.neptune.ttsapp.repository.MentorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * This is the NEW ViewModel for your mentor task list.
 * It connects to the NEW MentorRepository to get the paginated data stream.
 */
@HiltViewModel
class MentorTaskListViewModel @Inject constructor(
    private val mentorRepository: MentorRepository
) : ViewModel() {
    private val _mentorTasksCache = mutableMapOf<String, Flow<PagingData<MentorTaskItem>>>()
    /**
     * This function gets the stream of paginated data for the UI.
     * It returns a Flow of PagingData containing the correct MentorTaskItem type.
     */
    fun getMentorTasks(username: String): Flow<PagingData<MentorTaskItem>> {
        // .cachedIn(viewModelScope) is very important. It keeps the loaded data in memory
        // across screen rotations, providing a smooth user experience.
        return _mentorTasksCache.getOrPut(username) {
            mentorRepository.getMentorTasksStream(username).cachedIn(viewModelScope)
        }
    }
}
