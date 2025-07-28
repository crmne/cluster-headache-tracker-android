package me.paolino.clusterheadachetracker.bridge

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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class ButtonComponent(name: String, private val bridgeDelegate: BridgeDelegate<HotwireDestination>) :
    BridgeComponent<HotwireDestination>(name, bridgeDelegate) {

    companion object {
        private const val TAG = "ButtonComponent"
        private const val BUTTON_ID_START = 100
    }

    private var buttonId = BUTTON_ID_START // Start with a high ID to avoid conflicts
    private val fragment: HotwireFragment
        get() = bridgeDelegate.destination.fragment as HotwireFragment

    override fun onReceive(message: Message) {
        when (message.event) {
            "connect" -> addButton(message)
            "disconnect" -> removeButton()
            else -> Log.w(TAG, "Unknown event for message: $message")
        }
    }

    private fun addButton(message: Message) {
        removeButton()
        val data = message.data<MessageData>() ?: return

        // Generate unique ID for this button
        buttonId++
        val currentButtonId = buttonId

        val composeView = ComposeView(fragment.requireContext()).apply {
            id = currentButtonId
            setContent {
                ToolbarButton(
                    title = data.title,
                    androidImage = data.androidImage,
                    onClick = { performClick() },
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
        // Remove all button views we've added
        for (id in BUTTON_ID_START..buttonId) {
            val button = toolbar?.findViewById<ComposeView>(id)
            toolbar?.removeView(button)
        }
    }

    private fun performClick() {
        replyTo("connect")
    }

    @Serializable
    data class MessageData(
        @SerialName("title") val title: String,
        @SerialName("iosImage") val iosImage: String? = null,
        @SerialName("androidImage") val androidImage: String? = null,
    )
}

@Composable
@Suppress("FunctionNaming")
private fun ToolbarButton(
    title: String,
    @Suppress("UNUSED_PARAMETER") androidImage: String? = null,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.Black,
        ),
    ) {
        // For now, just show text until Material Symbols font is added
        Text(
            text = title,
            fontSize = 14.sp,
        )
    }
}
