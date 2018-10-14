package satbe.kearny.com.sendalltextosbyemail

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.io.IOException
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*


open class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        sendMailButton.setOnClickListener {
            AsyncReadSmsTask().execute()
        }
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

    inner class AsyncReadSmsTask : AsyncTask<Void, Int, Map<Contacts, MutableList<Sms>>>() {
        private val smsMap: EnumMap<Contacts, MutableList<Sms>> = EnumMap(Contacts::class.java)

        override fun onPreExecute() {
            super.onPreExecute()
            progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg params: Void): Map<Contacts, MutableList<Sms>>? {

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

                            addSmsToSmsMap(contactEnum, sms, smsMap)
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

            return smsMap
        }

        private fun addSmsToSmsMap(contact: Contacts, sms: Sms, smsMap: EnumMap<Contacts, MutableList<Sms>>) {
            val smsList = smsMap[contact] ?: mutableListOf()
            smsList.add(sms)
            smsMap[contact] = smsList
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

        override fun onPostExecute(result: Map<Contacts, MutableList<Sms>>?) {
            super.onPostExecute(result)

            progressBar.visibility = View.GONE
            progressBar.progress = 0

            if (null == result) {
                Log.e(TAG, "Result is null!")
            } else {
                result.forEach {
                    val smsList = it.value
                    smsList.sortedWith(compareBy(Sms::time))
                    smsList.reverse()
                }

                // Save to JSON
                val gson = Gson()
                writeToFile(gson.toJson(result), this@MainActivity)

                val smsLoic = result[Contacts.LOIC_GAUVAIN]

                if (smsLoic != null) {
                    var mailBody = "<html><table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">"
                    smsLoic.forEach { sms ->
                        mailBody += "<tr>"
                        val dateFromSms = Date(sms.time)
                        val formatter = SimpleDateFormat("dd/MM/yyyy hh:mm", Locale.FRANCE)

                        mailBody += if (sms.folderName == "inbox") {
                            "<td align='left'>"
                        } else {
                            "<td align='right'>"
                        }

                        mailBody += "<small>${formatter.format(dateFromSms)}</small><br/>"
                        mailBody += "${sms.msg}</span></td></tr>"
                    }

                    mailBody += "</table></html>"

                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:alexandre.liscia@gmail.com"))
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Les sms de ${Contacts.LOIC_GAUVAIN.name}")
                    intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(mailBody))
                    startActivity(Intent.createChooser(intent, "Envoi du mail..."))
                }
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
    }
}
