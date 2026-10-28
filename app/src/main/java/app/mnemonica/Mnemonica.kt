package app.mnemonica

/** Juan Tamariz's Mnemonica stack, top (1) to bottom (52). T is ten. */
val mnemonica: List<Card> =
    ("4C 2H 7D 3C 4H 6D AS 5H 9S 2S QH 3D QC 8H 6S 5S 9H KC 2D JH 3S 8S 6H TC 5D KD " +
        "2C 3H 8D 5C KS JD 8C TS KH JC 7S TH AD 4S 7H 4D AC 9C JS QD 7C QS TD 6C AH 9D")
        .split(' ')
        .map { Card(rank = it[0].toString().replace("T", "10"), suit = Suit.of(it[1])) }

data class Card(val rank: String, val suit: Suit)

enum class Suit(val code: Char, val symbol: String) {
    // U+FE0E keeps the glyphs as text instead of colour emoji.
    Clubs('C', "♣︎"),
    Hearts('H', "♥︎"),
    Spades('S', "♠︎"),
    Diamonds('D', "♦︎");

    companion object {
        fun of(code: Char) = entries.first { it.code == code }
    }
}

/** Deals every position once, in random order, before reshuffling. */
object Deck {
    private val bag = ArrayDeque<Int>()

    fun next(): Int {
        if (bag.isEmpty()) bag += (1..mnemonica.size).shuffled()
        return bag.removeFirst()
    }
}
