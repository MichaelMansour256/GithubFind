package com.sriyank.javatokotlindemo.activities

import android.support.v7.app.AppCompatActivity
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.RecyclerView
import com.sriyank.javatokotlindemo.adapters.DisplayAdapter
import com.sriyank.javatokotlindemo.retrofit.GithubAPIService
import android.os.Bundle
import com.sriyank.javatokotlindemo.R
import android.support.v7.widget.LinearLayoutManager
import com.sriyank.javatokotlindemo.retrofit.RetrofitClient
import android.content.Intent
import com.sriyank.javatokotlindemo.activities.DisplayActivity
import com.sriyank.javatokotlindemo.models.SearchResponse
import io.realm.RealmResults
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import com.sriyank.javatokotlindemo.app.Constants
import com.sriyank.javatokotlindemo.app.Util
import com.sriyank.javatokotlindemo.models.Repository
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_display.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.HashMap

class DisplayActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    //private var mDrawerLayout: DrawerLayout? = null
    //private var mRecyclerView: RecyclerView? = null
    private var mDisplayAdapter: DisplayAdapter? = null
    private var browsedRepositories: List<Repository?>? = null
    private var mService: GithubAPIService? = null
    private var mRealm: Realm? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display)

        setSupportActionBar(toolbar)
        supportActionBar!!.setTitle("Showing Browsed Results")

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = layoutManager
        mService = RetrofitClient.getGithubAPIService()
        mRealm = Realm.getDefaultInstance()

        nav_view.setNavigationItemSelectedListener(this)

        val drawerToggle = ActionBarDrawerToggle(
            this,
            drawer_layout,
            toolbar,
            R.string.drawer_open,
            R.string.drawer_close
        )
        drawer_layout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
        val intent = intent
        if (intent.getIntExtra(Constants.KEY_QUERY_TYPE, -1) == Constants.SEARCH_BY_REPO) {
            val queryRepo = intent.getStringExtra(Constants.KEY_REPO_SEARCH)
            val repoLanguage = intent.getStringExtra(Constants.KEY_LANGUAGE)
            fetchRepositories(queryRepo, repoLanguage)
        } else {
            val githubUser = intent.getStringExtra(Constants.KEY_GITHUB_USER)
            fetchUserRepositories(githubUser)
        }
    }

    private fun fetchUserRepositories(githubUser: String?) {
        mService!!.searchRepositoriesByUser(githubUser)
            .enqueue(object : Callback<List<Repository?>?> {
                override fun onResponse(
                    call: Call<List<Repository?>?>,
                    response: Response<List<Repository?>?>
                ) {
                    if (response.isSuccessful) {
                        Log.i(TAG, "posts loaded from API $response")
                        browsedRepositories = response.body()
                        if (browsedRepositories != null && browsedRepositories!!.size > 0) setupRecyclerView(
                            browsedRepositories
                        ) else Util.showMessage(this@DisplayActivity, "No Items Found")
                    } else {
                        Log.i(TAG, "Error $response")
                        Util.showErrorMessage(this@DisplayActivity, response.errorBody())
                    }
                }

                override fun onFailure(call: Call<List<Repository?>?>, t: Throwable) {
                    Util.showMessage(this@DisplayActivity, t.message)
                }
            })
    }

    private fun fetchRepositories(queryRepo: String?, repoLanguage: String?) {
        var queryRepo = queryRepo
        val query: MutableMap<String, String?> = HashMap()
        if (repoLanguage != null && !repoLanguage.isEmpty()) queryRepo += " language:$repoLanguage"
        query["q"] = queryRepo
        mService!!.searchRepositories(query).enqueue(object : Callback<SearchResponse> {
            override fun onResponse(
                call: Call<SearchResponse>,
                response: Response<SearchResponse>
            ) {
                if (response.isSuccessful) {
                    Log.i(TAG, "posts loaded from API $response")
                    browsedRepositories = response.body()!!.items
                    if (browsedRepositories!!.size > 0) setupRecyclerView(browsedRepositories) else Util.showMessage(
                        this@DisplayActivity,
                        "No Items Found"
                    )
                } else {
                    Log.i(TAG, "error $response")
                    Util.showErrorMessage(this@DisplayActivity, response.errorBody())
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                Util.showMessage(this@DisplayActivity, t.toString())
            }
        })
    }

    private fun setupRecyclerView(items: List<Repository?>?) {
        mDisplayAdapter = DisplayAdapter(this, items)
        recyclerView!!.adapter = mDisplayAdapter
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        menuItem.isChecked = true
        closeDrawer()
        when (menuItem.itemId) {
            R.id.item_bookmark -> {
                showBookmarks()
                supportActionBar!!.title = "Showing Bookmarks"
            }
            R.id.item_browsed_results -> {
                showBrowsedResults()
                supportActionBar!!.title = "Showing Browsed Results"
            }
        }
        return true
    }

    private fun showBrowsedResults() {
        mDisplayAdapter!!.swap(browsedRepositories)
    }

    private fun showBookmarks() {
        mRealm!!.executeTransaction { realm ->
            val repositories = realm.where(
                Repository::class.java
            ).findAll()
            mDisplayAdapter!!.swap(repositories)
        }
    }

    private fun closeDrawer() {
        drawer_layout!!.closeDrawer(GravityCompat.START)
    }

    override fun onBackPressed() {
        if (drawer_layout!!.isDrawerOpen(GravityCompat.START)) closeDrawer() else {
            super.onBackPressed()
            mRealm!!.close()
        }
    }

    companion object {
        private val TAG = DisplayActivity::class.java.simpleName
    }
}