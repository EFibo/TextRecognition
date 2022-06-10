package com.efibo.textrecognition;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.rustamg.filedialogs.FileDialog;
import com.rustamg.filedialogs.SaveFileDialog;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private Button textShowButton;
    private Button textSaveButton;
    private Bitmap selectedImage;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.image_view);
        textShowButton = findViewById(R.id.button_text);
        textSaveButton = findViewById(R.id.button_textSave);
        Button buttonUrl = findViewById(R.id.button_inputUrl);
        Button buttonPic = findViewById(R.id.button_choosePic);
        Button buttonCamera = findViewById(R.id.button_camera);

        textShowButton.setEnabled(false);
        textSaveButton.setEnabled(false);

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setTitle("Downloading image");
        progressDialog.setMessage("Please wait, we are downloading your image file ...");

        textShowButton.setOnClickListener(view -> recognizeText());

        textSaveButton.setOnClickListener(view -> {
            recognizeText();
            FileDialog dialog = new SaveFileDialog();
            dialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Base_Theme_AppCompat);
            dialog.show(getSupportFragmentManager(), SaveFileDialog.class.getName());
        });

        buttonUrl.setOnClickListener(view -> {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
            alertDialog.setTitle("URL");
            alertDialog.setMessage("Enter URL");

            final EditText input = new EditText(MainActivity.this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            input.setLayoutParams(lp);
            alertDialog.setView(input);

            alertDialog.setPositiveButton("DOWNLOAD",
                    (dialogInterface, i) -> new DownloadTask().execute(stringToURL(input.getText().toString())));
            alertDialog.setNegativeButton("CANCEL",
                    (dialogInterface, i) -> dialogInterface.cancel());
            alertDialog.show();
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

    private class DownloadTask extends AsyncTask<URL,Void,Bitmap> {
        protected void onPreExecute() {
            progressDialog.show();
        }
        protected Bitmap doInBackground(URL...urls) {
            URL url = urls[0];
            try {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                return BitmapFactory.decodeStream(bufferedInputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        protected void onPostExecute(Bitmap result) {
            progressDialog.dismiss();
            if (result != null) {
                selectedImage = result;
                imageView.setImageBitmap(selectedImage);
                textShowButton.setEnabled(true);
                textSaveButton.setEnabled(true);
            } else {
                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        }
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
        } else if (requestCode == 2) {
            Text text = (Text) data.getExtras().get("data");
            Intent i = new Intent(this, ShowTextActivity.class);
            i.putExtra("text", text.getText());
            startActivity(i);
        }
        textShowButton.setEnabled(true);
        textSaveButton.setEnabled(true);
    }

    private URL stringToURL(String string) {
        try {
            return new URL(string);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void recognizeText() {
        InputImage image = InputImage.fromBitmap(selectedImage, 0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        textShowButton.setEnabled(false);
        textSaveButton.setEnabled(false);
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
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String fileName = "document_" + sdf.format(new Date()) + ".txt";
            Log.e("filename: ", fileName);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput(fileName, Context.MODE_PRIVATE));
            outputStreamWriter.write(text.getText());
            outputStreamWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Intent i = new Intent(this, ShowTextActivity.class);
        i.putExtra("text", text.getText());
        startActivity(i);
    }
}