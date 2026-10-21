package com.example.util

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.CustomerJobEntity
import com.example.data.model.ExpertCategoryEntity
import com.example.data.model.ExpertEntity
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupRestoreHelper {
    data class BackupData(
        val jobs: List<CustomerJobEntity>,
        val experts: List<ExpertEntity>,
        val categories: List<ExpertCategoryEntity>
    )

    fun createBackupJson(
        jobs: List<CustomerJobEntity>,
        experts: List<ExpertEntity>,
        categories: List<ExpertCategoryEntity>
    ): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("timestamp", System.currentTimeMillis())

        val jobsArray = JSONArray()
        for (job in jobs) {
            val jobObj = JSONObject()
            jobObj.put("id", job.id)
            jobObj.put("customerName", job.customerName)
            jobObj.put("customerPhone", job.customerPhone)
            jobObj.put("serviceType", job.serviceType)
            jobObj.put("issueDescription", job.issueDescription)
            jobObj.put("address", job.address)
            jobObj.put("latitude", job.latitude)
            jobObj.put("longitude", job.longitude)
            jobObj.put("status", job.status)
            job.assignedExpertId?.let { jobObj.put("assignedExpertId", it) }
            job.assignedExpertName?.let { jobObj.put("assignedExpertName", it) }
            job.assignedExpertPhone?.let { jobObj.put("assignedExpertPhone", it) }
            job.distanceKmAtDispatch?.let { jobObj.put("distanceKmAtDispatch", it) }
            job.ratingGiven?.let { jobObj.put("ratingGiven", it) }
            job.reviewFeedback?.let { jobObj.put("reviewFeedback", it) }
            job.customerMediaUrls?.let { jobObj.put("customerMediaUrls", it) }
            job.technicianBeforePhotoUrl?.let { jobObj.put("technicianBeforePhotoUrl", it) }
            job.technicianAfterPhotoUrl?.let { jobObj.put("technicianAfterPhotoUrl", it) }
            jobObj.put("createdAt", job.createdAt)
            job.completedAt?.let { jobObj.put("completedAt", it) }
            jobObj.put("isExpertNotified", job.isExpertNotified)
            jobObj.put("isCustomerNotifiedOnAssign", job.isCustomerNotifiedOnAssign)
            jobObj.put("isCustomerNotifiedOnCompletion", job.isCustomerNotifiedOnCompletion)
            job.assignMessageLaterDismissedAt?.let { jobObj.put("assignMessageLaterDismissedAt", it) }
            jobsArray.put(jobObj)
        }
        root.put("jobs", jobsArray)

        val expertsArray = JSONArray()
        for (expert in experts) {
            val expObj = JSONObject()
            expObj.put("id", expert.id)
            expObj.put("name", expert.name)
            expObj.put("phone", expert.phone)
            expObj.put("category", expert.category)
            expObj.put("address", expert.address)
            expObj.put("latitude", expert.latitude)
            expObj.put("longitude", expert.longitude)
            expObj.put("isAvailable", expert.isAvailable)
            expObj.put("rating", expert.rating.toDouble())
            expObj.put("ratingSum", expert.ratingSum.toDouble())
            expObj.put("totalRatingsCount", expert.totalRatingsCount)
            expObj.put("completedJobsCount", expert.completedJobsCount)
            expObj.put("cancelledJobsCount", expert.cancelledJobsCount)
            expObj.put("isWelcomeMessageSent", expert.isWelcomeMessageSent)
            expObj.put("createdAt", expert.createdAt)
            expertsArray.put(expObj)
        }
        root.put("experts", expertsArray)

        val categoriesArray = JSONArray()
        for (cat in categories) {
            val catObj = JSONObject()
            catObj.put("id", cat.id)
            catObj.put("name", cat.name)
            catObj.put("isDefault", cat.isDefault)
            catObj.put("createdAt", cat.createdAt)
            categoriesArray.put(catObj)
        }
        root.put("categories", categoriesArray)

        return root.toString(2)
    }

    fun exportAndShareBackup(
        context: Context,
        jobs: List<CustomerJobEntity>,
        experts: List<ExpertEntity>,
        categories: List<ExpertCategoryEntity>
    ) {
        try {
            val json = createBackupJson(jobs, experts, categories)
            val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val filename = "hurifix_backup_$dateStr.json"
            val backupFile = File(context.cacheDir, filename)
            backupFile.writeText(json)

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", backupFile)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Hurifix Database Backup - $dateStr")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Export Backup").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            Toast.makeText(context, "Export error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    fun parseBackupJson(inputStream: InputStream): BackupData {
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        val root = JSONObject(jsonString)

        val categoriesList = mutableListOf<ExpertCategoryEntity>()
        if (root.has("categories")) {
            val arr = root.getJSONArray("categories")
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                categoriesList.add(
                    ExpertCategoryEntity(
                        id = obj.optLong("id", 0L),
                        name = obj.getString("name"),
                        isDefault = obj.optBoolean("isDefault", false),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        }

        val expertsList = mutableListOf<ExpertEntity>()
        if (root.has("experts")) {
            val arr = root.getJSONArray("experts")
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                expertsList.add(
                    ExpertEntity(
                        id = obj.optLong("id", 0L),
                        name = obj.getString("name"),
                        phone = obj.getString("phone"),
                        category = obj.getString("category"),
                        address = obj.optString("address", ""),
                        latitude = obj.optDouble("latitude", 28.57),
                        longitude = obj.optDouble("longitude", 77.32),
                        isAvailable = obj.optBoolean("isAvailable", true),
                        rating = obj.optDouble("rating", 4.8).toFloat(),
                        ratingSum = obj.optDouble("ratingSum", 4.8).toFloat(),
                        totalRatingsCount = obj.optInt("totalRatingsCount", 1),
                        completedJobsCount = obj.optInt("completedJobsCount", 0),
                        cancelledJobsCount = obj.optInt("cancelledJobsCount", 0),
                        isWelcomeMessageSent = obj.optBoolean("isWelcomeMessageSent", false),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        }

        val jobsList = mutableListOf<CustomerJobEntity>()
        if (root.has("jobs")) {
            val arr = root.getJSONArray("jobs")
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                jobsList.add(
                    CustomerJobEntity(
                        id = obj.optLong("id", 0L),
                        customerName = obj.getString("customerName"),
                        customerPhone = obj.getString("customerPhone"),
                        serviceType = obj.getString("serviceType"),
                        issueDescription = obj.optString("issueDescription", ""),
                        address = obj.optString("address", ""),
                        latitude = obj.optDouble("latitude", 28.57),
                        longitude = obj.optDouble("longitude", 77.32),
                        status = obj.optString("status", "PENDING"),
                        assignedExpertId = if (obj.isNull("assignedExpertId")) null else obj.optLong("assignedExpertId"),
                        assignedExpertName = if (obj.isNull("assignedExpertName")) null else obj.optString("assignedExpertName"),
                        assignedExpertPhone = if (obj.isNull("assignedExpertPhone")) null else obj.optString("assignedExpertPhone"),
                        distanceKmAtDispatch = if (obj.isNull("distanceKmAtDispatch")) null else obj.optDouble("distanceKmAtDispatch"),
                        ratingGiven = if (obj.isNull("ratingGiven")) null else obj.optDouble("ratingGiven").toFloat(),
                        reviewFeedback = if (obj.isNull("reviewFeedback")) null else obj.optString("reviewFeedback"),
                        customerMediaUrls = if (obj.isNull("customerMediaUrls")) null else obj.optString("customerMediaUrls"),
                        technicianBeforePhotoUrl = if (obj.isNull("technicianBeforePhotoUrl")) null else obj.optString("technicianBeforePhotoUrl"),
                        technicianAfterPhotoUrl = if (obj.isNull("technicianAfterPhotoUrl")) null else obj.optString("technicianAfterPhotoUrl"),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        completedAt = if (obj.isNull("completedAt")) null else obj.optLong("completedAt"),
                        isExpertNotified = obj.optBoolean("isExpertNotified", false),
                        isCustomerNotifiedOnAssign = obj.optBoolean("isCustomerNotifiedOnAssign", false),
                        isCustomerNotifiedOnCompletion = obj.optBoolean("isCustomerNotifiedOnCompletion", false),
                        assignMessageLaterDismissedAt = if (obj.isNull("assignMessageLaterDismissedAt")) null else obj.optLong("assignMessageLaterDismissedAt")
                    )
                )
            }
        }

        return BackupData(jobsList, expertsList, categoriesList)
    }
}
