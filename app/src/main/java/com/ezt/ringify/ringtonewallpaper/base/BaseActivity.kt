package com.ezt.ringify.ringtonewallpaper.base

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.viewbinding.ViewBinding
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.screen.home.MainActivity
import com.ezt.ringify.ringtonewallpaper.utils.Common
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

typealias Inflate<T> = (LayoutInflater) -> T

@Suppress("DEPRECATION")
abstract class BaseActivity<T : ViewBinding>(private val inflater: Inflate<T>) :
    AppCompatActivity() {
    protected val binding: T by lazy { inflater(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        val isDarkMode = false
        val flag: Int
        println("isDarkMode $isDarkMode")
        Common.setLocale(this@BaseActivity, Common.getPreLanguage(this))
        if (isDarkMode) {
            setTheme(R.style.Theme_Ringtone)
            flag =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        } else {
            setTheme(R.style.Theme_Ringtone)
            flag =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        // Force light theme
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Initialize lastKnownUiMode
        lastKnownUiMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility = flag

        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if ((visibility and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
                CoroutineScope(Dispatchers.Main).launch {
                    delay(1000)
                    window.decorView.systemUiVisibility = (
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            )

                    val isInDarkMode = false
                    val fl = if (isInDarkMode) {
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    } else {
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    }
                    window.decorView.systemUiVisibility = fl
                }
            }
        }

        window.navigationBarColor = Color.TRANSPARENT
        window.statusBarColor = Color.TRANSPARENT
        setContentView(binding.root)
    }


    open fun openFragment(fragment: Fragment) {
        val manager: FragmentManager = supportFragmentManager
        val transaction: FragmentTransaction = manager.beginTransaction()
        transaction.replace(R.id.frameLayout,fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val systemUiMode = newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val oldUiMode = 0
        Log.d(TAG, "System UI mode changed from $oldUiMode to $systemUiMode")

        if (oldUiMode != systemUiMode) {
            // Save new value
            lastKnownUiMode = systemUiMode

            // Restart app or move to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }
    }


    companion object {
        private var TAG = BaseActivity::class.java.simpleName
        var lastKnownUiMode = Configuration.UI_MODE_NIGHT_NO // default light mode
    }
}