package satbe.kearny.com.sendalltextosbyemail

data class Sms(
    val id: String,
    val address: String,
    val msg: String,
    val readState: String,
    val time: Long,
    val folderName: String
)