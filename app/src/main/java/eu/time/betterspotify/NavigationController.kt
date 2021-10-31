package eu.time.betterspotify

import android.content.Context
import android.content.Intent

class NavigationController {
    companion object {
        private val INSTANCE = NavigationController()


        fun getInstance(): NavigationController {
            return INSTANCE
        }
    }

    enum class Page {
        UNDEFINED, LIBRARY, SEARCH
    }

    private var currentPage = Page.UNDEFINED

    fun setCurrentPage(currentPage: Page) {
        this.currentPage = currentPage
    }

    fun navigateTo(context: Context, destination: Page) {
        if (destination == currentPage) return

        val activityClass = when (destination) {
            Page.LIBRARY -> LibraryActivity::class.java
            Page.SEARCH -> SearchActivity::class.java
            else -> return
        }

        currentPage = destination

        val intent = Intent(context, activityClass)
        context.startActivity(intent)
    }
}