#!/usr/bin/env bash
set -e

echo "=== Emma TTS Backend Başlatıcı ==="

# Check virtual environment or python
if [ ! -d "venv" ]; then
    echo "Python sanal ortamı oluşturuluyor (venv)..."
    python3 -m venv venv
fi

source venv/bin/activate

echo "Gereksinimler yükleniyor / kontrol ediliyor..."
pip install -r requirements.txt

echo "Sunucu başlatılıyor (0.0.0.0:8000)..."
python3 server.py
