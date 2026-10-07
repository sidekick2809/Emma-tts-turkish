import hashlib
import io
import os
import re
import traceback
from typing import Optional, List
import numpy as np
import soundfile as sf
from fastapi import FastAPI, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import Response, JSONResponse
from pydantic import BaseModel

app = FastAPI(
    title="Emma TTS Backend (EMA Lightning)",
    description="High-speed Turkish Text-to-Speech API powered by EMA Lightning for the Emma Android App",
    version="1.0.0"
)

# Enable CORS for local development and mobile network access
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Cache directory for generated audio files
CACHE_DIR = os.path.join(os.path.dirname(__file__), "cache")
os.makedirs(CACHE_DIR, exist_ok=True)

# Global TTS model instance
tts_model = None

def get_tts_model():
    """Lazy-load the EMA Lightning TTS model."""
    global tts_model
    if tts_model is None:
        try:
            print("[Emma TTS] Loading EMA Lightning model...")
            from ema_lightning import EMA
            tts_model = EMA()
            print("[Emma TTS] Model loaded successfully!")
        except Exception as e:
            print(f"[Emma TTS] Error loading model: {e}")
            traceback.print_exc()
            raise HTTPException(
                status_code=500,
                detail=f"Model yüklenemedi: {str(e)}. 'pip install ema-lightning' kurulu olduğundan emin olun."
            )
    return tts_model

class TTSRequest(BaseModel):
    text: str
    split_sentences: Optional[bool] = True

def split_text_into_sentences(text: str) -> List[str]:
    """Splits long Turkish text into sentence chunks to ensure smooth generation."""
    # Split by sentence ending punctuations or newlines
    sentences = re.split(r'(?<=[.!?…\n])\s+', text.strip())
    # Filter empty items and strip
    cleaned = [s.strip() for s in sentences if s.strip()]
    return cleaned if cleaned else [text.strip()]

def synthesize_audio(text: str, split_sentences: bool = True) -> bytes:
    """Synthesizes Turkish speech from text and returns WAV audio bytes."""
    text = text.strip()
    if not text:
        raise ValueError("Seslendirilecek metin boş olamaz.")

    # Calculate MD5 hash for caching
    cache_key = hashlib.md5(text.encode("utf-8")).hexdigest()
    cache_path = os.path.join(CACHE_DIR, f"{cache_key}.wav")

    if os.path.exists(cache_path):
        with open(cache_path, "rb") as f:
            return f.read()

    model = get_tts_model()

    if split_sentences and len(text) > 120:
        sentences = split_text_into_sentences(text)
        print(f"[Emma TTS] Processing {len(sentences)} sentence chunks...")
        speech_list = model.say(sentences)
        
        # Combine all audio chunks with a slight natural silence pause (0.15s)
        sample_rate = speech_list[0].sample_rate if speech_list else 24000
        silence = np.zeros(int(sample_rate * 0.15), dtype=np.float32)
        
        combined_audio = []
        for i, s in enumerate(speech_list):
            combined_audio.append(s.audio)
            if i < len(speech_list) - 1:
                combined_audio.append(silence)
        
        full_audio = np.concatenate(combined_audio, axis=0)
    else:
        speech = model.say(text)
        full_audio = speech.audio
        sample_rate = speech.sample_rate

    # Convert numpy float32 audio to 16-bit PCM WAV bytes
    buffer = io.BytesIO()
    sf.write(buffer, full_audio, sample_rate, format='WAV', subtype='PCM_16')
    wav_bytes = buffer.getvalue()

    # Save to cache
    try:
        with open(cache_path, "wb") as f:
            f.write(wav_bytes)
    except Exception as e:
        print(f"[Emma TTS] Cache write error: {e}")

    return wav_bytes

@app.get("/")
def root():
    return {
        "app": "Emma TTS Backend",
        "model": "EMA Lightning (canberkkkkkk/ema-lightning)",
        "status": "ready",
        "description": "Android Emma uygulaması için Türkçe TTS sunucusu",
        "endpoints": {
            "health": "/health",
            "tts_post": "/tts (JSON: {'text': '...'})",
            "tts_get": "/tts?text=Merhaba"
        }
    }

@app.get("/health")
def health_check():
    """Health check endpoint for Emma Android app connection verification."""
    return {"status": "ok", "app": "Emma"}

@app.post("/tts")
def tts_post(req: TTSRequest):
    """Generates speech for the requested text via POST."""
    try:
        wav_bytes = synthesize_audio(req.text, split_sentences=req.split_sentences)
        return Response(content=wav_bytes, media_type="audio/wav")
    except Exception as e:
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/tts")
def tts_get(text: str = Query(..., description="Seslendirilecek Türkçe metin")):
    """Generates speech for the requested text via GET (browser-friendly)."""
    try:
        wav_bytes = synthesize_audio(text)
        return Response(content=wav_bytes, media_type="audio/wav")
    except Exception as e:
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    print("[Emma TTS] Sunucu başlatılıyor: http://0.0.0.0:8000")
    print("Android telefonunuzdan aynı Wi-Fi ağındaki bilgisayarınızın IP adresiyle bağlanabilirsiniz.")
    uvicorn.run(app, host="0.0.0.0", port=8000)
