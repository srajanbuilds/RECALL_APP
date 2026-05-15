package com.recall.app.activities

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.recall.app.R
import com.recall.app.core.ai.ModelDownloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.GZIPInputStream

class ModelSetupActivity : AppCompatActivity() {

    // -- Views --
    private lateinit var progressEmbedding: ProgressBar
    private lateinit var statusEmbedding: TextView
    private lateinit var progressLlm: ProgressBar
    private lateinit var statusLlm: TextView
    private lateinit var cardPrivacy: MaterialCardView
    private lateinit var btnStartDownload: MaterialButton
    private lateinit var btnDownloadFromKaggle: MaterialButton
    private lateinit var btnPickLlmFile: MaterialButton
    private lateinit var llmPickInfo: TextView
    private lateinit var btnDone: MaterialButton
    private lateinit var btnSkip: TextView
    private lateinit var subtitleText: TextView

    // -- Download tracking --
    private var embeddingDownloadId: Long = -1L
    private val handler = Handler(Looper.getMainLooper())
    private var isPolling = false

    // -- File picker for LLM model --
    private val pickFileLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                copyLlmModelFromUri(uri)
            }
        }

    private val downloadCompleteReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            checkAllDownloadsComplete()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If both models already exist, skip straight through
        if (ModelDownloader.areBothModelsReady(this)) {
            ModelDownloader.markSetupComplete(this)
            launchAskRecall()
            return
        }

        setContentView(R.layout.activity_model_setup)

        // Bind views
        progressEmbedding = findViewById(R.id.progressEmbedding)
        statusEmbedding = findViewById(R.id.statusEmbedding)
        progressLlm = findViewById(R.id.progressLlm)
        statusLlm = findViewById(R.id.statusLlm)
        cardPrivacy = findViewById(R.id.cardPrivacy)
        btnStartDownload = findViewById(R.id.btnStartDownload)
        btnDownloadFromKaggle = findViewById(R.id.btnDownloadFromKaggle)
        btnPickLlmFile = findViewById(R.id.btnPickLlmFile)
        llmPickInfo = findViewById(R.id.llmPickInfo)
        btnDone = findViewById(R.id.btnDone)
        btnSkip = findViewById(R.id.btnSkip)
        subtitleText = findViewById(R.id.subtitleText)

        // Pre-fill status for models that are already downloaded
        if (ModelDownloader.isEmbeddingModelReady(this)) {
            statusEmbedding.text = "✓ Already downloaded"
        }
        if (ModelDownloader.isLlmModelReady(this)) {
            statusLlm.text = "✓ Already downloaded"
            btnDownloadFromKaggle.visibility = View.GONE
        }

        // -- Button listeners --
        btnStartDownload.setOnClickListener { startSetup() }

        btnDownloadFromKaggle.setOnClickListener {
            // Open Kaggle model page in the user's browser
            val url = "https://www.kaggle.com/models/google/gemma/tfLite/gemma-2b-it-cpu-int4"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)

            // Show the "pick file" UI now that they know what to download
            llmPickInfo.visibility = View.VISIBLE
            btnPickLlmFile.visibility = View.VISIBLE
        }

        btnPickLlmFile.setOnClickListener {
            pickFileLauncher.launch("*/*")
        }

        btnDone.setOnClickListener {
            ModelDownloader.markSetupComplete(this)
            launchAskRecall()
        }
        btnSkip.setOnClickListener { finish() }

        // Register broadcast receiver for download-complete events
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(downloadCompleteReceiver, filter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(downloadCompleteReceiver, filter)
        }
    }

    override fun onResume() {
        super.onResume()
        checkAllDownloadsComplete()
    }

    override fun onDestroy() {
        super.onDestroy()
        isPolling = false
        try { unregisterReceiver(downloadCompleteReceiver) } catch (_: Exception) {}
    }

    // =====================================================================
    // Step 1: Auto-download the embedding model
    // =====================================================================

    private fun startSetup() {
        btnStartDownload.isEnabled = false
        btnStartDownload.text = "Setting up…"

        if (!ModelDownloader.isEmbeddingModelReady(this)) {
            embeddingDownloadId = ModelDownloader.startEmbeddingDownload(this)
            progressEmbedding.visibility = View.VISIBLE
            statusEmbedding.text = "Starting download…"
            startProgressPolling()
        } else {
            statusEmbedding.text = "✓ Already downloaded"
        }

        if (!ModelDownloader.isLlmModelReady(this)) {
            llmPickInfo.visibility = View.VISIBLE
            btnPickLlmFile.visibility = View.VISIBLE
            statusLlm.text = "Tap \"Open Kaggle\" above, then come back and select the file."
        }
    }

    private fun startProgressPolling() {
        isPolling = true
        handler.post(object : Runnable {
            override fun run() {
                if (!isPolling) return
                updateEmbeddingProgress()
                handler.postDelayed(this, 500)
            }
        })
    }

    private fun updateEmbeddingProgress() {
        if (embeddingDownloadId == -1L) return

        val progress = ModelDownloader.queryProgress(this, embeddingDownloadId)
        if (progress != null) {
            when {
                progress.isComplete -> {
                    progressEmbedding.progress = 100
                    statusEmbedding.text = "✓ Download complete"
                    embeddingDownloadId = -1L
                    checkAllDownloadsComplete()
                }
                progress.isFailed -> {
                    statusEmbedding.text = "✗ Download failed — check your connection and retry."
                    btnStartDownload.isEnabled = true
                    btnStartDownload.text = "Retry"
                    embeddingDownloadId = -1L
                }
                progress.isRunning -> {
                    progressEmbedding.progress = progress.percent
                    val mbDown = progress.bytesDownloaded / (1024 * 1024)
                    val mbTotal = progress.totalBytes / (1024 * 1024)
                    statusEmbedding.text = "Downloading… $mbDown MB / $mbTotal MB (${progress.percent}%)"
                }
                progress.isPending -> {
                    statusEmbedding.text = "Waiting for network…"
                }
            }
        }
    }

    // =====================================================================
    // Step 2: Copy user-downloaded LLM file into app storage
    // =====================================================================

    private fun copyLlmModelFromUri(uri: Uri) {
        progressLlm.visibility = View.VISIBLE
        statusLlm.text = "Copying model file… this may take a moment."
        btnPickLlmFile.isEnabled = false

        lifecycleScope.launch {
            val success = withContext(Dispatchers.IO) {
                try {
                    val destFile = ModelDownloader.getLlmModelFile(this@ModelSetupActivity)
                    val inputStream = contentResolver.openInputStream(uri)
                        ?: throw Exception("Cannot open file")

                    val buffered = BufferedInputStream(inputStream, 8192)

                    // Detect gzip by magic bytes (0x1f, 0x8b)
                    buffered.mark(2)
                    val b1 = buffered.read()
                    val b2 = buffered.read()
                    buffered.reset()
                    val isGzip = (b1 == 0x1f && b2 == 0x8b)

                    if (isGzip) {
                        extractModelFromTarGz(GZIPInputStream(buffered), destFile)
                    } else {
                        // Direct copy — user picked the raw .bin file
                        FileOutputStream(destFile).use { out ->
                            buffered.copyTo(out)
                        }
                    }

                    buffered.close()
                    true
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }

            if (success) {
                statusLlm.text = "✓ Model installed successfully"
                progressLlm.visibility = View.GONE
                btnPickLlmFile.visibility = View.GONE
                btnDownloadFromKaggle.visibility = View.GONE
                llmPickInfo.visibility = View.GONE
                checkAllDownloadsComplete()
            } else {
                statusLlm.text = "✗ Failed — make sure you picked the correct file."
                btnPickLlmFile.isEnabled = true
                progressLlm.visibility = View.GONE
            }
        }
    }

    /**
     * Minimal tar.gz extractor — reads 512-byte tar headers to find the first
     * .bin or .task file and writes it to [destFile].
     * This avoids needing the Apache Commons Compress library.
     */
    private fun extractModelFromTarGz(gzipStream: InputStream, destFile: java.io.File) {
        val headerBuf = ByteArray(512)
        var found = false

        while (true) {
            // Read a 512-byte tar header
            val headerRead = readFully(gzipStream, headerBuf)
            if (headerRead < 512) break

            // Check for end-of-archive (all zeros)
            if (headerBuf.all { it == 0.toByte() }) break

            // Extract file name from header bytes 0..99
            val nameBytes = headerBuf.sliceArray(0 until 100)
            val name = String(nameBytes, Charsets.US_ASCII).trimEnd('\u0000').lowercase()

            // Extract file size from header bytes 124..135 (octal ASCII)
            val sizeStr = String(headerBuf.sliceArray(124 until 136), Charsets.US_ASCII).trimEnd('\u0000').trim()
            val fileSize = if (sizeStr.isNotEmpty()) sizeStr.toLongOrNull(8) ?: 0L else 0L

            // Extract type flag from header byte 156 ('0' or '\0' = regular file)
            val typeFlag = headerBuf[156]
            val isRegularFile = (typeFlag == '0'.code.toByte() || typeFlag == 0.toByte())

            if (isRegularFile && fileSize > 0 && (name.endsWith(".bin") || name.endsWith(".task"))) {
                // Found our model file — stream it directly to disk
                FileOutputStream(destFile).use { out ->
                    var remaining = fileSize
                    val buf = ByteArray(8192)
                    while (remaining > 0) {
                        val toRead = minOf(buf.size.toLong(), remaining).toInt()
                        val read = gzipStream.read(buf, 0, toRead)
                        if (read == -1) break
                        out.write(buf, 0, read)
                        remaining -= read
                    }
                }
                found = true
                break
            } else {
                // Skip this entry's data (padded to 512-byte boundary)
                val blocks = (fileSize + 511) / 512
                val toSkip = blocks * 512
                skipFully(gzipStream, toSkip)
            }
        }

        gzipStream.close()
        if (!found) throw Exception("No .bin or .task model file found in archive")
    }

    private fun readFully(stream: InputStream, buf: ByteArray): Int {
        var offset = 0
        while (offset < buf.size) {
            val read = stream.read(buf, offset, buf.size - offset)
            if (read == -1) return offset
            offset += read
        }
        return offset
    }

    private fun skipFully(stream: InputStream, count: Long) {
        var remaining = count
        val buf = ByteArray(8192)
        while (remaining > 0) {
            val toRead = minOf(buf.size.toLong(), remaining).toInt()
            val read = stream.read(buf, 0, toRead)
            if (read == -1) break
            remaining -= read
        }
    }

    // =====================================================================
    // Completion
    // =====================================================================

    private fun checkAllDownloadsComplete() {
        if (ModelDownloader.areBothModelsReady(this)) {
            isPolling = false

            cardPrivacy.visibility = View.VISIBLE
            btnDone.visibility = View.VISIBLE
            btnStartDownload.visibility = View.GONE
            btnSkip.visibility = View.GONE
            btnDownloadFromKaggle.visibility = View.GONE
            btnPickLlmFile.visibility = View.GONE
            llmPickInfo.visibility = View.GONE

            subtitleText.text = "Setup complete! Recall AI is ready to use."
            statusEmbedding.text = "✓ Ready"
            statusLlm.text = "✓ Ready"
        }
    }

    private fun launchAskRecall() {
        val intent = Intent(this, AskRecallActivity::class.java)
        startActivity(intent)
        finish()
    }
}
