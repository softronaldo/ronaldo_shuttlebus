package com.example.test_driver2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HistoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HistoryFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var totayHistoryButton: Button? = null
    private var weekHistoryButton: Button? = null
    private var monthHistoryButton: Button? = null
    private var totalHistoryTextView: TextView? = null
    private var historyStatsTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        // 뷰가 생성된 후에 findViewById를 호출합니다.
        totayHistoryButton = view.findViewById(R.id.totay_history)
        weekHistoryButton = view.findViewById(R.id.week_history)
        monthHistoryButton = view.findViewById(R.id.month_history)
        totalHistoryTextView = view.findViewById(R.id.total_history)
        historyStatsTextView = view.findViewById(R.id.history_stats)

        // 각 버튼에 클릭 리스너를 설정합니다.
        totayHistoryButton?.setOnClickListener {
            totalHistoryTextView?.text = "오늘 운행 현황"
            historyStatsTextView?.text = "오늘의 운행 통계"
        }

        weekHistoryButton?.setOnClickListener {
            totalHistoryTextView?.text = "주간 운행 현황"
            historyStatsTextView?.text = "주간 운행 통계"
        }

        monthHistoryButton?.setOnClickListener {
            totalHistoryTextView?.text = "월간 운행 현황"
            historyStatsTextView?.text = "월간 운행 통계"
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 뷰 바인딩을 사용할 경우 여기서 바인딩 해제
        totayHistoryButton = null
        weekHistoryButton = null
        monthHistoryButton = null
        totalHistoryTextView = null
        historyStatsTextView = null
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HistoryFragment.
         */
        @JvmStatic
        fun newInstance() =
            HistoryFragment()
    }
}