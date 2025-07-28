package me.paolino.clusterheadachetracker.bridge

import android.content.Intent
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.sp
import dev.hotwire.core.bridge.BridgeComponent
import dev.hotwire.core.bridge.BridgeDelegate
import dev.hotwire.core.bridge.Message
import dev.hotwire.navigation.destinations.HotwireDestination
import dev.hotwire.navigation.fragments.HotwireFragment
import kotlinx.serialization.Serializable

class ShareComponent(name: String, private val bridgeDelegate: BridgeDelegate<HotwireDestination>) :
    BridgeComponent<HotwireDestination>(name, bridgeDelegate) {
    private val buttonId = 1
    private val fragment: HotwireFragment
        get() = bridgeDelegate.destination.fragment as HotwireFragment

    override fun onReceive(message: Message) {
        when (message.event) {
            "connect" -> addButton(message)
            "disconnect" -> removeButton()
            else -> Log.w("ShareComponent", "Unknown event for message: $message")
        }
    }

    private fun addButton(message: Message) {
        removeButton()
        val data = message.data<MessageData>() ?: return

        val composeView = ComposeView(fragment.requireContext()).apply {
            id = buttonId
            setContent {
                ToolbarButton(
                    onClick = { share(data.url) },
                )
            }
        }
        val layoutParams = Toolbar.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        ).apply { gravity = Gravity.END }

        val toolbar = fragment.toolbarForNavigation()
        toolbar?.addView(composeView, layoutParams)
    }

    private fun removeButton() {
        val toolbar = fragment.toolbarForNavigation()
        val button = toolbar?.findViewById<ComposeView>(buttonId)
        toolbar?.removeView(button)
    }

    private fun share(url: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, url)
        }
        fragment.requireActivity().startActivity(Intent.createChooser(intent, "Share via"))
    }

    @Serializable
    data class MessageData(val url: String)
}

@Composable
@Suppress("FunctionNaming")
private fun ToolbarButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.Black,
        ),
    ) {
        // Using text fallback until Material Symbols font is added
        Text(
            text = "Share",
            fontSize = 14.sp,
        )
    }
}
