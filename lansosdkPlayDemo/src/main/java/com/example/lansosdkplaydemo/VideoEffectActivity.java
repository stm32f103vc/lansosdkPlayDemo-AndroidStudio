/*****************************************************************************
 * VideoPlayerActivity.java
 *****************************************************************************/

package com.example.lansosdkplaydemo;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.Presentation;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaRouter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.LanSoSdk.Play.AndroidVersion;
import com.LanSoSdk.Play.Media;
import com.LanSoSdk.Play.MediaPlayer;
import com.example.lansosdk.util.AndroidDevices;
import com.example.lansosdk.util.LanSoDemoApplication;
import com.example.lansosdk.util.SlidingLayer;


import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


import android.view.View.OnLayoutChangeListener;
import android.view.View.OnSystemUiVisibilityChangeListener;


public class VideoEffectActivity extends Activity implements OnSeekBarChangeListener,OnCheckedChangeListener, OnClickListener,MediaPlayer.onVideoSizeChangedListener,MediaPlayer.onHardwareAccelerationErrorListener,SurfaceHolder.Callback {

    public final static String TAG = "VideoPlayerActivity";

    
    public final static String PLAY_LOCATION = "item_location";
    public final static String PLAY_IS_SOFTWARE_CODEC = "is_software_codec";
    
    private MediaPlayer mMediaPlayer;
    private SurfaceView mSurfaceView = null;
	private SurfaceHolder holder;
    private FrameLayout mSurfaceFrame;
    
    
    private Uri mUri;
    private SeekBar mSeekbar;
    private TextView mTime;
    private TextView mLength;
    
    private boolean mCanSeek;



    private static final int SURFACE_BEST_FIT = 0;
    private static final int SURFACE_FIT_HORIZONTAL = 1;
    private static final int SURFACE_FIT_VERTICAL = 2;
    private static final int SURFACE_FILL = 3;
    private static final int SURFACE_16_9 = 4;
    private static final int SURFACE_4_3 = 5;
    private static final int SURFACE_ORIGINAL = 6;
    private int mCurrentSize = SURFACE_BEST_FIT;

   
    private View mOverlayProgress;
    private View mOverlayButtons;
    private static final int OVERLAY_TIMEOUT = 4000;
    private static final int OVERLAY_INFINITE = -1;
    private static final int FADE_OUT = 1;
    private static final int FADE_OUT_INFO = 3;
    private static final int RESET_BACK_LOCK = 6;

    private boolean mDragging;
    private boolean mShowing;
    private int mUiVisibility = -1;
    private TextView mInfo;
    private View mVerticalBar;
    private View mVerticalBarProgress;
    private boolean mEnableBrightnessGesture;
    private boolean mDisplayRemainingTime = false;
    private int mScreenOrientation;
    private int mScreenOrientationLock;
    
    
    private Button mLock;
    private Button mSize;
    private Button mPlayPause;
    private Button  mEdit;
    
    
    private boolean mIsLocked = false;
    /* -1 is a valid track (Disable) */
    private int mLastAudioTrack = -2;
    private int mLastSpuTrack = -2;
    private int mOverlayTimeout = 0;
    private boolean mLockBackButton = false;

    /**
     * For uninterrupted switching between audio and video mode
     */
    private boolean mEndReached;
    private boolean mHasSubItems = false;

    // Playlist
    private int savedIndexPosition = -1;

    // size of the video
    private int mVideoHeight;
    private int mVideoWidth;
    private int mVideoVisibleHeight;
    private int mVideoVisibleWidth;
    private int mSarNum;
    private int mSarDen;

    //Volume
    private AudioManager mAudioManager;
    private int mAudioMax;
    private boolean mMute = false;
    private int mVolSave;
    private float mVol;

    //Touch Events
    private static final int TOUCH_NONE = 0;
    private static final int TOUCH_VOLUME = 1;
    private static final int TOUCH_BRIGHTNESS = 2;
    private static final int TOUCH_SEEK = 3;
    private int mTouchAction = TOUCH_NONE;
    private int mSurfaceYDisplayRange;
    private float mInitTouchY, mTouchY =-1f, mTouchX=-1f;

    //stick event
    private static final int JOYSTICK_INPUT_DELAY = 300;
    private long mLastMove;

    // Brightness
    private boolean mIsFirstBrightnessGesture = true;
    private float mRestoreAutoBrightness = -1f;

    private boolean mPlaybackStarted = false;

    private int mMenuIdx = -1;
    private boolean mIsNavMenu = false;

    /* for getTime and seek */
    private long mForcedTime = -1;
    private long mLastTime = -1;

    private OnLayoutChangeListener mOnLayoutChangeListener;
    private AlertDialog mAlertDialog;
    
    private static final int SHOW_PROGRESS = 201;
    private static final int HW_ERROR = 202; 
    
    SlidingLayer slidelayerEffect;
	SlidingLayer  slidelayerAdjust;
	SeekBar    seekbarContrast,seekbarBrightness,seekbarHue,seekbarSaturation,seekbarGamma;
	SeekBar   seekbarExtractRed,seekbarExtractGreen,seekbarExtractBlue,seekbarSepia;
	
	private static final int CONTRAST_MAX=200; //x100;
	private static final int HUE_MAX=600; //x100;
	private static final int GAMMA_MAX=1000; //x100;
	private static final int BRIGHTNESS_MAX=200; //x100;
	private static final int SATURATION_MAX=300; //x100;
	
	private static final int SEPIA_MAX=255;
	
	private static final int EXTRACT_MAX=255;
	
	private int extractValue=0;
	 private boolean isSwCodec;
    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.player_effect);
        
        slidelayerEffect=(SlidingLayer)findViewById(R.id.id_play_slideinlayer_effect);
	   
	   slidelayerEffect.setStickTo(SlidingLayer.STICK_TO_RIGHT);
	   slidelayerEffect.setCloseOnTapEnabled(false);
	   slidelayerEffect.setShadowWidth(0);
	   slidelayerEffect.setShadowDrawable(null);
	 
	   slidelayerAdjust=(SlidingLayer)findViewById(R.id.id_play_slideinlayer_adjust);
	   slidelayerAdjust.setStickTo(SlidingLayer.STICK_TO_RIGHT);
	   slidelayerAdjust.setCloseOnTapEnabled(false);
	   slidelayerAdjust.setShadowWidth(0);
	   slidelayerAdjust.setShadowDrawable(null);
		   
        
        mTime = (TextView) findViewById(R.id.player_overlay_time);
        mLength = (TextView) findViewById(R.id.player_overlay_length);
        mSeekbar = (SeekBar) findViewById(R.id.player_overlay_seekbar);
        
        mPlayPause = (Button) findViewById(R.id.player_overlay_play);
        mSize = (Button) findViewById(R.id.player_overlay_size);
        mLock = (Button) findViewById(R.id.lock_overlay_button);
        mEdit =(Button)findViewById(R.id.id_player_editvideo);
        
        mSurfaceView = (SurfaceView) findViewById(R.id.player_surface);
        mSurfaceFrame = (FrameLayout) findViewById(R.id.player_surface_frame);


        mOverlayProgress = findViewById(R.id.progress_overlay);
        
        mOverlayButtons =  findViewById(R.id.player_overlay_buttons);
        
        mTime = (TextView) findViewById(R.id.player_overlay_time);
        mLength = (TextView) findViewById(R.id.player_overlay_length);

        // the info textView is not on the overlay
        mInfo = (TextView) findViewById(R.id.player_overlay_textinfo);
        mVerticalBar = findViewById(R.id.verticalbar);
        mVerticalBarProgress = findViewById(R.id.verticalbar_progress);
        
        findViewById(R.id.id_play_effect_palette).setOnClickListener(this);
        mEdit.setOnClickListener(this);
        
        initCheckBoxView(R.id.id_play_effect_mirror_ckx);
        initCheckBoxView(R.id.id_play_effect_psychedelic_ckx);
        initCheckBoxView(R.id.id_play_effect_wave_ckx);
        initCheckBoxView(R.id.id_play_effect_motiondetect_ckx);
        initCheckBoxView(R.id.id_play_effect_fugu_ckx);
        initCheckBoxView(R.id.id_play_effect_oldmovie_ckx);

        initCheckBoxView(R.id.id_play_adjust_adjustenable_ckx);
        
        initCheckBoxView(R.id.id_play_adjust_extractcolor_ckx);
        initCheckBoxView(R.id.id_play_adjust_sepiaenable_ckx);
        
        
        seekbarContrast=(SeekBar)findViewById(R.id.id_adjust_contrast_seekbar);
        seekbarHue=(SeekBar)findViewById(R.id.id_adjust_hue_seekbar);
        seekbarSaturation=(SeekBar)findViewById(R.id.id_adjust_saturation_seekbar);
        seekbarBrightness=(SeekBar)findViewById(R.id.id_adjust_brightness_seekbar);
        seekbarGamma=(SeekBar)findViewById(R.id.id_adjust_gamma_seekbar);
        
        
        seekbarContrast.setMax(CONTRAST_MAX);
        seekbarHue.setMax(HUE_MAX);
        seekbarSaturation.setMax(SATURATION_MAX);
        seekbarBrightness.setMax(BRIGHTNESS_MAX);
        seekbarGamma.setMax(GAMMA_MAX);

        seekbarContrast.setOnSeekBarChangeListener(this);
        seekbarHue.setOnSeekBarChangeListener(this);
        seekbarSaturation.setOnSeekBarChangeListener(this);
        seekbarBrightness.setOnSeekBarChangeListener(this);
        seekbarGamma.setOnSeekBarChangeListener(this);
   
        setAdjustItemEnable(false);
        
        
        seekbarExtractRed=(SeekBar)findViewById(R.id.id_adjust_extract_red_seekbar);
        seekbarExtractGreen=(SeekBar)findViewById(R.id.id_adjust_extract_green_seekbar);
        seekbarExtractBlue=(SeekBar)findViewById(R.id.id_adjust_extract_blue_seekbar);
        
        seekbarExtractRed.setEnabled(false);
        seekbarExtractGreen.setEnabled(false);
        seekbarExtractBlue.setEnabled(false);
        
        seekbarExtractRed.setMax(EXTRACT_MAX);
        seekbarExtractGreen.setMax(EXTRACT_MAX);
        seekbarExtractBlue.setMax(EXTRACT_MAX);
        
        seekbarExtractRed.setOnSeekBarChangeListener(this);
        seekbarExtractGreen.setOnSeekBarChangeListener(this);
        seekbarExtractBlue.setOnSeekBarChangeListener(this);
        
        
        seekbarSepia=(SeekBar)findViewById(R.id.id_adjust_sepia_seekbar);
        seekbarSepia.setMax(SEPIA_MAX);
        seekbarSepia.setEnabled(false);
        seekbarSepia.setOnSeekBarChangeListener(this);
        
        holder=mSurfaceView.getHolder();
        holder.addCallback(this);

        
        mSeekbar.setOnSeekBarChangeListener(mSeekListener);
        mPlayPause.setOnClickListener(mPlayPauseListener); 
        

        mAudioManager = (AudioManager) LanSoDemoApplication.getAppContext().getSystemService(AUDIO_SERVICE);
        mAudioMax = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

    
        RelativeLayout.LayoutParams layoutParams =
                (RelativeLayout.LayoutParams)mOverlayProgress.getLayoutParams();
        
        if (AndroidDevices.isPhone() || !AndroidDevices.hasNavBar()) {
            layoutParams.width = LayoutParams.MATCH_PARENT;
        } else {
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        }
        mOverlayProgress.setLayoutParams(layoutParams);
        

        dimStatusBar(false);

        mEndReached = false;

        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        setRequestedOrientation(mScreenOrientation != 100
                    ? mScreenOrientation
                    : getScreenOrientation());
        resetHudLayout();
        
        isSwCodec=getIntent().getBooleanExtra(PLAY_IS_SOFTWARE_CODEC, false);
        Log.i("sno",isSwCodec?"using SOFTWARE CODEC":"using FULL CODEC");
        
        mSeekbar.setOnSeekBarChangeListener(mSeekListener);
        mLock.setOnClickListener(mLockListener);
        mPlayPause.setOnClickListener(mPlayPauseListener);
        mSize.setOnClickListener(mSizeListener);
        
    }
    
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
    		int height) {
    	// TODO Auto-generated method stub
    	
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    	// TODO Auto-generated method stub
    	startPlayback(isSwCodec);
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    	// TODO Auto-generated method stub
    	stopPlayback();
    }
    private void initCheckBoxView(int resId)
    {
    	CheckBox  ckx=(CheckBox)findViewById(resId);
    	ckx.setOnCheckedChangeListener(this);
    }
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		if(fromUser==false)
			return ;
		
		float valuef=(float)progress;
		valuef/=100;
		switch (seekBar.getId()) 
		{
			case R.id.id_adjust_contrast_seekbar:
				 	mMediaPlayer.setContrastValue(valuef);
				break;
			case R.id.id_adjust_hue_seekbar:
				 	mMediaPlayer.setHueValue(valuef);
				break;
			case R.id.id_adjust_saturation_seekbar:
					mMediaPlayer.setSaturationValue(valuef);
				break;
			case R.id.id_adjust_brightness_seekbar:
					mMediaPlayer.setBrightnessValue(valuef);
				break;
			case R.id.id_adjust_gamma_seekbar:
					mMediaPlayer.setGammaValue(valuef);
				break;
			case R.id.id_adjust_extract_red_seekbar:
					extractValue&=0x00FFFF;
					extractValue|=(progress<<16);
					mMediaPlayer.setExtractValue(extractValue);
				break;
			case R.id.id_adjust_extract_green_seekbar:
				extractValue&=0xFF00FF;
				extractValue|=(progress<<8);
				mMediaPlayer.setExtractValue(extractValue);
				break;
			case R.id.id_adjust_extract_blue_seekbar:
				extractValue&=0xFFFF00;
				extractValue|=(progress);
				mMediaPlayer.setExtractValue(extractValue);
				break;
			case R.id.id_adjust_sepia_seekbar:
				mMediaPlayer.setSepiaValue(progress);
				break;
			default:
				break;
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}
	
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    	// TODO Auto-generated method stub
    	if(mMediaPlayer==null || mMediaPlayer.isPlaying()==false)
    		return ;
    	
    	switch (buttonView.getId()) 
    	{
			case R.id.id_play_effect_mirror_ckx:
				mMediaPlayer.setEnableMirror(isChecked);
				break;
			case R.id.id_play_effect_psychedelic_ckx:
				mMediaPlayer.setEnablePsychedelic(isChecked);
				break;
			case R.id.id_play_effect_wave_ckx:
				mMediaPlayer.setEnableWave(isChecked);
				break;
			case R.id.id_play_effect_motiondetect_ckx:
				mMediaPlayer.setEnableMotiondetect(isChecked);
				break;
			case R.id.id_play_effect_fugu_ckx:
				mMediaPlayer.setEnableSepia(isChecked);
				if(isChecked)
					mMediaPlayer.setSepiaValue(125);
				break;
			case R.id.id_play_effect_oldmovie_ckx:
				mMediaPlayer.setEnableExtract(isChecked);
				if(isChecked)
					mMediaPlayer.setExtractValue(0xFFFFFF);
				break;
			case R.id.id_play_adjust_adjustenable_ckx: //调色使能.
					mMediaPlayer.setEnableAdjuct(isChecked);
					setAdjustItemEnable(isChecked);
				break;
			case R.id.id_play_adjust_sepiaenable_ckx:
				mMediaPlayer.setEnableSepia(isChecked);
				seekbarSepia.setEnabled(isChecked);
				break;
			case R.id.id_play_adjust_extractcolor_ckx:
				mMediaPlayer.setEnableExtract(isChecked);
				seekbarExtractRed.setEnabled(isChecked);
				seekbarExtractGreen.setEnabled(isChecked);
				seekbarExtractBlue.setEnabled(isChecked);
				break;
		default:
			break;
		}
    }
    private void setAdjustItemEnable(boolean isenable)
    {
    	seekbarContrast.setEnabled(isenable);
    	seekbarHue.setEnabled(isenable);
    	seekbarBrightness.setEnabled(isenable);
    	seekbarSaturation.setEnabled(isenable);
    	seekbarGamma.setEnabled(isenable);
    }
    @Override
    public void onClick(View v) {
    	// TODO Auto-generated method stub
    	switch (v.getId()) {
    		case R.id.id_player_editvideo:
    			mEdit.setVisibility(View.GONE);
    			slidelayerEffect.openLayer(true);
    			break;
    		case R.id.id_play_effect_palette:
    			 slidelayerEffect.closeLayer(true);
    			 slidelayerAdjust.openLayer(true); 
    		break;
		default:
			break;
		}
    }
    
//    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
//    private void startPlayback() {
//
//        mUri = null;
//        Bundle extras = getIntent().getExtras();
//        mUri = extras.getParcelable(PLAY_LOCATION);
//
//        boolean isSwCodec=getIntent().getBooleanExtra(PLAY_IS_SOFTWARE_CODEC, false);
//        Log.i("sno",isSwCodec?"using SOFTWARE CODEC":"using FULL CODEC");
//        
//        mCanSeek = false;
//
//        if (mUri != null) 
//        {
//        	  	mMediaPlayer = new MediaPlayer(this);
//        	  	mMediaPlayer.setVideoView(mSurfaceView); 
//                    
//        	  	mMediaPlayer.setOnVideoSizeChangedListener(this);
//        	  	mSurfaceView.setKeepScreenOn(true);
//            	if(isSwCodec)
//            		mMediaPlayer.setDataSource(mUri,true);
//            	else
//            		mMediaPlayer.setDataSource(mUri);
//            	
//                mMediaPlayer.setEventListener(mMediaPlayerListener);
//                mMediaPlayer.setOnHardwareAccelerationErrorListener(this);
//                mMediaPlayer.play();
//        }
//    }
//    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
//    private void stopPlayback() {
//        mHandler.removeMessages(SHOW_PROGRESS);     
//    	mMediaPlayer.removeOnVideoSizeChangedListener();
//        mMediaPlayer.setEventListener(null);
//        mSurfaceView.setKeepScreenOn(false);
//        mMediaPlayer.stop();  
//        mMediaPlayer.release();
//    }
    private final MediaPlayer.EventListener mMediaPlayerListener = new MediaPlayer.EventListener() 
    {
        @Override
        public void onEvent(MediaPlayer.Event event) {
            switch (event.type) {
            	case MediaPlayer.Event.Buffering:
                    Log.i(TAG, "MediaPlayer.Event.Buffering========"+event.getBuffering());
                    break;
                case MediaPlayer.Event.Playing:
                    Log.i(TAG, "MediaPlayer.Event.Playing");
                    break;
                case MediaPlayer.Event.Paused:
                    Log.i(TAG, "MediaPlayer.Event.Paused");
                    break;
                case MediaPlayer.Event.Stopped:
                	 exitOK();
                    break;
                case MediaPlayer.Event.EndReached:
                    Log.i(TAG, "MediaPlayer.Event.EndReached");
                    endReached();
                    break;
                case MediaPlayer.Event.EncounteredError:
                    Log.i(TAG, "MediaPlayer.Event.EncounteredError");
                    Toast.makeText(VideoEffectActivity.this, 
							"MediaPlayer encounter Error.please check your input path/URL!!", Toast.LENGTH_LONG).show();
                    finish();
                    break;
                case MediaPlayer.Event.TimeChanged:
                    break;
                case MediaPlayer.Event.PositionChanged:
                    mCanSeek = true;
                    mHandler.sendEmptyMessage(SHOW_PROGRESS);
                    break;
                case MediaPlayer.Event.Vout:
                case MediaPlayer.Event.ESAdded:
                case MediaPlayer.Event.ESDeleted:
                    break;
            }
        }
    };
 

    private void endReached() {
        exitOK();
    }
    AlertDialog  alertDialog;
    private void encounteredError() {
    	  alertDialog = new AlertDialog.Builder(VideoEffectActivity.this)
         .setOnCancelListener(new DialogInterface.OnCancelListener() {
             @Override
             public void onCancel(DialogInterface dialog) {
            	 exitOK();
            	 finish();
             }
         })
         .setPositiveButton("软解播放", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int id) {
            	 stopPlayback();
            	 startPlayback(true);
             }
         })
           .setNegativeButton("退出", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
            	 exitOK();
            }
        })
         .setTitle("错误")
         .setMessage("硬解失败了,您要使用软解来播放吗?")
         .create();
    	alertDialog.show();
    }
    
    @Override
    public void eventHardwareAccelerationError()
    {
    	mHandler.sendEmptyMessage(HW_ERROR);
    }
    @Override
    public void onVideoSizeChanged(MediaPlayer mediaplayer,int width, int height) 
    {
        if (width * height == 0)
            return;
        mVideoHeight=height;
        mVideoWidth=width;
        mVideoVisibleHeight=height;
        mVideoVisibleWidth=width;
        mSarDen=1;
        mSarNum=1;
        changeSurfaceLayout();
    }
    private final OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        	if(mMediaPlayer.isPlaying())
	        mHandler.removeMessages(SHOW_PROGRESS);   
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        	if(mMediaPlayer.isPlaying())
        	{
        		Message  msg = mHandler.obtainMessage(SHOW_PROGRESS);
        		mHandler.sendMessageDelayed(msg, 500);	
        	}	
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser && mCanSeek) 
            {
                	mMediaPlayer.setTime(progress);
                	mTime.setText(millisToString(progress));
            }
        }
    };

 
    private final OnClickListener mPlayPauseListener = new OnClickListener() {
        @Override
        public void onClick(View v) 
        {
        	 if (mMediaPlayer.isPlaying()) 
        	 {
        	    	mMediaPlayer.pause();
        	        mSurfaceView.setKeepScreenOn(false);
        	        mHandler.removeMessages(SHOW_PROGRESS);     
        	        mPlayPause.setBackgroundResource(R.drawable.ic_play_circle_normal);
                    showOverlayTimeout(OVERLAY_INFINITE);
             } 
        	 else 
             {
             	mMediaPlayer.play();
            	mSurfaceView.setKeepScreenOn(true);
                mHandler.sendEmptyMessage(SHOW_PROGRESS);
    	        mPlayPause.setBackgroundResource(R.drawable.ic_pause_circle_normal);
                showOverlayTimeout(OVERLAY_TIMEOUT);
             }
        }
    };

//----------------------------------------------------------------------------------------------------------------------------------
      @Override
    protected void onResume() {
        super.onResume();
        if (mIsLocked && mScreenOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR)
            setRequestedOrientation(mScreenOrientationLock);
    }

   

 
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (!AndroidVersion.isHoneycombOrLater())
            changeSurfaceLayout();
        super.onConfigurationChanged(newConfig);
        resetHudLayout();
    }

    public void resetHudLayout() {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)mOverlayButtons.getLayoutParams();
        if (getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            layoutParams.addRule(RelativeLayout.BELOW, R.id.player_overlay_length);
            layoutParams.addRule(RelativeLayout.RIGHT_OF, 0);
            layoutParams.addRule(RelativeLayout.LEFT_OF, 0);
        } else {
            layoutParams.addRule(RelativeLayout.BELOW, R.id.player_overlay_seekbar);
            layoutParams.addRule(RelativeLayout.RIGHT_OF, R.id.player_overlay_time);
            layoutParams.addRule(RelativeLayout.LEFT_OF, R.id.player_overlay_length);
        }
        mOverlayButtons.setLayoutParams(layoutParams);
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onStop() {
        super.onStop();

        if (mAlertDialog != null && mAlertDialog.isShowing())
            mAlertDialog.dismiss();
       
        stopPlayback();
        restoreBrightness();
    }

    @TargetApi(android.os.Build.VERSION_CODES.FROYO)
    private void restoreBrightness() {
        if (mRestoreAutoBrightness != -1f) {
            int brightness = (int) (mRestoreAutoBrightness*255f);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS,
                    brightness);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAudioManager = null;
    }

  

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void startPlayback(boolean isSwCodec) {
    	mUri = null;
      Bundle extras = getIntent().getExtras();
      mUri = extras.getParcelable(PLAY_LOCATION);

  
      
      mCanSeek = false;

      if (mUri != null) 
      {
      	  	mMediaPlayer = new MediaPlayer(this);
      	  	mMediaPlayer.setVideoView(mSurfaceView); 
                  
      	  	mMediaPlayer.setOnVideoSizeChangedListener(this);
      	  	mSurfaceView.setKeepScreenOn(true);
          	if(isSwCodec)
          		mMediaPlayer.setDataSource(mUri,true);
          	else
          		mMediaPlayer.setDataSource(mUri);
          	
          		mMediaPlayer.setDisableAllEffect();
          	
              mMediaPlayer.setEventListener(mMediaPlayerListener);
              mMediaPlayer.setOnHardwareAccelerationErrorListener(this);
              mMediaPlayer.play();
      }
    	
    	
        mPlaybackStarted = true;

     
        if (AndroidVersion.isICSOrLater())
            getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(
                    new OnSystemUiVisibilityChangeListener() {
                        @Override
                        public void onSystemUiVisibilityChange(int visibility) {
                            if (visibility == mUiVisibility)
                                return;
                            if (visibility == View.SYSTEM_UI_FLAG_VISIBLE && !mShowing && !isFinishing()) {
                                showOverlay();
                            }
                            mUiVisibility = visibility;
                        }
                    }
            );

        if (AndroidVersion.isHoneycombOrLater()) {
            if (mOnLayoutChangeListener == null) {
                mOnLayoutChangeListener = new View.OnLayoutChangeListener() {
                    private final Runnable mRunnable = new Runnable() {
                        @Override
                        public void run() {
                            changeSurfaceLayout();
                        }
                    };
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right,
                                               int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom) {
                            /* changeSurfaceLayout need to be called after the layout changed */
                            mHandler.removeCallbacks(mRunnable);
                            mHandler.post(mRunnable);
                        }
                    }
                };
            }
            mSurfaceFrame.addOnLayoutChangeListener(mOnLayoutChangeListener);
        }
        changeSurfaceLayout();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void stopPlayback() {
        if (!mPlaybackStarted)
            return;

        if (mMute)
            mute(false);


        mPlaybackStarted = false;

        mSurfaceView.setKeepScreenOn(false);

        mHandler.removeCallbacksAndMessages(null);



        if (AndroidVersion.isHoneycombOrLater() && mOnLayoutChangeListener != null)
            mSurfaceFrame.removeOnLayoutChangeListener(mOnLayoutChangeListener);

        if (AndroidVersion.isICSOrLater())
            getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(null);

      mHandler.removeMessages(SHOW_PROGRESS);     
  		mMediaPlayer.removeOnVideoSizeChangedListener();
      mMediaPlayer.setEventListener(null);
      mSurfaceView.setKeepScreenOn(false);
      mMediaPlayer.stop();  
      mMediaPlayer.release();
    }




    private void exit(int resultCode){
        finish();
    }

    private void exitOK() {
        exit(RESULT_OK);
    }

    @TargetApi(12) //only active for Android 3.1+
    public boolean dispatchGenericMotionEvent(MotionEvent event){
        //Check for a joystick event
    	
    	Log.i("sno","dispatchGenericMotionEvent==============>");
    	
        if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) !=
                InputDevice.SOURCE_JOYSTICK ||
                event.getAction() != MotionEvent.ACTION_MOVE)
            return false;

        InputDevice mInputDevice = event.getDevice();

        float dpadx = event.getAxisValue(MotionEvent.AXIS_HAT_X);
        float dpady = event.getAxisValue(MotionEvent.AXIS_HAT_Y);
        if (mInputDevice == null || Math.abs(dpadx) == 1.0f || Math.abs(dpady) == 1.0f)
            return false;

        float x = AndroidDevices.getCenteredAxis(event, mInputDevice,
                MotionEvent.AXIS_X);
        float y = AndroidDevices.getCenteredAxis(event, mInputDevice,
                MotionEvent.AXIS_Y);
        float rz = AndroidDevices.getCenteredAxis(event, mInputDevice,
                MotionEvent.AXIS_RZ);

        if (System.currentTimeMillis() - mLastMove > JOYSTICK_INPUT_DELAY){
            if (Math.abs(x) > 0.3){
                    seekDelta(x > 0.0f ? 10000 : -10000);
            } else if (Math.abs(y) > 0.3){
               if (mIsFirstBrightnessGesture)
                        initBrightnessTouch();
                    changeBrightness(-y / 10f);
            } else if (Math.abs(rz) > 0.3){
                mVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                int delta = -(int) ((rz / 7) * mAudioMax);
                int vol = (int) Math.min(Math.max(mVol + delta, 0), mAudioMax);
                setAudioVolume(vol);
            }
            mLastMove = System.currentTimeMillis();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mLockBackButton) {
            mLockBackButton = false;
            mHandler.sendEmptyMessageDelayed(RESET_BACK_LOCK, 2000);
        } else
            exitOK();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_BUTTON_B)
            return super.onKeyDown(keyCode, event);

        showOverlayTimeout(OVERLAY_TIMEOUT);
        switch (keyCode) {
        case KeyEvent.KEYCODE_F:
        case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
        case KeyEvent.KEYCODE_MEDIA_NEXT:
            seekDelta(10000);
            return true;
        case KeyEvent.KEYCODE_R:
        case KeyEvent.KEYCODE_MEDIA_REWIND:
        case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
            seekDelta(-10000);
            return true;
        case KeyEvent.KEYCODE_BUTTON_R1:
            seekDelta(60000);
            return true;
        case KeyEvent.KEYCODE_BUTTON_L1:
            seekDelta(-60000);
            return true;
        case KeyEvent.KEYCODE_BUTTON_A:
            if (mOverlayProgress.getVisibility() == View.VISIBLE)
                return false;
        case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
        case KeyEvent.KEYCODE_MEDIA_PLAY:
        case KeyEvent.KEYCODE_MEDIA_PAUSE:
        case KeyEvent.KEYCODE_SPACE:
            return true;
        case KeyEvent.KEYCODE_O:
        case KeyEvent.KEYCODE_BUTTON_Y:
        case KeyEvent.KEYCODE_V:
        case KeyEvent.KEYCODE_MEDIA_AUDIO_TRACK:
        case KeyEvent.KEYCODE_N:
            showNavMenu();
            return true;
        case KeyEvent.KEYCODE_A:
            resizeVideo();
            return true;
        case KeyEvent.KEYCODE_M:
        case KeyEvent.KEYCODE_VOLUME_MUTE:
            updateMute();
            return true;
        case KeyEvent.KEYCODE_S:
        case KeyEvent.KEYCODE_MEDIA_STOP:
            exitOK();
            return true;
        case KeyEvent.KEYCODE_DPAD_UP:
        case KeyEvent.KEYCODE_DPAD_DOWN:
        case KeyEvent.KEYCODE_DPAD_LEFT:
        case KeyEvent.KEYCODE_DPAD_RIGHT:
        case KeyEvent.KEYCODE_DPAD_CENTER:
        case KeyEvent.KEYCODE_ENTER:
                return super.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }


    /**
     * Lock screen rotation
     */
    private void lockScreen() {
        if(mScreenOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
                setRequestedOrientation(14 /* SCREEN_ORIENTATION_LOCKED */);
            else
                setRequestedOrientation(getScreenOrientation());
            mScreenOrientationLock = getScreenOrientation();
        }
        showInfo("locked", 1000);
        mTime.setEnabled(false);
        mSeekbar.setEnabled(false);
        mLength.setEnabled(false);
        mSize.setEnabled(false);
        mEdit.setEnabled(false);
        hideOverlay(true);
        mLockBackButton = true;
        mLock.setBackgroundResource(R.drawable.video_lockbtn_locked);
    }

    private void unlockScreen() {
        if(mScreenOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        showInfo("unLock", 1000);
        mTime.setEnabled(true);
        mSeekbar.setEnabled(true);
        mLength.setEnabled(true);
        mSize.setEnabled(true);
        mEdit.setEnabled(true);
        mShowing = false;
        showOverlay();
        mLockBackButton = false;
        mLock.setBackgroundResource(R.drawable.video_lockbtn_unlock);
    }

    /**
     * Show text in the info view and vertical progress bar for "duration" milliseconds
     * @param text
     * @param duration
     * @param barNewValue new volume/brightness value (range: 0 - 15)
     */
    private void showInfoWithVerticalBar(String text, int duration, int barNewValue) {
        showInfo(text, duration);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mVerticalBarProgress.getLayoutParams();
        layoutParams.weight = barNewValue;
        mVerticalBarProgress.setLayoutParams(layoutParams);
        mVerticalBar.setVisibility(View.VISIBLE);
    }

    /**
     * Show text in the info view for "duration" milliseconds
     * @param text
     * @param duration
     */
    private void showInfo(String text, int duration) {
        mInfo.setVisibility(View.VISIBLE);
        mInfo.setText(text);
        mHandler.removeMessages(FADE_OUT_INFO);
        mHandler.sendEmptyMessageDelayed(FADE_OUT_INFO, duration);
    }

    private void showInfo(int textid, int duration) {
        mInfo.setVisibility(View.VISIBLE);
        mInfo.setText(textid);
        mHandler.removeMessages(FADE_OUT_INFO);
        mHandler.sendEmptyMessageDelayed(FADE_OUT_INFO, duration);
    }

    /**
     * Show text in the info view
     * @param text
     */
    private void showInfo(String text) {
        mHandler.removeMessages(FADE_OUT_INFO);
        mInfo.setVisibility(View.VISIBLE);
        mInfo.setText(text);
        hideInfo();
    }

    /**
     * hide the info view with "delay" milliseconds delay
     * @param delay
     */
    private void hideInfo(int delay) {
        mHandler.sendEmptyMessageDelayed(FADE_OUT_INFO, delay);
    }

    /**
     * hide the info view
     */
    private void hideInfo() {
        hideInfo(0);
    }

    private void fadeOutInfo() {
        if (mInfo.getVisibility() == View.VISIBLE)
            mInfo.startAnimation(AnimationUtils.loadAnimation(
                    VideoEffectActivity.this, android.R.anim.fade_out));
        mInfo.setVisibility(View.INVISIBLE);
        
        if (mVerticalBar.getVisibility() == View.VISIBLE)
        {

        	mVerticalBar.startAnimation(AnimationUtils.loadAnimation(
                    VideoEffectActivity.this, android.R.anim.fade_out));
        	mVerticalBar.setVisibility(View.INVISIBLE);
        }
    }


    /**
     * Handle resize of the surface and the overlay
     */
    private final Handler mHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (mMediaPlayer == null)
                return true;

            switch (msg.what) {
                case FADE_OUT:
                    hideOverlay(false);
                    break;
                case SHOW_PROGRESS:
	                	int time = (int) mMediaPlayer.getTime();
	            		int length = (int) mMediaPlayer.getLength();
	            		mSeekbar.setMax(length);
	            		mSeekbar.setProgress(time);        
	            		if (time >= 0) mTime.setText(millisToString(time));
	            		if (mCanSeek) {
	                       msg = mHandler.obtainMessage(SHOW_PROGRESS);
	                       int time2=length-time;
	                      	mHandler.sendMessageDelayed(msg, time2>1000? 1000: time2);
	            		}
                    break;
                case FADE_OUT_INFO:
                    fadeOutInfo();
                    break;
                case RESET_BACK_LOCK:
                    mLockBackButton = true;
                    break;
                case HW_ERROR:
                	encounteredError();
                    break;
            }
            return true;
        }
    });

    private boolean canShowProgress() {
        return !mDragging && mShowing && mMediaPlayer != null &&  mMediaPlayer.isPlaying();
    }
 

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void changeSurfaceLayout() {
        int sw;
        int sh;

        sw = getWindow().getDecorView().getWidth();
        sh = getWindow().getDecorView().getHeight();

        int length = (int) mMediaPlayer.getLength();
        mLength.setText(millisToString(length));

        double dw = sw, dh = sh;
        boolean isPortrait;

            isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        if (sw > sh && isPortrait || sw < sh && !isPortrait) {
            dw = sh;
            dh = sw;
        }

        // sanity check
        if (dw * dh == 0 || mVideoWidth * mVideoHeight == 0) {
            Log.e(TAG, "Invalid surface size");
            return;
        }

        // compute the aspect ratio
        double ar, vw;
        if (mSarDen == mSarNum) {
            /* No indication about the density, assuming 1:1 */
            vw = mVideoVisibleWidth;
            ar = (double)mVideoVisibleWidth / (double)mVideoVisibleHeight;
        } else {
            /* Use the specified aspect ratio */
            vw = mVideoVisibleWidth * (double)mSarNum / mSarDen;
            ar = vw / mVideoVisibleHeight;
        }

        // compute the display aspect ratio
        double dar = dw / dh;

        switch (mCurrentSize) {
            case SURFACE_BEST_FIT:
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_FIT_HORIZONTAL:
                dh = dw / ar;
                break;
            case SURFACE_FIT_VERTICAL:
                dw = dh * ar;
                break;
            case SURFACE_FILL:
                break;
            case SURFACE_16_9:
                ar = 16.0 / 9.0;
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_4_3:
                ar = 4.0 / 3.0;
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_ORIGINAL:
                dh = mVideoVisibleHeight;
                dw = vw;
                break;
        }

        SurfaceView surface;
        FrameLayout surfaceFrame;

            surface = mSurfaceView;
            surfaceFrame = mSurfaceFrame;
            
        LayoutParams lp = surface.getLayoutParams();
        lp.width  = (int) Math.ceil(dw * mVideoWidth / mVideoVisibleWidth);
        lp.height = (int) Math.ceil(dh * mVideoHeight / mVideoVisibleHeight);
        surface.setLayoutParams(lp);

        // set frame size (crop if necessary)
        lp = surfaceFrame.getLayoutParams();
        lp.width = (int) Math.floor(dw);
        lp.height = (int) Math.floor(dh);
        surfaceFrame.setLayoutParams(lp);

        surface.invalidate();
    }

    private void sendMouseEvent(int action, int button, int x, int y) {
    }

    /**
     * show/hide the overlay
     */

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mMediaPlayer == null )
            return false;
        if(slidelayerAdjust.isOpened())
        	slidelayerAdjust.closeLayer(true);
        if(slidelayerEffect.isOpened())
        	slidelayerEffect.closeLayer(true);
        
    	mEdit.setVisibility(View.VISIBLE);
    	
        if (mIsLocked) 
        {
            // locked, only handle show/hide & ignore all actions
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (!mShowing) {
                    showOverlay();
                } else {
                    hideOverlay(true);
                }
            }
            showInfo("已锁定");
            return false;
        }

        DisplayMetrics screen = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(screen);

        if (mSurfaceYDisplayRange == 0)
            mSurfaceYDisplayRange = Math.min(screen.widthPixels, screen.heightPixels);

        float x_changed, y_changed;
        if (mTouchX != -1f && mTouchY != -1f) {
            y_changed = event.getRawY() - mTouchY;
            x_changed = event.getRawX() - mTouchX;
        } else {
            x_changed = 0f;
            y_changed = 0f;
        }


        // coef is the gradient's move to determine a neutral zone
        float coef = Math.abs (y_changed / x_changed);
        float xgesturesize = ((x_changed / screen.xdpi) * 2.54f);
        float delta_y = Math.max(1f,((mInitTouchY - event.getRawY()) / screen.xdpi + 0.5f)*2f);

        /* Offset for Mouse Events */
        int[] offset = new int[2];
        mSurfaceView.getLocationOnScreen(offset);
        int xTouch = Math.round((event.getRawX() - offset[0]) * mVideoWidth / mSurfaceView.getWidth());
        int yTouch = Math.round((event.getRawY() - offset[1]) * mVideoHeight / mSurfaceView.getHeight());

        switch (event.getAction()) {

        case MotionEvent.ACTION_DOWN:
            // Audio
            mTouchY = mInitTouchY = event.getRawY();
            mVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            mTouchAction = TOUCH_NONE;
            // Seek
            mTouchX = event.getRawX();
            // Mouse events for the core
            sendMouseEvent(MotionEvent.ACTION_DOWN, 0, xTouch, yTouch);
            break;

        case MotionEvent.ACTION_MOVE:
            // Mouse events for the core
            sendMouseEvent(MotionEvent.ACTION_MOVE, 0, xTouch, yTouch);

            // No volume/brightness action if coef < 2 or a secondary display is connected
            //TODO : Volume action when a secondary display is connected
            if (mTouchAction != TOUCH_SEEK && coef > 2) {
                if (Math.abs(y_changed/mSurfaceYDisplayRange) < 0.05)
                    return false;
                mTouchY = event.getRawY();
                mTouchX = event.getRawX();
                // Volume (Up or Down - Right side)
                if (!mEnableBrightnessGesture || (int)mTouchX > (3 * screen.widthPixels / 5)){
                    doVolumeTouch(y_changed);
                    hideOverlay(true);
                }
                // Brightness (Up or Down - Left side)
                if (mEnableBrightnessGesture && (int)mTouchX < (2 * screen.widthPixels / 5)){
                    doBrightnessTouch(y_changed);
                    hideOverlay(true);
                }
            } else {
                // Seek (Right or Left move)
                doSeekTouch(Math.round(delta_y), xgesturesize, false);
            }
            break;

        case MotionEvent.ACTION_UP:
            // Mouse events for the core
            sendMouseEvent(MotionEvent.ACTION_UP, 0, xTouch, yTouch);

            if (mTouchAction == TOUCH_NONE) {
                if (!mShowing) {
                    showOverlay();
                } else {
                    hideOverlay(true);
                }
            }
            // Seek
            if (mTouchAction == TOUCH_SEEK)
                doSeekTouch(Math.round(delta_y), xgesturesize, true);
            mTouchX = -1f;
            mTouchY = -1f;
            break;
        }
        return mTouchAction != TOUCH_NONE;
    }

    private void doSeekTouch(int coef, float gesturesize, boolean seek) {
        if (coef == 0)
            coef = 1;
        // No seek action if coef > 0.5 and gesturesize < 1cm
        if (Math.abs(gesturesize) < 1 || !mCanSeek)
            return;

        if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_SEEK)
            return;
        mTouchAction = TOUCH_SEEK;

        long length = mMediaPlayer.getLength();
        long time = getTime();

        // Size of the jump, 10 minutes max (600000), with a bi-cubic progression, for a 8cm gesture
        int jump = (int) ((Math.signum(gesturesize) * ((600000 * Math.pow((gesturesize / 8), 4)) + 3000)) / coef);

        // Adjust the jump
        if ((jump > 0) && ((time + jump) > length))
            jump = (int) (length - time);
        if ((jump < 0) && ((time + jump) < 0))
            jump = (int) -time;

        //Jump !
        if (seek && length > 0)
            seek(time + jump, length);

        if (length > 0)
            //Show the jump's size
        {
        	String str="";
        	str+=jump>0? "前进 ":"后退";
        	 str+=millisToString2(jump);
        	showInfo(str,1000);
        }
        	
        else
            showInfo(R.string.unseekable_stream, 1000);
    }

    private void doVolumeTouch(float y_changed) {
        if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_VOLUME)
            return;
        float delta = - ((y_changed / mSurfaceYDisplayRange) * mAudioMax);
        mVol += delta;
        int vol = (int) Math.min(Math.max(mVol, 0), mAudioMax);
        if (delta != 0f) {
            setAudioVolume(vol);
        }
    }

    private void setAudioVolume(int vol) {
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0);

        /* Since android 4.3, the safe volume warning dialog is displayed only with the FLAG_SHOW_UI flag.
         * We don't want to always show the default UI volume, so show it only when volume is not set. */
        int newVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (vol != newVol)
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, AudioManager.FLAG_SHOW_UI);

        mTouchAction = TOUCH_VOLUME;
        vol = vol * 100 / mAudioMax;
        showInfoWithVerticalBar(getString(R.string.volume) + "\n" + Integer.toString(vol) + '%', 1000, vol);
    }

    private void mute(boolean mute) {
        mMute = mute;
        if (mMute)
            mVolSave = mMediaPlayer.getVolume();
        mMediaPlayer.setVolume(mMute ? 0 : mVolSave);
    }

    private void updateMute () {
        mute(!mMute);
        showInfo(mMute ? R.string.sound_off : R.string.sound_on,1000);
    }

    @TargetApi(android.os.Build.VERSION_CODES.FROYO)
    private void initBrightnessTouch() {
        float brightnesstemp = 0.6f;
        // Initialize the layoutParams screen brightness
        try {
            if (AndroidVersion.isFroyoOrLater() &&
                    Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                mRestoreAutoBrightness = android.provider.Settings.System.getInt(getContentResolver(),
                        android.provider.Settings.System.SCREEN_BRIGHTNESS) / 255.0f;
            } else {
                brightnesstemp = android.provider.Settings.System.getInt(getContentResolver(),
                    android.provider.Settings.System.SCREEN_BRIGHTNESS) / 255.0f;
            }
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = brightnesstemp;
        getWindow().setAttributes(lp);
        mIsFirstBrightnessGesture = false;
    }

    private void doBrightnessTouch(float y_changed) {
        if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_BRIGHTNESS)
            return;
        if (mIsFirstBrightnessGesture) initBrightnessTouch();
            mTouchAction = TOUCH_BRIGHTNESS;

        // Set delta : 2f is arbitrary for now, it possibly will change in the future
        float delta = - y_changed / mSurfaceYDisplayRange;

        changeBrightness(delta);
    }

    private void changeBrightness(float delta) {
        // Estimate and adjust Brightness
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness =  Math.min(Math.max(lp.screenBrightness + delta, 0.01f), 1);
        // Set Brightness
        getWindow().setAttributes(lp);
        int brightness = Math.round(lp.screenBrightness * 100);
        showInfoWithVerticalBar(getString(R.string.brightness) + "\n" + brightness + '%', 1000, brightness);
    }

  
    private void showNavMenu() {
    }

    private long getTime() {
        long time = mMediaPlayer.getTime();
        if (mForcedTime != -1 && mLastTime != -1) {
            /* XXX: After a seek, mMediaPlayer.getTime can return the position before or after
             * the seek position. Therefore we return mForcedTime in order to avoid the seekBar
             * to move between seek position and the actual position.
             * We have to wait for a valid position (that is after the seek position).
             * to re-init mLastTime and mForcedTime to -1 and return the actual position.
             */
            if (mLastTime > mForcedTime) {
                if (time <= mLastTime && time > mForcedTime || time > mLastTime)
                    mLastTime = mForcedTime = -1;
            } else {
                if (time > mForcedTime)
                    mLastTime = mForcedTime = -1;
            }
        }
        return mForcedTime == -1 ? time : mForcedTime;
    }

    private void seek(long position) {
        seek(position, mMediaPlayer.getLength());
    }

    private void seek(long position, float length) {
        mForcedTime = position;
        mLastTime = mMediaPlayer.getTime();
        if (length == 0f)
            mMediaPlayer.setTime(position);
        else
            mMediaPlayer.setPosition(position / length);
    }

    private void seekDelta(int delta) {
        // unseekable stream
        if(mMediaPlayer.getLength() <= 0 || !mCanSeek) return;

        long position = getTime() + delta;
        if (position < 0) position = 0;
        seek(position);
        showOverlay();
    }
    
    private final OnClickListener mLockListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mIsLocked) {
                mIsLocked = false;
                unlockScreen();
            } else {
                mIsLocked = true;
                lockScreen();
            }
        }
    };
    private final OnClickListener mSizeListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            resizeVideo();
        }
    };

    private void resizeVideo() {
        if (mCurrentSize < SURFACE_ORIGINAL) {
            mCurrentSize++;
        } else {
            mCurrentSize = 0;
        }
        changeSurfaceLayout();
        switch (mCurrentSize) {
            case SURFACE_BEST_FIT:
                showInfo(R.string.surface_best_fit, 1000);
                break;
            case SURFACE_FIT_HORIZONTAL:
                showInfo(R.string.surface_fit_horizontal, 1000);
                break;
            case SURFACE_FIT_VERTICAL:
                showInfo(R.string.surface_fit_vertical, 1000);
                break;
            case SURFACE_FILL:
                showInfo(R.string.surface_fill, 1000);
                break;
            case SURFACE_16_9:
                showInfo("16:9", 1000);
                break;
            case SURFACE_4_3:
                showInfo("4:3", 1000);
                break;
            case SURFACE_ORIGINAL:
                showInfo(R.string.surface_original, 1000);
                break;
        }
        showOverlay();
    }

    

    /**
     * show overlay
     * @param forceCheck: adjust the timeout in function of playing state
     */
    private void showOverlay(boolean forceCheck) {
        if (forceCheck)
            mOverlayTimeout = 0;
        showOverlayTimeout(0);
    }

    /**
     * show overlay with the previous timeout value
     */
    private void showOverlay() {
        showOverlay(false);
    }

    /**
     * show overlay
     */
    private void showOverlayTimeout(int timeout) {
        if (mMediaPlayer == null)
            return;
        if (timeout != 0)
            mOverlayTimeout = timeout;
        if (mOverlayTimeout == 0)
            mOverlayTimeout = mMediaPlayer.isPlaying() ? OVERLAY_TIMEOUT : OVERLAY_INFINITE;
        if (mIsNavMenu){
            mShowing = true;
            return;
        }
        mHandler.sendEmptyMessage(SHOW_PROGRESS);
        if (!mShowing) 
        {
            mShowing = true;
            if (!mIsLocked) {
                mPlayPause.setVisibility(View.VISIBLE);
                mSize.setVisibility(View.VISIBLE);
                dimStatusBar(false);
            }
            mOverlayProgress.setVisibility(View.VISIBLE);
            mEdit.setVisibility(View.VISIBLE);
            mLock.setVisibility(View.VISIBLE);
        }
        mHandler.removeMessages(FADE_OUT);
        if (mOverlayTimeout != OVERLAY_INFINITE)
            mHandler.sendMessageDelayed(mHandler.obtainMessage(FADE_OUT), mOverlayTimeout);
        updateOverlayPausePlay();
    }


    /**
     * hider overlay
     */
    private void hideOverlay(boolean fromUser) {
        if (mShowing) 
        {
            mHandler.removeMessages(FADE_OUT);
            mHandler.removeMessages(SHOW_PROGRESS);
            if (!fromUser && !mIsLocked) 
            {
                mOverlayProgress.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
                mPlayPause.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
                mLock.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
                mEdit.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
            } else
                mSize.setVisibility(View.INVISIBLE);
            
            mOverlayProgress.setVisibility(View.INVISIBLE);
            mPlayPause.setVisibility(View.INVISIBLE);
            mLock.setVisibility(View.INVISIBLE);
            mEdit.setVisibility(View.INVISIBLE);
            
            mShowing = false;
            dimStatusBar(true);
        } else if (!fromUser) {
            /*
             * Try to hide the Nav Bar again.
             * It seems that you can't hide the Nav Bar if you previously
             * showed it in the last 1-2 seconds.
             */
            dimStatusBar(true);
        }
    }

    /**
     * Dim the status bar and/or navigation icons when needed on Android 3.x.
     * Hide it on Android 4.0 and later
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void dimStatusBar(boolean dim) {
        if (!AndroidVersion.isHoneycombOrLater() || mIsNavMenu)
            return;
        int visibility = 0;
        int navbar = 0;

        if (!AndroidDevices.hasCombBar() && AndroidVersion.isJellyBeanOrLater()) {
            visibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            navbar = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        }
        visibility |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        if (dim) {
            navbar |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
            if (!AndroidDevices.hasCombBar()) {
                navbar |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
                if (AndroidVersion.isKitKatOrLater())
                    visibility |= View.SYSTEM_UI_FLAG_IMMERSIVE;
                visibility |= View.SYSTEM_UI_FLAG_FULLSCREEN;
            }
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            visibility |= View.SYSTEM_UI_FLAG_VISIBLE;
        }

        if (AndroidDevices.hasNavBar())
            visibility |= navbar;
        getWindow().getDecorView().setSystemUiVisibility(visibility);
    }

    private void updateOverlayPausePlay() {
        if (mMediaPlayer == null)
            return;
     //   mPlayPause.setImageResource(mMediaPlayer.isPlaying() ? R.drawable.ic_pause_circle
      //          : R.drawable.ic_play_circle);
    }

    /**
     * update the overlay
     */
    private int setOverlayProgress() {
        if (mMediaPlayer == null) {
            return 0;
        }
        int time = (int) getTime();
        int length = (int) mMediaPlayer.getLength();
      

        // Update all view elements
        mSeekbar.setMax(length);
        mSeekbar.setProgress(time);
        
        if (time >= 0) mTime.setText(millisToString(time));
        if (length >= 0) mLength.setText(mDisplayRemainingTime && length > 0
                ? "-" + '\u00A0' + millisToString(length - time)
                : millisToString(length));
        return time;
    }
    @SuppressWarnings("deprecation")
    private int getScreenRotation(){
        WindowManager wm = (WindowManager) LanSoDemoApplication.getAppContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO /* Android 2.2 has getRotation */) {
            try {
                Method m = display.getClass().getDeclaredMethod("getRotation");
                return (Integer) m.invoke(display);
            } catch (Exception e) {
                return Surface.ROTATION_0;
            }
        } else {
            return display.getOrientation();
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private int getScreenOrientation(){
        WindowManager wm = (WindowManager) LanSoDemoApplication.getAppContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int rot = getScreenRotation();
        /*
         * Since getRotation() returns the screen's "natural" orientation,
         * which is not guaranteed to be SCREEN_ORIENTATION_PORTRAIT,
         * we have to invert the SCREEN_ORIENTATION value if it is "naturally"
         * landscape.
         */
        @SuppressWarnings("deprecation")
        boolean defaultWide = display.getWidth() > display.getHeight();
        if(rot == Surface.ROTATION_90 || rot == Surface.ROTATION_270)
            defaultWide = !defaultWide;
        if(defaultWide) {
            switch (rot) {
            case Surface.ROTATION_0:
                return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            case Surface.ROTATION_90:
                return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            case Surface.ROTATION_180:
                // SCREEN_ORIENTATION_REVERSE_PORTRAIT only available since API
                // Level 9+
                return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO ? ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                        : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            case Surface.ROTATION_270:
                // SCREEN_ORIENTATION_REVERSE_LANDSCAPE only available since API
                // Level 9+
                return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO ? ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                        : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            default:
                return 0;
            }
        } else {
            switch (rot) {
            case Surface.ROTATION_0:
                return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            case Surface.ROTATION_90:
                return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            case Surface.ROTATION_180:
                // SCREEN_ORIENTATION_REVERSE_PORTRAIT only available since API
                // Level 9+
                return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO ? ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                        : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            case Surface.ROTATION_270:
                // SCREEN_ORIENTATION_REVERSE_LANDSCAPE only available since API
                // Level 9+
                return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO ? ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                        : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            default:
                return 0;
            }
        }
    }

  
   
    private void updateNavStatus() {
        mIsNavMenu = false;
        mMenuIdx = -1;
    }


    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    static String millisToString2(long millis) {
        boolean negative = millis < 0;
        millis = java.lang.Math.abs(millis);

        millis /= 1000;
        int sec = (int) (millis % 60);
        millis /= 60;
        int min = (int) (millis % 60);
        millis /= 60;
        int hours = (int) millis;

        String time=" ";
        DecimalFormat format = (DecimalFormat)NumberFormat.getInstance(Locale.US);
        format.applyPattern("00");
       
        if(hours>0)
       	 	time = hours + "时" + format.format(min) + "分" + format.format(sec)+"秒";
        else if(min>0)
        {
        	 time = format.format(min) + "分" + format.format(sec)+"秒";
        }else if(sec>0){
        	 time = format.format(sec)+"秒";
        }
        
        return time;
    }
    
    
    static String millisToString(long millis) {
        boolean negative = millis < 0;
        millis = java.lang.Math.abs(millis);

        millis /= 1000;
        int sec = (int) (millis % 60);
        millis /= 60;
        int min = (int) (millis % 60);
        millis /= 60;
        int hours = (int) millis;

        String time;
        DecimalFormat format = (DecimalFormat)NumberFormat.getInstance(Locale.US);
        format.applyPattern("00");
       
            if (millis > 0)
                time = (negative ? "-" : "") + hours + ":" + format.format(min) + ":" + format.format(sec);
            else
                time = (negative ? "-" : "") + min + ":" + format.format(sec);
        return time;
    }

}
