package mrayer.photohunt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseFile;
import com.parse.ParseObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.UUID;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

// TODO: make sure that all files have a location, either inside or manual before allowing uploading
// TODO: make sure anytime I grab location, I check the manual map and also make sure that the meta location isn't null

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

    private PhotoUploadProgressDialog uploadDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_photo_hunt);
        Toolbar toolbar = (Toolbar) findViewById(R.id.create_new_photo_hunt_toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        imageAdapter = new ImageAdapter(this);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(imageAdapter);

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
                if (!checkFields()) {
                    // if one or more of the EditTexts aren't filled out, don't upload and send error message
                    Toast.makeText(getApplicationContext(), "Required fields are not filled out.", Toast.LENGTH_LONG).show();
                    return;
                }
                else if (imageAdapter.getGalImages().size() == 0) {
                    // no images to upload
                    Toast.makeText(getApplicationContext(), "No photos to upload.", Toast.LENGTH_LONG).show();
                    return;
                }
                else {
                    for(String imagePath: imageAdapter.getGalImages()) {
                        if(imageAdapter.getManualLocation(imagePath) == null && imageAdapter.getMetaLocation(imagePath) == null) {
                            // all photos must have a location
                            Toast.makeText(getApplicationContext(), "Please ensure all photos have locations.", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                }

                setupUploadDialog(imageAdapter.getGalImages().size());

                // create Parse Object for the album
                String albumId = UUID.randomUUID().toString();
                PhotoHuntAlbum photoHunt = createPhotoHunt(albumId);

                new UploadAlbumTask(photoHunt, imageAdapter.getGalImages()).execute();
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
        viewPager.addOnPageChangeListener(new CustomOnPageChangeListener(viewAddLocationButton));
        viewAddLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageAdapter.getGalImages().size() == 0) {
                    // no images, so we can't set any locations
                    Toast.makeText(getApplicationContext(), "No images have been added.", Toast.LENGTH_LONG).show();
                    return;
                }

                int currentItem = viewPager.getCurrentItem();
                String filePath = imageAdapter.getGalImages().get(currentItem);

                Bundle args = new Bundle();
                Intent addSetLocationIntent = new Intent(CreateNewPhotoHuntActivity.this, SetChangeLocationActivity.class);

                LatLng manualLocation = imageAdapter.getManualLocation(filePath);
                LatLng metaLocation = imageAdapter.getMetaLocation(filePath);

                if(manualLocation != null) {
                    // user has previously set a location for this file
                    args.putParcelable("location", manualLocation);
                    addSetLocationIntent.putExtra("bundle", args);
                }
                else if(metaLocation != null) {
                    // able to find a location in the metadata
                    args.putParcelable("location", metaLocation);
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
            imageAdapter.addManualLocation(filePath, (LatLng) bundle.getParcelable("location"));
            viewAddLocationButton.setText("View/Set Location");
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

            // make sure it is valid
            if(!Utils.validImage(picturePath)) {
                makeAndShowInvalidImageToast();
                return;
            }

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
            ArrayList<String> galImages = imageAdapter.getGalImages();

            // make sure it is valid
            if(!Utils.validImage(mCurrentPhotoPath)) {
                makeAndShowInvalidImageToast();
                return;
            }

            // check if galImages already has images
            if (!galImages.contains(mCurrentPhotoPath)) {
                galImages.add(mCurrentPhotoPath);
                imageAdapter.setGalImages((galImages));
                imageAdapter.notifyDataSetChanged();
                viewPager.setCurrentItem(galImages.size() - 1);
            }
        }
    }

    private boolean checkFields() {
        if (inputNameEditText.getText().length() == 0 || inputAuthorEditText.getText().length() == 0
                || inputLocationEditText.getText().length() == 0) {
            return false;
        }

        return true;
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
                ".png",         /* suffix */
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
        editor.apply();
    }

    private void restorePreferences() {
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        mCurrentPhotoPath = settings.getString("mCurrentPhotoPath", "");
    }

    private void setupUploadDialog(int numPhotos) {
        uploadDialog = new PhotoUploadProgressDialog(this, numPhotos);
        uploadDialog.setup();
    }

    private PhotoHuntAlbum createPhotoHunt(String albumId) {
        String photoHuntName = ((EditText) findViewById(R.id.input_name)).getText().toString();
        String photoHuntAuthor = ((EditText) findViewById(R.id.input_author)).getText().toString();
        String photoHuntLocation = ((EditText) findViewById(R.id.input_location)).getText().toString();
        String photoHuntType = ((Spinner) findViewById(R.id.spinner_type)).getSelectedItem().toString();

        PhotoHuntAlbum photoHunt = new PhotoHuntAlbum();
        photoHunt.setName(photoHuntName);
        photoHunt.setAuthor(photoHuntAuthor);
        photoHunt.setLocation(photoHuntLocation);
        photoHunt.setType(photoHuntType);
        photoHunt.setAlbumId(albumId);
        photoHunt.setNumPhotos(imageAdapter.getGalImages().size());

        return photoHunt;
    }

    private void makeAndShowInvalidImageToast() {
        Toast toast = Toast.makeText(this, "Invalid image type.\nAccepted types are .png, and .jpg/.jpeg.", Toast.LENGTH_LONG);
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        if( v != null) v.setGravity(Gravity.CENTER);
        toast.show();
    }

    private class UploadAlbumTask extends AsyncTask<Void, Void, Void> {

        private PhotoHuntAlbum albumToUpload;
        private ArrayList<String> photos;

        public UploadAlbumTask(PhotoHuntAlbum albumToUpload, ArrayList<String> photos) {
            this.albumToUpload = albumToUpload;
            this.photos = photos;
        }

        @Override protected Void doInBackground(Void... params) {
            int index = 0;
            int numImages = photos.size();

            // upload all of the shit
            for(String imagePath: photos) {
                File file = new File(imagePath);

                int height = (int)getResources().getDimension(R.dimen.create_new_photo_hunt_image_size);
                int width = getResources().getDisplayMetrics().widthPixels;

                Bitmap bitmap = Utils.decodeFile(file, width, height);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();

                Bitmap.CompressFormat compressFormat = Utils.determineCompresionFormat(file.getName());

                byte[] byteArray;
                if(compressFormat != null) {
                    bitmap.compress(compressFormat, 100, stream);
                    byteArray = Utils.compressImage(bitmap, compressFormat);
                }
                else {
                    // I def feel like we should only accept jpg or png
                    byteArray = stream.toByteArray();
                }

                ParseFile photo = new ParseFile(file.getName(), byteArray);
                LatLng location;

                LatLng manualLocation = imageAdapter.getManualLocation(imagePath);
                LatLng metaLocation = imageAdapter.getMetaLocation(imagePath);

                if(manualLocation != null) {
                    location = manualLocation;
                }
                else {
                    location = metaLocation;
                }

                if(index > 0) {
                    // non-cover photo
                    photo.saveInBackground(new PhotoSaveCallback(albumToUpload.getAlbumId(), photo, location, index), new PhotoProgressCallback(uploadDialog, index, numImages));
                }
                else {
                    // cover photo
                    photo.saveInBackground(new PhotoSaveCallback(albumToUpload.getAlbumId(), photo, location, index, albumToUpload), new PhotoProgressCallback(uploadDialog, index, numImages));
                }
                index++;
            }

            return null;
        }
    }

    private class CustomOnPageChangeListener implements ViewPager.OnPageChangeListener {

        private Button toUpdate;

        public CustomOnPageChangeListener(Button toUpdate) {
            this.toUpdate = toUpdate;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if( imageAdapter.getGalImages().size() == 0) {return;}
            String filePath = imageAdapter.getGalImages().get(position);

            if(imageAdapter.getMetaLocation(filePath) != null || imageAdapter.getManualLocation(filePath) != null) {
                toUpdate.setText("View/Set Location");
            }
            else {
                toUpdate.setText("Set Location");
            }
        }

        @Override
        public void onPageSelected(int position) {
            // don't need to implement
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            // don't need to implement
        }
    }
}
