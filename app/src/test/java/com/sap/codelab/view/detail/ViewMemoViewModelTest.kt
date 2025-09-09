package com.sap.codelab.view.detail

import com.sap.codelab.model.Memo
import com.sap.codelab.repository.Repository
import io.mockk.coEvery
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ViewMemoViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: ViewMemoViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkObject(Repository)
        viewModel = ViewMemoViewModel()
    }

    @After
    fun tearDown() {
        unmockkObject(Repository)
        Dispatchers.resetMain()
    }

    @Test
    fun `loadMemo updates memo state flow with correct memo`() = runTest {
        val memoId = 123L
        val memo = Memo(
            id = memoId,
            title = "Test Title",
            description = "Test Description",
            reminderLatitude = 10.0,
            reminderLongitude = 20.0,
            reminderDate = 0L
        )

        coEvery { Repository.getMemoById(memoId) } returns memo

        viewModel.loadMemo(memoId)
        testDispatcher.scheduler.advanceUntilIdle()

        val emittedMemo = viewModel.memo.first()
        assertEquals(memo, emittedMemo)
    }
}
