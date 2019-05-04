package com.example.android.lazzyreader;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

public class TexttoSound extends AppCompatActivity {
    Bitmap bitmap;
    Button playSound;
    Bitmap viewImageBitmap;
    public  String imagePath;
    TextView textView ;
    public TextToSpeech mtts;
    public static final String TESS_DATA = "/tessdata";
    ImageView viewImage;
    MyTessOCR mTessOCR;
    Button pick;
    public  String textOfImage="";
    Button getText;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("OpenCV", "OpenCV loaded successfully");
                    Mat imageMat=new Mat();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );

        setContentView( R.layout.activity_textto_sound );
        playSound = findViewById( R.id.play );
        prepareTessData();
        mtts = new TextToSpeech( getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i == TextToSpeech.SUCCESS){
                    int result= mtts.setLanguage( Locale.US );

                    if(result == TextToSpeech.LANG_MISSING_DATA || result== TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.i( "tts","language not supported" );

                    }
                    else{
                        playSound.setEnabled(true);

                    }

                }
                else{
                    Log.i( "tts","Initialization failed" );

                }
            }
        } );
        pick=(Button)findViewById(R.id.pick);
        textView = findViewById( R.id.text );
        mTessOCR = new MyTessOCR(this);
        getText=(Button)findViewById(R.id.convert);
        getText.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                convertToText();
            }
        } );
        viewImage=(ImageView)findViewById(R.id.pikedImage);
        pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();


            }
        });
        playSound.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speak();
            }
        } );

    }

    @Override
    protected void onDestroy() {

        if(mtts != null){
            mtts.stop();
            mtts.shutdown();
        }
        super.onDestroy();

    }

    private void speak(){
      //  String MESSAGE = "HELLO BABY";

        mtts.speak( textOfImage,TextToSpeech.QUEUE_FLUSH ,null,null);
    }
    private void selectImage() {
        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(TexttoSound.this);
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
                   // viewImage.setImageBitmap(bitmap);
                    String path = android.os.Environment
                            .getExternalStorageDirectory()
                            + File.separator
                            + "Phoenix" + File.separator + "default";
                    imagePath =path;
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
                imagePath =picturePath;
                c.close();
                Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));

                viewImageBitmap = thumbnail;
                //Log.w("path of image from gallery", picturePath+"");
               // viewImage.setImageBitmap(thumbnail);

            }
        }
    }
    private void convertToText(){
        int w = viewImageBitmap.getWidth();
        int h = viewImageBitmap.getHeight();

        // Setting pre rotate
        Matrix mtx = new Matrix();



        try {
            //DATA_PATH= Environment.getExternalStorageDirectory().toString() + "/tesseract_languages/";
            //  BitmapFactory.Options o = new BitmapFactory.Options();
            //   o.inDither = false;
            //   o.inSampleSize=4;

            ExifInterface exif = new ExifInterface(imagePath);
            int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            Log.v("Image :", "Orient: " + exifOrientation);

            int rotate = 0;
            switch (exifOrientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
            }

            Log.v("Pic Rotate:", "Rotation: " + rotate);

            if (rotate != 0) {

                // Getting width & height of the given image.

                mtx.postRotate( rotate );


            }






            // Setting rotation, according to our changes in OpenCV library,
            // because we only changed the rotation in the preview, not in the actual inputStream


            // Rotating Bitmap
            Bitmap viewImageGrey;
            Mat rgba = new Mat();
            Mat grey = new Mat();


            //  BitmapFactory.Options o = new BitmapFactory.Options();
            //   o.inDither = false;
            //   o.inSampleSize=4;
            int width = viewImageBitmap.getWidth();
            int height = viewImageBitmap.getHeight();
            viewImageGrey = Bitmap.createBitmap( width,height,Bitmap.Config.RGB_565 );
            Utils.bitmapToMat( viewImageBitmap,rgba );
            Imgproc.cvtColor( rgba,grey,Imgproc.COLOR_RGB2GRAY );
            Imgproc.erode(grey, grey, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,2)));
            Imgproc.dilate(grey, grey, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,2)));





            Utils.matToBitmap( grey ,viewImageGrey);

            bitmap = Bitmap.createBitmap(viewImageGrey, 0, 0, w, h, mtx, false);
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

            textOfImage = mTessOCR.getOCRResult(bitmap);
        }catch(Exception ex){
            Log.d("Exception1",ex.getMessage());
        }
        textView.setText(textOfImage  );

    }
    private void prepareTessData() {
        try {
            File dir = getExternalFilesDir(TESS_DATA);
            if (!dir.exists()) {
                if (!dir.mkdir()) {
                    Toast.makeText(getApplicationContext(), "The folder " + dir.getPath() + "was not created", Toast.LENGTH_SHORT).show();
                }
            }
            String fileList[] = getAssets().list("tessdata");

            for (String fileName : fileList) {
                String pathToDataFile = dir + "/" + fileName;
                if (!(new File(pathToDataFile)).exists()) {
                    InputStream in = getAssets().open("tessdata/" + fileName);
                    OutputStream out = new FileOutputStream(pathToDataFile);
                    byte[] buff = new byte[1024];
                    int len;
                    while ((len = in.read(buff)) > 0) {
                        out.write(buff, 0, len);
                    }
                    in.close();
                    out.close();
                }
            }
        } catch (Exception e) {
            Log.e("Exception", e.getMessage());
        }
    }

}
