package com.efibo.textrecognition;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import java.io.*;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private Button textShowButton;
    private Button textSaveButton;
    private Bitmap selectedImage;
    private int showOrSave = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.image_view);
        textShowButton = findViewById(R.id.button_text);
        textSaveButton = findViewById(R.id.button_textSave);
        Button buttonPic = findViewById(R.id.button_choosePic);
        Button buttonCamera = findViewById(R.id.button_camera);

        textShowButton.setEnabled(false);
        textSaveButton.setEnabled(false);

        textShowButton.setOnClickListener(view -> {
            showOrSave = 1;
            recognizeText();
        });

        textSaveButton.setOnClickListener(view -> {
            showOrSave = 2;
            recognizeText();
        });

        buttonPic.setOnClickListener(view -> {
            Intent choosePic = new Intent(Intent.ACTION_GET_CONTENT);
            choosePic.setType("image/*");
            choosePic = Intent.createChooser(choosePic, "Choose a picture");
            startActivityForResult(choosePic, 1);
        });

        buttonCamera.setOnClickListener(view -> {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, 1888);
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1888) {
            selectedImage = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(selectedImage);
        } else if (requestCode == 1) {
            try {
                Uri imageUri = data.getData();
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                selectedImage = BitmapFactory.decodeStream(imageStream);
                imageView.setImageBitmap(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        textShowButton.setEnabled(true);
        textSaveButton.setEnabled(true);
    }

    private void recognizeText() {
        InputImage image = InputImage.fromBitmap(selectedImage, 0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        textShowButton.setEnabled(false);
        recognizer.process(image).addOnSuccessListener(text -> {
            textShowButton.setEnabled(true);
            textSaveButton.setEnabled(true);
            processResult(text);
        }).addOnFailureListener(e -> {
            textShowButton.setEnabled(true);
            textSaveButton.setEnabled(true);
            e.printStackTrace();
        });
    }

    private void processResult(Text text) {
        if (showOrSave == 1) {
            try {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("output.txt", Context.MODE_PRIVATE));
                outputStreamWriter.write(text.getText());
                outputStreamWriter.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            Intent i = new Intent(this, ShowTextActivity.class);
            i.putExtra("text", text.getText());
            startActivity(i);
        }
        else if (showOrSave == 2) {
            saveResult(text);
        }
    }

    private void saveResult(Text text) {
        String fileName = "";
        // Auswahl des Speicherorts und Eingabe des Dateinamens
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput(fileName, Context.MODE_PRIVATE));
            outputStreamWriter.write(text.getText());
            outputStreamWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}