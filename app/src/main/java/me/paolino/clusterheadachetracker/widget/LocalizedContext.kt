package me.paolino.clusterheadachetracker.widget

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/** Renders widget and shortcut text in the language the user picked in the web app, when it sent one. */
fun Context.localized(languageTag: String?): Context {
    if (languageTag == null) return this
    val configuration = Configuration(resources.configuration).apply { setLocale(Locale.forLanguageTag(languageTag)) }
    return createConfigurationContext(configuration)
}
