package org.seemeet.seemeet.ui.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import org.seemeet.seemeet.data.SeeMeetSharedPreference
import org.seemeet.seemeet.data.api.RetrofitBuilder
import org.seemeet.seemeet.data.model.request.mypage.RequestChangePush
import org.seemeet.seemeet.data.model.request.register.RequestRegisterNameId
import org.seemeet.seemeet.data.model.response.register.ResponseRegisterNameId
import org.seemeet.seemeet.data.model.response.withdrawal.ResponseWithdrawal
import org.seemeet.seemeet.ui.registration.RegisterNameIdActivity

class MyPageViewModel(application: Application) : BaseViewModel(application) {

    private val _MyPageNameIdList = MutableLiveData<ResponseRegisterNameId>()
    val MyPageNameIdList: LiveData<ResponseRegisterNameId>
        get() = _MyPageNameIdList
    val mypageName = MutableLiveData("")
    val mypageId = MutableLiveData("")
    val warning = MutableLiveData("")
    val status = MutableLiveData(false)
    val name_cursorPos = MutableLiveData(0)
    val id_cursorPos = MutableLiveData(0)
    val name_invalidCase = MutableLiveData(false)
    val id_upperCase = MutableLiveData(false)

    private val _withdrawalStatus = MutableLiveData<Boolean>(false)
    val withdrawalStatus: LiveData<Boolean>
        get() = _withdrawalStatus

    private val _mypageStatus = MutableLiveData<Boolean>(false)
    val mypageStatus: LiveData<Boolean>
        get() = _mypageStatus

    private val _profileStatus = MutableLiveData<Boolean>(false)
    val profileStatus: LiveData<Boolean>
        get() = _profileStatus

    fun requestMypageProfile(body: MultipartBody.Part) = viewModelScope.launch(exceptionHandler) {
        RetrofitBuilder.mypageService.postProfile(
            SeeMeetSharedPreference.getToken(),
            body
        )
        _profileStatus.value = true
    }

    fun requestMyPageNameIdList(
        name: String,
        nickname: String
    ) = viewModelScope.launch(exceptionHandler) {
        _MyPageNameIdList.postValue(
            RetrofitBuilder.registerService.putRegisterNameId(
                SeeMeetSharedPreference.getToken(),
                RequestRegisterNameId(name, nickname)
            )
        )
        _mypageStatus.value = true
    }

    fun requestMyPageWithdrawal(
    ) = viewModelScope.launch(exceptionHandler) {
        RetrofitBuilder.withdrawalService.putWithdrawal(
            SeeMeetSharedPreference.getToken()
        )
        _withdrawalStatus.value = true

    }

    //fcm 토큰 삭제 요청
    fun requestPushTokenNull() = viewModelScope.launch(exceptionHandler) {
        RetrofitBuilder.mypageService.postChangePush(
            SeeMeetSharedPreference.getToken(),
            RequestChangePush(SeeMeetSharedPreference.getPushOn(), null)
        )
    }

    fun check() {
        // 이름이나 아이디 길이가 0일 때 status false
        if (mypageName.value?.length == 0) {
            status.value = false
            warning.value = "이름을 입력해주세요"
        } else if (mypageId.value?.length == 0) {
            status.value = false
            warning.value = "아이디를 입력해주세요"
        } else {
            // 길이가 7 미만일 때 status false
            if (mypageId.value?.length!! < 7) {
                status.value = false
                warning.value = "아이디는 7자 이상 입력해주세요"
            } else {
                // 불가능한 문자 입력했을 때 status false
                if (!RegisterNameIdActivity.isIdFormat(mypageId.value.toString())) {
                    status.value = false
                    warning.value = "아이디는 알파벳, 숫자, 밑줄, 마침표만 사용 가능해요"
                } else {
                    // 모두 숫자로만 이루어져있을 때 status false
                    if (RegisterNameIdActivity.isNumberFormat(mypageId.value.toString())) {
                        status.value = false
                        warning.value = "아이디는 숫자로만은 만들 수 없어요"
                    }
                    // 모두 _으로만 이루어져있을 때 status false
                    else if (RegisterNameIdActivity.is_Format(mypageId.value.toString())) {
                        status.value = false
                        warning.value = "아이디는 _로만은 만들 수 없어요"
                    }
                    // 모두 .으로만 이루어져있을 때 status false
                    else if (RegisterNameIdActivity.isdotFormat(mypageId.value.toString())) {
                        status.value = false
                        warning.value = "아이디는 마침표로만은 만들 수 없어요"
                    }
                    // 형식이 모두 올바를 때 status true
                    else {
                        status.value = true
                        warning.value = ""
                    }
                }
            }
        }
    }
}