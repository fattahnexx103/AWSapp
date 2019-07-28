package apps.android.fattahnexx103.awsapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.auth.core.SignInStateChangeListener;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;

import org.w3c.dom.Text;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    Button signOutBtn;
    TextView userNameTxtView, userEmailTxtView, userGivenNametextView, userPhoneTextView;
    CognitoUserPool userPool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get the userPool through the configuration file
        userPool = new CognitoUserPool(this, AWSMobileClient.getInstance().getConfiguration());

        signOutBtn = (Button) findViewById(R.id.signout_btn);
        userNameTxtView = (TextView) findViewById(R.id.username_textView);
        userEmailTxtView = (TextView) findViewById(R.id.userEmail_textView);
        userGivenNametextView = (TextView) findViewById(R.id.userGivenName_textView);
        userPhoneTextView = (TextView) findViewById(R.id.userPhone_textView);

        final CognitoUser currUser = userPool.getCurrentUser(); //gets the current user
        userNameTxtView.setText(currUser.getUserId());

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



        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.signout_btn){
                    Toast.makeText(MainActivity.this, " ", Toast.LENGTH_SHORT).show();
                    currUser.signOut();

                }
        }}
        );


    }
}
