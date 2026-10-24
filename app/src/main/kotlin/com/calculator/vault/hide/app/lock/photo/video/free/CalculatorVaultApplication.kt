package com.calculator.vault.hide.app.lock.photo.video.free

import android.app.Application
import android.os.StrictMode
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import com.calculator.vault.core.media.image.VaultImageLoader
import com.calculator.vault.feature.applock.service.AppLockController
import com.calculator.vault.hide.app.lock.photo.video.free.di.AppModule
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.ksp.generated.module
import timber.log.Timber

class CalculatorVaultApplication : Application(), SingletonImageLoader.Factory {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            // Logs only exist in debug builds; release builds plant no tree and R8 strips the calls.
            Timber.plant(Timber.DebugTree())
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build())
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().detectLeakedClosableObjects().detectActivityLeaks().penaltyLog().build())
        }
        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.INFO else Level.NONE)
            androidContext(this@CalculatorVaultApplication)
            workManagerFactory()
            modules(AppModule().module)
        }
        get<AppLockController>().start()
    }

    /** Coil uses the vault loader everywhere: decrypting fetchers and no disk cache. */
    override fun newImageLoader(context: PlatformContext): ImageLoader = get<VaultImageLoader>().loader
}
