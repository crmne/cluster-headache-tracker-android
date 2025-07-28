package me.paolino.clusterheadachetracker

import dev.hotwire.navigation.navigator.NavigatorConfiguration
import dev.hotwire.navigation.tabs.HotwireBottomTab

private val logs = HotwireBottomTab(
    title = "Logs",
    iconResId = R.drawable.ic_tab_calendar,
    configuration = NavigatorConfiguration(
        name = "logs",
        navigatorHostId = R.id.logs_navigator_host,
        startLocation = "${AppConfig.baseUrl}/headache_logs",
    ),
)

private val charts = HotwireBottomTab(
    title = "Charts",
    iconResId = R.drawable.ic_tab_chart,
    configuration = NavigatorConfiguration(
        name = "charts",
        navigatorHostId = R.id.charts_navigator_host,
        startLocation = "${AppConfig.baseUrl}/charts",
    ),
)

private val new = HotwireBottomTab(
    title = "New",
    iconResId = R.drawable.ic_tab_add,
    configuration = NavigatorConfiguration(
        name = "new",
        navigatorHostId = R.id.new_navigator_host,
        startLocation = "${AppConfig.baseUrl}/headache_logs",
    ),
)

private val account = HotwireBottomTab(
    title = "Account",
    iconResId = R.drawable.ic_tab_person,
    configuration = NavigatorConfiguration(
        name = "account",
        navigatorHostId = R.id.account_navigator_host,
        startLocation = "${AppConfig.baseUrl}/settings",
    ),
)

private val feedback = HotwireBottomTab(
    title = "Feedback",
    iconResId = R.drawable.ic_tab_message,
    configuration = NavigatorConfiguration(
        name = "feedback",
        navigatorHostId = R.id.feedback_navigator_host,
        startLocation = "${AppConfig.baseUrl}/feedback",
    ),
)

val mainTabs = listOf(
    logs,
    charts,
    new,
    account,
    feedback,
)
