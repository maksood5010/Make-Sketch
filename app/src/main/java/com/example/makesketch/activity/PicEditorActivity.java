package com.example.makesketch.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.makesketch.Blur;
import com.example.makesketch.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.IntBuffer;

import ja.burhanrashid52.photoeditor.OnSaveBitmap;
import ja.burhanrashid52.photoeditor.PhotoEditor;
import ja.burhanrashid52.photoeditor.PhotoEditorView;
import ja.burhanrashid52.photoeditor.shape.ShapeBuilder;
import ja.burhanrashid52.photoeditor.shape.ShapeType;

public class PicEditorActivity extends AppCompatActivity {
    PhotoEditorView ivFilter;
    private PhotoEditor mPhotoEditor;
    BottomSheetDialog dialog;
    Button btBrush, btEraser, btUndo, btRedo;
    Integer opacity = 255, size = 100;
    private static final int MAX_VALUE_SIZE = 100;
    private static final int MAX_VALUE = 255;
    private static final int MIN_VALUE = 0;
    private Uri path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_editor);
        Uri data = getIntent().getData();
        ivFilter = findViewById(R.id.photoEditorView);

        mPhotoEditor = new PhotoEditor.Builder(this, ivFilter)
                .setPinchTextScalable(true)
                .setClipSourceImage(true)
                .build();

        btBrush = findViewById(R.id.btBrush);
        btEraser = findViewById(R.id.btEraser);
        btUndo = findViewById(R.id.btUndo);
        btRedo = findViewById(R.id.btRedo);
        btUndo.setOnClickListener(v -> mPhotoEditor.undo());
        btRedo.setOnClickListener(v -> mPhotoEditor.redo());
        dialog = new BottomSheetDialog(this, R.style.Bottom);
        View view = getLayoutInflater().inflate(R.layout.dialog_brush, null);
        dialog.setContentView(view);
        Button btDone = view.findViewById(R.id.btDone);
        SeekBar sb_opacity = view.findViewById(R.id.sb_opacity);
        SeekBar sb_size = view.findViewById(R.id.sb_size);
        TextView tvSize = view.findViewById(R.id.tvSize);
        TextView tvOpacity = view.findViewById(R.id.tvOpacity);
        sb_size.setMax(MAX_VALUE);
        sb_opacity.setMax(MAX_VALUE);
        sb_opacity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvOpacity.setText(progress + "");
                PicEditorActivity.this.opacity = progress;

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        sb_size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvSize.setText(progress + "");
                PicEditorActivity.this.size = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        sb_opacity.setProgress(MAX_VALUE);
        sb_size.setProgress(MAX_VALUE);
        btDone.setOnClickListener(v -> {

            dialog.dismiss();
        });
        btBrush.setOnClickListener(v -> {
            dialog.show();
            dialog.setOnDismissListener(dialog1 -> {
                ShapeBuilder shapeBuilder = new ShapeBuilder();
                shapeBuilder.withShapeType(ShapeType.BRUSH);
                shapeBuilder.withShapeSize(size.floatValue());
                shapeBuilder.withShapeOpacity(opacity);
                shapeBuilder.withShapeColor(Color.WHITE);
                mPhotoEditor.setShape(shapeBuilder);
                mPhotoEditor.setBrushDrawingMode(true);
            });
        });
        btEraser.setOnClickListener(v -> {
            dialog.show();
            dialog.setOnDismissListener(dialog1 -> {
                ShapeBuilder shapeBuilder = new ShapeBuilder();
                shapeBuilder.withShapeType(ShapeType.BRUSH);
                shapeBuilder.withShapeSize(size.floatValue());
                shapeBuilder.withShapeOpacity(opacity);
                shapeBuilder.withShapeColor(Color.WHITE);
                mPhotoEditor.setShape(shapeBuilder);
                mPhotoEditor.brushEraser();
            });
        });
        if (data != null) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(data);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                Bitmap changetosketch = Changetosketch(bitmap);
                ivFilter.getSource().setImageBitmap(changetosketch);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public Bitmap Changetosketch(Bitmap bmp) {
        Bitmap Copy, Invert, Result;
        Copy = bmp;
        Copy = toGrayscale(Copy);
        Invert = createInvertedBitmap(Copy);
        Invert = Blur.blur(getApplicationContext(), Invert);
        Result = ColorDodgeBlend(Invert, Copy);
        Result = toGrayscale(Result);
        return Result;
    }

    public Bitmap toGrayscale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    public Bitmap createInvertedBitmap(Bitmap src) {
        ColorMatrix colorMatrix_Inverted =
                new ColorMatrix(new float[]{
                        -1, 0, 0, 0, 255,
                        0, -1, 0, 0, 255,
                        0, 0, -1, 0, 255,
                        0, 0, 0, 1, 0});

        ColorFilter ColorFilter_Sepia = new ColorMatrixColorFilter(
                colorMatrix_Inverted);

        Bitmap bitmap = Bitmap.createBitmap(src.getWidth(), src.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();

        paint.setColorFilter(ColorFilter_Sepia);
        canvas.drawBitmap(src, 0, 0, paint);

        return bitmap;
    }

    public Bitmap ColorDodgeBlend(Bitmap source, Bitmap layer) {
        Bitmap base = source.copy(Bitmap.Config.ARGB_8888, true);
        Bitmap blend = layer.copy(Bitmap.Config.ARGB_8888, false);

        IntBuffer buffBase = IntBuffer.allocate(base.getWidth() * base.getHeight());
        base.copyPixelsToBuffer(buffBase);
        buffBase.rewind();

        IntBuffer buffBlend = IntBuffer.allocate(blend.getWidth() * blend.getHeight());
        blend.copyPixelsToBuffer(buffBlend);
        buffBlend.rewind();

        IntBuffer buffOut = IntBuffer.allocate(base.getWidth() * base.getHeight());
        buffOut.rewind();

        while (buffOut.position() < buffOut.limit()) {

            int filterInt = buffBlend.get();
            int srcInt = buffBase.get();

            int redValueFilter = Color.red(filterInt);
            int greenValueFilter = Color.green(filterInt);
            int blueValueFilter = Color.blue(filterInt);

            int redValueSrc = Color.red(srcInt);
            int greenValueSrc = Color.green(srcInt);
            int blueValueSrc = Color.blue(srcInt);

            int redValueFinal = colordodge(redValueFilter, redValueSrc);
            int greenValueFinal = colordodge(greenValueFilter, greenValueSrc);
            int blueValueFinal = colordodge(blueValueFilter, blueValueSrc);


            int pixel = Color.argb(255, redValueFinal, greenValueFinal, blueValueFinal);


            buffOut.put(pixel);
        }

        buffOut.rewind();

        base.copyPixelsFromBuffer(buffOut);
        blend.recycle();

        return base;
    }

    private int colordodge(int in1, int in2) {
        float image = (float) in2;
        float mask = (float) in1;
        return ((int) ((image == 255) ? image : Math.min(255, (((long) mask << 8) / (255 - image)))));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_save:
                saveImage();
                return true;
            case R.id.menu_share:
                if (path == null) {
                    saveImage();
                }
                showAlert();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAlert() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.share_image)
                .setMessage(R.string.are_you)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("image/jpeg");
                    try {
                        share.setData(path);
                        share.putExtra(Intent.EXTRA_TEXT,"playstore  link");
                        startActivity(Intent.createChooser(share, getString(R.string.share_image)));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_menu_share)
                .show();
    }

    private void saveImage() {

        mPhotoEditor.saveAsBitmap(new OnSaveBitmap() {
            @Override
            public void onBitmapReady(Bitmap saveBitmap) {
                path = insertImage(saveBitmap, System.currentTimeMillis() + "_sketch.jpg", null);
                if (path!=null) {
                    Toast.makeText(PicEditorActivity.this, "Image Saved to " + path, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(PicEditorActivity.this, "Failed to Save try again", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }

    public Uri insertImage(Bitmap source, String title, String desc) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, title);
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, title);
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, desc);
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        contentValues.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());

        Uri uri = null;
        String stringUrl = null;
        ContentResolver cr = getContentResolver();
        try {
            uri = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            if (source != null) {
                OutputStream outputStream = cr.openOutputStream(uri);
                try {
                    source.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                } finally {
                    outputStream.close();
                }
            } else {
                cr.delete(uri, null, null);
                uri = null;
            }
        } catch (IOException e) {
            if (uri != null) {
                cr.delete(uri, null, null);
                uri = null;
            }
        }
        return uri;
    }

}