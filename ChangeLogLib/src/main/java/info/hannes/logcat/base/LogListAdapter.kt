package info.hannes.logcat.base

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import info.hannes.R
import java.util.*

class LogListAdapter(private val completeLogs: ArrayList<String>, filter: String) : RecyclerView.Adapter<LogListAdapter.LogViewHolder>() {
    var filterLogs: List<String> = ArrayList()

    init {
        setFilter(filter)
    }

    fun setFilter(filter: String) {
        filterLogs = completeLogs.filter { it.contains(filter) }
        notifyDataSetChanged()
    }

    /**
     * Define the view for each log in the list
     */
    class LogViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val logContent: TextView = view.findViewById(R.id.logLine)

    }

    /**
     * Create the view for each log in the list
     *
     * @param viewGroup
     * @param i
     * @return
     */
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): LogViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_log, viewGroup, false)
        return LogViewHolder(view)
    }

    /**
     * Fill in each log in the list
     *
     * @param holder
     * @param position
     */
    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.logContent.text = filterLogs[position]
        filterLogs[position].let {
            if (it.contains(" E: ") || it.startsWith("E: ")) {
                holder.logContent.setTextColor(Color.RED)
            } else if (it.contains(" W: ") || it.startsWith("W: ")) {
                holder.logContent.setTextColor(Color.MAGENTA)
            } else if (it.contains(" V: ") || it.startsWith("V: ")) {
                holder.logContent.setTextColor(Color.GRAY)
//        } else {
//            holder.logContent.setTextColor(ContextCompat.getColor(context, R.color.primary_dark))
            }
        }
        if (filterLogs[position].contains(" E: ")) {
            holder.logContent.setTextColor(Color.RED)
        } else if (filterLogs[position].contains(" W: ")) {
            holder.logContent.setTextColor(Color.MAGENTA)
        } else if (filterLogs[position].contains(" V: ")) {
            holder.logContent.setTextColor(Color.GRAY)
//        } else {
//            holder.logContent.setTextColor(ContextCompat.getColor(context, R.color.primary_dark))
        }
    }

    override fun getItemCount(): Int {
        return filterLogs.size
    }

}
