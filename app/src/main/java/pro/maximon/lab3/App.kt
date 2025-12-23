package pro.maximon.lab3

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.*
import org.koin.core.context.startKoin
import org.koin.dsl.module
import pro.maximon.lab3.data.local.AppDatabase
import pro.maximon.lab3.data.repository.TimerRepository
import pro.maximon.lab3.data.repository.TimerRepositoryImpl
import pro.maximon.lab3.viewmodels.MainViewModel

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        val appModule = module {
            single { AppDatabase.getInstance(get()) }
            single { get<AppDatabase>().timerDao() }
            single<TimerRepository> { TimerRepositoryImpl(get()) }

            viewModel { MainViewModel(get()) }
        }

        startKoin {
            androidContext(this@App)
            modules(appModule)
        }
    }
}