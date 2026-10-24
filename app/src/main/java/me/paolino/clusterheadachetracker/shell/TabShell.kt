package me.paolino.clusterheadachetracker.shell

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.TooltipCompat
import androidx.core.view.isVisible
import com.google.android.material.button.MaterialButton
import dev.hotwire.navigation.activities.HotwireActivity
import dev.hotwire.navigation.navigator.NavigatorHost

/**
 * The tab shell: one navigator per tab, chosen from a Material 3 floating
 * toolbar, with the app's main action as a floating action button beside it.
 * It stands in for Hotwire Native's HotwireBottomNavigationController, which
 * only drives a BottomNavigationView.
 *
 * Each tab's navigator host is added to its container the first time the
 * tab is shown, so launching loads one page, not every tab's. Android
 * restores the hosts it added when it recreates the activity, and they load
 * again at once, so no tab comes back blank (Hotwire Native 1.3.1's lazy tabs
 * did, after a theme change).
 *
 * The toolbar shows each tab as an icon, and the chosen one with its label
 * too, on a tonal pill. It hides on pushed pages, in modals, and under the
 * keyboard; the activity says when ([setShown]).
 *
 * The same class is in Chat with Work's and Cluster Headache Tracker's
 * Android apps: keep them in step.
 */
class TabShell(
    private val activity: HotwireActivity,
    private val tabs: List<ShellTab>,
    /** The row inside the floating toolbar that holds the tabs' buttons. */
    private val buttons: ViewGroup,
    /** The toolbar and the action button together, shown and hidden as one. */
    private val dock: View,
    /** A MaterialButton layout for one tab ([R.layout] in each app). */
    @LayoutRes private val buttonLayout: Int,
) {
    private val buttonFor = mutableMapOf<ShellTab, MaterialButton>()
    private val hidden = mutableSetOf<ShellTab>()
    private var shownAnimator: ValueAnimator? = null

    /** Called when the person picks another tab (not when code selects one). */
    var onTabSelected: ((ShellTab) -> Unit)? = null

    /** Called when the person picks the tab already showing. */
    var onTabReselected: ((ShellTab) -> Unit)? = null

    var selected: ShellTab = tabs.first()
        private set

    /** Whether the toolbar is up (or on its way up). */
    var isShown = true
        private set

    fun load(selected: ShellTab) {
        val inflater = LayoutInflater.from(buttons.context)
        buttons.removeAllViews()
        tabs.forEach { tab ->
            val button = inflater.inflate(buttonLayout, buttons, false) as MaterialButton
            button.isCheckable = true
            button.setIconResource(tab.icon)
            val title = activity.getString(tab.title)
            button.contentDescription = title
            TooltipCompat.setTooltipText(button, title)
            button.setOnClickListener { onClick(tab) }
            // Tabs hidden before the buttons existed (remembered from the last launch).
            button.isVisible = tab !in hidden
            buttons.addView(button)
            buttonFor[tab] = button
        }
        select(selected.takeIf { it !in hidden } ?: tabs.first { it !in hidden }, animate = false)
    }

    /** Shows [tab]'s navigator, adding its host the first time. */
    fun select(tab: ShellTab, animate: Boolean = true) {
        val previous = selected
        selected = tab
        if (animate &&
            previous != tab
        ) {
            TransitionManager.beginDelayedTransition(buttons, AutoTransition().setDuration(LABEL_MS))
        }

        // The delegate loads a host's first page when it registers while
        // current, or when it becomes current once registered.
        activity.delegate.setCurrentNavigator(
            activity.navigatorConfigurations().first {
                it.navigatorHostId ==
                    tab.hostId
            },
        )
        val fragments = activity.supportFragmentManager
        if (fragments.findFragmentById(tab.hostId) == null) {
            fragments.beginTransaction().add(tab.hostId, NavigatorHost()).commitNow()
        }
        tabs.forEach { activity.findViewById<View>(it.hostId)?.isVisible = it == tab }

        buttonFor.forEach { (each, button) -> render(button, each, checked = each == tab) }
    }

    /** Hides a tab the person can't use (Bots, for a guest), or shows it again. */
    fun setHidden(tab: ShellTab, hide: Boolean) {
        if (if (hide) !hidden.add(tab) else !hidden.remove(tab)) return
        buttonFor[tab]?.isVisible = !hide
        if (hide && selected == tab) {
            tabs.firstOrNull { it !in hidden }?.let {
                select(it)
                onTabSelected?.invoke(it)
            }
        }
    }

    fun isHidden(tab: ShellTab): Boolean = tab in hidden

    /** Slides the toolbar and its action button in or out. */
    fun setShown(shown: Boolean, animate: Boolean = true) {
        if (shown == isShown && shownAnimator == null) return
        isShown = shown
        shownAnimator?.cancel()

        val travel = dock.height.toFloat() + (dock.layoutParams as? ViewGroup.MarginLayoutParams)?.bottomMargin.orZero()
        val from = dock.translationY
        val to = if (shown) 0f else travel
        if (!animate || !dock.isLaidOut || travel == 0f) {
            dock.translationY = to
            dock.alpha = if (shown) 1f else 0f
            dock.isVisible = shown
            shownAnimator = null
            return
        }

        dock.isVisible = true
        shownAnimator =
            ValueAnimator.ofFloat(0f, 1f).apply {
                duration = SLIDE_MS
                addUpdateListener {
                    val fraction = it.animatedValue as Float
                    dock.translationY = from + (to - from) * fraction
                    dock.alpha = if (shown) fraction.coerceAtLeast(dock.alpha) else 1f - fraction
                }
                doOnEnd {
                    shownAnimator = null
                    dock.isVisible = isShown
                }
                start()
            }
    }

    private fun onClick(tab: ShellTab) {
        if (tab == selected) {
            onTabReselected?.invoke(tab)
            buttonFor[tab]?.isChecked = true
            return
        }
        select(tab)
        onTabSelected?.invoke(tab)
    }

    private fun render(button: MaterialButton, tab: ShellTab, checked: Boolean) {
        button.isChecked = checked
        // Only the chosen tab says its name; the others are icons with
        // their name as the accessibility label and tooltip.
        button.text = if (checked) activity.getString(tab.title) else null
        button.iconPadding = if (checked) (LABEL_GAP_DP * button.resources.displayMetrics.density).toInt() else 0
    }

    private fun Int?.orZero(): Int = this ?: 0

    private fun ValueAnimator.doOnEnd(action: () -> Unit) {
        addListener(
            object : AnimatorListenerAdapter() {
                private var cancelled = false

                override fun onAnimationCancel(animation: Animator) {
                    cancelled = true
                }

                override fun onAnimationEnd(animation: Animator) {
                    if (!cancelled) action()
                }
            },
        )
    }

    private companion object {
        const val SLIDE_MS = 200L
        const val LABEL_MS = 180L
        const val LABEL_GAP_DP = 8
    }
}

/**
 * One tab: its name and icon in the toolbar, and the container its navigator
 * host goes in (the navigator configuration with that host id says where it
 * starts).
 */
class ShellTab(@StringRes val title: Int, @DrawableRes val icon: Int, @IdRes val hostId: Int)
