package com.easyfitness.utils;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.easyfitness.BuildConfig;
import com.easyfitness.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageUtil {

    public static final int REQUEST_TAKE_PHOTO = 1;
    public static final int REQUEST_PICK_GALERY_PHOTO = 2;
    public static final int REQUEST_DELETE_IMAGE = 3;
    private Fragment mF = null;
    private String mFilePath = null;
    private ImageView imgView = null;
    private ImageUtil.OnDeleteImageListener mDeleteImageListener;


    public ImageUtil() {
    }

    public ImageUtil(ImageView view) {
        imgView = view;
    }

    static public void setThumb(ImageView mImageView, String pPath) {
        try {
            if (pPath == null || pPath.isEmpty()) return;
            File f = new File(pPath);
            if (!f.exists() || f.isDirectory()) return;


            float targetW = 128;



            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(pPath, bmOptions);
            float photoW = bmOptions.outWidth;
            float photoH = bmOptions.outHeight;


            int scaleFactor = (int) (photoW / targetW);


            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            Bitmap bitmap = BitmapFactory.decodeFile(pPath, bmOptions);
            Bitmap orientedBitmap = ExifUtil.rotateBitmap(pPath, bitmap);
            mImageView.setImageBitmap(orientedBitmap);
            mImageView.setScaleType(ScaleType.CENTER_CROP);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static public String saveThumb(String pPath) {
        if (pPath == null || pPath.isEmpty()) return null;
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pPath, bmOptions);
        float photoW = bmOptions.outWidth;
        float photoH = bmOptions.outHeight;


        float scaleFactor = photoW / photoH;


        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = (int) scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap ThumbImage = null;

        Bitmap bitmap = BitmapFactory.decodeFile(pPath, bmOptions);
        Bitmap orientedBitmap = ExifUtil.rotateBitmap(pPath, bitmap);
        ThumbImage = ThumbnailUtils.extractThumbnail(orientedBitmap, 128, (int) (128 / scaleFactor));




        String nameOfOutputImage = pPath.substring(pPath.lastIndexOf('/') + 1, pPath.lastIndexOf('.'));
        String pathOfOutputFolder = pPath.substring(0, pPath.lastIndexOf('/'));
        File pathThumbFolder = new File(pathOfOutputFolder + "/.thumb/");
        if (!pathThumbFolder.exists()) {
            pathThumbFolder.mkdirs();
        }
        String pathOfThumbImage = pathOfOutputFolder + "/.thumb/" + nameOfOutputImage + "_TH.jpg";

        try {
            FileOutputStream out = new FileOutputStream(pathOfThumbImage);
            ThumbImage.compress(Bitmap.CompressFormat.JPEG, 80, out);
        } catch (Exception e) {
            Log.e("Image", e.getMessage(), e);
        }

        return pathOfThumbImage;
    }

    static public void setPic(ImageView mImageView, String pPath) {
        try {
            if (pPath == null) return;
            File f = new File(pPath);
            if (!f.exists() || f.isDirectory()) return;


            int targetW = mImageView.getWidth();
            if (targetW == 0) targetW = mImageView.getMeasuredWidth();
            int targetH = mImageView.getHeight();
            if (targetH == 0) targetH = mImageView.getMeasuredHeight();


            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(pPath, bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;


            int scaleFactor = photoW / targetW;


            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            Bitmap bitmap = BitmapFactory.decodeFile(pPath, bmOptions);
            Bitmap orientedBitmap = ExifUtil.rotateBitmap(pPath, bitmap);
            mImageView.setImageBitmap(orientedBitmap);


            mImageView.setAdjustViewBounds(true);
            mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ImageView getView() {
        return imgView;
    }

    public void setView(ImageView view) {
        imgView = view;
    }

    /**
     * Return path to the thumb of a picture if it is not already a thumb.
     * If necessary, it creates the thumb file.
     *
     * @param pPath Path to the full size picture
     * @return path to the thumb file
     */
    public String getThumbPath(String pPath) {
        if (pPath == null || pPath.isEmpty()) return null;

        String nameOfOutputImage = "";
        nameOfOutputImage = pPath.substring(pPath.lastIndexOf('/') + 1, pPath.lastIndexOf('.'));
        String pathOfOutputFolder = pPath.substring(0, pPath.lastIndexOf('/'));


        if (nameOfOutputImage.substring(nameOfOutputImage.length() - 3).equals("_TH")) {
            return pPath;

        } else {

            String pathOfThumbImage = "";
            pathOfThumbImage = pathOfOutputFolder + "/.thumb/" + nameOfOutputImage + "_TH.jpg";
            File f = new File(pathOfThumbImage);
            if (!f.exists())
                return saveThumb(pPath);
            else {
                return pathOfThumbImage;
            }
        }
    }

    public String getFilePath() {
        return mFilePath;
    }

    public void setOnDeleteImageListener(ImageUtil.OnDeleteImageListener listener) {
        mDeleteImageListener = listener;
    }

    public boolean CreatePhotoSourceDialog(Fragment pF) {

        mF = pF;

        String[] optionListArray = new String[3];
        optionListArray[0] = mF.getResources().getString(R.string.camera);
        optionListArray[1] = mF.getResources().getString(R.string.gallery);
        optionListArray[2] = "Remove Image";

        requestPermissionForWriting(pF);

        AlertDialog.Builder itemActionBuilder = new AlertDialog.Builder(mF.getActivity());
        itemActionBuilder.setTitle("").setItems(optionListArray, (dialog, which) -> {
            ListView lv = ((AlertDialog) dialog).getListView();

            switch (which) {

                case 1:
                    getGaleryPict(mF);
                    break;

                case 0:
                    CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(mF.getContext(), mF);
                    break;
                case 2:
                    if (mDeleteImageListener != null)
                        mDeleteImageListener.onDeleteImage(ImageUtil.this);
                    break;

                default:
            }
        });
        itemActionBuilder.show();

        return true;
    }

    private void dispatchTakePictureIntent(Fragment pF) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(mF.getActivity().getPackageManager()) != null) {

            File photoFile = null;
            try {
                photoFile = createImageFile(pF);
                mFilePath = photoFile.getAbsolutePath();
            } catch (IOException ex) {

                return;
            }

            Uri photoURI = FileProvider.getUriForFile(mF.getActivity(),
                BuildConfig.APPLICATION_ID + ".provider",
                photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            mF.startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
        }
    }

    private void getGaleryPict(Fragment pF) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        pF.startActivityForResult(photoPickerIntent, REQUEST_PICK_GALERY_PHOTO);
    }

    public void galleryAddPic(Fragment pF, String file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(file);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        pF.getActivity().sendBroadcast(mediaScanIntent);
    }

    private File createImageFile(Fragment pF) throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = null;

        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            return null;
        } else {

            storageDir = Environment.getExternalStoragePublicDirectory("/FastnFitness/DCIM/");
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }
        }

        File image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",         /* suffix */
            storageDir      /* directory */
        );



        return image;
    }

    private void requestPermissionForWriting(Fragment pF) {

        if (ContextCompat.checkSelfPermission(pF.getActivity(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {



            int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 102;
            ActivityCompat.requestPermissions(pF.getActivity(),
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);





        }
    }

    public File moveFile(File file, File dir) throws IOException {
        return copyFile(file, dir, "", true);
    }

    public File moveFile(File file, File dir, String newFileName) throws IOException {
        return copyFile(file, dir, newFileName, true);
    }

    public File copyFile(File file, File dir) throws IOException {
        return copyFile(file, dir, "", false);
    }

    public File copyFile(File file, File dir, String newFileName) throws IOException {
        return copyFile(file, dir, newFileName, false);
    }

    public File copyFile(File file, File dir, String newFileName, boolean moveFile) throws IOException {
        File newFile = null;
        if (newFileName.equals(""))
            newFile = new File(dir, file.getName());
        else
            newFile = new File(dir, newFileName);

        try (FileChannel outputChannel = new FileOutputStream(newFile).getChannel(); FileChannel inputChannel = new FileInputStream(file).getChannel()) {
            inputChannel.transferTo(0, inputChannel.size(), outputChannel);
            inputChannel.close();
            if (moveFile) file.delete();
        }

        return newFile;
    }

    public interface OnDeleteImageListener {
        void onDeleteImage(ImageUtil imgUtil);
    }
}
