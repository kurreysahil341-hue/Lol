/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React from "react";
import { motion } from "motion/react";
import { Mic, MicOff, Volume2, Shield, Radio, Terminal, Cpu } from "lucide-react";
import { VoiceLog } from "../types";

interface JarvisHUDProps {
  isListening: boolean;
  isSpeaking: boolean;
  isThinking: boolean;
  transcript: string;
  voiceLogs: VoiceLog[];
  onToggleListen: () => void;
  systemStatus: {
    online: boolean;
    apiConnected: boolean;
    microphonePermission: boolean;
    voiceEngineReady: boolean;
  };
}

export default function JarvisHUD({
  isListening,
  isSpeaking,
  isThinking,
  transcript,
  voiceLogs,
  onToggleListen,
  systemStatus,
}: JarvisHUDProps) {
  // Determine orb color & animation state
  let orbClass = "bg-cyan-500/15 border-cyan-400 shadow-cyan-500/40";
  let rippleColor = "border-cyan-400/30";
  let statusText = "SYSTEMS READIED";

  if (isListening) {
    orbClass = "bg-red-500/20 border-red-500 shadow-red-500/50 animate-pulse";
    rippleColor = "border-red-500/30";
    statusText = "LISTENING COMMAND...";
  } else if (isThinking) {
    orbClass = "bg-amber-500/15 border-amber-400 shadow-amber-500/40 animate-spin";
    rippleColor = "border-amber-400/30";
    statusText = "PROCESSING INTEL...";
  } else if (isSpeaking) {
    orbClass = "bg-emerald-500/20 border-emerald-400 shadow-emerald-500/40";
    rippleColor = "border-emerald-400/30";
    statusText = "JARVIS RESPONDING";
  }

  return (
    <div className="flex flex-col gap-6" id="jarvis-hud-container">
      {/* Visual Hologram Display */}
      <div className="relative flex flex-col items-center justify-center border border-cyan-500/20 bg-slate-950/70 p-8 rounded-2xl backdrop-blur-md overflow-hidden">
        {/* Futuristic background grid lines */}
        <div className="absolute inset-0 bg-[linear-gradient(rgba(6,182,212,0.05)_1px,transparent_1px),linear-gradient(90deg,rgba(6,182,212,0.05)_1px,transparent_1px)] bg-[size:24px_24px]" />
        
        {/* Corner HUD framing brackets */}
        <div className="absolute top-0 left-0 w-6 h-6 border-t-2 border-l-2 border-cyan-400/60 rounded-tl-sm" />
        <div className="absolute top-0 right-0 w-6 h-6 border-t-2 border-r-2 border-cyan-400/60 rounded-tr-sm" />
        <div className="absolute bottom-0 left-0 w-6 h-6 border-b-2 border-l-2 border-cyan-400/60 rounded-bl-sm" />
        <div className="absolute bottom-0 right-0 w-6 h-6 border-b-2 border-r-2 border-cyan-400/60 rounded-br-sm" />

        {/* HUD top-right status stats */}
        <div className="absolute top-4 right-4 flex items-center gap-3 text-[10px] tracking-widest text-cyan-400/70 font-mono">
          <span className="flex items-center gap-1">
            <Shield className="w-3 h-3 text-cyan-400" /> VIVO-Y29.SYS
          </span>
          <span className="flex items-center gap-1">
            <Radio className="w-3 h-3 text-cyan-400 animate-ping" /> ONLINE
          </span>
        </div>

        {/* HUD top-left branding */}
        <div className="absolute top-4 left-4 flex items-center gap-2 text-[10px] tracking-widest text-cyan-400/70 font-mono">
          <Cpu className="w-3.5 h-3.5 text-cyan-400 animate-spin" /> J.A.R.V.I.S. v3.7
        </div>

        {/* Central Tactical Orb */}
        <div className="relative flex items-center justify-center my-8 h-64 w-64 z-10">
          {/* Animated rings */}
          <motion.div
            animate={{ rotate: isThinking ? -360 : 360 }}
            transition={{ duration: 12, repeat: Infinity, ease: "linear" }}
            className={`absolute inset-0 border border-dashed rounded-full ${rippleColor} scale-100`}
          />
          <motion.div
            animate={{ rotate: isThinking ? 360 : -360 }}
            transition={{ duration: 18, repeat: Infinity, ease: "linear" }}
            className={`absolute inset-4 border border-cyan-400/10 rounded-full scale-100`}
          />
          <motion.div
            animate={{ rotate: 360 }}
            transition={{ duration: 25, repeat: Infinity, ease: "linear" }}
            className="absolute inset-8 border border-dotted border-cyan-400/20 rounded-full"
          />

          {/* Glowing Ripple Waves (Listening/Speaking) */}
          {(isListening || isSpeaking) && (
            <>
              <motion.div
                animate={{ scale: [1, 1.4, 1.6, 1], opacity: [0.6, 0.4, 0.2, 0.6] }}
                transition={{ duration: 2, repeat: Infinity, ease: "easeInOut" }}
                className={`absolute inset-8 border-2 rounded-full ${rippleColor}`}
              />
              <motion.div
                animate={{ scale: [1, 1.2, 1.5, 1], opacity: [0.5, 0.3, 0.1, 0.5] }}
                transition={{ duration: 2.5, repeat: Infinity, ease: "easeInOut", delay: 0.5 }}
                className={`absolute inset-12 border rounded-full ${rippleColor}`}
              />
            </>
          )}

          {/* Central Trigger Button Orb */}
          <motion.button
            whileTap={{ scale: 0.95 }}
            onClick={onToggleListen}
            className={`relative w-40 h-40 rounded-full border-4 flex flex-col items-center justify-center cursor-pointer transition-all duration-300 shadow-2xl backdrop-blur-lg ${orbClass}`}
          >
            {isListening ? (
              <MicOff className="w-12 h-12 text-red-400 mb-2 animate-pulse" />
            ) : isSpeaking ? (
              <Volume2 className="w-12 h-12 text-emerald-400 mb-2 animate-bounce" />
            ) : (
              <Mic className="w-12 h-12 text-cyan-400 mb-2 hover:text-cyan-300 transition-colors" />
            )}
            
            <span className="text-[10px] tracking-[0.2em] font-mono font-bold text-center text-cyan-200/90 max-w-[80%] uppercase">
              {isListening ? "STANDBY" : isSpeaking ? "SPEAKING" : "TAP TO TALK"}
            </span>
          </motion.button>
        </div>

        {/* Central HUD Status Tag */}
        <div className="flex flex-col items-center gap-1 z-10">
          <div className="px-3 py-1 bg-cyan-950/60 border border-cyan-400/30 rounded-full text-[10px] font-mono tracking-widest text-cyan-300 font-bold shadow-[0_0_12px_rgba(34,211,238,0.2)]">
            {statusText}
          </div>
          
          {/* Active speech transcript */}
          <div className="mt-4 min-h-[40px] max-w-md text-center">
            {transcript ? (
              <p className="text-cyan-100 text-sm italic font-mono bg-cyan-950/25 px-4 py-2 rounded-lg border border-cyan-500/10">
                &ldquo;{transcript}&rdquo;
              </p>
            ) : (
              <p className="text-cyan-400/40 text-xs tracking-wider font-mono">
                System listening for Hindi or English commands...
              </p>
            )}
          </div>
        </div>
      </div>

      {/* Terminal Systems Logger / Voice Console */}
      <div className="border border-cyan-500/20 bg-slate-950/80 rounded-xl p-4 font-mono text-xs flex flex-col gap-3 min-h-[220px] shadow-lg relative">
        <div className="flex items-center justify-between border-b border-cyan-500/10 pb-2 mb-1">
          <div className="flex items-center gap-2 text-cyan-400">
            <Terminal className="w-4 h-4" />
            <span className="font-bold tracking-wider">JARVIS CONSOLE LOGS</span>
          </div>
          <div className="flex items-center gap-1.5">
            <span className={`w-2 h-2 rounded-full ${systemStatus.microphonePermission ? 'bg-emerald-500' : 'bg-red-500 animate-ping'}`} title="Mic" />
            <span className={`w-2 h-2 rounded-full ${systemStatus.apiConnected ? 'bg-emerald-500' : 'bg-amber-500'}`} title="Gemini API" />
            <span className={`w-2 h-2 rounded-full ${systemStatus.online ? 'bg-emerald-500' : 'bg-red-500'}`} title="Online" />
          </div>
        </div>

        {/* Scrollable logs */}
        <div className="flex flex-col gap-2 overflow-y-auto max-h-56 pr-2 scrollbar-thin scrollbar-thumb-cyan-500/20">
          {voiceLogs.length === 0 ? (
            <div className="text-cyan-400/30 text-center py-6">
              [SYSTEM INITIALIZED - WAITING FOR INPUT]
            </div>
          ) : (
            voiceLogs.map((log) => (
              <div
                key={log.id}
                className={`p-2.5 rounded border border-l-4 ${
                  log.type === "command"
                    ? "bg-cyan-950/20 border-cyan-500/30 border-l-cyan-400 text-cyan-100"
                    : log.type === "gemini"
                    ? "bg-slate-900/60 border-cyan-500/10 border-l-emerald-400 text-emerald-100"
                    : log.type === "error"
                    ? "bg-red-950/20 border-red-500/20 border-l-red-500 text-red-200"
                    : "bg-slate-900/40 border-slate-700/50 border-l-slate-400 text-cyan-400/70"
                }`}
              >
                <div className="flex justify-between items-center text-[10px] opacity-60 mb-1">
                  <span>[{log.type.toUpperCase()}]</span>
                  <span>{log.timestamp}</span>
                </div>
                
                {log.type === "command" && (
                  <div className="font-semibold text-cyan-300">
                    &gt; User: &ldquo;{log.text}&rdquo;
                  </div>
                )}
                
                {log.response && (
                  <div className="mt-1 font-serif text-xs leading-relaxed pl-1.5 border-l border-cyan-500/10">
                    {log.response}
                  </div>
                )}
                
                {log.actionTriggered && (
                  <div className="mt-1 text-[10px] text-amber-300 font-semibold uppercase tracking-wider flex items-center gap-1">
                    ⚡ ACTION TRIGGERED: {log.actionTriggered}
                  </div>
                )}
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}
