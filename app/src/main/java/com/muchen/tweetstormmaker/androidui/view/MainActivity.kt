package com.muchen.tweetstormmaker.androidui.view

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.navigateUp
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import com.muchen.tweetstormandroid.R
import com.muchen.tweetstormandroid.databinding.ActivityMainBinding
import com.muchen.tweetstormandroid.databinding.HeaderNavDrawerBinding
import com.muchen.tweetstormandroid.NavGraphDirections
import com.muchen.tweetstormmaker.androidui.AndroidUIConstants.HDPI
import com.muchen.tweetstormmaker.androidui.AndroidUIConstants.PROFILE_IMG_DIMEN
import com.muchen.tweetstormmaker.androidui.AndroidUIConstants.PROFILE_IMG_FILE_NAME
import com.muchen.tweetstormmaker.androidui.TweetstormMakerApplication
import com.muchen.tweetstormmaker.androidui.di.ActivityComponent
import com.muchen.tweetstormmaker.androidui.model.NotificationEnum
import com.muchen.tweetstormmaker.androidui.model.NotificationEnum.*
import com.muchen.tweetstormmaker.androidui.model.TwitterUserAndTokens
import com.muchen.tweetstormmaker.androidui.toAccessTokens
import com.muchen.tweetstormmaker.androidui.viewmodel.DraftsViewModel
import com.muchen.tweetstormmaker.androidui.viewmodel.TwitterViewModel
import com.muchen.tweetstormmaker.androidui.viewmodel.factory.ActivityViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    internal lateinit var activityViewModelFactory: ActivityViewModelFactory

    internal lateinit var activityComponent: ActivityComponent

    internal val twitterApiViewModel by viewModels<TwitterViewModel> { activityViewModelFactory }

    internal val draftsViewModel by viewModels<DraftsViewModel> { activityViewModelFactory }

    internal val hasInternetAccess by lazy {
        // Activity.getApplication() returns null at class instance creation time
        (application as TweetstormMakerApplication).hasInternetAccess
    }

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val drawerHeaderBinding by lazy {
        HeaderNavDrawerBinding.inflate(layoutInflater)
    }

    private val navController by lazy {
        // Navigation.findNavController(Activity, int) fails in onCreate()
        (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
    }

    private val profileImgScaledLength by lazy {
        // AppCompatActivity.getResources() returns null at class instance creation time
        PROFILE_IMG_DIMEN * resources.configuration.densityDpi / HDPI
    }

    private val topLevelDestinationIds = setOf(R.id.local_list_fragment, R.id.partially_sent_list_fragment, R.id.sent_list_fragment)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityComponent = (application as TweetstormMakerApplication).applicationComponent
                .activityComponentBuilder
                .apiKey(getString(R.string.API_KEY))
                .apiKeySecret(getString(R.string.API_KEY_SECRET))
                .build()
        activityComponent.inject(this)
        setContentView(binding.root)
        setupNavDrawer()
        setupActionBar()
        setupDataObservers()
    }

    override fun onStart() {
        super.onStart()
        if (twitterApiViewModel.twitterUserAndTokens.value != null &&
            hasInternetAccess.value == true) {
            twitterApiViewModel.refreshTwitterUser()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_tool_bar, menu)
        return true
    }

    /**
     * Override [onSupportNavigateUp] so that pressing the nav button (either showing the nav
     * drawer the Up icon) opens the nav drawer or navigates up.
     */
    override fun onSupportNavigateUp(): Boolean {
        /* topLevelDestinationsIds is supplied so that the nav drawer opens when clicking on the
           nav button at the specified destination(s). Otherwise, the drawer would would only
           open at the start destination while the nav button would show the drawer icon at all
           three logical top level destinations of the nav graph.*/
        return navigateUp(navController, AppBarConfiguration(topLevelDestinationIds, binding.drawerLayout))
    }

    /**
     * Override [onBackPressed] so that nav drawer closes when back button is pressed, which is not
     * the default behavior.
     */
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun setupNavDrawer() {
        binding.navView.apply {
            addHeaderView(drawerHeaderBinding.root)
            setupWithNavController(navController)
            setNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.drawer_item_login -> twitterApiViewModel.startLogin()
                    R.id.drawer_item_logout -> twitterApiViewModel.logout()
                    else -> it.onNavDestinationSelected(navController)
                }
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                true
            }
        }
    }

    private fun setupActionBar() {
        binding.toolbar.apply {
            setupWithNavController(navController, AppBarConfiguration(topLevelDestinationIds, binding.drawerLayout))
            setSupportActionBar(this)
        }
    }

    private fun setupDataObservers() {
        twitterApiViewModel.apply{
            authorizationUrl.observe(this@MainActivity) {
                Log.d(TAG, "authorizationUrl changed, $it")
                onAuthorizationUrlChanged(it)
                if (it != null) authorizationUrl.value = null
            }
            showNotification.observe(this@MainActivity) {
                Log.d(TAG, "showNotification changed, $it")
                onShowNotificationChanged(it)
                if (it != null) showNotification.value = null
            }
            showProgressIndicator.observe(this@MainActivity) {
                Log.d(TAG, "showProgressIndicator changed, $it")
                onShowProgressIndicatorChanged(it)
            }
            twitterUserAndTokens.observe(this@MainActivity) {
                Log.d(TAG, "twitterUserAndTokens changed, $it")
                onTwitterUserAndTokensChanged(it)
            }
        }

        hasInternetAccess.observe(this@MainActivity) {
            Log.d(TAG, "hasInternetAccess changed, $it")
            onInternetAccessChanged(it)
        }
    }

    private fun onAuthorizationUrlChanged(url: String?) {
        if (url != null)
            navController.navigate(NavGraphDirections.actionToRedirectDialogFragment(url))
    }

    private fun onShowNotificationChanged(notificationEnum: NotificationEnum?) {
        if (notificationEnum != null) {
            val text = when (notificationEnum) {
                LOGIN_SUCCESSFUL -> getString(R.string.notification_message_login_successful)
                LOGIN_FAILED -> getString(R.string.notification_message_login_failed)
                SEND_TWEETSTORM_SUCCESSFUL -> getString(R.string.notification_message_send_tweetstorm_successful)
                SEND_TWEETSTORM_FAILED_SOME_TWEETS_SENT -> getString(R.string.notification_message_send_tweetstorm_failed_some_tweets_sent)
                SEND_TWEETSTORM_FAILED_NO_TWEET_SENT -> getString(R.string.notification_message_send_tweetstorm_failed_no_tweet_sent)
                UNSEND_TWEETSTORM_SUCCESSFUL -> getString(R.string.notification_message_unsend_tweetstorm_successful)
                UNSEND_TWEETSTORM_FAILED_SOME_TWEETS_UNSENT -> getString(R.string.notification_message_unsend_tweetstorm_failed_some_tweets_unsent)
                UNSEND_TWEETSTORM_FAILED_NO_TWEET_UNSENT -> getString(R.string.notification_message_unsend_tweetstorm_failed_no_tweet_unsent)
                UNSEND_TWEETSTORMS_SUCCESSFUL -> getString(R.string.notification_message_unsend_tweetstorms_successful)
                UNSEND_TWEETSTORMS_FAILED_SOME_TWEETSTORMS_UNSENT -> getString(R.string.notification_message_unsend_tweetstorms_failed_some_tweetstorms_unsent)
                UNSEND_TWEETSTORMS_FAILED_NO_TWEETSTORM_UNSENT -> getString(R.string.notification_message_unsend_tweetstorms_failed_no_tweetstorm_unsent)
                UNSEND_TWEETSTORMS_FAILED_NO_TWEET_UNSENT -> getString(R.string.notification_message_unsend_tweetstorms_failed_no_tweet_unsent)
            }
            Snackbar.make(this, binding.root, text, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun onShowProgressIndicatorChanged(show: Boolean) {
        if (show) {
            when (navController.currentDestination?.id ) {
                R.id.local_edit_fragment, R.id.non_local_edit_fragment -> navController.popBackStack()
            }
            binding.progressIndicator.isVisible = true
        } else {
            binding.progressIndicator.isVisible = false
        }
    }

    private fun onTwitterUserAndTokensChanged(data: TwitterUserAndTokens?) {
        if (data != null) {
            twitterApiViewModel.updateTokensWith(data.toAccessTokens())
            setLoginVisible(false)
            drawerHeaderBinding.textViewTwitterName.text = data.name
            drawerHeaderBinding.textViewTwitterHandle.text = "@${data.screenName}"
            try {
                useCachedProfileImage()
            } catch (e: FileNotFoundException) {
                downloadProfileImage(data.profileImageURLHttps)
            }
        } else {
            setLoginVisible(true)
            drawerHeaderBinding.textViewTwitterName.text = getString(R.string.default_name)
            drawerHeaderBinding.textViewTwitterHandle.text = getString(R.string.default_twitter_handle)
            drawerHeaderBinding.imageViewProfileImage.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.default_profile_normal))
        }
    }

    private fun onInternetAccessChanged(hasAccess: Boolean) {
        setLoginEnabled(enabled = hasAccess)
    }

    private fun setLoginVisible(visible: Boolean) {
        binding.navView.menu.apply{
            findItem(R.id.drawer_item_login).isVisible = visible
            findItem(R.id.drawer_item_logout).isVisible = !visible
        }
    }

    private fun setLoginEnabled(enabled: Boolean) {
        binding.navView.menu.apply{
            findItem(R.id.drawer_item_login).isEnabled = enabled
        }
    }

    @Throws(FileNotFoundException::class)
    private fun useCachedProfileImage() {
        val drawable = Drawable.createFromStream(openFileInput(PROFILE_IMG_FILE_NAME), "")
        drawerHeaderBinding.imageViewProfileImage.setImageDrawable(
            drawable?.toBitmap(profileImgScaledLength, profileImgScaledLength)?.toDrawable(resources))
    }

    private fun downloadProfileImage(url: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val inputStream = BufferedInputStream(URL(url).openStream())
                val drawable = Drawable.createFromStream(inputStream, "")
                inputStream.close()

                withContext(Dispatchers.Main) {
                    drawerHeaderBinding.imageViewProfileImage.setImageDrawable(
                        drawable?.toBitmap(profileImgScaledLength, profileImgScaledLength)?.toDrawable(resources))
                }

                val outputStream = openFileOutput(PROFILE_IMG_FILE_NAME, MODE_PRIVATE)
                drawable?.toBitmap()?.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()
            } catch (e: Exception) {
                when(e) {
                    is MalformedURLException -> Log.e(TAG, "MalformedURLException: $url")
                    is IOException -> Log.e(TAG, "IOException: ${e.message}")
                    else -> Log.e(TAG, "exception: $e")
                }
            }
        }
    }

    companion object {
        const val TAG = "MainActivity"
    }
}