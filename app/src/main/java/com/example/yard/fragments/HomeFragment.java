package com.example.yard.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yard.R;
import com.example.yard.adapter.PostCreation;
import com.example.yard.adapter.PostsAdapter;
import com.example.yard.helperclass.QueryPosts;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
  public static final String TAG = "HomeFragment";
  final Handler handler = new Handler();
  ProgressBar fetchMoreBar;
  NestedScrollView nestedScrollView;
  int value;
  boolean hasFetchedData = false;
  private PostsAdapter adapter;
  private List<PostCreation> allPosts;
  private QueryPosts queryPosts;
  private ProgressBar loadingBar;

  public HomeFragment() {}

  @Nullable
  public View onCreateView(
      LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_home, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    RecyclerView rvPosts = getView().findViewById(R.id.rvPosts);
    loadingBar = view.findViewById(R.id.progressBarId);
    fetchMoreBar = view.findViewById(R.id.fetchMoreBar);
    nestedScrollView = view.findViewById(R.id.scrollBar);
    allPosts = new ArrayList<>();
    adapter = new PostsAdapter(getContext(), allPosts);
    rvPosts.setAdapter(adapter);
    rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));
    queryPosts = new QueryPosts();
    startThread();
    nestedScrollView.setOnScrollChangeListener(
        new NestedScrollView.OnScrollChangeListener() {
          @Override
          public void onScrollChange(
              NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
            if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
              fetchMoreBar.setVisibility(View.VISIBLE);
              startThread();
            }
          }
        });
  }

  public void startThread() {
    startActivityBackground startActivity = new startActivityBackground();
    new Thread(startActivity).start();
  }

  public Boolean showProgressBar() {
    return true;
  }

  public class startActivityBackground implements Runnable {
    @Override
    public void run() {
      // if the user is just logging in
      if (!hasFetchedData) {
        for (value = 0; value <= 100; value++) {
          try {
            Thread.sleep(50);
            loadingBar.setProgress(value);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
      handler.post(
          new Runnable() {
            @Override
            public void run() {
              loadingBar.setVisibility(View.GONE);
              if (showProgressBar()) {
                queryPosts.queryPosts(false, adapter, allPosts, new OnQueryDone());
              }
              hasFetchedData = true;
            }
          });
    }

    public class OnQueryDone implements QueryPosts.Callback {
      @Override
      public void call() {
        fetchMoreBar.setVisibility(View.GONE);
      }
    }
  }
}
