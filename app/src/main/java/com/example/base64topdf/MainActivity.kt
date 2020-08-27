package com.example.base64topdf

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream
import java.util.*


class MainActivity : AppCompatActivity() {

    private val mTag = "MainActivity"
    private lateinit var buttonExport: Button
    private lateinit var buttonView: Button
    private lateinit var buttonCreateFolder: Button
    private var newGlobalFilePath: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonExport = findViewById(R.id.buttonExport)
        buttonView = findViewById(R.id.buttonView)
        buttonCreateFolder = findViewById(R.id.buttonCreateFolder)
        buttonView.visibility = View.INVISIBLE

        buttonExport.setOnClickListener {
            try {
                createFolder()
            } catch (e: Exception) {
                Log.d("MainActivity", "catch error = ${e.message}")
            }
        }

        buttonView.setOnClickListener {
            openPDFFile()
        }

        buttonCreateFolder.setOnClickListener {
            createFolderInRoot()
        }

    }

    companion object {
        const val FOLDER_NAME = "kreduno"
    }

    private fun createFolderInRoot() {
        val direct =
            File("${Environment.getExternalStorageDirectory()}${File.separator}${Environment.DIRECTORY_DOWNLOADS}${File.separator}$FOLDER_NAME")
        Log.d(mTag, "folder path = ${direct.path}")
        if (direct.exists()) {
            Log.d(mTag, "folder already exist")
        } else {
            Log.d(mTag, "folder created now")
            direct.mkdir()
        }
    }

    private fun openPDFFile() {
        try {

            newGlobalFilePath?.let {

                Log.d(mTag, "file = ${it.path}")

                val mimeTypeMap = MimeTypeMap.getSingleton()
                val pdfIntent = Intent(Intent.ACTION_VIEW)
                val mimeType = mimeTypeMap.getMimeTypeFromExtension(it.extension)
                Log.d(mTag, "mimeType = $mimeType")
                val photoURI = FileProvider.getUriForFile(
                    applicationContext,
                    packageName.plus(".provider"), File("storage/emulated/0/Download/Demo.pdf")
                )
                pdfIntent.apply {
                    setDataAndType(photoURI, mimeType)
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(Intent.createChooser(this, "Open File Using..."))
                }

                /*val intent = Intent(Intent.ACTION_VIEW)
                val uri = Uri.fromFile(it)
                intent.setDataAndType(uri, "application/pdf")
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                try {
                    startActivity(Intent.createChooser(intent, "Select"))
                } catch (e: Exception) {
                    Log.d(mTag, "catch error 1 = ${e.message}")
                    Toast.makeText(this, "No Application available to view pdf", Toast.LENGTH_LONG)
                        .show()
                }*/

            } ?: kotlin.run {
                Log.d(mTag, "newGlobalFilePath is null")

            }

        } catch (e: Exception) {
            Log.d(mTag, "catch error 2 = ${e.message}")
            Toast.makeText(this, "No handler for this type of file.", Toast.LENGTH_LONG).show()
        }
    }

    private fun createFolder() {
        val fileRoot = File(getExternalFilesDir(null), "Invoices")
        Log.d(mTag, "new filePath = $fileRoot")
        if (fileRoot.exists()) {
            Log.d(mTag, "folder already exist")
            createPDF(fileRoot)
        } else {
            Log.d(mTag, "if not then create new folder")
            val isCreated = fileRoot.mkdirs()
            if (isCreated) {
                createPDF(fileRoot)
            } else {
                Log.d(mTag, "folder not created")
            }
        }
    }

    private fun createPDF(fileRoot: File) {
        var pdfAsBytes: ByteArray?
        val newFileName = "Demo.pdf"
        val newFilePath = File(fileRoot.path.plus(File.separator), newFileName)
        newGlobalFilePath = newFilePath
        val fos = FileOutputStream(newFilePath, false)

        Log.d(mTag, "before runBlock")
        runBlocking {
            try {
                pdfAsBytes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Base64.getDecoder().decode(getString(R.string.base64Data))
                } else {
                    android.util.Base64.decode(
                        getString(R.string.base64Data),
                        android.util.Base64.DEFAULT
                    )
                }

                /*pdfAsBytes = android.util.Base64.decode(
                    getString(R.string.base64Data),
                    android.util.Base64.DEFAULT
                )*/

                fos.write(pdfAsBytes)
                fos.flush()
                fos.close()
                Log.d(mTag, "finish runBlock")
                buttonView.visibility = View.VISIBLE
            } catch (e: java.lang.Exception) {
                Log.d(mTag, "catch error = ${e.message}")
            }
        }
        Log.d(mTag, "after runBlock")
    }

}