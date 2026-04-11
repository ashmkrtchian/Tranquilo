package ashot.mkrtchyan.tranquilo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private final Context context;
    private List<LeaderboardUser> data;

    public LeaderboardAdapter(Context context, List<LeaderboardUser> data) {
        this.context = context;
        this.data    = new ArrayList<>(data);
    }

    public void setData(List<LeaderboardUser> newData) {
        this.data = new ArrayList<>(newData);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_leaderboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LeaderboardUser user = data.get(position);

        holder.tvRank.setText(String.valueOf(user.getRank()));
        holder.tvName.setText(user.getName());
        holder.tvCoins.setText(String.valueOf(user.getCalmCoins()));

        holder.tvSubtitle.setText(getCalmTitle(user.getCalmCoins()));

        String key = user.getAvatarUrl();
        int drawableRes = getDrawableForKey(key);
        holder.ivAvatar.setImageResource(drawableRes);
    }
    private int getDrawableForKey(String key) {
        if (key == null) return R.drawable.man;
        switch (key) {
            case "dog": return R.drawable.dog;
            case "woman":   return R.drawable.woman;
            case "cat":  return R.drawable.cat;
            default:       return R.drawable.man;
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private String getCalmTitle(long coins) {
        if (coins >= 1000) return "🌳 Zen Master";
        if (coins >= 500)  return "🍃 Calm Guide";
        if (coins >= 200)  return "🌱 Peace Keeper";
        if (coins >= 50)   return "🌾 Calm Seeker";
        return "🌿 Just Starting";
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView  tvRank, tvName, tvSubtitle, tvCoins;
        ImageView ivAvatar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank     = itemView.findViewById(R.id.tvRank);
            tvName     = itemView.findViewById(R.id.tvName);
            tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
            tvCoins    = itemView.findViewById(R.id.tvCoins);
            ivAvatar   = itemView.findViewById(R.id.ivAvatar);
        }
    }
}