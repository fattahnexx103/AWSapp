package apps.android.fattahnexx103.awsapp;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.amazonaws.mobile.auth.ui.AuthUIConfiguration;
import com.amazonaws.mobile.auth.ui.SignInUI;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;

public class AuthenticatorUIActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenticator_ui);


        //initiate the AWS Mobile Client
        AWSMobileClient.getInstance().initialize(this, new AWSStartupHandler() {
            @Override
            public void onComplete(final AWSStartupResult awsStartupResult) {

                //configure the authenticator client with customization options
                AuthUIConfiguration config =
                        new AuthUIConfiguration.Builder()
                                .userPools(true)
                                .logoResId(R.drawable.company_logo)
                                .backgroundColor(Color.BLACK)
                                .isBackgroundColorFullScreen(true)
                                .fontFamily("sans-serif-light")
                                .canCancel(true)
                                .build();

                //Call the SignInUi and use the login function calling it with the above config
                SignInUI signinUI = (SignInUI) AWSMobileClient.getInstance().getClient(AuthenticatorUIActivity.this, SignInUI.class);
                signinUI.login(AuthenticatorUIActivity.this, MainActivity.class).authUIConfiguration(config).execute();
            }
        }).execute();
    }

}
