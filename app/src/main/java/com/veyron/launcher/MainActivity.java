package com.veyron.launcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

/**
 * VEYRON Launcher foundation screen.
 *
 * Account authentication and the Android game runtime are intentionally not
 * represented as complete until their provider configuration and integrations
 * have been implemented and tested.
 */
public final class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button accountButton = findViewById(R.id.accountButton);
        TextView accountName = findViewById(R.id.accountName);
        Button playButton = findViewById(R.id.playButton);

        accountButton.setOnClickListener(view -> new AlertDialog.Builder(this)
                .setTitle("Microsoft account")
                .setMessage("Microsoft sign-in is the next integration step. It requires a registered Microsoft application ID and the OAuth authorization-code flow with PKCE. VEYRON will use Microsoft's sign-in page; it will never ask for your Microsoft password in a custom form.")
                .setPositiveButton("Got it", (dialog, which) -> dialog.dismiss())
                .show());

        playButton.setEnabled(false);
        accountName.setText("No account connected");
    }
}
