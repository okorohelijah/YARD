package com.example.yard.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.yard.R;
import com.parse.ParseException;
import com.parse.ParseFile;

import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {
  private final Context context;
  private final List<Post> posts;

  public PostsAdapter(Context context, List<Post> posts) {
    this.context = context;
    this.posts = posts;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    Post post = posts.get(position);
    try {
      holder.bind(post);
    } catch (ParseException e) {
      e.printStackTrace();
    }
  }

  @Override
  public int getItemCount() {
    return posts.size();
  }

  class ViewHolder extends RecyclerView.ViewHolder {
    private final TextView tvUsername;
    private final ImageView ivUserImage;
    private final TextView tvCaption;
    private final TextView tvLocation;
    private final View postDivider;

    public ViewHolder(@NonNull View itemView) {
      super(itemView);
      tvUsername = itemView.findViewById(R.id.tvUsername);
      ivUserImage = itemView.findViewById(R.id.ivUserImage);
      tvCaption = itemView.findViewById(R.id.tvCaption);
      tvLocation = itemView.findViewById(R.id.tvLocation);
      postDivider = itemView.findViewById(R.id.postSeparator);
    }

    public void bind(Post post) throws ParseException {
      tvCaption.setText(post.getKeyDescription());
      if (post.getKeyUser() == null) {
        //        User doesn't exist
        postDivider.setVisibility(View.GONE);
        tvUsername.setVisibility(View.GONE);
        ivUserImage.setVisibility(View.GONE);
        tvCaption.setVisibility(View.GONE);
        tvLocation.setVisibility(View.GONE);
        return;
      } else {
        tvUsername.setText(post.getKeyUser().getUsername());
      }
      tvLocation.setText(post.getKeyLocation());
      ParseFile image = post.getImage();
      if (image != null) {
        Glide.with(context).load(image.getUrl()).into(ivUserImage);
        ivUserImage.setVisibility(View.VISIBLE);
      } else {
        ivUserImage.setVisibility(View.GONE);
      }
      if (tvLocation.getText().toString().equals("")) {
        tvLocation.setVisibility(View.GONE);
      }
    }
  }
}
