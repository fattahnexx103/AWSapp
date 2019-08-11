package apps.android.fattahnexx103.awsapp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.util.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;

public class S3Activity extends AppCompatActivity implements View.OnClickListener {

    TextView fileNameTextView;
    Button uploadButton, chooseFileButton, downloadButton;
    EditText downloadFileEditText;
    Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide(); //hide the action bar on top
        setContentView(R.layout.activity_s3);

        //bind the views to layout
        fileNameTextView = (TextView) findViewById(R.id.fileName_textView);
        uploadButton = (Button) findViewById(R.id.uploadFile_btn);
        chooseFileButton = (Button) findViewById(R.id.chooseFile_btn);
        downloadButton = (Button) findViewById(R.id.downloadFile_btn);
        downloadFileEditText = (EditText)findViewById(R.id.downloadfile_editText);

        fileNameTextView.setText("Choose File");
        uploadButton.setOnClickListener(this);
        downloadButton.setOnClickListener(this);
        chooseFileButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        if(view.getId() == R.id.uploadFile_btn){

            if(!fileNameTextView.getText().toString().equals("Choose File")){
                uploadWithTransferUtility(fileNameTextView.getText().toString()); //call the upload method
            }

        }

        if(view.getId() == R.id.chooseFile_btn){

            //open dialog to choose a file from device or computer
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*"); //any type of file
            startActivityForResult(intent.createChooser(intent, "Select File"), 10);
        }

        if(view.getId() == R.id.downloadFile_btn){
            if(downloadFileEditText.getText() != null) {
                downloadWithTransferUtility(downloadFileEditText.getText().toString());
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode){

            case 10: //this must match the req code
                if(resultCode == RESULT_OK){
                    fileUri = data.getData(); //get the uri

                    String filePath = getPath(this, fileUri); //use the uri to get the path
                    String formattedFilePath = filePath.substring(0,filePath.length() - 1); //take out the / in the end of the file name
                    fileNameTextView.setText(formattedFilePath); //set the edit text with file path

                }
                break;
        }
    }

    public static String getPath(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            System.out.println("getPath() uri: " + uri.toString());
            System.out.println("getPath() uri authority: " + uri.getAuthority());
            System.out.println("getPath() uri path: " + uri.getPath());

            // ExternalStorageProvider
            if ("com.android.externalstorage.documents".equals(uri.getAuthority())) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                System.out.println("getPath() docId: " + docId + ", split: " + split.length + ", type: " + type);

                // This is for checking Main Memory
                if ("primary".equalsIgnoreCase(type)) {
                    if (split.length > 1) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1] + "/";
                    } else {
                        return Environment.getExternalStorageDirectory() + "/";
                    }
                    // This is for checking SD Card
                } else {
                    return "storage" + "/" + docId.replace(":", "/");
                }

            }
        }
        return null;
    }

    public void uploadWithTransferUtility(String filepath){


        TransferUtility transferUtility = TransferUtility.builder()
                .context(this)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .s3Client(new AmazonS3Client(AWSMobileClient.getInstance().getCredentialsProvider()))
                .build();


        TransferObserver uploadObserver = transferUtility.upload("s3Folder/main.json",new File(fileNameTextView.getText().toString()));

        //add uploadObserver Listener for progress
        uploadObserver.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if(state == TransferState.COMPLETED){
                    Toast.makeText(getApplicationContext(), "FILE HAS BEEN UPLOADED", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                int percentDone = (int)percentDonef;

                Log.d("YourActivity", "ID:" + id + " bytesCurrent: " + bytesCurrent
                        + " bytesTotal: " + bytesTotal + " " + percentDone + "%");
            }

            @Override
            public void onError(int id, Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void downloadWithTransferUtility(String filePath){

        TransferUtility transferUtility = TransferUtility.builder()
                .context(this)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .s3Client(new AmazonS3Client(AWSMobileClient.getInstance().getCredentialsProvider()))
                .build();

        TransferObserver downloadObserver = transferUtility.download("s3Folder/main.json", new File(filePath));

        downloadObserver.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state == TransferState.COMPLETED){
                    Toast.makeText(getApplicationContext(), "FILE HAS BEEN DOWNLOADED", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

            }

            @Override
            public void onError(int id, Exception ex) {
                    ex.printStackTrace();
            }
        });
    }

//    public void Download(File DownloadingImagePath,String ImageBucket,String ImageName){
//        try{
////            AWSCredentials credentials = new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY);
////            AmazonS3 s3 = new AmazonS3Client(credentials);
////            java.security.Security.setProperty("networkaddress.cache.ttl" , "60");
////            s3.setRegion(Region.getRegion(Regions.AP_SOUTHEAST_1));
////            s3.setEndpoint("https://s3-ap-southeast-1.amazonaws.com/");
////            TransferUtility transferUtility = new TransferUtility(s3, mContext);
//            TransferObserver observer = transferUtility.download(ImageBucket, ImageName, DownloadingImagePath);
//            observer.setTransferListener(new TransferListener() {
//                @Override
//                public void onStateChanged(int id, TransferState state) {
//                    //Log.e("Amazon Stats",state.name());
//                }
//
//                @Override
//                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
//
//                }
//
//                @Override
//                public void onError(int id, Exception ex) {
//
//                }
//            });
//        } catch (Exception ignored){
//        }
//    }


}
