package apps.android.fattahnexx103.awsapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button signOutBtn, s3AccessBtn, lambdaButton;
    TextView userNameTxtView, userEmailTxtView, userGivenNametextView, userPhoneTextView;
    CognitoUserPool userPool;
    CognitoUser currUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide(); //hide the action bar on top
        setContentView(R.layout.activity_main);

        //get the userPool through the configuration file
        userPool = new CognitoUserPool(this, AWSMobileClient.getInstance().getConfiguration());

        signOutBtn = (Button) findViewById(R.id.signout_btn);
        s3AccessBtn = (Button) findViewById(R.id.s3_storage_button);
        userNameTxtView = (TextView) findViewById(R.id.username_textView);
        userEmailTxtView = (TextView) findViewById(R.id.userEmail_textView);
        userGivenNametextView = (TextView) findViewById(R.id.userGivenName_textView);
        userPhoneTextView = (TextView) findViewById(R.id.userPhone_textView);
        lambdaButton = (Button) findViewById(R.id.lambda_button);

        currUser = userPool.getCurrentUser(); //gets the current user
        userNameTxtView.setText(currUser.getUserId());
        signOutBtn.setOnClickListener(this);
        s3AccessBtn.setOnClickListener(this);
        lambdaButton.setOnClickListener(this);


        currUser.getDetailsInBackground(new GetDetailsHandler() {
            @Override
            public void onSuccess(CognitoUserDetails cognitoUserDetails) {
                CognitoUserAttributes cognitoUserAttributes = cognitoUserDetails.getAttributes();

                //get the atributes
                String userEmail = cognitoUserAttributes.getAttributes().get("email");
                String userGivenName = cognitoUserAttributes.getAttributes().get("given_name");
                String phoneNumber = cognitoUserAttributes.getAttributes().get("phone_number");

                //set the attributes in the textViews
                userEmailTxtView.setText(userEmail);
                userGivenNametextView.setText(userGivenName);
                userPhoneTextView.setText(phoneNumber);
            }

            @Override
            public void onFailure(Exception exception) {
                Toast.makeText(MainActivity.this, "FAILED TO LOAD USER DETAILS ", Toast.LENGTH_LONG).show();
            }
        });


    }

    @Override
    public void onClick(View view) {

        if(view.getId() == R.id.signout_btn){
            currUser.signOut();
            IdentityManager.getDefaultIdentityManager().signOut();
            currUser = null;
            //go back to authenticatorActivity class for sign in again
            Intent intent = new Intent(MainActivity.this, AuthenticatorUIActivity.class);
            startActivity(intent);

        }

        if(view.getId() == R.id.s3_storage_button){
            if (currUser != null) {
                ///send out intent to go to S3 File Upload/Download Activity
                Intent intent = new Intent(MainActivity.this, S3Activity.class);
                startActivity(intent);
            }
        }

        if(view.getId() == R.id.lambda_button){
            if(currUser != null){
                Intent intent = new Intent(MainActivity.this, DynamoDbActivity.class);
                startActivity(intent);
            }
        }

    }
}
