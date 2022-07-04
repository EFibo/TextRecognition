package com.efibo.textrecognition;

// Imports
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class ShowTextActivity extends AppCompatActivity {

    // Legt fest was passiert, wenn die Activity gestartet wird
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showtext);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Text");
        actionBar.setDisplayHomeAsUpEnabled(true);

        TextView textView = findViewById(R.id.textView);
        textView.setMovementMethod(new ScrollingMovementMethod());

        // Mitgegebener Text wird in Variable gespeichert
        Bundle b = getIntent().getExtras();
        String text = b.getString("text");

        textView.setText(text);
    }

    // Legt fest was passiert, wenn der Zurück-Button gedrückt wird
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}