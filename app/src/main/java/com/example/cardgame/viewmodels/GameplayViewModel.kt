package com.example.cardgame.viewmodels

import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cardgame.models.CardInfo
import com.example.cardgame.models.NewCardResponse
import com.example.cardgame.models.NewDeckResponse
import com.example.cardgame.utils.Consts
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import okhttp3.*
import java.io.IOException
import java.lang.Exception
import javax.inject.Inject

//todo make it @Singleton?
class GameplayViewModel @Inject constructor(

) : ViewModel() {

    private val TAG = "GameplayViewModel"
//    private var disposable: Disposable? = null
//    private var theGameHasEnded = false
//    private var currentImage = ""
//    private var returnCard = -1
    private var deckId = ""
    private var currentScore = 0
//    private var currentCard = -1

    val card = MutableLiveData<NewCardResponse>()

    fun getCardObservable(): Observable<CardInfo?> = Observable.fromCallable {

        if(deckId == "" || deckId == " ")
            createNewDeck()

        val client = OkHttpClient()
        val deckApi = "https://deckofcardsapi.com/api/deck/$deckId/draw/?count=1"
        val request = Request.Builder().url(deckApi).build()

        var cardInfo: CardInfo? = null

        try {
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val result = response.body()!!.string()
                val strRes = Gson().fromJson<NewCardResponse>(
                    result,
                    NewCardResponse::class.java
                )

                cardInfo = strRes.cards[0]
            } else {
                Log.d(TAG, "Response unsuccessful")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Draw card was unsuccessful due to network request fail")
        }
        Log.d(TAG, "card draw = ${cardInfo?.value}")
        cardInfo
    }

    private fun createNewDeck()  {
        val client = OkHttpClient()
        val deckApi = "https://deckofcardsapi.com/api/deck/new/shuffle/?deck_count=1"
        val request = Request.Builder().url(deckApi).build()

        val response = client.newCall(request).execute()
        try {
            if (response.isSuccessful) {
                val result = response.body()!!.string()
                val strRes = Gson().fromJson<NewDeckResponse>(
                    result,
                    NewDeckResponse::class.java
                )
                deckId = strRes.deckId
                Log.d(TAG, "onResponse-> deck id = $deckId")
                currentScore = 0
                //drawNewCard(false) { _, _ -> true }
            }
            else {
                Log.d(TAG, "Response unsuccessful")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Draw card was unsuccessful due to network request fail")
        }
    }
}
