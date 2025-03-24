package utils.general

object RandomStringUtils {
    private val simpleAlphabet: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    fun randomString(length: Int, alphabet: List<Char> = simpleAlphabet) =
        List(length) { alphabet.random() }.joinToString("")
}