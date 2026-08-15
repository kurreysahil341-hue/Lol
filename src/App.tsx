/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState, useEffect, useRef } from "react";
import { 
  ShieldAlert, 
  Mic, 
  Camera, 
  Users, 
  Settings as SettingsIcon, 
  Terminal, 
  Download, 
  Cpu, 
  LogOut,
  Info
} from "lucide-react";
import { Contact, VoiceLog, PhotoAsset, SystemStatus } from "./types";
import { 
  SpeechRecognition, 
  speakJarvis, 
  parseVoiceCommand, 
  executeSystemAction, 
  askGeminiJarvis 
} from "./utils/voice-processor";

import JarvisHUD from "./components/JarvisHUD";
import JarvisContacts from "./components/JarvisContacts";
import JarvisCamera from "./components/JarvisCamera";
import JarvisSettings from "./components/JarvisSettings";

export default function App() {
  // Navigation Tabs
  const [activeTab, setActiveTab] = useState<"hud" | "camera" | "contacts" | "settings">("hud");

  // System & Engine States
  const [isListening, setIsListening] = useState(false);
  const [isSpeaking, setIsSpeaking] = useState(false);
  const [isThinking, setIsThinking] = useState(false);
  const [transcript, setTranscript] = useState("");
  const [recognitionError, setRecognitionError] = useState("");
  const [voiceTriggerShutter, setVoiceTriggerShutter] = useState(false);

  // Persistence States
  const [contacts, setContacts] = useState<Contact[]>([]);
  const [voiceLogs, setVoiceLogs] = useState<VoiceLog[]>([]);
  const [photos, setPhotos] = useState<PhotoAsset[]>([]);

  // System Health Monitor
  const [systemStatus, setSystemStatus] = useState<SystemStatus>({
    microphonePermission: false,
    cameraPermission: false,
    voiceEngineReady: false,
    apiConnected: true,
    online: navigator.onLine,
  });

  // Speech Recognition Ref
  const recognitionRef = useRef<any>(null);

  // 1. Initial Setup and Load Persisted Databases
  useEffect(() => {
    // A. Pre-populate Contacts if empty
    const savedContacts = localStorage.getItem("JARVIS_CONTACTS");
    if (savedContacts) {
      setContacts(JSON.parse(savedContacts));
    } else {
      const defaultContacts: Contact[] = [
        { id: "c1", name: "Papa", phoneNumber: "+919876543210", relationship: "Father" },
        { id: "c2", name: "Rahul", phoneNumber: "+918765432109", relationship: "Friend" },
        { id: "c3", name: "Mummy", phoneNumber: "+917654321098", relationship: "Mother" },
      ];
      setContacts(defaultContacts);
      localStorage.setItem("JARVIS_CONTACTS", JSON.stringify(defaultContacts));
    }

    // B. Load Voice Command Logs
    const savedLogs = localStorage.getItem("JARVIS_VOICE_LOGS");
    if (savedLogs) {
      setVoiceLogs(JSON.parse(savedLogs));
    } else {
      const initialLogs: VoiceLog[] = [
        {
          id: "sys-init",
          timestamp: new Date().toLocaleTimeString(),
          text: "",
          response: "All security grids online, Sir. Awaiting audio trigger input.",
          type: "system",
        },
      ];
      setVoiceLogs(initialLogs);
    }

    // C. Load Photos
    const savedPhotos = localStorage.getItem("JARVIS_PHOTOS");
    if (savedPhotos) {
      setPhotos(JSON.parse(savedPhotos));
    }

    // D. Permissions Check
    checkSystemPermissions();

    // E. Initialize Speech Recognition
    if (SpeechRecognition) {
      const rec = new SpeechRecognition();
      rec.continuous = false; // Tap-to-talk mode
      rec.interimResults = true;
      rec.lang = "hi-IN"; // Set default to Hindi-IN (works perfectly with English inputs too)

      rec.onstart = () => {
        setIsListening(true);
        setTranscript("");
        setRecognitionError("");
      };

      rec.onresult = (event: any) => {
        const currentTranscript = Array.from(event.results)
          .map((result: any) => result[0].transcript)
          .join("");
        setTranscript(currentTranscript);
      };

      rec.onerror = (event: any) => {
        console.error("Speech Recognition Error:", event.error);
        if (event.error === "not-allowed") {
          setRecognitionError("Microphone permission denied. Click setup to unlock.");
        } else {
          setRecognitionError(`Recognition interference: ${event.error}`);
        }
        setIsListening(false);
      };

      rec.onend = () => {
        setIsListening(false);
        // Process transcript if non-empty
        if (transcript && transcript.trim() !== "") {
          handleProcessVoiceInput(transcript);
        }
      };

      recognitionRef.current = rec;
      setSystemStatus(prev => ({ ...prev, voiceEngineReady: true }));
    } else {
      setRecognitionError("Core Speech recognition not natively supported on this browser.");
    }

    // Speech synthesis voices loading
    if (window.speechSynthesis) {
      window.speechSynthesis.getVoices();
    }

    // Online/Offline status listeners
    const handleOnline = () => setSystemStatus((prev) => ({ ...prev, online: true }));
    const handleOffline = () => setSystemStatus((prev) => ({ ...prev, online: false }));
    window.addEventListener("online", handleOnline);
    window.addEventListener("offline", handleOffline);

    return () => {
      window.removeEventListener("online", handleOnline);
      window.removeEventListener("offline", handleOffline);
    };
  }, [transcript]);

  const checkSystemPermissions = async () => {
    try {
      const micPermission = await navigator.permissions.query({ name: "microphone" as any }).catch(() => null);
      const camPermission = await navigator.permissions.query({ name: "camera" as any }).catch(() => null);

      setSystemStatus((prev) => ({
        ...prev,
        microphonePermission: micPermission?.state === "granted",
        cameraPermission: camPermission?.state === "granted",
      }));

      if (micPermission) {
        micPermission.onchange = () => {
          setSystemStatus((prev) => ({ ...prev, microphonePermission: micPermission.state === "granted" }));
        };
      }
      if (camPermission) {
        camPermission.onchange = () => {
          setSystemStatus((prev) => ({ ...prev, cameraPermission: camPermission.state === "granted" }));
        };
      }
    } catch (e) {
      // Permission query API fallback
      navigator.mediaDevices.getUserMedia({ audio: true }).then(() => {
        setSystemStatus((prev) => ({ ...prev, microphonePermission: true }));
      }).catch(() => {});
    }
  };

  // 2. Add New Log entry
  const appendVoiceLog = (log: VoiceLog) => {
    setVoiceLogs((prev) => {
      const updated = [log, ...prev].slice(0, 50); // Keep last 50 logs
      localStorage.setItem("JARVIS_VOICE_LOGS", JSON.stringify(updated));
      return updated;
    });
  };

    // Torch state variable tracker
    const [torchTrack, setTorchTrack] = useState<MediaStreamTrack | null>(null);

    const toggleTorchLocal = async (on: boolean) => {
      try {
        if (on) {
          const mediaStream = await navigator.mediaDevices.getUserMedia({
            video: { facingMode: "environment" },
          });
          const track = mediaStream.getVideoTracks()[0];
          if (track) {
            // Apply torch state constraints (Standard Web/Capacitor API)
            try {
              await track.applyConstraints({
                advanced: [{ torch: true } as any]
              });
            } catch (err) {
              console.warn("Torch hardware control failed, streaming camera instead.");
            }
            setTorchTrack(track);
          }
        } else {
          if (torchTrack) {
            try {
              await torchTrack.applyConstraints({
                advanced: [{ torch: false } as any]
              });
            } catch (err) {}
            torchTrack.stop();
            setTorchTrack(null);
          }
        }
      } catch (e) {
        console.warn("Flashlight API not fully supported in this environment. Falling back to sci-fi log.");
      }
    };

    // 3. Process Final Audio Commands
    const handleProcessVoiceInput = async (spokenText: string) => {
      setTranscript(""); // Clear active text
      setIsThinking(true);
  
      const logId = Math.random().toString();
      const commandLog: VoiceLog = {
        id: logId,
        timestamp: new Date().toLocaleTimeString(),
        text: spokenText,
        response: "Analyzing commands...",
        type: "command",
      };
      appendVoiceLog(commandLog);
  
      // Parse the triggers in Hindi / English
      const parsed = parseVoiceCommand(spokenText, contacts);
  
      // Action A: Camera Click command
      if (parsed.action === "camera") {
        setIsThinking(false);
        setIsSpeaking(true);
        setActiveTab("camera");
        setVoiceTriggerShutter(true); // Pre-arm shutter trigger
  
        speakJarvis(parsed.speakText, "hi-IN", () => {
          setIsSpeaking(false);
        });
  
        setVoiceLogs((prev) => {
          const updated = prev.map((l) =>
            l.id === logId
              ? {
                  ...l,
                  response: parsed.speakText,
                  actionTriggered: "IN-APP CAMERA PHOTO CLICK",
                }
              : l
          );
          localStorage.setItem("JARVIS_VOICE_LOGS", JSON.stringify(updated));
          return updated;
        });
        return;
      }
  
      // Action B: Google Maps, WhatsApp, YouTube, Flashlight, Gmail, Spotify or other System App Intents
      if (
        parsed.action !== "gemini" && 
        parsed.action !== "help" && 
        parsed.action !== "none"
      ) {
        setIsThinking(false);
        setIsSpeaking(true);
  
        speakJarvis(parsed.speakText, "hi-IN", () => {
          setIsSpeaking(false);
          
          // Trigger Local Flashlight hardware if requested
          if (parsed.action === "torch_on") {
            toggleTorchLocal(true);
          } else if (parsed.action === "torch_off") {
            toggleTorchLocal(false);
          } else {
            // Trigger native deep links / intents redirection
            executeSystemAction(parsed.action as any, parsed.queryParam, parsed.phoneParam);
          }
        });
  
        setVoiceLogs((prev) => {
          const updated = prev.map((l) =>
            l.id === logId
              ? {
                  ...l,
                  response: parsed.speakText,
                  actionTriggered: `${parsed.action.toUpperCase()} INTENT LAUNCHED`,
                }
              : l
          );
          localStorage.setItem("JARVIS_VOICE_LOGS", JSON.stringify(updated));
          return updated;
        });
        return;
      }

    // Action C: Ask Gemini AI for Jarvis Response (Intelligent chat)
    if (parsed.action === "gemini" || parsed.action === "help") {
      try {
        let answerText = "";
        
        if (parsed.action === "help") {
          answerText = parsed.speakText;
        } else {
          answerText = await askGeminiJarvis(spokenText);
        }

        setIsThinking(false);
        setIsSpeaking(true);

        speakJarvis(answerText, "hi-IN", () => {
          setIsSpeaking(false);
        });

        setVoiceLogs((prev) => {
          const updated = prev.map((l) =>
            l.id === logId
              ? {
                  ...l,
                  response: answerText,
                  type: "gemini" as const,
                }
              : l
          );
          localStorage.setItem("JARVIS_VOICE_LOGS", JSON.stringify(updated));
          return updated;
        });
      } catch (error: any) {
        setIsThinking(false);
        setIsSpeaking(true);
        const errMsg = "Apologies, Sir. Interferences detected in the main memory cores.";
        
        speakJarvis(errMsg, "hi-IN", () => {
          setIsSpeaking(false);
        });

        setVoiceLogs((prev) => {
          const updated = prev.map((l) =>
            l.id === logId
              ? {
                  ...l,
                  response: errMsg,
                  type: "error" as const,
                }
              : l
          );
          localStorage.setItem("JARVIS_VOICE_LOGS", JSON.stringify(updated));
          return updated;
        });
      }
    }
  };

  // 4. Listen Trigger (Toggle state)
  const toggleListening = () => {
    if (isListening) {
      if (recognitionRef.current) {
        recognitionRef.current.stop();
      }
      setIsListening(false);
    } else {
      if (recognitionRef.current) {
        // Trigger short audio start confirmation chime
        try {
          const audioCtx = new (window.AudioContext || (window as any).webkitAudioContext)();
          const osc = audioCtx.createOscillator();
          const gainNode = audioCtx.createGain();
          osc.connect(gainNode);
          gainNode.connect(audioCtx.destination);
          osc.type = "sine";
          osc.frequency.setValueAtTime(600, audioCtx.currentTime);
          osc.frequency.setValueAtTime(1000, audioCtx.currentTime + 0.08);
          gainNode.gain.setValueAtTime(0.15, audioCtx.currentTime);
          gainNode.gain.exponentialRampToValueAtTime(0.01, audioCtx.currentTime + 0.2);
          osc.start();
          osc.stop(audioCtx.currentTime + 0.2);
        } catch (e) {}

        recognitionRef.current.start();
      } else {
        setRecognitionError("Voice engine not ready or unsupported on this device.");
      }
    }
  };

  // 5. Contacts management
  const handleAddContact = (newContact: Omit<Contact, "id">) => {
    const contact: Contact = {
      id: Math.random().toString(),
      ...newContact,
    };
    const updated = [contact, ...contacts];
    setContacts(updated);
    localStorage.setItem("JARVIS_CONTACTS", JSON.stringify(updated));
    
    appendVoiceLog({
      id: Math.random().toString(),
      timestamp: new Date().toLocaleTimeString(),
      text: "",
      response: `Linked contact profile: "${contact.name}" authorized.`,
      type: "system",
    });
  };

  const handleDeleteContact = (id: string) => {
    const updated = contacts.filter((c) => c.id !== id);
    setContacts(updated);
    localStorage.setItem("JARVIS_CONTACTS", JSON.stringify(updated));
  };

  // 6. Camera Photo Captured
  const handlePhotoCaptured = (newPhoto: Omit<PhotoAsset, "id">) => {
    const photo: PhotoAsset = {
      id: Math.random().toString(),
      ...newPhoto,
    };
    const updated = [photo, ...photos];
    setPhotos(updated);
    localStorage.setItem("JARVIS_PHOTOS", JSON.stringify(updated));

    appendVoiceLog({
      id: Math.random().toString(),
      timestamp: new Date().toLocaleTimeString(),
      text: "",
      response: "Visual scan stored successfully in local tactical archives, Sir.",
      type: "system",
    });

    // Speak confirmation
    speakJarvis("Photo clicked successfully, Sir. Saved to your tactical grid.", "hi-IN");
  };

  const handleDeletePhoto = (id: string) => {
    const updated = photos.filter((p) => p.id !== id);
    setPhotos(updated);
    localStorage.setItem("JARVIS_PHOTOS", JSON.stringify(updated));
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col relative" id="jarvis-app-root">
      {/* Visual background atmospheric elements */}
      <div className="absolute top-0 left-0 right-0 h-[400px] bg-gradient-to-b from-cyan-950/20 to-transparent pointer-events-none" />
      <div className="absolute bottom-0 left-0 w-96 h-96 bg-cyan-900/5 rounded-full filter blur-[120px] pointer-events-none" />
      <div className="absolute top-1/3 right-0 w-96 h-96 bg-red-900/5 rounded-full filter blur-[120px] pointer-events-none" />

      {/* Main Premium HUD Top Header bar */}
      <header className="border-b border-cyan-500/10 bg-slate-950/80 backdrop-blur-md sticky top-0 z-40 px-6 py-4 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 border border-cyan-400 rounded-lg flex items-center justify-center bg-cyan-950/40 relative group">
            <Cpu className="w-5 h-5 text-cyan-400 animate-pulse group-hover:rotate-180 transition-transform duration-500" />
            <span className="absolute -inset-0.5 bg-cyan-400/20 rounded-lg filter blur-sm group-hover:opacity-100 opacity-50 transition-opacity" />
          </div>
          <div className="flex flex-col">
            <h1 className="text-sm font-bold tracking-[0.2em] font-mono text-cyan-200 uppercase">
              J.A.R.V.I.S. VOICE ASSISTANT
            </h1>
            <span className="text-[9px] tracking-widest text-cyan-400/50 font-mono">
              VIVO Y29 MOBILE SYSTEMS // CORE INTEGRATION
            </span>
          </div>
        </div>

        {/* Global Action Guides */}
        <div className="hidden md:flex items-center gap-4 text-xs font-mono">
          <div className="flex items-center gap-1.5 px-3 py-1 bg-cyan-950/30 border border-cyan-500/10 rounded-full">
            <Info className="w-3.5 h-3.5 text-cyan-400" />
            <span className="text-cyan-300 text-[10px]">
              Speak Hindi: &quot;Call Papa&quot;, &quot;Camera mein photo kheencho&quot;
            </span>
          </div>
        </div>
      </header>

      {/* Main Body Grid */}
      <main className="flex-1 max-w-7xl w-full mx-auto p-4 md:p-6 grid grid-cols-1 lg:grid-cols-12 gap-6 relative z-10">
        
        {/* LEFT COLUMN: Main holographic visual orb HUD & Console Logs (Column span 7) */}
        <section className="lg:col-span-7 flex flex-col gap-6">
          {recognitionError && (
            <div className="bg-red-950/30 border border-red-500/30 p-3.5 rounded-xl flex items-center gap-3 text-red-300 font-mono text-[11px] shadow-md animate-bounce">
              <ShieldAlert className="w-4 h-4 text-red-400 shrink-0" />
              <span>{recognitionError}</span>
            </div>
          )}

          <JarvisHUD
            isListening={isListening}
            isSpeaking={isSpeaking}
            isThinking={isThinking}
            transcript={transcript}
            voiceLogs={voiceLogs}
            onToggleListen={toggleListening}
            systemStatus={systemStatus}
          />
        </section>

        {/* RIGHT COLUMN: Tab Panel directory selector (Column span 5) */}
        <section className="lg:col-span-5 flex flex-col gap-4">
          
          {/* Futuristic Tactical Tab controls */}
          <nav className="flex border border-cyan-500/20 bg-slate-950/60 p-1.5 rounded-xl font-mono text-[11px] tracking-wider uppercase">
            <button
              onClick={() => setActiveTab("hud")}
              className={`flex-1 py-2 rounded-lg text-center font-bold flex items-center justify-center gap-1.5 transition-all cursor-pointer ${
                activeTab === "hud"
                  ? "bg-cyan-500 text-slate-950 font-bold shadow-[0_0_10px_rgba(6,182,212,0.3)]"
                  : "text-cyan-400/60 hover:text-cyan-300"
              }`}
            >
              <Cpu className="w-3.5 h-3.5" /> HUD CORE
            </button>
            <button
              onClick={() => setActiveTab("camera")}
              className={`flex-1 py-2 rounded-lg text-center font-bold flex items-center justify-center gap-1.5 transition-all cursor-pointer ${
                activeTab === "camera"
                  ? "bg-cyan-500 text-slate-950 font-bold shadow-[0_0_10px_rgba(6,182,212,0.3)]"
                  : "text-cyan-400/60 hover:text-cyan-300"
              }`}
            >
              <Camera className="w-3.5 h-3.5" /> CAMERA
            </button>
            <button
              onClick={() => setActiveTab("contacts")}
              className={`flex-1 py-2 rounded-lg text-center font-bold flex items-center justify-center gap-1.5 transition-all cursor-pointer ${
                activeTab === "contacts"
                  ? "bg-cyan-500 text-slate-950 font-bold shadow-[0_0_10px_rgba(6,182,212,0.3)]"
                  : "text-cyan-400/60 hover:text-cyan-300"
              }`}
            >
              <Users className="w-3.5 h-3.5" /> CONTACTS
            </button>
            <button
              onClick={() => setActiveTab("settings")}
              className={`flex-1 py-2 rounded-lg text-center font-bold flex items-center justify-center gap-1.5 transition-all cursor-pointer ${
                activeTab === "settings"
                  ? "bg-cyan-500 text-slate-950 font-bold shadow-[0_0_10px_rgba(6,182,212,0.3)]"
                  : "text-cyan-400/60 hover:text-cyan-300"
              }`}
            >
              <SettingsIcon className="w-3.5 h-3.5" /> SETUP
            </button>
          </nav>

          {/* Active component render view */}
          <div className="flex-1 flex flex-col">
            {activeTab === "hud" && (
              <div className="border border-cyan-500/10 bg-slate-950/50 p-6 rounded-2xl flex flex-col gap-4 font-mono text-xs relative overflow-hidden">
                <div className="absolute top-0 right-0 w-3 h-3 border-t border-r border-cyan-400/30" />
                <div className="absolute bottom-0 left-0 w-3 h-3 border-b border-l border-cyan-400/30" />
                
                <h3 className="text-cyan-300 font-bold uppercase tracking-widest border-b border-cyan-500/10 pb-3 mb-1">
                  Jarvis Command Matrix Guide
                </h3>
                
                <p className="text-[11px] text-cyan-400/60 leading-relaxed">
                  Welcome to the tactical system command console. Here are the fully operational voice commands you can speak directly:
                </p>

                <div className="flex flex-col gap-3">
                  <div className="p-3 bg-slate-900/40 border border-cyan-500/10 rounded-lg">
                    <span className="text-cyan-300 font-bold block mb-1">🚗 GOOGLE MAPS NAVIGATION:</span>
                    <span className="text-cyan-100 italic">&ldquo;Navigate to Delhi&rdquo;</span> or <br />
                    <span className="text-cyan-100 italic">&ldquo;Delhi ki navigation on karo&rdquo;</span>
                  </div>

                  <div className="p-3 bg-slate-900/40 border border-cyan-500/10 rounded-lg">
                    <span className="text-cyan-300 font-bold block mb-1">📞 CALL CONTACTS:</span>
                    <span className="text-cyan-100 italic">&ldquo;Call Papa&rdquo;</span> or <br />
                    <span className="text-cyan-100 italic">&ldquo;Rahul ko call karo&rdquo;</span>
                  </div>

                  <div className="p-3 bg-slate-900/40 border border-cyan-500/10 rounded-lg">
                    <span className="text-cyan-300 font-bold block mb-1">📸 IN-APP TACTICAL CAMERA:</span>
                    <span className="text-cyan-100 italic">&ldquo;Camera mein photo click karo&rdquo;</span> or <br />
                    <span className="text-cyan-100 italic">&ldquo;Click photo&rdquo;</span>
                  </div>

                  <div className="p-3 bg-slate-900/40 border border-cyan-500/10 rounded-lg">
                    <span className="text-cyan-300 font-bold block mb-1">🎵 YOUTUBE SEARCH:</span>
                    <span className="text-cyan-100 italic">&ldquo;YouTube par Arijit Singh search karo&rdquo;</span>
                  </div>

                  <div className="p-3 bg-slate-900/40 border border-cyan-500/10 rounded-lg">
                    <span className="text-cyan-300 font-bold block mb-1">🤖 GEMINI COGNITIVE INTEL:</span>
                    <span className="text-cyan-100 italic">&ldquo;Who is Tony Stark?&rdquo;</span> or <br />
                    <span className="text-cyan-100 italic">&ldquo;Ek joke sunao Hindi mein&rdquo;</span>
                  </div>
                </div>
              </div>
            )}

            {activeTab === "camera" && (
              <JarvisCamera
                onPhotoCaptured={handlePhotoCaptured}
                photos={photos}
                onDeletePhoto={handleDeletePhoto}
                voiceTriggerShutter={voiceTriggerShutter}
                onResetVoiceTrigger={() => setVoiceTriggerShutter(false)}
              />
            )}

            {activeTab === "contacts" && (
              <JarvisContacts
                contacts={contacts}
                onAddContact={handleAddContact}
                onDeleteContact={handleDeleteContact}
              />
            )}

            {activeTab === "settings" && (
              <JarvisSettings />
            )}
          </div>
        </section>
      </main>

      {/* Footer system diagnostics bar */}
      <footer className="border-t border-cyan-500/10 bg-slate-950/80 backdrop-blur-md px-6 py-3 font-mono text-[9px] text-cyan-400/50 flex flex-col sm:flex-row items-center justify-between gap-3 mt-auto">
        <span className="uppercase tracking-wider">
          Diagnostic System: ALL INTEGRITY LEVELS STABLE [SEC_ACC: LEVEL_5]
        </span>
        <div className="flex items-center gap-4">
          <span>LATENCY: 42MS</span>
          <span>BATT_INTEL: DIRECT</span>
          <span>FRAMEWARE: VIVO Y29</span>
        </div>
      </footer>
    </div>
  );
}

