package com.example.memorymastermind;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView noData;
    private FirebaseFirestore db;
    private List<UserData> userDataList;
    private HistoryAdapter adapter;
    private static final String PREFS_NAME = "user_prefs";
    private static final String USER_NAME_KEY = "user_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        noData = findViewById(R.id.no_data);
        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        Toolbar toolbar = findViewById(R.id.toolbar_history);
        setSupportActionBar(toolbar);
        // Initialize RecyclerView and adapter
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            toolbar.getNavigationIcon().setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_ATOP);
        }
        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        userDataList = new ArrayList<>();
        adapter = new HistoryAdapter(this, userDataList);
        recyclerView.setAdapter(adapter);
        fetchWinningData();
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private String getUserNameFromPrefs() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(USER_NAME_KEY, null);
    }
    private void fetchWinningData() {
        db.collection("user")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            UserData userData = documentSnapshot.toObject(UserData.class);
                            if(userData.getUsername().equals(getUserNameFromPrefs())) {
                                userDataList.add(new UserData(userData.getCorrectGuesses(),userData.getUsername(),Common.convertTimestampToDateTime(Long.parseLong(userData.getTimestamp()))));
                            }
                            Log.d("test",userData.getUsername().toString());
                            Log.d("test",getUserNameFromPrefs());
                        }
                        adapter.notifyDataSetChanged();
                        if(userDataList.isEmpty()){
                            recyclerView.setVisibility(View.GONE);
                            noData.setVisibility(View.VISIBLE);
                            Log.d("test","list is empty");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(HistoryActivity.this, "Failed to fetch data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
