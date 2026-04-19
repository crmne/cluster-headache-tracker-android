package me.paolino.clusterheadachetracker

import dev.hotwire.navigation.navigator.NavigatorConfiguration
import dev.hotwire.navigation.tabs.HotwireBottomTab

object MainTabs {
    const val logsIndex = 0
    const val chartsIndex = 1
    const val newIndex = 2
    const val accountIndex = 3
    const val feedbackIndex = 4
    const val defaultIndex = logsIndex

    val all = listOf(
        HotwireBottomTab(
            title = "Logs",
            iconResId = R.drawable.ic_tab_calendar,
            configuration = NavigatorConfiguration(
                name = "logs",
                navigatorHostId = R.id.logs_navigator_host,
                startLocation = AppRoutes.logsUrl,
            ),
        ),
        HotwireBottomTab(
            title = "Charts",
            iconResId = R.drawable.ic_tab_chart,
            configuration = NavigatorConfiguration(
                name = "charts",
                navigatorHostId = R.id.charts_navigator_host,
                startLocation = AppRoutes.chartsUrl,
            ),
        ),
        HotwireBottomTab(
            title = "New",
            iconResId = R.drawable.ic_tab_add,
            configuration = NavigatorConfiguration(
                name = "new",
                navigatorHostId = R.id.new_navigator_host,
                startLocation = AppRoutes.logsUrl,
            ),
        ),
        HotwireBottomTab(
            title = "Account",
            iconResId = R.drawable.ic_tab_person,
            configuration = NavigatorConfiguration(
                name = "account",
                navigatorHostId = R.id.account_navigator_host,
                startLocation = AppRoutes.accountUrl,
            ),
        ),
        HotwireBottomTab(
            title = "Feedback",
            iconResId = R.drawable.ic_tab_message,
            configuration = NavigatorConfiguration(
                name = "feedback",
                navigatorHostId = R.id.feedback_navigator_host,
                startLocation = AppRoutes.feedbackUrl,
            ),
        ),
    )

    fun isActionTab(index: Int): Boolean = index == newIndex
}
