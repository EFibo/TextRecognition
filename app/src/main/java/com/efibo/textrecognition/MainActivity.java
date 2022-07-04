package com.efibo.textrecognition;

// Imports
import android.content.Intent;
import android.graphics.*;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.widget.*;
import androidx.appcompat.app.*;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.*;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private Button textShowButton;
    private Bitmap selectedImage;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");

    // Legt fest was passiert, wenn die Activity gestartet wird
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bindet die Design-Elemente in den Code ein
        imageView = findViewById(R.id.image_view);
        textShowButton = findViewById(R.id.button_text);
        Button buttonUrl = findViewById(R.id.button_inputUrl);
        Button buttonPic = findViewById(R.id.button_choosePic);
        Button buttonCamera = findViewById(R.id.button_camera);

        textShowButton.setEnabled(false);

        textShowButton.setOnClickListener(view -> recognizeText());

        buttonUrl.setOnClickListener(view -> showAlertDialog());

        // File chooser für Bilder
        buttonPic.setOnClickListener(view -> {
            Intent choosePic = new Intent(Intent.ACTION_GET_CONTENT);
            choosePic.setType("image/*"); // Filter für Bilder
            choosePic = Intent.createChooser(choosePic, "Choose a picture");
            startActivityForResult(choosePic, 1);
        });

        // Kamera, um ein Bild aufzunehmen
        buttonCamera.setOnClickListener(view -> {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, 1888);
        });
    }

    // Fenster für Eingabe der URL
    private void showAlertDialog() {
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
                (dialogInterface, i) -> isUrlValid(input.getText().toString()));
        alertDialog.setNegativeButton("CANCEL",
                (dialogInterface, i) -> dialogInterface.cancel());
        alertDialog.show();
    }

    // Prüft, ob die URL korrekt eingegeben wurde
    private void isUrlValid(String url) {
        try {
            URL obj = new URL(url);
            obj.toURI();
            new DownloadTask().execute(stringToURL(url));
        } catch (MalformedURLException | URISyntaxException e) {
            Toast.makeText(MainActivity.this, getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    // Konvertiert den eingegebenen String zu einer URL
    private URL stringToURL(String string) {
        try {
            return new URL(string);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Es wird versucht, das Bild von der URL herunterzuladen
    private class DownloadTask extends AsyncTask<URL,Void,Bitmap> {
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
        // Wenn das erfolgreich ist, wird das Bild angezeigt
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                selectedImage = result;
                imageView.setImageBitmap(selectedImage);
                textShowButton.setEnabled(true);
            } else {
                Toast.makeText(MainActivity.this, getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
            }
        }
    }

    //
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Wenn ein Bild gemacht wurde
        if (requestCode == 1888) {
            selectedImage = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(selectedImage);
        }
        // Wenn ein Bild aus dem Speicher ausgewählt wurde
        else if (requestCode == 1) {
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

    // Text wird aus dem ausgewählten oder heruntergeladenen Bild erkannt
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

    // Erkannter Text wird gespeichert und angezeigt
    private void processResult(Text text) {
        String fileName = "document_" + sdf.format(new Date()) + ".txt";
        try {
            File file = new File(Environment.getExternalStorageDirectory()+"/Documents"+"/TextRecognition");
            if (!file.exists()) { boolean success = file.mkdirs(); }
            file = new File(Environment.getExternalStorageDirectory()+"/Documents"+"/TextRecognition/"+ fileName);
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
            writer.write(text.getText());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Anzeige des ausgelesenen Textes in der ShowTextActivity
        Intent i = new Intent(this, ShowTextActivity.class);
        i.putExtra("text", text.getText());
        startActivity(i);
    }
}