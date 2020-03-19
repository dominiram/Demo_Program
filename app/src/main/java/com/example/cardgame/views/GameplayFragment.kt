package com.example.cardgame.views

import android.annotation.SuppressLint
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
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.example.cardgame.R
import com.example.cardgame.ViewModelFactory
import com.example.cardgame.models.CardInfo
import com.example.cardgame.models.NewCardResponse
import com.example.cardgame.models.NewDeckResponse
import com.example.cardgame.utils.Consts
import com.example.cardgame.viewmodels.GameplayViewModel
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.wajahatkarim3.easyflipview.EasyFlipView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.*
import java.io.IOException
import java.lang.Exception
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 * Use the [GameplayFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GameplayFragment : DaggerFragment() {

    private val GAME_SCORE_KEY = "current_game_score"
    private var theGameHasEnded = false
    private var currentImage = ""
    private var returnCard = -1
    private var root: View? = null
    private var deckId = ""
    private var currentScore = 0
    private var currentCard = -1
    private val TAG = "Fragment gameplay"
    @Inject
    lateinit var factory: ViewModelFactory

    private val viewModel by lazy {
        @Suppress("DEPRECATION")
        ViewModelProviders.of(this, factory)
            .get(GameplayViewModel::class.java)
    }

    companion object {
        const val flipDurationBack = 500
        const val flipDurationFront = 2000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "Created!")
        super.onCreate(savedInstanceState)
    }

    @Suppress("DEPRECATION")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "VIEW CREATED!")
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

                    Log.d(TAG, "currentScore = $currentScore, currentCard = $currentCard," +
                            "currentImage = $currentImage, deckId = $deckId")

                    //todo value se ne upisuje lepo u card?
                    viewModel.setCurrentScore(currentScore)
                    viewModel.deckId = deckId
                    viewModel.setCardImage(currentImage)
                    viewModel.setCardValue(currentCard)
                    Log.d(TAG, "card value = ${viewModel.cardGetter.value?.value}")
                }
            }
            val imgView = rootView?.findViewById<ImageView>(R.id.ivCurrentCard)
            val tvScore = rootView.findViewById<TextView>(R.id.tvScore)
            Picasso.get().load(currentImage).into(imgView)
            tvScore.text = viewModel.currentScoreGetter.toString()

        } else {
            activity?.getPreferences(Context.MODE_PRIVATE)?.let {
                viewModel.drawNewCard(false){ _, _ -> true }
                it.edit().clear().commit()
            }
        }
        root = rootView

        return rootView
    }

    private fun instantiateElements(root: View) {
        val tv = root.findViewById<TextView>(R.id.tvScore)
        tv.text = currentScore.toString()
        val ivDeck = root.findViewById<ImageView>(R.id.ivDeckOfCards)
        ivDeck.setImageResource(R.drawable.back_of_a_card)

        viewModel.cardGetter.observe(viewLifecycleOwner, Observer {
            card ->
            run {
                currentImage = card.image
                returnCard = Consts.indexOf(card.value)

                activity?.runOnUiThread {
                    val imgBack = root.findViewById<ImageView>(R.id.ivCurrentCardBack)
                    val imgView = root.findViewById<ImageView>(R.id.ivCurrentCard)
                    val easyFlipView = root.findViewById<EasyFlipView>(R.id.easyFlipView)

                    Picasso.get().load(R.drawable.back_of_a_card).into(imgBack)

                    easyFlipView.flipDuration = flipDurationBack
                    easyFlipView.setFlipTypeFromLeft()
                    easyFlipView.flipTheView(true)

                    easyFlipView.flipDuration = flipDurationFront
                    easyFlipView.setFlipTypeFromLeft()
                    easyFlipView.flipTheView(true)

                    Picasso.get().load(currentImage).into(imgView)
                }
            }
        })

        viewModel.gameHasEndedGetter.observe(viewLifecycleOwner, Observer {
            endGame()
        })

        viewModel.currentScoreGetter.observe(viewLifecycleOwner, Observer {
            val tv = root.findViewById<TextView>(R.id.tvScore)
            Log.d(TAG, "current score = ${viewModel.currentScoreGetter.value}")
            tv.text = viewModel.currentScoreGetter.value.toString()
        })

        root.findViewById<Button>(R.id.btnHigher).apply {
            setOnClickListener { viewModel.drawNewCard(true) { a, b -> a > b } }
        }

        root.findViewById<Button>(R.id.btnLower).apply {
            setOnClickListener { viewModel.drawNewCard(true) { a, b -> a < b } }
        }
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
                    currentScore = viewModel.currentScoreGetter.value!!
                    currentCard = Consts.indexOf(viewModel.cardGetter.value!!.value)
                    deckId = viewModel.deckId
                    currentImage = viewModel.cardGetter.value!!.image
                    val suite = viewModel.cardGetter.value!!.suit
                    val code = viewModel.cardGetter.value!!.code
                    val imagespng = viewModel.cardGetter.value!!.images.png
                    val imagessvg = viewModel.cardGetter.value!!.images.svg

                    putString("cardInfo1", suite)
                    putString("cardInfo2", code)
                    putString("cardInfo3", imagespng)
                    putString("cardInfo4", imagessvg)
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
}

