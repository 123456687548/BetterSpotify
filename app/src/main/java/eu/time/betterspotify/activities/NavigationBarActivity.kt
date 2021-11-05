package eu.time.betterspotify.activities

import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageButton
import eu.time.betterspotify.controllers.NavigationController
import eu.time.betterspotify.R

abstract class NavigationBarActivity : AppCompatActivity() {
    private val navigationController = NavigationController.getInstance()

    override fun onStart() {
        super.onStart()

        navigationController.setCurrentPage(getCurrentPage())

        initNavigationBarListeners()
    }

    protected abstract fun getCurrentPage(): NavigationController.Page

    private fun initNavigationBarListeners() {
        findViewById<ImageButton>(R.id.btnLibrary)?.setOnClickListener {
            navigationController.navigateTo(it.context, NavigationController.Page.LIBRARY)
        }

        findViewById<ImageButton>(R.id.btnSearch)?.setOnClickListener {
            navigationController.navigateTo(it.context, NavigationController.Page.SEARCH)
        }
    }
}