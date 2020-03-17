package com.example.cardgame

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.example.cardgame.viewmodels.GameplayViewModel
import com.example.cardgame.viewmodels.ViewModelProviderFactory
import com.example.cardgame.views.GameplayFragment
import com.example.cardgame.views.MainFragment
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

//todo
//    @Inject
//    private var providerFactory : ViewModelProviderFactory? = null
//    private var gameplayViewModel : GameplayViewModel? = null
    private val manager = supportFragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
// todo
//        gameplayViewModel = ViewModelProvider(this)[GameplayViewModel::class.java]

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
