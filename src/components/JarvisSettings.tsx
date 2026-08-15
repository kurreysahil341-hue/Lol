/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState, useEffect } from "react";
import { Key, Save, Play, BookOpen, AlertTriangle, Github, Code, Check } from "lucide-react";
import { API_CONFIG, getGeminiApiKey } from "../config/api-config";
import { speakJarvis } from "../utils/voice-processor";

export default function JarvisSettings() {
  const [apiKey, setApiKey] = useState("");
  const [saveSuccess, setSaveSuccess] = useState(false);
  const [testSpeechText, setTestSpeechText] = useState("Greetings, Sir. Core intelligence modules are online.");

  useEffect(() => {
    // Load existing key from localStorage
    const saved = localStorage.getItem("JARVIS_GEMINI_API_KEY") || "";
    if (saved) {
      setApiKey(saved);
    } else if (API_CONFIG.GEMINI_API_KEY && API_CONFIG.GEMINI_API_KEY !== "YOUR_GEMINI_API_KEY_HERE") {
      setApiKey(API_CONFIG.GEMINI_API_KEY);
    }
  }, []);

  const handleSaveKey = () => {
    localStorage.setItem("JARVIS_GEMINI_API_KEY", apiKey.trim());
    setSaveSuccess(true);
    speakJarvis("System authorization credentials updated successfully, Sir.", "en-US");
    setTimeout(() => setSaveSuccess(false), 3000);
  };

  const handleTestSpeech = () => {
    speakJarvis(testSpeechText, "en-US");
  };

  return (
    <div className="border border-cyan-500/20 bg-slate-950/70 p-6 rounded-2xl backdrop-blur-md relative overflow-hidden" id="jarvis-settings-panel">
      {/* Visual background framing */}
      <div className="absolute top-0 right-0 w-3 h-3 border-t border-r border-cyan-400/30" />
      <div className="absolute bottom-0 left-0 w-3 h-3 border-b border-l border-cyan-400/30" />

      <div className="flex items-center justify-between border-b border-cyan-500/10 pb-4 mb-4">
        <div className="flex items-center gap-2">
          <Key className="w-5 h-5 text-cyan-400" />
          <h2 className="text-sm font-bold tracking-widest text-cyan-200 uppercase font-mono">
            Jarvis Security &amp; Deployment Configuration
          </h2>
        </div>
      </div>

      <div className="flex flex-col gap-6 font-mono text-xs">
        {/* 1. API Key Input Section */}
        <div className="bg-slate-900/40 border border-cyan-500/10 rounded-xl p-4">
          <h3 className="text-cyan-300 font-bold uppercase tracking-wider text-[11px] mb-2 flex items-center gap-1.5">
            <Key className="w-3.5 h-3.5" /> Configure Gemini API Key
          </h3>
          
          <p className="text-[10px] text-cyan-400/60 leading-relaxed mb-4">
            HINDI: Aap apni Gemini API Key yahan niche daal sakte hain. Yeh key direct aapke local browser aur APK mein use hogi. 
            Aap ise <code>src/config/api-config.ts</code> file mein bhi likh kar rakh sakte hain!
          </p>

          <div className="flex flex-col sm:flex-row gap-3">
            <input
              type="password"
              placeholder="AIzaSy..."
              value={apiKey}
              onChange={(e) => setApiKey(e.target.value)}
              className="flex-1 bg-slate-950 border border-cyan-500/20 rounded px-3 py-2 text-cyan-100 outline-none focus:border-cyan-400 placeholder:text-cyan-400/20 text-xs"
            />
            
            <button
              onClick={handleSaveKey}
              className="px-4 py-2 bg-cyan-600 hover:bg-cyan-500 text-slate-950 rounded font-bold uppercase tracking-wide flex items-center justify-center gap-1.5 transition-colors"
            >
              {saveSuccess ? <Check className="w-4 h-4 animate-bounce" /> : <Save className="w-4 h-4" />}
              {saveSuccess ? "LINKED!" : "SAVE KEY"}
            </button>
          </div>
        </div>

        {/* 2. Audio Vocal Tester */}
        <div className="bg-slate-900/40 border border-cyan-500/10 rounded-xl p-4">
          <h3 className="text-cyan-300 font-bold uppercase tracking-wider text-[11px] mb-2 flex items-center gap-1.5">
            <Play className="w-3.5 h-3.5" /> Vocal Frequency Tester
          </h3>
          
          <p className="text-[10px] text-cyan-400/60 leading-relaxed mb-3">
            Test Jarvis&apos; advanced speech engine directly. Type anything to trigger Vocal feedback.
          </p>

          <div className="flex flex-col sm:flex-row gap-3">
            <input
              type="text"
              value={testSpeechText}
              onChange={(e) => setTestSpeechText(e.target.value)}
              className="flex-1 bg-slate-950 border border-cyan-500/20 rounded px-3 py-2 text-cyan-100 outline-none focus:border-cyan-400 placeholder:text-cyan-400/20 text-xs"
            />
            
            <button
              onClick={handleTestSpeech}
              className="px-4 py-2 bg-cyan-950/50 border border-cyan-400/30 hover:bg-cyan-900/40 text-cyan-300 rounded font-bold uppercase tracking-wide flex items-center justify-center gap-1.5 transition-colors"
            >
              <Play className="w-3.5 h-3.5" /> Voice Test
            </button>
          </div>
        </div>

        {/* 3. GitHub & APK Compilation Manual */}
        <div className="bg-slate-900/40 border border-cyan-500/10 rounded-xl p-4">
          <h3 className="text-cyan-300 font-bold uppercase tracking-wider text-[11px] mb-3 flex items-center gap-1.5">
            <BookOpen className="w-3.5 h-3.5" /> APK Compilation Guide
          </h3>

          <div className="flex flex-col gap-3 leading-relaxed text-[10px] text-cyan-400/80">
            <div className="p-3 bg-cyan-950/20 border-l-2 border-cyan-400 rounded">
              <span className="font-bold text-cyan-300 block mb-1">⚡ AUTOMATIC APK COMPILATION VIA GITHUB (HINDI):</span>
              1. Humne is project mein <code>.github/workflows/android.yml</code> file banayi hai.<br />
              2. Is app ki Zip file download karein, use extract karke GitHub par ek naya repository banayein aur code push karein.<br />
              3. GitHub par push karte hi automatic build trigger ho jayega jo Java 17 aur Node 22 ka use karke aapka **Debug APK** bana dega!<br />
              4. Build poora hone par aap GitHub Actions Tab mein jaakar **app-debug.apk** download karke apne Vivo Y29 mobile mein install kar sakte hain!
            </div>

            <div className="p-3 bg-red-950/20 border-l-2 border-red-500/40 rounded">
              <span className="font-bold text-red-400 block mb-1 flex items-center gap-1">
                <AlertTriangle className="w-3.5 h-3.5" /> MICROPHONE &amp; CAMERA PERMISSION IN APK:
              </span>
              Vivo Y29 mobile mein installing ke baad ensure karein ki aap app settings mein jaakar **Microphone** aur **Camera** permissions ko manual allow karein, taaki voice commands aur optical clicks bina kisi problem ke kaam kar sakein!
            </div>

            <div className="flex items-center justify-between border-t border-cyan-500/10 pt-3 mt-1 font-mono">
              <span className="text-[9px] text-cyan-400/40">SYSTEM DEPLOYER COMPATIBLE: NODE 22 + CAPACITOR 6</span>
              <span className="flex items-center gap-1 text-cyan-400 font-bold uppercase text-[9px]">
                <Github className="w-3 h-3" /> Actions Enabled
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
