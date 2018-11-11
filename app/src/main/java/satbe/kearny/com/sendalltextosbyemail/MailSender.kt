package satbe.kearny.com.sendalltextosbyemail

import android.util.Log

class MailSender {
    fun sendMail(contactName: String, mailBody: String): Boolean {
        var success = false


        try {
            SSLMailSender.sendMail(
                "alexandre.liscia@gmail.com",
                "alexandre.liscia@gmail.com",
                "Texto de  $contactName",
                mailBody
            )
            success = true
        } catch (e: Exception) {
            Log.e("KEARNY - $TAG", e.message, e)
        }

        return success
    }

    companion object {
        const val TAG = "MailSender"
    }
}