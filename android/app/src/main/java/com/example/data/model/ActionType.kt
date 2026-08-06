package com.example.data.model

enum class ActionType(val category: String, val requiresConfirmation: Boolean = false) {
    // System & Apps
    OPEN_APP("System", false),
    SYSTEM_SETTING("Settings", false),
    WIFI_CONTROL("Settings", false),
    BLUETOOTH_CONTROL("Settings", false),
    BRIGHTNESS_CONTROL("Settings", false),
    VOLUME_CONTROL("Settings", false),

    // Communication
    CALL_CONTACT("Phone", false),
    OPEN_DIALER("Phone", false),
    REDIAL("Phone", false),
    EMERGENCY_CALL("Phone", true),
    SEND_SMS("SMS", false),
    WHATSAPP_MESSAGE("WhatsApp", false),
    WHATSAPP_CALL("WhatsApp", false),
    WHATSAPP_VIDEO_CALL("WhatsApp", false),

    // Media & Camera
    OPEN_CAMERA("Camera", false),
    TAKE_PHOTO("Camera", false),
    RECORD_VIDEO("Camera", false),
    OPEN_GALLERY("Gallery", false),
    YOUTUBE_SEARCH("YouTube", false),
    YOUTUBE_PLAY("YouTube", false),

    // Maps & Navigation
    MAPS_SEARCH("Maps", false),
    MAPS_NAVIGATE("Maps", false),
    SHARE_LOCATION("Maps", false),
    MAPS_NEARBY("Maps", false),

    // Cloud & Storage
    DRIVE_SEARCH("Drive", false),
    DRIVE_UPLOAD("Drive", false),
    FILES_OPEN("Files", false),

    // Voice Teacher & Utility
    TEACHER_MODE("Teacher", false),
    SET_ALARM_REMINDER("Utility", false),
    PROBLEM_SOLVER("Troubleshoot", false),
    SAVE_PREFERENCE("Memory", false),
    RECALL_PREFERENCE("Memory", false),

    // Dangerous & Security
    DELETE_FILE("Security", true),
    FACTORY_RESET("Security", true),
    FORMAT_STORAGE("Security", true),
    SEND_MONEY("Security", true),

    // General AI Assistant Conversation
    GENERAL_AI("Assistant", false),
    UNKNOWN("Assistant", false)
}
