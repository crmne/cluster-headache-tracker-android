package me.paolino.clusterheadachetracker.bridge

import me.paolino.clusterheadachetracker.bridge.AppButtonComponent.Action
import me.paolino.clusterheadachetracker.bridge.AppButtonComponent.MessageData
import org.junit.Assert.assertEquals
import org.junit.Test

class AppButtonComponentTest {
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
