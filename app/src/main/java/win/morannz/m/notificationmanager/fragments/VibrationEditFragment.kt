package win.morannz.m.notificationmanager.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.fragment.app.Fragment
import com.db.williamchart.data.Scale
import kotlinx.android.synthetic.main.fragment_vibration_edit.*
import win.morannz.m.notificationmanager.AlertGroup
import win.morannz.m.notificationmanager.BuildConfig
import win.morannz.m.notificationmanager.R
import win.morannz.m.notificationmanager.getAlertGroups


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [VibrationEditFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [VibrationEditFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class VibrationEditFragment : Fragment() {
    private var mAgId = -1
    private var mAg = AlertGroup()
    private var mListener: OnFragmentInteractionListener? = null
    private var mAlertGroups = mutableMapOf<Int, AlertGroup>()
    private var mLineSet = linkedMapOf<String, Float>()

    companion object {
        private val TAG = VibrationEditFragment::class.java.simpleName
        private const val ALERT_GROUP_ID = "alertGroupId"

        private const val ME = "${BuildConfig.APPLICATION_ID}.VibrationEditFragment"

        fun newInstance(alertGroupId: Int) = VibrationEditFragment().apply {
            arguments = Bundle().apply {
                putInt(ALERT_GROUP_ID, alertGroupId)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAgId = arguments!!.getInt(ALERT_GROUP_ID)
        mAlertGroups = getAlertGroups(context!!).toMutableMap()
        mAg = mAlertGroups[mAgId] ?: AlertGroup()

        //setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_vibration_edit, container, false)
    }

    override fun onStart() {
        super.onStart()

        // update the action bar title
        activity?.setTitle(R.string.title_vibration_edit)

        addSeekBarListeners()
        loadVibrationChart()
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(type: String, data: Any)
    }

    private fun addSeekBarListeners() {
        seek_bar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // TODO: reset mLineSet
                //mLineSet = linkedMapOf("0" to 0F)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // TODO: append progress to mLineSet, render chart
                Log.d(TAG, "seek_bar progress: $progress")
            }
        })
    }

    private fun loadVibrationChart() {
        val amplitudesString = mAg.vibrationPatternAmplitudes ?: "0,50,250,100,30"
        val amplitudes = amplitudesString.split(",").map { it.toInt() }

        amplitudes.forEachIndexed { i, element ->
            mLineSet[i.toString()] = element.toFloat()
        }

        line_chart.scale = Scale(0F, 255F)
        line_chart.animation.duration = 1000
        line_chart.animate(mLineSet)
    }
}
