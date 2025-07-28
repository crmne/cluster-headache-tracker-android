package me.paolino.clusterheadachetracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.graphics.Color
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import dev.hotwire.core.config.Hotwire
import dev.hotwire.navigation.activities.HotwireActivity
import dev.hotwire.navigation.navigator.NavigatorConfiguration
import dev.hotwire.navigation.tabs.HotwireBottomNavigationController
import dev.hotwire.navigation.tabs.navigatorConfigurations
import dev.hotwire.core.turbo.visit.VisitOptions
import me.paolino.clusterheadachetracker.util.AuthEvents
import me.paolino.clusterheadachetracker.util.NEW_HEADACHE_LOG_URL
import me.paolino.clusterheadachetracker.util.SIGN_IN_URL

class MainActivity : HotwireActivity() {
    private lateinit var bottomNavigationController: HotwireBottomNavigationController
    
    companion object {
        const val TAG = "MainActivity"
        const val NEW_TAB_INDEX = 2 // Index of the "New" tab
    }
    
    private val activity: HotwireActivity
        get() = this@MainActivity
    
    private val authReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                AuthEvents.SIGN_OUT_REQUESTED -> handleSignOut()
                AuthEvents.AUTHENTICATION_CHANGED -> refreshAllTabs()
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        configureAppearance()
        initializeBottomTabs()
        configureEdgeToEdge()
        registerReceivers()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(authReceiver)
    }

    private fun configureAppearance() {
        // Primary color from Rails app (#4f46e5)
        val primaryColor = Color.parseColor("#4f46e5")
        
        // Configure action bar
        supportActionBar?.apply {
            setBackgroundDrawable(android.graphics.drawable.ColorDrawable(primaryColor))
        }
        
        // Configure status bar color
        window.statusBarColor = primaryColor
    }

    private fun initializeBottomTabs() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav)
        
        // Style the bottom navigation
        bottomNavigationView.itemIconTintList = getColorStateList(R.color.bottom_nav_color)
        bottomNavigationView.itemTextColor = getColorStateList(R.color.bottom_nav_color)
        
        bottomNavigationController = HotwireBottomNavigationController(this, bottomNavigationView)
        bottomNavigationController.load(mainTabs, 0)
        
        // Handle special "New" tab behavior like iOS
        bottomNavigationController.setOnTabSelectedListener { index, tab ->
            if (index == NEW_TAB_INDEX) {
                // Don't actually switch to the "New" tab
                // Instead, navigate to new headache log on the current tab
                val currentNavigator = activity.delegate.currentNavigator
                currentNavigator?.route(
                    location = NEW_HEADACHE_LOG_URL, 
                    options = VisitOptions(),
                    bundle = Bundle().apply {
                        putString("presentation", "modal")
                    }
                )
                
                // Return to the previous tab - stay on current tab
                false // Don't select the "New" tab
            } else {
                true // Allow normal tab selection
            }
        }
    }
    
    override fun navigatorConfigurations() = mainTabs.navigatorConfigurations
    
    private fun configureEdgeToEdge() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav)
        
        // Apply window insets to bottom navigation
        ViewCompat.setOnApplyWindowInsetsListener(bottomNavigationView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                view.paddingLeft,
                view.paddingTop,
                view.paddingRight,
                systemBars.bottom
            )
            insets
        }
    }
    
    private fun registerReceivers() {
        val intentFilter = IntentFilter().apply {
            addAction(AuthEvents.SIGN_OUT_REQUESTED)
            addAction(AuthEvents.AUTHENTICATION_CHANGED)
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(authReceiver, intentFilter)
    }
    
    private fun handleSignOut() {
        Log.d(TAG, "Handling sign out")
        
        // Clear all navigators and refresh tabs
        mainTabs.forEach { tab ->
            val navigatorHost = supportFragmentManager.findFragmentById(tab.configuration.navigatorHostId) as? dev.hotwire.navigation.navigator.NavigatorHost
            navigatorHost?.navigator?.clearAll()
        }
        
        // Reset to first tab
        bottomNavigationController.selectTab(0)
        
        // Navigate to sign in
        activity.delegate.currentNavigator?.route(SIGN_IN_URL)
    }
    
    fun refreshAllTabs() {
        Log.d(TAG, "Refreshing all tabs")
        
        mainTabs.forEach { tab ->
            val navigatorHost = supportFragmentManager.findFragmentById(tab.configuration.navigatorHostId) as? dev.hotwire.navigation.navigator.NavigatorHost
            navigatorHost?.navigator?.currentDestination?.refresh(displayProgress = false)
        }
    }
}