package com.example.androidfronted.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.canhub.cropper.CropImageView;
import com.example.androidfronted.R;
import com.github.chrisbanes.photoview.PhotoView;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Stack;

public class ImageEditActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_URI = "image_uri";
    public static final String EXTRA_RESULT_URI = "result_uri";
    public static final String EXTRA_CROP_SHAPE = "crop_shape";
    public static final int CROP_SHAPE_RECTANGLE = 0;
    public static final int CROP_SHAPE_OVAL = 1;

    private PhotoView photoView;
    private CropImageView cropImageView;
    private LinearLayout previewButtons;
    private LinearLayout editButtons;
    private LinearLayout cropButtons;
    private View topToolbar;
    private ImageButton btnBack;
    private ImageButton btnUndo;
    private ImageButton btnRedo;
    private Button btnEdit;
    private Button btnConfirm;
    private ImageButton btnRotate;
    private ImageButton btnCrop;
    private Button btnConfirmEdit;
    private Button btnCancelCrop;
    private Button btnConfirmCrop;
    private TextView tvTitle;

    private Uri originalImageUri;
    private Bitmap currentBitmap;
    private Bitmap originalBitmap;
    private Stack<Bitmap> undoStack = new Stack<>();
    private Stack<Bitmap> redoStack = new Stack<>();
    private int cropShape = CROP_SHAPE_RECTANGLE;

    private enum Mode {
        PREVIEW,
        EDIT,
        CROP
    }

    private Mode currentMode = Mode.PREVIEW;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_edit);

        originalImageUri = getIntent().getParcelableExtra(EXTRA_IMAGE_URI);
        cropShape = getIntent().getIntExtra(EXTRA_CROP_SHAPE, CROP_SHAPE_RECTANGLE);
        
        if (originalImageUri == null) {
            Toast.makeText(this, "图片加载失败", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadImage();
        setupClickListeners();
        updateMode(Mode.PREVIEW);
    }

    private void initViews() {
        photoView = findViewById(R.id.photo_view);
        cropImageView = findViewById(R.id.crop_image_view);
        previewButtons = findViewById(R.id.preview_buttons);
        editButtons = findViewById(R.id.edit_buttons);
        cropButtons = findViewById(R.id.crop_buttons);
        topToolbar = findViewById(R.id.top_toolbar);
        btnBack = findViewById(R.id.btn_back);
        btnUndo = findViewById(R.id.btn_undo);
        btnRedo = findViewById(R.id.btn_redo);
        btnEdit = findViewById(R.id.btn_edit);
        btnConfirm = findViewById(R.id.btn_confirm);
        btnRotate = findViewById(R.id.btn_rotate);
        btnCrop = findViewById(R.id.btn_crop);
        btnConfirmEdit = findViewById(R.id.btn_confirm_edit);
        btnCancelCrop = findViewById(R.id.btn_cancel_crop);
        btnConfirmCrop = findViewById(R.id.btn_confirm_crop);
        tvTitle = findViewById(R.id.tv_title);
    }

    private void loadImage() {
        try {
            originalBitmap = android.graphics.BitmapFactory.decodeStream(
                    getContentResolver().openInputStream(originalImageUri));
            if (originalBitmap != null) {
                currentBitmap = originalBitmap;
                photoView.setImageBitmap(currentBitmap);
            }
        } catch (Exception e) {
            Toast.makeText(this, "图片加载失败", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupCropImageView() {
        cropImageView.setImageBitmap(currentBitmap);
        cropImageView.setScaleType(CropImageView.ScaleType.FIT_CENTER);
        cropImageView.setAutoZoomEnabled(true);
        cropImageView.setMultiTouchEnabled(true);
        
        if (cropShape == CROP_SHAPE_OVAL) {
            cropImageView.setCropShape(CropImageView.CropShape.OVAL);
            cropImageView.setFixedAspectRatio(true);
            cropImageView.setAspectRatio(1, 1);
            cropImageView.setGuidelines(CropImageView.Guidelines.ON);
        } else {
            cropImageView.setCropShape(CropImageView.CropShape.RECTANGLE);
            cropImageView.setFixedAspectRatio(false);
            cropImageView.setGuidelines(CropImageView.Guidelines.ON);
            cropImageView.setShowCropOverlay(true);
            cropImageView.setFlippedHorizontally(false);
            cropImageView.setFlippedVertically(false);
            
            int padding = 50;
            Rect initialRect = new Rect(padding, padding, 
                currentBitmap.getWidth() - padding, currentBitmap.getHeight() - padding);
            cropImageView.setCropRect(initialRect);
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> onBackClicked());

        btnEdit.setOnClickListener(v -> updateMode(Mode.EDIT));

        btnConfirm.setOnClickListener(v -> returnResult(originalImageUri));

        btnUndo.setOnClickListener(v -> performUndo());
        btnRedo.setOnClickListener(v -> performRedo());

        btnRotate.setOnClickListener(v -> rotateImage());

        btnCrop.setOnClickListener(v -> {
            setupCropImageView();
            updateMode(Mode.CROP);
        });

        btnConfirmEdit.setOnClickListener(v -> returnEditedResult());

        btnCancelCrop.setOnClickListener(v -> updateMode(Mode.EDIT));

        btnConfirmCrop.setOnClickListener(v -> applyCrop());
    }

    private void onBackClicked() {
        switch (currentMode) {
            case EDIT:
                updateMode(Mode.PREVIEW);
                break;
            case PREVIEW:
            default:
                setResult(RESULT_CANCELED);
                finish();
                break;
        }
    }

    private void updateMode(Mode mode) {
        currentMode = mode;
        
        previewButtons.setVisibility(View.GONE);
        editButtons.setVisibility(View.GONE);
        cropButtons.setVisibility(View.GONE);
        photoView.setVisibility(View.GONE);
        cropImageView.setVisibility(View.GONE);
        btnUndo.setVisibility(View.GONE);
        btnRedo.setVisibility(View.GONE);
        btnBack.setVisibility(View.VISIBLE);

        switch (mode) {
            case PREVIEW:
                previewButtons.setVisibility(View.VISIBLE);
                photoView.setVisibility(View.VISIBLE);
                tvTitle.setText("预览图片");
                break;
            case EDIT:
                editButtons.setVisibility(View.VISIBLE);
                photoView.setVisibility(View.VISIBLE);
                btnUndo.setVisibility(View.VISIBLE);
                btnRedo.setVisibility(View.VISIBLE);
                tvTitle.setText("编辑图片");
                updateUndoRedoButtons();
                break;
            case CROP:
                cropButtons.setVisibility(View.VISIBLE);
                cropImageView.setVisibility(View.VISIBLE);
                btnBack.setVisibility(View.GONE);
                tvTitle.setText("裁剪图片");
                break;
        }
    }

    private void rotateImage() {
        if (currentBitmap == null) return;

        saveToUndoStack();

        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        currentBitmap = Bitmap.createBitmap(currentBitmap, 0, 0,
                currentBitmap.getWidth(), currentBitmap.getHeight(), matrix, true);

        photoView.setImageBitmap(currentBitmap);
        redoStack.clear();
        updateUndoRedoButtons();
    }

    private void applyCrop() {
        Bitmap croppedBitmap = cropImageView.getCroppedImage();
        if (croppedBitmap != null) {
            saveToUndoStack();
            currentBitmap = croppedBitmap;
            photoView.setImageBitmap(currentBitmap);
            redoStack.clear();
            updateUndoRedoButtons();
        }
        updateMode(Mode.EDIT);
    }

    private void saveToUndoStack() {
        if (currentBitmap != null) {
            undoStack.push(currentBitmap.copy(currentBitmap.getConfig(), true));
        }
    }

    private void performUndo() {
        if (!undoStack.isEmpty()) {
            redoStack.push(currentBitmap.copy(currentBitmap.getConfig(), true));
            currentBitmap = undoStack.pop();
            photoView.setImageBitmap(currentBitmap);
            updateUndoRedoButtons();
        }
    }

    private void performRedo() {
        if (!redoStack.isEmpty()) {
            undoStack.push(currentBitmap.copy(currentBitmap.getConfig(), true));
            currentBitmap = redoStack.pop();
            photoView.setImageBitmap(currentBitmap);
            updateUndoRedoButtons();
        }
    }

    private void updateUndoRedoButtons() {
        btnUndo.setEnabled(!undoStack.isEmpty());
        btnUndo.setAlpha(undoStack.isEmpty() ? 0.5f : 1.0f);
        btnRedo.setEnabled(!redoStack.isEmpty());
        btnRedo.setAlpha(redoStack.isEmpty() ? 0.5f : 1.0f);
    }

    private void returnResult(Uri uri) {
        Intent result = new Intent();
        result.putExtra(EXTRA_RESULT_URI, uri);
        setResult(RESULT_OK, result);
        finish();
    }

    private void returnEditedResult() {
        try {
            File tempFile = new File(getCacheDir(),
                    "edited_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream out = new FileOutputStream(tempFile);
            currentBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.close();

            Intent result = new Intent();
            result.putExtra(EXTRA_RESULT_URI, Uri.fromFile(tempFile));
            setResult(RESULT_OK, result);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "保存图片失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (currentMode == Mode.CROP) {
            updateMode(Mode.EDIT);
        } else {
            onBackClicked();
        }
    }
}
