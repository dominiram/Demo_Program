package com.example.cardgame.viewmodels

import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cardgame.models.CardInfo
import com.example.cardgame.models.NewCardResponse
import com.example.cardgame.models.NewDeckResponse
import com.example.cardgame.utils.Consts
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.*
import java.io.IOException
import java.lang.Exception
import javax.inject.Inject
import javax.inject.Singleton

class GameplayViewModel @Inject constructor(

) : ViewModel() {

    var prevValue: String? = null
    private val TAG = "GameplayViewModel"
    private val _gameHasEnded = MutableLiveData<Boolean>()
    val gameHasEndedGetter: LiveData<Boolean>
        get() = _gameHasEnded
    var deckId = ""
    var score: Int = 0
    var prevCard: CardInfo? = null
    private var currentScore = MutableLiveData<Int>()
    val currentScoreGetter: LiveData<Int>
        get() = currentScore
    private var disposable: Disposable? = null
    private var _card = MutableLiveData<CardInfo>()
    val cardGetter: LiveData<CardInfo>
        get() = _card

    fun setWholeCardInfo(card: CardInfo) {
        this._card.value = card
        Log.d(TAG, "setWholeCard value = ${this._card.value?.value}")
    }

    fun setCardImage(card: String) {
        this._card.value?.image = card
    }

    fun setCurrentScore(score: Int) {
        this.score = score
        this.currentScore.value = score
    }

    private fun getCardObservable(): Observable<CardInfo?> = Observable.fromCallable {

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
            }
            else {
                Log.d(TAG, "Response unsuccessful")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Draw card was unsuccessful due to network request fail")
        }
    }

    fun drawNewCard(shouldCompare: Boolean, op: (Int, Int) -> Boolean) {
        disposable?.dispose()
        disposable = getCardObservable()
            .subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe {
                it?.apply {
                    prevCard = _card.value
                    _card.value = it
                    if(shouldCompare) {
                        Log.d(TAG, "COMPARING: current card = ${prevCard?.value}, " +
                                "next card = ${_card.value?.value}")
                        if(op(Consts.indexOf(_card.value!!.value),
                                Consts.indexOf(prevCard!!.value))){
                            score++
                            currentScore.value = score
                        }
                        else
                            _gameHasEnded.value = false
                    }
                }
            }
    }

}
