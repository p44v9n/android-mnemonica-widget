package app.mnemonica

import android.app.Activity
import android.os.Bundle

/** Only here so the app can be opened once, which lets the dealer service start. */
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        DealerService.start(this)
    }
}
