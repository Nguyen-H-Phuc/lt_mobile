package com.example.project183.service;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class ImageHandler {
    private final Activity activity;
    private final int imageSize;
    private Uri photoUri;

    public static final int REQUEST_CAMERA = 100;
    public static final int REQUEST_GALLERY = 200;
    public static final int REQUEST_CAMERA_PERMISSION = 300;
    public static final int REQUEST_STORAGE_PERMISSION = 400;

    public ImageHandler(Activity activity, int imageSize) {
        this.activity = activity;
        this.imageSize = imageSize;
    }

    public void showImagePickerDialog() {
        String[] options = {"Chụp ảnh", "Chọn từ thư viện"};
        new AlertDialog.Builder(activity)
                .setTitle("Chọn ảnh")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        checkCameraPermission();
                    } else {
                        checkStoragePermission();
                    }
                }).show();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        } else {
            openCamera();
        }
    }

    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        REQUEST_STORAGE_PERMISSION);
            } else {
                openGallery();
            }
        } else {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_STORAGE_PERMISSION);
            } else {
                openGallery();
            }
        }
    }

    public void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = File.createTempFile(
                        "photo_", ".jpg",
                        activity.getExternalCacheDir()
                );
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(
                        activity,
                        activity.getPackageName() + ".provider",
                        photoFile
                );
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                activity.startActivityForResult(intent, REQUEST_CAMERA);
            }
        }
    }

    public void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        activity.startActivityForResult(intent, REQUEST_GALLERY);
    }

    public void handleActivityResult(int requestCode, int resultCode, Intent data, Consumer<Bitmap> onImageReady) {
        if (resultCode == Activity.RESULT_OK) {
            try {
                Bitmap image;
                if (requestCode == REQUEST_CAMERA) {
                    image = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), photoUri);
                } else if (requestCode == REQUEST_GALLERY && data != null) {
                    Uri imageUri = data.getData();
                    image = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), imageUri);
                } else {
                    return;
                }

                if (image != null) {
                    int dimension = Math.min(image.getWidth(), image.getHeight());
                    image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
                    image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                    onImageReady.accept(image); // gọi callback
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(activity, "Lỗi khi đọc ảnh", Toast.LENGTH_SHORT).show();
            }
        }
    }
}