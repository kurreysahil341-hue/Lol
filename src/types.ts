/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

export interface Contact {
  id: string;
  name: string;
  phoneNumber: string;
  relationship?: string;
}

export interface VoiceLog {
  id: string;
  timestamp: string;
  text: string;
  response: string;
  type: "command" | "gemini" | "error" | "system";
  actionTriggered?: string;
}

export interface PhotoAsset {
  id: string;
  timestamp: string;
  dataUrl: string;
  filter?: string;
}

export interface SystemStatus {
  microphonePermission: boolean;
  cameraPermission: boolean;
  voiceEngineReady: boolean;
  apiConnected: boolean;
  online: boolean;
}
