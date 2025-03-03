package org.seemeet.seemeet.ui.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.material.chip.Chip
import org.seemeet.seemeet.R
import org.seemeet.seemeet.databinding.ActivityDetailBinding
import org.seemeet.seemeet.ui.viewmodel.DetailViewModel
import org.seemeet.seemeet.util.BindingAdapters

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private val viewModel: DetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val planId = intent.getIntExtra("planId", -1)
        val id = intent.getIntExtra("id", -1)

        viewModel.requestPlanId(planId)
        viewModel.requestInvitationId(id)
        viewModel.requestInvitationDetail(id)
        initClickListener()
        planDetailObserver()
        canceledDetailObserver()
    }

    private fun planDetailObserver() {
        val planId = intent.getIntExtra("planId", -1)
        if(planId != 0){
            onConfirmed()
        }
    }

    private fun canceledDetailObserver(){
        viewModel.canceledDetail.observe(this, Observer { canceledDetail ->
            canceledDetail.forEach {
                if(it.data.isCanceled) {
                    onCanceled()
                }
            }
        })
    }

    private fun initClickListener() {
        binding.ivDetailBack.setOnClickListener {
            finish()
        }

        binding.btnAppointmentCancel.setOnClickListener {
            showDialog()
        }
    }

    private fun onConfirmed(){
        viewModel.planDetail.observe(this, Observer { planDetail ->
            planDetail.forEach {
                BindingAdapters.setYearMonthDate(binding.tvDetailDay, it.data.date)
                BindingAdapters.setStartEndTimeText(
                    binding.tvDetailTime,
                    it.data.start,
                    it.data.end
                )
                binding.apply {
                    tvDetailTitle.text = it.data.invitationTitle
                    tvRecieveLetterTitle.text = it.data.invitationTitle
                    tvRecieveLetterContent.text = it.data.invitationDesc
                    tvDetailHostName.text = it.data.hostname
                }

                // 가능한 사람 이름 칩그룹
                it.data.possible.forEach {
                    binding.cgDetailFriendList.addView(Chip(this).apply {
                        text = it.username
                        setChipBackgroundColorResource(R.color.pink01)
                        setTextAppearance(R.style.chipTextWhiteStyle2)
                        isCheckable = false
                        isEnabled = false
                    })
                }

                // 불가능한 사람 이름 칩그룹
                it.data.impossible.forEach {
                    binding.cgDetailFriendList.addView(Chip(this).apply {
                        text = it.username
                        setChipBackgroundColorResource(R.color.gray04)
                        setTextAppearance(R.style.chipTextWhiteStyle2)
                        isCheckable = false
                        isEnabled = false
                    })
                }
            }
        })
    }

    private fun onCanceled(){
        viewModel.canceledDetail.observe(this, Observer { canceledDetail ->
            canceledDetail.forEach {
                binding.tvDetailTime.apply {
                    text = getString(R.string.noti_done_canceled)
                    setTextColor(getColor(R.color.pink01))
                }

                binding.apply {
                    tvDetailTitle.text = it.data.invitationTitle
                    tvRecieveLetterTitle.text = it.data.invitationTitle
                    tvRecieveLetterContent.text = it.data.invitationDesc
                }

                // 받은 모든 사람 이름 칩그룹
                it.data.guest.forEach {
                    binding.cgDetailFriendList.addView(Chip(this).apply {
                        text = it.username
                        setChipBackgroundColorResource(R.color.gray04)
                        setTextAppearance(R.style.chipTextWhiteStyle2)
                        isCheckable = false
                        isEnabled = false
                    })
                }

                binding.btnAppointmentCancel.visibility = View.GONE
            }
        })

        viewModel.canceledHostDetail.observe(this, Observer { canceledHostDetail ->
            canceledHostDetail.forEach {
                binding.apply {
                    tvDetailHostName.text = it.data.invitation.host.username
                }
            }
        })
    }


    private fun showDialog(){
        val planId = intent.getIntExtra("planId", -1)
        var dialogView = DetailDialogFragment()
        val bundle = Bundle()

        dialogView.arguments = bundle
        dialogView.setButtonClickListener( object :  DetailDialogFragment.OnButtonClickListener {
            override fun onCancelNoClicked() {
            }

            override fun onCancelYesClicked() {
                viewModel.deletePlan(planId)
                Toast.makeText(this@DetailActivity, "약속이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
        })
        dialogView.show(supportFragmentManager, "send wish checkbox time")
    }

    companion object {
        fun start(context: Context, planId: Int) {
            val intent = Intent(context, DetailActivity::class.java)
            intent.putExtra("planId", planId)
            context.startActivity(intent)
        }
    }
}