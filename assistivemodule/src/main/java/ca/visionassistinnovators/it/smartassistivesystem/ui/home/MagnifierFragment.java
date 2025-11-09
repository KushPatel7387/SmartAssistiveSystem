package ca.visionassistinnovators.it.smartassistivesystem.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.core.ZoomState;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

import ca.visionassistinnovators.it.smartassistivesystem.R;

public class MagnifierFragment extends Fragment {

    private static final String TAG = "MagnifierFragment";

    private PreviewView previewView;
    private ImageButton btnFlashlight;
    private Camera camera;
    private boolean flashOn = false;

    private ActivityResultLauncher<String> cameraPermissionLauncher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_magnifier, container, false);

        previewView   = root.findViewById(R.id.previewView);
        btnFlashlight = root.findViewById(R.id.btnFlashlight);

        cameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        startCamera();
                    } else {
                        Toast.makeText(requireContext(), R.string.camera_permission_denied, Toast.LENGTH_SHORT).show();
                    }
                });

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                Toast.makeText(requireContext(), R.string.camera_permission_rationale, Toast.LENGTH_SHORT).show();
            }
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }

        btnFlashlight.setOnClickListener(v -> toggleFlash());
        setupZoomGesture();

        return root;
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                cameraProvider.unbindAll();
                // Use viewLifecycleOwner in Fragments
                camera = cameraProvider.bindToLifecycle(getViewLifecycleOwner(), cameraSelector, preview);

                // Hide flashlight if device lacks flash
                if (camera.getCameraInfo() != null && camera.getCameraInfo().hasFlashUnit()) {
                    btnFlashlight.setVisibility(View.VISIBLE);
                } else {
                    btnFlashlight.setVisibility(View.GONE);
                }

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting camera: " + e.getMessage(), e);
                Toast.makeText(requireContext(), R.string.camera_start_error, Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void setupZoomGesture() {
        final ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(
                requireContext(),
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    @Override
                    public boolean onScale(ScaleGestureDetector detector) {
                        if (camera == null || camera.getCameraInfo() == null) return false;
                        ZoomState zs = camera.getCameraInfo().getZoomState().getValue();
                        if (zs == null) return false;

                        float current = zs.getZoomRatio();
                        float next = current * detector.getScaleFactor();
                        float clamped = Math.max(zs.getMinZoomRatio(), Math.min(next, zs.getMaxZoomRatio()));
                        camera.getCameraControl().setZoomRatio(clamped);
                        return true;
                    }
                });

        previewView.setOnTouchListener((v, event) -> {
            scaleGestureDetector.onTouchEvent(event);
            // Consume move/pinch; let taps fall through if needed later
            return event.getPointerCount() > 1 || event.getAction() == MotionEvent.ACTION_MOVE;
        });
    }

    private void toggleFlash() {
        if (camera != null && camera.getCameraInfo() != null && camera.getCameraInfo().hasFlashUnit()) {
            flashOn = !flashOn;
            camera.getCameraControl().enableTorch(flashOn);
            Toast.makeText(
                    requireContext(),
                    flashOn ? R.string.flash_on : R.string.flash_off,
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            Toast.makeText(requireContext(), R.string.flash_not_supported, Toast.LENGTH_SHORT).show();
        }
    }
}
