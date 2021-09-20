package eu.time.betterspotify

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import eu.time.betterspotify.recycleview.adapter.SearchRecycleViewAdapter
import eu.time.betterspotify.spotify.SpotifyApi
import eu.time.betterspotify.spotify.data.search.Item
import eu.time.betterspotify.spotify.data.search.Search

class SearchActivity : AppCompatActivity() {
    private lateinit var adapter: SearchRecycleViewAdapter
    private val searchResults = mutableListOf<Item>()
    private lateinit var etSearchField: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        initRecycleView()

        etSearchField = findViewById(R.id.etSearchField)

        val context = this.applicationContext

        etSearchField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            //todo search types wie in der app
            override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
                if (text.isNotEmpty()) {
                    SpotifyApi.getInstance().search(context, text.toString(), listOf("track", "artist")) { result ->
                        val searchResult = Gson().fromJson(result, Search::class.java)

                        updateRecycleView(searchResult.tracks.items)
                    }
                }
            }
        })
    }

    private fun focusSearchField() {
        etSearchField.requestFocus()
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(etSearchField, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun onStart() {
        super.onStart()
        focusSearchField()

        MiniPlayerController.getInstance().start(this)
    }

    override fun onStop() {
        super.onStop()

        MiniPlayerController.getInstance().stop()
    }

    private fun initRecycleView() {
        val rvSearchResult = findViewById<RecyclerView>(R.id.rvSearchResult)

        adapter = SearchRecycleViewAdapter(searchResults)

        rvSearchResult.adapter = adapter
        rvSearchResult.layoutManager = LinearLayoutManager(this)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateRecycleView(newData: List<Item>) {
        searchResults.clear()
        searchResults.addAll(newData)
        adapter.notifyDataSetChanged()
    }
}