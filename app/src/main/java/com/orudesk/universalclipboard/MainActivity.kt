package com.orudesk.universalclipboard

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.transition.Slide
import android.transition.TransitionManager
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.popup.view.*
import java.io.File
import java.io.FileNotFoundException
import java.security.SecureRandom
import java.util.*

class MainActivity : AppCompatActivity() {

    var id: String? = "";
    val FILE_NAME = "orudesk-uuid"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // If ID is already present use it
        try {
            var textFromFile = File("$filesDir/$FILE_NAME").readText(Charsets.UTF_8)
            id = textFromFile;

        } catch (e: FileNotFoundException) {
        }

        if (id == null || id == "") {
            getUserName()
        }


        var hideWatchToast = false;

        var baseUrl = "copy/$id"
        var firebaseUrl = "$baseUrl/value";
        var websiteUrl = "https://oru-desk.web.app/$baseUrl"

        val copyButton = findViewById<Button>(R.id.btn_copy)
        val uploadButton = findViewById<Button>(R.id.btn_upload)
        val downloadButton = findViewById<Button>(R.id.btn_download)
        val clearButton = findViewById<Button>(R.id.btn_clear)

        val urlText = findViewById<TextView>(R.id.txt_url)
        val clipBoxText = findViewById<EditText>(R.id.txt_clipbox)
        urlText.setText(websiteUrl)

        copyButton.setOnClickListener {
            copyButton.isEnabled = false
            copyButton.isClickable = false


            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(id, clipBoxText.text)
            clipboard.setPrimaryClip(clip);
            Toast.makeText(applicationContext, "Copied to clipboard", Toast.LENGTH_SHORT).show()

            copyButton.isEnabled = true
            copyButton.isClickable = true
        }
        uploadButton.setOnClickListener {
            uploadButton.isEnabled = false
            uploadButton.isClickable = false


            baseUrl = "copy/$id"
            firebaseUrl = "$baseUrl/value";
            websiteUrl = "https://oru-desk.web.app/$baseUrl"

            hideWatchToast = true
            val database = Firebase.database
            val myRef = database.getReference(firebaseUrl)

            myRef.setValue(clipBoxText.text.toString())
                .addOnSuccessListener {
                    Toast.makeText(applicationContext, "Uploaded", Toast.LENGTH_SHORT).show()
                    hideWatchToast = false
                    uploadButton.isEnabled = true
                    uploadButton.isClickable = true
                }
                .addOnFailureListener {
                    Toast.makeText(applicationContext, "Some error uploading", Toast.LENGTH_LONG)
                        .show()
                    hideWatchToast = false
                    uploadButton.isEnabled = true
                    uploadButton.isClickable = true
                }
        }
        downloadButton.setOnClickListener {
            downloadButton.isEnabled = false
            downloadButton.isClickable = false


            baseUrl = "copy/$id"
            firebaseUrl = "$baseUrl/value";
            websiteUrl = "https://oru-desk.web.app/$baseUrl"

            val messageListener = object : ValueEventListener {

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val message = dataSnapshot.getValue()
                        clipBoxText.setText(message.toString())
                        if (!hideWatchToast) {
                            Toast.makeText(applicationContext, "Updated", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(applicationContext, "No Data", Toast.LENGTH_LONG).show()
                    }

                    downloadButton.isEnabled = true
                    downloadButton.isClickable = true
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(applicationContext, "Some unexpected error", Toast.LENGTH_LONG)
                        .show()
                    downloadButton.isEnabled = true
                    downloadButton.isClickable = true
                }
            }

            val database = Firebase.database
            val myRef = database.getReference(firebaseUrl)
            myRef.addListenerForSingleValueEvent(messageListener)


        }

        clearButton.setOnClickListener {
            clearButton.isEnabled = false
            clearButton.isClickable = false


            baseUrl = "copy/$id"
            firebaseUrl = "$baseUrl/value";
            websiteUrl = "https://oru-desk.web.app/$baseUrl"

            hideWatchToast = true;
            val database = Firebase.database
            val myRef = database.getReference(firebaseUrl)

            myRef.setValue("")
                .addOnSuccessListener {
                    Toast.makeText(applicationContext, "Cleared in server too", Toast.LENGTH_SHORT)
                        .show()
                    hideWatchToast = false;
                    clearButton.isEnabled = true
                    clearButton.isClickable = true
                }
                .addOnFailureListener {
                    Toast.makeText(applicationContext, "Not cleared in server", Toast.LENGTH_LONG)
                        .show()
                    hideWatchToast = false;
                    clearButton.isEnabled = true
                    clearButton.isClickable = true
                }
            clipBoxText.setText("")
        }
    }

    private fun generateRandomString(length: Int): String? {
        val RANDOM = SecureRandom()
        val ALPHABET: String = "abcdefghijklmnopqrstuvwxyz";
        val returnValue = StringBuilder(length)
        for (i in 0 until length) {
            returnValue.append(ALPHABET.get(RANDOM.nextInt(ALPHABET.length)))
        }
        return String(returnValue)
    }

    private fun getUserName() {
        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.popup, null)
        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)
            .setCancelable(false)
            .setTitle("Set User Name")
        //show dialog
        val mAlertDialog = mBuilder.show()

        mDialogView.btn_username.setOnClickListener {
            mDialogView.btn_username.isEnabled = false
            mDialogView.btn_username.isClickable = false

            val messageListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        mDialogView.txt_username.setText("")
                        mDialogView.txt_username.hint = "User Name not available"
                    } else {

                        id = mDialogView.txt_username.text.toString()
                        txt_url.setText("http://oru-desk.web.app/copy/$id")
                        mDialogView.txt_username.setText("")

                        try {
                            File("$filesDir/$FILE_NAME").writeText(id.toString())
                        } catch (e: Exception) {
                        }
                        mAlertDialog.dismiss()
                    }

                    mDialogView.btn_username.isEnabled = true
                    mDialogView.btn_username.isClickable = true
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    mDialogView.txt_username.setText("")
                    mDialogView.txt_username.hint = "User Name not available"

                    mDialogView.btn_username.isEnabled = true
                    mDialogView.btn_username.isClickable = true
                }
            }

            val database = Firebase.database
            val myRef = database.getReference("copy/${mDialogView.txt_username.text.toString()}")
            myRef.addListenerForSingleValueEvent(messageListener)
        }
    }

}
