package com.wyp.avatarstudio;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;


public class AvatarStudio extends DialogFragment implements View.OnClickListener {
    private static final int     CAMAER_REQUEST_STORAGE_WRITE_ACCESS_PERMISSION  = 110;
    private static final int     GALLERY_REQUEST_STORAGE_WRITE_ACCESS_PERMISSION = 111;
    private static final int     REQUEST_CAMERA                                  = 100;
    private static final int     REQUEST_GALLERY                                 = 101;
    private static final int     REQUEST_CROP                                    = 102;
    public static final  String  EXTRA_NEEDCROP                                  = "needcrop";
    public static final  String  EXTRA_DIMENABLED                                = "dimEnabled";
    public static final  String  EXTRA_CROP_ASPECTX                              = "crop_aspectX";
    public static final  String  EXTRA_CROP_ASPECTY                              = "crop_aspectY";
    public static final  String  EXTRA_CROP_OUTPUTX                              = "crop_outputX";
    public static final  String  EXTRA_CROP_OUTPUTY                              = "crop_outputY";
    public static final  String  EXTRA_TEXT_COLOR                                = "text_color";
    public static final  String  EXTRA_TEXT_CAMERA                               = "text_camera";
    public static final  String  EXTRA_TEXT_GALLERY                              = "text_gallery";
    public static final  String  EXTRA_TEXT_CANCEL                               = "text_cancel";
    public static final  int     DEFAULT_CROP_ASPECTX                            = 1;
    public static final  int     DEFAULT_CROP_ASPECTY                            = 1;
    public static final  int     DEFAULT_CROP_OUTPUTX                            = 400;
    public static final  int     DEFAULT_CROP_OUTPUTY                            = 400;
    public static final  int     DEFAULT_TEXT_COLOR                              = Color.BLACK;
    public static final  boolean DEFAULT_NEEDCROP                                = true;
    public static final  boolean DEFAULT_DIMENABLED                              = true;
    private File    mTmpFile;
    private File    mCropImageFile;
    private boolean mNeedCrop;
    private boolean mDimEnabled;
    private int     aspectX;
    private int     aspectY;
    private int     outputX;
    private int     outputY;
    private int     textColor;
    private String  cameraText;
    private String  galleryText;
    private String  cancelText;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNeedCrop = getArguments().getBoolean(EXTRA_NEEDCROP, DEFAULT_NEEDCROP);
        aspectX = getArguments().getInt(EXTRA_CROP_ASPECTX, DEFAULT_CROP_ASPECTX);
        aspectY = getArguments().getInt(EXTRA_CROP_ASPECTY, DEFAULT_CROP_ASPECTY);
        outputX = getArguments().getInt(EXTRA_CROP_OUTPUTX, DEFAULT_CROP_OUTPUTX);
        outputY = getArguments().getInt(EXTRA_CROP_OUTPUTY, DEFAULT_CROP_OUTPUTY);
        textColor = getArguments().getInt(EXTRA_TEXT_COLOR, DEFAULT_TEXT_COLOR);
        mDimEnabled = getArguments().getBoolean(EXTRA_DIMENABLED, DEFAULT_DIMENABLED);
        cameraText = getArguments().getString(EXTRA_TEXT_CAMERA, getString(R.string.camera));
        galleryText = getArguments().getString(EXTRA_TEXT_GALLERY, getString(R.string.gallery));
        cancelText = getArguments().getString(EXTRA_TEXT_CANCEL, getString(R.string.cancel));
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity(),
                mDimEnabled ? R.style.BottomDialogDim : R.style.BottomDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_layout);
        dialog.setCanceledOnTouchOutside(true);
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.gravity = Gravity.BOTTOM;
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(lp);
        }
        initView(dialog);
        return dialog;
    }

    private void initView(Dialog dialog) {
        TextView camera = (TextView) dialog.findViewById(R.id.camera);
        camera.setText(cameraText);
        camera.setTextColor(textColor);
        camera.setOnClickListener(this);
        TextView gallery = (TextView) dialog.findViewById(R.id.gallery);
        gallery.setText(galleryText);
        gallery.setTextColor(textColor);
        gallery.setOnClickListener(this);
        TextView cancel = (TextView) dialog.findViewById(R.id.cancel);
        cancel.setText(cancelText);
        cancel.setTextColor(textColor);
        cancel.setOnClickListener(this);
    }

    private void gallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    private void camera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            try {
                mTmpFile = FileUtils.createTmpFile(getActivity());
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (mTmpFile != null && mTmpFile.exists()) {
                Uri pictureUri;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    ContentValues contentValues = new ContentValues(1);
                    contentValues.put(MediaStore.Images.Media.DATA, mTmpFile.getAbsolutePath());
                    pictureUri = getActivity().getContentResolver()
                            .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                } else {
                    pictureUri = Uri.fromFile(mTmpFile);
                }
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        pictureUri);
                startActivityForResult(cameraIntent, REQUEST_CAMERA);
            }
        } else {
            Toast.makeText(getActivity(), R.string.msg_no_camera, Toast.LENGTH_SHORT).show();
        }
    }

    private void requestPermission(final String permission, String rationale, final int requestCode) {
        if (shouldShowRequestPermissionRationale(permission)) {
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.mis_permission_dialog_title)
                    .setMessage(rationale)
                    .setPositiveButton(R.string.mis_permission_dialog_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissions(new String[]{permission}, requestCode);
                        }
                    })
                    .setNegativeButton(R.string.mis_permission_dialog_cancel, null)
                    .show();
        } else {
            requestPermissions(new String[]{permission}, requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == CAMAER_REQUEST_STORAGE_WRITE_ACCESS_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                camera();
            }
        } else if (requestCode == GALLERY_REQUEST_STORAGE_WRITE_ACCESS_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                gallery();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void crop(String imagePath) {
        try {
            mCropImageFile = FileUtils.createTmpFile(getActivity());
            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setDataAndType(getImageContentUri(new File(imagePath)), "image/*");
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", aspectX);
            intent.putExtra("aspectY", aspectY);
            intent.putExtra("outputX", outputX);
            intent.putExtra("outputY", outputY);
            intent.putExtra("scale", true);
            intent.putExtra("return-data", false);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mCropImageFile));
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            intent.putExtra("noFaceDetection", true);
            startActivityForResult(intent, REQUEST_CROP);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Uri getImageContentUri(File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = getActivity().getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return getActivity().getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (getActivity() == null || !isAdded())
            return;
        switch (requestCode) {
            case REQUEST_CAMERA:
                getActivity();
                if (resultCode == Activity.RESULT_OK) {
                    if (mNeedCrop) {
                        crop(mTmpFile.getAbsolutePath());
                    } else if (mCallBack != null) {
                        mCallBack.callback(mTmpFile.getAbsolutePath());
                        dismiss();
                    }
                } else {
                    delete(mTmpFile);
                    dismiss();
                }
                break;
            case REQUEST_GALLERY:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    String imagePath = handleImage(data);
                    if (mNeedCrop) {
                        crop(imagePath);
                    } else if (mCallBack != null) {
                        mCallBack.callback(imagePath);
                        dismiss();
                    }
                } else {
                    dismiss();
                }
                break;
            case REQUEST_CROP:
                if (resultCode == Activity.RESULT_OK) {
                    if (mCallBack != null) {
                        mCallBack.callback(mCropImageFile.getAbsolutePath());
                    }
                } else {
                    delete(mCropImageFile);
                }
                dismiss();
                break;
        }
    }

    private void delete(File file) {
        while (file != null && file.exists()) {
            boolean success = file.delete();
            if (success) {
                file = null;
            }
        }
    }

    private String handleImage(Intent data) {
        Uri uri = data.getData();
        String imagePath = null;
        if (Build.VERSION.SDK_INT >= 19) {
            if (DocumentsContract.isDocumentUri(getActivity(), uri)) {
                String docId = DocumentsContract.getDocumentId(uri);
                if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                    String id = docId.split(":")[1];
                    String selection = MediaStore.Images.Media._ID + "=" + id;
                    imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
                } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                    Uri contentUri = ContentUris.withAppendedId(Uri.parse("" +
                            "content://downloads/public_downloads"), Long.valueOf(docId));
                    imagePath = getImagePath(contentUri, null);
                }
            } else if ("content".equals(uri.getScheme())) {
                imagePath = getImagePath(uri, null);
            }
        } else {
            imagePath = getImagePath(uri, null);
        }
        return imagePath;
    }

    private String getImagePath(Uri uri, String seletion) {
        String path = null;
        Cursor cursor = getActivity().getContentResolver().query(uri, null, seletion, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.camera) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        getString(R.string.mis_permission_rationale_write_storage),
                        CAMAER_REQUEST_STORAGE_WRITE_ACCESS_PERMISSION);
            } else {
                camera();
            }
        } else if (id == R.id.gallery) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        getString(R.string.mis_permission_rationale_write_storage),
                        GALLERY_REQUEST_STORAGE_WRITE_ACCESS_PERMISSION);
            } else {
                gallery();
            }
        } else if (id == R.id.cancel) {
            dismiss();
        }
    }

    private CallBack mCallBack;

    public interface CallBack {
        void callback(String uri);
    }

    public static class Builder {
        AvatarStudio mAvatarStudio;
        private FragmentActivity mActivity;

        public Builder(FragmentActivity activity) {
            mActivity = activity;
            mAvatarStudio = new AvatarStudio();
        }

        public Builder needCrop(boolean crop) {
            mAvatarStudio.mNeedCrop = crop;
            return this;
        }

        public Builder setTextColor(int color) {
            mAvatarStudio.textColor = color;
            return this;
        }

        public Builder setAspect(int aspectX, int aspectY) {
            mAvatarStudio.aspectX = aspectX;
            mAvatarStudio.aspectY = aspectY;
            return this;
        }

        public Builder setOutput(int outPutX, int outPutY) {
            mAvatarStudio.outputX = outPutX;
            mAvatarStudio.outputY = outPutY;
            return this;
        }

        public Builder dimEnabled(boolean enable) {
            mAvatarStudio.mDimEnabled = enable;
            return this;
        }

        public Builder setText(String camera, String gallery, String cancel) {
            mAvatarStudio.cameraText = camera;
            mAvatarStudio.galleryText = gallery;
            mAvatarStudio.cancelText = cancel;
            return this;
        }

        public AvatarStudio show(CallBack callBack) {
            mAvatarStudio.mCallBack = callBack;
            Bundle bundle = new Bundle();
            bundle.putBoolean(EXTRA_NEEDCROP, mAvatarStudio.mNeedCrop);
            bundle.putBoolean(EXTRA_DIMENABLED, mAvatarStudio.mDimEnabled);
            bundle.putInt(EXTRA_CROP_ASPECTX, mAvatarStudio.aspectX);
            bundle.putInt(EXTRA_CROP_ASPECTY, mAvatarStudio.aspectY);
            bundle.putInt(EXTRA_CROP_OUTPUTX, mAvatarStudio.outputX);
            bundle.putInt(EXTRA_CROP_OUTPUTY, mAvatarStudio.outputY);
            bundle.putInt(EXTRA_TEXT_COLOR, mAvatarStudio.textColor);
            bundle.putString(EXTRA_TEXT_CAMERA, mAvatarStudio.cameraText);
            bundle.putString(EXTRA_TEXT_GALLERY, mAvatarStudio.galleryText);
            bundle.putString(EXTRA_TEXT_CANCEL, mAvatarStudio.cancelText);
            mAvatarStudio.setArguments(bundle);
            FragmentManager fm = mActivity.getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            Fragment fragment = fm.findFragmentByTag("avatarStudio");
            if (fragment != null) {
                ft.remove(fragment);
            }
            ft.addToBackStack(null);
            mAvatarStudio.show(ft, "avatarStudio");
            return mAvatarStudio;
        }

    }


}
