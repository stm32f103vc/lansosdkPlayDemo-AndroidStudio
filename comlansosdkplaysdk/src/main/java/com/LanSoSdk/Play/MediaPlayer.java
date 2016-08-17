/*****************************************************************
 *Email: support@lansongtech.com
 * MediaPlayer.java
 *
 *
 *这个程序仅仅是演示版本，仅是功能上的呈现，不保证性能和适用性。如正好满足您的项目，我们深感荣幸。
 *我们有更专业稳定强大的发行版本，期待和您进一步的合作。
 *
 *Email: support@lansongtech.com
 *****************************************************************/
package com.LanSoSdk.Play;




import java.io.File;

import com.LanSoSdk.Play.LibPlay.HardwareAccelerationError;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceView;

@SuppressWarnings("unused")
public class MediaPlayer extends PlayObject<MediaPlayer.Event> implements LibPlay.HardwareAccelerationError,IPlayVout.Callback{

    public static class Event extends PlayEvent {
        //public static final int MediaChanged        = 0x100;
        //public static final int NothingSpecial      = 0x101;
        public static final int Opening             = 0x102;
        public static final int Buffering           = 0x103;  //add buffering EVENT....for URL
        public static final int Playing             = 0x104;
        public static final int Paused              = 0x105;
        public static final int Stopped             = 0x106;
        //public static final int Forward             = 0x107;
        //public static final int Backward            = 0x108;
        public static final int EndReached          = 0x109;
        public static final int EncounteredError   = 0x10a; 
        public static final int TimeChanged         = 0x10b;
        public static final int PositionChanged     = 0x10c;
        //public static final int SeekableChanged     = 0x10d;
        //public static final int PausableChanged     = 0x10e;
        //public static final int TitleChanged        = 0x10f;
        //public static final int SnapshotTaken       = 0x110;
        //public static final int LengthChanged       = 0x111;
        public static final int Vout                = 0x112;
        //public static final int ScrambledChanged    = 0x113;
        public static final int ESAdded             = 0x114;
        public static final int ESDeleted           = 0x115;
        //public static final int ESSelected          = 0x116;
        
        public static final int HardwareAccelerationError           = 6001;
        private final long arg1;
        private final float arg2;
        protected Event(int type) {
            super(type);
            this.arg1 = 0;
            this.arg2 = 0;
        }
        protected Event(int type, long arg1) {
            super(type);
            this.arg1 = arg1;
            this.arg2 = 0;
        }
        protected Event(int type, float arg2) {
            super(type);
            this.arg1 = 0;
            this.arg2 = arg2;
        }
        public long getTimeChanged() {
            return arg1;
        }
        public float getPositionChanged() {
            return arg2;
        }
        public float getBuffering() {  //0---100
            return arg2;
        }
        public int getVoutCount() {
            return (int) arg1;
        }
        public int getEsChangedType() {
            return (int) arg1;
        }
    }

    public interface EventListener extends PlayEvent.Listener<MediaPlayer.Event> {}


    public static class TrackDescription {
        public final int id;
        public final String name;

        private TrackDescription(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    @SuppressWarnings("unused") /* Used from JNI */
    private static TrackDescription createTrackDescriptionFromNative(int id, String name) {
        return new TrackDescription(id, name);
    }


    private Media mMedia = null;
    private boolean mPlaying = false;
    private boolean mPlayRequested = false;
    private int mVoutCount = 0;
    private boolean mAudioReset = false;
    private String mAudioOutput = null;
    private String mAudioOutputDevice = null;
    private LibPlay mLibPlay= null;
    private boolean mHardwareAccelerationError=false;

    
    private final AWindow mWindow = new AWindow(new AWindow.SurfaceCallback() 
    {
        @Override
        public void onSurfacesCreated(AWindow vout) 
        {
            boolean isplay = false;
            boolean enableVideo = false;
            synchronized (MediaPlayer.this) {
                if (!mPlaying && mPlayRequested)
                    isplay = true;
                else if (mVoutCount == 0)
                    enableVideo = true;
            }
            if (isplay)
                play();
            else if (enableVideo)
                setVideoTrackEnabled(true);
        }

        @Override
        public void onSurfacesDestroyed(AWindow vout) 
        {
		            boolean disableVideo = false;
		            synchronized (MediaPlayer.this) 
		            {
		                if (mVoutCount > 0)
		                    disableVideo = true;
		            }
		            if (disableVideo)
		                setVideoTrackEnabled(false);
		            
		            synchronized (MediaPlayer.this) 
		            {
		                while (mVoutCount > 0) 
		                {
		                    try {
		                        MediaPlayer.this.wait();
		                    } catch (InterruptedException ignored) {
		                    }
		                }
		            }
        }
    });

    /**
     * new a MediaPlayer Object. 
     * after using . call {@link #release()} to release this object.
     * 
     * 
     */
    public MediaPlayer(Context context)  
    {
    	mLibPlay =new LibPlay();
    	Log.i("MediaPlayer","current LanSoSdkPlay version is:"+mLibPlay.version());
    	mLibPlay.setOnHardwareAccelerationError(this);
    	setNativeCrashListener();
    	nativeNewFromLibPlay(context,mLibPlay, mWindow);	
    	setAudioOutput("android_audiotrack");
    }
    
    
    /**
     * Sets the data source (Uri  object) to use.
     * 
     * @param path  the path of the uri. 
     * 				if media is an absolute path, can use:  Uri path=Uri.fromFile(new File("/storage/sdcard1/xxxx.mp4"));
     * 				if media is a http/rtsp/rtmp/ URL. can use: Uri path=Uri.parse("rtsp://192.168.1.33:554/xxxx");  
     */
    public void setDataSource(Uri path)
    {
    	if(mLibPlay!=null)
    	{
    		final Media media = new Media(mLibPlay,path);
            media.setHWDecoderEnabled(true, true);
            setMedia(media);
            media.release(); 
            setRate(1.0f);
    	}
    }
    /**
     * 
     *Sets the data source (Uri  object) to use.
     *
     * @param path	 the path of the uri. 
     * 				if media is an absolute path, can use:  Uri path=Uri.fromFile(new File("/storage/sdcard1/xxxx.mp4"));
     * 				if media is a http/rtsp/rtmp/ URL. can use: Uri path=Uri.parse("rtsp://192.168.1.33:554/xxxx");  
     * @param isOnlySW  true if only use software codec.
     */
    public void setDataSource(Uri path,boolean isOnlySW) 
    {
    	if(mLibPlay!=null)
    	{
    		final Media media = new Media(mLibPlay,path);
    		if(isOnlySW)
    			media.setHWDecoderEnabled(false, false); 
    		else
    			media.setHWDecoderEnabled(true, false);  
    		
            setMedia(media);
            media.release(); 
            setRate(1.0f);
    	}
    }
    private void setMedia(Media media) {
        if (media != null) {
            if (media.isReleased())
                throw new IllegalArgumentException("Media is released");
        }
        nativeSetMedia(media);
        synchronized (this) {
            if (mMedia != null) {
                mMedia.release();
            }
            if (media != null)
                media.retain();
            mMedia = media;  ///here is valid. refcount=1;
        }
    }

    
    public interface onHardwareAccelerationErrorListener {	    
    	void eventHardwareAccelerationError();
    }
	private onHardwareAccelerationErrorListener mOnHardwareAccelerationErrorListener=null;
	
	
	/**
	 * * Register a callback to be invoked when the hardware  acceleration Error.
	 * @param listener
	 */
	public void setOnHardwareAccelerationErrorListener(onHardwareAccelerationErrorListener listener)
	{
		mOnHardwareAccelerationErrorListener=listener;
	}
	
	/**
	 * 截屏
	 *
	 */
	    public interface onSnapShotCompletedListener {
	        void snapShotCompleted( byte[] bytes,int width,int height,int bytesPerPixel); 
	    }
	    public void setOnSnapShotCompletedListener( onSnapShotCompletedListener listener) {
	    	nativeSetOnSnapShotCompletedListener(listener);
	    }
	    private native void nativeSetOnSnapShotCompletedListener( onSnapShotCompletedListener listener);

	    private native void nativeTriggerSnapShot();  //截屏, 截屏后,会调用snapshot的回调.
	    
	
	
	
	public interface onNativeCrashListener {	    
    	void onNativeCrash();
    }
	private onNativeCrashListener mOnNativeCrashListener=null;
	
	
	/**
	 * * Register a callback to be invoked when the native creashed
	 * @param listener the callback that will be run
	 */
	public void setOnNativeCrashListener(onNativeCrashListener listener)
	{
		mOnNativeCrashListener=listener;
	}
	
	private void setNativeCrashListener()
	{
		 LibPlay.setOnNativeCrashListener(new LibPlay.OnNativeCrashListener() {
             @Override
             public void onNativeCrash() {
            	 if(mOnNativeCrashListener!=null)
            	 {
            		 mOnNativeCrashListener.onNativeCrash();
            	 }
             }
         });
	}
	
    @Override
    public void eventHardwareAccelerationError() {
    	mHardwareAccelerationError = true;
    	if(mOnHardwareAccelerationErrorListener!=null)
    	{
    		mOnHardwareAccelerationErrorListener.eventHardwareAccelerationError();
    	}
    }
    
    public void triggerSnapShot()
    {
    	nativeTriggerSnapShot();
    }
    /**
     * set surfaceView to display video picture.
     * @param view
     */
    public void setVideoView(SurfaceView view)
    {
    	mWindow.setVideoView(view);
    }
    public void setSubtitlesView(SurfaceView subtitlesSurfaceView)
    {
    	mWindow.setSubtitlesView(subtitlesSurfaceView);
    }
    public void setVideoSurface2Showing(boolean isShow)
    
    {
    	mWindow.setVideoSurface2Showing(isShow);
    }
    public void setVideoView2(SurfaceView view)
    {
    	mWindow.setVideoView2(view);
    }
    
    
    
    public void setWindowSize(int width, int height)
    {
    	mWindow.setWindowSize(width, height);
    }
    public void sendMouseEvent(int action, int button, int x, int y)
    {
    	mWindow.sendMouseEvent(action,button,x,y);
    }
	
	public interface onVideoSizeChangedListener {	    
    	void onVideoSizeChanged(MediaPlayer mediaplayer,int width, int height);
    }
	private onVideoSizeChangedListener mOnVideoSizeChangedListener=null;
	
	
	/**
	 * Register a callback to be invoked when the video size is known or updated.
	 * @param listener the callback that will be run
	 */
	public void setOnVideoSizeChangedListener(onVideoSizeChangedListener listener)
	{
		mOnVideoSizeChangedListener=listener;
		mWindow.addCallback(this);
		mWindow.attachViews();
	}
	
	/**
	 * remove video size changed callback.
	 */
	public void removeOnVideoSizeChangedListener()
	{
		mOnVideoSizeChangedListener=null;
		mWindow.removeCallback(this);
		mWindow.detachViews();
	}
    //call by native function:AWindowHandler_setWindowLayout 
    @Override
    public void onNewLayout(IPlayVout playVout, int width, int height) 
    {
        if(mOnVideoSizeChangedListener!=null)
        {
        	mOnVideoSizeChangedListener.onVideoSizeChanged(this,width, height);
        }
    }

    @Override
    public void onSurfacesCreated(IPlayVout playVout) {
    }

    @Override
    public void onSurfacesDestroyed(IPlayVout playVout) {
    }
    
    /**
     * Get the Media used by this MediaPlayer. This Media should be released with {@link #release()}.
     */
    public synchronized Media getMedia() {
        if (mMedia != null)
            mMedia.retain();
        return mMedia;
    }

    /**
     * Play the media
     *
     */
    public void play() {
        synchronized (this) {
            if (!mPlaying) {
                /* HACK: stop() reset the audio output, so set it again before first play. */
                if (mAudioReset) 
                {
                    if (mAudioOutput != null)
                        nativeSetAudioOutput(mAudioOutput);
                    
                    if (mAudioOutputDevice != null)
                        nativeSetAudioOutputDevice(mAudioOutputDevice);
                    
                    mAudioReset = false;
                }
                mPlayRequested = true;
                if (mWindow.areSurfacesWaiting())
                    return;
            }
            mPlaying = true;
        }
        nativePlay();
    }
    
    /**
     * 
     * @return   true if playing. or not false
     */
    public boolean isVideoPlaying() {  
    	return mPlaying;
    }
    
    /**
     * stop Media play
     */
    public void stop() {
        synchronized (this) {
            mPlayRequested = false;
            mPlaying = false;
            mAudioReset = true;
        	mLibPlay.setOnHardwareAccelerationError(null); 
        	mOnHardwareAccelerationErrorListener=null;
        }
        nativeStop();
    }

    public synchronized void setEventListener(EventListener listener) {
        super.setEventListener(listener);
    }

    @Override
    protected synchronized Event onEventNative(int eventType, long arg1, float arg2) {
        switch (eventType) {
            case Event.Stopped:
            case Event.EndReached:
            case Event.EncounteredError:
                mVoutCount = 0;
                notify();
            case Event.Buffering:
            	return new Event(eventType,arg2);  //float type. percentage.
            case Event.Opening:
            case Event.Playing:
            case Event.Paused:
                return new Event(eventType);
            case Event.TimeChanged:
                return new Event(eventType, arg1);
            case Event.PositionChanged:
                return new Event(eventType, arg2);
            case Event.Vout:
                mVoutCount = (int) arg1;
                notify();
                return new Event(eventType, arg1);
            case Event.ESAdded:
            case Event.ESDeleted:
                return new Event(eventType, arg1);
        }
        return null;
    }

    @Override
    protected void onReleaseNative() {
        if (mMedia != null)
        	mMedia.release();
        
        nativeRelease();

        if(mLibPlay!=null)
        	mLibPlay.release();
    }
   
    /**
     * Selects an audio output module.
     * Any change will take effect only after playback is stopped and
     * restarted. Audio output cannot be changed while playing.
     *
     * @return true on success.
     */
    private boolean setAudioOutput(String aout) {
        final boolean ret = nativeSetAudioOutput(aout);
        if (ret) {
            synchronized (this) {
                mAudioOutput = aout;
            }
        }
        return ret;
    }

    /**
     * Configures an explicit audio output device.
     * Audio output will be moved to the device specified by the device identifier string.
     *
     * @return true on success.
     */
    private boolean setAudioOutputDevice(String id) {
        final boolean ret = nativeSetAudioOutputDevice(id);
        if (ret) {
            synchronized (this) {
                mAudioOutputDevice = id;
            }
        }
        return ret;
    }

   

    /**
     * Get the number of available video tracks.
     */
    public int getVideoTracksCount() {
        return nativeGetVideoTracksCount();
    }

    /**
     * Get the list of available video tracks.
     */
    public TrackDescription[] getVideoTracks() {
        return nativeGetVideoTracks();
    }

    /**
     * Get the current video track.
     *
     * @return the video track ID or -1 if no active input
     */
    public int getVideoTrack() {
        return nativeGetVideoTrack();
    }

    /**
     * Set the video track.
     *
     * @return true on success.
     */
    public boolean setVideoTrack(int index) {
        return nativeSetVideoTrack(index);
    }

    private void setVideoTrackEnabled(boolean enabled) {
        if (!enabled) {
            setVideoTrack(-1);
        } else {
            final MediaPlayer.TrackDescription tracks[] = getVideoTracks();

            if (tracks != null) {
                for (MediaPlayer.TrackDescription track : tracks) {
                    if (track.id != -1) {
                        setVideoTrack(track.id);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Get the number of available audio tracks.
     */
    public int getAudioTracksCount() {
        return nativeGetAudioTracksCount();
    }

    /**
     * Get the list of available audio tracks.
     */
    public TrackDescription[] getAudioTracks() {
        return nativeGetAudioTracks();
    }

    /**
     * Get the current audio track.
     *
     * @return the audio track ID or -1 if no active input
     */
    public int getAudioTrack() {
        return nativeGetAudioTrack();
    }

    /**
     * Set the audio track.
     *
     * @return true on success.
     */
    public boolean setAudioTrack(int index) {
        return nativeSetAudioTrack(index);
    }

    /**
     * Get the current audio delay.
     *
     * @return delay in microseconds.
     */
    public long getAudioDelay() {
        return nativeGetAudioDelay();
    }

    /**
     * Set current audio delay. The audio delay will be reset to zero each time the media changes.
     *
     * @param delay in microseconds.
     * @return true on success.
     */
    public boolean setAudioDelay(long delay) {
        return nativeSetAudioDelay(delay);
    }

    /**
     * Get the number of available spu (subtitle) tracks.
     */
    public int getSpuTracksCount() {
        return nativeGetSpuTracksCount();
    }

    /**
     * Get the list of available spu (subtitle) tracks.
     */
    public TrackDescription[] getSpuTracks() {
        return nativeGetSpuTracks();
    }

    /**
     * Get the current spu (subtitle) track.
     *
     * @return the spu (subtitle) track ID or -1 if no active input
     */
    public int getSpuTrack() {
        return nativeGetSpuTrack();
    }

    /**
     * Set the spu (subtitle) track.
     *
     * @return true on success.
     */
    public boolean setSpuTrack(int index) {
        return nativeSetSpuTrack(index);
    }

    /**
     * Get the current spu (subtitle) delay.
     *
     * @return delay in microseconds.
     */
    public long getSpuDelay() {
        return nativeGetSpuDelay();
    }

    /**
     * Set current spu (subtitle) delay. The spu delay will be reset to zero each time the media changes.
     *
     * @param delay in microseconds.
     * @return true on success.
     */
    public boolean setSpuDelay(long delay) {
        return nativeSetSpuDelay(delay);
    }

  

    /**
     * Set a new video subtitle file.
     *
     * @param path local path.
     * @return true on success.
     */
    public boolean setSubtitleFile(String path) {
        return nativeSetSubtitleFile(path);
    }

    /**
     * Sets the speed of playback (1 being normal speed, 2 being twice as fast)
     *
     * @param rate
     */
    public native void setRate(float rate);

    /**
     * Get the current playback speed
     */
    public native float getRate();

    /**
     * Returns true if any media is playing
     */
    public native boolean isPlaying();

    /**
     * Returns true if any media is seekable
     */
    public native boolean isSeekable();

    /**
     * Pauses any playing media
     */
    public native void pause();

    /**
     * Get player state.
     */
    public native int getPlayerState();

    /**
     * Gets volume as integer
     */
    public native int getVolume();

    /**
     * Sets volume as integer
     * @param volume: Volume level passed as integer
     */
    public native int setVolume(int volume);

    /**
     * Gets the current movie time (in ms).
     * @return the movie time (in ms), or -1 if there is no media.
     */
    public native long getTime();

    /**
     * Sets the movie time (in ms), if any media is being played.
     * @param time: Time in ms.
     * @return the movie time (in ms), or -1 if there is no media.
     */
    public native long setTime(long time);

    /**
     * Gets the movie position. functions similar as getTime
     * @return the movie position, or -1 for any error. unit :Percentage. min is 0%, max=100%.
     * 
     * 
     */
    public native float getPosition();

    /**
     * Sets the movie position.  functions similar as setTime.
     * @param pos: movie position.  unit percentage, min is0%, max is 100%.
     */
    public native void setPosition(float pos);

    /**
     * Gets current movie's length in ms. 
     * @return the movie length (in ms), or -1 if there is no media.
     */
    public native long getLength();

//------------------------------------------
    
  

    /* JNI */
    private native void nativeNewFromLibPlay(Context ctx,LibPlay libPlay, IAWindowNativeHandler window);
    private native void nativeNewFromMedia(Media media, IAWindowNativeHandler window);
    
    private native void nativeRelease();
    
    private native void nativeSetMedia(Media media);
    
    
    private native void nativePlay();
    private native void nativeStop();
    
    
    private native boolean nativeSetAudioOutput(String aout);
    private native boolean nativeSetAudioOutputDevice(String id);
    private native int nativeGetVideoTracksCount();
    private native TrackDescription[] nativeGetVideoTracks();
    private native int nativeGetVideoTrack();
    private native boolean nativeSetVideoTrack(int index);
    private native int nativeGetAudioTracksCount();
    private native TrackDescription[] nativeGetAudioTracks();
    private native int nativeGetAudioTrack();
    private native boolean nativeSetAudioTrack(int index);
    private native long nativeGetAudioDelay();
    private native boolean nativeSetAudioDelay(long delay);
    private native int nativeGetSpuTracksCount();
    private native TrackDescription[] nativeGetSpuTracks();
    private native int nativeGetSpuTrack();
    private native boolean nativeSetSpuTrack(int index);
    private native long nativeGetSpuDelay();
    private native boolean nativeSetSpuDelay(long delay);
    private native boolean nativeSetSubtitleFile(String path);

    
    
    
    public native void setDisableAllEffect();  //新增的两个函数.
    public native String getCurrentEffects();
    
    //video picture effects....
    public native void setEnableAnaglyph(boolean isEnable);  //3D
    public native void setEnableMirror(boolean isEnable);
    public native void setEnablePsychedelic(boolean isEnable);
    public native void setEnableWave(boolean isEnable);
    public native void setEnableRipple(boolean isEnable);
    public native void setEnableMotiondetect(boolean isEnable);
    
    public native void setEnableInvert(boolean isEnable);
    public native void setEnablePosterize(boolean isEnable);
    

    public native void setEnableExtract(boolean isEnable);
    public native void setExtractValue(int value);
    
    public native void setEnableSepia(boolean isEnable);
    public native void setSepiaValue(int value); 

    public native void setEnableMotionblur(boolean isEnable);
    public native void setMotionblurValue(int value);    
    
    //video adjuct effectttt... gamma..and so on...
    public native void setEnableAdjuct(boolean isEnable);
    public native boolean setContrastValue(float value);
    public native boolean setBrightnessValue(float value);
    public native boolean setHueValue(float value);
    public native boolean setSaturationValue(float value);
    public native boolean setGammaValue(float value);
    

     
}
