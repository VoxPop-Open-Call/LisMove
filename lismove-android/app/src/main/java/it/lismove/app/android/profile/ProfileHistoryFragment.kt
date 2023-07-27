package it.lismove.app.android.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import it.lismove.app.android.awards.AwardWrapperActivity
import it.lismove.app.android.databinding.FragmentProfileHistoryBinding
import it.lismove.app.android.general.LisMoveFragment
import it.lismove.app.android.session.ui.SessionsHistoryActivity


class ProfileHistoryFragment : LisMoveFragment() {
    private var _binding: FragmentProfileHistoryBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileHistoryBinding.inflate(inflater, container, false)
        val view = binding.root

        with(binding){
            profileHistoryMySession.setOnClickListener { openMySessionHistory() }
            profileHistoryHomeWorkSession.setOnClickListener { openMyHomeWorkSessionHistory() }
            profileMyAwards.setOnClickListener { openMyAward() }
        }

        return view
    }

    fun openMySessionHistory(){
        startActivity(SessionsHistoryActivity.getIntent(requireActivity(), false))
    }
    fun openMyHomeWorkSessionHistory(){
        startActivity(SessionsHistoryActivity.getIntent(requireActivity(), true))
    }
    fun openMyAward(){
        startActivity(AwardWrapperActivity.getMyAwardOnlyActivity(requireActivity()))
    }
}