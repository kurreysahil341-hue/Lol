package com.example.engine

import com.example.BuildConfig
import com.example.data.db.ContactAliasEntity
import com.example.data.model.ActionType
import com.example.data.model.AssistantAction
import com.example.data.model.ParseResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class GeminiPart(val text: String? = null)

data class GeminiContent(val parts: List<GeminiPart>)

data class GeminiRequest(
    val contents: List<GeminiContent>
)

data class GeminiCandidate(val content: GeminiContent)

data class GeminiResponse(val candidates: List<GeminiCandidate>? = null)

interface GeminiRestService {
    @POST("v1beta/models/gemini-1.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

class AssistantNlpEngine {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    private val geminiService = retrofit.create(GeminiRestService::class.java)

    /**
     * Parses a spoken text prompt (in English, Hindi, or Hinglish).
     */
    suspend fun parseCommand(
        rawInput: String,
        contactsCache: List<ContactAliasEntity> = emptyList()
    ): ParseResult = withContext(Dispatchers.Default) {
        val cleanInput = rawInput
            .replace("AI Assistant", "", ignoreCase = true)
            .replace("Assistant", "", ignoreCase = true)
            .replace("hey", "", ignoreCase = true)
            .replace("ok", "", ignoreCase = true)
            .trim()

        if (cleanInput.isEmpty()) {
            return@withContext ParseResult(
                action = AssistantAction(
                    type = ActionType.GENERAL_AI,
                    rawCommand = rawInput,
                    feedbackMessage = "Aapka kya hukum hai? Main aapki help ke liye tayaar hu."
                ),
                confidence = 1.0f,
                sourceEngine = "RuleEngine"
            )
        }

        // Step 1: Fast Rule-based parser
        val ruleAction = parseWithRules(cleanInput, rawInput, contactsCache)
        if (ruleAction != null) {
            return@withContext ParseResult(
                action = ruleAction,
                confidence = 0.95f,
                sourceEngine = "RuleEngine"
            )
        }

        // Step 2: Gemini AI fallback for complex sentences
        val geminiApiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
        if (geminiApiKey.isNotBlank() && geminiApiKey != "MY_GEMINI_API_KEY") {
            val aiAction = parseWithGemini(cleanInput, rawInput, geminiApiKey)
            if (aiAction != null) {
                return@withContext ParseResult(
                    action = aiAction,
                    confidence = 0.90f,
                    sourceEngine = "GeminiAI"
                )
            }
        }

        // Step 3: General Fallback Response
        ParseResult(
            action = AssistantAction(
                type = ActionType.GENERAL_AI,
                rawCommand = rawInput,
                feedbackMessage = "Main samajh gaya: '$cleanInput'. Iske liye main app search kar rha hu."
            ),
            confidence = 0.6f,
            sourceEngine = "Fallback"
        )
    }

    private fun parseWithRules(
        input: String,
        rawInput: String,
        contactsCache: List<ContactAliasEntity>
    ): AssistantAction? {
        val lower = input.lowercase()

        // Personal Memory System (Preferences & Notes)
        if (lower.contains("yaad rakho") || lower.contains("remember that") || lower.contains("mera favourite") || lower.contains("mera favorite")) {
            val note = lower.replace("yaad rakho", "").replace("remember that", "").trim()
            return AssistantAction(
                type = ActionType.SAVE_PREFERENCE,
                targetApp = "Memory",
                rawCommand = rawInput,
                feedbackMessage = "Aapki preference secure memory me save kar li gayi hai: '$rawInput'."
            )
        }

        if (lower.contains("kya yaad hai") || lower.contains("mera preference") || lower.contains("kya pasand hai") || lower.contains("what do you remember")) {
            return AssistantAction(
                type = ActionType.RECALL_PREFERENCE,
                targetApp = "Memory",
                rawCommand = rawInput,
                feedbackMessage = "Aapki memory me saved hain: Frequent contacts (Papa, Rahul), Favourite songs (Arijit Singh) aur Voice Preferences."
            )
        }

        // Voice Teacher Mode - Step by Step Voice Guide
        if (lower.contains("teacher") || lower.contains("sikhao") || lower.contains("samjhao") || lower.contains("kaise chalaye") || lower.contains("guide karo") || lower.contains("kaise kare")) {
            val topic = when {
                lower.contains("whatsapp") -> "WhatsApp chalane ke liye: Pehle WhatsApp app icon par tap karein, fir contact chunein aur type karein ya mic dabakar bolein."
                lower.contains("call") || lower.contains("phone") -> "Call karne ke liye: 'AI Assistant Papa ko call karo' kahein, ya Phone app kholkar contact chun kar dial button dabayein."
                lower.contains("camera") || lower.contains("photo") -> "Photo khichne ke liye: 'Camera kholo' bolein, fir shutter button par click karein."
                lower.contains("wifi") || lower.contains("internet") -> "Wi-Fi chalu karne ke liye: Settings me Wi-Fi option me jayein ya bolin 'Wi-Fi settings kholo'."
                else -> "Main aapka Voice Teacher hu. Aap bolkar phone call, WhatsApp, Camera, Maps aur settings bina touchscreen chhuye chala sakte hain!"
            }
            return AssistantAction(
                type = ActionType.TEACHER_MODE,
                targetApp = "VoiceTeacher",
                rawCommand = rawInput,
                feedbackMessage = topic
            )
        }

        // Alarm & Reminder
        if (lower.contains("alarm") || lower.contains("reminder")) {
            val timeString = if (lower.contains("7") || lower.contains("saat")) "7:00 AM" else "8:00 AM"
            return AssistantAction(
                type = ActionType.SET_ALARM_REMINDER,
                targetApp = "Clock",
                rawCommand = rawInput,
                feedbackMessage = "Kal subah $timeString ka alarm set ho gaya hai."
            )
        }

        // Mobile Problem Solver Mode
        if (lower.contains("problem") || lower.contains("issue") || lower.contains("khatam") || lower.contains("nahi chal raha") || lower.contains("full") || lower.contains("trouble") || lower.contains("troble")) {
            val solution = when {
                lower.contains("battery") -> "Battery Samasya Samadhan: 1. Screen brightness kam karein. 2. Unused background apps band karein. 3. Settings -> Battery me Power Saving Mode chalu karein."
                lower.contains("storage") || lower.contains("space") || lower.contains("memory") -> "Storage Full Samadhan: 1. Unnecessary WhatsApp videos/photos delete karein. 2. Settings -> Storage me Cached Data clear karein. 3. Google Photos me backup karke local photos clean karein."
                lower.contains("wifi") || lower.contains("net") || lower.contains("internet") -> "Internet/Wi-Fi Samadhan: 1. Airplane Mode 5 sec ke liye ON karke OFF karein. 2. Wi-Fi router restart karein. 3. Mobile data toggle off-on karein."
                lower.contains("bluetooth") -> "Bluetooth Samadhan: 1. Bluetooth OFF karke dobara ON karein. 2. Device ko Unpair karke dobara Pair karein."
                lower.contains("notification") -> "Notification Samadhan: 1. Settings -> Apps me jayein. 2. Apne app par 'Allow Notifications' toggle verify karein. 3. Do Not Disturb Mode OFF rakhein."
                else -> "Mobile System Assistant: Main aapki Battery, Storage, Wi-Fi, Bluetooth aur Notifications ki sabhi samasyaon me madad kar sakta hu. Bol kar batayein konsi dikkat hai."
            }
            return AssistantAction(
                type = ActionType.PROBLEM_SOLVER,
                targetApp = "ProblemSolver",
                rawCommand = rawInput,
                feedbackMessage = solution
            )
        }

        // 1. YouTube Search / Play
        if (lower.contains("youtube") || lower.contains("song") || lower.contains("gana") || lower.contains("gaana")) {
            val query = lower
                .replace("youtube", "")
                .replace("me", "")
                .replace("mein", "")
                .replace("par", "")
                .replace("search karo", "")
                .replace("search", "")
                .replace("play", "")
                .replace("kholo", "")
                .replace("chalo", "")
                .replace("songs", "")
                .replace("song", "")
                .replace("gana", "")
                .replace("gaana", "")
                .trim()
            val targetQuery = if (query.isNotBlank()) query else "Arijit Singh songs"
            return AssistantAction(
                type = ActionType.YOUTUBE_SEARCH,
                targetApp = "YouTube",
                searchQuery = targetQuery,
                rawCommand = rawInput,
                feedbackMessage = "YouTube me search kar raha hu: '$targetQuery'"
            )
        }

        // 2. Google Maps / Navigation / Nearby
        if (lower.contains("map") || lower.contains("maps") || lower.contains("navigate") || lower.contains("navigation") || lower.contains("rasta") || lower.contains("location")) {
            if (lower.contains("nearest") || lower.contains("paas me") || lower.contains("kareeb")) {
                val category = when {
                    lower.contains("hospital") || lower.contains("aspatal") -> "Hospital"
                    lower.contains("petrol") || lower.contains("pump") -> "Petrol Pump"
                    lower.contains("atm") || lower.contains("bank") -> "ATM"
                    lower.contains("railway") || lower.contains("station") -> "Railway Station"
                    lower.contains("bus") -> "Bus Stand"
                    else -> "Hospital"
                }
                return AssistantAction(
                    type = ActionType.MAPS_NEARBY,
                    targetApp = "Google Maps",
                    locationQuery = category,
                    rawCommand = rawInput,
                    feedbackMessage = "Nearest $category search kar raha hu Maps me."
                )
            }

            val locQuery = lower
                .replace("google maps", "")
                .replace("maps", "")
                .replace("map", "")
                .replace("me", "")
                .replace("mein", "")
                .replace("par", "")
                .replace("search karo", "")
                .replace("search", "")
                .replace("navigation start karo", "")
                .replace("navigation", "")
                .replace("start", "")
                .replace("dikhao", "")
                .replace("kholo", "")
                .replace("rasta", "")
                .trim()
            val targetLoc = if (locQuery.isNotBlank()) locQuery else "Bilaspur"
            val actionType = if (lower.contains("navigate") || lower.contains("navigation") || lower.contains("rasta")) ActionType.MAPS_NAVIGATE else ActionType.MAPS_SEARCH

            return AssistantAction(
                type = actionType,
                targetApp = "Google Maps",
                locationQuery = targetLoc,
                rawCommand = rawInput,
                feedbackMessage = "Google Maps me $targetLoc search/navigation start kar raha hu."
            )
        }

        // 3. Phone Call / Dialer
        if (lower.contains("call") || lower.contains("phone karo") || lower.contains("dial")) {
            if (lower.contains("emergency") || lower.contains("police") || lower.contains("112") || lower.contains("100")) {
                return AssistantAction(
                    type = ActionType.EMERGENCY_CALL,
                    phoneNumber = "112",
                    targetName = "Emergency Services",
                    rawCommand = rawInput,
                    feedbackMessage = "Emergency call initiation sequence activated.",
                    requiresConfirmation = true
                )
            }

            val targetName = lower
                .replace("call karo", "")
                .replace("call", "")
                .replace("ko", "")
                .replace("phone karo", "")
                .replace("dial", "")
                .trim()

            if (targetName.isBlank()) {
                return AssistantAction(
                    type = ActionType.GENERAL_AI,
                    rawCommand = rawInput,
                    feedbackMessage = "Aap kisse call karna chahte hain? Kripya contact ka naam batayein."
                )
            }

            val matchedContact = contactsCache.firstOrNull { it.aliasName.equals(targetName, ignoreCase = true) }
            val num = matchedContact?.phoneNumber ?: getSampleNumberForName(targetName)

            return AssistantAction(
                type = ActionType.CALL_CONTACT,
                targetName = targetName.replaceFirstChar { it.uppercase() },
                phoneNumber = num,
                rawCommand = rawInput,
                feedbackMessage = "$targetName ko call kar raha hu."
            )
        }

        // 4. SMS Messaging
        if (lower.contains("sms") || (lower.contains("message") && !lower.contains("whatsapp"))) {
            val targetName = lower.replace("sms", "").replace("message", "").replace("bhejo", "").replace("karo", "").trim()
            val extractedName = if (targetName.isNotBlank()) targetName.replaceFirstChar { it.uppercase() } else "Rahul"
            val num = getSampleNumberForName(extractedName)

            return AssistantAction(
                type = ActionType.SEND_SMS,
                targetName = extractedName,
                phoneNumber = num,
                messageText = "Hello from AI Assistant 2.0",
                rawCommand = rawInput,
                feedbackMessage = "$extractedName ko SMS app me message compose kar raha hu."
            )
        }

        // 5. WhatsApp Messaging / Calling
        if (lower.contains("whatsapp") || lower.contains("what's app")) {
            val extractedName = when {
                lower.contains("rahul") -> "Rahul"
                lower.contains("papa") -> "Papa"
                lower.contains("mom") || lower.contains("mummy") -> "Mummy"
                else -> "Friend"
            }
            val messageText = if (lower.contains("message")) {
                input.substringAfter("message", "").replace("bhejo", "").replace("karo", "").trim()
            } else "Hello"

            return AssistantAction(
                type = ActionType.WHATSAPP_MESSAGE,
                targetApp = "WhatsApp",
                targetName = extractedName,
                phoneNumber = getSampleNumberForName(extractedName),
                messageText = if (messageText.isNotBlank()) messageText else "Hello from AI Assistant 2.0",
                rawCommand = rawInput,
                feedbackMessage = "WhatsApp me $extractedName ko message bhej raha hu."
            )
        }

        // 5. Camera & Photos
        if (lower.contains("camera") || lower.contains("photo") || lower.contains("picture") || lower.contains("video")) {
            if (lower.contains("video") || lower.contains("record")) {
                return AssistantAction(
                    type = ActionType.RECORD_VIDEO,
                    targetApp = "Camera",
                    rawCommand = rawInput,
                    feedbackMessage = "Video recorder open kar raha hu."
                )
            }
            return AssistantAction(
                type = ActionType.OPEN_CAMERA,
                targetApp = "Camera",
                rawCommand = rawInput,
                feedbackMessage = "Camera open kar raha hu."
            )
        }

        // 6. Settings & System Toggles
        if (lower.contains("wifi") || lower.contains("wi-fi")) {
            return AssistantAction(
                type = ActionType.WIFI_CONTROL,
                targetApp = "wifi",
                rawCommand = rawInput,
                feedbackMessage = "WiFi Settings open kar raha hu."
            )
        }

        if (lower.contains("bluetooth")) {
            return AssistantAction(
                type = ActionType.BLUETOOTH_CONTROL,
                targetApp = "bluetooth",
                rawCommand = rawInput,
                feedbackMessage = "Bluetooth Settings open kar raha hu."
            )
        }

        if (lower.contains("brightness") || lower.contains("display")) {
            val percent = lower.replace("[^0-9]".toRegex(), "").toIntOrNull() ?: 40
            return AssistantAction(
                type = ActionType.BRIGHTNESS_CONTROL,
                targetApp = "display",
                numericValue = percent,
                rawCommand = rawInput,
                feedbackMessage = "Display Brightness settings adjust kar raha hu ($percent%)."
            )
        }

        if (lower.contains("sound") || lower.contains("volume") || lower.contains("awaz")) {
            return AssistantAction(
                type = ActionType.VOLUME_CONTROL,
                targetApp = "sound",
                rawCommand = rawInput,
                feedbackMessage = "Sound & Volume settings open kar raha hu."
            )
        }

        if (lower.contains("battery")) {
            return AssistantAction(
                type = ActionType.SYSTEM_SETTING,
                targetApp = "battery",
                rawCommand = rawInput,
                feedbackMessage = "Battery Settings open kar raha hu."
            )
        }

        if (lower.contains("storage")) {
            return AssistantAction(
                type = ActionType.SYSTEM_SETTING,
                targetApp = "storage",
                rawCommand = rawInput,
                feedbackMessage = "Storage Settings open kar raha hu."
            )
        }

        if (lower.contains("drive") || lower.contains("google drive")) {
            return AssistantAction(
                type = ActionType.DRIVE_SEARCH,
                targetApp = "Google Drive",
                rawCommand = rawInput,
                feedbackMessage = "Google Drive open kar raha hu."
            )
        }

        if (lower.contains("file") || lower.contains("files") || lower.contains("document")) {
            return AssistantAction(
                type = ActionType.FILES_OPEN,
                targetApp = "Files",
                rawCommand = rawInput,
                feedbackMessage = "Device Files Manager open kar raha hu."
            )
        }

        if (lower.contains("settings") || lower.contains("setting")) {
            return AssistantAction(
                type = ActionType.SYSTEM_SETTING,
                targetApp = "settings",
                rawCommand = rawInput,
                feedbackMessage = "Android System Settings khol raha hu."
            )
        }

        // 7. Security / Dangerous Actions
        if (lower.contains("delete file") || lower.contains("file delete") || lower.contains("format") || lower.contains("reset") || lower.contains("paisa bhejo") || lower.contains("send money")) {
            val actType = when {
                lower.contains("format") || lower.contains("reset") -> ActionType.FACTORY_RESET
                lower.contains("send money") || lower.contains("paisa") -> ActionType.SEND_MONEY
                else -> ActionType.DELETE_FILE
            }
            return AssistantAction(
                type = actType,
                rawCommand = rawInput,
                feedbackMessage = "Kripya security confirmation check karein. Kya aap ye sensitive action perform karna chahte hain?",
                requiresConfirmation = true
            )
        }

        // 8. General Open App (Chrome, Drive, Files, Calculator, Gmail, etc.)
        if (lower.contains("kholo") || lower.contains("open") || lower.contains("launch")) {
            val appName = lower
                .replace("kholo", "")
                .replace("open", "")
                .replace("launch", "")
                .trim()
            if (appName.isNotBlank()) {
                return AssistantAction(
                    type = ActionType.OPEN_APP,
                    targetApp = appName,
                    rawCommand = rawInput,
                    feedbackMessage = "$appName application open kar raha hu."
                )
            }
        }

        return null
    }

    private suspend fun parseWithGemini(
        cleanInput: String,
        rawInput: String,
        apiKey: String
    ): AssistantAction? {
        return try {
            val prompt = """
                System: You are AI Assistant 2.0. Analyze the user command in Hindi/English/Hinglish: "$cleanInput".
                Map it to one action type from: [OPEN_APP, CALL_CONTACT, YOUTUBE_SEARCH, MAPS_SEARCH, MAPS_NAVIGATE, WHATSAPP_MESSAGE, OPEN_CAMERA, SYSTEM_SETTING, GENERAL_AI].
                Respond in plain text formatted as: ACTION_TYPE|TARGET|PARAMS|FEEDBACK
                Example: YOUTUBE_SEARCH|YouTube|Arijit Singh|Arijit Singh ke gaane chala raha hu.
            """.trimIndent()

            val req = GeminiRequest(contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))))
            val resp = geminiService.generateContent(apiKey, req)
            val outputText = resp.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: return null

            val parts = outputText.trim().split("|")
            if (parts.size >= 4) {
                val actionTypeName = parts[0].trim()
                val target = parts[1].trim()
                val param = parts[2].trim()
                val feedback = parts[3].trim()

                val actionType = try { ActionType.valueOf(actionTypeName) } catch (e: Exception) { ActionType.GENERAL_AI }

                AssistantAction(
                    type = actionType,
                    targetApp = target,
                    searchQuery = param,
                    locationQuery = param,
                    phoneNumber = getSampleNumberForName(target),
                    rawCommand = rawInput,
                    feedbackMessage = feedback
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun getSampleNumberForName(name: String): String {
        return when (name.lowercase()) {
            "papa", "dad" -> "+919876543210"
            "rahul" -> "+919876543211"
            "mummy", "mom" -> "+919876543212"
            else -> "+919876500000"
        }
    }
}
