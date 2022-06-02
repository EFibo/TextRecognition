package com.efibo.textrecognition;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.*;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private Button textShowButton;
    private Button textOpenButton;
    private Button buttonUrl;
    private Bitmap selectedImage;
    private int showOrSave = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.image_view);
        textShowButton = findViewById(R.id.button_text);
        textOpenButton = findViewById(R.id.button_textOpen);
        buttonUrl = findViewById(R.id.button_inputUrl);
        Button buttonPic = findViewById(R.id.button_choosePic);
        Button buttonCamera = findViewById(R.id.button_camera);

        textShowButton.setEnabled(false);

        textShowButton.setOnClickListener(view -> {
            recognizeText();
        });

        textOpenButton.setOnClickListener(view -> {
            openText();
        });

        buttonUrl.setOnClickListener(view -> {
            // Abfrage der URL
            Log.e("request", "request");
            String src = "https://files.realpython.com/media/sample5.ca470b17f6d7.jpg";
            Thread download = new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.e("kek", "kek");
                    try {
                        Log.e("start", "start");
                        java.net.URL url = new java.net.URL(src);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setDoInput(true);
                        connection.connect();
                        Log.e("connected", "connected");
                        int responseCode = connection.getResponseCode();
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            Log.e("ok", "ok");
                            InputStream input = connection.getInputStream();
                            Bitmap bmp = BitmapFactory.decodeStream(input);
                            input.close();
                            selectedImage = bmp;
                            imageView.setImageBitmap(bmp);
                            Log.e("done", "done");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e("failed", "failed");
                    }
                }
            });
            Toast.makeText(this, "finished", Toast.LENGTH_LONG).show();
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
    }

    private void recognizeText() {
        InputImage image = InputImage.fromBitmap(selectedImage, 0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        textShowButton.setEnabled(false);
        recognizer.process(image).addOnSuccessListener(text -> {
            textShowButton.setEnabled(true);
            processResult(text);
        }).addOnFailureListener(e -> {
            textShowButton.setEnabled(true);
            e.printStackTrace();
        });
    }

    private void processResult(Text text) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String fileName = "document_" + sdf.format(new Date()) + ".txt";
            Log.e("filename: ", fileName);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput(fileName, Context.MODE_PRIVATE));
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

    private void openText() {

    }
}