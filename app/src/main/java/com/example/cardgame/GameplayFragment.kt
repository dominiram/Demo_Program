package com.example.cardgame

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Display
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.IntegerRes
import androidx.core.text.parseAsHtml
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_gameplay.*
import okhttp3.*
import java.io.IOException
import kotlin.math.log

/**
 * A simple [Fragment] subclass.
 * Use the [GameplayFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GameplayFragment : Fragment() {

    private var theGameHasEnded = false
    private val DECK_ID_KEY = "current_deck_id_in_the_game"
    private val CURRENT_CARD_KEY = "current_card_in_the_game"
    private val GAME_SCORE_KEY = "current_game_score"
    private var currentImage = ""
    private var returnCard = -1
    private var root: View? = null
    private val cardValues = arrayOf("2","3","4","5","6","7","8","9","10","JACK",
        "QUEEN", "KING", "ACE")
    private var deckId = ""
    private var currentScore = 0
    private var currentCard = -1
    private val TAG = "Fragment gameplay"

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "Created!")
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        theGameHasEnded = false
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_gameplay, container,
            false)
        instantiateElements(rootView)

        if(arguments != null && arguments!!.getBoolean("ContinueTheGame")) {
            activity?.getPreferences(Context.MODE_PRIVATE).let {
                if (it != null) {
                    currentScore = it.getInt(getString(R.string.saved_score_key), 0)
                    currentCard = it.getInt(getString(R.string.saved_card_key), -1)
                    deckId = it.getString(getString(R.string.saved_deck_id_key), "a")
                        .toString()
                    currentImage = it.getString(getString(R.string.saved_image_key), "a")
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
                //returnCard = cardValues.indexOf(cardValue)
        }
        else {
            activity?.getPreferences(Context.MODE_PRIVATE)?.let {
                it.edit().clear().commit()
            }
            createNewDeck(rootView)
        }
        root = rootView

        return rootView
    }

    private fun instantiateElements(root : View) {
// toDo: da li sme ove da se instancira zbog resume-a koji ce ponovo da pokrene onCreate?
//        currentScore = 0
        val tv = root.findViewById<TextView>(R.id.tvScore)
        tv.text = currentScore.toString()
        val ivCard = root.findViewById<ImageView>(R.id.ivCurrentCard)
        val ivDeck = root.findViewById<ImageView>(R.id.ivDeckOfCards)
        ivDeck.setImageResource(R.drawable.back_of_a_card)

        val btnH = root.findViewById<Button>(R.id.btnHigher)
        btnH.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                higherCard(root)
            }
        })

        val btnL = root.findViewById<Button>(R.id.btnLower)
        btnL.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                lowerCard(root)
            }
        })
    }

    private fun createNewDeck(root: View) {
        val client = OkHttpClient()
        val deckApi = "https://deckofcardsapi.com/api/deck/new/shuffle/?deck_count=1"
        val request = Request.Builder().url(deckApi).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                Log.d(TAG, "REQUEST FAILED! " + e.printStackTrace())
            }

            override fun onResponse(call: Call, response: Response) {
                if(response.isSuccessful) {
                    val result = response.body()!!.string()
                    val strRes = Gson().fromJson<ResponseNewDeck>(result,
                        ResponseNewDeck::class.java)

                    deckId = strRes.deckId
                    drawNewCard(deckId, root)
                    currentScore = 0
                }
            }
        })
    }

    fun lowerCard(btn : View) {
        val client = OkHttpClient()
        val deckApi = "https://deckofcardsapi.com/api/deck/$deckId/draw/?count=1"
        val request = Request.Builder().url(deckApi).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                Log.d(TAG, "REQUEST FOR NEW CARD FAILED!" + e.printStackTrace())
            }

            override fun onResponse(call: Call, response: Response) {
                if(response.isSuccessful) {
                    val result = response.body()!!.string()
                    //Log.d(TAG, "result = "+result)
                    val strRes = Gson().fromJson<ResponseForNewCard>(result,
                        ResponseForNewCard::class.java)
                    val image = strRes.cards[0].image
                    currentImage = image
                    val cardValue = strRes.cards[0].value
                    returnCard = cardValues.indexOf(cardValue)
                    //Log.d(TAG, "index of card value = $returnCard")

                    val nextCard = returnCard
                    Log.d(TAG, "${cardValues[nextCard]} < ${cardValues[currentCard]}")
                    if(nextCard < currentCard) {
                        currentCard = nextCard
                        currentScore++

                        activity?.runOnUiThread {
                            val imgView = root!!.findViewById<ImageView>(R.id.ivCurrentCard)
                            Picasso.get().load(image).into(imgView)
                            val tv = root!!.findViewById<TextView>(R.id.tvScore)
                            tv.text = currentScore.toString()
                            //returnCard = cardValues.indexOf(cardValue)
                        }
                    }
                    else endGame()
                    currentCard = nextCard
                }
                else {
                    Log.d(TAG, "RESPONSE FOR NEW CARD IS UNSUCCESSFUL")
                }
            }
        })

    }

    fun higherCard(btn : View) {
        val client = OkHttpClient()
        val deckApi = "https://deckofcardsapi.com/api/deck/$deckId/draw/?count=1"
        val request = Request.Builder().url(deckApi).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                Log.d(TAG, "REQUEST FOR NEW CARD FAILED!" + e.printStackTrace())
            }

            override fun onResponse(call: Call, response: Response) {
                if(response.isSuccessful) {
                    val result = response.body()!!.string()
                    //Log.d(TAG, "result = "+result)
                    val strRes = Gson().fromJson<ResponseForNewCard>(result,
                        ResponseForNewCard::class.java)
                    val image = strRes.cards[0].image
                    currentImage = image
                    val cardValue = strRes.cards[0].value
                    returnCard = cardValues.indexOf(cardValue)
                    //Log.d(TAG, "index of card value = $returnCard")

                    val nextCard = returnCard
                    Log.d(TAG, "${cardValues[nextCard]} > ${cardValues[currentCard]}")
                    if(nextCard > currentCard) {
                        currentCard = nextCard
                        currentScore++

                        activity?.runOnUiThread {
                            val imgView = root!!.findViewById<ImageView>(R.id.ivCurrentCard)
                            Picasso.get().load(image).into(imgView)
                            val tv = root!!.findViewById<TextView>(R.id.tvScore)
                            tv.text = currentScore.toString()
                            //returnCard = cardValues.indexOf(cardValue)
                        }
                    }
                    else endGame()

                    currentCard = nextCard

                }
                else {
                    Log.d(TAG, "RESPONSE FOR NEW CARD IS UNSUCCESSFUL")
                }
            }
        })
    }

    //toDo izbrisi ovu metodu
    private fun callApiForNextCard(root: View): Int {
        val client = OkHttpClient()
        val deckApi = "https://deckofcardsapi.com/api/deck/$deckId/draw/?count=1"
        val request = Request.Builder().url(deckApi).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                Log.d(TAG, "REQUEST FOR NEW CARD FAILED!" + e.printStackTrace())
            }

            override fun onResponse(call: Call, response: Response) {
                if(response.isSuccessful) {
                    val result = response.body()!!.string()
                    //Log.d(TAG, "result = "+result)
                    val strRes = Gson().fromJson<ResponseForNewCard>(result,
                        ResponseForNewCard::class.java)
                    val image = strRes.cards[0].image
                    val cardValue = strRes.cards[0].value
                    returnCard = cardValues.indexOf(cardValue)
                    //Log.d(TAG, "index of card value = $returnCard")
                    activity?.runOnUiThread {
                        val imgView = root.findViewById<ImageView>(R.id.ivCurrentCard)
                        Picasso.get().load(image).into(imgView)
                        //returnCard = cardValues.indexOf(cardValue)
                    }
                }
                else {
                    Log.d(TAG, "RESPONSE FOR NEW CARD IS UNSUCCESSFUL")
                }
            }
        })
        return returnCard
    }

    fun endGame() {
        activity?.getPreferences(Context.MODE_PRIVATE)?.let {
            it.edit().putInt(getString(R.string.saved_card_key), -1)
            it.edit().putInt(getString(R.string.saved_score_key), 0)
            it.edit().commit()
//            it.edit().clear().commit()
            Log.d(TAG, "sharedPrefs deleted from endGame")
        }
        theGameHasEnded = true
        val bundle = Bundle()
        bundle.putInt(GAME_SCORE_KEY, currentScore)

        activity?.supportFragmentManager?.popBackStack()
        //toDo
        // Ako je u top 10 skorova sacuvaj ga!
    }

    override fun onPause() {
        super.onPause()
        if(!theGameHasEnded) {
            activity?.getPreferences(Context.MODE_PRIVATE)?.let {
                with (it.edit()) {
                    putInt(getString(R.string.saved_score_key), currentScore)
                    putInt(getString(R.string.saved_card_key), currentCard)
                    putString(getString(R.string.saved_deck_id_key), deckId)
                    putString(getString(R.string.saved_image_key), currentImage)
                    commit()
                }
            }
        }
        else {
            activity?.getPreferences(Context.MODE_PRIVATE)?.let {
//                it.edit().putInt(getString(R.string.saved_card_key), -1)
//                it.edit().putInt(getString(R.string.saved_score_key), 0)
//                it.edit().commit()
            it.edit().clear().commit()
                Log.d(TAG, "sharedPrefs deleted from endGame")
            }
        }

        Log.d(TAG, "onPause")
    }

    //toDO
    // zapamti rezultat kartu i deckId u bundle-u u onDestroy metodi





    //toDo STAVI OVE KLASE U MODEL FOLDER
    data class ResponseNewDeck (
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



    private fun drawNewCard(id: String, root: View) {
        val client = OkHttpClient()
        val deckApi = "https://deckofcardsapi.com/api/deck/$id/draw/?count=1"
        val request = Request.Builder().url(deckApi).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                Log.d(TAG, "REQUEST FOR NEW CARD FAILED!" + e.printStackTrace())
            }

            override fun onResponse(call: Call, response: Response) {
                if(response.isSuccessful) {
                    val result = response.body()!!.string()
                    val strRes = Gson().fromJson<ResponseForNewCard>(result,
                        ResponseForNewCard::class.java)
                    val image = strRes.cards[0].image
                    currentImage = image
                    val cardValue = strRes.cards[0].value
                    currentCard = cardValues.indexOf(cardValue)
                    activity?.runOnUiThread(object: Runnable {
                        override fun run() {
                            val imgView = root.findViewById<ImageView>(R.id.ivCurrentCard)
                            Picasso.get().load(image).into(imgView)
                        }
                    })
                }
                else {
                    Log.d(TAG, "RESPONSE FOR NEW CARD IS UNSUCCESSFUL")
                }
            }
        })
    }
}
