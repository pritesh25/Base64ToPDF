package com.example.base64topdf

import android.content.Intent
import android.os.Build
import android.os.Bundle
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
    private var newGlobalFilePath: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonExport = findViewById(R.id.buttonExport)
        buttonView = findViewById(R.id.buttonView)
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
    }

    private fun openPDFFile() {
        try {
            val mimeTypeMap = MimeTypeMap.getSingleton()
            val pdfIntent = Intent(Intent.ACTION_VIEW)
            val mimeType = mimeTypeMap.getMimeTypeFromExtension(newGlobalFilePath!!.extension)
            Log.d(mTag, "mimeType = $mimeType")
            val photoURI = FileProvider.getUriForFile(
                applicationContext,
                packageName.plus(".provider"), newGlobalFilePath!!
            )
            pdfIntent.apply {
                setDataAndType(photoURI, mimeType);
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(this)
            }

        } catch (e: Exception) {
            Log.d(mTag, "catch error = ${e.message}")
            Toast.makeText(this, "No handler for this type of file.", Toast.LENGTH_LONG).show()
        }
    }

    private fun createFolder() {
        val fileRoot = File(getExternalFilesDir(null), "Invoices")
        Log.d(mTag, "new filePath = $fileRoot")
        if (fileRoot.exists()) {
            Log.d(mTag, "folder already exist")
        } else {
            Log.d(mTag, "if not then create new folder")
            fileRoot.mkdir()
        }
        var pdfAsBytes: ByteArray? = null
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

                fos.write(pdfAsBytes)
                //fos.write(decode(getString(R.string.base64Data), NO_WRAP))
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