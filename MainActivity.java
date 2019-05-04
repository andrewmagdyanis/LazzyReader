package com.example.android.lazzyreader;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.lazzyreader.R;
import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    Button playSound;





    Button imageEnhancement;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_main);


        imageEnhancement=(Button)findViewById(R.id.getEnhancementIntent);


        playSound=(Button)findViewById(R.id.getSoundIntent);
        imageEnhancement.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent imageEnhancementIntent = new Intent( MainActivity.this,ImageEnhancement.class);
                startActivity(imageEnhancementIntent);

            }
        } );

        playSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imagetosound = new Intent( MainActivity.this,TexttoSound.class);
                startActivity(imagetosound);



            }
        });


    }


}