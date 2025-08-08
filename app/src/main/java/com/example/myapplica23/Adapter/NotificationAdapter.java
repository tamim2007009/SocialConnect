package com.example.myapplica23.Adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplica23.Model.Notification;
import com.example.myapplica23.Model.User;
import com.example.myapplica23.R;
import com.example.myapplica23.databinding.Notification2sampleBinding;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.viewHolder> {

    ArrayList<Notification> list;
    Context context;

    public NotificationAdapter(ArrayList<Notification> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.notification2sample, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Notification notification = list.get(position);

        String time = TimeAgo.using(notification.getNotificationAt());
        holder.binding.time.setText(time);

        // Fetch the user who created the notification
        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(notification.getNotificationBy())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            Picasso.get()
                                    .load(user.getProfilePhoto())
                                    .placeholder(R.drawable.cover_placeholder)
                                    .into(holder.binding.profileImage);

                            String notificationText = "";
                            // Customize text based on notification type
                            switch (notification.getType()) {
                                case "like":
                                    notificationText = "<b>" + user.getName() + "</b>" + " liked your post.";
                                    break;
                                case "comment":
                                    notificationText = "<b>" + user.getName() + "</b>" + " commented on your post.";
                                    break;
                                case "follow":
                                    notificationText = "<b>" + user.getName() + "</b>" + " started following you.";
                                    break;
                                default:
                                    notificationText = "New notification";
                                    break;
                            }
                            holder.binding.notification.setText(Html.fromHtml(notificationText));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        Notification2sampleBinding binding;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = Notification2sampleBinding.bind(itemView);
        }
    }
}