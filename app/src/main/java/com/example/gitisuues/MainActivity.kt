package com.example.gitisuues

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import android.widget.ProgressBar
import androidx.appcompat.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    var datafetched = false
    var filteredDataSet = ArrayList<issuesdata>()
    var issueData = ArrayList<issuesdata>()
    var filterTitle = " "                                          //to Store the search query text
    var filterStatus = "all"                                       // initial storing the status filter to all
    private var selectedFilterOption: String = "All"

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //initializing all the required view and adapter

        issueAdapter = IssueAdapter(issueData, "fix")
        searchView = findViewById(R.id.search_issues)
        recyclerView = findViewById(R.id.issues_recycle)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = issueAdapter

        searchView.setOnClickListener {
            searchView.isIconified = false
        }

        // searching for  the issue when uer type in search view
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterTitle = newText!!
                filterDataByTitle(filterTitle)
                return true
            }
        })

        // initializing filter Status button for status filter
        val filterStatusButton: Button = findViewById(R.id.btn_filter)
        filterStatusButton.setOnClickListener {
            showFilterMenu(it)
        }

        fetchClosedIssues()
    }

    private fun fetchClosedIssues() {
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
                    datafetched = true
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
                        )
                        issueData.add(issue)
                    }
                    issueAdapter.notifyDataSetChanged()
                    filterDataByTitle(filterTitle)
                }
            }

            override fun onFailure(call: Call<List<issuesdataitem>>, t: Throwable) {
                progressBar.visibility=View.INVISIBLE
                val noResultFound = findViewById<TextView>(R.id.resultnotfound)
                noResultFound.text="Failed to fetch data !! Check network connectivity"
                noResultFound.visibility=View.VISIBLE

                println(t)
                Toast.makeText(this@MainActivity, "Failed to fetch data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filterDataByTitle(titleFilter: String) {

        if (datafetched) {
            filteredDataSet = issueData.filter {
                it.title?.contains(titleFilter, ignoreCase = true) == true
            } as ArrayList<issuesdata>

            val noResultFound = findViewById<TextView>(R.id.resultnotfound)
            noResultFound.visibility = if (filteredDataSet.isEmpty()) View.VISIBLE else View.INVISIBLE

            issueAdapter = IssueAdapter(filteredDataSet, "")
            issueAdapter.notifyDataSetChanged()
            recyclerView.adapter = issueAdapter
        }
    }

    @SuppressLint("ResourceType")
    private fun showFilterMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        val inflater = popupMenu.menuInflater
        inflater.inflate(R.menu.filter_menu, popupMenu.menu)

        for (i in 0 until popupMenu.menu.size()) {
            val item = popupMenu.menu.getItem(i)

            if (item.title == selectedFilterOption) {
                item.isChecked = true
            }
        }

        popupMenu.setOnMenuItemClickListener { menuItem ->
            selectedFilterOption = menuItem.title.toString()

            for (i in 0 until popupMenu.menu.size()) {
                val item = popupMenu.menu.getItem(i)
                if (item.title == selectedFilterOption) {
                    item.isChecked = true
                }
            }

            when (menuItem.itemId) {
                R.id.menuAll -> {
                    filterStatus = "all"
                    fetchClosedIssues()
                }

                R.id.menuOpen -> {
                    filterStatus = "open"
                    fetchClosedIssues()
                }

                R.id.menuClosed -> {
                    filterStatus = "closed"
                    fetchClosedIssues()
                }

                else -> false
            }
            fetchClosedIssues()
            true
        }
        popupMenu.show()
    }
}
