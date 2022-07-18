package com.example.yard.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yard.R;
import com.example.yard.adapter.PostCreation;
import com.example.yard.adapter.PostsAdapter;
import com.example.yard.helperclass.QueryPosts;

import java.util.ArrayList;
import java.util.List;

public class BioGridFragment extends Fragment {
  protected RecyclerView rvPosts;
  protected List<PostCreation> allPosts;
  protected PostsAdapter adapter;
  QueryPosts queryPosts;

  @Nullable
  public View onCreateView(
      LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_bio_grid, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    rvPosts = view.findViewById(R.id.rvPosts);
    allPosts = new ArrayList<>();
    adapter = new PostsAdapter(getContext(), allPosts);
    rvPosts.setAdapter(adapter);
    rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));
    queryPosts = new QueryPosts();
    queryPosts.queryPosts(true, adapter, allPosts, null);
  }
}