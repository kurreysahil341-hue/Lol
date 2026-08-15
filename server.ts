import express from "express";
import path from "path";
import dotenv from "dotenv";
import { GoogleGenAI } from "@google/genai";
import { createServer as createViteServer } from "vite";

dotenv.config();

async function startServer() {
  const app = express();
  const PORT = 3000;

  app.use(express.json());

  // Shared Gemini client setup on the server
  const apiKey = process.env.GEMINI_API_KEY || "";
  let ai: GoogleGenAI | null = null;
  
  if (apiKey) {
    ai = new GoogleGenAI({
      apiKey: apiKey,
      httpOptions: {
        headers: {
          'User-Agent': 'aistudio-build',
        }
      }
    });
    console.log("Server: Gemini API client initialized with process.env.GEMINI_API_KEY");
  } else {
    console.warn("Server: GEMINI_API_KEY is not defined in environment variables.");
  }

  // API proxy endpoint for Gemini requests
  app.post("/api/gemini/generate", async (req, res) => {
    try {
      const { prompt, systemInstruction } = req.body;
      if (!prompt) {
        return res.status(400).json({ error: "Prompt is required" });
      }

      // Check for user-provided client key fallback
      const clientApiKey = req.headers["x-client-api-key"] as string;
      let activeAi = ai;

      if (clientApiKey && clientApiKey.trim() !== "" && clientApiKey !== "YOUR_GEMINI_API_KEY_HERE") {
        activeAi = new GoogleGenAI({
          apiKey: clientApiKey,
          httpOptions: {
            headers: {
              'User-Agent': 'aistudio-build',
            }
          }
        });
      }

      if (!activeAi) {
        return res.status(500).json({ 
          error: "Gemini API client not initialized. Please specify an API key." 
        });
      }

      const response = await activeAi.models.generateContent({
        model: "gemini-3.7-flash",
        contents: prompt,
        config: {
          systemInstruction: systemInstruction || "You are Jarvis, the highly intelligent, witty, and loyal AI assistant to Tony Stark. Respond in a smooth, classy, sci-fi manner.",
          temperature: 0.7,
        }
      });

      res.json({ text: response.text });
    } catch (error: any) {
      console.error("Gemini API Error:", error);
      res.status(500).json({ error: error.message || "Failed to generate content from Gemini" });
    }
  });

  // Health check
  app.get("/api/health", (req, res) => {
    res.json({ status: "ok", serverTime: new Date().toISOString() });
  });

  // Vite middleware setup for development, standard serving for production
  if (process.env.NODE_ENV !== "production" && process.env.DISABLE_HMR !== "true") {
    console.log("Starting server in development mode with Vite middleware...");
    const vite = await createViteServer({
      server: { middlewareMode: true },
      appType: "spa",
    });
    app.use(vite.middlewares);
  } else {
    console.log("Starting server in production static mode...");
    const distPath = path.join(process.cwd(), "dist");
    app.use(express.static(distPath));
    app.get("*", (req, res) => {
      res.sendFile(path.join(distPath, "index.html"));
    });
  }

  app.listen(PORT, "0.0.0.0", () => {
    console.log(`Server running on http://0.0.0.0:${PORT}`);
  });
}

startServer().catch((err) => {
  console.error("Failed to start server:", err);
});
