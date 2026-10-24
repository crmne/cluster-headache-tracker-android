package me.paolino.clusterheadachetracker

import dev.hotwire.navigation.navigator.NavigatorConfiguration
import me.paolino.clusterheadachetracker.shell.ShellTab

/**
 * The floating toolbar's tabs. Logging an attack isn't a tab: it's the
 * floating button beside them, which opens the form over whatever tab is
 * showing.
 */
object MainTabs {
    private class Tab(val name: String, val shellTab: ShellTab, val startLocation: String)

    private val tabs = listOf(
        Tab("logs", ShellTab(R.string.tab_logs, R.drawable.ic_tab_logs, R.id.logs_navigator_host), AppRoutes.logsUrl),
        Tab(
            "charts",
            ShellTab(R.string.tab_charts, R.drawable.ic_tab_charts, R.id.charts_navigator_host),
            AppRoutes.chartsUrl,
        ),
        Tab(
            "account",
            ShellTab(R.string.tab_account, R.drawable.ic_tab_account, R.id.account_navigator_host),
            AppRoutes.accountUrl,
        ),
        Tab(
            "feedback",
            ShellTab(R.string.tab_feedback, R.drawable.ic_tab_feedback, R.id.feedback_navigator_host),
            AppRoutes.feedbackUrl,
        ),
    )

    val shellTabs: List<ShellTab> = tabs.map { it.shellTab }

    val logs: ShellTab = shellTabs.first()

    val navigatorConfigurations: List<NavigatorConfiguration> = tabs.map {
        NavigatorConfiguration(name = it.name, navigatorHostId = it.shellTab.hostId, startLocation = it.startLocation)
    }

    fun named(name: String?): ShellTab? = tabs.firstOrNull { it.name == name }?.shellTab

    fun nameOf(tab: ShellTab): String = tabs.first { it.shellTab === tab }.name
}
