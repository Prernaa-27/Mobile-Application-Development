package com.example.uniglobe;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Note: This activity is currently secondary to BrowseCollegesActivity.
 * It has been fixed to use a valid layout to resolve project-wide errors.
 */
public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Using explorer layout as a valid fallback
        setContentView(R.layout.activity_browse_colleges);
    }
}
