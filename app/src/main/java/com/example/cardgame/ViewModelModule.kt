package com.example.cardgame

import androidx.lifecycle.ViewModel
import com.example.cardgame.viewmodels.GameplayViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
@Suppress("UNUSED")
abstract class ViewModelModule {

    @IntoMap
    @Binds
    @ViewModelKey(GameplayViewModel::class)
    abstract fun bindViewModelFactory(gameplayViewModel: GameplayViewModel): ViewModel
}
