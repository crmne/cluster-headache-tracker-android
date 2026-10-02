package me.paolino.clusterheadachetracker.downloads

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FileDownloadsTest {
    @Test
    fun recognizesReportAndExportFiles() {
        assertTrue(FileDownloads.isDownloadLocation("https://clusterheadachetracker.com/reports/2026.pdf"))
        assertTrue(
            FileDownloads.isDownloadLocation("https://clusterheadachetracker.com/headache_log_export.csv?from=1"),
        )
        assertTrue(FileDownloads.isDownloadLocation("https://clusterheadachetracker.com/report.PDF#page=2"))
    }

    @Test
    fun leavesPagesToTheWebView() {
        assertFalse(FileDownloads.isDownloadLocation("https://clusterheadachetracker.com/headache_logs"))
        assertFalse(FileDownloads.isDownloadLocation("https://clusterheadachetracker.com/headache_log_print"))
        assertFalse(FileDownloads.isDownloadLocation("https://clusterheadachetracker.com/search?q=report.pdf"))
        assertFalse(FileDownloads.isDownloadLocation("https://clusterheadachetracker.com/pdf"))
    }
}
