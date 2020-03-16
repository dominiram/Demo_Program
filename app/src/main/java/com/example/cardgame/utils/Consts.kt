package com.example.cardgame.utils

object Consts {

    const val SAVED_SCORE_KEY = "saved_score_key"
    const val SAVED_CARD_KEY = "saved_card_key"
    const val SAVED_DECK_ID = "saved_deck_id_key"
    const val SAVED_IMAGE_KEY = "saved_image_key"

    fun getName(i: Int) : String {
        when(i) {
            0 -> return "2"
            1 -> return "3"
            2 -> return "4"
            3 -> return "5"
            4 -> return "6"
            5 -> return "7"
            6 -> return "8"
            7 -> return "9"
            8 -> return "10"
            9 -> return "JACK"
            10 -> return "QUEEN"
            11 -> return "KING"
            12 -> return "ACE"
        }
        return "no card"
    }
    fun indexOf(string: String): Int {
        when(string){
            "2" -> return 0
            "3" -> return 1
            "4" -> return 2
            "5" -> return 3
            "6" -> return 4
            "7" -> return 5
            "8" -> return 6
            "9" -> return 7
            "10" -> return 8
            "JACK" -> return 9
            "QUEEN" -> return 10
            "KING" -> return 11
            "ACE" -> return 12
        }
        return -1
    }

}