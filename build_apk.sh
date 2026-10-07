#!/usr/bin/env bash
set -e

# Emma TTS - Tek Komutla APK Derleme Betiği
export JAVA_HOME="${JAVA_HOME:-$HOME/.jdk17/amazon-corretto-17.jdk/Contents/Home}"
export ANDROID_HOME="${ANDROID_HOME:-$HOME/.android-sdk}"
export PATH="$JAVA_HOME/bin:$PATH"
export COPYFILE_DISABLE=1

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "==> 🧹 FAT32 gölge dosyaları temizleniyor..."
find "$DIR" -name "._*" -delete 2>/dev/null || true

echo "==> 🔨 Android APK derleniyor (assembleDebug)..."
cd "$DIR/android"
./gradlew assembleDebug

echo "==> 📦 APK kopyalanıyor..."
cp /tmp/emma-build/app/outputs/apk/debug/app-debug.apk "$DIR/Emma.apk"

echo "==> ✅ Başarılı! APK dosyası hazır:"
ls -lh "$DIR/Emma.apk"
