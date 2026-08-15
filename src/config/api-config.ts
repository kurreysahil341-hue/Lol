/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

// =========================================================================
// JARVIS VOICE ASSISTANT - API CONFIGURATION FILE
// =========================================================================
// HINDI: Aap apni Gemini API Key yahan niche "GEMINI_API_KEY" ke samne daal sakte hain.
// Jab aap is app ki zip file ko GitHub par upload karenge aur APK banayenge,
// toh yeh API Key aapke mobile app (APK) mein seedhe kaam karegi!
//
// ENGLISH: You can paste your Gemini API Key in the field below.
// When you upload this zip to GitHub and compile your debug APK, 
// this key will be baked into the APK so that your voice assistant works offline/directly!
// =========================================================================

export const API_CONFIG = {
  // Yahan apni Gemini API Key likhein (e.g., "AIzaSy...")
  GEMINI_API_KEY: "YOUR_GEMINI_API_KEY_HERE",
  
  // App default system language ('hi-IN' for Hindi, 'en-US' for English, or 'auto')
  DEFAULT_LANGUAGE: "auto",
  
  // Set to true if you want to use the API key directly in client-side (required for standalone APK)
  USE_CLIENT_SIDE_API_KEY: true,
};

/**
 * Helper function to retrieve the active Gemini API Key.
 * It will check:
 * 1. Saved key in LocalStorage (set dynamically in the UI settings)
 * 2. This configuration file (API_CONFIG.GEMINI_API_KEY)
 * 3. Environment variables (process.env.GEMINI_API_KEY / import.meta.env.VITE_GEMINI_API_KEY)
 */
export function getGeminiApiKey(): string {
  // Check localStorage first (so users can change it in the app dynamically)
  const savedKey = localStorage.getItem("JARVIS_GEMINI_API_KEY");
  if (savedKey && savedKey.trim() !== "" && savedKey !== "YOUR_GEMINI_API_KEY_HERE") {
    return savedKey.trim();
  }
  
  // Check the config file
  if (API_CONFIG.GEMINI_API_KEY && API_CONFIG.GEMINI_API_KEY !== "YOUR_GEMINI_API_KEY_HERE") {
    return API_CONFIG.GEMINI_API_KEY.trim();
  }
  
  // Fallback to build env if available
  const envKey = (import.meta as any).env?.VITE_GEMINI_API_KEY || "";
  if (envKey) {
    return envKey;
  }
  
  return "";
}
