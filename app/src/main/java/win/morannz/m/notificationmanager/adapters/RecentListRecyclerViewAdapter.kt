package win.morannz.m.notificationmanager.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import win.morannz.m.notificationmanager.fragments.RecentsListFragment.OnListFragmentInteractionListener
import kotlinx.android.synthetic.main.fragment_recent_list_item.view.*
import win.morannz.m.notificationmanager.C
import win.morannz.m.notificationmanager.RecentNotification
import win.morannz.m.notificationmanager.R
import android.content.pm.PackageManager
import android.util.Log
import android.widget.ImageView
import java.text.SimpleDateFormat
import java.util.*


/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class RecentListRecyclerViewAdapter(
    private val mValues: List<RecentNotification>,
    private val mListener: OnListFragmentInteractionListener?
) : RecyclerView.Adapter<RecentListRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as RecentNotification
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            mListener?.onListFragmentInteraction(C.RECENT_NOTIFICATION, item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_recent_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]

        holder.mTitleView.text = item.title
        holder.mTextView.text = item.text

        // set datetime field
        val sdf = SimpleDateFormat.getDateTimeInstance()
        holder.mDatetimeView.text = sdf.format(Date(item.time))

        // set package icon
        try {
            val icon = holder.mPackageIconView.context.packageManager.getApplicationIcon(item.packageName)
            holder.mPackageIconView.setImageDrawable(icon)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("NM3", "Caught PackageManager.NameNotFoundException for " + item.packageName)
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
        val mPackageIconView: ImageView = mView.packageIcon

        override fun toString(): String {
            return super.toString() + " '" + mTitleView.text + "'"
        }
    }
}
