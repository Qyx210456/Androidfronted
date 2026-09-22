package com.example.androidfronted.ui.smartcustomerservice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.androidfronted.R
import com.example.androidfronted.ui.MainActivity
import com.example.androidfronted.ui.smartcustomerservice.screen.SmartCustomerServiceScreen
import com.example.androidfronted.viewmodel.smartcustomerservice.ChatViewModel
import com.example.androidfronted.util.FloatingBallManager

class SmartCustomerServiceFragment : Fragment() {
    
    private val viewModel: ChatViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ChatViewModel(requireActivity().application) as T
            }
        }
    }
    
    private var onBackCallback: (() -> Unit)? = null
    private var finishOnBack: Boolean = false

    /** 从搜索页返回时侧边栏保持展开（在跳转搜索前置 true，onResume 复位） */
    private var startWithDrawerOpen = false
    
    fun setOnBackCallback(callback: () -> Unit) {
        onBackCallback = callback
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        finishOnBack = arguments?.getBoolean(ARG_FINISH_ON_BACK, false) ?: false
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activity?.window?.let { window ->
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
        
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                SmartCustomerServiceScreen(
                    viewModel = viewModel,
                    startDrawerOpen = startWithDrawerOpen,
                    onBackClick = {
                        if (onBackCallback != null) {
                            onBackCallback?.invoke()
                        } else if (parentFragmentManager.backStackEntryCount > 0) {
                            parentFragmentManager.popBackStack()
                        } else {
                            activity?.finish()
                        }
                    },
                    onSearchClick = {
                        startWithDrawerOpen = true
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.container, SmartCustomerServiceSearchFragment())
                            .addToBackStack(null)
                            .commit()
                    }
                )
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.setBottomNavigationVisible(false)
        // 标志位只在返回重建 View 时生效一次
        startWithDrawerOpen = false
    }
    
    override fun onPause() {
        super.onPause()
        (activity as? MainActivity)?.setBottomNavigationVisible(true)
    }
    
    companion object {
        private const val ARG_FINISH_ON_BACK = "finish_on_back"
        
        fun newInstance(finishOnBack: Boolean = false): SmartCustomerServiceFragment {
            return SmartCustomerServiceFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_FINISH_ON_BACK, finishOnBack)
                }
            }
        }
    }
}
