package com.example.cam;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.content.Context;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.poi.xwpf.converter.pdf.PdfConverter;
import org.apache.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;

import com.itextpdf.text.pdf.PdfWriter;

import java.io.Console;
import java.io.File;

import java.io.FileOutputStream;
import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;


//import android.graphics.Camera;

public class MainActivity extends AppCompatActivity {


    public final static String DEBUG_TAG = "MakePhotoActivity";
    private Camera camera;
    private int cameraId = 0;
    private List<String> IMAGES;
    private int pictureCount = 0;
    Button button ;
    ImageView imageView ;

    public MainActivity(){
        IMAGES = new ArrayList<>();
    }
    protected Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e){
        }
        return c;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button)findViewById(R.id.clickButton);
        imageView = (ImageView)findViewById(R.id.imageView);
        // do we have a camera?
        if (!getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            Toast.makeText(this, "No camera on this device", Toast.LENGTH_LONG)
                    .show();
        }
        else {
            cameraId = findFrontFacingCamera();
            if (cameraId < 0) {
                Toast.makeText(this, "No front facing camera found.",
                        Toast.LENGTH_LONG).show();
            } else {
                camera = this.getCameraInstance();
            }
        }

    }



    private int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                Log.d(DEBUG_TAG, "Camera found");
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }


    public void onBtnClick(View V) {
        Intent Intent3=new   Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(Intent3,7);

    }



    public void convertDoc (View V) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    10);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }

    }

    public void CreatePdf(View v) throws IOException, DocumentException {
        String directoryPath = android.os.Environment.getExternalStorageDirectory().toString();
        directoryPath = directoryPath + "/Cam";
        Image img = Image.getInstance(IMAGES.get(0));
        Document document = new Document(img);
        PdfWriter.getInstance(document, new FileOutputStream(directoryPath+"/Example.pdf"));
        document.open();
        for (String image : IMAGES) {
            img = Image.getInstance(image);
            document.setPageSize(img);
            document.newPage();
            img.setAbsolutePosition(0, 0);
            document.add(img);
        }
        document.close();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 7 && resultCode == RESULT_OK) {

            Bitmap bitmap = (Bitmap) data.getExtras().get("data");

            imageView.setImageBitmap(bitmap);
            ;
            String fName = "Cam"+Double.toString(Math.random() * ((10 - 1) + 1)) + 1 + ".jpg";
            createDirectoryAndSaveFile(bitmap,fName + "");
            String directoryPath = android.os.Environment.getExternalStorageDirectory().toString();
            directoryPath = directoryPath + "/Cam" + "/" + fName;
            IMAGES.add(pictureCount++ ,directoryPath);
        }

       else if(requestCode == 10){
            // Get the Uri of the selected file
            // Get the Uri of the selected file
            Uri uri = data.getData();
            String uriString = uri.toString();
            File myFile = new File(uriString);
            String path = myFile.getAbsolutePath();
            String displayName = null;
            path = this.getFilesDir().getAbsolutePath();
           // path = getRealPathFromURI(uri);
           // Log.d(MainActivity.DEBUG_TAG, "path : " + path);
            if (uriString.startsWith("content://")) {
                Cursor cursor = null;
                try {
                    cursor = this.getContentResolver().query(uri, null, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {

                        displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                        //path = getRealPathFromURI(uri);
                        Log.d(MainActivity.DEBUG_TAG, "path :" + path);
                        String f = path +displayName;
                        File file = new File(path +displayName );
                        convertDoc1(path, displayName);
                        Log.d(MainActivity.DEBUG_TAG, "fil :" + file);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    cursor.close();
                }
            } else if (uriString.startsWith("file://")) {
                displayName = myFile.getName();
            }


            Log.d(MainActivity.DEBUG_TAG, "displayName :" + displayName);

        }




    }



        public void convertDoc1 (String path ,String f) throws IOException {

        String inputFile = path + f;
        String outputFile = path+ "out.docx";
        FileInputStream in=new FileInputStream(inputFile);
        XWPFDocument document=new XWPFDocument(in);
        File outFile=new File(outputFile);
        OutputStream out=new FileOutputStream(outFile);
        PdfOptions options=null;
        PdfConverter.getInstance().convert(document,out,options);

    }

    private void createDirectoryAndSaveFile(Bitmap imageToSave, String fileName) {

        File direct = new File(Environment.getExternalStorageDirectory() + "/Cam");
        Log.d(MainActivity.DEBUG_TAG, "directory" + direct);
        if (!direct.exists()) {
            File wallpaperDirectory = new File("/sdcard/Cam/");
            wallpaperDirectory.mkdirs();
        }

        File file = new File("/sdcard/Cam/", fileName);
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            imageToSave.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();



        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
