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
import com.example.androidfronted.ui.MainActivity
import com.example.androidfronted.ui.smartcustomerservice.screen.SmartCustomerServiceScreen
import com.example.androidfronted.viewmodel.smartcustomerservice.ChatViewModel

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
    
    fun setOnBackCallback(callback: () -> Unit) {
        onBackCallback = callback
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
                    onBackClick = {
                        if (onBackCallback != null) {
                            onBackCallback?.invoke()
                        } else {
                            parentFragmentManager.popBackStack()
                        }
                    }
                )
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.setBottomNavigationVisible(false)
    }
    
    override fun onPause() {
        super.onPause()
        (activity as? MainActivity)?.setBottomNavigationVisible(true)
    }
    
    companion object {
        fun newInstance(): SmartCustomerServiceFragment {
            return SmartCustomerServiceFragment()
        }
    }
}
