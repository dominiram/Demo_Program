package com.example.cardgame

import com.example.cardgame.views.GameplayFragment
import com.example.cardgame.views.MainFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBindingModule {
    @ContributesAndroidInjector(modules = [FragmentBindingModule::class])
    internal abstract fun mainActivity() : MainActivity
}

@Module
abstract class FragmentBindingModule {
    @ContributesAndroidInjector
    internal abstract fun gamePlayFragment() : GameplayFragment

    @ContributesAndroidInjector
    internal abstract fun mainFragment() : MainFragment
}
