package com.example.base64topdf

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream
import java.util.*

class MainActivity : AppCompatActivity() {

    private val mTag = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {
            createFolder()
        } catch (e: Exception) {
            Log.d("MainActivity", "catch error = ${e.message}")
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
        val fos = FileOutputStream(File(fileRoot.path.plus(File.separator), newFileName), false)

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
            } catch (e: java.lang.Exception) {
                Log.d(mTag, "catch error = ${e.message}")
            }
        }
        Log.d(mTag, "after runBlock")
    }

}