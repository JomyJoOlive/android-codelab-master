package com.sap.codelab.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.sap.codelab.model.Memo
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.robolectric.RobolectricTestRunner
import org.junit.runner.RunWith

@RunWith(RobolectricTestRunner::class)
class RepositoryTest {

    private lateinit var database: Database
    private lateinit var repo: IMemoRepository
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, Database::class.java)
            .allowMainThreadQueries()  // For testing only
            .build()
        repo = Repository

        // Initialize Repository with the in-memory database
        Repository.initialize(context)
        // Replace internal database with in-memory instance for testing
        val dbField = Repository::class.java.getDeclaredField("database")
        dbField.isAccessible = true
        dbField.set(null, database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testSaveAndGetMemo() = runBlocking {
        val memo = Memo(
            id = 0L,
            title = "Test Title",
            description = "Test Description",
            reminderLatitude = 10.0,
            reminderLongitude = 20.0,
            reminderDate = 0L
        )
        repo.saveMemo(memo)
        val allMemos = repo.getAll()
        assertEquals(1, allMemos.size)
        assertEquals("Test Title", allMemos[0].title)
    }

    @Test
    fun testSaveMemoAndReturnId() = runBlocking {
        val memo = Memo(
            id = 0L,
            title = "Title 2",
            description = "Description 2",
            reminderLatitude = 30.0,
            reminderLongitude = 40.0,
            reminderDate = 0L
        )
        val id = repo.saveMemoAndReturnId(memo)
        val memoFromDb = repo.getMemoById(id)
        assertEquals(id, memoFromDb.id)
        assertEquals("Title 2", memoFromDb.title)
    }

    @Test
    fun testGetOpen() = runBlocking {
        // Prepare memos, some done and some open
        val openMemo1 = Memo(id = 0, title = "Open1", description = "desc", reminderDate = 0L, reminderLatitude = 0.0, reminderLongitude = 0.0, isDone = false)
        val openMemo2 = Memo(id = 0, title = "Open2", description = "desc", reminderDate = 0L, reminderLatitude = 0.0, reminderLongitude = 0.0, isDone = false)
        val doneMemo = Memo(id = 0, title = "Done", description = "desc", reminderDate = 0L, reminderLatitude = 0.0, reminderLongitude = 0.0, isDone = true)

        // Insert all memos
        repo.saveMemo(openMemo1)
        repo.saveMemo(openMemo2)
        repo.saveMemo(doneMemo)

        // Retrieve open memos
        val openMemos = repo.getOpen()

        // Assert only open memos
        assertTrue(openMemos.all { !it.isDone })
        assertTrue(openMemos.any { it.title == "Open1" })
        assertTrue(openMemos.any { it.title == "Open2" })
        assertFalse(openMemos.any { it.title == "Done" })
    }

}
