package com.example.camerademo;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.ColorDrawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.RggbChannelVector;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static android.view.animation.Animation.RELATIVE_TO_PARENT;
import static androidx.core.math.MathUtils.clamp;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity implements View.OnTouchListener, GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener, MediaScannerConnection.MediaScannerConnectionClient, SeekBar.OnSeekBarChangeListener {

    TextureView mPreviewView;
    HandlerThread mHandlerThread;
    Handler mCameraHandler;
    CameraManager manager;
    Size mPreviewSize;//最佳预览尺寸
    Size mCaptureSize;//最佳拍照尺寸

    String mCameraId;
    CameraDevice mCameraDevice;
    CaptureRequest.Builder mCaptureRequestBuilder;
    private CaptureRequest.Builder mPreviewBuilder;
    CaptureRequest mCaptureRequest;
    CameraCaptureSession mCameraCaptureSession;
    CaptureRequest.Builder mCameraBuilder;
    SurfaceTexture mSurfaceTexture;

    ImageReader mImageReader;
    CameraCharacteristics characteristics;
    Surface previewSurface;


    static String TAG = "jjb";
    boolean loading = false;
    boolean take = true;
    private MediaRecorder mMediaRecorder;
    private String mNextVideoAbsolutePath;
    private LinearLayout all;
    private Button btn_video;
    private Chronometer time;
    private Button takePhoto;
    private Button flip;
    private Button flash;
    private TextView flashAuto;
    private TextView flashOn;
    private TextView flashOff;
    private static ImageView readImg;
    private TextView screenone;//all
    private TextView screentwo;//1:1
    private TextView screenthree;//4:3
    private TextView screenfour;//16:9
    private TextView screenfive;//full
    private Button hdr;
    private Button meiyan;
    private HorizontalPickerView meiyan_srcoll;
    private int lv = 0;
    private Button grid;
    //private Button videoicon;
    private TextView[] screensize;
    private TextView[] flashsta;
    private TextView[] pickGone;
    private ImageView scanBtn;
    private int pheight;
    private int pwidth;
    boolean isflash = false;
    boolean isback = true;
    boolean isgrid = false;
    boolean ismeiyan = true;
    boolean ishdr = true;
    private HorizontalPickerView picker;
    String flashState;
    private GestureDetector detector;
    private float oldDistance;
    private int zoomParams;
    private TextView zoomMag;
    Rect zoomRect;
    Rect focusRect = new Rect();
    private ImageView focusImg;
    private int phoneHeight;
    private int phoneWidth;
    private SeekBar seekbar_zoom;
    private SeekBar seekbar_focus;
    private SeekBar seekbar_ev;
    private SeekBar seekbar_awb;
    private int AWB = 65;
    private TextView seekTag;
    private LinearLayout seekLayout;
    private LinearLayout major;
    private boolean isMajor = false;
    private LinearLayout btn_ev;
    private LinearLayout btn_focus;
    private LinearLayout btn_zoom;
    private LinearLayout btn_awb;
    private GLSurfaceView glSurfaceView;


    public String[] allFiles;
    public String[] allFiles1;
    public String[] allFiles2;
    private String SCAN_PATH;
    private static final String FILE_TYPE = "image/*";
    private MediaScannerConnection conn;
    private MeteringRectangle[] mResetRect = new MeteringRectangle[]{
            new MeteringRectangle(0, 0, 0, 0, 0)
    };


    private static final SparseIntArray ORIENTATION = new SparseIntArray();

    static {
        ORIENTATION.append(Surface.ROTATION_0, 90);
        ORIENTATION.append(Surface.ROTATION_90, 0);
        ORIENTATION.append(Surface.ROTATION_180, 270);
        ORIENTATION.append(Surface.ROTATION_270, 180);
    }

    private static final SparseIntArray ORIENTATION_FRONT = new SparseIntArray();

    static {
        ORIENTATION_FRONT.append(Surface.ROTATION_0, 270);
        ORIENTATION_FRONT.append(Surface.ROTATION_90, 270);
        ORIENTATION_FRONT.append(Surface.ROTATION_180, 270);
        ORIENTATION_FRONT.append(Surface.ROTATION_270, 270);
    }


    private ConstraintLayout.LayoutParams params;
    DrawView root;
    private long oldTime;
    private long videoTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSystemUiHide();
        setContentView(R.layout.activity_main);
        mPreviewView = findViewById(R.id.textureView);
        picker = findViewById(R.id.scrollPicker);
        btn_video = findViewById(R.id.video);
        time = findViewById(R.id.time);
        takePhoto = findViewById(R.id.photo);
        flip = findViewById(R.id.flip);
        flash = findViewById(R.id.switch_flash);
        flashAuto = findViewById(R.id.switch_flashauto);
        flashOn = findViewById(R.id.switch_flashon);
        flashOff = findViewById(R.id.switch_flashoff);
        readImg = findViewById(R.id.album);
        screenone = findViewById(R.id.screen);
        screentwo = findViewById(R.id.screen1t1);
        screenthree = findViewById(R.id.screen4t3);
        screenfour = findViewById(R.id.screen16t9);
        screenfive = findViewById(R.id.screenfull);
        scanBtn = (ImageView) findViewById(R.id.album);
        root = findViewById(R.id.root);
        grid = findViewById(R.id.grid);
        seekbar_zoom = findViewById(R.id.seekbar_zoom);
        zoomMag = findViewById(R.id.zoomMag);
        seekbar_focus = findViewById(R.id.seekbar_focus);
        focusImg = findViewById(R.id.focusImg);
        seekbar_ev = findViewById(R.id.seekbar_ev);
        hdr = findViewById(R.id.HDR);
        meiyan = findViewById(R.id.meiyan);
        meiyan_srcoll = findViewById(R.id.meiyanPicker);
        //videoicon = findViewById(R.id.videoicon);
        major = findViewById(R.id.major);
        btn_ev = findViewById(R.id.btn_ev);
        btn_focus = findViewById(R.id.btn_focus);
        btn_zoom = findViewById(R.id.btn_zoom);
        btn_awb = findViewById(R.id.btn_awb);
        seekTag = findViewById(R.id.seekTag);
        seekLayout = findViewById(R.id.seekLayout);
        seekbar_awb = findViewById(R.id.seekbar_awb);
        screenone.setOnClickListener(new onClick());
        screentwo.setOnClickListener(new onClick());
        screenthree.setOnClickListener(new onClick());
        screenfour.setOnClickListener(new onClick());
        screenfive.setOnClickListener(new onClick());

        params = (ConstraintLayout.LayoutParams) mPreviewView.getLayoutParams();
        pickGone = new TextView[]{flashAuto, flashOn, flashOff, screentwo, screenthree, screenfour, screenfive};
        screensize = new TextView[]{screenone, screentwo, screenthree, screenfour, screenfive};
        //scrollView = findViewById(R.id.scrollview);

        detector = new GestureDetector(this, this);
        mPreviewView.setOnTouchListener(this);
        btn_video.setOnTouchListener(this);
        flip.setOnTouchListener(this);
//        //scrollView.setOnTouchListener(this);
//        btn_video.setOnClickListener(new View.OnClickListener() {
//            @RequiresApi(api = Build.VERSION_CODES.M)
//            @Override
//            public void onClick(View v) {
//                Log.d(TAG, "onClick: 11111");
//                video();
//            }
//        });
//        flip.setOnClickListener(new View.OnClickListener() {
//            @RequiresApi(api = Build.VERSION_CODES.M)
//            @Override
//            public void onClick(View v) {
//                Log.d(TAG, "flip: 11122");
//                flip();
//            }
//        });
        flash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switchFlash(v);
                        focusImg.setVisibility(View.GONE);
                        //setAWB(6600);
                    }
                });
            }
        });
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scannerImg();
                if (SCAN_PATH == null) {
                    Toast.makeText(getApplicationContext(), "相册为空，先去拍几张照片吧", Toast.LENGTH_SHORT).show();
                } else {
                    startScan();
                }
            }
        });
        grid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isgrid) {
                    isgrid = !isgrid;
                    init();
                } else {
                    isgrid = !isgrid;
                    init();
                }
            }
        });
        hdr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ishdr) {
                    ishdr = !ishdr;
                    hdr.setBackgroundDrawable(getResources().getDrawable(R.drawable.hdron));
                } else {
                    ishdr = !ishdr;
                    hdr.setBackgroundDrawable(getResources().getDrawable(R.drawable.hdroff));
                }


            }
        });
        meiyan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: 111111");
                if (ismeiyan) {
                    ismeiyan = !ismeiyan;
                    meiyan.setBackgroundDrawable(getResources().getDrawable(R.drawable.meiyanon));
                    picker.setVisibility(View.GONE);
                    major.setVisibility(View.GONE);
                    seekLayout.setVisibility(View.GONE);
                    meiyan_srcoll.setVisibility(View.VISIBLE);
                    meiyanPicker();
                } else {
                    ismeiyan = !ismeiyan;
                    meiyan.setBackgroundDrawable(getResources().getDrawable(R.drawable.meiyan));
                    meiyan_srcoll.setVisibility(View.GONE);
                    picker.setVisibility(View.VISIBLE);
                    //setEV(0);
                    mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO);
                    //setAWB(65);
//                    mCaptureRequestBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE,CaptureRequest.CONTROL_EFFECT_MODE_OFF);
                    setlvjing(8);
                }
            }
        });
        btn_zoom.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                seekGone(seekbar_zoom);
                seekTag.setText("ZOOM");
                zoomMag.setText("1.0x");
                seekbar_zoom.setProgress(zoomParams * 2, true);
                zoom(zoomParams * 6);
            }
        });
        btn_focus.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                seekGone(seekbar_focus);
                seekTag.setText("FOCUS");
                seekbar_focus.setProgress(0, true);
            }
        });
        btn_ev.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                seekGone(seekbar_ev);
                seekTag.setText("E_V");
                seekbar_ev.setProgress(50, true);
            }
        });
        btn_awb.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                seekGone(seekbar_awb);
                seekTag.setText("AWB");
                seekbar_awb.setProgress(50, true);
            }
        });
        seekbar_zoom.setOnSeekBarChangeListener(this);
        seekbar_focus.setOnSeekBarChangeListener(this);
        seekbar_ev.setOnSeekBarChangeListener(this);
        seekbar_awb.setOnSeekBarChangeListener(this);
        scrollPicker();
        //getFlashState();
        flip.bringToFront();

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
        startCameraThread();//1 开启摄像头线程
        Log.d(TAG, "onResume: success1");
        if (!mPreviewView.isAvailable()) {
            Log.d(TAG, "onResume: !mPreviewView.isAvailable()");
            mPreviewView.setSurfaceTextureListener(mTexturelistener);//2 添加预览
        } else {
            openCamera();//5 开始预览
            Log.d(TAG, "onResume: success2");
        }
        scannerImg();
        Log.d(TAG, "onResume: SCAN_PATH = " + SCAN_PATH);
        if (SCAN_PATH != null) {
            if (SCAN_PATH.indexOf("mp4") == -1) {
                readImg(SCAN_PATH);
            } else {
                readImg.setImageBitmap(fristVideo(SCAN_PATH));
            }
        }
//        Display display = getWindow().getWindowManager().getDefaultDisplay();
//        int rotation = display.getRotation();
//        Point outSize = new Point();
//        display.getSize(outSize);
//        phoneWidth = outSize.x;
//        phoneHeight = outSize.y;


        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        phoneHeight = dm.heightPixels;
        phoneWidth = dm.widthPixels;

        Log.d(TAG, "onResume: dm.widthPixels = " + phoneWidth);
        Log.d(TAG, "onResume: dm. dm.heightPixels = " + phoneHeight);
        oldDistance = 0;
        zoomMag.setText("1.0x");
        meiyan_srcoll.setVisibility(View.GONE);
        picker.setVisibility(View.VISIBLE);
        ismeiyan = true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        pheight = mPreviewView.getHeight();
        pwidth  = mPreviewView.getWidth();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent dispatchTouchEvent) {

        long nowTime = System.currentTimeMillis();
        if (nowTime - oldTime >= 10) {
            oldTime = nowTime;
            return super.dispatchTouchEvent(dispatchTouchEvent);
        }
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeCamera();
        setupImageReader();
    }

    void dispatchTouchEvent() {

    }

    private void getDrawPhone(int phoneWidth2, int phoneHeight2) {
        root.getPhone(phoneWidth2, phoneHeight2);
    }

    private void init() {

        if (isgrid)
            root.setSize(pwidth, pheight);
        else
            root.setSize(0, 0);
        root.bringToFront();

    }

    private void cut() {
        if (take) {
            take = false;
            //videoicon.setVisibility(View.VISIBLE);
            flash.setVisibility(View.GONE);
            hdr.setVisibility(View.GONE);
            meiyan.setVisibility(View.GONE);
            screenone.setVisibility(View.GONE);
            takePhoto.setVisibility(View.GONE);
            btn_video.setVisibility(View.VISIBLE);
        } else {
            take = true;
            flash.setVisibility(View.VISIBLE);
            hdr.setVisibility(View.VISIBLE);
            meiyan.setVisibility(View.VISIBLE);
            screenone.setVisibility(View.VISIBLE);
            btn_video.setVisibility(View.GONE);
            takePhoto.setVisibility(View.VISIBLE);
        }
    }

    private void scrollPicker() {
//        final HorizontalPickerView picker = (HorizontalPickerView) findViewById(R.id.scrollPicker);

        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            list.add(i);
        }

        final ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this, 0, list) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                int value = getItem(position);
                Log.d(TAG, "getView: value = " + value);
                if (convertView == null) {
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    convertView = inflater.inflate(android.R.layout.simple_list_item_1, null);
                }
                TextView view = (TextView) convertView.findViewById(android.R.id.text1);
                if (value == 0) {
                    view.setText("照片");
                } else if (value == 1) {
                    view.setText("视频");
                } else if (value == 2) {
                    view.setText("人像");
                } else if (value == 3) {
                    view.setText("专业");
                }
                return convertView;

            }
        };

        picker.setAdapter(adapter);

        picker.setOnSelectedListener(new HorizontalPickerView.OnSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void selected(View v, int index) {
                ViewGroup group = (ViewGroup) picker.getChildAt(0);
                for (int i = 0; i < adapter.getCount(); i++) {
                    View view = group.getChildAt(i);
                    TextView textview = (TextView) view;
                    if (i == index) {
                        textview.setTextColor(0xfffcaf17);
                        switch (i) {
                            case 0:
                                take = false;
                                cut();
                                pickedgone();
                                //videoicon.setVisibility(View.GONE);
                                focusImg.setVisibility(View.GONE);
                                major.setVisibility(View.GONE);
                                zoomMag.setVisibility(View.VISIBLE);
                                break;
                            case 1:
                                take = true;
                                pickedgone();
                                cut();
                                focusImg.setVisibility(View.GONE);
                                flash.setVisibility(View.GONE);
                                major.setVisibility(View.GONE);
                                zoomMag.setVisibility(View.VISIBLE);
                                break;
                            case 2:
                                take = false;
                                cut();
                                pickedgone();
                                focusImg.setVisibility(View.GONE);
                                //videoicon.setVisibility(View.GONE);
                                major.setVisibility(View.GONE);
                                zoomMag.setVisibility(View.VISIBLE);
                                if (isMajor){
                                    isMajor = false;
                                    setmCaptureRequestBuilder();
                                }
                                break;
                            case 3:
                                isMajor = true;
                                take = false;
                                cut();
                                pickedgone();
                                focusImg.setVisibility(View.GONE);
                                //videoicon.setVisibility(View.GONE);
                                major.setVisibility(View.VISIBLE);
                                zoomMag.setVisibility(View.GONE);
                                break;
                            default:
                                break;
                        }

                    } else {
                        view.setBackgroundColor(group.getDrawingCacheBackgroundColor());
                        textview.setTextColor(0xFFFFFFFF);
                    }
                }
            }
        });
    }

    private void pickedgone() {
        seekLayout.setVisibility(View.GONE);
        flash.setVisibility(View.VISIBLE);
        hdr.setVisibility(View.VISIBLE);
        grid.setVisibility(View.VISIBLE);
        meiyan.setVisibility(View.VISIBLE);
        screenone.setVisibility(View.VISIBLE);
        for (int i = 0; i < pickGone.length; i++) {
            pickGone[i].setVisibility(View.GONE);
        }
    }

    private void configureTransform(int viewWidth, int viewHeight) {
        Log.d(TAG, "configureTransform: 1111111111111");
        if (null == mPreviewView || null == mPreviewSize || null == this.getClass()) {
            return;
        }
        int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        Log.d(TAG, "configureTransform: rotation = " + rotation);
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mPreviewView.setTransform(matrix);
    }

    TextureView.SurfaceTextureListener mTexturelistener = new TextureView.SurfaceTextureListener() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            //mTexturelistener 可用时，设置摄像头参数，打开摄像头
            Log.d("jjb", "setupCamera11111111: width = " + width + " height =   " + height);
            setupCamera(width, height);//3 设置摄像头参数
            openCamera();//4 打开摄像头

        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            Log.d("jjb", "setupCamera:mPreviewSize  onSurfaceTextureSizeChanged   width = " + width + " height = " + width);
            configureTransform(width, height);
            closeCamera();
            setupCamera(width, height);
            openCamera();
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    //1 开启摄像头线程
    private void startCameraThread() {//1
        mHandlerThread = new HandlerThread("CameraThread");
        mHandlerThread.start();
        mCameraHandler = new Handler(mHandlerThread.getLooper());
    }

    //切换摄像头
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void flip() {
        long nowTime = System.currentTimeMillis();
        Log.d(TAG, "flip: " + nowTime + " " +oldTime );
        if (nowTime - oldTime >= 0) {
            Log.d(TAG, "flip: mCameraId = " + mCameraId);
            if (isback) {
                screengone();
                flash.setVisibility(View.GONE);
                flashAuto.setVisibility(View.GONE);
                flashOn.setVisibility(View.GONE);
                flashOff.setVisibility(View.GONE);
                Log.d(TAG, "flip: isback1 = " + isback);
                mCameraId = "1";
                closeCamera();
                setupImageReader();
                openCamera();
                zoomParams = 0;
                zoomMag.setText("1.0x");
                isback = false;
                meiyan_srcoll.setVisibility(View.GONE);
                picker.setVisibility(View.VISIBLE);
                ismeiyan = true;
            } else if (!isback) {
                screengone();
                Log.d(TAG, "flip: isback2 = " + isback);
                mCameraId = "0";
                closeCamera();
                setupImageReader();
                openCamera();
                zoomParams = 0;
                zoomMag.setText("1.0x");
                isback = true;
                meiyan_srcoll.setVisibility(View.GONE);
                picker.setVisibility(View.VISIBLE);
                ismeiyan = true;
            }
            oldTime = nowTime;
        }

    }

    private void closeCamera() {
        if (null != mCameraCaptureSession) {
//            try {
//                mCameraCaptureSession.stopRepeating();
//            } catch (CameraAccessException e) {
//                e.printStackTrace();
//            }
            mCameraCaptureSession.close();
            mCameraCaptureSession = null;
        }
        if (null != mCameraDevice) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
//            if (null != mImageReader) {
//                mImageReader.close();
//                mImageReader = null;
//            }

    }

    //3 设置摄像头参数
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setupCamera(int width, int height) {
        Log.e(TAG, "setupCamera: mPreviewSize  width = " + width + " height = " + height);
        Log.d(TAG, "setupCamera: 1111111111111");
        manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        //Log.d("jjb", "setupCamera111111: width = "+width+" height = "+height + manager);

        //3.1 拿到摄像头ID
        try {
            for (String cameraID : manager.getCameraIdList()) {
                characteristics = manager.getCameraCharacteristics(cameraID);
                zoomRect = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);//摄像头朝向
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }
                //Log.d(TAG, "setupCamera: facing = " + facing);
                //3.2  设置摄像头尺寸
                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                Size[] sizes = map.getOutputSizes(ImageFormat.JPEG);
//                for (int i = 0;i< sizes.length;i++){
//                  Log.d("jjb", "setupCamera: width : "+ sizes[i].getWidth()+" height : "+ sizes[i].getHeight());
//                }
                if (map != null) {// 3.2 找出摄像头能够输出的，最符合我们当前界面分辨率的最小值
                    Log.d("jjb", "setupCamera:mPreviewSize  width = " + width + " height = " + height);
                    mPreviewSize = getOptimalSize(map.getOutputSizes(SurfaceTexture.class), width, height);
                    Log.d("jjb", "setupCamera:mPreviewSize  width = " + mPreviewSize.getWidth() + " height = " + mPreviewSize.getHeight());
                    //6.2 图片缓冲区尺寸   拍照尺寸   保存照片的最高分辨率

                    mCaptureSize = getSaveSize(map.getOutputSizes(SurfaceTexture.class), width, height);
                    Log.d(TAG, "setupCamera: mCaptureSize = " + mCaptureSize);
                }
                // 6.3 建立ImageReader，准备存储照片
                setupImageReader();
                mCameraId = cameraID;
                Log.d(TAG, "setupCamera: mCameraId = " + mCameraId);
                break;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    //3.2 返回最适合的摄像头尺寸
    private Size getOptimalSize(Size[] sizeMap, int width, int height) {
        Log.d(TAG, "getOptimalSize: width = " + width + " height = " + height);
        //传入所有尺寸和预览界面的宽高，将所有符合比例且比例的宽高大于预览宽高的保存下来
        List<Size> sizeList = new ArrayList<Size>();
        //要使用Double，不然用int类型的数据，计算  除法  时结果也是 int
        double width1, height1, width2, height2;
        for (Size option : sizeMap) {
            width1 = option.getWidth();
            height1 = option.getHeight();
            width2 = height;
            height2 = width;
            //Log.d(TAG, "getOptimalSize: "+width1/height1+"  "+width2/height2);
            if ((option.getWidth() >= height && option.getHeight() >= width) && (width1 / height1 == width2 / height2)) {
                sizeList.add(option);
                //Log.d(TAG, "getOptimalSize1111: option = " + option.getWidth() + "*" + option.getHeight());
            }
        }
        //如果上一部没有保存到任何数据，就将所有比例宽高大于预览的保存下来
        if (sizeList.size() == 0) {
            for (Size option : sizeMap) {
                if (option.getWidth() >= height && option.getHeight() >= width) {
                    sizeList.add(option);
                    //Log.d(TAG, "getOptimalSize4444: option = " + option.getWidth() + "*" + option.getHeight());
                }
            }
        }
        //如果保存的数据较多，则在其中选择一个最接近的
        if (sizeList.size() > 1) {
            return Collections.min(sizeList, new Comparator<Size>() {
                @Override
                public int compare(Size o1, Size o2) {
                    //Log.e(TAG, "compare: o1 = "+ o1 + "      o2 = " + o2);
                    return Long.signum(o1.getWidth() * o1.getHeight() - o2.getWidth() * o2.getHeight());
                }
            });
        }
        return sizeMap[0];
    }

    private Size getSaveSize(Size[] sizeMap, int width, int height) {
        Log.d(TAG, "getOptimalSize: width = " + width + " height = " + height);
        //传入所有尺寸和预览界面的宽高，将所有符合比例且比例的宽高大于预览宽高的保存下来
        List<Size> sizeList = new ArrayList<Size>();
        //要使用Double，不然用int类型的数据，计算  除法  时结果也是 int
        double width1, height1, width2, height2;
        for (Size option : sizeMap) {
            width1 = option.getWidth();
            height1 = option.getHeight();
            width2 = height;
            height2 = width;
            Log.d(TAG, "mCaptureSize: option = " + option.getWidth() + "*" + option.getHeight());
            Log.d(TAG, "getSaveSize: mCaptureSize = " + width1 + "   " + height1 + "  " + width2 + "   " + height2);
            Log.d(TAG, "getSaveSize   mCaptureSize : " + width1 / height1 + "  " + width2 / height2);
            if ((option.getWidth() >= height && option.getHeight() >= width) && (width1 / height1 == width2 / height2)) {
                sizeList.add(option);
                Log.d(TAG, "mCaptureSize: option = " + option.getWidth() + "*" + option.getHeight());
            }
        }
        //如果上一部没有保存到任何数据，就将所有比例宽高大于预览的保存下来
        if (sizeList.size() == 0) {
            return mPreviewSize;
        }
        //如果保存的数据较多，则在其中选择一个像素最好的
        if (sizeList.size() > 1) {
            return Collections.max(sizeList, new Comparator<Size>() {
                @Override
                public int compare(Size o1, Size o2) {
                    //Log.e(TAG, "compare: o1 = "+ o1 + "      o2 = " + o2);
                    return Long.signum(o1.getWidth() * o1.getHeight() - o2.getWidth() * o2.getHeight());
                }
            });
        }
        return sizeMap[0];
    }

    //4 打开摄像头
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void openCamera() {
        Log.e(TAG, "openCamera: 1111111111111111");
        //configureTransform(mPreviewView.getWidth(), mPreviewView.getHeight());
        //configureTransform(pheight, pwidth);
        String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO};
        int i = 0;
        for (String permission : permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(permissions, i++);
            }
        }
        try {
            //Log.d(TAG, "openCamera: manager " + manager);
            manager.openCamera(mCameraId, mStateCallback, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    // 打开摄像头，摄像头状态的回调函数
    CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.e(TAG, "startPreview: onOpened1111111111111");
            //确定摄像头打开  启动预览
            mCameraDevice = camera;
            startPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.e(TAG, "startPreview: onDisconnected111111111111");
            mCameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.e(TAG, "startPreview: onError111111111111111111111");
            mCameraDevice.close();
            mCameraDevice = null;
        }
    };

    //扫描文件夹内容，实现点击缩略图弹出相册
    private void scannerImg() {
        File folder = new File(Environment.getExternalStorageDirectory() + "/DCIM/CameraV2/");
        if (!folder.exists())
            folder.mkdir();
        allFiles = folder.list();
        allFiles1 = new String[allFiles.length];
        //会获取到从当前目录删除放入回收站的照片，需要做一个判断 将名称带有.trashed的照片剔除
        int j = 0;
        for (int i = 0; i < allFiles.length; i++) {
            //Log.d(TAG+i, allFiles[i]+"  "+allFiles.length);
            if (allFiles[i].indexOf("trashed") == -1) {
                allFiles1[j] = allFiles[i];
                j++;
            }
        }

        //将不带有trashed的照片保存到allFiles2[]中
        allFiles2 = new String[j];
        for (int i = 0; i < allFiles2.length; i++) {
            allFiles2[i] = allFiles1[i];
            //Log.d(TAG+i, "allFiles2[i]  " + allFiles2[i]+"  "+allFiles2.length);
        }
        if (allFiles2.length != 0) {
            SCAN_PATH = Environment.getExternalStorageDirectory() + "/DCIM/CameraV2/" + allFiles2[allFiles2.length - 1];
            //Log.d(TAG, "Scan Path = " + SCAN_PATH);
            readImg.setColorFilter(Color.parseColor("#00000000"));
        } else {
            SCAN_PATH = null;
            readImg.setBackground(new ColorDrawable(Color.parseColor("#00000000")));
        }
    }

    private void startScan() {
        Log.d(TAG, "startScan success = " + conn);
        if (conn != null)
            conn.disconnect();
        conn = new MediaScannerConnection(this, this);
        conn.connect();
    }

    @Override
    public void onMediaScannerConnected() {
        Log.d(TAG, "onMediaScannerConnected success + " + conn);
        conn.scanFile(SCAN_PATH, FILE_TYPE);
    }

    @Override
    public void onScanCompleted(String s, Uri uri) {
        try {
            Log.d(TAG, uri + " + onScanCompleted success + " + conn);
            if (uri != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(uri);
                startActivity(intent);
            }
        } finally {
            conn.disconnect();
            conn = null;
        }
    }

    //屏幕比例的点击事件
    class onClick implements View.OnClickListener {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @SuppressLint("WrongConstant")
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.screen:
                    if (screentwo.getVisibility() != 0) {
                        for (TextView textView : screensize)
                            textView.setVisibility(View.VISIBLE);
                        screenone.setVisibility(View.GONE);
                        flash.setVisibility(View.GONE);
                        grid.setVisibility(View.GONE);
                        hdr.setVisibility(View.GONE);
                        meiyan.setVisibility(View.GONE);
                    } else {
                        screengone();
                        screenone.scrollTo(0, 0);
                    }
                    break;
                case R.id.screen1t1:
                    screenone.setBackgroundDrawable(getResources().getDrawable(R.drawable.screen1t1));
                    //screencolor(2);
                    screengone();
                    presize(2);
                    setmPreviewView(pheight, pwidth);
                    init();
                    zoomParams = 0;
                    zoom(0);
                    break;
                case R.id.screen4t3:
                    screenone.setBackgroundDrawable(getResources().getDrawable(R.drawable.screen4t3));
                    //screencolor(3);
                    screengone();
                    presize(3);
                    setmPreviewView(pheight, pwidth);
                    init();
                    zoomParams = 0;
                    zoom(0);
                    break;
                case R.id.screen16t9:
                    screenone.setBackgroundDrawable(getResources().getDrawable(R.drawable.screen16t9));
                    //screencolor(4);
                    screengone();
                    presize(4);
                    setmPreviewView(pheight, pwidth);
                    init();
                    zoomParams = 0;
                    zoom(0);
                    break;
                case R.id.screenfull:
                    screenone.setBackgroundDrawable(getResources().getDrawable(R.drawable.fullscreen));
                    //screencolor(5);
                    screengone();
                    presize(5);
                    setmPreviewView(pheight, pwidth);
                    init();
                    zoomParams = 0;
                    zoom(0);
                    break;
            }
        }
    }

    //设置所选中比例的按钮的颜色
    private void screencolor(int num) {
        int no = num - 1;
        for (int i = 0; i < 5; i++)
            screensize[i].setBackground(new ColorDrawable(Color.parseColor("#00000000")));
        screensize[no].setBackground(new ColorDrawable(Color.parseColor("#d0d0d0d0")));
    }

    //点击按钮隐藏
    private void screengone() {
        for (int i = 1; i < 5; i++)
            screensize[i].setVisibility(View.GONE);
        screenone.setVisibility(View.VISIBLE);
        flash.setVisibility(View.VISIBLE);
        grid.setVisibility(View.VISIBLE);
        hdr.setVisibility(View.VISIBLE);
        meiyan.setVisibility(View.VISIBLE);
        meiyan_srcoll.setVisibility(View.GONE);
        picker.setVisibility(View.VISIBLE);
    }

    //重新开启预览
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setmPreviewView(int height, int width) {
        params.height = height;
        params.width = width;
        Log.d(TAG, "onclick: params.width = " + params.width + "params.height = " + params.height);
        mPreviewView.setLayoutParams(params);
        Log.d(TAG, "onclick: mPreviewView.getWidth = " + mPreviewView.getWidth() + "mPreviewView.getHeight = " + mPreviewView.getHeight());
        //startPreview();
    }

    private void presize(int num) {
        switch (num) {
            case 2:
                pheight = phoneWidth;
                pwidth = phoneWidth;
                break;
            case 3:
                pheight = phoneWidth / 3 * 4;
                pwidth = phoneWidth;
                break;
            case 4:
                pheight = phoneWidth / 9 * 16;
                pwidth = phoneWidth;
                break;
            case 5:
                Log.d(TAG, "startPreview: ===== " + ViewGroup.LayoutParams.MATCH_PARENT);
//                getDrawPhone(phoneWidth,ViewGroup.LayoutParams.MATCH_PARENT);
                pheight = root.getHeight();
                pwidth = phoneWidth;
                break;
        }
    }

    //5 开始预览
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startPreview() {
        //Log.d(TAG, "startPreview: startPreview111");
        //5.1 建立图像缓冲区
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                focusImg.setVisibility(View.GONE);
                setFlashSet();
                //setAWB(6600);
            }
        });


        mSurfaceTexture = mPreviewView.getSurfaceTexture();
        Log.d("jjb", "startPreview: mPreviewView size11111 =  " + mPreviewView.getWidth() + " * " + mPreviewView.getHeight());
        if (isScreenOriatonPortrait() == true) {
            Log.d(TAG, "startPreview: pwidht = " + pwidth + "pheight = " + pheight);
            mSurfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        } else {
            mSurfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        }

        Log.d("jjb", "startPreview: mPreviewSize  =  " + mPreviewSize.getWidth() + " * " + mPreviewSize.getHeight());
        previewSurface = new Surface(mSurfaceTexture);//显示界面

        setFlashSet();
        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            //mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH);
            mCaptureRequestBuilder.addTarget(previewSurface);//捕获请求的对象和显示对象绑定在一起
            //5.3 建立通道 （CaptureRequest 和CaptureSession会话）
            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {//捕获的状态的回调函数
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        mCaptureRequest = mCaptureRequestBuilder.build();//构建一个发送请求的对象
                        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,CaptureRequest.CONTROL_AF_MODE_AUTO);
                        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,CaptureRequest.CONTROL_AE_MODE_ON);
                        mCameraCaptureSession = session;
                        mCameraCaptureSession.setRepeatingRequest(mCaptureRequest, null, mCameraHandler);
                        getDrawPhone(root.getWidth(), root.getHeight());
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                }
            }, mCameraHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    // 隐藏状态栏
    private void setSystemUiHide() {
        if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    // 打开闪光灯
    private void openFlash() {
        if (flashState.equals("flashAuto")) {
            mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_SINGLE);
        } else if (flashState.equals("flashOn")) {
            mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
        } else if (flashState.equals("flashOff")) {
            mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
        }
        mCaptureRequest = mCaptureRequestBuilder.build();
        try {
            mCameraCaptureSession.setRepeatingRequest(mCaptureRequest, null, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    // 关闭闪关灯
    private void closeFlash() {
        mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
        mCaptureRequest = mCaptureRequestBuilder.build();
        try {
            mCameraCaptureSession.setRepeatingRequest(mCaptureRequest, null, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    //6 开始拍照
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void capture(View view) {

        //6.1获取摄像头的请求
        //myOrientoinListener = new MyOrientoinListener(this);
        // myOrientoinListener.enable();

        try {
            mCameraBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        mCameraBuilder.addTarget(mImageReader.getSurface());

        //获取摄像头方向
        int rotaion = getWindowManager().getDefaultDisplay().getRotation();
        //设置拍照方向
        if (isback) {
            mCameraBuilder.set(CaptureRequest.JPEG_ORIENTATION, (Integer) ORIENTATION.get(rotaion));
            Log.d(TAG, "capture: ORIENTATION = " + ORIENTATION.get(rotaion));
        } else {
            mCameraBuilder.set(CaptureRequest.JPEG_ORIENTATION, (Integer) ORIENTATION_FRONT.get(rotaion));
            Log.d(TAG, "capture: ORIENTATION = " + ORIENTATION.get(rotaion));
        }
        setBuilder();
        CameraCaptureSession.CaptureCallback mCaptureCallBack = new CameraCaptureSession.CaptureCallback() {//捕获（拍照）的回调函数
            @Override
            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                Toast.makeText(getApplicationContext(), "拍照结束，相片已保存", Toast.LENGTH_SHORT).show();
                unLockFocus();//释放摄像头的相关资源
                super.onCaptureCompleted(session, request, result);
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                openFlash();
                try {
                    Thread.sleep(200);
//                    mCameraCaptureSession.stopRepeating();//停止请求，捕获照片
                    mCameraCaptureSession.capture(mCameraBuilder.build(), mCaptureCallBack, mCameraHandler);
                } catch (InterruptedException | CameraAccessException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(200);
                    closeFlash();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        //6.2 获取图像的缓冲区
        //6.3 换取文件的存储权限及操作
    }

    //开始录像
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void video() {
        Log.d("jjb", "video: 11111111");
        if (!loading) {
            try {
                videoTime = System.currentTimeMillis();
                Log.d(TAG, "video: oldtime = " + oldTime);
                loading = !loading;
                time.setVisibility(View.VISIBLE);
                btn_video.setBackgroundDrawable(getResources().getDrawable(R.drawable.videoing));
                time.setBase(SystemClock.elapsedRealtime());//计时器清零
                time.setTextColor(Color.parseColor("#FFFF0000"));
                time.start();
                flip.setVisibility(View.GONE);
                screenone.setVisibility(View.GONE);
                flash.setVisibility(View.GONE);
                scanBtn.setVisibility(View.GONE);
                picker.setVisibility(View.GONE);
                grid.setVisibility(View.GONE);
                hdr.setVisibility(View.GONE);
                meiyan.setVisibility(View.GONE);
                closePreviewSession();
                setUpMediaRecorder();
                SurfaceTexture texture = mPreviewView.getSurfaceTexture();
                assert texture != null;
                //texture.setDefaultBufferSize(mPreviewView.getWidth(),mPreviewView.getHeight());

                texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

                mCameraBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
                List<Surface> surfaces = new ArrayList<>();

                Surface previewsurface = new Surface(texture);
                surfaces.add(previewsurface);
                mCameraBuilder.addTarget(previewsurface);

                Surface recorderSurface = mMediaRecorder.getSurface();
                surfaces.add(recorderSurface);
                mCameraBuilder.addTarget(recorderSurface);
                setBuilder();
                mCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession session) {
                        mCameraCaptureSession = session;
                        try {
                            mCameraCaptureSession.setRepeatingRequest(mCameraBuilder.build(), null, mCameraHandler);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mMediaRecorder.start();
                            }
                        });
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    }
                }, mCameraHandler);
            } catch (CameraAccessException | IOException e) {
                e.printStackTrace();
            }
        } else {
            long nowTime = System.currentTimeMillis();
            Log.d(TAG, "video: nowtime" + nowTime+" video = " + videoTime);
            if (nowTime - videoTime >= 1000) {
                loading = !loading;
                time.setVisibility(View.GONE);
                btn_video.setBackgroundDrawable(getResources().getDrawable(R.drawable.video));
                time.stop();
                time.setBase(SystemClock.elapsedRealtime());//计时器清零
                time.setTextColor(Color.parseColor("#FFFFFFFF"));
                flip.setVisibility(View.VISIBLE);
                screenone.setVisibility(View.VISIBLE);
                flash.setVisibility(View.VISIBLE);
                scanBtn.setVisibility(View.VISIBLE);
                picker.setVisibility(View.VISIBLE);
                grid.setVisibility(View.VISIBLE);
                hdr.setVisibility(View.VISIBLE);
                meiyan.setVisibility(View.VISIBLE);
                mMediaRecorder.stop();
                mMediaRecorder.reset();
                mNextVideoAbsolutePath = null;
                startPreview();
                scannerImg();
                readImg.setImageBitmap(fristVideo(SCAN_PATH));
                zoomParams = 0;
                zoom(zoomParams);
                Log.d(TAG, "video: 123");
            }
        }
    }

    public void setBuilder() {
        mCameraBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoomRect);
        mCameraBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_OFF);
        mCameraBuilder.set(CaptureRequest.COLOR_CORRECTION_MODE, CaptureRequest.COLOR_CORRECTION_MODE_TRANSFORM_MATRIX);
        mCameraBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CaptureRequest.CONTROL_EFFECT_MODE_OFF);
        if (meiyan_srcoll.getVisibility() == View.VISIBLE) {
            mCameraBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, lv);
        }else {
            mCameraBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE,CaptureRequest.CONTROL_EFFECT_MODE_OFF);
        }
        if (major.getVisibility() == View.VISIBLE) {
            Log.d(TAG, "setBuilder: sda  da  = " + (btn_awb.getVisibility() == View.VISIBLE) );
//            mCameraBuilder.set(CaptureRequest.COLOR_CORRECTION_GAINS, this.colorTemperature(AWB));
            Log.d(TAG, "setBuilder: 12313123");
        }else {
            mCameraBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO);
            Log.d(TAG, "setBuilder: 111111");
        }
        mCameraBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, new MeteringRectangle[]{new MeteringRectangle(focusRect, 1000)});
        mCameraBuilder.set(CaptureRequest.CONTROL_AE_REGIONS, new MeteringRectangle[]{new MeteringRectangle(focusRect, 1000)});
        mCameraBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
        mCameraBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
        mCameraBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START);
    }
    public void setmCaptureRequestBuilder(){
        mCaptureRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoomRect);
        mCaptureRequestBuilder.set(CaptureRequest.COLOR_CORRECTION_MODE, CaptureRequest.COLOR_CORRECTION_MODE_TRANSFORM_MATRIX);
        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE,CaptureRequest.CONTROL_EFFECT_MODE_OFF);
        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO);
        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, new MeteringRectangle[]{new MeteringRectangle(focusRect, 1000)});
        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AE_REGIONS, new MeteringRectangle[]{new MeteringRectangle(focusRect, 1000)});
        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START);
        try {
            mCameraCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setUpMediaRecorder() throws IOException {
        Log.d(TAG, "setUpMediaRecorder: MediaRecorder.AudioSource.MIC = " + MediaRecorder.AudioSource.MIC);
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC); //设置用于录制的音源
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);//开始捕捉和编码数据到setOutputFile（指定的文件）
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); //设置在录制过程中产生的输出文件的格式
        if (mNextVideoAbsolutePath == null || mNextVideoAbsolutePath.isEmpty()) {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            mNextVideoAbsolutePath = Environment.getExternalStorageDirectory() + "/DCIM/CameraV2/" + timestamp + "_VIDEO.mp4";
        }
        mMediaRecorder.setOutputFile(mNextVideoAbsolutePath);//设置输出文件的路径
        mMediaRecorder.setVideoEncodingBitRate(10000000);//设置录制的视频编码比特率
        mMediaRecorder.setVideoFrameRate(25);//设置要捕获的视频帧速率
        mMediaRecorder.setVideoSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());//设置要捕获的视频的宽度和高度
        //mMediaRecorder.setVideoSize(1560,720);
        Log.d(TAG, "setUpMediaRecorder: mPreviewSize.getWidth = " + mPreviewSize.getWidth() + "mPreviewSize.getHeight = " + mPreviewSize.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);//设置视频编码器，用于录制
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);//设置audio的编码格式
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        Log.d(TAG, "setUpMediaRecorder: " + rotation);
        // mMediaRecorder.setOrientationHint(90);

        switch (rotation) {
            case 0:
                mMediaRecorder.setOrientationHint(90);
                break;
            case 1:
                mMediaRecorder.setOrientationHint(0);
                break;
            case 3:
                mMediaRecorder.setOrientationHint(180);
                break;
            default:
                break;
        }
        if (!isback) {
            mMediaRecorder.setOrientationHint(270);
        }
        mMediaRecorder.prepare();
    }

    private void closePreviewSession() {
        if (mCameraCaptureSession != null) {
            try {
                mCameraCaptureSession.stopRepeating();
                mCameraCaptureSession.close();
                mCameraCaptureSession = null;
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void unLockFocus() {
        try {
            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            mCameraCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, mCameraHandler);//恢复预览
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    //6.3 建立ImageReader，准备存储照片
    private void setupImageReader() {
        mImageReader = ImageReader.newInstance(mCaptureSize.getWidth(), mCaptureSize.getHeight(), ImageFormat.JPEG, 2);
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                mCameraHandler.post(new ImageSaver(reader.acquireNextImage()));//开启保存图片的线程 得到下一张照片
            }
        }, mCameraHandler);
    }

    //7 存储照片
    private class ImageSaver implements Runnable {
        Image mImage;

        public ImageSaver(Image image) {
            mImage = image;
        }//将传入的下一张照片保存

        @Override
        public void run() {
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();//图片的像素矩阵
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);

            String path = Environment.getExternalStorageDirectory() + "/DCIM/CameraV2/";
            Log.d(TAG, "run: path = " + path);
            File mImageFile = new File(path);
            if (!mImageFile.exists()) {
                mImageFile.mkdir();
            }

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = path + timestamp + "_IMG.jpg";
            AddPictureToAlbum(timestamp + "_IMG.jpg", data);
            //Log.d("jjb", "run: saved"+timestamp);
            Log.d(TAG, "run: fileName = " + fileName);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    readImg(fileName);
                }
            });
            mImage.close();
//            try {
//                FileOutputStream fos = new FileOutputStream(fileName);
//                fos.write(data,0,data.length);
//
//            } catch (FileNotFoundException e) {
//                Log.d(TAG, "run: 11");
//                e.printStackTrace();
//            } catch (IOException e) {
//                Log.d(TAG, "run: 22");
//                e.printStackTrace();
//            }

        }
    }

    public void AddPictureToAlbum(String fileName, byte[] data) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_DCIM + "/CameraV2/");
        }
        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        OutputStream outputStream = null;
        Log.d(TAG, "AddPictureToAlbum: uri = " + uri);
        try {
            outputStream = getContentResolver().openOutputStream(uri);
            outputStream.write(data, 0, data.length);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public boolean isScreenOriatonPortrait() {
        return this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    //切换闪光灯状态
    public void switchFlash(View view) {
        if (!isflash) {
            isflash = !isflash;
            flashAuto.setVisibility(View.VISIBLE);
            flashOn.setVisibility(View.VISIBLE);
            flashOff.setVisibility(View.VISIBLE);
            screenone.setVisibility(View.GONE);
            grid.setVisibility(View.GONE);
            hdr.setVisibility(View.GONE);
            meiyan.setVisibility(View.GONE);
            flashAuto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    flash.setBackgroundDrawable(getResources().getDrawable(R.drawable.flashauto));
                    flashColor(flashAuto);
                    isflash = !isflash;
                    flashState = "flashAuto";
                    saveFlashSet(flashState);
                }
            });
            flashOn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    flash.setBackgroundDrawable(getResources().getDrawable(R.drawable.flashon));
                    flashColor(flashOn);
                    isflash = !isflash;
                    flashState = "flashOn";
                    saveFlashSet(flashState);
                }
            });
            flashOff.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    flash.setBackgroundDrawable(getResources().getDrawable(R.drawable.flashoff));
                    flashColor(flashOff);
                    isflash = !isflash;
                    flashState = "flashOff";
                    saveFlashSet(flashState);
                }
            });
        } else {
            isflash = !isflash;
            flashAuto.setVisibility(View.GONE);
            flashOn.setVisibility(View.GONE);
            flashOff.setVisibility(View.GONE);
            screenone.setVisibility(View.VISIBLE);
            grid.setVisibility(View.VISIBLE);
            hdr.setVisibility(View.VISIBLE);
            meiyan.setVisibility(View.VISIBLE);
        }
    }

    //从相册中读取图片，形成缩略图
    public void readImg(String name) {
        Log.d(TAG, "readImg: start-------------");
        try {
            if (name != null) {
                Matrix matrix = new Matrix();
                if (isback) {
                    matrix.postRotate(90);
                } else {
                    matrix.postRotate(270);
                }

                Log.d(TAG, "readImg: name = " + name);
                //FileInputStream fs = new FileInputStream(Environment.getExternalStorageDirectory()+"/DCIM/CameraV2/IMG_20210813_092706.jpg");
                FileInputStream fs = new FileInputStream(name);
                Bitmap bitmap = BitmapFactory.decodeStream(fs);
                Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                resizedBitmap = resizedBitmap.createBitmap(resizedBitmap, 0, (resizedBitmap.getHeight() - resizedBitmap.getWidth()) / 2, resizedBitmap.getWidth(), resizedBitmap.getWidth());
                readImg.setImageBitmap(resizedBitmap);
                scannerImg();
                Log.d(TAG, "readImg: success------------- ");
            } else {
                readImg.setBackground(new ColorDrawable(Color.parseColor("#FF000000")));
            }
        } catch (FileNotFoundException e) {
            Log.d(TAG, "readImg: false--------------");
            e.printStackTrace();
        }
    }

    //android 获取视频第一帧作为缩略图
    public static Bitmap fristVideo(String filePath) {
        Log.d(TAG, "fristVideo: success");
        if (filePath != null) {
            Log.d(TAG, "fristVideo: filePath !!!!!!!!!!!!!!!");
            readImg.setColorFilter(Color.parseColor("#00000000"));
            Bitmap bitmap = null;
            bitmap = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Images.Thumbnails.MICRO_KIND);
            if (bitmap != null) {
                bitmap = ThumbnailUtils.extractThumbnail(bitmap, 50, 50);
            }
            return bitmap;
        } else {
            Log.d(TAG, "fristVideo: filePath = null");
            readImg.setColorFilter(Color.BLACK);
        }
        return null;
    }

    public void flashColor(TextView flash) {
        flashsta = new TextView[]{flashAuto, flashOn, flashOff};
        for (int i = 0; i < flashsta.length; i++)
            flashsta[i].setBackground(new ColorDrawable(Color.parseColor("#00000000")));
        flash.setBackground(new ColorDrawable(Color.parseColor("#FF3700B3")));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                flashAuto.setVisibility(View.GONE);
                flashOn.setVisibility(View.GONE);
                flashOff.setVisibility(View.GONE);
                screenone.setVisibility(View.VISIBLE);
                grid.setVisibility(View.VISIBLE);
                hdr.setVisibility(View.VISIBLE);
                meiyan.setVisibility(View.VISIBLE);
            }
        });
    }

    public void saveFlashSet(String flashState) {
        SharedPreferences userInfo = getSharedPreferences("file", MODE_PRIVATE);
        SharedPreferences.Editor editor = userInfo.edit();
        editor.putString("flashState", flashState);
        editor.commit();
        Log.d(TAG, "saveFlashSet: flashState = " + flashState);
    }

    public String getFlashState() {
        SharedPreferences sp = getSharedPreferences("file", Context.MODE_PRIVATE);
        Log.d(TAG, "getFlashState: " + sp.getString("flashState", "flashOff"));
        return sp.getString("flashState", "flashOff");
    }

    public void setFlashSet() {
        flashState = getFlashState();
        Log.d(TAG, "setFlashSet: flashState = " + flashState);
        if (flashState.equals("flashAuto")) {
            flash.setBackgroundDrawable(getResources().getDrawable(R.drawable.flashauto));
            flashColor(flashAuto);
            Log.d(TAG, "setFlashSet: flashon11");
        } else if (flashState.equals("flashOn")) {
            flash.setBackgroundDrawable(getResources().getDrawable(R.drawable.flashon));
            flashColor(flashOn);
            Log.d(TAG, "setFlashSet: flashOn22");
        } else if (flashState.equals("flashOff")) {
            flash.setBackgroundDrawable(getResources().getDrawable(R.drawable.flashoff));
            flashColor(flashOff);
            Log.d(TAG, "setFlashSet: flashOff333");
        }
    }

    private void seekGone(SeekBar seekbar) {
        seekLayout.setVisibility(View.VISIBLE);
        seekTag.setVisibility(View.VISIBLE);
        seekbar_ev.setVisibility(View.GONE);
        seekbar_focus.setVisibility(View.GONE);
        seekbar_zoom.setVisibility(View.GONE);
        seekbar_awb.setVisibility(View.GONE);
        seekbar.setVisibility(View.VISIBLE);
    }

    public void focusing(int progress) {

        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
        //float hyperfocal = mCharacteristics.get(CameraCharacteristics.LENS_INFO_FOCUS_DISTANCE_CALIBRATION);
        float[] hy = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
        float min = characteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
        Log.d(TAG, "focusing: min = " + min);
        float num = ((float) (10 - progress / 10));
//        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
//        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
        mCaptureRequestBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, num);

        try {
            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
            mCameraCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            Log.e(TAG, "focusing: e = " + e);
        }
    }

    public void zoom(int i) {
        zoomMag.setVisibility(View.VISIBLE);
        double data1 = i;
        double data2 = data1 / 60 + 1;
        String data = String.format("%.1f", data2);
        zoomMag.setText(data + "x");
        Log.d(TAG, "zoom: data = " + i + "          " + data2);

        Rect rect = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);

        Log.d(TAG, "zoom   onProgressChanged: rect = " + rect);
        int radio = characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM).intValue() / 2;
        int centerX = rect.centerX();
        int centerY = rect.centerY();
        zoomRect = new Rect((i * centerX / radio) / 100, (i * centerY / radio) / 100, rect.right - ((i * centerX) / 100 / radio) - 1, rect.bottom - ((i * centerY) / 100 / radio) - 1);
        if (i == 0) zoomRect = new Rect(rect.left, rect.top, rect.right, rect.bottom);
        mCaptureRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoomRect);
        Log.d(TAG, "onProgressChanged: 放大 ：=" + i);
        try {
            mCameraCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void tapFocus(MotionEvent e) {
        double x = e.getX(), y = e.getY(), tmp;
        int left = (int) e.getX(), top = (int) e.getY();
        setTranslate(left, top);
        setScale();
        Log.d(TAG, "onSingleTapUp: " + left + "   " + top);

        int rotaion = getWindowManager().getDefaultDisplay().getRotation();
        int realPreviewWidth = mPreviewSize.getWidth(), realPreviewHeight = mPreviewSize.getHeight();
        Log.d(TAG, "tapFocus: realwidth = " + realPreviewWidth + "   realheight = " + realPreviewHeight);
        if (90 == rotaion || 270 == rotaion) {
            realPreviewWidth = mPreviewSize.getHeight();
            realPreviewHeight = mPreviewSize.getWidth();
        }
        Log.d(TAG, "tapFocus2222: realwidth = " + realPreviewWidth + "   realheight = " + realPreviewHeight);

        // 计算摄像头取出的图像相对于view放大了多少，以及有多少偏移
        double imgScale = 1.0, verticalOffset = 0, horizontalOffset = 0;
        if (realPreviewHeight * mPreviewView.getWidth() > realPreviewWidth * mPreviewView.getHeight()) {
            imgScale = mPreviewView.getWidth() * 1.0 / realPreviewWidth;
            verticalOffset = (realPreviewHeight - mPreviewView.getHeight() / imgScale) / 2;
            Log.d(TAG, "onSingleTapUp111: " + realPreviewHeight + "     " + mPreviewView.getHeight());
        } else {
            imgScale = mPreviewView.getHeight() * 1.0 / realPreviewHeight;
            horizontalOffset = (realPreviewWidth - mPreviewView.getWidth() / imgScale) / 2;
            Log.d(TAG, "tapFocus: mpreview = " + realPreviewWidth + "   " + mPreviewView.getWidth());
        }
        Log.d(TAG, "tapFocus: imgscale = " + imgScale + "  ver = " + verticalOffset + "  hor = " + horizontalOffset);

        // 将点击的坐标转换为图像上的坐标
        x = x / imgScale + horizontalOffset;
        y = y / imgScale + verticalOffset;
        if (90 == rotaion) {
            tmp = x;
            x = y;
            y = mPreviewSize.getHeight() - tmp;
        } else if (270 == rotaion) {
            tmp = x;
            x = mPreviewSize.getWidth() - y;
            y = tmp;
        }

        Rect cropRegion = mCaptureRequest.get(CaptureRequest.SCALER_CROP_REGION);
        Log.d(TAG, "tapFocus: crop = " + cropRegion);
        int cropWidth = cropRegion.width(), cropHeight = cropRegion.height();
        if (mPreviewSize.getHeight() * cropWidth > mPreviewSize.getWidth() * cropHeight) {
            imgScale = cropHeight * 1.0 / mPreviewSize.getHeight();
            verticalOffset = 0;
            horizontalOffset = (cropWidth - imgScale * mPreviewSize.getWidth()) / 2;
        } else {
            imgScale = cropWidth * 1.0 / mPreviewSize.getWidth();
            horizontalOffset = 0;
            verticalOffset = (cropHeight - imgScale * mPreviewSize.getHeight()) / 2;
        }
        Log.d(TAG, "tapFocus: imgscale = " + imgScale + "  ver = " + verticalOffset + "  hor = " + horizontalOffset);
        // 将点击区域相对于图像的坐标，转化为相对于成像区域的坐标
        x = x * imgScale + horizontalOffset + cropRegion.left;
        y = y * imgScale + verticalOffset + cropRegion.top;

        double tapAreaRatio = 0.1;
        focusRect.left = clamp((int) (x - tapAreaRatio / 2 * cropRegion.width()), 0, cropRegion.width());
        focusRect.right = clamp((int) (x + tapAreaRatio / 2 * cropRegion.width()), 0, cropRegion.width());
        focusRect.top = clamp((int) (y - tapAreaRatio / 2 * cropRegion.height()), 0, cropRegion.height());
        focusRect.bottom = clamp((int) (y + tapAreaRatio / 2 * cropRegion.height()), 0, cropRegion.height());

        setFocus(focusRect);
    }

    public void setFocus(Rect rect) {
        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, new MeteringRectangle[]{new MeteringRectangle(rect, 1000)});
        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AE_REGIONS, new MeteringRectangle[]{new MeteringRectangle(rect, 1000)});
        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START);
        try {
            mCameraCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, mCameraHandler);
        } catch (CameraAccessException e) {
            Log.e(TAG, "setRepeatingRequest failed, " + e.getMessage());
        }
    }

    //取消手动聚焦效果
    public void reFocus(int afState) {
        if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState
                || CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO);
            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);

            try {
                mCameraCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, mCameraHandler);
            } catch (CameraAccessException e) {
                Log.e(TAG, "setRepeatingRequest failed, errMsg: " + e.getMessage());
            }
        }
    }

    public void setScale() {
        AnimationSet animationSet = new AnimationSet(true);
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.1f, 1, 1.1f, 1,
                RELATIVE_TO_PARENT, 0.5f, RELATIVE_TO_PARENT, 0.5f);
        scaleAnimation.setDuration(300);
        animationSet.addAnimation(scaleAnimation);
        focusImg.startAnimation(animationSet);
    }

    public void setTranslate(int x, int y) {
        focusImg.setVisibility(View.VISIBLE);
        TranslateAnimation ta = new TranslateAnimation(0, 0, 0, 0);
        ta.setDuration(0);
        ta.setFillAfter(true);
        focusImg.setTranslationX(x - 80 + (phoneWidth - mPreviewView.getWidth()) / 2);
        Log.d(TAG, "setTranslate: " + mPreviewView.getHeight() + "    " + mPreviewSize.getHeight());
        focusImg.setTranslationY(y + 100 + (phoneHeight - mPreviewView.getHeight()) / 2);

        focusImg.startAnimation(ta);
        Log.d(TAG, "setTranslate: x = " + x + " , y = " + y);
    }


    public void setEV(int ev) {
        ev = (0 - ev);
        Log.d(TAG, "setEV: ev = " + ev);
        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, ev);
        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
        try {
            mCameraCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            Log.e(TAG, "focusing: e = " + e);
        }
    }

    public void setAWB(int awb) {
        AWB = awb;
        Log.d(TAG, "setAWB: awb = " + awb);
        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_OFF);
        // adjust color correction using seekbar's params
        mCaptureRequestBuilder.set(CaptureRequest.COLOR_CORRECTION_MODE, CaptureRequest.COLOR_CORRECTION_MODE_TRANSFORM_MATRIX);
        mCaptureRequestBuilder.set(CaptureRequest.COLOR_CORRECTION_GAINS, this.colorTemperature(awb));

        try {
            mCameraCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            Log.e(TAG, "focusing: e = " + e);
        }
    }

    public static RggbChannelVector colorTemperature(int WhiteBalanceValue) {
        return new RggbChannelVector(0.635f + (0.0208333f * WhiteBalanceValue),
                1.0f, 1.0f, 3.7420394f + (-0.0287829f * WhiteBalanceValue));
    }

    private void meiyanPicker() {
        final HorizontalPickerView picker = (HorizontalPickerView) findViewById(R.id.meiyanPicker);

        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            list.add(i);
        }

        final ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this, 0, list) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                int value = getItem(position);
                Log.d(TAG, "getView: value = " + value);
                if (convertView == null) {
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    convertView = inflater.inflate(android.R.layout.simple_list_item_1, null);
                }
                TextView view = (TextView) convertView.findViewById(android.R.id.text1);
                if (value == 0) {
                    view.setText("蓝调");
                } else if (value == 1) {
                    view.setText("黑板");
                } else if (value == 2) {
                    view.setText("白板");
                } else if (value == 3) {
                    view.setText("色调分离");
                } else if (value == 4) {
                    view.setText("乌贼墨");
                } else if (value == 5) {
                    view.setText("日晒");
                } else if (value == 6) {
                    view.setText("负片");
                } else if (value == 7) {
                    view.setText("单色");
                }
                //view.setText("第"+value+"个");
                return convertView;

            }
        };

        picker.setAdapter(adapter);

        picker.setOnSelectedListener(new HorizontalPickerView.OnSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void selected(View v, int index) {
                ViewGroup group = (ViewGroup) picker.getChildAt(0);
                for (int i = 0; i < adapter.getCount(); i++) {
                    View view = group.getChildAt(i);
                    TextView textview = (TextView) view;
                    if (i == index) {
                        textview.setTextColor(0xfffcaf17);
                        lv = 8 - i;
                        switch (i) {
                            case 0:
//                                mCaptureRequestBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE,CaptureRequest.CONTROL_EFFECT_MODE_AQUA);
                                setlvjing(0);
//                                setEV(0);
//                                setAWB(65);
                                break;
                            case 1:
//                                mCaptureRequestBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE,CaptureRequest.CONTROL_EFFECT_MODE_BLACKBOARD);
                                setlvjing(1);
//                                setEV(-15);
//                                setAWB(65);
                                break;
                            case 2:
//                                mCaptureRequestBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE,CaptureRequest.CONTROL_EFFECT_MODE_WHITEBOARD);
                                setlvjing(2);
//                                setEV(-10);
//                                setAWB(80);
                                break;
                            case 3:
//                                mCaptureRequestBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE,CaptureRequest.CONTROL_EFFECT_MODE_POSTERIZE);
                                setlvjing(3);
//                                setEV(-10);
//                                setAWB(50);
                                break;
                            case 4:
//                                mCaptureRequestBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE,CaptureRequest.CONTROL_EFFECT_MODE_SEPIA);
                                setlvjing(4);
//                                setEV(0);
//                                setAWB(65);
                                break;
                            case 5:
//                                mCaptureRequestBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE,CaptureRequest.CONTROL_EFFECT_MODE_SOLARIZE);
                                setlvjing(5);
//                                setEV(0);
//                                setAWB(80);
                                break;
                            case 6:
//                                mCaptureRequestBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE,CaptureRequest.CONTROL_EFFECT_MODE_NEGATIVE);
                                setlvjing(6);
//                                setEV(0);
//                                setAWB(50);
                                break;
                            case 7:
//                                mCaptureRequestBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE,CaptureRequest.CONTROL_EFFECT_MODE_MONO);
                                setlvjing(7);
                            default:
                                break;
                        }

                    } else {
                        view.setBackgroundColor(group.getDrawingCacheBackgroundColor());
                        textview.setTextColor(0xFFFFFFFF);
                    }
                }
            }
        });
    }

    public void setlvjing(int i) {
        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, 8 - i);
        try {
            mCameraCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        Log.d(TAG, "onProgressChanged: 123");
        if (seekBar.getId() == R.id.seekbar_zoom) {
            zoom(progress * 3);
        } else if (seekBar.getId() == R.id.seekbar_focus) {
            focusing(progress);
        } else if (seekBar.getId() == R.id.seekbar_ev) {
            setEV(50 - progress);
        } else if (seekBar.getId() == R.id.seekbar_awb) {
            setAWB(progress + 15);
            Log.d(TAG, "onProgressChanged: progress = " + progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private static float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setZoom(boolean isZoomIn) {
        if (isZoomIn && zoomParams < 50)
            zoomParams++;
        else if (zoomParams > 0)
            zoomParams--;
        seekbar_zoom.setProgress(zoomParams * 2, true);
        zoom(zoomParams * 6);
    }

    @SuppressLint({"NonConstantResourceId", "ClickableViewAccessibility"})
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()){
            case R.id.video:
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN){
                    video();
                    Log.d(TAG, "onTouch: video   13212313212");
                }
                break;
            case R.id.flip:
                Log.d(TAG, "onTouch: flip   13212313212");
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN){
                    flip();
                    Log.d(TAG, "onTouch: flip   13212313212");
                }
                break;
        }
        int pointerCount = event.getPointerCount();
        Log.d(TAG, "onTouch: getPointerCount = " + pointerCount);
        if (pointerCount == 1) {
            Log.d(TAG, "onTouch: pointerCount == 1 ");
        } else {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_POINTER_DOWN:
                    oldDistance = getFingerSpacing(event);
                    Log.d(TAG, "onTouch: ACTION_POINTER_DOWN oldDistance = " + oldDistance);
                    break;
                case MotionEvent.ACTION_MOVE:
                    float newDistance = getFingerSpacing(event);
                    if (newDistance > oldDistance && (newDistance - oldDistance) > 5) {
                        setZoom(true);
                    } else if (newDistance < oldDistance && (oldDistance - newDistance) > 5) {
                        setZoom(false);
                    }
                    oldDistance = newDistance;
                    break;
            }
        }
        return detector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        Log.d(TAG, "onDown: 111 ");
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        Log.d(TAG, "onShowPress: 111");
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        Log.d(TAG, "onSingleTapUp: 111");
        //tapFocus(e);
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        Log.d(TAG, "onScroll: 1111");
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        Log.d(TAG, "onLongPress: 111");
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Log.d(TAG, "onFling: 1111");
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        Log.d(TAG, "onSingleTapConfirmed: 111");
        float y = e.getY();
        if (y >= 100 && y <= phoneHeight - 190){
            tapFocus(e);
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onDoubleTap(MotionEvent e) {
        Log.d(TAG, "onDoubleTap: 111");

        //flip();
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }


}