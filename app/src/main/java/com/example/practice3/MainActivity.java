package com.example.practice3;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.usage.NetworkStats;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.logging.Log;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 672;
    private String imageFIlePath;
    private Uri photoUri;
    private Object permissionListener;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    //private final DatabaseReference root = FirebaseDatabase.getInstance().getReference("images");
    //private final StorageReference reference = FirebaseStorage.getReference();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //FirebaseStorage storage = FirebaseStorage.getInstance();  // [START storage_field_initialization]
        //mStorageRef = FirebaseStorage.getInstance().getReference();

        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                Toast.makeText(getApplicationContext(), "????????? ?????????", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(getApplicationContext(), "????????? ?????????", Toast.LENGTH_SHORT).show();
            }
        };

        // ?????? ??????
        TedPermission.create() //getApplicationContext()
                .setPermissionListener(permissionListener)
                .setRationaleMessage("????????? ????????? ???????????????.")
                .setDeniedMessage("?????????????????????.")
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .check();


        findViewById(R.id.btn_capture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CountDownTimer CDT = new CountDownTimer(10*1000, 4*1000) {//10(10*1000)????????? 4????????? ??????
                    @Override
                    public void onTick(long l) {  //??????????????? ??????
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if(getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) { //if(intent.resolveActivity(getPackageManageer())!=null){
                            // Create the File where the photo should go
                            File photoFile = null;
                            try {
                                photoFile = createImageFile();
                            } catch (IOException e) {

                            }
                            if (photoFile != null) {
                                //Uri photoUri = Uri.fromFile(new File("path/to/images"));
                                //StorageReference riversRef = storageRef.child("images");
                                photoUri = FileProvider.getUriForFile(getApplicationContext(), getPackageName(), photoFile);
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                                Toast.makeText(getApplicationContext(), "?????????????????????", Toast.LENGTH_SHORT).show();
                            }
                        }

                        //DB??? ????????????
                        //mAuth = FirebaseAuth.getInstance();
                        final String cu = mAuth.getUid();
                        //????????? storage??? ???????????? url ?????????
                        String filename = cu + "_" + System.currentTimeMillis();
                        //StorageReference storageRef = storage.getReferenceFromUrl("https://console.firebase.google.com/project/python-connection-1692b/storage/python-connection-1692b.appspot.com/files").child("images/" + filename);
                        StorageReference storageRef = storage.getReferenceFromUrl("https://console.firebase.google.com/project/python-connection-1692b/storage/python-connection-1692b.appspot.com/files").child("images/" + filename);
                        Uri file = null;
                        file = photoUri;
                        UploadTask uploadTask = storageRef.putFile(file);
                    }

                    @Override
                    public void onFinish() {  // ???????????? ????????? ??????
                        Toast.makeText(getApplicationContext(), "???????????? ?????????????????????", Toast.LENGTH_SHORT).show();
                    }
                };

                CDT.start(); //CDT ??????
                CDT.cancel(); //CDT ??????

            } //onClick
        });

    } //oncreate


    private File createImageFile() throws IOException { //????????? ?????? ??????
        /***
        // create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "TEST_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = new File( //File.createTempFile ????????? createNewFile ?????? ????????? ??????????????? ????????????
                storageDir,
                imageFileName + ".jpg"   //?????? ??????:imgFIleName,".jpg",storageDir
        );
        if(!image.exists()) image.createNewFile();
        imageFIlePath = image.getAbsolutePath();
        return image; ***/
        String imageFileName = System.currentTimeMillis() + ".jpg";
        File imageFile= null;
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/Pictures", "ireh");

        imageFile = new File(storageDir,imageFileName);
        String mCurrentPhotoPath = imageFile.getAbsolutePath();
        return imageFile;

         /***
        String imgFileName = System.currentTimeMillis() + ".jpg";
        File imageFile= null;
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/Pictures", "ireh");

        if(!storageDir.exists()){
            //????????? ?????????
            Log.v("??????","storageDir ?????? x " + storageDir.toString());
            storageDir.mkdirs();
        }
        Log.v("??????","storageDir ????????? " + storageDir.toString());
        imageFile = new File(storageDir,imgFileName);
        mCurrentPhotoPath = imageFile.getAbsolutePath();

        return imageFile;
          ***/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bitmap bitmap = BitmapFactory.decodeFile(imageFIlePath);
            ExifInterface exif = null;

            try {
                exif = new ExifInterface(imageFIlePath);
            } catch (IOException e) {
                e.printStackTrace();
            }

            int exifOrientation;
            int exifDegree;

            if (exif != null) {
                exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                exifDegree = exifOrientationToDegress(exifOrientation);
            } else {
                exifDegree = 0;
            }

            ((ImageView) findViewById(R.id.iv_result)).setImageBitmap(rotate(bitmap, exifDegree));

        }

    }

    private Bitmap rotate(Bitmap bitmap, float degree) { //int exifDegree
        Matrix matrix = new Matrix();
        matrix.postRotate(degree); //degree ?????????
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private int exifOrientationToDegress(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }
}