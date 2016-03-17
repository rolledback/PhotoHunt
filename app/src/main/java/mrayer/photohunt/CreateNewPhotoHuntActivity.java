package mrayer.photohunt;

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

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;
import java.util.UUID;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CreateNewPhotoHuntActivity extends AppCompatActivity {
    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;

    private String mCurrentPhotoPath;

    private ImageAdapter imageAdapter;

    private Uri mostRecentTakenPhoto;

    private ViewPager viewPager;
    private Button addFromGalleryButton;
    private Button takePhotoButton;
    private Button uploadButton;
    private Button cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_photo_hunt);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        imageAdapter = new ImageAdapter(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Create New Photo Hunt");

        addFromGalleryButton = (Button) findViewById(R.id.add_from_gallery_button);
        addFromGalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE);
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

                    public void done(ParseException e) {
                        if (e != null) {
                            Log.d(Constants.CreateNewPhotoHunt_Tag, e.toString());
                        } else {
                            Log.d(Constants.CreateNewPhotoHunt_Tag, "Photo hunt saved.");
                        }
                    }
                });

                // create Parse File for each photo
                for(String imagePath: imageAdapter.getGalImages()) {
                    File file = new File(imagePath);

                    int height = (int) getResources().getDimension(R.dimen.create_new_photo_hunt_image_size);
                    int width = getResources().getDisplayMetrics().widthPixels;

                    Bitmap bitmap = ImageUtils.decodeFile(file, width, height);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] byteArray= stream.toByteArray();

                    final ParseFile photo = new ParseFile(file.getName(), byteArray);
                    photo.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.d(Constants.CreateNewPhotoHunt_Tag, e.toString());
                            } else {
                                Log.d(Constants.CreateNewPhotoHunt_Tag, "Photo file uploaded.");
                                ParseObject photoObject = new ParseObject("Photo");
                                photoObject.add("album_id", albumId);
                                photoObject.add("photo", photo);
                                photoObject.saveInBackground();
                                Log.d(Constants.CreateNewPhotoHunt_Tag, "Photo object is being uploaded.");
                            }
                        }
                    });
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

        viewPager = (ViewPager) findViewById(R.id.view_pager);
//        imageAdapter = new ImageAdapter(this);
        viewPager.setAdapter(imageAdapter);
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
        outState.putStringArrayList("galImages", imageAdapter.getGalImages());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        ArrayList<String> list = savedInstanceState.getStringArrayList("galImages");
        imageAdapter.setGalImages(list);
        imageAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
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

        else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
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
                Log.e("CREATE NEW PHOTO HUNT", "gal images = " + imageAdapter.getGalImages().toString());
            }
            Log.e("CREATE NEW PHOTO HUNT", "AFTER THE CHECK FOR REPEAT IMAGES");
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
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        mostRecentTakenPhoto);
                savePreferences();
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
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
        Log.e("NEW PHOTO", "CURRENT PHOTO PATH = " + mCurrentPhotoPath);
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Log.e("NEW PHOTO", "IN GALLERY ADD PIC, CURRENT PHOTO PATH = " + mCurrentPhotoPath);
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

}
