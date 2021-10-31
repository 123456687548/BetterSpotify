package eu.time.betterspotify

import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageButton

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