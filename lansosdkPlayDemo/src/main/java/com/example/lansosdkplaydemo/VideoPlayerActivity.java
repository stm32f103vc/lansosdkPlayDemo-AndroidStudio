/*****************************************************************************
 * Email:support@lansongtech.com
 * VideoPlayerActivity.java
 * 
 * 这个程序仅仅是演示版本，仅是功能上的呈现，不保证性能和适用性。如正好满足您的项目，我们深感荣幸。
 * 我们有更专业稳定强大的发行版本，期待和您进一步的合作。
 *  
 *Email: support@lansongtech.com 
 * 
 * 
 * 
 *****************************************************************************/

package com.example.lansosdkplaydemo;

import android.annotation.TargetApi;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.LanSoSdk.Play.Media;
import com.LanSoSdk.Play.MediaPlayer;




import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class VideoPlayerActivity extends Activity implements MediaPlayer.onVideoSizeChangedListener,MediaPlayer.onHardwareAccelerationErrorListener,SurfaceHolder.Callback 
{
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
	    private Button mPlayPause;
	    
	    private boolean mCanSeek;

	    private static final int SHOW_PROGRESS = 201;
	    private static final int HW_ERROR = 202; 
	    @Override
	    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.player);

	        mTime = (TextView) findViewById(R.id.player_overlay_time);
	        mLength = (TextView) findViewById(R.id.player_overlay_length);

	        mPlayPause = (Button) findViewById(R.id.player_overlay_play);


	        mSurfaceView = (SurfaceView) findViewById(R.id.player_surface);
	        mSurfaceFrame = (FrameLayout) findViewById(R.id.player_surface_frame);

	        holder=mSurfaceView.getHolder();
	        holder.addCallback(this);
	        
	        mSeekbar = (SeekBar) findViewById(R.id.player_overlay_seekbar);

	        mSeekbar.setOnSeekBarChangeListener(mSeekListener);
	        mPlayPause.setOnClickListener(mPlayPauseListener); 
	    }
	    
	    @Override
	    public void surfaceChanged(SurfaceHolder holder, int format, int width,
	    		int height) {
	    	// TODO Auto-generated method stub
	    	
	    }
	    @Override
	    public void surfaceCreated(SurfaceHolder holder) {
	    	// TODO Auto-generated method stub
	    	startPlayback();
	    }
	    @Override
	    public void surfaceDestroyed(SurfaceHolder holder) {
	    	// TODO Auto-generated method stub
	    	stopPlayback();
	    }
	    @Override
	    protected void onPause() {
	    	// TODO Auto-generated method stub
	    	super.onPause();
	    }
	    
	    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	    private void startPlayback() {

	        mUri = null;
	        Bundle extras = getIntent().getExtras();
	        mUri = extras.getParcelable(PLAY_LOCATION);

	        boolean isSwCodec=getIntent().getBooleanExtra(PLAY_IS_SOFTWARE_CODEC, false);
	        Log.i("sno",isSwCodec?"using SOFTWARE CODEC":"using FULL CODEC");
	        
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
	            	
	                mMediaPlayer.setEventListener(mMediaPlayerListener);
	                mMediaPlayer.setOnHardwareAccelerationErrorListener(this);
	                mMediaPlayer.play();
	        }
	    }
	    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	    private void stopPlayback() {
	        mHandler.removeMessages(SHOW_PROGRESS);     
	    	mMediaPlayer.removeOnVideoSizeChangedListener();
	        mMediaPlayer.setEventListener(null);
	        mSurfaceView.setKeepScreenOn(false);
	        mMediaPlayer.stop();  
	        mMediaPlayer.release();
	    }

	  


	    private final MediaPlayer.EventListener mMediaPlayerListener = new MediaPlayer.EventListener() 
	    {
	        @Override
	        public void onEvent(MediaPlayer.Event event) {
	            switch (event.type) {
	            	case MediaPlayer.Event.Buffering:
	                    Log.i(TAG, "MediaPlayer.Event.Buffering"+event.getBuffering());
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
	                    Toast.makeText(VideoPlayerActivity.this, 
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

	    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	    private void changeSurfaceLayout(int width, int height) {
	    	
	    	//holder.setFixedSize(width, height);  //当然,您也可以通过这样来设置, 无需下面的各种精细计算.

	        int length = (int) mMediaPlayer.getLength();
	        mLength.setText(millisToString(length));
	        
	        
	        int screenWidth= getWindow().getDecorView().getWidth();
	        int screenHeight = getWindow().getDecorView().getHeight();
	        
	        if (mMediaPlayer != null) {
	            mMediaPlayer.setWindowSize(screenWidth, screenHeight);
	        }
	        
	        double dstWidth = screenWidth, dstHeight = screenHeight;  //目标宽高.
	        boolean isPortrait; //是否是竖屏.

	        isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
	        if (screenWidth > screenHeight && isPortrait || screenWidth < screenHeight && !isPortrait) {
	            dstWidth = screenHeight;
	            dstHeight = screenWidth;
	        }

	        // sanity check
	        if (dstWidth * dstHeight == 0 || width * height == 0) {
	            Log.e(TAG, "Invalid surface size");
	            return;
	        }

	        double sar = (double)width / (double)height;

	        double dar = dstWidth / dstHeight; 

	        //如果屏幕的宽高比, 小于 视频的宽高比.则说明屏幕的宽度小于屏幕的高度,则应放大屏幕的高度
	        if (dar < sar) 
	            dstHeight = dstWidth / sar;
	        else
	            dstWidth = dstHeight * sar;
	              
	        LayoutParams lp = mSurfaceView.getLayoutParams();
	        lp.width  = (int) Math.ceil(dstWidth);
	        lp.height = (int) Math.ceil(dstHeight);
	        
	        mSurfaceView.setLayoutParams(lp);
	        
	        lp = mSurfaceFrame.getLayoutParams();
	        lp.height = (int) Math.floor(dstHeight);
	        mSurfaceFrame.setLayoutParams(lp); 
	        mSurfaceView.invalidate();
	    }
	    AlertDialog  alertDialog;
	    private void encounteredError() {
	    	  alertDialog = new AlertDialog.Builder(VideoPlayerActivity.this)
	         .setOnCancelListener(new DialogInterface.OnCancelListener() {
	             @Override
	             public void onCancel(DialogInterface dialog) {
	            	 exitOK();
	            	 finish();
	             }
	         })
	         .setPositiveButton("No", new DialogInterface.OnClickListener() {
	             @Override
	             public void onClick(DialogInterface dialog, int id) {
	            	 
	             }
	         })
	           .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int id) {
	            	 exitOK();
	            }
	        })
	         .setTitle("title")
	         .setMessage("Hardware acceleration error!,are you Exit?")
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

	        changeSurfaceLayout(width,height);
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
	             } 
	        	 else 
	             {
	             	mMediaPlayer.play();
	            	mSurfaceView.setKeepScreenOn(true);
	                mHandler.sendEmptyMessage(SHOW_PROGRESS);
	    	        mPlayPause.setBackgroundResource(R.drawable.ic_pause_circle_normal);
	             }
	        }
	    };
	    /**
	     * Handle resize of the surface and the overlay
	     */
	    private final Handler mHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
	        @Override
	        public boolean handleMessage(Message msg) {

	            switch (msg.what) {
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
	                case HW_ERROR:
	                	encounteredError();
	                	break;
	            }
	            return true;
	        }
	    });
	    private void exit(int resultCode){
	        setResult(resultCode);
	        finish();
	    }

	    private void exitOK() {
	        exit(RESULT_OK);
	    }
	  
	    @Override
	    public void onBackPressed() {
	            exitOK();
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
