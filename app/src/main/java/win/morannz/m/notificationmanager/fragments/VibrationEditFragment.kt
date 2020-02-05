package win.morannz.m.notificationmanager.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.math.MathUtils.clamp
import androidx.fragment.app.Fragment
import com.db.williamchart.data.Scale
import kotlinx.android.synthetic.main.fragment_vibration_edit.*
import win.morannz.m.notificationmanager.AlertGroup
import win.morannz.m.notificationmanager.BuildConfig
import win.morannz.m.notificationmanager.R
import win.morannz.m.notificationmanager.getAlertGroups
import kotlin.math.max


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
    private var mLineSet = mutableListOf(Pair(0F, 0F))
    private var mLastSeekBarValue = -1
    private var mLastSeekBarTimeMs = 0L
    private var mCumulativeTime = 0F

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
                // reset mLineSet
                mLineSet = mutableListOf(Pair(0F, 0F))
                mCumulativeTime = 0F
                mLastSeekBarTimeMs = System.currentTimeMillis()
                mLastSeekBarValue = -1
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // TODO: save as vibration pattern
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // TODO: append progress to mLineSet, render chart
                var value = clamp(progress, 0, 31)
                if (value == mLastSeekBarValue) {
                    return
                }
                mLastSeekBarValue = value
                value *= 8 // 32 values in the range 0 - 255

                // calculate how long the user
                val elapsedTime = System.currentTimeMillis() - mLastSeekBarTimeMs
                mLastSeekBarTimeMs = System.currentTimeMillis()

                mCumulativeTime += elapsedTime / 1000F
                mLineSet.add(Pair(mCumulativeTime, value.toFloat()))
                line_chart.updateScaleX(Scale(0F, max(2F, mCumulativeTime)))
                line_chart.notifyDataSetChanged()
                line_chart.show(mLineSet)
            }
        })
    }

    private fun loadVibrationChart() {
        val timingsString = mAg.vibrationPatternTimings ?: "0,500,1000,500,500"
        val timings = timingsString.split(",").map { it.toInt() }
        val amplitudesString = mAg.vibrationPatternAmplitudes ?: "0,50,250,100,30"
        val amplitudes = amplitudesString.split(",").map { it.toInt() }
        var cumulativeTime = 0F

        timings.forEachIndexed { i, element ->
            cumulativeTime += element / 1000F
            mLineSet.add(Pair(cumulativeTime, amplitudes[i].toFloat()))
        }

        line_chart.scaleX = Scale(0F, max(2F, cumulativeTime))
        line_chart.scaleY = Scale(0F, 255F)
        line_chart.horizontalJoiningLines = true
        line_chart.animation.duration = 1000
        line_chart.show(mLineSet)
    }
}
