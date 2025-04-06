package com.example.ndvdetector;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageCapture;
import androidx.camera.view.CameraController;
import androidx.camera.view.LifecycleCameraController;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {
    private PreviewView previewView;
    private Button captureButton;
    private Button analyzeButton;
    private ImageView resultImage;
    private TextView resultText;
    private Bitmap currentImage;
    private ImageCapture imageCapture;
    private CameraController cameraController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.previewView);
        captureButton = findViewById(R.id.captureButton);
        analyzeButton = findViewById(R.id.analyzeButton);
        resultImage = findViewById(R.id.resultImage);
        resultText = findViewById(R.id.resultText);

        // Initialize camera
        cameraController = new LifecycleCameraController(this);
        cameraController.bindToLifecycle(this);
        previewView.setController(cameraController);

        captureButton.setOnClickListener(v -> captureImage());
        analyzeButton.setOnClickListener(v -> analyzeImage());
    }

    private void captureImage() {
        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        cameraController.setEnabledUseCases(CameraController.IMAGE_CAPTURE);
        cameraController.takePicture(
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(ImageCapture.OutputFileResults outputFileResults) {
                        super.onCaptureSuccess(outputFileResults);
                        // Convert to bitmap
                        ByteBuffer buffer = outputFileResults.getSavedImage().getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.remaining()];
                        buffer.get(bytes);
                        currentImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        
                        // Show preview
                        runOnUiThread(() -> {
                            resultImage.setImageBitmap(currentImage);
                            resultText.setText("Image captured. Ready to analyze.");
                        });
                    }

                    @Override
                    public void onError(ImageCaptureException exception) {
                        super.onError(exception);
                        runOnUiThread(() -> 
                            Toast.makeText(MainActivity.this, "Capture failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                }
        );
    }

    private void analyzeImage() {
        if (currentImage == null) {
            Toast.makeText(this, "Please capture an image first", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Load model
            NDVDetectorModel model = NDVDetectorModel.newInstance(this);

            // Resize image to 224x224 (model input size)
            Bitmap resizedImage = Bitmap.createScaledBitmap(currentImage, 224, 224, true);

            // Convert to TensorImage
            TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
            tensorImage.load(resizedImage);
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            inputFeature0.loadBuffer(tensorImage.getBuffer());

            // Run inference
            NDVDetectorModel.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            // Get prediction
            float confidence = outputFeature0.getFloatValue(0);
            String result = confidence > 0.5 ? "NDV Detected" : "No NDV Detected";

            // Display result
            resultText.setText(String.format("%s (%.1f%% confidence)", result, confidence * 100));

            // Clean up
            model.close();
        } catch (IOException e) {
            Toast.makeText(this, "Error analyzing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}