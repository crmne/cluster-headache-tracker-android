package me.paolino.clusterheadachetracker.bridge

import kotlinx.serialization.json.Json
import me.paolino.clusterheadachetracker.bridge.AppButtonComponent.Action
import me.paolino.clusterheadachetracker.bridge.AppButtonComponent.MessageData
import org.junit.Assert.assertEquals
import org.junit.Test

class AppButtonComponentTest {
    @Test
    fun nativeActionDecidesWhateverTheTitleSays() {
        assertEquals(Action.PRINT, MessageData(title = "Drucken", nativeAction = "print").action)
        assertEquals(Action.SIGN_OUT, MessageData(title = "Abmelden", nativeAction = "sign-out").action)
        assertEquals(Action.SPONSOR, MessageData(title = "Unterstützen", nativeAction = "sponsor").action)
        assertEquals(Action.DEFAULT, MessageData(title = "Neu", imageName = "add").action)
    }

    @Test
    fun decodesNativeActionFromTheBridgeMessage() {
        val json = Json { ignoreUnknownKeys = true }
        val data = json.decodeFromString(
            MessageData.serializer(),
            """
            {"title":"Abmelden","androidImage":null,"color":null,
             "nativeAction":"sign-out","metadata":{"url":"/settings"}}
            """.trimIndent(),
        )

        assertEquals(Action.SIGN_OUT, data.action)
    }

    @Test
    fun androidImageIdentifiesNativeActionsInAnyLanguage() {
        assertEquals(Action.PRINT, MessageData(title = "Drucken", imageName = "print").action)
        assertEquals(Action.SIGN_OUT, MessageData(title = "Esci", imageName = "logout").action)
    }

    @Test
    fun englishTitlesStillWorkForPagesWithoutAnImage() {
        assertEquals(Action.PRINT, MessageData(title = "Print").action)
        assertEquals(Action.SIGN_OUT, MessageData(title = "Sign Out").action)
        assertEquals(Action.SPONSOR, MessageData(title = "Sponsor").action)
    }

    @Test
    fun everythingElseClicksTheWebElement() {
        assertEquals(Action.DEFAULT, MessageData(title = "New").action)
        assertEquals(Action.DEFAULT, MessageData(title = "Neu", imageName = "add").action)
    }
}
