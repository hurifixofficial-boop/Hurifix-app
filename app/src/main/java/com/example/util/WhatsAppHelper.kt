package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.model.CustomerJobEntity
import com.example.data.model.ExpertEntity

object WhatsAppHelper {
    fun normalizePhoneNumber(phone: String): String {
        val digits = phone.filter { it.isDigit() }
        return when {
            digits.length == 10 -> "91$digits"
            digits.length == 11 && digits.startsWith("0") -> "91${digits.substring(1)}"
            digits.length == 12 && digits.startsWith("91") -> digits
            else -> digits
        }
    }

    fun openWhatsAppChat(context: Context, phone: String, message: String) {
        val normalized = normalizePhoneNumber(phone)
        try {
            val encodedMsg = Uri.encode(message)
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$normalized&text=$encodedMsg")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open WhatsApp: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openWhatsAppChatWithoutMessage(context: Context, phone: String) {
        openWhatsAppChat(context, phone, "Hello")
    }

    fun openDirectWhatsApp(context: Context, phone: String, message: String = "") {
        if (message.isBlank()) {
            openWhatsAppChatWithoutMessage(context, phone)
        } else {
            openWhatsAppChat(context, phone, message)
        }
    }

    fun sendWhatsAppDirectMessage(context: Context, phone: String, message: String) {
        openWhatsAppChat(context, phone, message)
    }

    fun openDialer(context: Context, phone: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open dialer: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendWhatsAppMessageToExpert(context: Context, expert: ExpertEntity, customer: CustomerJobEntity) {
        val mapsUrl = if (customer.latitude != 0.0 && customer.longitude != 0.0) {
            LocationHelper.createGoogleMapsUrl(customer.latitude, customer.longitude)
        } else if (customer.address.isNotBlank()) {
            "https://www.google.com/maps/search/?api=1&query=${Uri.encode(customer.address)}"
        } else {
            "https://maps.google.com"
        }
        val msg = createExpertDispatchMessage(
            expertName = expert.name,
            customerName = customer.customerName,
            customerPhone = customer.customerPhone,
            serviceType = customer.serviceType,
            problemDescription = customer.issueDescription,
            address = customer.address,
            mapsUrl = mapsUrl
        )
        openWhatsAppChat(context, expert.phone, msg)
    }

    fun formatDurationHoursAndMinutes(totalMinutes: Int): String {
        val minutes = totalMinutes.coerceAtLeast(30)
        return if (minutes < 60) {
            "$minutes minutes"
        } else {
            val hours = minutes / 60
            val remMin = minutes % 60
            val hourLabel = if (hours == 1) "1 hour" else "$hours hours"
            if (remMin == 0) {
                hourLabel
            } else {
                "$hourLabel $remMin minutes"
            }
        }
    }

    fun calculateEstimatedArrivalTimeWithBuffer(distanceKm: Double): String {
        val actualMinutes = LocationHelper.estimateTravelTimeMinutes(distanceKm)
        val totalMinutes = actualMinutes + 30
        return formatDurationHoursAndMinutes(totalMinutes)
    }

    fun createCustomerAssignmentNotificationMessage(
        customerName: String,
        expertName: String,
        expertPhone: String,
        serviceType: String,
        estimatedTimeText: String
    ): String {
        return "🛠 *HURIFIX SERVICE UPDATE* 🛠\n" +
                "━━━━━━━━━━━━━━━━━━━━\n" +
                "Dear *$customerName* ✨,\n\n" +
                "An expert has been assigned to your service request:\n\n" +
                "👨‍🔧 *Expert Name:* $expertName\n" +
                "📞 *Contact Number:* $expertPhone\n" +
                "🛠 *Service Required:* $serviceType\n" +
                "⏳ *Estimated Time of Arrival:* $estimatedTimeText\n" +
                "Our expert will arrive at your address shortly. Please keep your phone reachable.\n" +
                "━━━━━━━━━━━━━━━━━━━━\n" +
                "- *Team Hurifix*\n" +
                "_Many Problems | One Solution_"
    }

    fun createExpertDispatchMessage(
        expertName: String,
        customerName: String,
        customerPhone: String,
        serviceType: String,
        problemDescription: String,
        address: String,
        mapsUrl: String
    ): String {
        val desc = if (problemDescription.isNotBlank()) problemDescription else "Standard Service Request"
        return "🔧 *HURIFIX DISPATCH ORDER* 🔧\n" +
                "━━━━━━━━━━━━━━━━━━━━\n" +
                "Hello *$expertName*, a new service task has been dispatched to you:\n\n" +
                "👤 *Customer Name:* $customerName\n" +
                "📞 *Customer Phone:* $customerPhone\n" +
                "🛠 *Service Required:* $serviceType\n" +
                "📝 *Problem Description:* $desc\n" +
                "📍 *Address:* $address\n\n" +
                "🗺 *Google Maps Location:*\n" +
                "$mapsUrl\n" +
                "━━━━━━━━━━━━━━━━━━━━\n" +
                "- *Hurifix Operations Team*"
    }

    fun createCompletionCustomerMessage(customerName: String): String {
        return "Dear $customerName✨,\n\n" +
                "Your work has been successfully completed! 🎉 We hope we met your expectations and provided a great experience. 💯\n\n" +
                "One of our representatives will call you shortly for your valuable feedback—please do share your experience with us! 📞\n\n" +
                "If you have any questions or need assistance, feel free to reach out to us anytime. 📞💬\n\n" +
                "Stay Connected! 🚀\n" +
                "Follow us on Instagram for future updates, offers, and exclusive services:\n" +
                "👇\n" +
                "https://www.instagram.com/hurifix_official?stkn=MzUzMW9xOTh2eTJ4\n\n" +
                "Thank you for choosing us! Have a great day ahead! 😊🙏"
    }

    fun createNewExpertWelcomeMessage(expertName: String, category: String = ""): String {
        return "Hello $expertName, 👋\n" +
                "Welcome to Hurifix! 🎉\n" +
                "Aap ab hamare Official Business Partner ban chuke hain. Hum aapke sath kaam karne ke liye bohot excited hain! 🛠️🚀\n" +
                "Hum aapko jald hi WhatsApp par ek message bhejenge jisme aapko hamara poora work process aur guidelines achhe se samjha di jayengi. 📲\n" +
                "Hurifix family se judne ke liye aapka bohot dhanyawad! Have a great day! 😊🙏"
    }

    fun openGoogleMaps(context: Context, latitude: Double, longitude: Double, label: String = "Location") {
        LocationHelper.openInGoogleMaps(context, latitude, longitude, label)
    }
}
