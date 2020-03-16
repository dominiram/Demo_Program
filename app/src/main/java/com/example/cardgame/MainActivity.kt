package com.example.cardgame

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_main.*

class MainActivity : AppCompatActivity() {

    private val manager = supportFragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val transaction = manager.beginTransaction()
        val fragment = MainFragment()
        transaction.replace(R.id.fragment_holder, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    fun newGame(view: View) {
        val transaction = manager.beginTransaction()
        val fragment = GameplayFragment().apply {
            val bundle = Bundle()
            bundle.putBoolean("ContinueTheGame", false)
            arguments = bundle
        }
        transaction.replace(R.id.fragment_holder, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    fun continueGame(view: View) {
        val transaction = manager.beginTransaction()
        val fragment = GameplayFragment().apply {
            val bundle = Bundle()
            bundle.putBoolean("ContinueTheGame", true)
            arguments = bundle
        }
        transaction.replace(R.id.fragment_holder, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    fun highScores(view: View) {

    }


}
