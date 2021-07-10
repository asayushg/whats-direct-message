package saini.ayush.whatsdirectmessage.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Singleton
    @Provides
    fun provideApplicationContext(app: Application): Context {
        return app.applicationContext
    }

    @Data
    @Provides
    @Singleton
    fun provideAccountPreference(context: Context): SharedPreferences {
        return context.getSharedPreferences("account", Context.MODE_PRIVATE)
    }

    @Data
    @Provides
    @Singleton
    fun provideAccountPreferenceEditor(@Data pref: SharedPreferences): SharedPreferences.Editor {
        return pref.edit()
    }

}