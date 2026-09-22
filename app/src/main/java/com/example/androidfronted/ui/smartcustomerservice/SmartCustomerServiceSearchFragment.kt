package com.example.androidfronted.ui.smartcustomerservice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.example.androidfronted.ui.MainActivity
import com.example.androidfronted.ui.smartcustomerservice.screen.SmartCustomerServiceSearchScreen

/**
 * 智能客服搜索页宿主 Fragment
 */
class SmartCustomerServiceSearchFragment : Fragment() {

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
                SmartCustomerServiceSearchScreen(
                    onBackClick = {
                        parentFragmentManager.popBackStack()
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
}
