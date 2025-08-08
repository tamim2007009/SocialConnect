package com.example.myapplica23.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplica23.Model.Story;
import com.example.myapplica23.Model.UserStories;
import com.example.myapplica23.R;
import com.example.myapplica23.databinding.StoryRvDesignBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.callback.StoryClickListeners;
import omari.hamza.storyview.model.MyStory;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.viewHolder> {

    ArrayList<Story> list;
    Context context;

    public StoryAdapter(ArrayList<Story> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.story_rv_design, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Story story = list.get(position);

        if (story.getStories() != null && !story.getStories().isEmpty()) {

            UserStories lastStory = story.getStories().get(story.getStories().size() - 1);
            Picasso.get()
                    .load(lastStory.getImage())
                    .placeholder(R.drawable.cover_placeholder)
                    .into(holder.binding.storyImage);

            holder.binding.statusCircle.setPortionsCount(story.getStories().size());

            // Get pre-fetched user data directly from the story object
            Picasso.get()
                    .load(story.getUserProfilePhoto())
                    .placeholder(R.drawable.cover_placeholder)
                    .into(holder.binding.profileImage);

            holder.binding.name.setText(story.getUserName());

            holder.binding.storyImage.setOnClickListener(v -> {
                ArrayList<MyStory> myStories = new ArrayList<>();
                for (UserStories stories : story.getStories()) {
                    myStories.add(new MyStory(stories.getImage()));
                }

                new StoryView.Builder(((AppCompatActivity) context).getSupportFragmentManager())
                        .setStoriesList(myStories)
                        .setStoryDuration(5000)
                        .setTitleText(story.getUserName()) // Use pre-fetched name
                        .setSubtitleText("") // Set subtitle if you have it
                        .setTitleLogoUrl(story.getUserProfilePhoto()) // Use pre-fetched photo
                        .setStoryClickListeners(new StoryClickListeners() {
                            @Override
                            public void onDescriptionClickListener(int pos) {}

                            @Override
                            public void onTitleIconClickListener(int pos) {}
                        })
                        .build()
                        .show();
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        StoryRvDesignBinding binding;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = StoryRvDesignBinding.bind(itemView);
        }
    }
}