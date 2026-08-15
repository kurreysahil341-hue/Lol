/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useRef, useState, useEffect } from "react";
import { Camera, RefreshCw, Aperture, Image as ImageIcon, Trash2, Eye } from "lucide-react";
import { PhotoAsset } from "../types";

interface JarvisCameraProps {
  onPhotoCaptured: (photo: Omit<PhotoAsset, "id">) => void;
  photos: PhotoAsset[];
  onDeletePhoto: (id: string) => void;
  // External voice-command shutter trigger
  voiceTriggerShutter: boolean;
  onResetVoiceTrigger: () => void;
}

export default function JarvisCamera({
  onPhotoCaptured,
  photos,
  onDeletePhoto,
  voiceTriggerShutter,
  onResetVoiceTrigger,
}: JarvisCameraProps) {
  const videoRef = useRef<HTMLVideoElement | null>(null);
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const [stream, setStream] = useState<MediaStream | null>(null);
  const [active, setActive] = useState(false);
  const [cameraError, setCameraError] = useState("");
  const [isCapturing, setIsCapturing] = useState(false);
  const [facingMode, setFacingMode] = useState<"user" | "environment">("user");
  const [selectedPhoto, setSelectedPhoto] = useState<PhotoAsset | null>(null);

  // Auto shutter-click when voice triggers
  useEffect(() => {
    if (voiceTriggerShutter && active) {
      takePhoto();
      onResetVoiceTrigger();
    }
  }, [voiceTriggerShutter, active]);

  // Handle camera init / release
  useEffect(() => {
    if (active) {
      initCamera();
    } else {
      stopCamera();
    }
    return () => stopCamera();
  }, [active, facingMode]);

  const initCamera = async () => {
    setCameraError("");
    try {
      if (stream) {
        stopCamera();
      }

      const mediaStream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: facingMode },
        audio: false,
      });

      setStream(mediaStream);
      if (videoRef.current) {
        videoRef.current.srcObject = mediaStream;
      }
    } catch (err: any) {
      console.error("Camera access error:", err);
      setCameraError(
        "Could not access camera. Please authorize microphone/camera permissions in your browser or device."
      );
      setActive(false);
    }
  };

  const stopCamera = () => {
    if (stream) {
      stream.getTracks().forEach((track) => track.stop());
      setStream(null);
    }
  };

  const takePhoto = () => {
    if (!videoRef.current || !canvasRef.current || isCapturing) return;

    setIsCapturing(true);

    const video = videoRef.current;
    const canvas = canvasRef.current;
    const context = canvas.getContext("2d");

    if (context) {
      // Fit canvas to video stream resolution
      canvas.width = video.videoWidth || 640;
      canvas.height = video.videoHeight || 480;

      // Draw current video frame
      context.drawImage(video, 0, 0, canvas.width, canvas.height);

      // Play audio shutter sound natively in browser
      try {
        const audioCtx = new (window.AudioContext || (window as any).webkitAudioContext)();
        const osc = audioCtx.createOscillator();
        const gainNode = audioCtx.createGain();
        osc.connect(gainNode);
        gainNode.connect(audioCtx.destination);
        osc.type = "sine";
        osc.frequency.setValueAtTime(800, audioCtx.currentTime);
        gainNode.gain.setValueAtTime(0.3, audioCtx.currentTime);
        gainNode.gain.exponentialRampToValueAtTime(0.01, audioCtx.currentTime + 0.15);
        osc.start();
        osc.stop(audioCtx.currentTime + 0.15);
      } catch (e) {
        // Fallback if audio context fails
      }

      // Convert to Base64
      const dataUrl = canvas.toDataURL("image/png");
      
      // Delay to simulate tactical scan analysis
      setTimeout(() => {
        onPhotoCaptured({
          timestamp: new Date().toLocaleTimeString(),
          dataUrl,
          filter: "neon-hud",
        });
        setIsCapturing(false);
      }, 800);
    }
  };

  const toggleFacingMode = () => {
    setFacingMode((prev) => (prev === "user" ? "environment" : "user"));
  };

  return (
    <div className="border border-cyan-500/20 bg-slate-950/70 p-6 rounded-2xl backdrop-blur-md relative overflow-hidden" id="jarvis-camera-panel">
      {/* Visual HUD Corner markings */}
      <div className="absolute top-0 left-0 w-3 h-3 border-t border-l border-cyan-400/30" />
      <div className="absolute bottom-0 right-0 w-3 h-3 border-b border-r border-cyan-400/30" />

      <div className="flex items-center justify-between border-b border-cyan-500/10 pb-4 mb-4">
        <div className="flex items-center gap-2">
          <Camera className="w-5 h-5 text-cyan-400 animate-pulse" />
          <h2 className="text-sm font-bold tracking-widest text-cyan-200 uppercase font-mono">
            Jarvis In-App Tactical Camera
          </h2>
        </div>

        <button
          onClick={() => setActive(!active)}
          className={`px-3 py-1 border rounded text-[11px] font-mono tracking-wider transition-all duration-300 ${
            active
              ? "bg-red-950/30 border-red-500/40 text-red-400"
              : "bg-cyan-950/50 border-cyan-400/30 text-cyan-300"
          }`}
        >
          {active ? "OFFLINE CAMERA" : "INITIALIZE CAMERA"}
        </button>
      </div>

      <p className="text-[11px] text-cyan-400/60 font-mono mb-4 leading-relaxed">
        HINDI: Camera on karne ke baad, aap voice se &quot;click photo&quot;, &quot;photo click karo&quot;, ya &quot;capture&quot; bolkar photo kheench sakte hain.
      </p>

      {/* Main Camera Viewport */}
      {active ? (
        <div className="relative border border-cyan-500/30 rounded-xl overflow-hidden aspect-video bg-slate-900 shadow-[0_0_20px_rgba(6,182,212,0.1)] mb-4">
          <video
            ref={videoRef}
            autoPlay
            playsInline
            muted
            className="w-full h-full object-cover transform scale-x-[-1]"
          />

          {/* Tactical HUD Overlay graphics */}
          <div className="absolute inset-0 border border-cyan-400/15 pointer-events-none">
            {/* Center crosshair */}
            <div className="absolute inset-0 flex items-center justify-center">
              <div className="w-12 h-12 border border-cyan-400/30 rounded-full flex items-center justify-center">
                <div className="w-2 h-2 bg-cyan-400/40 rounded-full" />
              </div>
              <div className="absolute w-8 h-[1px] bg-cyan-400/30" />
              <div className="absolute h-8 w-[1px] bg-cyan-400/30" />
            </div>

            {/* Scanning Laser Line effect */}
            {isCapturing && (
              <div className="absolute left-0 right-0 h-0.5 bg-cyan-400 shadow-[0_0_8px_rgba(6,182,212,0.8)] animate-bounce" />
            )}

            {/* Corner Bracket Lines */}
            <div className="absolute top-4 left-4 w-4 h-4 border-t-2 border-l-2 border-cyan-400/80" />
            <div className="absolute top-4 right-4 w-4 h-4 border-t-2 border-r-2 border-cyan-400/80" />
            <div className="absolute bottom-4 left-4 w-4 h-4 border-b-2 border-l-2 border-cyan-400/80" />
            <div className="absolute bottom-4 right-4 w-4 h-4 border-b-2 border-r-2 border-cyan-400/80" />

            {/* Overlay indicators */}
            <div className="absolute bottom-4 left-4 font-mono text-[9px] tracking-widest text-cyan-400/70 uppercase">
              FPS: 30 // EXP: AUTO // RES: 1080P
            </div>
            <div className="absolute top-4 left-4 font-mono text-[9px] tracking-widest text-red-500 font-bold flex items-center gap-1">
              <span className="w-1.5 h-1.5 rounded-full bg-red-500 animate-ping" />
              REC HUD_VIEW
            </div>
          </div>

          {/* Analysing image overlay loader */}
          {isCapturing && (
            <div className="absolute inset-0 bg-slate-950/80 flex flex-col items-center justify-center gap-3 font-mono z-20">
              <Aperture className="w-10 h-10 text-cyan-400 animate-spin" />
              <span className="text-xs tracking-widest text-cyan-300 animate-pulse">
                SCANNING FIELD &amp; CAPTURING...
              </span>
            </div>
          )}

          {/* Control Bar inside Viewport */}
          <div className="absolute bottom-4 right-4 flex items-center gap-3 z-10">
            <button
              onClick={toggleFacingMode}
              className="p-2 bg-slate-950/80 hover:bg-slate-900 border border-cyan-500/20 rounded-full text-cyan-300 transition-colors"
              title="Flip Camera orientation"
            >
              <RefreshCw className="w-4 h-4" />
            </button>

            <button
              onClick={takePhoto}
              className="px-4 py-2 bg-cyan-500 hover:bg-cyan-400 text-slate-950 rounded-lg text-xs font-mono font-bold uppercase tracking-wider flex items-center gap-1.5 shadow-[0_0_15px_rgba(6,182,212,0.4)] transition-all"
            >
              <Aperture className="w-4 h-4" />
              SHUTTER
            </button>
          </div>
        </div>
      ) : (
        <div className="relative border border-cyan-500/10 rounded-xl aspect-video bg-slate-950/40 flex flex-col items-center justify-center gap-3 py-16 mb-4">
          <Aperture className="w-12 h-12 text-cyan-400/20" />
          <span className="text-xs font-mono text-cyan-400/30 uppercase tracking-widest">
            Tactical optical scanner stands dark
          </span>
          {cameraError && (
            <p className="text-[10px] text-red-400 text-center font-mono max-w-sm px-4">
              {cameraError}
            </p>
          )}
        </div>
      )}

      {/* Hidden processing canvas */}
      <canvas ref={canvasRef} className="hidden" />

      {/* Captured Image Gallery */}
      <div className="border-t border-cyan-500/10 pt-4">
        <div className="flex items-center gap-2 mb-3">
          <ImageIcon className="w-4 h-4 text-cyan-400" />
          <span className="text-xs font-mono font-bold tracking-widest text-cyan-200 uppercase">
            Captured Tactical Scans ({photos.length})
          </span>
        </div>

        {photos.length === 0 ? (
          <div className="py-6 text-center text-cyan-400/20 font-mono text-xs border border-dashed border-cyan-500/10 rounded-lg">
            [TACTICAL ENTRANCE EMPTY - CAPTURE FIRST SCANS]
          </div>
        ) : (
          <div className="grid grid-cols-3 sm:grid-cols-4 md:grid-cols-6 gap-3">
            {photos.map((photo) => (
              <div
                key={photo.id}
                className="relative group border border-cyan-500/10 bg-slate-900/30 aspect-square rounded-lg overflow-hidden cursor-pointer hover:border-cyan-400/40 transition-all shadow-md"
              >
                <img
                  src={photo.dataUrl}
                  alt="tactical scan"
                  className="w-full h-full object-cover"
                  referrerPolicy="no-referrer"
                />

                <div className="absolute inset-0 bg-slate-950/60 opacity-0 group-hover:opacity-100 flex items-center justify-center gap-1.5 transition-all duration-300">
                  <button
                    onClick={() => setSelectedPhoto(photo)}
                    className="p-1 bg-cyan-950 border border-cyan-400/30 rounded text-cyan-400 hover:text-cyan-300"
                    title="Examine Scan"
                  >
                    <Eye className="w-3.5 h-3.5" />
                  </button>
                  <button
                    onClick={() => onDeletePhoto(photo.id)}
                    className="p-1 bg-slate-950 border border-red-500/20 rounded text-red-400 hover:bg-red-950/50 hover:text-red-300"
                    title="Delete Scan"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Lightbox Modal for Full Examining of scans */}
      {selectedPhoto && (
        <div className="fixed inset-0 bg-slate-950/95 flex flex-col items-center justify-center p-4 z-50">
          <div className="relative max-w-2xl w-full border border-cyan-400/30 rounded-xl overflow-hidden shadow-[0_0_40px_rgba(6,182,212,0.3)] bg-slate-900">
            {/* Visual tactical border frame */}
            <div className="absolute top-4 left-4 font-mono text-[10px] text-cyan-300 tracking-wider">
              SCAN_REF: {selectedPhoto.id.substring(0, 8).toUpperCase()} // TIME: {selectedPhoto.timestamp}
            </div>

            <img
              src={selectedPhoto.dataUrl}
              alt="Expanded Scan"
              className="w-full h-auto"
              referrerPolicy="no-referrer"
            />

            <div className="bg-slate-950 p-3 flex justify-between items-center border-t border-cyan-500/20 font-mono text-xs">
              <span className="text-cyan-400/60 font-mono">
                [J.A.R.V.I.S. OPTICAL INTELLIGENCE REVIEW]
              </span>
              <button
                onClick={() => setSelectedPhoto(null)}
                className="px-3 py-1 bg-cyan-500 text-slate-950 rounded font-bold uppercase hover:bg-cyan-400 transition-colors"
              >
                Close Review
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
