package com.sap.codelab.view.create

import com.sap.codelab.repository.Repository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateMemoViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: CreateMemoViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkObject(Repository)
        viewModel = CreateMemoViewModel()
    }

    @After
    fun tearDown() {
        unmockkObject(Repository)
        Dispatchers.resetMain()
    }

    @Test
    fun testUpdateMemo_andValidation() {
        Assert.assertFalse(viewModel.isMemoValid())
        Assert.assertTrue(viewModel.hasTitleError())
        Assert.assertTrue(viewModel.hasTextError())
        Assert.assertTrue(viewModel.hasLocationError())

        viewModel.updateMemo("Title", "Description")
        Assert.assertFalse(viewModel.hasTitleError())
        Assert.assertFalse(viewModel.hasTextError())
        Assert.assertTrue(viewModel.hasLocationError())
        Assert.assertFalse(viewModel.isMemoValid())

        viewModel.updateMemo("Title", "Description", 10.0, 20.0)
        Assert.assertFalse(viewModel.hasLocationError())
        Assert.assertTrue(viewModel.isMemoValid())
    }

    @Test
    fun testSaveMemoAndReturn() = runTest {
        val mockId = 123L
        coEvery { Repository.saveMemoAndReturnId(any()) } returns mockId

        viewModel.updateMemo("Title", "Description", 10.0, 20.0)
        val savedMemo = viewModel.saveMemoAndReturn()

        Assert.assertEquals(mockId, savedMemo.id)
        Assert.assertEquals("Title", savedMemo.title)
        Assert.assertEquals("Description", savedMemo.description)
        Assert.assertEquals(10.0, savedMemo.reminderLatitude, 0.0)
        Assert.assertEquals(20.0, savedMemo.reminderLongitude, 0.0)

        coVerify { Repository.saveMemoAndReturnId(any()) }
    }
}