package satbe.kearny.com.sendalltextosbyemail

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.io.OutputStreamWriter
import java.util.*

open class MainActivity : AppCompatActivity() {

    val REQUEST_CODE_ASK_PERMISSIONS = 123

    private var contactsAndSmsMap: EnumMap<Contacts, MutableList<Sms>> = EnumMap(Contacts::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        sendMailButton.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            progressBar.progress = 0

            // https://kotlinlang.org/docs/reference/coroutines/basics.html#scope-builder
            runBlocking {
                coroutineScope {
                    launch { getSmsMap(contactsAndSmsMap) }
                }

                progressBar.visibility = View.GONE
            }

//
//            contactsAndSmsMap.forEach { contactEntry ->
//                val smsList = contactEntry.value
//                smsList.sortedWith(compareBy(Sms::time))
//                smsList.reverse()
//            }
//
//            contactsAndSmsMap.forEach { contact ->
//                val contactName = contact.key.name
//
//                var mailBody = "<html><table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">"
//                contact.value.forEach { sms ->
//                    mailBody += "<tr>"
//                    val dateFromSms = Date(sms.time)
//                    val formatter = SimpleDateFormat("dd/MM/yyyy hh:mm", Locale.FRANCE)
//
//                    mailBody += if (sms.folderName == "inbox") {
//                        "<td align='left'>"
//                    } else {
//                        "<td align='right'>"
//                    }
//
//                    mailBody += "<small>${formatter.format(dateFromSms)}</small><br/>"
//                    mailBody += "${sms.msg}</span></td></hr>"
//                }
//
//                mailBody += "</table></html>"
//
//                MailAsyncTask().execute(contactName, mailBody)
//            }
//
//            // Save to JSON
//            val gson = Gson()
//            writeToFile(gson.toJson(contactsAndSmsMap), this@MainActivity)
        }
    }

    private fun writeToFile(data: String, context: Context) {
        try {
            val outputStreamWriter = OutputStreamWriter(context.openFileOutput("sms.json", Context.MODE_PRIVATE))
            outputStreamWriter.write(data)
            outputStreamWriter.close()
        } catch (e: IOException) {
            Log.e("Exception", "File write failed: " + e.toString())
        }
    }

    private fun getSmsMap(contactsAndSmsMap: EnumMap<Contacts, MutableList<Sms>>) {
        if (ContextCompat.checkSelfPermission(
                baseContext,
                "android.permission.READ_SMS"
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                Array(1) { "android.permission.READ_SMS" },
                REQUEST_CODE_ASK_PERMISSIONS
            )
        }
        val message = Uri.parse("content://sms/")

        val cursor = contentResolver.query(message, null, null, null, null)
        if (null != cursor) {
            if (cursor.moveToFirst()) {

                progressBar.max = cursor.count

                do {
                    val contactEnum: Contacts? =
                        getContactFromPhoneNumber(cursor.getString(cursor.getColumnIndexOrThrow("address")))

                    if (contactEnum != null) {
                        val folderName = if (cursor.getString(cursor.getColumnIndexOrThrow("type")).contains("1")) {
                            "inbox"
                        } else {
                            "sent"
                        }

                        val sms = Sms(
                            cursor.getString(cursor.getColumnIndexOrThrow("_id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("address")),
                            cursor.getString(cursor.getColumnIndexOrThrow("body")),
                            cursor.getString(cursor.getColumnIndexOrThrow("read")),
                            cursor.getLong(cursor.getColumnIndexOrThrow("date")),
                            folderName
                        )

                        addSmsToSmsMap(contactEnum, sms, contactsAndSmsMap)
                    } else {
                        Log.d(
                            TAG,
                            "${cursor.getString(cursor.getColumnIndexOrThrow("address"))} ${cursor.getString(
                                cursor.getColumnIndexOrThrow("body")
                            )}"
                        )
                    }

                    progressBar.progress++
                } while (cursor.moveToNext())
            }

            cursor.close()
        }
    }

    private fun getContactFromPhoneNumber(string: String): Contacts? {
        for (contact in Contacts.values()) {
            for (phoneNumber in contact.phoneNumbers) {
                if (string.contains(phoneNumber)) {
                    return contact
                }
            }
        }

        return null
    }

    private fun addSmsToSmsMap(contact: Contacts, sms: Sms, smsMap: EnumMap<Contacts, MutableList<Sms>>) {
        val smsList = smsMap[contact] ?: mutableListOf()
        smsList.add(sms)
        smsMap[contact] = smsList
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        const val TAG = "MainActivity"
    }
}
