package com.example

import com.example.data.model.ExpertEntity
import com.example.data.model.RankedExpert
import com.example.util.LocationHelper
import com.example.util.WhatsAppHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DispatchLogicTest {

    @Test
    fun testHaversineDistanceCalculation() {
        // Distance between Sector 18 (28.5708, 77.3261) and Sector 22 (28.5925, 77.3392) ~ 2.7 km
        val distance = LocationHelper.calculateDistanceKm(28.5708, 77.3261, 28.5925, 77.3392)
        assertTrue(distance > 2.0 && distance < 4.0)

        // Same location distance should be 0.0
        val zeroDist = LocationHelper.calculateDistanceKm(28.5708, 77.3261, 28.5708, 77.3261)
        assertEquals(0.0, zeroDist, 0.001)
    }

    @Test
    fun testTravelTimeEstimate() {
        val timeFor5Km = LocationHelper.estimateTravelTimeMinutes(5.0)
        assertTrue(timeFor5Km in 10..20)
    }

    @Test
    fun testCoordinateParsingFromGoogleMapsUrl() {
        val url = "https://maps.google.com/?q=28.5708,77.3261"
        val coords = LocationHelper.parseCoordinatesFromText(url)
        assertNotNull(coords)
        assertEquals(28.5708, coords!!.first, 0.0001)
        assertEquals(77.3261, coords.second, 0.0001)

        val urlEncoded = "https://maps.google.com/?q=28.5708%2C77.3261"
        val coordsEncoded = LocationHelper.parseCoordinatesFromText(urlEncoded)
        assertNotNull(coordsEncoded)
        assertEquals(28.5708, coordsEncoded!!.first, 0.0001)
        assertEquals(77.3261, coordsEncoded.second, 0.0001)

        val urlLoc = "https://maps.google.com/?q=loc:28.5708+77.3261"
        val coordsLoc = LocationHelper.parseCoordinatesFromText(urlLoc)
        assertNotNull(coordsLoc)
        assertEquals(28.5708, coordsLoc!!.first, 0.0001)
        assertEquals(77.3261, coordsLoc.second, 0.0001)

        val urlSearch = "https://www.google.com/maps/search/?api=1&query=28.5708,77.3261"
        val coordsSearch = LocationHelper.parseCoordinatesFromText(urlSearch)
        assertNotNull(coordsSearch)
        assertEquals(28.5708, coordsSearch!!.first, 0.0001)
        assertEquals(77.3261, coordsSearch.second, 0.0001)

        val urlAt = "https://www.google.com/maps/@28.5708,77.3261,15z"
        val coordsAt = LocationHelper.parseCoordinatesFromText(urlAt)
        assertNotNull(coordsAt)
        assertEquals(28.5708, coordsAt!!.first, 0.0001)
        assertEquals(77.3261, coordsAt.second, 0.0001)

        val urlProto = "https://www.google.com/maps/place/Shop/@28.5708,77.3261,17z/data=!3d28.5708!4d77.3261"
        val coordsProto = LocationHelper.parseCoordinatesFromText(urlProto)
        assertNotNull(coordsProto)
        assertEquals(28.5708, coordsProto!!.first, 0.0001)
        assertEquals(77.3261, coordsProto.second, 0.0001)

        assertTrue(LocationHelper.isGoogleMapsUrl("https://goo.gl/maps/xyz"))
        assertTrue(LocationHelper.isGoogleMapsUrl("https://maps.app.goo.gl/abc"))
        assertTrue(LocationHelper.isGoogleMapsUrl("https://maps.google.com/?q=28.57,77.32"))

        val rawCoords = "28.6280, 77.3650"
        val parsedRaw = LocationHelper.parseCoordinatesFromText(rawCoords)
        assertNotNull(parsedRaw)
        assertEquals(28.6280, parsedRaw!!.first, 0.0001)
        assertEquals(77.3650, parsedRaw.second, 0.0001)
    }

    @Test
    fun testSoundManagerMethods() {
        // SoundHelper should execute cleanly without throwing
        com.example.util.SoundHelper.playSFX("click")
        com.example.util.SoundHelper.playSFX("success")
        com.example.util.SoundHelper.playSFX("error")
    }

    @Test
    fun testPhoneFormattingForWhatsApp() {
        val formatted = WhatsAppHelper.normalizePhoneNumber("9876543210")
        assertEquals("919876543210", formatted)

        val alreadyWithCode = WhatsAppHelper.normalizePhoneNumber("+919876543210")
        assertEquals("919876543210", alreadyWithCode)
    }

    @Test
    fun testOrderNumbering() {
        val job1 = com.example.data.model.CustomerJobEntity(id = 1, serviceType = "Electrician", customerName = "Test", customerPhone = "9876543210")
        assertEquals("001", job1.orderNumberFormatted)

        val job2 = com.example.data.model.CustomerJobEntity(id = 25, serviceType = "Plumber", customerName = "Test", customerPhone = "9876543210")
        assertEquals("025", job2.orderNumberFormatted)

        val job3 = com.example.data.model.CustomerJobEntity(id = 100, serviceType = "Electrician", customerName = "Test", customerPhone = "9876543210")
        assertEquals("100", job3.orderNumberFormatted)
    }

    @Test
    fun testAscendingSortOrder() {
        val expert1 = ExpertEntity(id = 1, name = "Far Expert", phone = "111", category = "AC", address = "Far", latitude = 28.7, longitude = 77.5)
        val expert2 = ExpertEntity(id = 2, name = "Near Expert", phone = "222", category = "AC", address = "Near", latitude = 28.571, longitude = 77.327)

        val customerLat = 28.5708
        val customerLng = 77.3261

        val rankedList = listOf(expert1, expert2).map {
            val dist = LocationHelper.calculateDistanceKm(customerLat, customerLng, it.latitude, it.longitude)
            val time = LocationHelper.estimateTravelTimeMinutes(dist)
            RankedExpert(it, dist, time)
        }.sortedBy { it.distanceKm }

        // Closest expert must be #1
        assertEquals("Near Expert", rankedList.first().expert.name)
        assertTrue(rankedList.first().distanceKm < rankedList.last().distanceKm)
    }
}
