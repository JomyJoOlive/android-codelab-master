package com.sap.codelab.view.create

import androidx.lifecycle.ViewModel
import com.sap.codelab.model.Memo
import com.sap.codelab.repository.Repository
import com.sap.codelab.utils.extensions.empty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * ViewModel for matching CreateMemo view. Handles user interactions.
 */
internal class CreateMemoViewModel : ViewModel() {

    private var memo = Memo(0, String.empty(), String.empty(), 0, 0.0, 0.0, false)

    /**
     * Saves the memo in it's current state.
     */

    suspend fun saveMemoAndReturn(): Memo {
        return withContext(Dispatchers.IO) {
            val id = Repository.saveMemoAndReturnId(memo)
            memo.copy(id = id)
        }
    }
    /**
     * Call this method to update the memo. This is usually needed when the user changed his input.
     */

    fun updateMemo(title: String, description: String, latitude: Double = 0.0, longitude: Double = 0.0) {
        memo = Memo(
            title = title,
            description = description,
            id = 0,
            reminderDate = 0,
            reminderLatitude = latitude,
            reminderLongitude = longitude,
            isDone = false
        )
    }

    /**
     * @return true if the title and content are not blank; false otherwise.
     */
    fun isMemoValid(): Boolean = memo.title.isNotBlank() && memo.description.isNotBlank() && !hasLocationError()

    /**
     * @return true if the memo text is blank, false otherwise.
     */
    fun hasTextError() = memo.description.isBlank()

    /**
     * @return true if the memo title is blank, false otherwise.
     */
    fun hasTitleError() = memo.title.isBlank()

    /**
     * @return true if the memo location is not set (latitude and longitude are 0.0); false otherwise.
     */
    fun hasLocationError(): Boolean = memo.reminderLatitude == 0.0 && memo.reminderLongitude == 0.0
}