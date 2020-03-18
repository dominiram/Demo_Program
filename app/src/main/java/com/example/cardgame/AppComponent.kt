package com.example.cardgame

import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [ViewModelModule::class, ActivityBindingModule::class,
    AndroidSupportInjectionModule::class])
interface AppComponent : AndroidInjector<DemoApplication> {
    @Component.Builder
    interface Builder{
        fun build():AppComponent
    }

    override fun inject(demoApplication: DemoApplication)
}