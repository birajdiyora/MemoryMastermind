package com.example.memorymastermind;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private Context context;
    private List<UserData> userDataList;

    public HistoryAdapter(Context context, List<UserData> userDataList) {
        this.context = context;
        this.userDataList = userDataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserData userData = userDataList.get(position);
        holder.correctGuessesTextView.setText(userData.getCorrectGuesses()+holder.correctGuessesTextView.getText());
        holder.timestampTextView.setText("Timestamp: "+userData.getTimestamp());
    }

    @Override
    public int getItemCount() {
        return userDataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView correctGuessesTextView;
        TextView timestampTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            correctGuessesTextView = itemView.findViewById(R.id.correctGuessesTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
        }
    }
}
