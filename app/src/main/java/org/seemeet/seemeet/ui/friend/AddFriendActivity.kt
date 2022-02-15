package org.seemeet.seemeet.ui.friend

import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import org.seemeet.seemeet.databinding.ActivityAddFriendBinding
import org.seemeet.seemeet.ui.viewmodel.AddFriendViewModel

class AddFriendActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddFriendBinding
    private val viewModel: AddFriendViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddFriendBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initClickListener()
    }

    private fun initClickListener() {
        // x버튼
        binding.ivFriendAddBack.setOnClickListener {
            finish()
        }

        // 입력창 리스너 (x버튼)
        binding.etSearchFriendId.addTextChangedListener {
            if (binding.etSearchFriendId.text.isNullOrBlank()) { //공백일 때
                setVisibility(binding.ivFriendIdRemoveAll, View.GONE)
            } else {
                setVisibility(binding.ivFriendIdRemoveAll, View.VISIBLE)
                binding.ivFriendIdRemoveAll.setOnClickListener {
                    binding.etSearchFriendId.text = null
                }
            }
        }

        // 검색 enter 버튼
        binding.etSearchFriendId.setOnEditorActionListener { _, action, _ ->
            var handled = false
            if (action == EditorInfo.IME_ACTION_DONE) {
                val email = binding.etSearchFriendId.text
                viewModel.requestUserList(email)
                handled = true
                setVisibility(binding.tvSearchFriendNull, View.VISIBLE)
                setUserObserver()
                setVisibility(binding.clFriendSearchList, View.GONE)
            }
            handled
        }

        // 친구 추가 버튼
        binding.ivAddFriendList.setOnClickListener {
            val email = binding.etSearchFriendId.text
            viewModel.requestAddFriend(email)
            setAddFriendObserver()
        }
    }

    private fun setUserObserver() {
        viewModel.userList.observe(this, Observer { userData ->
            binding.apply {
                setVisibility(binding.clFriendSearchList, View.VISIBLE)
                tvSearchFriendEmail.text = userData.data.email
                tvSearchFriendName.text = userData.data.username
            }
        })
    }

    private fun setAddFriendObserver() {
        viewModel.userList.observe(this, Observer {
            binding.ivAddFriendList.isSelected = true
        })
    }

    private fun setVisibility(view: View, visibility: Int){
        view.visibility = visibility
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        val focusView = currentFocus
        if (focusView != null && ev != null) {
            val rect = Rect()
            focusView.getGlobalVisibleRect(rect)
            val x = ev.x.toInt()
            val y = ev.y.toInt()

            if (!rect.contains(x, y)) {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm?.hideSoftInputFromWindow(focusView.windowToken, 0)
                focusView.clearFocus()
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}