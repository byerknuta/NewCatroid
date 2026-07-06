package org.catrobat.catroid.test.common

import org.catrobat.catroid.common.NewCatroidHttpManager
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class NewCatroidHttpManagerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testGetRequestAndFormulas() {
        val requestId = "test_get"
        val url = "https://httpbingo.org/get"

        NewCatroidHttpManager.createRequest(requestId, "GET", url)

        NewCatroidHttpManager.setConfig(requestId, "header", "User-Agent", "NewCatroidTestClient")
        NewCatroidHttpManager.setConfig(requestId, "query", "test_param", "working")

        val latch = CountDownLatch(1)
        NewCatroidHttpManager.executeRequest(requestId) {
            latch.countDown()
        }

        assertTrue("Request timed out", latch.await(15, TimeUnit.SECONDS))

        val code = NewCatroidHttpManager.getResponseCode(requestId)
        assertEquals(200, code)

        val text = NewCatroidHttpManager.getResponseText(requestId)
        assertTrue("Response doesn't contain header", text.contains("NewCatroidTestClient"))
        assertTrue("Response doesn't contain query param", text.contains("working"))
    }

    @Test
    fun testDownloadAndUploadFile() {
        val downloadId = "test_download"
        val uploadId = "test_upload"

        val testDir = tempFolder.newFolder("catroid_project")
        val localFile = File(testDir, "downloaded_logo.png")

        NewCatroidHttpManager.createRequest(downloadId, "GET", "https://httpbingo.org/image/png")

        val downloadLatch = CountDownLatch(1)
        NewCatroidHttpManager.executeRequest(downloadId) {
            downloadLatch.countDown()
        }
        assertTrue("Download timed out", downloadLatch.await(15, TimeUnit.SECONDS))

        val saveSuccess = NewCatroidHttpManager.saveResponseToFile(downloadId, localFile)
        assertTrue("Failed to save response to file", saveSuccess)
        assertTrue("File does not exist on disk", localFile.exists())
        assertTrue("File is empty", localFile.length() > 0)

        NewCatroidHttpManager.createRequest(uploadId, "POST", "https://httpbingo.org/post")
        NewCatroidHttpManager.attachFile(uploadId, localFile, "file", "image/png")

        val uploadLatch = CountDownLatch(1)
        NewCatroidHttpManager.executeRequest(uploadId) {
            uploadLatch.countDown()
        }
        assertTrue("Upload timed out", uploadLatch.await(15, TimeUnit.SECONDS))

        val uploadResponse = NewCatroidHttpManager.getResponseText(uploadId)

        assertTrue("File was not uploaded correctly: $uploadResponse",
            uploadResponse.contains("downloaded_logo.png") || uploadResponse.contains("\"file\"")
        )
    }
}
