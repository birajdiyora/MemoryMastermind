package com.example.memorymastermind;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import com.example.memorymastermind.R;

public class MainActivity extends AppCompatActivity {

    private GridLayout gridLayout;
    private TextView timerTextView;
    private Handler handler = new Handler();
    private Runnable timerRunnable;
    private ArrayList<FrameLayout> frameLayouts;
    private ArrayList<Integer> imagePool;
    private ArrayList<Integer> currentRoundImages;
    private ArrayList<Integer> correctOrder;
    private ArrayList<Integer> userSelections;
    private int timer = 15;
    private int selectionCount = 0;
    private FirebaseFirestore firestore;
    private static final String PREFS_NAME = "user_prefs";
    private static final String USER_NAME_KEY = "user_name";
    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.getOverflowIcon().setColorFilter(Color.WHITE , PorterDuff.Mode.SRC_ATOP);

        gridLayout = findViewById(R.id.gridLayout);
        timerTextView = findViewById(R.id.timerTextView);
        frameLayouts = new ArrayList<>();
        userSelections = new ArrayList<>();
        correctOrder = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();

        initializeImagePool();
        initializeGrid();

        startTimer();

        handler.postDelayed(this::enableImageClicks, 15000);  // Changed from 10000 to 15000 milliseconds
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.action_history){
            Intent intent = new Intent(this, HistoryActivity.class);
            startActivity(intent);
            return true;
        }else if(item.getItemId() == R.id.action_logout){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(USER_NAME_KEY,"");
            editor.apply();
            startActivity(new Intent(MainActivity.this,LoginActivity.class));
            finish();
        }
        else{
            return true;
        }
        return false;
    }
    private void initializeImagePool() {
        imagePool = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            imagePool.add(getResources().getIdentifier("img" + (i + 1), "drawable", getPackageName()));
        }
    }

    private void initializeGrid() {
        selectNewRoundImages();

        for (int i = 0; i < 9; i++) {
            FrameLayout frameLayout = (FrameLayout) getLayoutInflater().inflate(R.layout.grid_item, gridLayout, false);
            ImageView imageView = frameLayout.findViewById(R.id.imageView);
            imageView.setImageResource(currentRoundImages.get(i));
            frameLayout.setTag(currentRoundImages.get(i));
            frameLayout.setOnClickListener(this::onImageClick);
            frameLayout.setClickable(false);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = 0;
            params.columnSpec = GridLayout.spec(i % 3, 1f);
            params.rowSpec = GridLayout.spec(i / 3, 1f);
            gridLayout.addView(frameLayout, params);
            frameLayouts.add(frameLayout);
        }
    }

    private void selectNewRoundImages() {
        Set<Integer> selectedImages = new HashSet<>();
        Random random = new Random();

        while (selectedImages.size() < 9) {
            int randomIndex = random.nextInt(imagePool.size());
            selectedImages.add(imagePool.get(randomIndex));
        }

        currentRoundImages = new ArrayList<>(selectedImages);
        correctOrder = new ArrayList<>(currentRoundImages);
    }

    private void startTimer() {
        timerTextView.setText(String.valueOf(timer));
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (timer > 0) {
                    timer--;
                    timerTextView.setText(String.valueOf(timer));
                    handler.postDelayed(this, 1000);
                } else {
                    timerTextView.setVisibility(View.GONE);
                }
            }
        };
        handler.post(timerRunnable);
    }

    private void enableImageClicks() {
        Collections.shuffle(currentRoundImages);
        for (int i = 0; i < 9; i++) {
            FrameLayout frameLayout = frameLayouts.get(i);
            ImageView imageView = frameLayout.findViewById(R.id.imageView);
            imageView.setImageResource(currentRoundImages.get(i));
            frameLayout.setTag(currentRoundImages.get(i));
            frameLayout.setClickable(true);
        }
    }

    private void onImageClick(View view) {
        FrameLayout frameLayout = (FrameLayout) view;
        if (!frameLayout.isClickable()) {
            Toast.makeText(this, "Please wait for the initial display time to complete.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userSelections.contains(frameLayouts.indexOf(frameLayout))) {
            Toast.makeText(this, "You already selected this image", Toast.LENGTH_SHORT).show();
            return;
        }

        selectionCount++;
        userSelections.add(frameLayouts.indexOf(frameLayout));


        TextView positionTextView = frameLayout.findViewById(R.id.positionTextView);
        positionTextView.setText(String.valueOf(selectionCount));
        positionTextView.setVisibility(View.VISIBLE);

        if (selectionCount == 9) {
            showResult();
        }
    }

    private void showResult() {
        AtomicInteger correctGuesses = new AtomicInteger();
        for (int i = 0; i < 9; i++) {
            if (currentRoundImages.get(userSelections.get(i)).equals(correctOrder.get(i))) {
                correctGuesses.getAndIncrement();
            }
        }
        insertDataIntoFireBase(Integer.parseInt(correctGuesses+""));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Game Over");
        builder.setMessage(correctGuesses + " out of 9 correct");
        builder.setPositiveButton("OK", (dialog, which) -> {
            correctGuesses.set(0);
            selectionCount = 0;
            userSelections.clear();
            recreate();
        });
        builder.show();
    }

    private String getUserNameFromPrefs() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(USER_NAME_KEY, null);
    }
    private void insertDataIntoFireBase(int correctGuises) {
        firestore.collection("user").document(UUID.randomUUID().toString()).set(
                new UserData(String.valueOf(correctGuises),getUserNameFromPrefs())
        );
    }
}
