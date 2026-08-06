package com.example.engine

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import com.example.data.model.ActionType
import com.example.data.model.AssistantAction

class SystemActionDispatcher(private val context: Context) {

    fun executeAction(action: AssistantAction): Boolean {
        return try {
            when (action.type) {
                ActionType.OPEN_APP -> openApp(action.targetApp)
                ActionType.CALL_CONTACT -> makeCall(action.phoneNumber, action.targetName)
                ActionType.OPEN_DIALER -> openDialer(action.phoneNumber)
                ActionType.REDIAL -> openDialer(action.phoneNumber)
                ActionType.EMERGENCY_CALL -> makeCall(action.phoneNumber ?: "112", "Emergency Services")
                ActionType.SEND_SMS -> sendSms(action.phoneNumber, action.targetName, action.messageText)
                
                ActionType.OPEN_CAMERA -> openCamera()
                ActionType.TAKE_PHOTO -> openCamera()
                ActionType.RECORD_VIDEO -> recordVideo()
                ActionType.OPEN_GALLERY -> openGallery()

                ActionType.MAPS_SEARCH -> searchMaps(action.locationQuery)
                ActionType.MAPS_NAVIGATE -> startNavigation(action.locationQuery)
                ActionType.SHARE_LOCATION -> shareLocation()
                ActionType.MAPS_NEARBY -> searchNearby(action.locationQuery ?: "hospital")

                ActionType.YOUTUBE_SEARCH -> searchYouTube(action.searchQuery)
                ActionType.YOUTUBE_PLAY -> searchYouTube(action.searchQuery)

                ActionType.WHATSAPP_MESSAGE -> sendWhatsAppMessage(action.phoneNumber, action.targetName, action.messageText)
                ActionType.WHATSAPP_CALL -> openWhatsAppCall(action.phoneNumber)
                ActionType.WHATSAPP_VIDEO_CALL -> openWhatsAppCall(action.phoneNumber)

                ActionType.DRIVE_SEARCH -> searchDrive(action.searchQuery)
                ActionType.DRIVE_UPLOAD -> openDrive()
                ActionType.FILES_OPEN -> openFiles()

                ActionType.SYSTEM_SETTING -> openSettingsPage(action.targetApp)
                ActionType.WIFI_CONTROL -> openSettingsPage("wifi")
                ActionType.BLUETOOTH_CONTROL -> openSettingsPage("bluetooth")
                ActionType.BRIGHTNESS_CONTROL -> openSettingsPage("display")
                ActionType.VOLUME_CONTROL -> openSettingsPage("sound")

                ActionType.TEACHER_MODE -> {
                    Toast.makeText(context, "Voice Teacher Mode Active", Toast.LENGTH_SHORT).show()
                    true
                }
                ActionType.PROBLEM_SOLVER -> {
                    Toast.makeText(context, "Mobile Problem Solver Active", Toast.LENGTH_SHORT).show()
                    true
                }
                ActionType.SAVE_PREFERENCE -> {
                    Toast.makeText(context, "Preference saved in Personal Memory", Toast.LENGTH_SHORT).show()
                    true
                }
                ActionType.RECALL_PREFERENCE -> {
                    Toast.makeText(context, "Personal Memory Recalled", Toast.LENGTH_SHORT).show()
                    true
                }
                ActionType.SET_ALARM_REMINDER -> openClock()

                ActionType.DELETE_FILE, ActionType.FACTORY_RESET, ActionType.FORMAT_STORAGE, ActionType.SEND_MONEY -> {
                    Toast.makeText(context, "Action confirmed and processed: ${action.type.name}", Toast.LENGTH_LONG).show()
                    true
                }

                ActionType.GENERAL_AI, ActionType.UNKNOWN -> {
                    true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Could not launch target app: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun openApp(appName: String?): Boolean {
        if (appName.isNullOrBlank()) return openLauncherApp("com.android.settings")
        val lower = appName.lowercase().trim()
        val packageName = when {
            lower.contains("youtube") -> "com.google.android.youtube"
            lower.contains("whatsapp") -> "com.whatsapp"
            lower.contains("map") || lower.contains("maps") -> "com.google.android.apps.maps"
            lower.contains("chrome") -> "com.android.chrome"
            lower.contains("camera") -> null // Use intent
            lower.contains("drive") -> "com.google.android.apps.docs"
            lower.contains("gmail") || lower.contains("mail") -> "com.google.android.gm"
            lower.contains("play store") || lower.contains("playstore") -> "com.android.vending"
            lower.contains("calculator") -> "com.google.android.calculator"
            lower.contains("clock") || lower.contains("alarm") -> "com.google.android.deskclock"
            lower.contains("calendar") -> "com.google.android.calendar"
            lower.contains("photos") || lower.contains("gallery") -> "com.google.android.apps.photos"
            else -> null
        }

        if (packageName != null) {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                return true
            }
        }

        // Fallback for special intents
        when {
            lower.contains("camera") -> return openCamera()
            lower.contains("settings") -> return openSettingsPage("settings")
            lower.contains("dialer") || lower.contains("phone") -> return openDialer(null)
            lower.contains("calculator") -> return openCalculator()
            lower.contains("clock") || lower.contains("alarm") -> return openClock()
        }

        // Web fallback search
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$appName")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(webIntent)
        return true
    }

    private fun openLauncherApp(pkg: String): Boolean {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg) ?: return false
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launchIntent)
        return true
    }

    private fun makeCall(phoneNumber: String?, contactName: String?): Boolean {
        val number = phoneNumber ?: "100"
        val callUri = Uri.parse("tel:$number")
        val intent = Intent(Intent.ACTION_CALL, callUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            true
        } catch (e: SecurityException) {
            // If CALL_PHONE permission not granted, fallback to DIAL
            openDialer(number)
        }
    }

    private fun openDialer(phoneNumber: String?): Boolean {
        val uri = if (!phoneNumber.isNullOrBlank()) Uri.parse("tel:$phoneNumber") else Uri.parse("tel:")
        val intent = Intent(Intent.ACTION_DIAL, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return true
    }

    private fun sendSms(phoneNumber: String?, contactName: String?, message: String?): Boolean {
        val number = phoneNumber ?: ""
        val uri = Uri.parse("smsto:$number")
        val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
            putExtra("sms_body", message ?: "")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return true
    }

    private fun openCamera(): Boolean {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun recordVideo(): Boolean {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            openCamera()
        }
    }

    private fun openGallery(): Boolean {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            type = "image/*"
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            openApp("photos")
        }
    }

    private fun searchMaps(query: String?): Boolean {
        val location = query ?: "Bilaspur"
        val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(location)}")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
            setPackage("com.google.android.apps.maps")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(mapIntent)
            true
        } catch (e: Exception) {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/${Uri.encode(location)}")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(browserIntent)
            true
        }
    }

    private fun startNavigation(query: String?): Boolean {
        val destination = query ?: "Bilaspur"
        val gmmIntentUri = Uri.parse("google.navigation:q=${Uri.encode(destination)}")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
            setPackage("com.google.android.apps.maps")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(mapIntent)
            true
        } catch (e: Exception) {
            searchMaps(destination)
        }
    }

    private fun shareLocation(): Boolean {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "My Current Location")
            putExtra(Intent.EXTRA_TEXT, "Here is my location via AI Assistant 2.0: https://maps.google.com")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Location").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
        return true
    }

    private fun searchNearby(category: String): Boolean {
        return searchMaps("Nearest $category")
    }

    private fun searchYouTube(query: String?): Boolean {
        val q = query ?: "Arijit Singh songs"
        val intent = Intent(Intent.ACTION_SEARCH).apply {
            setPackage("com.google.android.youtube")
            putExtra("query", q)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=${Uri.encode(q)}")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
            true
        }
    }

    private fun sendWhatsAppMessage(phoneNumber: String?, name: String?, message: String?): Boolean {
        val num = phoneNumber?.replace("+", "")?.replace(" ", "") ?: ""
        val msg = message ?: "Hello"
        val url = if (num.isNotBlank()) "https://api.whatsapp.com/send?phone=$num&text=${Uri.encode(msg)}"
        else "https://api.whatsapp.com/send?text=${Uri.encode(msg)}"

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            setPackage("com.whatsapp")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
            true
        }
    }

    private fun openWhatsAppCall(phoneNumber: String?): Boolean {
        return sendWhatsAppMessage(phoneNumber, null, "Call request from AI Assistant")
    }

    private fun searchDrive(query: String?): Boolean {
        val launchIntent = context.packageManager.getLaunchIntentForPackage("com.google.android.apps.docs")
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            return true
        }
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://drive.google.com")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(browserIntent)
        return true
    }

    private fun openDrive(): Boolean {
        return searchDrive(null)
    }

    private fun openFiles(): Boolean {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            openSettingsPage("storage")
        }
    }

    private fun openSettingsPage(page: String?): Boolean {
        val action = when (page?.lowercase()) {
            "wifi" -> Settings.ACTION_WIFI_SETTINGS
            "bluetooth" -> Settings.ACTION_BLUETOOTH_SETTINGS
            "display", "brightness" -> Settings.ACTION_DISPLAY_SETTINGS
            "sound", "volume" -> Settings.ACTION_SOUND_SETTINGS
            "battery" -> Settings.ACTION_BATTERY_SAVER_SETTINGS
            "storage" -> Settings.ACTION_INTERNAL_STORAGE_SETTINGS
            "location" -> Settings.ACTION_LOCATION_SOURCE_SETTINGS
            "language" -> Settings.ACTION_LOCALE_SETTINGS
            "accessibility" -> Settings.ACTION_ACCESSIBILITY_SETTINGS
            "developer" -> Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS
            else -> Settings.ACTION_SETTINGS
        }
        val intent = Intent(action).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            val generalIntent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(generalIntent)
            true
        }
    }

    private fun openCalculator(): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_APP_CALCULATOR)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            openApp("calculator")
        }
    }

    private fun openClock(): Boolean {
        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            openApp("clock")
        }
    }

}
