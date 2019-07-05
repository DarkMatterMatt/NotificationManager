package win.morannz.m.notificationmanager.adapters

import android.content.pm.PackageManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_recent_list_item.view.*
import win.morannz.m.notificationmanager.R
import win.morannz.m.notificationmanager.RecentNotification
import win.morannz.m.notificationmanager.fragments.RecentsFragment
import win.morannz.m.notificationmanager.fragments.RecentsListFragment.OnListFragmentInteractionListener
import java.text.SimpleDateFormat
import java.util.*

class RecentListRecyclerViewAdapter(
    private val mValues: List<RecentNotification>,
    private val mListener: OnListFragmentInteractionListener?
) : RecyclerView.Adapter<RecentListRecyclerViewAdapter.ViewHolder>() {
    companion object {
        private val TAG = this::class.java.simpleName
    }

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            mListener?.onListFragmentInteraction(RecentsFragment.INTERACTION, v.tag)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_recent_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]

        holder.mTitleView.text = if (item.title != "") item.title else "<title is empty>"
        holder.mTextView.text = if (item.text != "") item.text else "<text is empty>"

        // set datetime field
        val sdf = SimpleDateFormat.getDateTimeInstance()
        holder.mDatetimeView.text = sdf.format(Date(item.time))

        // set package icon
        try {
            val icon = holder.mPackageIconView.context.packageManager.getApplicationIcon(item.packageName)
            holder.mPackageIconView.setImageDrawable(icon)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "Caught PackageManager.NameNotFoundException for " + item.packageName)
        }

        with (holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mDatetimeView: TextView = mView.time
        val mTitleView: TextView = mView.title
        val mTextView: TextView = mView.text
        val mPackageIconView: ImageView = mView.package_icon

        override fun toString(): String {
            return "${super.toString()} '${mTitleView.text}'"
        }
    }
}
