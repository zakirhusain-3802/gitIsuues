package com.example.gitisuues


import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView

import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import com.example.gitisuues.adapter.IssueAdapter
import com.example.gitisuues.apiservice.GetIsuuesData
import com.example.gitisuues.apiservice.RetrofitClient
import com.example.gitisuues.dataclass.issuesdata
import com.example.gitisuues.dataclass.issuesdataitem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var issueAdapter: IssueAdapter
    private lateinit var searchView: SearchView
    var datafeteched = false
    var filteredDataSet = ArrayList<issuesdata>()
    var issueData = ArrayList<issuesdata>()
    var filterTiltel = " "
    var filterStatus = "all"
    private var selectedFilterOption: String = "All"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        issueAdapter = IssueAdapter(issueData, "fix")
        searchView = findViewById(R.id.search_issues)

        searchView.setOnClickListener() {
            searchView.isIconified = false
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterTiltel = newText!!
                filterDataByTitle(filterTiltel, filterStatus)
                return true
            }

        })

        val filterSataus: Button = findViewById(R.id.btn_filter)
        filterSataus.setOnClickListener() {
            showFilterMenu(it)
        }



        recyclerView = findViewById(R.id.issues_recylce)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = issueAdapter


        fetchClosedIssues()
    }

    private fun fetchClosedIssues() {
        // Using Retrofit to make API call to GitHub
        val progressBar = findViewById<ProgressBar>(R.id.progressbar)
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.INVISIBLE
        val apiService = RetrofitClient.getRetrofitInstance().create(GetIsuuesData::class.java)
        val call = apiService.getClosedIssues("supabase", "supabase", filterStatus)
        call.enqueue(object : Callback<List<issuesdataitem>> {
            override fun onResponse(
                call: Call<List<issuesdataitem>>,
                response: Response<List<issuesdataitem>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    progressBar.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    datafeteched = true
                    issueData.clear()
                    val res = response.body()
                    for (data in res!!) {
                        val issue = issuesdata(
                            data.title,
                            data.created_at,
                            data.closed_at,
                            data.user.login,
                            data.user.avatar_url,
                            data.state
                        );
                        issueData.add(issue)
                    }
                    issueAdapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(call: Call<List<issuesdataitem>>, t: Throwable) {
                println(t)
                Toast.makeText(this@MainActivity, "Failed to fetch data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun filterDataByTitle(titleFilter: String, status: String) {

        if (datafeteched) {
            filteredDataSet = issueData.filter { issue ->
                // You can customize the logic here based on your requirements
                issue.title?.contains(titleFilter, ignoreCase = true) == true

            } as ArrayList<issuesdata>
            println(filteredDataSet.size)
            if (filteredDataSet.size == 0) {
                val noresultfound = findViewById<TextView>(R.id.resultnotfound)
                noresultfound.visibility = View.VISIBLE
            } else {
                val noresultfound = findViewById<TextView>(R.id.resultnotfound)
                noresultfound.visibility = View.INVISIBLE
            }
            issueAdapter = IssueAdapter(filteredDataSet, "")
            issueAdapter.notifyDataSetChanged()
            recyclerView.adapter = issueAdapter
        }

    }

    @SuppressLint("ResourceType")
    fun showFilterMenu(view: View) {
        println(" in the menu")
        val popupMenu = PopupMenu(this, view)
        val inflater = popupMenu.menuInflater
        inflater.inflate(
            R.menu.filter_menu,
            popupMenu.menu
        )
        for (i in 0 until popupMenu.menu.size()) {
            val item = popupMenu.menu.getItem(i)



            if (item.title == selectedFilterOption) {
                if (item.title == selectedFilterOption) {
                    item.isChecked = true
                }
            }

        }





        popupMenu.setOnMenuItemClickListener { menuItem ->

            selectedFilterOption = menuItem.title.toString()
            println(selectedFilterOption)

            for (i in 0 until popupMenu.menu.size()) {
                val item = popupMenu.menu.getItem(i)
                if (item.title == selectedFilterOption) {
                    item.isChecked = true
                }

            }

            // Update icons for all items to show/hide the checkmark

            when (menuItem.itemId) {
                R.id.menuAll -> {
                    filterStatus = "all"
                    fetchClosedIssues()

                    true
                }

                R.id.menuOpen -> {
                    filterStatus = "open"
                    fetchClosedIssues()

                    true
                }

                R.id.menuClosed -> {
                    filterStatus = "closed"
                    fetchClosedIssues()

                    true
                }

                else -> false
            }
        }
        popupMenu.show()
    }
}




