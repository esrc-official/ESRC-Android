package com.esrc.android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.esrc.sdk.android.ESRC;
import com.esrc.sdk.android.ESRCException;
import com.esrc.sdk.android.ESRCFragment;
import com.esrc.sdk.android.ESRCLicense;
import com.esrc.sdk.android.ESRCType;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String TAG = "MainActivity";
    private static final String APP_ID = "";  // Application ID.

    // Permission
    private static final int PERMISSIONS_REQUEST_CODE = 1000;
    private static final String[] PERMISSIONS = {INTERNET, CAMERA, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE};

    // Property
    private ESRCType.Property mProperty = new ESRCType.Property(
            false,  // Whether visualize result or not. It is only valid If you bind the ESRC Fragment (i.e., Step 2).
            true,  // Whether analyze measurement environment or not.
            true,  // Whether detect face or not.
            true,  // Whether detect facial landmark or not. If enableFace is false, it is also automatically set to false.
            true,  // Whether analyze facial action unit or not. If enableFace or enableFacialLandmark is false, it is also automatically set to false.
            true,  // Whether recognize basic facial expression or not. If enableFace is false, it is also automatically set to false.
            true,  // Whether recognize valence facial expression or not. If enableFace is false, it is also automatically set to false.
            true,  // Whether estimate remote hr or not. If enableFace is false, it is also automatically set to false.
            true,  // Whether analyze HRV or not. If enableFace or enableRemoteHR is false, it is also automatically set to false.
            true,  // Whether recognize engagement or not. If enableRemoteHR and enableHRV are false, it is also automatically set to false.
            true);  // Whether print information or not.

    // Layout variables for FaceBox
    private TextView mFaceBoxText;
    private ImageView mFaceBoxImage;
    private GradientDrawable mFaceBoxDrawable;

    // Layout variables for Basic Facial Expression
    private int[] mBasicFacialExpImageDrawables;
    private View mBasicFacialExpValContainer;
    private ImageView mBasicFacialExpImage;
    private TextView mBasicFacialExpValText;

    // Layout variables for Valence Facial Expression
    private int[] mValenceFacialExpImageDrawables;
    private View mValenceFacialExpValContainer;
    private ImageView mValenceFacialExpImage;
    private TextView mValenceFacialExpValText;

    // Layout variables for Attention
    private View mAttentionValContainer;
    private TextView mAttentionValText;

    // Layout variables for Heart Rate
    private View mHRValContainer;
    private TextView mHRValText;
    private ProgressBar mHRSpinnerDummy;
    private ProgressBar mHRSpinner;

    // Layout variables for Heart Rate Variability
    private View mHRVValContainer;
    private TextView mHRVValText;
    private ProgressBar mHRVSpinnerDummy;
    private ProgressBar mHRVSpinner;

    // Layout variables for Engagement
    private View mEngagementValContainer;
    private TextView mEngagementValText;
    private ProgressBar mEngagementSpinnerDummy;
    private ProgressBar mEngagementSpinner;

    // Dialog variables
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind ESRCFragment (optional)
        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, ESRCFragment.newInstance())
                    .commit();
        }

        // Set always display on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Request Permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasPermissions(PERMISSIONS)) {
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        }

        // Initialize layout
        initLayout();

        // Initialize ESRC
        ESRC.init(APP_ID, this, new ESRCLicense.ESRCLicenseHandler() {
            @Override
            public void onValidatedLicense() {
                // Start
                start();
            }

            @Override
            public void onInvalidatedLicense() {
                Toast.makeText(getApplicationContext(), "Invalid license", Toast.LENGTH_SHORT).show();
            }
        });

        // Timer
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),
                        "If you want to use the ESRC SDK, please visit the homepage: https://www.esrc.co.kr",
                        Toast.LENGTH_LONG).show();
                MainActivity.this.finish();
            }
        }, 120000);
    }

    @Override
    protected void onDestroy() {
        // Stop
        stop();

        super.onDestroy();
    }

    /**
     * Initialize layout.
     */
    private void initLayout() {
        // Initialize layout for FaceBox
        mFaceBoxText = findViewById(R.id.facebox_text);
        mFaceBoxImage = findViewById(R.id.facebox_image);
        mFaceBoxDrawable = (GradientDrawable) mFaceBoxImage.getBackground();

        // Initialize layout for Basic Facial Expression
        mBasicFacialExpImageDrawables = new int[] {
                R.drawable.ic_facial_exp_anger, R.drawable.ic_facial_exp_disgust, R.drawable.ic_facial_exp_fear,
                R.drawable.ic_facial_exp_happy, R.drawable.ic_facial_exp_sad, R.drawable.ic_facial_exp_surprise,
                R.drawable.ic_facial_exp_neutral,
        };
        mBasicFacialExpValContainer = findViewById(R.id.basic_facial_expression_val_container);
        mBasicFacialExpImage = findViewById(R.id.basic_facial_expression_image);
        mBasicFacialExpValText = findViewById(R.id.basic_facial_expression_val_text);

        // Initialize layout for Valence Facial Expression
        mValenceFacialExpImageDrawables = new int[] {
                R.drawable.ic_facial_exp_positive, R.drawable.ic_facial_exp_negative, R.drawable.ic_facial_exp_neutral,
        };
        mValenceFacialExpValContainer = findViewById(R.id.valence_facial_expression_val_container);
        mValenceFacialExpImage = findViewById(R.id.valence_facial_expression_image);
        mValenceFacialExpValText = findViewById(R.id.valence_facial_expression_val_text);

        // Initialize layout for Attention
        mAttentionValContainer = findViewById(R.id.attention_val_container);
        mAttentionValText = findViewById(R.id.attention_val_text);

        // Initialize layout for Heart Rate
        mHRValContainer = findViewById(R.id.hr_val_container);
        mHRValText = findViewById(R.id.hr_val_text);
        mHRSpinnerDummy = findViewById(R.id.hr_spinner_dummy);
        mHRSpinner = findViewById(R.id.hr_spinner);

        // Initialize layout for Heart Rate Variability
        mHRVValContainer = findViewById(R.id.hrv_val_container);
        mHRVValText = findViewById(R.id.hrv_val_text);
        mHRVSpinnerDummy = findViewById(R.id.hrv_spinner_dummy);
        mHRVSpinner = findViewById(R.id.hrv_spinner);

        // Initialize layout for Engagement
        mEngagementValContainer = findViewById(R.id.engagement_val_container);
        mEngagementValText = findViewById(R.id.engagement_val_text);
        mEngagementSpinnerDummy = findViewById(R.id.engagement_spinner_dummy);
        mEngagementSpinner = findViewById(R.id.engagement_spinner);
    }

    /**
     * Starts ESRC process.
     */
    private void start() {
        // Show dummy spinners
        mHRSpinnerDummy.setVisibility(View.VISIBLE);
        mHRVSpinnerDummy.setVisibility(View.VISIBLE);
        mEngagementSpinnerDummy.setVisibility(View.VISIBLE);
        mHRSpinner.setVisibility(View.GONE);
        mHRVSpinner.setVisibility(View.GONE);
        mEngagementSpinner.setVisibility(View.GONE);

        // Start ESRC
        ESRC.start(mProperty, new ESRC.ESRCHandler() {
            @Override
            public void onAnalyzedMeasureEnv(ESRCType.MeasureEnv measureEnv, ESRCException e) {
                if (e == null) {
                    Log.d(TAG, "onAnalyzedMeasureEnv: " + measureEnv.toString());
                } else {
                    e.printStackTrace();
                }
            }

            @Override
            public void onDetectedFace(ESRCType.Face face, ESRCException e) {
                if (e == null) {
                    Log.d(TAG, "onDetectedFace: " + face.toString());

                    // If face is detected
                    if (face.getIsDetect()) {
                        // Show FaceBox
                        int color = getResources().getColor(R.color.primary_color);
                        mFaceBoxText.setTextColor(color);
                        mFaceBoxDrawable.setStroke(8, color);

                        // Show Attention containers
                        mAttentionValContainer.setVisibility(View.VISIBLE);
                    } else {
                        // Hide FaceBox
                        int color = getResources().getColor(R.color.gray);
                        mFaceBoxText.setTextColor(color);
                        mFaceBoxDrawable.setStroke(4, color);

                        // Hide containers
                        mBasicFacialExpValContainer.setVisibility(View.GONE);
                        mValenceFacialExpValContainer.setVisibility(View.GONE);
                        mAttentionValContainer.setVisibility(View.GONE);
                        mHRValContainer.setVisibility(View.GONE);
                        mHRVValContainer.setVisibility(View.GONE);
                        mEngagementValContainer.setVisibility(View.GONE);
                    }
                } else {
                    e.printStackTrace();
                }
            }

            @Override
            public void onDetectedFacialLandmark(ESRCType.FacialLandmark facialLandmark, ESRCException e) {
                if (e == null) {
                    Log.d(TAG, "onDetectedFacialLandmark: " + facialLandmark.toString());
                } else {
                    e.printStackTrace();
                }
            }

            @Override
            public void onAnalyzedFacialActionUnit(ESRCType.FacialActionUnit facialActionUnit, ESRCException e) {
                if (e == null) {
                    Log.d(TAG, "onAnalyzedFacialActionUnit: " + facialActionUnit.toString());
                } else {
                    e.printStackTrace();
                }
            }

            @Override
            public void onRecognizedBasicFacialExpression(ESRCType.BasicFacialExpression basicFacialExpression, ESRCException e) {
                if (e == null) {
                    Log.d(TAG, "onRecognizedBasicFacialExpression: " + basicFacialExpression.toString());

                    // Set Basic Facial Expression values
                    ESRCType.BasicFacialExpression.Emotion emotion = basicFacialExpression.getEmotion();
                    if (emotion != null) {
                        mBasicFacialExpImage.setImageResource(mBasicFacialExpImageDrawables[emotion.ordinal()]);
                    }
                    mBasicFacialExpValText.setText(basicFacialExpression.getEmotionStr());

                    // Show container for Basic Facial Expression
                    mBasicFacialExpValContainer.setVisibility(View.VISIBLE);
                } else {
                    e.printStackTrace();
                }
            }

            @Override
            public void onRecognizedValenceFacialExpression(ESRCType.ValenceFacialExpression valenceFacialExpression, ESRCException e) {
                if (e == null) {
                    Log.d(TAG, "onRecognizedValenceFacialExpression: " + valenceFacialExpression.toString());

                    // Set Valence Facial Expression values
                    ESRCType.ValenceFacialExpression.Emotion emotion = valenceFacialExpression.getEmotion();
                    if (emotion != null) {
                        mValenceFacialExpImage.setImageResource(mValenceFacialExpImageDrawables[emotion.ordinal()]);
                    }
                    mValenceFacialExpValText.setText(valenceFacialExpression.getEmotionStr());

                    // Show container for Valence Facial Expression
                    mValenceFacialExpValContainer.setVisibility(View.VISIBLE);
                } else {
                    e.printStackTrace();
                }
            }

            @Override
            public void onEstimatedHeadPose(ESRCType.HeadPose headPose, ESRCException e) {
                if (e == null) {
                    Log.d(TAG, "onEstimatedHeadPose: " + headPose.toString());
                } else {
                    e.printStackTrace();
                }
            }

            @Override
            public void onRecognizedAttention(ESRCType.Attention attention, ESRCException e) {
                if (e == null) {
                    Log.d(TAG, "onRecognizedAttention: " + attention.toString());

                    // Set Attention values
                    if(attention.getIsAttend()) {
                        mAttentionValText.setText("O");
                    } else {
                        mAttentionValText.setText("X");
                    }

                    // Show container for Attention
                    mAttentionValContainer.setVisibility(View.VISIBLE);
                } else {
                    e.printStackTrace();
                }
            }

            @Override
            public void didChangedProgressRatioOnRemoteHR(ESRCType.ProgressRatio progressRatio, ESRCException e) {
                if (e == null) {
                    Log.d(TAG, "didChangedProgressRatioOnRemoteHR: " + progressRatio.toString());

                    // Set HR spinner
                    mHRSpinner.setProgress((int) progressRatio.getProgress());

                    // Hide dummy spinner
                    if (mHRSpinnerDummy.getVisibility() == View.VISIBLE) {
                        mHRSpinnerDummy.setVisibility(View.GONE);
                        mHRSpinner.setVisibility(View.VISIBLE);
                    }
                } else {
                    e.printStackTrace();
                }
            }

            @Override
            public void onEstimatedRemoteHR(ESRCType.RemoteHR remoteHR, ESRCException e) {
                if (e == null) {
                    Log.d(TAG, "onEstimatedRemoteHR: " + remoteHR.toString());

                    // Hide HR spinner
                    if(mHRSpinner.getVisibility() == View.VISIBLE) {
                        mHRSpinner.setVisibility(View.GONE);
                        mHRSpinnerDummy.setVisibility(View.GONE);
                    }

                    // Set HR values
                    mHRValText.setText(Long.toString(Math.round(remoteHR.getHR())));

                    // Show container for HR
                    mHRValContainer.setVisibility(View.VISIBLE);
                } else {
                    e.printStackTrace();
                }
            }

            @Override
            public void didChangedProgressRatioOnHRV(ESRCType.ProgressRatio progressRatio, ESRCException e) {
                if (e == null) {
                    Log.d(TAG, "didChangedProgressRatioOnHRV: " + progressRatio.toString());

                    // Set HRV and Engagement spinner
                    mHRVSpinner.setProgress((int) progressRatio.getProgress());
                    mEngagementSpinner.setProgress((int) progressRatio.getProgress());

                    // Hide dummy spinner
                    if (mHRVSpinnerDummy.getVisibility() == View.VISIBLE) {
                        mHRVSpinnerDummy.setVisibility(View.GONE);
                        mEngagementSpinnerDummy.setVisibility(View.GONE);
                        mHRVSpinner.setVisibility(View.VISIBLE);
                        mEngagementSpinner.setVisibility(View.VISIBLE);
                    }
                } else {
                    e.printStackTrace();
                }
            }

            @Override
            public void onAnalyzedHRV(ESRCType.HRV hrv, ESRCException e) {
                if (e == null) {
                    Log.d(TAG, "onAnalyzedHRV: " + hrv.toString());

                    // Hide HRV spinner
                    if(mHRVSpinner.getVisibility() == View.VISIBLE) {
                        mHRVSpinner.setVisibility(View.GONE);
                        mHRVSpinnerDummy.setVisibility(View.GONE);
                    }

                    // Set HRV values
                    mHRVValText.setText(Double.toString((double)Math.round(hrv.getSdnn()*100) / 100));

                    // Show container for HRV
                    mHRVValContainer.setVisibility(View.VISIBLE);
                } else {
                    e.printStackTrace();
                }
            }

            @Override
            public void onRecognizedEngagement(ESRCType.Engagement engagement, ESRCException e) {
                if (e == null) {
                    Log.d(TAG, "onRecognizedEngagement: " + engagement.toString());

                    // Hide Engagement spinner
                    if(mEngagementSpinner.getVisibility() == View.VISIBLE) {
                        mEngagementSpinner.setVisibility(View.GONE);
                        mEngagementSpinnerDummy.setVisibility(View.GONE);
                    }

                    // Set Engagement values
                    mEngagementValText.setText(engagement.getEmotionStr());
                    //mEngagementValText.setText(Integer.toString(engagement.getEmotion()));

                    // Show container for Engagement
                    mEngagementValContainer.setVisibility(View.VISIBLE);
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Stop ESRC process.
     */
    private void stop() {
        // Stop ESRC
        ESRC.stop();
    }

    /**
     * Check granted permissions.
     */
    private boolean hasPermissions(String[] permissions) {
        int result;

        for (String perms : permissions) {
            result = ContextCompat.checkSelfPermission(this, perms);

            if (result == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }

        return true;
    }

    /**
     * Request permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean cameraPermissionAccepted = grantResults[0]
                            == PackageManager.PERMISSION_GRANTED;

                    if (!cameraPermissionAccepted)
                        showDialogForPermission("If you reject permission, you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]");
                }
                break;
        }
    }

    /**
     * Show dialog for permission.
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void showDialogForPermission(String msg) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Permission");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("ALLOW", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("DENY", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                finish();
            }
        });
        builder.create().show();
    }
}