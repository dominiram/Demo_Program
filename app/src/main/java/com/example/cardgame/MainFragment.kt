package com.example.cardgame

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class MainFragment : Fragment() {

    private var root: View? = null
    private val DECK_ID_KEY = "current_deck_id_in_the_game"
    private val CURRENT_CARD_KEY = "current_card_in_the_game"
    private val GAME_SCORE_KEY = "current_game_score"
    var isContinueClickable = false
    private val TAG = "Fragment Main"

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "Created!")
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_main, container,
            false)
        instantiateElements(rootView)
        root = rootView

        return rootView
    }

    private fun instantiateElements(rootView : View) {
        val btnContinue = rootView.findViewById<Button>(R.id.btnContinue)
        btnContinue.isClickable = Intent().hasExtra(GAME_SCORE_KEY) &&
           Intent().hasExtra(DECK_ID_KEY) && Intent().hasExtra(CURRENT_CARD_KEY)
    }

    override fun onResume() {
        super.onResume()
        //toDo OVDE SE ENEJBLUJE DUGME

        Log.d(TAG, "onResume")
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
                val btnContinue = root?.findViewById<Button>(R.id.btnContinue)
        btnContinue?.isClickable = true
    }
}