package com.example.project183.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;

import com.example.project183.Adapter.CategoryAdapter;
import com.example.project183.Adapter.SliderAdapter;
import com.example.project183.Domain.Category;
import com.example.project183.Domain.SliderItems;
import com.example.project183.R;
import com.example.project183.databinding.ActivityMainBinding;
import com.example.project183.ml.Model;
import com.example.project183.service.UserAuthService;
// import com.example.project183.Domain.User; // Không cần import lại ở đây nếu BaseActivity đã có
import com.example.project183.R;
import com.example.project183.databinding.ActivityMainBinding;
import com.example.project183.service.UserAuthService;
// import com.example.project183.service.UserCallback; // Không thấy được sử dụng trực tiếp

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

// import com.ismaeldivita.chipnavigation.ChipNavigationBar; // Không cần import nếu binding đã có

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

public class MainActivity extends BaseActivity {
    ActivityMainBinding binding;
    private UserAuthService userAuthService;
    private Uri photoUri;
    private static final int REQUEST_CAMERA = 100;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final int REQUEST_STORAGE_PERMISSION = 300;
    private static final int REQUEST_GALLERY = 400;
    int imageSize = 224;

    private final Map<String, Integer> categoryMap = new HashMap<String, Integer>() {{
        put("Pizza", 0);
        put("Burger", 1);
        put("Chicken", 2);
        put("Sushi", 3);
        put("Meat", 4);
        put("Hot dog", 5);
        put("Drink", 6);
        put("More", 7);
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // khi người dùng nhấn ImageView cameraIcon
        binding.photoIcon.setOnClickListener(v -> {
            showImagePickerDialog();
        });

        // khi người dùng nhấn ImageView searchIcon
        binding.searchIcon.setOnClickListener(v -> {
            String input = binding.searchEditText.getText().toString().trim();
            if (!input.isEmpty()) {
                searchAndOpenListFoodActivity(input);
            } else {
                Toast.makeText(v.getContext(), "Vui lòng nhập từ khoá", Toast.LENGTH_SHORT).show();
            }
        });

        userAuthService = new UserAuthService();
        // Lấy thông tin người dùng và hiển thị
        userAuthService.getUserToRealtimeDatabase(user -> {
            // kiểm tra thông tin người dùng
            if (user != null) {
                Log.d("DEBUG_USER", "User: " + user.getPhoneNumber());
                // set tên nguười dùng nếu người dùng có tên
                if (user.getName() != null && !user.getName().isEmpty()) {
                    binding.textView2.setText(user.getName());
                } // không tìm thấy tên thì set số điện thoại
                else if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
                    binding.textView2.setText(user.getPhoneNumber());
                } // để trống nếu không tìm thấy tên và thuộc tính
                else {
                    binding.textView2.setText("");
                }
            }
            // Không tìm thấy người dùng thì để trống
            else {
                binding.textView2.setText("");
            }
        });

        initCategory();
        initBanner();
        setVariable(); // Gọi sau khi các view đã được khởi tạo
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.bottomMenu.setItemSelected(R.id.home, true);
    }

    private void initBanner() {
        DatabaseReference myRef = database.getReference("Banners"); // `database` được kế thừa từ BaseActivity
        binding.progressBarBanner.setVisibility(View.VISIBLE);
        ArrayList<SliderItems> items = new ArrayList<>();
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        items.add(issue.getValue(SliderItems.class));
                    }
                    banners(items);
                }
                binding.progressBarBanner.setVisibility(View.GONE); // Luôn ẩn ProgressBar sau khi hoàn tất
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseBanner", "Error loading banners: " + error.getMessage());
                binding.progressBarBanner.setVisibility(View.GONE);
            }
        });
    }

    private void banners(ArrayList<SliderItems> items) {
        binding.viewpager2.setAdapter(new SliderAdapter(items, binding.viewpager2));
        binding.viewpager2.setClipChildren(false);
        binding.viewpager2.setClipToPadding(false);
        binding.viewpager2.setOffscreenPageLimit(3);
        binding.viewpager2.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(40));
        // Nếu bạn muốn có hiệu ứng scale cho item ở giữa (tùy chọn)
//         compositePageTransformer.addTransformer((page, position) -> {
//             float r = 1 - Math.abs(position);
//             page.setScaleY(0.85f + r * 0.15f);
//         });
        binding.viewpager2.setPageTransformer(compositePageTransformer);
    }

    private void setVariable() {
        // Đặt item "home" được chọn mặc định khi khởi động MainActivity
        binding.bottomMenu.setItemSelected(R.id.home, true);

        binding.bottomMenu.setOnItemSelectedListener(i -> {
            // 'i' ở đây chính là ID của item được chọn
            if (i == R.id.home) {
                // Đã ở trang chủ, không cần làm gì hoặc refresh nếu cần
                Log.d("Navigation", "Home selected");
            } else if (i == R.id.cart) {
                Log.d("Navigation", "Cart selected, starting CartActivity");
                startActivity(new Intent(MainActivity.this, CartActivity.class));
            } else if (i == R.id.favorites) { // Giả sử bạn có item favorites
                Log.d("Navigation", "Favorites selected");
                // startActivity(new Intent(MainActivity.this, FavoritesActivity.class));
            } else if (i == R.id.profile) { // XỬ LÝ CHO PROFILE
                Log.d("Navigation", "Profile selected, starting ProfileActivity");
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            }
            // ChipNavigationBar không yêu cầu return true/false trong listener này
        });
    }

    private void initCategory() {
        DatabaseReference myRef = database.getReference("Category"); // `database` được kế thừa từ BaseActivity
        binding.progressBarCategory.setVisibility(View.VISIBLE);
        ArrayList<Category> list = new ArrayList<>();

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        Category category = issue.getValue(Category.class);
                        if (category != null) { // Kiểm tra null để tránh lỗi
                            list.add(category);
                        }
                    }

                    if (!list.isEmpty()) { // Kiểm tra list không rỗng
                        binding.categoryView.setLayoutManager(new GridLayoutManager(MainActivity.this, 3)); // Thử 4 cột nếu item nhỏ
                        binding.categoryView.setAdapter(new CategoryAdapter(list));
                    }
                }
                binding.progressBarCategory.setVisibility(View.GONE); // Luôn ẩn ProgressBar
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseCategory", "Error loading categories: " + error.getMessage());
                binding.progressBarCategory.setVisibility(View.GONE);
            }
        });
    }

    public void classifyImage(Bitmap image) {
        try {
            // Khởi tạo mô hình đã được huấn luyện sẵn từ file model.tflite
            Model model = Model.newInstance(getApplicationContext());

            // Tạo Tensor đầu vào với kích thước cố định (1 ảnh, kích thước 224x224, 3 kênh màu RGB)
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);

            // Cấp phát bộ nhớ cho dữ liệu ảnh đầu vào (4 byte cho mỗi giá trị float * kích thước ảnh * 3 kênh)
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            // Mảng để lưu giá trị pixel từ ảnh đầu vào
            int[] intValues = new int[imageSize * imageSize];

            // Resize ảnh về kích thước 224x224 (đảm bảo đúng đầu vào cho mô hình)
            Bitmap resized = Bitmap.createScaledBitmap(image, imageSize, imageSize, true);

            // Lấy giá trị pixel của ảnh và lưu vào mảng
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, resized.getWidth(), resized.getHeight());

            // Chuyển đổi pixel sang dạng float (chuẩn hoá giá trị từ 0–255 về 0–1) và lưu vào byteBuffer
            int pixel = 0;
            for (int i = 0; i < imageSize; i++) {
                for (int j = 0; j < imageSize; j++) {
                    int val = intValues[pixel++];
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f)); // Red
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));  // Green
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 255.f));         // Blue
                }
            }

            // Nạp dữ liệu đã xử lý vào tensor đầu vào
            inputFeature0.loadBuffer(byteBuffer);

            // Chạy mô hình để dự đoán
            Model.Outputs outputs = model.process(inputFeature0);

            // Lấy kết quả đầu ra từ mô hình dưới dạng mảng xác suất
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
            float[] confidences = outputFeature0.getFloatArray();

            // Tìm nhãn có xác suất cao nhất
            int maxPos = 0;
            float maxConfidence = 0;
            for (int i = 0; i < confidences.length; i++) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }

            // Danh sách các label
            String[] classes = {"Meat", "Sushi", "Hot dog", "Drink", "Chicken", "Burger", "Pizza"};

            // Nếu độ tin cậy thấp hơn 0.5 thì gán nhãn là "More", ngược lại gán theo lớp dự đoán
            String resultLabel;
            if (maxConfidence < 0.5f) {
                resultLabel = "More"; // Không chắc chắn => yêu cầu người dùng chọn thủ công
            } else {
                resultLabel = classes[maxPos]; // Dự đoán có độ tin cậy cao
            }

            // Hiển thị kết quả dự đoán lên ô tìm kiếm
            binding.searchEditText.setText(resultLabel);

            // Đóng mô hình để giải phóng bộ nhớ
            model.close();

        } catch (IOException e) {
            // Xử lý lỗi khi không thể tải mô hình
            Toast.makeText(this, "Không thể khởi động chức năng tìm kiếm bằng hình ảnh.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            // Xử lý lỗi không xác định
            Toast.makeText(this, "Lỗi không xác định.", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                try {
                    Bitmap image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
                    processImageForClassification(image);
                } catch (IOException e) {
                    Toast.makeText(this, "Lỗi khi đọc ảnh", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == REQUEST_GALLERY) {
                try {
                    Uri imageUri = data.getData();
                    Bitmap image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    processImageForClassification(image);
                } catch (IOException e) {
                    Toast.makeText(this, "Lỗi khi đọc ảnh từ gallery", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private void processImageForClassification(Bitmap image) {
        if (image == null) {
            Toast.makeText(this, "Không lấy được ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        int dimension = Math.min(image.getWidth(), image.getHeight());
        image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
        image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);

        classifyImage(image);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Được cấp quyền mở camera luôn
                openCamera();
            } else {
                // Bị từ chối quyền
                Toast.makeText(this, "Bạn cần cấp quyền Camera để sử dụng chức năng này.", Toast.LENGTH_SHORT).show();
            }
        } if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Bạn cần cấp quyền để chọn ảnh từ thư viện.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = File.createTempFile(
                        "photo_", ".jpg",
                        getExternalCacheDir()
                );
            } catch (IOException ex) {
                Toast.makeText(this, "Không thể tải ảnh vui lòng thử lại.", Toast.LENGTH_SHORT).show();
            }

            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(
                        this,
                        getPackageName() + ".provider",
                        photoFile
                );
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(intent, REQUEST_CAMERA);
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_GALLERY);
    }
    private void showImagePickerDialog() {
        String[] options = {"Chụp ảnh", "Chọn từ thư viện"};
        new AlertDialog.Builder(this)
                .setTitle("Chọn ảnh")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        checkCameraPermission(); // Mở camera
                    } else {
                        checkStoragePermission(); // Mở gallery
                    }
                }).show();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        } else {
            openCamera(); // nếu đã có quyền
        }
    }

    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        REQUEST_STORAGE_PERMISSION);
            } else {
                openGallery();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_STORAGE_PERMISSION);
            } else {
                openGallery();
            }
        }
    }
    //Hàm tìm món ăn dựa trên thông tin người dùng nhập vào
    private void searchAndOpenListFoodActivity(String keyword) {
        Integer id = categoryMap.get(keyword);
        // Nếu thông tin người dùng nhập vào là một category thì mở categogry như khi bấm vào category
        if (id != null) {
            Intent intent = new Intent(this, ListFoodActivity.class);
            intent.putExtra("CategoryId", id);
            intent.putExtra("CategoryName", keyword);
            startActivity(intent);
        } else {
            // nếu không phải là catelory thì tìm kiếm theo tên
            Intent intent = new Intent(this, ListFoodActivity.class);
            intent.putExtra("Title", keyword);
            startActivity(intent);
        }
    }
}