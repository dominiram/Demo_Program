package com.example.cardgame

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.cardgame.utils.Consts
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.squareup.picasso.Picasso
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.*
import java.io.IOException
import java.lang.Exception


/**
 * A simple [Fragment] subclass.
 * Use the [GameplayFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GameplayFragment : Fragment() {

    private var theGameHasEnded = false
    private val GAME_SCORE_KEY = "current_game_score"
    private var currentImage = ""
    private var returnCard = -1
    private var root: View? = null
    private var deckId = ""
    private var currentScore = 0
    private var currentCard = -1
    private val TAG = "Fragment gameplay"

    private var disposable : Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "Created!")
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
        disposable = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        theGameHasEnded = false
        val rootView = inflater.inflate(
            R.layout.fragment_gameplay, container,
            false
        )
        instantiateElements(rootView)

        if (arguments != null && arguments!!.getBoolean("ContinueTheGame")) {
            activity?.getPreferences(Context.MODE_PRIVATE).let {
                if (it != null) {
                    currentScore = it.getInt(Consts.SAVED_SCORE_KEY, 0)
                    currentCard = it.getInt(Consts.SAVED_CARD_KEY, -1)
                    deckId = it.getString(Consts.SAVED_DECK_ID, "a")
                        .toString()
                    currentImage = it.getString(Consts.SAVED_IMAGE_KEY, "a")
                        .toString()
                    Log.d(TAG, "current image = $currentImage")
                    Log.d(TAG, "current score = $currentScore")
                    Log.d(TAG, "current card = $currentCard")
                    Log.d(TAG, "deck id = $deckId")
                }
            }
            val imgView = rootView?.findViewById<ImageView>(R.id.ivCurrentCard)
            val tvScore = rootView.findViewById<TextView>(R.id.tvScore)
            Picasso.get().load(currentImage).into(imgView)
            tvScore.text = currentScore.toString()
        } else {
            activity?.getPreferences(Context.MODE_PRIVATE)?.let {
                it.edit().clear().commit()
            }
            createNewDeck(rootView)
        }
        root = rootView

        return rootView
    }

    private fun instantiateElements(root: View) {
        val tv = root.findViewById<TextView>(R.id.tvScore)
        tv.text = currentScore.toString()
        val ivDeck = root.findViewById<ImageView>(R.id.ivDeckOfCards)
        ivDeck.setImageResource(R.drawable.back_of_a_card)

        root.findViewById<Button>(R.id.btnHigher).apply {
            setOnClickListener { drawNewCard(true) { a, b -> a > b } }
        }

        root.findViewById<Button>(R.id.btnLower).apply {
            setOnClickListener { drawNewCard(true) { a, b -> a < b } }
        }
    }

    private fun createNewDeck(root: View) {
        val client = OkHttpClient()
        val deckApi = "https://deckofcardsapi.com/api/deck/new/shuffle/?deck_count=1"
        val request = Request.Builder().url(deckApi).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                Log.d(TAG, "REQUEST FAILED! ", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val result = response.body()!!.string()
                    val strRes = Gson().fromJson<ResponseNewDeck>(
                        result,
                        ResponseNewDeck::class.java
                    )

                    deckId = strRes.deckId
                    drawNewCard(false) { _, _ -> true }
                    currentScore = 0
                }
            }
        })
    }

    private fun getDrawCardObservable() =
        Observable.fromCallable {
            val client = OkHttpClient()
            val deckApi = "https://deckofcardsapi.com/api/deck/$deckId/draw/?count=1"
            val request = Request.Builder().url(deckApi).build()

            var cardInfo : CardInfo? = null

            try {
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val result = response.body()!!.string()
                    val strRes = Gson().fromJson<ResponseForNewCard>(
                        result,
                        ResponseForNewCard::class.java
                    )

                    cardInfo = strRes.cards[0]

                    Log.d(TAG, cardInfo.toString())
                }
                else {
                    Log.d(TAG, "Response unsuccessful")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Draw card network request failed")
            }

            cardInfo
        }

    private fun drawNewCard(shouldCompare: Boolean, op: (Int, Int) -> Boolean) {
        disposable?.dispose()
        disposable = getDrawCardObservable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { cardInfo ->
                cardInfo?.apply {
                    currentImage = image
                    returnCard = Consts.indexOf(value)

                    activity?.runOnUiThread {
                        val imgView = root!!.findViewById<ImageView>(R.id.ivCurrentCard)
                        Picasso.get().load(image).into(imgView)
                    }

                    val nextCard = returnCard

                    if (shouldCompare) {
                        if (op(nextCard, currentCard)) {
                            currentCard = nextCard
                            currentScore++

                            activity?.runOnUiThread {
                                val tv = root!!.findViewById<TextView>(R.id.tvScore)
                                tv.text = currentScore.toString()
                            }
                        } else endGame()
                    }
                    currentCard = nextCard
                }
            }
    }

    fun endGame() {
        activity?.getPreferences(Context.MODE_PRIVATE)?.let {
            it.edit().putInt(Consts.SAVED_CARD_KEY, -1).commit()
            it.edit().putInt(Consts.SAVED_SCORE_KEY, 0).commit()
            Log.d(TAG, "sharedPrefs deleted from endGame")
        }
        theGameHasEnded = true
        val bundle = Bundle()
        bundle.putInt(GAME_SCORE_KEY, currentScore)

        activity?.supportFragmentManager?.popBackStack()
        //toDo
        // Save the score if it's in top 10
    }

    override fun onPause() {
        super.onPause()
        if (!theGameHasEnded) {
            activity?.getPreferences(Context.MODE_PRIVATE)?.let {
                with(it.edit()) {
                    putInt(Consts.SAVED_SCORE_KEY, currentScore)
                    putInt(Consts.SAVED_CARD_KEY, currentCard)
                    putString(Consts.SAVED_DECK_ID, deckId)
                    putString(Consts.SAVED_IMAGE_KEY, currentImage)
                    commit()
                }
            }
        } else {
            activity?.getPreferences(Context.MODE_PRIVATE)?.let {
                it.edit().clear().commit()
                Log.d(TAG, "sharedPrefs deleted from endGame")
            }
        }

        Log.d(TAG, "onPause")
    }


    //toDo Put these classes in a separate folder
    data class ResponseNewDeck(
        @field:SerializedName("success")
        val success: Boolean,

        @field:SerializedName("deck_id")
        val deckId: String,

        @field:SerializedName("shuffled")
        val shuffled: Boolean,

        @field:SerializedName("remaining")
        val remaining: Int
    )

    data class ResponseForNewCard(
        @field:SerializedName("cards")
        val cards: Array<CardInfo>,

        @field:SerializedName("remaining")
        val remaining: String,

        @field:SerializedName("deck_id")
        val deckId: String,

        @field:SerializedName("success")
        val success: Boolean
    )

    data class CardInfo(
        @field:SerializedName("images")
        val images: ImageData,

        @field:SerializedName("image")
        val image: String,

        @field:SerializedName("value")
        val value: String,

        @field:SerializedName("code")
        val code: String,

        @field:SerializedName("suit")
        val suit: String
    )

    data class ImageData(

        @field:SerializedName("svg")
        val svg: String,

        @field:SerializedName("png")
        val png: String
    )
}
