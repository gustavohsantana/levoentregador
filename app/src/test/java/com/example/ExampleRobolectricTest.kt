package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.api.RouteParser
import com.example.data.local.DriverDatabase
import com.example.data.local.PendingStopActionEntity
import com.example.data.model.DriverCoordinates
import com.example.data.model.DriverStop
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `verify app name resource`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Levô Entregador", appName)
    }

    @Test
    fun `verify demo route model creation`() {
        val route = RouteParser.createDemoRoute("test_token")
        assertEquals(4, route.stops.size)
        assertEquals("Pizzaria Bella Nápoles", route.establishmentName)
        assertTrue(route.stops[0].isPending)
        assertEquals("R$ 38,50", route.stops[0].formattedAmount)
        assertEquals("8 min", route.stops[0].formattedEta)
        assertNotNull(route.stops[0].coordinates)
    }

    @Test
    fun `verify stop status helpers and open completed states`() {
        val pendingStop = DriverStop(id = "1", position = 1, customerName = "Cliente 1", address = "Rua A", status = "PENDING")
        assertTrue(pendingStop.isPending)
        assertTrue(pendingStop.isOpen)
        assertFalse(pendingStop.isCompleted)

        val inProgressStop = DriverStop(id = "2", position = 2, customerName = "Cliente 2", address = "Rua B", status = "IN_PROGRESS")
        assertTrue(inProgressStop.isInProgress)
        assertTrue(inProgressStop.isOpen)
        assertFalse(inProgressStop.isCompleted)

        val deliveredStop = DriverStop(id = "3", position = 3, customerName = "Cliente 3", address = "Rua C", status = "DELIVERED")
        assertTrue(deliveredStop.isDelivered)
        assertFalse(deliveredStop.isOpen)
        assertTrue(deliveredStop.isCompleted)

        val failedStop = DriverStop(id = "4", position = 4, customerName = "Cliente 4", address = "Rua D", status = "FAILED", failureReason = "Ausente")
        assertTrue(failedStop.isFailed)
        assertFalse(failedStop.isOpen)
        assertTrue(failedStop.isCompleted)
        assertEquals("Ausente", failedStop.failureReason)
    }

    @Test
    fun `verify code requirement logic across stop and route levels`() {
        // Stop with no specific setting (null) -> inherits from route
        val defaultStop = DriverStop(id = "1", position = 1, customerName = "Cliente 1", address = "Rua A", exigeCodigo = null)
        assertFalse(defaultStop.requiresConfirmationCode(routeDefault = false))
        assertTrue(defaultStop.requiresConfirmationCode(routeDefault = true))

        // Stop explicitly requiring code -> always requires code regardless of route
        val stopWithCode = DriverStop(id = "2", position = 2, customerName = "Cliente 2", address = "Rua B", exigeCodigo = true)
        assertTrue(stopWithCode.requiresConfirmationCode(routeDefault = false))
        assertTrue(stopWithCode.requiresConfirmationCode(routeDefault = true))

        // Stop explicitly excused from code (e.g. doorman) -> does not require code
        val stopExcused = DriverStop(id = "3", position = 3, customerName = "Cliente 3", address = "Rua C", exigeCodigo = false)
        assertFalse(stopExcused.requiresConfirmationCode(routeDefault = false))
        assertFalse(stopExcused.requiresConfirmationCode(routeDefault = true))
    }

    @Test
    fun `verify route completion and progress fractions`() {
        val route = RouteParser.createDemoRoute("test_token")
        assertEquals(0, route.completedStopsCount)
        assertEquals(4, route.pendingStops.size)
        assertEquals(0f, route.progressFraction)

        // Mark 2 delivered
        val halfRoute = route.copy(
            stops = route.stops.mapIndexed { index, stop ->
                if (index < 2) stop.copy(status = "DELIVERED") else stop
            }
        )
        assertEquals(2, halfRoute.completedStopsCount)
        assertEquals(2, halfRoute.pendingStops.size)
        assertEquals(0.5f, halfRoute.progressFraction)

        // Mark all delivered
        val fullRoute = route.copy(
            stops = route.stops.map { it.copy(status = "DELIVERED") }
        )
        assertEquals(4, fullRoute.completedStopsCount)
        assertEquals(0, fullRoute.pendingStops.size)
        assertEquals(1.0f, fullRoute.progressFraction)
    }

    @Test
    fun `verify room pending action insert retrieve and delete`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = DriverDatabase.getDatabase(context)
        val dao = db.driverDao()

        // Clean previous test data
        val initial = dao.getAllPendingActions()
        initial.forEach { dao.deletePendingAction(it.id) }

        val id1 = dao.insertPendingAction(
            PendingStopActionEntity(
                token = "test_token",
                stopId = "stop_1",
                outcome = "DELIVERED",
                deliveryCode = "1234",
                occurredAt = "2026-09-20T21:00:00Z"
            )
        )
        assertTrue(id1 > 0)

        val id2 = dao.insertPendingAction(
            PendingStopActionEntity(
                token = "test_token",
                stopId = "stop_2",
                outcome = "FAILED",
                reason = "Cliente ausente",
                occurredAt = "2026-09-20T21:15:00Z"
            )
        )
        assertTrue(id2 > 0)

        val pending = dao.getAllPendingActions()
        assertEquals(2, pending.size)
        assertEquals("DELIVERED", pending[0].outcome)
        assertEquals("1234", pending[0].deliveryCode)
        assertEquals("FAILED", pending[1].outcome)
        assertEquals("Cliente ausente", pending[1].reason)

        dao.deletePendingAction(id1)
        dao.deletePendingAction(id2)
        val afterDelete = dao.getAllPendingActions()
        assertEquals(0, afterDelete.size)
    }

    @Test
    fun `verify coordinates and formatted amounts`() {
        val stop = DriverStop(
            id = "stop_custom",
            position = 1,
            customerName = "Cliente Teste",
            address = "Av Paulista, 1000",
            amountCents = 4550,
            coordinates = DriverCoordinates(lat = -23.561, lng = -46.655)
        )

        assertEquals("R$ 45,50", stop.formattedAmount)
        assertEquals(-23.561, stop.coordinates?.lat ?: 0.0, 0.0001)
        assertEquals(-46.655, stop.coordinates?.lng ?: 0.0, 0.0001)
    }
}
