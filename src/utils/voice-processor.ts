/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import { Contact } from "../types";
import { getGeminiApiKey } from "../config/api-config";

// Detect if SpeechRecognition is available
export const SpeechRecognition =
  (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;

/**
 * Text-to-Speech (TTS) function in Jarvis Voice Style
 */
export function speakJarvis(text: string, lang: string = "hi-IN", callback?: () => void) {
  if (!window.speechSynthesis) {
    console.error("Speech Synthesis not supported in this browser.");
    if (callback) callback();
    return;
  }

  // Cancel any ongoing speech
  window.speechSynthesis.cancel();

  const utterance = new SpeechSynthesisUtterance(text);
  
  // Set elegant rate and pitch for Jarvis voice
  utterance.rate = 1.05; // Slightly fast, intelligent pace
  utterance.pitch = 0.95; // Slightly lower pitch for a sophisticated voice

  // Try to find an English (UK/US) or Hindi voice based on preference
  const voices = window.speechSynthesis.getVoices();
  
  // For Hindi commands or mixed Hindi/English, we try to find a suitable voice
  if (lang.startsWith("hi")) {
    const hindiVoice = voices.find(
      (v) => v.lang.startsWith("hi") || v.name.toLowerCase().includes("india")
    );
    if (hindiVoice) utterance.voice = hindiVoice;
    utterance.lang = "hi-IN";
  } else {
    // English Jarvis voice selection
    const jarvisVoice = voices.find(
      (v) =>
        v.name.toLowerCase().includes("google uk english male") ||
        v.name.toLowerCase().includes("male") ||
        v.lang.startsWith("en-GB") ||
        v.lang.startsWith("en-US")
    );
    if (jarvisVoice) utterance.voice = jarvisVoice;
    utterance.lang = "en-US";
  }

  utterance.onend = () => {
    if (callback) callback();
  };

  utterance.onerror = (e) => {
    console.error("Speech synthesis error:", e);
    if (callback) callback();
  };

  window.speechSynthesis.speak(utterance);
}

/**
 * Interface for parsed Voice Command outcomes
 */
export interface CommandResult {
  action: "navigate" | "call" | "youtube" | "camera" | "gemini" | "help" | "none" |
          "torch_on" | "torch_off" | "whatsapp" | "calculator" | "clock" | "settings" |
          "gmail" | "playstore" | "instagram" | "spotify" | "browser";
  speakText: string;
  queryParam?: string;
  phoneParam?: string;
}

/**
 * Parsing voice inputs for custom Hindi & English triggers
 */
export function parseVoiceCommand(text: string, contacts: Contact[]): CommandResult {
  const t = text.toLowerCase().trim();

  // 1. HELP / MANUAL COMMANDS
  if (t.includes("help") || t.includes("madad") || t.includes("kya kar sakte ho") || t.includes("guide")) {
    return {
      action: "help",
      speakText: "Sir, I can control system apps on your Vivo Y29 like calling, Google Maps, camera, WhatsApp, Torch, Calculator, Settings, Gmail, Play Store, Spotify, Instagram, or answer anything using my main Gemini core. Just speak a command!"
    };
  }

  // 2. FLASHLIGHT / TORCH CONTROL
  if (t.includes("flashlight on") || t.includes("torch on") || t.includes("torch jalao") || t.includes("flashlight jalao") || t.includes("light on")) {
    return {
      action: "torch_on",
      speakText: "Initializing secondary power backup. Flashlight turned on, Sir."
    };
  }
  if (t.includes("flashlight off") || t.includes("torch off") || t.includes("torch band") || t.includes("flashlight band") || t.includes("light off")) {
    return {
      action: "torch_off",
      speakText: "Power grid restored. Flashlight deactivated, Sir."
    };
  }

  // 3. WHATSAPP CONTROL
  const waPatterns = [
    /whatsapp (.+)/,
    /whatsapp par (.+) ko message karo/,
    /whatsapp message (.+)/,
    /whatsapp pe (.+)/
  ];
  for (const pattern of waPatterns) {
    const match = t.match(pattern);
    if (match) {
      const target = match[1].replace("ko", "").replace("message karo", "").replace("message", "").trim();
      const found = contacts.find(
        (c) => c.name.toLowerCase().includes(target) || target.includes(c.name.toLowerCase())
      );
      if (found) {
        return {
          action: "whatsapp",
          queryParam: found.name,
          phoneParam: found.phoneNumber,
          speakText: `Understood Sir. Opening WhatsApp chat window with ${found.name}. Connecting secure messaging link.`
        };
      }
    }
  }
  if (t.includes("whatsapp kholo") || t.includes("open whatsapp") || t.includes("whatsapp chalao")) {
    return {
      action: "whatsapp",
      speakText: "Opening WhatsApp application, Sir."
    };
  }

  // 4. CALCULATOR
  if (t.includes("calculator kholo") || t.includes("open calculator") || t.includes("hisab karo") || t.includes("calculate")) {
    return {
      action: "calculator",
      speakText: "Launching tactical arithmetic engine. Opening Calculator, Sir."
    };
  }

  // 5. CLOCK / TIMER / ALARM
  if (t.includes("timer") || t.includes("alarm") || t.includes("clock kholo") || t.includes("open clock") || t.includes("ghadi kholo") || t.includes("stopwatch")) {
    return {
      action: "clock",
      speakText: "Synchronizing system clock frequencies. Opening Clock module, Sir."
    };
  }

  // 6. SYSTEM SETTINGS
  if (t.includes("settings kholo") || t.includes("open settings") || t.includes("system settings") || t.includes("phone settings")) {
    return {
      action: "settings",
      speakText: "Accessing core mainframe configuration. Opening Device Settings, Sir."
    };
  }

  // 7. GMAIL / EMAIL
  if (t.includes("gmail kholo") || t.includes("open gmail") || t.includes("email open") || t.includes("mail open") || t.includes("post kholo")) {
    return {
      action: "gmail",
      speakText: "Retrieving secure encrypted postal archives. Opening Gmail, Sir."
    };
  }

  // 8. PLAY STORE
  if (t.includes("play store kholo") || t.includes("open play store") || t.includes("download app") || t.includes("playstore")) {
    return {
      action: "playstore",
      speakText: "Connecting to global database. Launching Google Play Store, Sir."
    };
  }

  // 9. INSTAGRAM / SOCIALS
  if (t.includes("instagram kholo") || t.includes("open instagram") || t.includes("insta kholo") || t.includes("instagram chalao")) {
    return {
      action: "instagram",
      speakText: "Accessing holographic social matrix. Launching Instagram, Sir."
    };
  }

  // 10. SPOTIFY / MUSIC
  if (t.includes("spotify kholo") || t.includes("open spotify") || t.includes("gaana bajao") || t.includes("play music") || t.includes("music chalao")) {
    return {
      action: "spotify",
      speakText: "Tuning audio frequency filters. Opening Spotify Music Player, Sir."
    };
  }

  // 11. BROWSER / GOOGLE SEARCH
  const searchPatterns = [
    /google par (.+) search karo/,
    /google (.+) search karo/,
    /search for (.+) on google/,
    /search (.+) on google/,
    /google search (.+)/
  ];
  for (const pattern of searchPatterns) {
    const match = t.match(pattern);
    if (match) {
      const query = match[1].trim();
      return {
        action: "browser",
        queryParam: query,
        speakText: `Searching main database grids for ${query}. Opening browser.`
      };
    }
  }
  if (t.includes("browser kholo") || t.includes("open browser") || t.includes("google kholo") || t.includes("open google")) {
    return {
      action: "browser",
      speakText: "Launching default secure web browser, Sir."
    };
  }

  // 12. CAMERA AND PHOTOS
  // Triggers: "photo", "camera", "click pic", "selfie", "capture"
  if (
    t.includes("camera") || 
    t.includes("photo click") || 
    t.includes("photo kheencho") || 
    t.includes("click photo") || 
    t.includes("take photo") || 
    t.includes("capture") || 
    t.includes("selfie")
  ) {
    return {
      action: "camera",
      speakText: "Understood, Sir. Initializing tactical camera feed. Get ready for the capture."
    };
  }

  // 13. GOOGLE MAPS NAVIGATION
  // Triggers: "navigation on karo", "navigate to Delhi", "Delhi ki navigation on karo", "map me to Delhi"
  const mapsPatterns = [
    /navigate to (.+)/,
    /map me to (.+)/,
    /(.+) ki navigation on karo/,
    /navigation on karo (.+)/,
    /show map of (.+)/,
    /rasta dikhao (.+)/,
    /(.+) ka rasta/
  ];

  for (const pattern of mapsPatterns) {
    const match = t.match(pattern);
    if (match) {
      const destination = match[1].replace("to", "").replace("for", "").trim();
      return {
        action: "navigate",
        queryParam: destination,
        speakText: `Understood, Sir. Launching Google Maps navigation to ${destination}. Safe travels.`
      };
    }
  }

  // Fallback map triggers
  if (t.includes("navigation") || t.includes("map") || t.includes("location") || t.includes("gps")) {
    // Extract a place name if possible
    let place = t.replace("navigation", "").replace("on karo", "").replace("map", "").replace("navigate", "").trim();
    if (place.length > 2) {
      return {
        action: "navigate",
        queryParam: place,
        speakText: `Plotting coordinates for ${place}. Opening Google Maps.`
      };
    }
  }

  // 14. CALLING CONTACTS
  // Triggers: "call papa", "call rahul", "phone lagao papa ko", "rahul ko calling karo"
  const callPatterns = [
    /call (.+)/,
    /phone lagao (.+)/,
    /(.+) ko phone lagao/,
    /(.+) ko call karo/,
    /calling karo (.+)/,
    /(.+) ko calling karo/,
    /dial (.+)/
  ];

  for (const pattern of callPatterns) {
    const match = t.match(pattern);
    if (match) {
      const contactName = match[1].replace("ko", "").replace("phone", "").trim();
      // Search in custom contacts
      const found = contacts.find(
        (c) => c.name.toLowerCase().includes(contactName) || contactName.includes(c.name.toLowerCase())
      );

      if (found) {
        return {
          action: "call",
          queryParam: found.name,
          phoneParam: found.phoneNumber,
          speakText: `Sir, dialing ${found.name} now on ${found.phoneNumber}. Initiating link.`
        };
      } else {
        // If contact not found, prompt them
        return {
          action: "call",
          queryParam: contactName,
          speakText: `Sir, I found the command to call ${contactName}, but there is no contact saved with this name. Would you like me to open your dialer directly?`
        };
      }
    }
  }

  // 15. YOUTUBE SEARCHES
  // Triggers: "youtube par gaana search karo", "youtube search [song]", "play [song] on youtube", "gana search karo [song]"
  const ytPatterns = [
    /youtube per (.+) search karo/,
    /youtube par (.+) search karo/,
    /youtube par gana (.+) search karo/,
    /youtube par (.+) gana search karo/,
    /youtube search (.+)/,
    /play (.+) on youtube/,
    /youtube pe (.+) chalao/,
    /youtube par (.+) chalao/
  ];

  for (const pattern of ytPatterns) {
    const match = t.match(pattern);
    if (match) {
      const song = match[1].replace("search", "").replace("gaana", "").replace("gana", "").trim();
      return {
        action: "youtube",
        queryParam: song,
        speakText: `Understood, Sir. Searching YouTube for ${song}. Connecting to stream.`
      };
    }
  }

  if (t.includes("youtube") || t.includes("gana search") || t.includes("song search")) {
    let cleanSong = t.replace("youtube", "").replace("gana search karo", "").replace("song search", "").replace("par", "").replace("pe", "").trim();
    if (cleanSong.length > 2) {
      return {
        action: "youtube",
        queryParam: cleanSong,
        speakText: `Sir, launching YouTube search for ${cleanSong}.`
      };
    }
  }

  // 16. DEFAULT - ASK GEMINI AI FOR INTEL
  return {
    action: "gemini",
    speakText: "", // Let Gemini reply
    queryParam: text
  };
}

/**
 * Triggers native mobile triggers or web redirects
 */
export function executeSystemAction(
  action: "navigate" | "call" | "youtube" | "whatsapp" | "calculator" | "clock" | "settings" | "gmail" | "playstore" | "instagram" | "spotify" | "browser" | "torch_on" | "torch_off" | "camera" | "gemini" | "help" | "none", 
  param?: string, 
  phoneParam?: string
) {
  if (action === "navigate" && param) {
    const url = `https://www.google.com/maps/dir/?api=1&destination=${encodeURIComponent(param)}`;
    window.open(url, "_blank");
  } 
  else if (action === "youtube" && param) {
    const url = `https://www.youtube.com/results?search_query=${encodeURIComponent(param)}`;
    window.open(url, "_blank");
  } 
  else if (action === "call") {
    if (phoneParam) {
      window.location.href = `tel:${phoneParam}`;
    } else if (param) {
      window.location.href = `tel:${param}`;
    } else {
      window.location.href = `tel:`;
    }
  }
  else if (action === "whatsapp") {
    if (phoneParam) {
      // Direct message link
      window.open(`https://api.whatsapp.com/send?phone=${encodeURIComponent(phoneParam)}`, "_blank");
    } else {
      // Standard app launch
      window.open("intent://#Intent;package=com.whatsapp;end", "_blank");
    }
  }
  else if (action === "calculator") {
    // Open Android system calculator
    window.open("intent://#Intent;action=android.intent.action.MAIN;category=android.intent.category.APP_CALCULATOR;end", "_blank");
  }
  else if (action === "clock") {
    // Open Android clock/alarms page
    window.open("intent://#Intent;action=android.intent.action.SHOW_ALARMS;end", "_blank");
  }
  else if (action === "settings") {
    // Open system settings page
    window.open("intent://#Intent;action=android.settings.SETTINGS;end", "_blank");
  }
  else if (action === "gmail") {
    // Open Gmail or default mail app
    window.location.href = "mailto:";
  }
  else if (action === "playstore") {
    // Open Google Play Store
    window.open("intent://#Intent;package=com.android.vending;end", "_blank");
  }
  else if (action === "instagram") {
    // Open Instagram app
    window.open("intent://#Intent;package=com.instagram.android;end", "_blank");
  }
  else if (action === "spotify") {
    // Open Spotify
    window.open("intent://#Intent;package=com.spotify.music;end", "_blank");
  }
  else if (action === "browser") {
    if (param) {
      window.open(`https://www.google.com/search?q=${encodeURIComponent(param)}`, "_blank");
    } else {
      window.open("https://www.google.com", "_blank");
    }
  }
}

/**
 * Calls Gemini AI either server-side (web preview) or client-side (standalone APK)
 */
export async function askGeminiJarvis(prompt: string, customApiKey?: string): Promise<string> {
  const activeKey = customApiKey || getGeminiApiKey();

  // If we have an API Key (perfect for local APK or configured users)
  if (activeKey && activeKey !== "YOUR_GEMINI_API_KEY_HERE") {
    try {
      console.log("Client-Side: Requesting Gemini API with localized API key...");
      const response = await fetch(`https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=${activeKey}`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          contents: [{ parts: [{ text: prompt }] }],
          systemInstruction: {
            parts: [{ text: "You are Jarvis, the highly advanced, classy, witty, and deeply loyal AI voice assistant of Tony Stark from the Iron Man films. Speak directly to Tony (Sir) in a futuristic, sleek, helpful manner. Mix English and conversational Hindi/Urdu beautifully when needed. Keep answers extremely direct, neat, conversational and quick to speak." }]
          },
          generationConfig: {
            temperature: 0.7,
            maxOutputTokens: 250,
          }
        }),
      });

      if (!response.ok) {
        const errData = await response.json().catch(() => ({}));
        throw new Error(errData?.error?.message || "Gemini API HTTP Error " + response.status);
      }

      const resData = await response.json();
      const textResponse = resData.candidates?.[0]?.content?.parts?.[0]?.text || "";
      return textResponse;
    } catch (e: any) {
      console.error("Client-Side Gemini Call failed, falling back to server:", e);
      // Fallback to server proxy if client call fails or throws CORS/network issues
    }
  }

  // Server-side fallback proxy (Ideal for Google AI Studio Web Preview)
  try {
    console.log("Server-Side: Fetching through server proxy /api/gemini/generate...");
    const response = await fetch("/api/gemini/generate", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "x-client-api-key": activeKey || "",
      },
      body: JSON.stringify({
        prompt: prompt,
        systemInstruction: "You are Jarvis, the highly advanced, classy, witty, and deeply loyal AI voice assistant of Tony Stark from the Iron Man films. Speak directly to Tony (Sir) in a futuristic, sleek, helpful manner. Mix English and conversational Hindi/Urdu beautifully when needed. Keep answers extremely direct, neat, conversational and quick to speak."
      }),
    });

    if (!response.ok) {
      const err = await response.json().catch(() => ({}));
      throw new Error(err.error || "Failed to contact Gemini proxy");
    }

    const data = await response.json();
    return data.text || "Sir, I am online, but failed to retrieve a proper response.";
  } catch (error: any) {
    console.error("AI Assistant query failed completely:", error);
    return "Sir, I'm experiencing interference. Please check your network connection and API credentials.";
  }
}
