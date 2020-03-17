package com.example.cardgame.views

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.cardgame.R
import com.example.cardgame.models.CardInfo
import com.example.cardgame.models.NewCardResponse
import com.example.cardgame.models.NewDeckResponse
import com.example.cardgame.utils.Consts
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.wajahatkarim3.easyflipview.EasyFlipView
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
    private var disposable: Disposable? = null

    companion object {
        const val flipDurationBack = 500
        const val flipDurationFront = 2000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "Created!")
        super.onCreate(savedInstanceState)
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
                    val strRes = Gson().fromJson<NewDeckResponse>(
                        result,
                        NewDeckResponse::class.java
                    )

                    deckId = strRes.deckId
                    drawNewCard(false) { _, _ -> true }
                    currentScore = 0
                }
            }
        })
    }

    private fun endGame() {
        activity?.getPreferences(Context.MODE_PRIVATE)?.let {
            it.edit().putInt(Consts.SAVED_CARD_KEY, -1).commit()
            it.edit().putInt(Consts.SAVED_SCORE_KEY, 0).commit()
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
            }
        }

        Log.d(TAG, "onPause")
    }

    private fun getCardObservable() = Observable.fromCallable {

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

    private fun drawNewCard(shouldCompare: Boolean, op: (Int, Int) -> Boolean) {
        disposable?.dispose()
        disposable = getCardObservable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { cardInfo ->

                cardInfo?.apply {

                    currentImage = image
                    returnCard = Consts.indexOf(value)

                    activity?.runOnUiThread {

                        val imgBack = root!!.findViewById<ImageView>(R.id.ivCurrentCardBack)
                        val imgView = root!!.findViewById<ImageView>(R.id.ivCurrentCard)
                        val easyFlipView = root!!.findViewById<EasyFlipView>(R.id.easyFlipView)

                        Picasso.get().load(R.drawable.back_of_a_card).into(imgBack)

                        easyFlipView.flipDuration = flipDurationBack
                        easyFlipView.setFlipTypeFromLeft()
                        easyFlipView.flipTheView(true)

                        easyFlipView.flipDuration = flipDurationFront
                        easyFlipView.setFlipTypeFromLeft()
                        easyFlipView.flipTheView(true)

                        Picasso.get().load(image).into(imgView)

                        val tv = root!!.findViewById<TextView>(R.id.tvScore)
                        tv.text = currentScore.toString()
                    }

                    val nextCard = returnCard
                    if (shouldCompare) {
                        if (cmpAndLog(currentCard, nextCard, op(nextCard, currentCard))) {
                            currentCard = nextCard
                            currentScore++
                            activity?.runOnUiThread {
                                val tvScore = root!!.findViewById<TextView>(R.id.tvScore)
                                tvScore.text = currentScore.toString()
                            }
                        } else
                            endGame()
                    }
                    currentCard = nextCard
                }
            }
    }

    private fun cmpAndLog(currentCard: Int, nextCard: Int, op: Boolean) : Boolean {
        Log.d(TAG, "current card = ${Consts.getName(currentCard)}")
        Log.d(TAG, "next card = ${Consts.getName(nextCard)}")
        return if(op) {
            Log.d(TAG, "comparison is  a success")
            true
        } else {
            Log.d(TAG, "comparison is  a failure")
            false
        }
    }
}