package com.wmren.notemd.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.wmren.notemd.R;

import java.io.File;

import static android.app.Activity.RESULT_OK;


public class EditNoteFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "EditNoteFragment";
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String NOTE_TITLE = "param1";
    public static final String NOTE_CONTENT = "param2";

    private static final int TAKE_PICTURE = 1;
    private static final int CHOOSE_PHOTO = 2;

    private String noteTitle = null;
    private String noteContent = null;

    private String photoPath;
    private File tempFile;

    public EditNoteFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            noteTitle = getArguments().getString(NOTE_TITLE);
            noteContent = getArguments().getString(NOTE_CONTENT);
        }
    }

    private ImageButton buttonInsertTitle;
    private ImageButton buttonInsertQuote;
    private ImageButton buttonInsertPhoto;
    private ImageButton buttonInsertLink;
    private ImageButton buttonInsertCode;
    private ImageButton buttonInsertUlist;
    private ImageButton buttonInsertCheckbox;
    private ImageButton buttonInsertItalic;
    private ImageButton buttonInsertBold;
    private ImageButton buttonInsertStrikeThrough;

    private EditText noteTitleField;
    private EditText noteContentField;

    private View rootView;

    private EditFragmentInteraction mListener = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_note, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        rootView = getView();
        noteTitleField = rootView.findViewById(R.id.note_title);
        noteContentField = rootView.findViewById(R.id.note_content);
        buttonInsertTitle = rootView.findViewById(R.id.insert_title);
        buttonInsertQuote = rootView.findViewById(R.id.insert_quote);
        buttonInsertPhoto = rootView.findViewById(R.id.insert_photo);
        buttonInsertLink = rootView.findViewById(R.id.insert_link);
        buttonInsertCode = rootView.findViewById(R.id.insert_code);
        buttonInsertUlist = rootView.findViewById(R.id.insert_ulist);
        buttonInsertCheckbox = rootView.findViewById(R.id.insert_checkbox);
        buttonInsertItalic = rootView.findViewById(R.id.insert_italic);
        buttonInsertBold = rootView.findViewById(R.id.insert_bold);
        buttonInsertStrikeThrough = rootView.findViewById(R.id.insert_strikethrough);

        buttonInsertTitle.setOnClickListener(this);
        buttonInsertQuote.setOnClickListener(this);
        buttonInsertPhoto.setOnClickListener(this);
        buttonInsertLink.setOnClickListener(this);
        buttonInsertCode.setOnClickListener(this);
        buttonInsertUlist.setOnClickListener(this);
        buttonInsertCheckbox.setOnClickListener(this);
        buttonInsertItalic.setOnClickListener(this);
        buttonInsertBold.setOnClickListener(this);
        buttonInsertStrikeThrough.setOnClickListener(this);

        if (noteTitle != null) {
            noteTitleField.setText(noteTitle);
        }
        if (noteContent != null) {
            noteContentField.setText(noteContent);
        }

        noteTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mListener != null) {
                    noteTitle = noteTitleField.getText().toString();
                    mListener.onTitleChanged(noteTitle);
                }
            }
        });
        noteContentField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mListener != null) {
                    noteContent = noteContentField.getText().toString();
                    mListener.onContentChanged(noteContent);
                }
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (EditFragmentInteraction) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    
    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: EditNoteFragment");
    }

    @Override
    public void onClick(View v) {
        //底部工具栏的点击操作
        switch (v.getId()) {
            case R.id.insert_title: {
                insertToContentField("#");
                break;
            }
            case R.id.insert_quote: {
                insertToContentField(">");
                break;
            }
            case R.id.insert_photo: {
                photoPath = "";

                if ((ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                        (ContextCompat.checkSelfPermission(getActivity(),
                                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                        (ContextCompat.checkSelfPermission(getActivity(),
                                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA}, 1);
                }

                //TODO: call content provider
                final String[] items = {"拍照","本地图片"};
                AlertDialog.Builder listDialog = new AlertDialog.Builder(getContext());
                listDialog.setTitle("插入图片");
                listDialog.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // which 下标从0开始
                        switch (which) {
                            case 0: {
                                //照相
                                tempFile = null;
                                openCamera();
                                break;
                            }
                            case 1: {
                                //本地选取
                                openAlbum();
                                break;
                            }
                            default:
                                break;
                        }
                    }
                });
                listDialog.show();

                break;
            }
            case R.id.insert_link: {
                AlertDialog.Builder linkDialog =
                        new AlertDialog.Builder(getContext());
                final View dialogView = LayoutInflater.from(getContext())
                        .inflate(R.layout.dialog_link, null);
                linkDialog.setTitle("创建超链接");
                linkDialog.setView(dialogView);
                linkDialog.setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 获取EditView中的输入内容
                                EditText linkName = dialogView.findViewById(R.id.link_name);
                                EditText linkHref = dialogView.findViewById(R.id.link_href);
                                String lName = linkName.getText().toString();
                                String lHref = linkHref.getText().toString();
                                int index = insertToContentField("[" + lName + "](" + lHref + ")");
                            }
                        });
                linkDialog.setNegativeButton("取消", ((dialog, which) -> {}));
                linkDialog.show();
                break;
            }
            case R.id.insert_code: {
                int index = insertToContentField("```" + "\n\n" + "```");
                noteContentField.setSelection(index + 4);
                break;
            }
            case R.id.insert_ulist: {
                insertToContentField("-");
                break;
            }
            case R.id.insert_checkbox: {
                insertToContentField("- [x]");
                break;
            }
            case R.id.insert_italic: {
                int index = insertToContentField("__");
                noteContentField.setSelection(index + 1);
                break;
            }
            case R.id.insert_bold: {
                int index = insertToContentField("****");
                noteContentField.setSelection(index + 2);
                break;
            }
            case R.id.insert_strikethrough: {
                int index = insertToContentField("~~~~");
                noteContentField.setSelection(index + 2);
                break;
            }
            default:
                break;
        }
    }

    private void openCamera() {
        File appDir = new File(Environment.getExternalStorageDirectory().getPath(), "noteMD/photo");
        Log.d(TAG, "openCamera: " + appDir);
        if (!appDir.exists()) {
            appDir.mkdir();
        }

        tempFile = new File(appDir, System.currentTimeMillis() + ".jpg");
        //跳转到调用系统相机
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        //判断版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //如果在Android7.0以上,使用FileProvider获取Uri
            Uri contentUri = FileProvider.getUriForFile(getContext(), "com.wmren.notemd.fileprovider", tempFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
        } else {
            //否则使用Uri.fromFile(file)方法获取Uri
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
        }
        startActivityForResult(intent, TAKE_PICTURE);
    }

    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);
    }

    private int insertToContentField(String str) {
        int index = noteContentField.getSelectionStart();
        Editable editable = noteContentField.getText();
        editable.insert(index, str);
        return index;
    }

    public interface EditFragmentInteraction {
        void onTitleChanged(String title);
        void onContentChanged(String content);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PICTURE: {
                if (resultCode == RESULT_OK) {
                    photoPath = tempFile.getPath();
                    Log.d(TAG, "onActivityResult: " + photoPath);
                    if (!photoPath.equals("")) {
                        photoPath = "file://" + photoPath;
                        int index = insertToContentField("![](" + photoPath + ")");
                        noteContentField.setSelection(index + 2);
                    }
                }
                break;
            }
            case CHOOSE_PHOTO: {
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    handleUri(uri);

                    Log.d(TAG, "onActivityResult: " + photoPath);
                    if (!photoPath.equals("")) {
                        photoPath = "file://" + photoPath;
                        int index = insertToContentField("![](" + photoPath + ")");
                        noteContentField.setSelection(index + 2);
                    }
                }
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                Log.d(TAG, "onRequestPermissionsResult: " + grantResults.length);
                if (grantResults.length <= 0) {
                    Toast.makeText(getActivity(), "权限被拒绝", Toast.LENGTH_SHORT).show();
                } else {
                    if (!(grantResults[0] == PackageManager.PERMISSION_GRANTED) ||
                            !(grantResults[1] == PackageManager.PERMISSION_GRANTED) ||
                            !(grantResults[2] == PackageManager.PERMISSION_GRANTED)) {
                        Toast.makeText(getActivity(), "权限被拒绝", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            default:
                break;
        }
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        // 通过Uri和selection来获取真实的图片路径
        Cursor cursor = getActivity().getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void handleUri(Uri uri) {
        if (DocumentsContract.isDocumentUri(getContext(), uri)) {
            // 如果是document类型的Uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1]; // 解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                photoPath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                photoPath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果是content类型的Uri，则使用普通方式处理
            photoPath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // 如果是file类型的Uri，直接获取图片路径即可
            photoPath = uri.getPath();
        }
    }
}
