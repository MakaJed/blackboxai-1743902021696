# NDV Detection Android App

## Key Features
- Real-time image analysis for NDV symptoms
- Camera integration using CameraX
- TensorFlow Lite model inference
- Simple results interface

## Setup Guide

1. Requirements:
- Android Studio 2022+
- Android SDK 33
- Physical device/emulator (API 24+)

2. Installation:
```bash
git clone [repository_url]
cd NDVDetector
```

3. Model Preparation:
- Convert your model to .tflite format
- Place in: app/src/main/assets/

4. Building:
- Open in Android Studio
- Click "Build > Make Project"
- Run on connected device

## Technical Details
- Minimum SDK: 24
- Dependencies:
  - CameraX 1.2.3
  - TensorFlow Lite 2.12.0