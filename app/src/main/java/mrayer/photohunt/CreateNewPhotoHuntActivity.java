package mrayer.photohunt;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.UUID;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CreateNewPhotoHuntActivity extends AppCompatActivity {
    private String mCurrentPhotoPath;

    private ImageAdapter imageAdapter;

    private Uri mostRecentTakenPhoto;

    private EditText inputNameEditText;
    private EditText inputAuthorEditText;
    private EditText inputLocationEditText;
    private Spinner typeSpinner;

    private ViewPager viewPager;
    private Button addFromGalleryButton;
    private Button takePhotoButton;
    private Button uploadButton;
    private Button cancelButton;
    private Button viewAddLocationButton;

    // file path - > location
    private HashMap<String, LatLng> manualLocations;

    private ProgressDialog uploadDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_photo_hunt);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        imageAdapter = new ImageAdapter(this);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(imageAdapter);
        manualLocations = new HashMap<String, LatLng>();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Create New Photo Hunt");

        inputNameEditText = (EditText) findViewById(R.id.input_name);
        inputAuthorEditText = (EditText) findViewById(R.id.input_author);
        inputLocationEditText = (EditText) findViewById(R.id.input_location);
        typeSpinner = (Spinner) findViewById(R.id.spinner_type);

        addFromGalleryButton = (Button) findViewById(R.id.add_from_gallery_button);
        addFromGalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, Constants.REQUEST_LOAD_IMAGE);
            }
        });

        uploadButton = (Button) findViewById(R.id.upload_button);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // grab fields
                String photoHuntName = ((EditText) findViewById(R.id.input_name)).getText().toString();
                String photoHuntAuthor = ((EditText) findViewById(R.id.input_author)).getText().toString();
                String photoHuntLocation = ((EditText) findViewById(R.id.input_location)).getText().toString();
                String photoHuntType = ((Spinner) findViewById(R.id.spinner_type)).getSelectedItem().toString();

                final String albumId = UUID.randomUUID().toString();

                // create Parse Object for the album
                ParseObject photoHunt = new ParseObject("PhotoHuntAlbum");
                photoHunt.put("name", photoHuntName);
                photoHunt.put("author", photoHuntAuthor);
                photoHunt.put("location", photoHuntLocation);
                photoHunt.put("type", photoHuntType);
                photoHunt.put("albumId", albumId);
                photoHunt.put("numPhotos", imageAdapter.getGalImages().size());
                photoHunt.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.d(Constants.CreateNewPhotoHunt_Tag, e.toString());
                        } else {
                            Log.d(Constants.CreateNewPhotoHunt_Tag, "Photo hunt saved.");
                        }
                    }
                });

                // create Parse File for each photo
                int index = 0;
                int numImages = imageAdapter.getGalImages().size();
                setupUploadDialog(numImages);
                for(String imagePath: imageAdapter.getGalImages()) {
                    File file = new File(imagePath);

                    int height = (int) getResources().getDimension(R.dimen.create_new_photo_hunt_image_size);
                    int width = getResources().getDisplayMetrics().widthPixels;

                    Bitmap bitmap = ImageUtils.decodeFile(file, width, height);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] byteArray= stream.toByteArray();

                    final ParseFile photo = new ParseFile(file.getName(), byteArray);
                    // if user set the location, it will be in the manual locations map, otherwise, grab it from the file
                    final LatLng location = manualLocations.containsKey(imagePath) ? manualLocations.get(imagePath) : ImageUtils.getImageLocation(file);
                    // TODO: make sure that all files have a location, either inside or manual before allowing uploading
                    // TODO: make sure anytime I grab location, I check the manual map and also make sure that the meta location isn't null

                    photo.saveInBackground(new PhotoSaveCallback(albumId, photo, location, index == 0),
                            new PhotoProgressCallback(uploadDialog, index, numImages));
                    index++;
                }
            }
        });

        cancelButton = (Button) findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                return;
            }
        });

        takePhotoButton = (Button) findViewById(R.id.take_photo_button);
        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        viewAddLocationButton = (Button) findViewById(R.id.view_add_location_button);
        viewAddLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageAdapter.getGalImages().size() == 0) {
                    // no images, so we can't set any locations
                    return;
                }

                int currentItem = viewPager.getCurrentItem();
                String filePath = imageAdapter.getGalImages().get(currentItem);
                File currentFile = new File(imageAdapter.getGalImages().get(currentItem));

                LatLng loc;
                Bundle args = new Bundle();
                Intent addSetLocationIntent = new Intent(CreateNewPhotoHuntActivity.this, SetChangeLocationActivity.class);
                if(manualLocations.containsKey(filePath)) {
                    // user has previously set a location for this file
                    args.putParcelable("location", manualLocations.get(filePath));
                    addSetLocationIntent.putExtra("bundle", args);
                }
                else if((loc = ImageUtils.getImageLocation(currentFile)) != null) {
                    // able to find a location in the metadata
                    args.putParcelable("location", loc);
                    addSetLocationIntent.putExtra("bundle", args);
                }

                startActivityForResult(addSetLocationIntent, Constants.REQUEST_SET_ADD_LOCATION);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("name", inputNameEditText.getText().toString());
        outState.putString("author", inputAuthorEditText.getText().toString());
        outState.putString("location", inputLocationEditText.getText().toString());
        outState.putInt("type_number", typeSpinner.getSelectedItemPosition());
        outState.putStringArrayList("galImages", imageAdapter.getGalImages());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        inputNameEditText.setText(savedInstanceState.getString("name"));
        inputAuthorEditText.setText(savedInstanceState.getString("author"));
        inputLocationEditText.setText(savedInstanceState.getString("location"));
        typeSpinner.setSelection(savedInstanceState.getInt("type_number"));
        ArrayList<String> list = savedInstanceState.getStringArrayList("galImages");
        imageAdapter.setGalImages(list);
        imageAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_SET_ADD_LOCATION && resultCode == RESULT_OK && data != null) {
            // user changed or set a photo's location, so add it to the mapping of manual locations
            int currentItem = viewPager.getCurrentItem();
            String filePath = imageAdapter.getGalImages().get(currentItem);
            Bundle bundle = data.getParcelableExtra("bundle");
            manualLocations.put(filePath, (LatLng) bundle.getParcelable("location"));
        }
        else if (requestCode == Constants.REQUEST_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);

            // String picturePath contains the path of selected Image
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            ArrayList<String> galImages = imageAdapter.getGalImages();

            // check if galImages already has images
            if (!galImages.contains(picturePath)) {
                galImages.add(picturePath);
                imageAdapter.setGalImages((galImages));
                imageAdapter.notifyDataSetChanged();
                viewPager.setCurrentItem(galImages.size() - 1);
            }
        }
        else if (requestCode == Constants.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            restorePreferences();
            galleryAddPic();
//            String[] filePathColumn = {MediaStore.Images.Media.DATA};
//
//            Cursor cursor = getContentResolver().query(mostRecentTakenPhoto,
//                    filePathColumn, null, null, null);
//            cursor.moveToFirst();
//
//            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//
//            // String picturePath contains the path of selected Image
//            String picturePath = cursor.getString(columnIndex);
//            cursor.close();


            ArrayList<String> galImages = imageAdapter.getGalImages();

            // check if galImages already has images
            if (!galImages.contains(mCurrentPhotoPath)) {
                galImages.add(mCurrentPhotoPath);
                imageAdapter.setGalImages((galImages));
                imageAdapter.notifyDataSetChanged();
                viewPager.setCurrentItem(galImages.size() - 1);
            }
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e("CREATE PHOTO HUNT", "ERROR OCCURED WHILE CREATING FILE FOR NEW PICTURE");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                mostRecentTakenPhoto = Uri.fromFile(photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mostRecentTakenPhoto);
                savePreferences();
                startActivityForResult(takePictureIntent, Constants.REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp;
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath =  image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void savePreferences(){
        // We need an Editor object to make preference changes.
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putString("mCurrentPhotoPath", mCurrentPhotoPath);

        // Commit the edits!
        editor.commit();
    }

    private void restorePreferences() {
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        mCurrentPhotoPath = settings.getString("mCurrentPhotoPath", "");
    }

    private void setupUploadDialog(int numPhotos) {
        uploadDialog = new ProgressDialog(this);
        uploadDialog.setMessage("Uploading...");
        uploadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        uploadDialog.setProgress(0);
        uploadDialog.setMax(100);
        uploadDialog.setCancelable(false);
        uploadDialog.show();
    }
}
