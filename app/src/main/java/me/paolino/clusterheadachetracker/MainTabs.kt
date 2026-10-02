package me.paolino.clusterheadachetracker

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import dev.hotwire.navigation.navigator.NavigatorConfiguration
import dev.hotwire.navigation.tabs.HotwireBottomTab

object MainTabs {
    const val LOGS_INDEX = 0
    const val CHARTS_INDEX = 1
    const val NEW_INDEX = 2
    const val ACCOUNT_INDEX = 3
    const val FEEDBACK_INDEX = 4
    const val DEFAULT_INDEX = LOGS_INDEX

    private data class Tab(
        val name: String,
        @StringRes val titleResId: Int,
        @DrawableRes val iconResId: Int,
        @IdRes val navigatorHostId: Int,
        val startLocation: String,
    )

    private val tabs = listOf(
        Tab("logs", R.string.tab_logs, R.drawable.ic_tab_calendar, R.id.logs_navigator_host, AppRoutes.logsUrl),
        Tab("charts", R.string.tab_charts, R.drawable.ic_tab_chart, R.id.charts_navigator_host, AppRoutes.chartsUrl),
        // Action tab: never stays selected, it opens the new-log form on the current tab instead.
        Tab("new", R.string.tab_new, R.drawable.ic_tab_add, R.id.new_navigator_host, AppRoutes.logsUrl),
        Tab(
            "account",
            R.string.tab_account,
            R.drawable.ic_tab_person,
            R.id.account_navigator_host,
            AppRoutes.accountUrl,
        ),
        Tab(
            "feedback",
            R.string.tab_feedback,
            R.drawable.ic_tab_message,
            R.id.feedback_navigator_host,
            AppRoutes.feedbackUrl,
        ),
    )

    fun all(context: Context): List<HotwireBottomTab> = tabs.map { tab ->
        HotwireBottomTab(
            title = context.getString(tab.titleResId),
            iconResId = tab.iconResId,
            configuration = NavigatorConfiguration(
                name = tab.name,
                navigatorHostId = tab.navigatorHostId,
                startLocation = tab.startLocation,
            ),
        )
    }

    fun isActionTab(index: Int): Boolean = index == NEW_INDEX
}
