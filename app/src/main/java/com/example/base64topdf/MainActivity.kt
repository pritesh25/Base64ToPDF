package com.example.base64topdf

import android.app.DownloadManager
import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.*


class MainActivity : AppCompatActivity() {

    private val mTag = "MainActivity"
    private lateinit var buttonExport: Button
    private lateinit var buttonView: Button
    private lateinit var buttonSave: Button
    private var newGlobalFilePath: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonExport = findViewById(R.id.buttonExport)
        buttonView = findViewById(R.id.buttonView)
        buttonSave = findViewById(R.id.buttonSave)

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

        buttonSave.setOnClickListener {
            //saveInDownloadFolder(newFilePath)//click
        }

        findViewById<Button>(R.id.buttonCreateFolder).setOnClickListener {
            createMyFolder()
        }

    }

    private fun createMyFolder() {

        /*val sub: File = File(filesDir, "subdirectory")
        Log.d(mTag, "sub = $sub")
        if (!sub.exists()) sub.mkdirs()*/


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = contentResolver
            val contentValues = ContentValues().apply {
                //put(MediaStore.MediaColumns.DISPLAY_NAME, "CuteKitten001")
                //put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(
                    MediaStore.Downloads.RELATIVE_PATH,
                    Environment.DIRECTORY_DOWNLOADS + File.separator + "Pritesh"
                )
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            resolver.openOutputStream(uri!!).use {

            }
        } else {
            Log.d(mTag, "rootFile = ${Environment.getExternalStorageState()}")
            val rootDirectory =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator)

            Log.d(mTag, "rootFile = $rootDirectory")
            Log.d(mTag, "rootFile isDirectory = ${rootDirectory.isDirectory}")

            if (rootDirectory.exists()) {
                //no need to create
                Log.d(mTag, "root folder already exist, now create custom Directory")
                createMyNameFolder()
            } else {
                val status = rootDirectory.mkdir()
                Log.d(mTag, "directory created | status = $status")
                if (status) {
                    createMyNameFolder()
                } else {
                    Log.d(mTag, "unable to create folder")
                }
            }
        }
    }

    private fun createMyNameFolder() {
        val myDirectory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Pritesh" + File.separator)
        if (myDirectory.exists()) {
            Log.d(mTag, "myDirectory folder already exist")
        } else {
            Log.d(mTag, "myDirectory folder created now")
            myDirectory.mkdir()
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

        Log.d(mTag, "coroutine before")
        GlobalScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                try {
                    pdfAsBytes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Base64.getDecoder().decode(getString(R.string.base64Data))
                    } else {
                        android.util.Base64.decode(
                            getString(R.string.base64Data),
                            android.util.Base64.DEFAULT
                        )
                    }
                    fos.write(pdfAsBytes)
                    fos.flush()
                    Log.d(mTag, "coroutine finish")
                } catch (e: java.lang.Exception) {
                    Log.d(mTag, "catch error = ${e.message}")
                } finally {
                    fos.close()
                }
            }
            buttonView.visibility = View.VISIBLE
            Log.d(mTag, "coroutine after withcontext")
            saveInDownloadFolder2(newGlobalFilePath!!)//after Coroutine
        }
        Log.d(mTag, "coroutine after GlobalScope")
    }

    private fun openPDFFile() {
        try {

            newGlobalFilePath?.let { /* openPDFFile */

                Log.d(mTag, "file = ${it.path}")

                val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(it.extension)
                Log.d(mTag, "mimeType = $mimeType")
                val photoURI = FileProvider.getUriForFile(
                    applicationContext,
                    "com.example.base64topdf.provider", it
                )
                val pdfIntent = Intent(Intent.ACTION_VIEW)
                pdfIntent.apply {
                    setDataAndType(photoURI, mimeType)
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(this)
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

    private fun saveInDownloadFolder(it: File) {

        //Log.d(mTag, "(saveInDownloadFolder) it exist ? = ${it!!.exists()}")
        //Log.d(mTag, "(saveInDownloadFolder) it read    = ${it.canRead()}")

        //it?.let { /* saveInDownloadFolder */

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // You can add more columns.. Complete list of columns can be found at
            // https://developer.android.com/reference/android/provider/MediaStore.Downloads
            val contentValues = ContentValues()
            contentValues.put(MediaStore.Downloads.TITLE, it.name);
            contentValues.put(MediaStore.Downloads.DISPLAY_NAME, it.name);
            contentValues.put(
                MediaStore.Downloads.MIME_TYPE,
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(it.extension)
            );
            contentValues.put(MediaStore.Downloads.SIZE, it.length());

            // If you downloaded to a specific folder inside "Downloads" folder
            contentValues.put(
                MediaStore.Downloads.RELATIVE_PATH,
                Environment.DIRECTORY_DOWNLOADS + File.separator + "Temp"
            );

            // Insert into the database
            val database = contentResolver;
            database.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        } else {
            val downloadService = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            downloadService.addCompletedDownload(
                it.name,
                it.name,
                true,
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(it.extension),
                it.absolutePath,
                it.length(),
                true
            )
        }

        /*} ?: kotlin.run {
            Log.d(mTag, "(saveInDownloadFolder) newGlobalFilePath is null")
        }*/

        Log.d(mTag, "after let newFilePath = $it")

    }

    private fun saveInDownloadFolder2(it: File?) {

        //Log.d(mTag, "(saveInDownloadFolder) it exist ? = ${it!!.exists()}")
        //Log.d(mTag, "(saveInDownloadFolder) it read    = ${it.canRead()}")

        it?.let { /* saveInDownloadFolder */

            val downloadService = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                downloadService.addCompletedDownload(
                    it.name,
                    it.name,
                    true,
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(it.extension),
                    it.absolutePath,
                    it.length(),
                    true
                )
            } else {
                downloadService.addCompletedDownload(
                    it.name,
                    it.name,
                    true,
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(it.extension),
                    it.absolutePath,
                    it.length(),
                    true
                )
            }

        } ?: kotlin.run {
            Log.d(mTag, "(saveInDownloadFolder) newGlobalFilePath is null")
        }

        Log.d(mTag, "after let newFilePath = $it")

    }
}