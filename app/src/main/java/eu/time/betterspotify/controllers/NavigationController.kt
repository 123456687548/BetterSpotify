package eu.time.betterspotify.controllers

import android.content.Context
import eu.time.betterspotify.activities.SearchActivity
import eu.time.betterspotify.activities.LibraryActivity

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

        currentPage = destination

        when (destination) {
            Page.LIBRARY -> LibraryActivity.openLibrary(context)
            Page.SEARCH -> SearchActivity.open(context)
            else -> return
        }
    }
}