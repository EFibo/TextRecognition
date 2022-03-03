package com.efibo.textrecognition;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.efibo.textrecognition.GraphicOverlay.Graphic;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private ImageView imageView;
    private Button textButton;
    private Bitmap selectedImage;
    private GraphicOverlay graphicOverlay;
    private Integer imageMaxWidth;
    private Integer imageMaxHeigth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.image_view);
        textButton = findViewById(R.id.button_text);
        graphicOverlay = findViewById(R.id.graphic_overlay);

        textButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recognizeText();
            }
        });
        Spinner dropdown = findViewById(R.id.spinner);
        String[] items = new String[]{"Test Image 1", "Test Image 2"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(this);
    }

    private void recognizeText() {
        InputImage image = InputImage.fromBitmap(selectedImage, 0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        textButton.setEnabled(false);
        recognizer.process(image).addOnSuccessListener(new OnSuccessListener<Text>() {
            @Override
            public void onSuccess(Text text) {
                textButton.setEnabled(true);
                processResult(text);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                textButton.setEnabled(true);
                e.printStackTrace();
            }
        });
    }

    private void processResult(Text text) {
        List<Text.TextBlock> blocks = text.getTextBlocks();
        if (blocks.size() == 0) {
            Toast.makeText(getApplicationContext(), "No face found", Toast.LENGTH_SHORT).show();
            return;
        }
        graphicOverlay.clear();
        for (int i = 0; i < blocks.size(); i++) {
            List<Text.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                List<Text.Element> elements = lines.get(j).getElements();
                for (int k = 0; k < elements.size(); k++) {
                    Graphic textGraphic = new TextGraphic(graphicOverlay, elements.get(k));
                    graphicOverlay.add(textGraphic);
                }
            }
        }
    }

    private Integer getImageMaxWidth() {
        if (imageMaxWidth == null) {
            imageMaxWidth = imageView.getWidth();
        }
        return imageMaxWidth;
    }

    private Integer getImageMaxHeigth() {
        if (imageMaxHeigth == null) {
            imageMaxHeigth = imageView.getHeight();
        }
        return imageMaxHeigth;
    }

    private Pair<Integer, Integer> getTargetedWidthHeigth() {
        int targetWidth;
        int targetHeight;
        int maxWidthForPortraitMode = getImageMaxWidth();
        int maxHeightForPortraitMode = getImageMaxHeigth();
        targetWidth = maxWidthForPortraitMode;
        targetHeight = maxHeightForPortraitMode;
        return new Pair<>(targetWidth, targetHeight);
    }

    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        graphicOverlay.clear();
        switch (position) {
            case 0:
                selectedImage = getBitmapFromAsset(this, "testImage1.png");
                break;
            case 1:
                selectedImage = getBitmapFromAsset(this, "testImage2.png");
                break;
        }
        if (selectedImage != null) {
            Pair<Integer, Integer> targetedSize = getTargetedWidthHeigth();

            int targetWidth = targetedSize.first;
            int maxHeight = targetedSize.second;

            float scaleFactor = Math.max(
                    (float) selectedImage.getWidth() / (float) targetWidth,
                    (float) selectedImage.getHeight() / (float) maxHeight);

            Bitmap resizedBitmap =
                    Bitmap.createScaledBitmap(
                            selectedImage,
                            (int) (selectedImage.getWidth() / scaleFactor),
                            (int) (selectedImage.getHeight() / scaleFactor),
                            true);

            imageView.setImageBitmap(resizedBitmap);
            selectedImage = resizedBitmap;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    public static Bitmap getBitmapFromAsset(Context context, String filePath) {
        AssetManager assetManager = context.getAssets();

        InputStream is;
        Bitmap bitmap = null;
        try {
            is = assetManager.open(filePath);
            bitmap = BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}