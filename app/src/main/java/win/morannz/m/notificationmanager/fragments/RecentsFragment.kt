package win.morannz.m.notificationmanager.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import win.morannz.m.notificationmanager.BuildConfig
import win.morannz.m.notificationmanager.R

class RecentsFragment : Fragment() {
    private var mListener: OnFragmentInteractionListener? = null

    companion object {
        fun newInstance() = RecentsFragment()
        private val TAG = this::class.java.simpleName

        private const val ME = "${BuildConfig.APPLICATION_ID}.RecentsFragment"
        const val INTENT_UPDATE = "$ME.INTENT_UPDATE"
        const val UPDATE_DATA = "$ME.UPDATE_DATA"
        const val INTERACTION = "$ME.INTERACTION"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recents, container, false)
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(type: String, data: Any)
    }
}
