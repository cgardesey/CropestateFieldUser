package com.cropestate.fielduser.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.cropestate.fielduser.constants.keyConst;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import com.kbeanie.multipicker.api.entity.ChosenImage;
import com.kbeanie.multipicker.api.entity.ChosenVideo;
import com.noelchew.multipickerwrapper.library.MultiPickerWrapper;
import com.noelchew.multipickerwrapper.library.ui.MultiPickerWrapperAppCompatActivity;
import com.cropestate.fielduser.R;
import com.cropestate.fielduser.adapter.EnrolmentActivityAdapter;
import com.cropestate.fielduser.adapter.FileAdapter;
import com.cropestate.fielduser.other.InitApplication;
import com.cropestate.fielduser.other.MyHttpEntity;
import com.cropestate.fielduser.realm.RealmSubmittedAssignment;
import com.cropestate.fielduser.util.PixelUtil;
import com.cropestate.fielduser.util.RealmUtility;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.util.FileUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.http.util.TextUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;
import io.realm.Realm;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.cropestate.fielduser.activity.GetAuthActivity.APITOKEN;
import static com.cropestate.fielduser.activity.GetAuthActivity.MYUSERID;
import static com.cropestate.fielduser.constants.keyConst.API_URL;
import static com.cropestate.fielduser.constants.Const.fileSize;
import static com.cropestate.fielduser.constants.Const.isExternalStorageWritable;
import static com.cropestate.fielduser.constants.Const.myVolleyError;


public class SubmitAssignmentActivity extends MultiPickerWrapperAppCompatActivity implements EasyPermissions.PermissionCallbacks {

    public static final int RC_PHOTO_PICKER_PERM = 123;
    public static final int RC_FILE_PICKER_PERM = 321;
    private static final int CUSTOM_REQUEST_CODE = 532;
    private static final String TAG = SubmitAssignmentActivity.class.getSimpleName();
    public static Button submit;
    public static EditText submissiontitle;
    public static String title, assignmentid;
    RecyclerView filerecyclerview;
    ArrayList<File> files = new ArrayList<>();
    LinearLayout statuslayout, progresslayout;
    TextView assigmenttitle, statustext;
    RelativeLayout cam, gal, doc;
    ProgressBar progressbar;
    FileAdapter fileAdapter;
    private ArrayList<Uri> docUris = new ArrayList<>();
    Toast toast;
    MultiPickerWrapper.PickerUtilListener multiPickerWrapperListener = new MultiPickerWrapper.PickerUtilListener() {
        @Override
        public void onPermissionDenied() {
            // do something here
        }

        @Override
        public void onImagesChosen(List<ChosenImage> list) {
            ArrayList<String> filePaths = new ArrayList<>();
            for (ChosenImage chosenImage : list) {
                filePaths.add(chosenImage.getOriginalPath());
                /*File file = new File(chosenImage.getOriginalPath());
                if (!files.contains(file)) {
                    files.add(file);
                    fileAdapter.notifyDataSetChanged();

                    submit.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getApplicationContext(), "File already added!", Toast.LENGTH_LONG).show();
                }*/
            }
            createPdfFromImages(filePaths);
            String sourcePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SchoolDirectStudent/Assignments/Temp/temp.pdf";
            String destinationPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SchoolDirectStudent/" + getIntent().getStringExtra("COURSEPATH").replace(" >> ", "/") + "/Assignments/Submitted/" + submittedassignmentid + "0.pdf";
            File destinationfile = new File(destinationPath);
            File parentFile = destinationfile.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            try {
                FileUtils.copyFile(sourcePath, destinationPath);
                files.add(destinationfile);
                fileAdapter.notifyDataSetChanged();

                submit.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onVideosChosen(List<ChosenVideo> list) {

        }

        @Override
        public void onError(String s) {
            Toast.makeText(getApplicationContext(), getString(R.string.error_choosing_image), Toast.LENGTH_LONG).show();
            Log.d(TAG, s);
        }
    };
    private int MAX_ATTACHMENT_COUNT = 1;
    private ArrayList<String> docPaths = new ArrayList<>();

    private String path;

    static String submittedassignmentid;

    @Override
    protected MultiPickerWrapper.PickerUtilListener getMultiPickerWrapperListener() {
        return multiPickerWrapperListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_assignment);

        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        assignmentid = intent.getStringExtra("assignmentid");
        submittedassignmentid = UUID.randomUUID().toString();

        statustext = findViewById(R.id.statustext);
        progressbar = findViewById(R.id.progressbar);
        statuslayout = findViewById(R.id.statuslayout);
        progresslayout = findViewById(R.id.progresslayout);
        assigmenttitle = findViewById(R.id.assigmenttitle);
        assigmenttitle.setText(title);
        submissiontitle = findViewById(R.id.submissiontitle);
        submit = findViewById(R.id.submit);

        doc = findViewById(R.id.upcomingdoc);
        gal = findViewById(R.id.gal);
        cam = findViewById(R.id.cam);

        filerecyclerview = findViewById(R.id.filerecyclerview);

        fileAdapter = new FileAdapter(files);
        filerecyclerview.setLayoutManager(new LinearLayoutManager(this));
        filerecyclerview.setHasFixedSize(false);
        filerecyclerview.setNestedScrollingEnabled(false);
        filerecyclerview.setItemAnimator(new DefaultItemAnimator());
        filerecyclerview.setAdapter(fileAdapter);


        submit.setOnClickListener(view -> {
            if (files.size() > 0) {

                if (!TextUtils.isEmpty(submissiontitle.getText())) {
                    if (isExternalStorageWritable()) {
                        File file_dir = new File(Environment.getExternalStorageDirectory() + "/SchoolDirectStudent/" + getIntent().getStringExtra("COURSEPATH").replace(" >> ", "/") + "/Assignments", "Submitted");
                        if (!file_dir.exists()) {
                            file_dir.mkdirs();
                        }
                        for (int i = 0; i < files.size(); i++) {
                            File file = files.get(i);
                            String ext = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("."));
                            String destinationPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SchoolDirectStudent/" + getIntent().getStringExtra("COURSEPATH").replace(" >> ", "/") + "/Assignments/Submitted/" + submittedassignmentid + i + ext;
                            try {
                                FileUtils.copyFile(file.getAbsolutePath(), destinationPath);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        new UploadAsyncTask(getApplicationContext()).execute();
                    }
                } else {
                    submissiontitle.setError(getString(R.string.error_field_required));
                    submissiontitle.requestFocus();
                }

            } else {
                Toast.makeText(SubmitAssignmentActivity.this,
                        getString(R.string.pls_attach_file), Toast.LENGTH_LONG).show();
            }
        });

        doc.setOnClickListener(v -> {
            if (files.size() == MAX_ATTACHMENT_COUNT) {
                Toast.makeText(getApplicationContext(), getString(R.string.cannot_select_more_than) + " " + MAX_ATTACHMENT_COUNT + " " + getString(R.string.item), Toast.LENGTH_LONG).show();
            } else {
                pickDocClicked();
            }
        });

        gal.setOnClickListener(v -> {
            if (files.size() == MAX_ATTACHMENT_COUNT) {
                Toast.makeText(getApplicationContext(), getString(R.string.cannot_select_more_than) + " " + MAX_ATTACHMENT_COUNT + " " + getString(R.string.item), Toast.LENGTH_LONG).show();
            } else {
                multiPickerWrapper.getPermissionAndPickMultipleImage();
            }
        });

        cam.setOnClickListener(v -> {
            if (files.size() == MAX_ATTACHMENT_COUNT) {
                Toast.makeText(getApplicationContext(), getString(R.string.cannot_select_more_than) + " " + MAX_ATTACHMENT_COUNT + " " + getString(R.string.item), Toast.LENGTH_LONG).show();
            } else {
                multiPickerWrapper.getPermissionAndTakePicture();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case FilePickerConst.REQUEST_CODE_DOC:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        if (data != null) {
                            docPaths = new ArrayList<>();
                            docPaths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS));

                            for (final String selectedDocPath : docPaths) {
                                File file = new File(selectedDocPath);

                                if (file.length() > 10000000L) {
                                    Toast.makeText(this, getString(R.string.file_size_of) + " " + fileSize(file.length()) + " " + getString(R.string.larger_than_limit), Toast.LENGTH_SHORT).show();
                                } else if (!files.contains(file)) {
                                    files.add(file);
                                    fileAdapter.notifyDataSetChanged();

                                    submit.setVisibility(View.VISIBLE);
                                } else {
                                    Toast.makeText(this, getString(R.string.file_already_added), Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                }

                break;

            default:
                break;

        }
    }

    @AfterPermissionGranted(RC_FILE_PICKER_PERM)
    public void pickDocClicked() {
        if (EasyPermissions.hasPermissions(this, FilePickerConst.PERMISSIONS_FILE_PICKER)) {
            docUris.clear();
            String[] pdf_file_type = {"pdf"};
            String[] doc_file_type = {"doc", "docx"};
            String[] ppt_file_type = {"ppt", "pptx"};
            String[] xls_file_type = {"xls", "xlsx"};
            String[] txt_file_type = {"txt"};
            String[] zip_file_type = {"zip","rar"};
            FilePickerBuilder.getInstance()
                    .setMaxCount(MAX_ATTACHMENT_COUNT)
                    .setSelectedFiles(docUris)
                    .enableDocSupport(false)
                    .addFileSupport("PDF", pdf_file_type, R.mipmap.ic_pdf)
                    .addFileSupport("DOC", doc_file_type, R.mipmap.ic_doc)
                    .addFileSupport("PPT", ppt_file_type, R.mipmap.ic_ppt)
                    .addFileSupport("XLS", xls_file_type, R.mipmap.ic_xls)
                    .addFileSupport("TXT", txt_file_type, R.mipmap.ic_txt)
//                    .addFileSupport("ZIP", zip_file_type, R.drawable.zip)

                    .setActivityTheme(R.style.FilePickerTheme)
                    .pickFile(this);
        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_doc_picker),
                    RC_FILE_PICKER_PERM, FilePickerConst.PERMISSIONS_FILE_PICKER);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }

    private UCrop.Options imgOptions() {
        UCrop.Options options = new UCrop.Options();
        options.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));
        options.setToolbarColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        options.setCropFrameColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));
        options.setCropFrameStrokeWidth(PixelUtil.dpToPx(getApplicationContext(), 4));
        options.setCropGridColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        options.setCropGridStrokeWidth(PixelUtil.dpToPx(getApplicationContext(), 2));
        options.setActiveWidgetColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        options.setToolbarTitle(getString(R.string.crop_image));

        // set rounded cropping guide
        options.setCircleDimmedLayer(true);
        return options;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    public void fetchSubmittedAssignments() {
        String URL = null;
        try {
            URL = API_URL + "submitted-assignments";

//            loadinggif.setVisibility(View.VISIBLE);
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                    Request.Method.GET,
                    URL,
                    null,
                    response -> {
                        Realm.init(getApplicationContext());
                        Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {
                            realm.createOrUpdateAllFromJson(RealmSubmittedAssignment.class, response);
                            Toast.makeText(getApplicationContext(), getString(R.string.assignment_successfully_submitted), Toast.LENGTH_LONG).show();
                        });
                    },
                    error -> myVolleyError(getApplicationContext(), error)
            ) {
                /** Passing some request headers* */
                @Override
                public Map getHeaders() throws AuthFailureError {
                    HashMap headers = new HashMap();
                    headers.put("accept", "application/json");
                    headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(APITOKEN, ""));
                    return headers;
                }
            };
            jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            InitApplication.getInstance().addToRequestQueue(jsonArrayRequest);

        } catch (Exception e) {
            Log.e("My error", e.toString());
        }
    }

    private class UploadAsyncTask extends AsyncTask<Void, Integer, String> {

        HttpClient httpClient = new DefaultHttpClient();

        private Context context;
        private Exception exception;

        // private ProgressDialog progressDialog;

        private UploadAsyncTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(Void... voids) {

            HttpResponse httpResponse = null;
            HttpEntity httpEntity = null;
            String responseString = null;
            String URL = keyConst.API_URL + "submitted-assignments";
            try {
                HttpPost httpPost = new HttpPost(URL);
                MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();

                // Add the file to be uploaded
                for (int i = 0; i < files.size(); i++) {
                    multipartEntityBuilder.addPart("file" + i, new FileBody(files.get(i)));
                }
                multipartEntityBuilder.addTextBody("submittedassignmentid", submittedassignmentid);
                multipartEntityBuilder.addTextBody("title", submissiontitle.getText().toString().trim());
                multipartEntityBuilder.addTextBody("assignmentid", assignmentid);
                multipartEntityBuilder.addTextBody("studentid", PreferenceManager.getDefaultSharedPreferences(context).getString(MYUSERID, ""));
                // Progress listener - updates task's progress
                MyHttpEntity.ProgressListener progressListener =
                        progress -> publishProgress((int) progress);

                // POST
                httpPost.setEntity(new MyHttpEntity(multipartEntityBuilder.build(),
                        progressListener));
                httpPost.setHeader("accept", "application/json");
                httpPost.setHeader("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(APITOKEN, ""));


                httpResponse = httpClient.execute(httpPost);
                httpEntity = httpResponse.getEntity();

                int statusCode = httpResponse.getStatusLine().getStatusCode();

                if (statusCode == 200 || statusCode == 201)  {
                    // Server response
                    String confirmation_token = EntityUtils.toString(httpEntity);
                    new EnrolmentActivityAdapter.notifyInstructor(getApplicationContext(),"assignment", confirmation_token, getIntent().getStringExtra("COURSEPATH"), submissiontitle.getText().toString().trim()).execute();
                    responseString = null;
                } else {
                    responseString = "Error occurred!";
                }
            } catch (UnsupportedEncodingException | ClientProtocolException e) {
                responseString = e.getMessage();
                e.printStackTrace();
                Log.e("UPLOAD", e.getMessage());
                this.exception = e;
            } catch (IOException e) {
                responseString = e.getMessage();
                Log.e("gardes", e.toString());
//                e.printStackTrace();
            }

            return responseString;
        }

        @Override
        protected void onPreExecute() {
            progresslayout.setVisibility(View.VISIBLE);
            progressbar.animate();
        }

        @Override
        protected void onPostExecute(String result) {
            progresslayout.setVisibility(View.GONE);
            if (result == null) {
                files.clear();
                fileAdapter.notifyDataSetChanged();
                submissiontitle.getText().clear();
                fetchSubmittedAssignments();
            }
            else if (result.contains("connect")){
                Toast.makeText(context, context.getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
            }
            else {
                if (toast != null) {
                    toast.cancel();
                }
                toast = Toast.makeText(context, result, Toast.LENGTH_SHORT);
                toast.show();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // Update process
            progressbar.setProgress(progress[0]);
            statustext.setText(progress[0].toString() + "%  complete");
        }
    }

    private void createPdfFromImages(ArrayList<String> stringArrayList) {
        String directoryPath = Environment.getExternalStorageDirectory() + "/SchoolDirectStudent/Assignments/Temp";
        File directory = new File(directoryPath);

        if (!directory.exists()) {
            directory.mkdirs();
        }
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, new FileOutputStream(directoryPath + "/temp.pdf")); //  Change pdf's name.
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        document.open();

        com.itextpdf.text.Image image = null;
        for (int i = 0; i < stringArrayList.size(); i++) {
            try {
                image = com.itextpdf.text.Image.getInstance(stringArrayList.get(i));
            } catch (BadElementException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            float scaler = ((document.getPageSize().getWidth() - document.leftMargin()
                    - document.rightMargin() - 0) / image.getWidth()) * 100; // 0 means you have no indentation. If you have any, change it.
            image.scalePercent(scaler);
            image.setAlignment(com.itextpdf.text.Image.ALIGN_CENTER | com.itextpdf.text.Image.ALIGN_TOP);

            try {
                document.add(image);
            } catch (DocumentException e) {
                e.printStackTrace();
            }
        }
        document.close();
    }
}
