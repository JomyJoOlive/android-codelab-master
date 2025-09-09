package com.sap.codelab.view.home

import com.sap.codelab.model.Memo
import com.sap.codelab.repository.Repository
import com.sap.codelab.utils.coroutines.ScopeProvider
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkObject(Repository)
        mockkObject(ScopeProvider)
        viewModel = HomeViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `updateMemo calls Repository saveMemo only if isChecked is true`() = runTest {
        val memo = Memo(1, "Title", "Desc", 0, 0.0, 0.0)
        coEvery { Repository.saveMemo(any()) } just runs

        viewModel.updateMemo(memo, false)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify(exactly = 0) { Repository.saveMemo(any()) }

        viewModel.updateMemo(memo, true)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            Repository.saveMemo(memo.copy(isDone = true))
        }
    }
}
