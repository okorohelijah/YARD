package com.example.yard.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.GestureDetector;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.yard.Adapter.PostCreation;
import com.example.yard.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

@RequiresApi(api = Build.VERSION_CODES.O)
public class PostActivity extends AppCompatActivity implements View.OnTouchListener, GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
  public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 45;
  public static final int REQUEST_CODE = 100;
  public static final String TAG = "PostActivity";
  private static final String PHOTO_FILE_NAME = "photo.jpg";
  FusedLocationProviderClient fusedLocationProviderClient;
  private Button btnGetLocation;
  private TextView tvUserAddress;
  private Button btnRemoveLocation;
  private TextView etDescription;
  private File photoFile;
  private ImageView ivImage;
  private GestureDetector gestureDetector;
  private PostCreation post;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_post);
    Button takePicture = findViewById(R.id.btnTakePicture);
    Button submit = findViewById(R.id.btnSubmit);
    Button upload = findViewById(R.id.btnUploadPicture);
    etDescription = findViewById(R.id.tvDescription);
    ivImage = findViewById(R.id.ivImage);
    tvUserAddress = findViewById(R.id.tvLocationAddress);
    tvUserAddress.setText("");
    btnGetLocation = findViewById(R.id.btnGetLocation);
    btnRemoveLocation = findViewById(R.id.btnRemoveLocation);
    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    btnGetLocation.setOnClickListener(view -> getLastLocation());
    takePicture.setOnClickListener(view -> launchcamera());
    ivImage.setOnTouchListener(this);
    PostCreation post;
    gestureDetector = new GestureDetector(this, this);

    submit.setOnClickListener(
        view -> {
          String description = etDescription.getText().toString();
          String userLocation = tvUserAddress.getText().toString();

          if (description.isEmpty()) {
            Toast.makeText(PostActivity.this, "Description cannot be empty", Toast.LENGTH_SHORT)
                .show();
            return;
          }
          ParseUser currentUser = ParseUser.getCurrentUser();
          savePost(description, userLocation, currentUser, photoFile);
        });
    btnRemoveLocation.setOnClickListener(
        view -> {
          String userLocation = tvUserAddress.getText().toString();
          tvUserAddress.setText("");
        });
  }

  private void getLastLocation() {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED) {
      fusedLocationProviderClient
          .getLastLocation()
          .addOnSuccessListener(
              location -> {
                if (location != null) {
                  Geocoder geocoder = new Geocoder(PostActivity.this, Locale.getDefault());
                  List<Address> addresses;
                  try {
                    addresses =
                        geocoder.getFromLocation(
                            location.getLatitude(), location.getLongitude(), 1);
                    tvUserAddress.setText(
                        ""
                            + addresses.get(0).getLocality()
                            + ", "
                            + addresses.get(0).getCountryName());
                  } catch (IOException e) {
                    e.printStackTrace();
                  }

                } else {
                  askPermission();
                }
              });
    }
  }

  private void askPermission() {
    ActivityCompat.requestPermissions(
        PostActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (requestCode == REQUEST_CODE) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        getLastLocation();
      } else {
        Toast.makeText(this, "Required Permission", Toast.LENGTH_SHORT).show();
      }
    }
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  private void launchcamera() {
    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    photoFile = getPhotoFileUri(PHOTO_FILE_NAME);
    Uri fileProvider =
        FileProvider.getUriForFile(PostActivity.this, "com.codepath.fileprovider", photoFile);
    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);
    if (intent.resolveActivity(PostActivity.this.getPackageManager()) != null) {
      startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
      if (resultCode == RESULT_OK) {
        Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
        ivImage.setImageBitmap(takenImage);
      } else { // Result was a failure
        Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
      }
    }
  }

  public File getPhotoFileUri(String fileName) {
    File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);
    if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
      Log.d(TAG, "failed to create directory");
    }
    return new File(mediaStorageDir.getPath() + File.separator + fileName);
  }

  private void savePost(
      String description, String userLocation, ParseUser currentUser, File photoFile) {
    PostCreation postCreation = new PostCreation();
    postCreation.setKeyDescription(description);
    postCreation.setKeyLocation(userLocation);
    if (ivImage.getDrawable() != null) {
      postCreation.setImage(new ParseFile(photoFile));
    }
    postCreation.setKeyUser(currentUser);
    postCreation.saveInBackground(
        e -> {
          if (e != null) {
            Log.e("error", "error while saving:" + e);
            Toast.makeText(PostActivity.this, "error while saving", Toast.LENGTH_SHORT).show();
          }
          etDescription.setText("");
          ivImage.setImageResource(0);
        });
  }

  @Override
  public boolean onTouch(View view, MotionEvent motionEvent) {
    gestureDetector.onTouchEvent(motionEvent);
    return true ;
  }

  @Override
  public boolean onDown(MotionEvent motionEvent) {
    return false;
  }

  @Override
  public void onShowPress(MotionEvent motionEvent) {

  }

  @Override
  public boolean onSingleTapUp(MotionEvent motionEvent) {
    return false;
  }

  @Override
  public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
    return false;
  }

  @Override
  public void onLongPress(MotionEvent motionEvent) {
    ivImage.setImageResource(0);

  }

  @Override
  public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
    return false;
  }

  @Override
  public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
    return false;
  }

  @Override
  public boolean onDoubleTap(MotionEvent motionEvent) {
    return false;
  }

  @Override
  public boolean onDoubleTapEvent(MotionEvent motionEvent) {
    return false;
  }
}