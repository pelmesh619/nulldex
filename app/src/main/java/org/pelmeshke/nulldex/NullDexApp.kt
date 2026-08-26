package org.pelmeshke.nulldex

import android.app.Application
import org.pelmeshke.nulldex.data.api.RetrofitInstance

class NullDexApp : Application() {
    override fun onCreate() {
        super.onCreate()
        RetrofitInstance.init(this)
    }
}
