package com.example.android.lazzyreader;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.InstallCallbackInterface;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ImageEnhancement extends AppCompatActivity {
    Button makeNormal, makeGray, makeCanny, makeDilate, makeErode,pickIt;

    Bitmap viewImageBitmap;
    Bitmap bitmapStack;
    public  String textOfImage="";

    Mat imageStack;
    ImageView viewImage;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback( this ) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i( "OpenCV", "OpenCV loaded successfully" );
                    imageStack = new Mat();
                }
                break;
                default: {
                    super.onManagerConnected( status );
                }
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_image_enhancement );

        makeNormal = findViewById( R.id.normal_mode );
        makeGray = findViewById( R.id.gray_mode );

        makeCanny = findViewById( R.id.canny_mode );
        makeDilate = findViewById( R.id.dilate_mode );
        makeErode = findViewById( R.id.erose_mode );
        viewImage = findViewById( R.id.cam_view1 );
        pickIt = findViewById( R.id.pickIt );
        pickIt.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();

            }
        } );


        makeNormal.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setNormal();

            }
        } );
        makeGray.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleGray();
            }
        } );
        makeDilate.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleDilate();
            }
        } );
        makeErode.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleErode();
            }
        } );
        makeCanny.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleCanny();
            }
        } );
        /*
        makeRead.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        } );
        */
    }
    private void selectImage() {
        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(ImageEnhancement.this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo"))
                {
                    Intent intent = new Intent( MediaStore.ACTION_IMAGE_CAPTURE);
                    File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    startActivityForResult(intent, 1);
                }
                else if (options[item].equals("Choose from Gallery"))
                {
                    Intent intent = new   Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 2);
                }
                else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected( LoaderCallbackInterface.SUCCESS);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                File f = new File( Environment.getExternalStorageDirectory().toString());
                for (File temp : f.listFiles()) {
                    if (temp.getName().equals("temp.jpg")) {
                        f = temp;
                        break;
                    }
                }
                try {
                    Bitmap bitmap;
                    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
                    bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(),
                            bitmapOptions);
                    viewImageBitmap = bitmap;
                    viewImage.setImageBitmap(bitmap);
                    String path = android.os.Environment
                            .getExternalStorageDirectory()
                            + File.separator
                            + "Phoenix" + File.separator + "default";
                    f.delete();
                    OutputStream outFile = null;
                    File file = new File(path, String.valueOf(System.currentTimeMillis()) + ".jpg");
                    try {
                        outFile = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outFile);
                        outFile.flush();
                        outFile.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (requestCode == 2) {
                Uri selectedImage = data.getData();
                String[] filePath = { MediaStore.Images.Media.DATA };
                Cursor c = getContentResolver().query(selectedImage,filePath, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                String picturePath = c.getString(columnIndex);
                c.close();
                Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));
                viewImageBitmap = thumbnail;
                //Log.w("path of image from gallery", picturePath+"");
                viewImage.setImageBitmap(thumbnail);

            }
        }
    }
    void setNormal(){
        viewImage.setImageBitmap(viewImageBitmap);
        Utils.bitmapToMat( viewImageBitmap,imageStack );


    }
    void toggleGray(){
        Bitmap viewImageGrey;
        Mat rgba = new Mat();


        //  BitmapFactory.Options o = new BitmapFactory.Options();
        //   o.inDither = false;
        //   o.inSampleSize=4;
        int width = viewImageBitmap.getWidth();
        int height = viewImageBitmap.getHeight();
        viewImageGrey = Bitmap.createBitmap( width,height,Bitmap.Config.RGB_565 );
        Utils.bitmapToMat( viewImageBitmap,rgba );
        Imgproc.cvtColor( rgba,imageStack,Imgproc.COLOR_RGB2GRAY );
        Utils.matToBitmap( imageStack ,viewImageGrey);
        viewImage.setImageBitmap( viewImageGrey );
    }
    void toggleDilate(){
        makeGray();

        Bitmap viewImageDilate;
        int width = viewImageBitmap.getWidth();
        int height = viewImageBitmap.getHeight();
        viewImageDilate = Bitmap.createBitmap( width,height,Bitmap.Config.RGB_565 );

        Imgproc.dilate(imageStack, imageStack, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,2)));
        Utils.matToBitmap( imageStack ,viewImageDilate);
        viewImage.setImageBitmap(viewImageDilate);

    }
    void toggleErode(){
        makeGray();

        Bitmap viewImageErode;
        int width = viewImageBitmap.getWidth();
        int height = viewImageBitmap.getHeight();
        viewImageErode = Bitmap.createBitmap( width,height,Bitmap.Config.RGB_565 );
        Imgproc.erode(imageStack, imageStack, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,2)));
        Utils.matToBitmap( imageStack ,viewImageErode);
        viewImage.setImageBitmap(viewImageErode);
    }
    void toggleCanny(){
        makeGray();

        Bitmap viewImageCann;
        int width = viewImageBitmap.getWidth();
        int height = viewImageBitmap.getHeight();
        viewImageCann = Bitmap.createBitmap( width,height,Bitmap.Config.RGB_565 );
        Imgproc.Canny(imageStack, imageStack, 70, 100);
        Utils.matToBitmap( imageStack ,viewImageCann);
        viewImage.setImageBitmap(viewImageCann);

    }
    void makeGray(){


        Mat rgba = new Mat();
        Utils.bitmapToMat( viewImageBitmap,rgba );
       if (rgba == imageStack ){
           toggleGray();

       }

    }
    /*
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }
*/
}