package com.example

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.test.core.app.ApplicationProvider
import com.example.data.CleanerDatabase
import com.example.data.ReviewDao
import com.example.data.ReviewItemEntity
import com.example.model.MediaItem
import com.example.model.MediaSource
import com.example.model.ReviewDecision
import com.example.model.SmartCategory
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ReviewHistoryPersistenceTest {

    private lateinit var database: CleanerDatabase
    private lateinit var reviewDao: ReviewDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, CleanerDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        reviewDao = database.reviewDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testPersistentKeepDeleteAndSkipDecisions() = runBlocking {
        // 1. Insert KEEP decision
        val keepEntity = ReviewItemEntity(
            mediaId = 101L,
            uri = "content://media/external/images/media/101",
            displayName = "photo_101.jpg",
            sizeBytes = 2048000L,
            mimeType = "image/jpeg",
            isVideo = false,
            decision = ReviewDecision.KEEP.name,
            category = SmartCategory.CAMERA.id
        )
        reviewDao.insertOrUpdate(keepEntity)

        // 2. Insert DELETE decision
        val deleteEntity = ReviewItemEntity(
            mediaId = 102L,
            uri = "content://media/external/images/media/102",
            displayName = "screenshot_102.png",
            sizeBytes = 1024000L,
            mimeType = "image/png",
            isVideo = false,
            decision = ReviewDecision.DELETE.name,
            category = SmartCategory.SCREENSHOTS.id
        )
        reviewDao.insertOrUpdate(deleteEntity)

        // 3. Insert SKIP decision
        val skipEntity = ReviewItemEntity(
            mediaId = 103L,
            uri = "content://media/external/images/media/103",
            displayName = "doc_103.jpg",
            sizeBytes = 512000L,
            mimeType = "image/jpeg",
            isVideo = false,
            decision = ReviewDecision.SKIP.name,
            category = SmartCategory.OTHER_IMAGES.id
        )
        reviewDao.insertOrUpdate(skipEntity)

        // Verify all 3 decisions are persistently stored
        val reviewedUris = reviewDao.getAllReviewedUris().toSet()
        assertEquals(3, reviewedUris.size)
        assertTrue(reviewedUris.contains(keepEntity.uri))
        assertTrue(reviewedUris.contains(deleteEntity.uri))
        assertTrue(reviewedUris.contains(skipEntity.uri))

        // Verify decision types
        assertEquals(ReviewDecision.KEEP.name, reviewDao.getDecisionForUri(keepEntity.uri)?.decision)
        assertEquals(ReviewDecision.DELETE.name, reviewDao.getDecisionForUri(deleteEntity.uri)?.decision)
        assertEquals(ReviewDecision.SKIP.name, reviewDao.getDecisionForUri(skipEntity.uri)?.decision)
    }

    @Test
    fun testReviewedMediaStaysOutOfQueueAfterReload() = runBlocking {
        val candidateItems = listOf(
            MediaItem(id = 1L, uri = "uri://1", displayName = "1.jpg", sizeBytes = 100L, mimeType = "image/jpeg", dateAdded = 1L, isVideo = false),
            MediaItem(id = 2L, uri = "uri://2", displayName = "2.jpg", sizeBytes = 100L, mimeType = "image/jpeg", dateAdded = 2L, isVideo = false),
            MediaItem(id = 3L, uri = "uri://3", displayName = "3.jpg", sizeBytes = 100L, mimeType = "image/jpeg", dateAdded = 3L, isVideo = false)
        )

        // Review item 1 as KEEP and item 2 as DELETE
        reviewDao.insertOrUpdate(ReviewItemEntity(mediaId = 1L, uri = "uri://1", displayName = "1.jpg", sizeBytes = 100L, mimeType = "image/jpeg", isVideo = false, decision = ReviewDecision.KEEP.name, category = "camera"))
        reviewDao.insertOrUpdate(ReviewItemEntity(mediaId = 2L, uri = "uri://2", displayName = "2.jpg", sizeBytes = 100L, mimeType = "image/jpeg", isVideo = false, decision = ReviewDecision.DELETE.name, category = "camera"))

        // Simulate app restart / reload by querying unreviewed candidates against Room
        val reviewedUris = reviewDao.getAllReviewedUris().toHashSet()
        val unreviewed = candidateItems.filter { it.uri !in reviewedUris }

        // Only unreviewed item 3 should appear in the queue
        assertEquals(1, unreviewed.size)
        assertEquals(3L, unreviewed[0].id)
        assertFalse(unreviewed.any { it.id == 1L })
        assertFalse(unreviewed.any { it.id == 2L })
    }

    @Test
    fun testNewMediaAppearsInQueue() = runBlocking {
        val initialItems = listOf(
            MediaItem(id = 1L, uri = "uri://1", displayName = "1.jpg", sizeBytes = 100L, mimeType = "image/jpeg", dateAdded = 1L, isVideo = false)
        )
        // Item 1 is reviewed
        reviewDao.insertOrUpdate(ReviewItemEntity(mediaId = 1L, uri = "uri://1", displayName = "1.jpg", sizeBytes = 100L, mimeType = "image/jpeg", isVideo = false, decision = ReviewDecision.KEEP.name, category = "camera"))

        // Now a new photo is taken by user and discovered by MediaStore
        val newlyDiscoveredItems = initialItems + MediaItem(id = 99L, uri = "uri://99", displayName = "new_photo.jpg", sizeBytes = 300L, mimeType = "image/jpeg", dateAdded = 99L, isVideo = false)

        val reviewedUris = reviewDao.getAllReviewedUris().toHashSet()
        val unreviewed = newlyDiscoveredItems.filter { it.uri !in reviewedUris }

        assertEquals(1, unreviewed.size)
        assertEquals(99L, unreviewed[0].id)
        assertEquals("new_photo.jpg", unreviewed[0].displayName)
    }

    @Test
    fun testDuplicateMediaPrevention() = runBlocking {
        val entity1 = ReviewItemEntity(
            mediaId = 10L,
            uri = "uri://duplicate_check",
            displayName = "same.jpg",
            sizeBytes = 500L,
            mimeType = "image/jpeg",
            isVideo = false,
            decision = ReviewDecision.SKIP.name,
            category = "camera"
        )
        reviewDao.insertOrUpdate(entity1)

        // Same URI inserted with a different decision
        val entity2 = ReviewItemEntity(
            mediaId = 10L,
            uri = "uri://duplicate_check",
            displayName = "same.jpg",
            sizeBytes = 500L,
            mimeType = "image/jpeg",
            isVideo = false,
            decision = ReviewDecision.KEEP.name,
            category = "camera"
        )
        reviewDao.insertOrUpdate(entity2)

        // Unique index on URI prevents duplicate row; entry is updated
        val allReviewed = reviewDao.getAllReviewedUris()
        assertEquals(1, allReviewed.size)
        val record = reviewDao.getDecisionForUri("uri://duplicate_check")
        assertEquals(ReviewDecision.KEEP.name, record?.decision)
    }

    @Test
    fun testResetThisFolderAffectsOnlySelectedCategoryAndPreservesDeleteDecisions() = runBlocking {
        // Camera items
        reviewDao.insertOrUpdate(ReviewItemEntity(mediaId = 1L, uri = "uri://cam1", displayName = "c1.jpg", sizeBytes = 100L, mimeType = "image/jpeg", isVideo = false, decision = "KEEP", category = SmartCategory.CAMERA.id))
        reviewDao.insertOrUpdate(ReviewItemEntity(mediaId = 2L, uri = "uri://cam2", displayName = "c2.jpg", sizeBytes = 100L, mimeType = "image/jpeg", isVideo = false, decision = "SKIP", category = SmartCategory.CAMERA.id))
        // Camera item marked for DELETE
        reviewDao.insertOrUpdate(ReviewItemEntity(mediaId = 3L, uri = "uri://cam3_del", displayName = "c3.jpg", sizeBytes = 100L, mimeType = "image/jpeg", isVideo = false, decision = "DELETE", category = SmartCategory.CAMERA.id))

        // Screenshot items
        reviewDao.insertOrUpdate(ReviewItemEntity(mediaId = 4L, uri = "uri://screen1", displayName = "s1.png", sizeBytes = 100L, mimeType = "image/png", isVideo = false, decision = "KEEP", category = SmartCategory.SCREENSHOTS.id))
        reviewDao.insertOrUpdate(ReviewItemEntity(mediaId = 5L, uri = "uri://screen2_del", displayName = "s2.png", sizeBytes = 100L, mimeType = "image/png", isVideo = false, decision = "DELETE", category = SmartCategory.SCREENSHOTS.id))

        // Verify initial counts
        assertEquals(2, reviewDao.getCountOfKeepSkipForCategory(SmartCategory.CAMERA.id))
        assertEquals(1, reviewDao.getCountOfPendingDeleteForCategory(SmartCategory.CAMERA.id))
        assertEquals(1, reviewDao.getCountOfKeepSkipForCategory(SmartCategory.SCREENSHOTS.id))

        // Reset ONLY Camera history
        val resetCount = reviewDao.resetKeepSkipHistoryForCategory(SmartCategory.CAMERA.id)
        assertEquals(2, resetCount)

        // Camera KEEP and SKIP are cleared
        assertEquals(0, reviewDao.getCountOfKeepSkipForCategory(SmartCategory.CAMERA.id))

        // CRITICAL SAFETY CHECK: Camera pending DELETE item is PRESERVED!
        assertNotNull(reviewDao.getDecisionForUri("uri://cam3_del"))
        assertEquals("DELETE", reviewDao.getDecisionForUri("uri://cam3_del")?.decision)

        // CRITICAL SAFETY CHECK: Screenshots category is completely UNCHANGED!
        assertNotNull(reviewDao.getDecisionForUri("uri://screen1"))
        assertEquals("KEEP", reviewDao.getDecisionForUri("uri://screen1")?.decision)
        assertNotNull(reviewDao.getDecisionForUri("uri://screen2_del"))
        assertEquals("DELETE", reviewDao.getDecisionForUri("uri://screen2_del")?.decision)
    }

    @Test
    fun testResetAllAffectsEveryCategoryAndPreservesDeleteDecisions() = runBlocking {
        // Items in multiple categories
        reviewDao.insertOrUpdate(ReviewItemEntity(mediaId = 1L, uri = "uri://cam", displayName = "c.jpg", sizeBytes = 100L, mimeType = "image/jpeg", isVideo = false, decision = "KEEP", category = SmartCategory.CAMERA.id))
        reviewDao.insertOrUpdate(ReviewItemEntity(mediaId = 2L, uri = "uri://scr", displayName = "s.png", sizeBytes = 100L, mimeType = "image/png", isVideo = false, decision = "SKIP", category = SmartCategory.SCREENSHOTS.id))
        reviewDao.insertOrUpdate(ReviewItemEntity(mediaId = 3L, uri = "uri://dl", displayName = "d.jpg", sizeBytes = 100L, mimeType = "image/jpeg", isVideo = false, decision = "KEEP", category = SmartCategory.DOWNLOADS.id))

        // Pending DELETE items in queue
        reviewDao.insertOrUpdate(ReviewItemEntity(mediaId = 4L, uri = "uri://del1", displayName = "del1.jpg", sizeBytes = 500L, mimeType = "image/jpeg", isVideo = false, decision = "DELETE", category = SmartCategory.CAMERA.id))
        reviewDao.insertOrUpdate(ReviewItemEntity(mediaId = 5L, uri = "uri://del2", displayName = "del2.jpg", sizeBytes = 600L, mimeType = "image/jpeg", isVideo = false, decision = "DELETE", category = SmartCategory.VIDEOS.id))

        // Reset All review history
        val resetCount = reviewDao.resetAllKeepSkipHistory()
        assertEquals(3, resetCount)

        // All KEEP and SKIP are gone
        assertEquals(0, reviewDao.getCountOfKeepSkipAll())

        // CRITICAL SAFETY CHECK: Pending DELETE decisions are NOT deleted!
        assertNotNull(reviewDao.getDecisionForUri("uri://del1"))
        assertNotNull(reviewDao.getDecisionForUri("uri://del2"))
        val remainingUris = reviewDao.getAllReviewedUris()
        assertEquals(2, remainingUris.size)
        assertTrue(remainingUris.contains("uri://del1"))
        assertTrue(remainingUris.contains("uri://del2"))
    }

    @Test
    fun testDatabaseMigrationPreservesExistingHistory() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Create a V1 database helper
        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name("test_migration_db")
            .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS review_items (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            mediaId INTEGER NOT NULL,
                            uri TEXT NOT NULL,
                            displayName TEXT NOT NULL,
                            sizeBytes INTEGER NOT NULL,
                            mimeType TEXT NOT NULL,
                            isVideo INTEGER NOT NULL,
                            decision TEXT NOT NULL,
                            decidedAt INTEGER NOT NULL,
                            durationMs INTEGER
                        )
                        """.trimIndent()
                    )
                    db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_review_items_uri ON review_items (uri)")
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()

        val helper = FrameworkSQLiteOpenHelperFactory().create(config)
        val db = helper.writableDatabase

        // Insert a V1 row
        db.execSQL(
            "INSERT INTO review_items (mediaId, uri, displayName, sizeBytes, mimeType, isVideo, decision, decidedAt, durationMs) " +
                    "VALUES (101, 'content://test/101', 'sample.jpg', 1024, 'image/jpeg', 0, 'KEEP', 100000, NULL)"
        )

        // Execute MIGRATION_1_2
        CleanerDatabase.MIGRATION_1_2.migrate(db)

        // Query the row back and verify the category column was added with default value and data is preserved
        val cursor = db.query("SELECT uri, decision, category FROM review_items WHERE uri = 'content://test/101'")
        assertTrue(cursor.moveToFirst())
        assertEquals("content://test/101", cursor.getString(0))
        assertEquals("KEEP", cursor.getString(1))
        assertEquals("all_media", cursor.getString(2))
        cursor.close()
        db.close()
        context.deleteDatabase("test_migration_db")
    }
}
