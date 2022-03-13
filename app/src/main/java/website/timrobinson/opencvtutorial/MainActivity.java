package website.timrobinson.opencvtutorial;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends AppCompatActivity implements OnTouchListener, CvCameraViewListener2 {

    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat mRgba;
    private Scalar mBlobColorHsv;
    private Scalar mBlobColorRgba;

    TextView touch_coordinates;
    TextView touch_color;
    TextView touch_color2;
    TextView square_color;
    Button slot_1;
    Button slot_2;
    TextView red_1;
    TextView green_1;
    TextView blue_1;
    TextView red_2;
    TextView green_2;
    TextView blue_2;
    Float r1;
    Float g1;
    Float b1;
    Float r2;
    Float g2;
    Float b2;
    Float delta;
    TextView delta_result;

    double x = -1;
    double y = -1;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(MainActivity.this);
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        touch_coordinates = (TextView) findViewById(R.id.touch_coordinates);
        touch_color = (TextView) findViewById(R.id.touch_color);
        touch_color2 = (TextView) findViewById(R.id.touch_color2);
        square_color = (TextView) findViewById(R.id.square);
        slot_1 = (Button)findViewById(R.id.button1);
        slot_2 = (Button)findViewById(R.id.button2);
        red_1 = (TextView)findViewById(R.id.red1);
        green_1 = (TextView)findViewById(R.id.green1);
        blue_1 = (TextView)findViewById(R.id.blue1);
        red_2 = (TextView)findViewById(R.id.red2);
        green_2 = (TextView)findViewById(R.id.green2);
        blue_2 = (TextView)findViewById(R.id.blue2);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.opencv_tutorial_activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat();
        mBlobColorRgba = new Scalar(255);
        mBlobColorHsv = new Scalar(255);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        return mRgba;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int cols = mRgba.cols();
        int rows = mRgba.rows();

        double yLow = (double)mOpenCvCameraView.getHeight() * 0.2401961;
        double yHigh = (double)mOpenCvCameraView.getHeight() * 0.7696078;

        double xScale = (double)cols / (double)mOpenCvCameraView.getWidth();
        double yScale = (double)rows / (yHigh - yLow);

        x = event.getX();
        y = event.getY();

        y = y - yLow;

        x = x * xScale;
        y = y * yScale;

        if((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;

        touch_coordinates.setText("X: " + Double.valueOf(x) + ", Y: " + Double.valueOf(y));

        Rect touchedRect = new Rect();

        touchedRect.x = (int)x;
        touchedRect.y = (int)y;

        touchedRect.width = 8;
        touchedRect.height = 8;

        Mat touchedRegionRgba = mRgba.submat(touchedRect);

        Mat touchedRegionHsv = new Mat();
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = touchedRect.width * touchedRect.height;
        for (int i = 0; i < mBlobColorHsv.val.length; i++)
            mBlobColorHsv.val[i] /= pointCount;

        mBlobColorRgba = convertScalarHsv2Rgba(mBlobColorHsv);

        touch_color.setText("HEX Color: #" + String.format("%02X", (int)mBlobColorRgba.val[0])
                + String.format("%02X", (int)mBlobColorRgba.val[1])
                + String.format("%02X", (int)mBlobColorRgba.val[2]));

        touch_color2.setText("RGB Color: " + (int)mBlobColorRgba.val[0] + " , "
                + (int)mBlobColorRgba.val[1] + " , "
                + (int)mBlobColorRgba.val[2]);

        square_color.setTextColor(Color.rgb((int) mBlobColorRgba.val[0],
                (int) mBlobColorRgba.val[1],
                (int) mBlobColorRgba.val[2]));

        Button button1= (Button) findViewById(R.id.button1);
        button1.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mRGB = (String) touch_color2.getText();
                String[] str = mRGB.split(" ");
                String str1 = str[2];
                String str2 = str[4];
                String str3 = str[6];
                red_1.setText(str1);
                green_1.setText(str2);
                blue_1.setText(str3);
            }
        });

        Button button2= (Button) findViewById(R.id.button2);
        button2.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mRGB = (String) touch_color2.getText();
                String[] str = mRGB.split(" ");
                String str1 = str[2];
                String str2 = str[4];
                String str3 = str[6];
                red_2.setText(str1);
                green_2.setText(str2);
                blue_2.setText(str3);

//                String r = (String) red_1.getText();
//                String g = (String) green_1.getText();
//                String b = (String) blue_1.getText();
//                r1 = Float.parseFloat(r);
//                g1 = Float.parseFloat(g);
//                b1 = Float.parseFloat(b);
//
//                r2 = Float.parseFloat(str1);
//                g2 = Float.parseFloat(str2);
//                b2 = Float.parseFloat(str3);
//
//                delta = r1+g1+g2;
//                String deltaResult = delta.toString();
//                delta_result.setText(deltaResult);

            }
        });

        return false;
    }

    private Scalar convertScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        return new Scalar(pointMatRgba.get(0, 0));
    }


}
