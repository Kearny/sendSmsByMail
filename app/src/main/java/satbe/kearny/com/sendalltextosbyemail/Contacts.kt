package satbe.kearny.com.sendalltextosbyemail

enum class Contacts(val phoneNumbers: Array<String>) {
    ALBERT(arrayOf("692231886")),
    ALEXANDRE_LISCIA(arrayOf("783062443","769004605", "648968549", "662101715")),
    ALINE(arrayOf("692200651")),
    CHRISTINE(arrayOf("617456526")),
    DAMIEN_SEILER(arrayOf("662856302")),
    ERIC_BAZIN(arrayOf("617912153")),
    GUILLAUME_LE_CADRE(arrayOf("761709001")),
    JB(arrayOf("627289557")),
    JULES_HAMON(arrayOf("678322525")),
    LOIC_GAUVAIN(arrayOf("692244241", "641297413")),
    MATHIEU_PASCO(arrayOf("638224224")),
    MEME(arrayOf("771046619")),
    ;

    fun value(): Array<String> {
        return phoneNumbers
    }
}