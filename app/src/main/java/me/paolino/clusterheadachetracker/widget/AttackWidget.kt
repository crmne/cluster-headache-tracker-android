package me.paolino.clusterheadachetracker.widget

import android.content.Context
import android.os.SystemClock
import android.text.format.DateFormat
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.components.FilledButton
import androidx.glance.appwidget.components.OutlineButton
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.width
import androidx.glance.layout.wrapContentSize
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import me.paolino.clusterheadachetracker.AppRoutes
import me.paolino.clusterheadachetracker.DeepLinks
import me.paolino.clusterheadachetracker.R
import java.time.Instant
import java.time.ZoneId
import java.util.Date

/**
 * Home-screen widget: a live timer while an attack is ongoing, otherwise days attack-free, with one-tap
 * buttons into quick logging and the current-attack screen. It only shows what the web app last reported
 * through the `widget-status` bridge component; it never talks to the server.
 */
class AttackWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Responsive(setOf(COMPACT, WIDE))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val store = WidgetStatusStore(context)

        provideContent {
            val snapshot by store.snapshots().collectAsState(initial = store.load())
            GlanceTheme {
                WidgetContent(snapshot)
            }
        }
    }

    companion object {
        val COMPACT = DpSize(110.dp, 110.dp)
        val WIDE = DpSize(250.dp, 110.dp)
    }
}

@Composable
private fun WidgetContent(snapshot: WidgetSnapshot?) {
    val context = LocalContext.current.localized(snapshot?.locale)
    val wide = LocalSize.current.width >= AttackWidget.WIDE.width
    val now = Instant.now()

    Scaffold(horizontalPadding = 12.dp) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.Start,
        ) {
            when {
                snapshot == null -> NoStatus(context)
                snapshot.ongoing -> OngoingAttack(context, snapshot, now)
                else -> AttackFree(context, snapshot, now)
            }
            Spacer(GlanceModifier.height(8.dp))
            Actions(context, ongoing = snapshot?.ongoing == true, wide = wide)
        }
    }
}

@Composable
private fun OngoingAttack(context: Context, snapshot: WidgetSnapshot, now: Instant) {
    Caption(context.getString(R.string.widget_attack_ongoing))
    snapshot.elapsed(now)?.let { elapsed ->
        val timer = RemoteViews(context.packageName, R.layout.widget_chronometer).apply {
            setChronometer(R.id.chronometer, SystemClock.elapsedRealtime() - elapsed.toMillis(), null, true)
        }
        AndroidRemoteViews(timer, modifier = GlanceModifier.wrapContentSize())
    }
    snapshot.startedAt?.let { startedAt ->
        val time = DateFormat.getTimeFormat(context).format(Date.from(startedAt))
        Caption(context.getString(R.string.widget_started_at, time))
    }
}

@Composable
private fun AttackFree(context: Context, snapshot: WidgetSnapshot, now: Instant) {
    val zone = ZoneId.systemDefault()

    if (snapshot.lastAttackAt == null) {
        Caption(context.getString(R.string.widget_no_attacks_logged))
    } else {
        val days = snapshot.attackFreeDays(now, zone)
        val label = context.resources.getQuantityString(R.plurals.widget_days_attack_free, days, days)
        Row(
            modifier = GlanceModifier.semantics { contentDescription = label },
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = days.toString(),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(GlanceModifier.width(6.dp))
            Caption(context.resources.getQuantityString(R.plurals.widget_days_attack_free_label, days))
        }
    }

    val attacksToday = snapshot.attacksToday(now, zone)
    if (attacksToday > 0) {
        Caption(context.resources.getQuantityString(R.plurals.widget_attacks_today, attacksToday, attacksToday))
    }
}

@Composable
private fun NoStatus(context: Context) {
    Caption(context.getString(R.string.widget_open_app_to_sync))
}

@Composable
private fun Actions(context: Context, ongoing: Boolean, wide: Boolean) {
    val logAttack = open(context, AppRoutes.QUICK_LOG_PATH)
    val currentAttack = open(context, AppRoutes.CURRENT_ATTACK_PATH)

    if (ongoing) {
        FilledButton(
            text = context.getString(R.string.action_end_attack),
            onClick = currentAttack,
            modifier = GlanceModifier.fillMaxWidth(),
        )
    } else if (wide) {
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            FilledButton(
                text = context.getString(R.string.action_log_attack),
                onClick = logAttack,
                modifier = GlanceModifier.defaultWeight(),
            )
            Spacer(GlanceModifier.width(8.dp))
            OutlineButton(
                text = context.getString(R.string.action_start_attack),
                contentColor = GlanceTheme.colors.primary,
                onClick = currentAttack,
                modifier = GlanceModifier.defaultWeight(),
            )
        }
    } else {
        FilledButton(
            text = context.getString(R.string.action_log_attack),
            onClick = logAttack,
            modifier = GlanceModifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun Caption(text: String) {
    Text(
        text = text,
        maxLines = 2,
        style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 14.sp),
    )
}

private fun open(context: Context, path: String): Action = actionStartActivity(DeepLinks.intent(context, path))
