package com.example.gitisuues.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gitisuues.R
import com.example.gitisuues.dataclass.issuesdata
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId


class IssueAdapter(private val issueList: List<issuesdata>, titleFilter: String) :
    RecyclerView.Adapter<IssueAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title = itemView.findViewById<TextView>(R.id.tittleTextView)
        val created_at = itemView.findViewById<TextView>(R.id.createdAtTextView)
        val closed_at = itemView.findViewById<TextView>(R.id.closedAtTextView)
        val userName = itemView.findViewById<TextView>(R.id.userNameTextView)
        val userImage = itemView.findViewById<ImageView>(R.id.userImage)
        val status = itemView.findViewById<TextView>(R.id.statusTextView)
        val status_sym = itemView.findViewById<CardView>(R.id.statuscard)

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate your list item view here and return a new ViewHolder
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.issue_layout, parent, false)
        return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val issue = issueList[position]

        holder.title.text = issue.title
        holder.userName.text = issue.user_name
        val createddate = formatTimeAgo(issue.created_at.toString())
        holder.created_at.text = ("opened " + createddate)


        if (issue.closed_at.isNullOrBlank()) {

            holder.closed_at.text = issue.closed_at
        } else {
            val closeddate = formatTimeAgo(issue.closed_at.toString())
            holder.closed_at.text = ("closed " + closeddate)
        }


        holder.status.text = issue.status
        if (issue.status.equals("closed")) {
            println("closed")
            holder.status_sym.setCardBackgroundColor(
                ContextCompat.getColor(
                    holder.itemView.context.applicationContext,
                    R.color.md_theme_light_primary
                )
            )
        } else {
            holder.status_sym.setCardBackgroundColor(
                ContextCompat.getColor(
                    holder.itemView.context.applicationContext,
                    R.color.opened
                )
            )
        }

        // Using Glide to load image from Url
        Glide.with(holder.itemView.context.applicationContext)
            .load(issue.image_url.toString())
            .placeholder(R.drawable.ic_action_name) // Placeholder image resource ID
            .error(R.drawable.ic_action_name) // Error image resource ID (optional)
            .into(holder.userImage)

    }

    override fun getItemCount(): Int {
        return issueList.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatTimeAgo(iso8601Date: String): String {
        val instant = Instant.parse(iso8601Date)
        val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        val now = LocalDateTime.now()

        val duration = Duration.between(dateTime, now)
        val seconds = duration.seconds

        return when {
            seconds < 60 -> "$seconds seconds ago"
            seconds < 3600 -> "${seconds / 60} minutes ago"
            seconds < 86400 -> "${seconds / 3600} hours ago"
            seconds < 2592000 -> "${seconds / 86400} days ago"
            seconds < 31536000 -> "${seconds / 2592000} months ago"
            else -> "${seconds / 31536000} years ago"
        }
    }
}
