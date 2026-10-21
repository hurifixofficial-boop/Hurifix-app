package com.example

import com.example.util.LocationHelper
import com.example.util.NetworkErrorHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class BackendAndLocationUpdatesTest {

    @Test
    fun testRawCoordinatesParsing() {
        val coords = LocationHelper.parseCoordinatesFromText("28.5708, 77.3261")
        assertNotNull(coords)
        assertEquals(28.5708, coords!!.first, 0.0001)
        assertEquals(77.3261, coords.second, 0.0001)
    }

    @Test
    fun testGoogleMapsQueryUrlParsing() {
        val url = "https://maps.google.com/?q=28.5708,77.3261"
        assertTrue(LocationHelper.isGoogleMapsUrl(url))
        val coords = LocationHelper.parseCoordinatesFromText(url)
        assertNotNull(coords)
        assertEquals(28.5708, coords!!.first, 0.0001)
        assertEquals(77.3261, coords.second, 0.0001)
    }

    @Test
    fun testGoogleMapsPlusUrlParsing() {
        val url = "https://maps.google.com/?q=28.5708+77.3261"
        assertTrue(LocationHelper.isGoogleMapsUrl(url))
        val coords = LocationHelper.parseCoordinatesFromText(url)
        assertNotNull(coords)
        assertEquals(28.5708, coords!!.first, 0.0001)
        assertEquals(77.3261, coords.second, 0.0001)
    }

    @Test
    fun testGoogleMapsAtUrlParsing() {
        val url = "https://www.google.com/maps/@28.5708,77.3261,16z"
        assertTrue(LocationHelper.isGoogleMapsUrl(url))
        val coords = LocationHelper.parseCoordinatesFromText(url)
        assertNotNull(coords)
        assertEquals(28.5708, coords!!.first, 0.0001)
        assertEquals(77.3261, coords.second, 0.0001)
    }

    @Test
    fun testGoogleMapsPlaceUrlParsing() {
        val url = "https://www.google.com/maps/place/28.5708,77.3261"
        assertTrue(LocationHelper.isGoogleMapsUrl(url))
        val coords = LocationHelper.parseCoordinatesFromText(url)
        assertNotNull(coords)
        assertEquals(28.5708, coords!!.first, 0.0001)
        assertEquals(77.3261, coords.second, 0.0001)
    }

    @Test
    fun testGoogleMapsShortUrlDetection() {
        val shortUrl1 = "https://goo.gl/maps/abc123xyz"
        val shortUrl2 = "https://maps.app.goo.gl/987xyz"
        assertTrue(LocationHelper.isGoogleMapsUrl(shortUrl1))
        assertTrue(LocationHelper.isGoogleMapsUrl(shortUrl2))
        assertFalse(LocationHelper.isGoogleMapsUrl("https://example.com/location"))
    }

    @Test
    fun testNetworkErrorDetection() {
        val dnsException = UnknownHostException("Unable to resolve host: firestore.googleapis.com")
        val connectException = ConnectException("Failed to connect to host")
        val timeoutException = SocketTimeoutException("connect timed out")

        assertTrue(NetworkErrorHelper.isNetworkOrUnavailableError(dnsException))
        assertTrue(NetworkErrorHelper.isNetworkOrUnavailableError(connectException))
        assertTrue(NetworkErrorHelper.isNetworkOrUnavailableError(timeoutException))

        assertEquals("Please check your internet connection", NetworkErrorHelper.formatErrorMessage(dnsException))
        assertEquals("Please check your internet connection", NetworkErrorHelper.formatErrorMessage(connectException))

        val otherException = IllegalArgumentException("Invalid password entered")
        assertFalse(NetworkErrorHelper.isNetworkOrUnavailableError(otherException))
        assertEquals("Invalid password entered", NetworkErrorHelper.formatErrorMessage(otherException))
    }
}
