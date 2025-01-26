package saini.ayush.whatsdirectmessage.ui

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import saini.ayush.whatsdirectmessage.R
import saini.ayush.whatsdirectmessage.model.CallLogEntry

class CallLogsDialog(
    private val interaction: CallLogsViewAdapter.Interaction,
    private val callLogs: List<CallLogEntry>,
) : BottomSheetDialogFragment() {

    private lateinit var callLogsViewAdapter: CallLogsViewAdapter

    lateinit var layout: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        layout =  inflater.inflate(R.layout.call_logs_layout, container, false)
        initRV()
        return layout
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (view.parent as View).setBackgroundColor(Color.TRANSPARENT)
    }

    private fun initRV() {

        layout.findViewById<RecyclerView>(R.id.callLogsRV).apply {
            Log.d("@AYUSH", "initRV: $callLogs")
            callLogsViewAdapter = CallLogsViewAdapter(interaction = interaction)
            adapter = callLogsViewAdapter
        }
        callLogsViewAdapter.submitList(callLogs)
    }

}