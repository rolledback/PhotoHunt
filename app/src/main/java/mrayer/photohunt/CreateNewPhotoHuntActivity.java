package mrayer.photohunt;

import android.app.Activity;
import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.UUID;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

// TODO: make sure that all files have a location, either inside or manual before allowing uploading
// TODO: make sure anytime I grab location, I check the manual map and also make sure that the meta location isn't null

public class CreateNewPhotoHuntActivity extends AppCompatActivity {
    private String mCurrentPhotoPath;

    private CreateNewPhotoHuntImageAdapter createNewPhotoHuntImageAdapter;

    private Uri mostRecentTakenPhoto;

    private EditText inputNameEditText;
    private EditText inputLocationEditText;
    private EditText inputDescriptionEditText;
    private Spinner typeSpinner;

    private FrameLayout viewPagerLayout;

    private ViewPager viewPager;
    private Button addFromGalleryButton;
    private Button takePhotoButton;
    private Button uploadButton;
    private Button cancelButton;
    private ImageView locationStatus;

    private PhotoUploadProgressDialog uploadDialog;
    private AlertDialog quitConfirmation;

    private AlertDialog.Builder dialogBuilder;
    private LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_photo_hunt);
        Toolbar toolbar = (Toolbar) findViewById(R.id.create_new_photo_hunt_toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder = new AlertDialog.Builder(this);
        inflater = this.getLayoutInflater();

        viewPagerLayout = (FrameLayout) findViewById(R.id.view_pager_layout);
        viewPagerLayout.setVisibility(View.GONE);

        createNewPhotoHuntImageAdapter = new CreateNewPhotoHuntImageAdapter(this, inflater, dialogBuilder, this);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.addOnPageChangeListener(new CustomOnPageChangeListener());
        viewPager.setAdapter(createNewPhotoHuntImageAdapter);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Create New Photo Hunt");

        inputNameEditText = (EditText) findViewById(R.id.input_name);
        inputLocationEditText = (EditText) findViewById(R.id.input_location);
        inputDescriptionEditText = (EditText) findViewById(R.id.input_description);
        inputDescriptionEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (v.getId() == R.id.input_description) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_UP:
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            break;
                    }
                }
                return false;
            }
        });
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
                else if (createNewPhotoHuntImageAdapter.getGalImages().size() == 0) {
                    // no images to upload
                    Toast.makeText(getApplicationContext(), "No photos to upload.", Toast.LENGTH_LONG).show();
                    return;
                }
                else {
                    for(String imagePath: createNewPhotoHuntImageAdapter.getGalImages()) {
                        if(createNewPhotoHuntImageAdapter.getManualLocation(imagePath) == null && createNewPhotoHuntImageAdapter.getMetaLocation(imagePath) == null) {
                            // all photos must have a location
                            Toast.makeText(getApplicationContext(), "Please ensure all photos have locations.", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                }

                setupUploadDialog(createNewPhotoHuntImageAdapter.getGalImages().size());

                // create Parse Object for the album
                String albumId = UUID.randomUUID().toString();
                PhotoHuntAlbum photoHunt = createPhotoHunt(albumId);

                new UploadAlbumTask(photoHunt, createNewPhotoHuntImageAdapter.getGalImages()).execute();
            }
        });

        cancelButton = (Button) findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.getAdapter().notifyDataSetChanged();// finish();
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

        locationStatus = (ImageView) findViewById(R.id.location_status);
        locationStatus.setVisibility(View.GONE);

        // this only changes if the uplaod dialog was cancelled after upload complete
        setResult(Activity.RESULT_CANCELED);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if(userStartedCreating()) {
                confirmExit();
            }
            else {
                finish();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(userStartedCreating()) {
            confirmExit();
        }
        else {
            finish();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("name", inputNameEditText.getText().toString());
        outState.putString("location", inputLocationEditText.getText().toString());
        outState.putInt("type_number", typeSpinner.getSelectedItemPosition());
        outState.putString("description", inputDescriptionEditText.getText().toString());
        outState.putStringArrayList("galImages", createNewPhotoHuntImageAdapter.getGalImages());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        inputNameEditText.setText(savedInstanceState.getString("name"));
        inputLocationEditText.setText(savedInstanceState.getString("location"));
        inputDescriptionEditText.setText(savedInstanceState.getString("description"));
        typeSpinner.setSelection(savedInstanceState.getInt("type_number"));
        ArrayList<String> list = savedInstanceState.getStringArrayList("galImages");
        createNewPhotoHuntImageAdapter.setGalImages(list);
        createNewPhotoHuntImageAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_SET_ADD_LOCATION && resultCode == RESULT_OK && data != null) {
            // user changed or set a photo's location, so add it to the mapping of manual locations
            int currentItem = viewPager.getCurrentItem();
            String filePath = createNewPhotoHuntImageAdapter.getGalImages().get(currentItem);
            Bundle bundle = data.getParcelableExtra("bundle");
            createNewPhotoHuntImageAdapter.addManualLocation(filePath, (LatLng) bundle.getParcelable("location"));
            locationStatus.setImageResource(R.drawable.ic_done_black_24dp);
        }
        else if (requestCode == Constants.REQUEST_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            viewPagerLayout.setVisibility(View.VISIBLE);
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

            ArrayList<String> galImages = createNewPhotoHuntImageAdapter.getGalImages();

            // check if galImages already has images
            if (!galImages.contains(picturePath)) {
                galImages.add(picturePath);
                createNewPhotoHuntImageAdapter.setGalImages((galImages));
                createNewPhotoHuntImageAdapter.notifyDataSetChanged();

                viewPager.setCurrentItem(galImages.size() - 1);
            }
            else {
                makeAndShowDuplicateToast();
            }
        }
        else if (requestCode == Constants.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            viewPagerLayout.setVisibility(View.VISIBLE);
            restorePreferences();
            galleryAddPic();
            ArrayList<String> galImages = createNewPhotoHuntImageAdapter.getGalImages();

            // make sure it is valid
            if(!Utils.validImage(mCurrentPhotoPath)) {
                makeAndShowInvalidImageToast();
                return;
            }

            // check if galImages already has images
            if (!galImages.contains(mCurrentPhotoPath)) {
                galImages.add(mCurrentPhotoPath);
                createNewPhotoHuntImageAdapter.setGalImages((galImages));
                createNewPhotoHuntImageAdapter.notifyDataSetChanged();
                viewPager.setCurrentItem(galImages.size() - 1);
            }
        }
    }

    private boolean checkFields() {
        if (inputNameEditText.getText().length() == 0 || inputLocationEditText.getText().length() == 0) {
            return false;
        }
        return true;
    }

    public void confirmExit() {
        dialogBuilder.setTitle("Warning");
        dialogBuilder.setMessage("Your progress will be lost if you return to the gallery. " +
                "Are you sure you want to continue?");
        dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                quitConfirmation.dismiss();
                quitConfirmation = null;
                finish();
            }

        });
        dialogBuilder.setNegativeButton("No", null);
        quitConfirmation = dialogBuilder.show();
    }

    public boolean userStartedCreating() {
        return inputNameEditText.getText().length() != 0 || inputLocationEditText.getText().length() != 0 ||
                inputDescriptionEditText.getText().length() != 0 || viewPager.getAdapter().getCount() != 0;
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
                Log.e(Constants.CreateNewPhotoHuntTag, "ERROR OCCURED WHILE CREATING FILE FOR NEW PICTURE");
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
        // multiply numPhoto by two to account for thumbnails
        uploadDialog = new PhotoUploadProgressDialog(this, numPhotos * 2);
        uploadDialog.setup();
        uploadDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                setResult(Activity.RESULT_OK);
                finish(); //If you want to finish the activity.
            }
        });
    }

    private PhotoHuntAlbum createPhotoHunt(String albumId) {
        String photoHuntName = ((EditText) findViewById(R.id.input_name)).getText().toString();
        String photoHuntAuthorId = ParseUser.getCurrentUser().getObjectId();
        String photoHuntAuthor = ParseUser.getCurrentUser().getString("username");
        String photoHuntLocation = ((EditText) findViewById(R.id.input_location)).getText().toString();
        String photoHuntDescription = ((EditText) findViewById(R.id.input_description)).getText().toString();
        String photoHuntType = ((Spinner) findViewById(R.id.spinner_type)).getSelectedItem().toString();

        PhotoHuntAlbum photoHunt = new PhotoHuntAlbum();
        photoHunt.setName(photoHuntName);
        photoHunt.setAuthorId(photoHuntAuthorId);
        photoHunt.setAuthor(photoHuntAuthor);
        photoHunt.setLocation(photoHuntLocation);
        photoHunt.setType(photoHuntType);
        photoHunt.setAlbumId(albumId);
        photoHunt.setDescription(photoHuntDescription);
        photoHunt.setNumPhotos(createNewPhotoHuntImageAdapter.getGalImages().size());
        photoHunt.setSearchName(photoHuntName.toLowerCase());
        photoHunt.setSearchAuthor(photoHuntAuthor.toLowerCase());
        photoHunt.setSearchLocation(photoHuntLocation.toLowerCase());
        photoHunt.setSearchDescription(photoHuntDescription.toLowerCase());

        return photoHunt;
    }

    private void makeAndShowInvalidImageToast() {
        Toast toast = Toast.makeText(this, "Invalid image type.\nAccepted types are .png, and .jpg/.jpeg.", Toast.LENGTH_LONG);
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        if( v != null) v.setGravity(Gravity.CENTER);
        toast.show();
    }

    private void makeAndShowDuplicateToast() {
        Toast toast = Toast.makeText(this, "This image has already been added to the photo hunt.", Toast.LENGTH_LONG);
        toast.show();
    }

    public void launchViewSetLocation() {
        if (createNewPhotoHuntImageAdapter.getGalImages().size() == 0) {
            // no images, so we can't set any locations
            Toast.makeText(getApplicationContext(), "No images have been added.", Toast.LENGTH_LONG).show();
            return;
        }

        int currentItem = viewPager.getCurrentItem();
        String filePath = createNewPhotoHuntImageAdapter.getGalImages().get(currentItem);

        Bundle args = new Bundle();
        Intent addSetLocationIntent = new Intent(CreateNewPhotoHuntActivity.this, SetChangeLocationActivity.class);

        LatLng manualLocation = createNewPhotoHuntImageAdapter.getManualLocation(filePath);
        LatLng metaLocation = createNewPhotoHuntImageAdapter.getMetaLocation(filePath);

        if (manualLocation != null) {
            // user has previously set a location for this file
            args.putParcelable("location", manualLocation);
            addSetLocationIntent.putExtra("bundle", args);
        } else if (metaLocation != null) {
            // able to find a location in the metadata
            args.putParcelable("location", metaLocation);
            addSetLocationIntent.putExtra("bundle", args);
        }

        startActivityForResult(addSetLocationIntent, Constants.REQUEST_SET_ADD_LOCATION);
    }

    public void launchViewPhoto(String path) {
        Intent viewPhotoIntent = new Intent(CreateNewPhotoHuntActivity.this, ViewPhotoActivity.class);
        viewPhotoIntent.putExtra("path", path);
        startActivity(viewPhotoIntent);
    }

    public void hideViewPager() {
        if(createNewPhotoHuntImageAdapter.getCount() == 0) {
            viewPagerLayout.setVisibility(View.GONE);
        }
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
            int numPhoto = 0;
            int numImages = photos.size();

            // upload all of the shit
            for(String imagePath: photos) {
                File file = new File(imagePath);

                int height = (int)getResources().getDimension(R.dimen.create_new_photo_hunt_image_size);
                int width = getResources().getDisplayMetrics().widthPixels;

                Bitmap thumbBitmap;
                byte[] fullByteArray, thumbByteArray;
                Bitmap fullBitmap = Utils.decodeFile(file, width, height);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                Bitmap.CompressFormat compressFormat = Utils.determineCompresionFormat(file.getName());

                if(compressFormat != null) {
                    fullBitmap.compress(compressFormat, 100, stream);
                    thumbBitmap = Utils.createThumbnail(fullBitmap);

                    fullByteArray = Utils.compressImage(fullBitmap, compressFormat);
                    thumbByteArray = Utils.compressImage(thumbBitmap, compressFormat);
                }
                else {
                    Log.w(Constants.CreateNewPhotoHuntTag, "Unable to find a compression format for photo: " + imagePath);
                    continue;
                }

                int lastDot = file.getName().lastIndexOf('.');
                String thumbnailFileName = file.getName().substring(0, lastDot) + "_thumbnail" + file.getName().substring(lastDot);

                ParseFile fullPhoto = new ParseFile(file.getName(), fullByteArray);
                ParseFile thumbPhoto = new ParseFile(thumbnailFileName, thumbByteArray);

                Photo photoObject = new Photo();
                photoObject.setAlbumId(albumToUpload.getAlbumId());
                photoObject.setLocation(createNewPhotoHuntImageAdapter.getLocation(imagePath));
                photoObject.setIndex(index);

                if(index > 0) {
                    // non-cover photo
                    fullPhoto.saveInBackground(new PhotoSaveCallback(fullPhoto, thumbPhoto, photoObject, numPhoto + 1, uploadDialog),
                            new PhotoProgressCallback(uploadDialog, numPhoto));
                }
                else {
                    // cover photo
                    fullPhoto.saveInBackground(new PhotoSaveCallback(fullPhoto, thumbPhoto, photoObject, albumToUpload, numPhoto + 1, uploadDialog),
                            new PhotoProgressCallback(uploadDialog, numPhoto));
                }
                index++;
                numPhoto += 2;
            }

            return null;
        }
    }

    private class CustomOnPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if(createNewPhotoHuntImageAdapter.getGalImages().size() == 0) {
                viewPagerLayout.setVisibility(View.GONE);
                return;
            }
            String filePath = createNewPhotoHuntImageAdapter.getGalImages().get(position);
            locationStatus.setVisibility(View.INVISIBLE);

            if(createNewPhotoHuntImageAdapter.getMetaLocation(filePath) != null || createNewPhotoHuntImageAdapter.getManualLocation(filePath) != null) {
                locationStatus.setImageResource(R.drawable.ic_done_black_24dp);
            }
            else {
                locationStatus.setImageResource(R.drawable.ic_report_problem_black_24dp);
            }
            locationStatus.setVisibility(View.VISIBLE);
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
