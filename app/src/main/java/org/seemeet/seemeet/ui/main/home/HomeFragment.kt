package org.seemeet.seemeet.ui.main.home

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import org.seemeet.seemeet.R
import org.seemeet.seemeet.data.SeeMeetSharedPreference
import org.seemeet.seemeet.data.SeeMeetSharedPreference.getLogin
import org.seemeet.seemeet.data.local.ReminderData
import org.seemeet.seemeet.databinding.FragmentHomeBinding
import org.seemeet.seemeet.ui.detail.DetailActivity
import org.seemeet.seemeet.ui.friend.FriendActivity
import org.seemeet.seemeet.ui.main.home.adapter.ReminderListAdapter
import org.seemeet.seemeet.ui.notification.NotificationActivity
import org.seemeet.seemeet.ui.receive.DialogHomeNoLoginFragment
import org.seemeet.seemeet.ui.receive.ReceiveNoDialogFragment
import org.seemeet.seemeet.ui.registration.LoginActivity
import org.seemeet.seemeet.ui.viewmodel.HomeViewModel
import org.seemeet.seemeet.util.calDday
import org.seemeet.seemeet.util.setBetweenDays
import org.seemeet.seemeet.util.setBetweenDays2
import java.time.YearMonth

class HomeFragment : Fragment() {
    private var _binding : FragmentHomeBinding? = null
    val binding get() = _binding!!

    private val viewmodel : HomeViewModel by activityViewModels()
    var friendCnt : Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

        //원래 이 코드는 로그인 했을 경우, id와 다른 토큰과 함께 sharedPreference에 넣어야함.
        //그리고 로그아웃할때 setLogin을 false로 하던가, sharedPreference를 다 지우던가. 후자가 나을듯...?
        SeeMeetSharedPreference.clearStorage()
        SeeMeetSharedPreference.setLogin(true)
        SeeMeetSharedPreference.setUserId(7)

        //김안드 토큰 친구 있 _ id 6
        //SeeMeetSharedPreference.setToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6NiwiZW1haWwiOiJhbmRyb2lkMDFAYW5kcm9pZC5jb20iLCJuYW1lIjpudWxsLCJpZEZpcmViYXNlIjoiODlqaVprRlFMYVVPV2dSSzB6TU94NXFHeVY1MyIsImlhdCI6MTY0MjUxNjUzNywiZXhwIjoxNjQ1MTA4NTM3LCJpc3MiOiJ3ZXNvcHQifQ.lIGJGaaWExXwYO9wS1j4okvuXb_aoAlkuFNlxmwmbk8")

        //이코트 토큰 친구 없 _ id 7
        SeeMeetSharedPreference.setToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6NywiZW1haWwiOiJhbmRyb2lkMDJAYW5kcm9pZC5jb20iLCJuYW1lIjpudWxsLCJpZEZpcmViYXNlIjoiMzJ3SkN3S0dBYVB5RFU4eVRxczN5TGlUeTJiMiIsImlhdCI6MTY0MjUxMjk0NSwiZXhwIjoxNjQ1MTA0OTQ1LCJpc3MiOiJ3ZXNvcHQifQ.Tyuqdf3PYswKJcVekUJZ0KZ2SA2mZ1kevTJEENhthl8")

        //최구글 토큰 친구 없, 약속 없 id 8
        //SeeMeetSharedPreference.setToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6OCwiZW1haWwiOiJhbmRyb2lkMDNAYW5kcm9pZC5jb20iLCJuYW1lIjpudWxsLCJpZEZpcmViYXNlIjoiR21xMUF5cElFNFRVWHR2SDYzSFNGdmxibE9OMiIsImlhdCI6MTY0MjUxMzAyNywiZXhwIjoxNjQ1MTA1MDI3LCJpc3MiOiJ3ZXNvcHQifQ._XKs792zcgzL47RjGTyQDaeVnHS_thz2euerMg1Nnzw")

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClickListener()
        initNaviDrawer()
        // (1) 더미데이터 셋팅 _ 이후 서버통신 시 교체

        if(getLogin()) {
            //로그인 했을 경우 이것저것 서버통신 후 뷰모델 쪽에서 homebanner도 호출하자..
            //viewmodel.setReminderList()
            viewmodel.requestFriendList()
            viewmodel.requestComePlanList()
            viewmodel.requestLastPlanData()
        } else {
            //안 했을 경우.
            setHomeBanner(-1)
            setNoReminderList()
        }

        // (2) 어뎁터와 옵저버 셋팅
        setReminderAdapter()

        setViewModelObserve()
    }


    private fun initClickListener(){

        binding.apply{
            ivHomeFriend.setOnClickListener {
                FriendActivity.start(requireContext())
            }
            ivHomeNoti.setOnClickListener {
                NotificationActivity.start(requireContext())
            }

            ivMypageMenu.setOnClickListener{
                binding.dlHomeMypage.openDrawer(GravityCompat.START)
            }
            nvMypage.ivMypageBack.setOnClickListener {
                binding.dlHomeMypage.closeDrawer(GravityCompat.START)
            }

            nvMypage.clMypageLogin.setOnClickListener{
                LoginActivity.start(requireContext())
            }
        }

    }

    private fun initNaviDrawer(){
        val toggle = ActionBarDrawerToggle(
            this.activity, binding.dlHomeMypage, R.string.home_drawer_open, R.string.home_drawer_close
        )

        binding.dlHomeMypage.addDrawerListener(toggle)
        toggle.syncState()

    }

    // 어댑터
    private fun setReminderAdapter() {
        val reminderListAdapter = ReminderListAdapter()

        reminderListAdapter.setItemClickListener(object: ReminderListAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                Log.d("****************home_reminder_title_click_position", "${v.id}/${position}")
                //val rd : ReminderData = viewmodel.reminderList.value!!.get(position)
                val comePlanId = viewmodel.comePlanList.value!!.data.get(position).planId
                val intent = Intent(requireContext(), DetailActivity::class.java)
                intent.putExtra("planId", comePlanId)
                startActivity(intent)
            }
        })

        binding.rvHomeReminder.adapter = reminderListAdapter
    }

    // 옵저버 _ 위에서 (1)로 데이터 넣을 경우 옵저버가 관찰하다가 업데이트함.
    private fun setViewModelObserve() {

        viewmodel.comePlanList.observe(viewLifecycleOwner) { comePlanList ->
            with(binding.rvHomeReminder.adapter as ReminderListAdapter) {
                //어댑터 내에서 notifyDataSetChanged() 해주는 역할의 함수


                setReminder(comePlanList.data.filter {
                    it.date.setBetweenDays2() < 0
                })

                if (comePlanList.data.isEmpty()) {
                    setNoReminderList()
                }
            }
        }

        viewmodel.lastPlan.observe(viewLifecycleOwner){
            lastPlan ->
                if(lastPlan.date.isNullOrEmpty()){
                    setHomeBanner(-1)
                } else {
                    setHomeBanner(lastPlan.date.calDday())
                }
        }

    }

    private fun setNoReminderList(){
        binding.clHomeNoReminder.visibility = View.VISIBLE
    }

    private fun setHomeBanner(day : Int){

        var flag = -1
        var text = ""
        var white = ""
        var imgId = 0
        var random = (1..2).random()
        if(day == -1 ){
            flag = if(getLogin() && friendCnt != 0 ) 2
                    else 1
        } else {
            if(random == 1) {
                if (day == 0) flag = 3
                else if (day <= 14) flag = 4
                else if (day <= 21) flag = 5
                else flag = 6
            }else {
                flag = 2
            }
        }

        when(flag){
            1 -> {
                text = "씨밋과 함께\n약속을 잡아볼까요?"
                white = "약속"
                imgId = R.drawable.img_illust_5
            }
            2 -> {
                text = "친구가 당신의 약속 신청을\n 기다리고 있어요!"
                white = "약속 신청"
                imgId = R.drawable.img_illust_4
            }
            3 -> {
                text = "아싸 오늘은\n친구 만나는 날이다!"
                white = "친구"
                imgId = R.drawable.img_illust_1
            }
            4 -> {
                text = "약속 잡기에\n딱 좋은 시기예요!"
                white = "딱 좋은"
                imgId = R.drawable.img_illust_8
            }
            5 -> {
                text = "친구와 만난지\n벌써 ${day}일이 지났어요!"
                white = "${day}일"
                imgId = R.drawable.img_illust_6

            }
            6 -> {
                text = "친구를 언제 만났는지\n기억도 안나요...."
                white = "기억도"
                imgId = R.drawable.img_illust_7
            }
        }

        val start = text.indexOf(white)
        val end = start + white.length

        Log.d("*************HOME_BANNER_SETTING_INDEX", "$flag, $start, $end, ${text.length}")
        val ss = SpannableStringBuilder(text)
        ss.setSpan(ForegroundColorSpan(Color.WHITE), start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
        ss.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        ss.setSpan(RelativeSizeSpan(1.2f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.tvHomeMsg.text = ss
        binding.ivHomeBanner.setImageResource(imgId)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()

        if(getLogin()) {
            //로그인 했을 경우 이것저것 서버통신 후 뷰모델 쪽에서 homebanner도 호출하자..
            //viewmodel.setReminderList()
            viewmodel.requestFriendList()
            viewmodel.requestComePlanList()
            viewmodel.requestLastPlanData()
        } else {
            //안 했을 경우.
            setHomeBanner(-1)
            setNoReminderList()
        }
    }

}