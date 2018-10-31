package satbe.kearny.com.sendalltextosbyemail

import android.os.AsyncTask
import android.util.Log

class MailAsyncTask : AsyncTask<String, Int, Boolean>() {
    override fun doInBackground(vararg params: String): Boolean {
        var success = false

        val contactName = params[0].replace("_", " ").capitalize()
        val mailBody = params[1]
        try {
            SSLMailSender.sendMail("alexandre.liscia@gmail.com", "alexandre.liscia@gmail.com", "Texto de  $contactName", mailBody)
            success = true
        } catch (e: Exception) {
            Log.e(MainActivity.TAG, e.message, e)
        }

        return success
    }
}