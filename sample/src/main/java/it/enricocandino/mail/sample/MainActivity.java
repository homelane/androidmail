package it.enricocandino.mail.sample;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import it.enricocandino.androidmail.model.Attachment;
import it.enricocandino.androidmail.model.Mail;
import it.enricocandino.androidmail.MailSender;
import it.enricocandino.androidmail.model.Recipient;
import it.enricocandino.sample.R;

/**
 * Copyright (c) 2016 Enrico Candino
 * <p>
 * Distributed under the MIT License.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        verifyStoragePermissions(this);

        String filePath = Environment.getExternalStorageDirectory().getPath() +"/file.txt";
        String fileName = "file.txt";

        final String email = "spacecraft@homelane.com";
        final String password = "homelane14";
        MailSender mailSender = new MailSender(email, password);

        Mail.MailBuilder builder = new Mail.MailBuilder();
        Mail mail = builder
                .setSender(email)
                .addRecipient(new Recipient("vinay.sh@homelane.com"))
                .setText("Ciao")
                .setHtml("<h1 style=\"color:red;\">Ciao</h1>")
//                .addAttachment(new Attachment(filePath, fileName))
                .build();

        mailSender.sendMailAsync(mail, new MailSender.OnMailSentListener() {

            @Override
            public void onSuccess() {
                Log.d(TAG, "Mail Sent!");
            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, "Error sending mail!", error);
            }
        });
    }

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
}
