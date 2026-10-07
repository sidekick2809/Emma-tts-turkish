#!/usr/bin/env python3
"""
Emma TTS Test Script
Bu betik Emma TTS sunucusuna istek atıp ses dosyasını kaydeder.
"""
import sys
import requests

SERVER_URL = "http://localhost:8000"

def test_tts(text="Merhaba! Ben Emma. Türkçe metinleri hızlı ve doğal bir şekilde seslendiriyorum."):
    print(f"[1] Sunucu kontrol ediliyor: {SERVER_URL}/health")
    try:
        r = requests.get(f"{SERVER_URL}/health", timeout=5)
        print("    Durum:", r.json())
    except Exception as e:
        print(f"    Hata: Sunucuya ulaşılamadı ({e})")
        print("    Önce sunucuyu başlatın: python server/server.py")
        return

    print(f"\n[2] Metin seslendiriliyor: '{text}'")
    try:
        res = requests.post(
            f"{SERVER_URL}/tts",
            json={"text": text, "split_sentences": True},
            timeout=30
        )
        if res.status_code == 200:
            output_file = "test_output.wav"
            with open(output_file, "wb") as f:
                f.write(res.content)
            print(f"    Başarılı! Ses dosyası kaydedildi: {output_file} ({len(res.content)} bayt)")
        else:
            print(f"    Hata kodu {res.status_code}: {res.text}")
    except Exception as e:
        print(f"    İstek hatası: {e}")

if __name__ == "__main__":
    test_text = sys.argv[1] if len(sys.argv) > 1 else "Merhaba! Ben Emma. Metin okuma sistemim hazır."
    test_tts(test_text)
