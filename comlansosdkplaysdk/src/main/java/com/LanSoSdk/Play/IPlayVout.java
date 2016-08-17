/*****************************************************************
 *Email: support@lansongtech.com
 * public class IPlayVout.java
 *
 *
 *这个程序仅仅是演示版本，仅是功能上的呈现，不保证性能和适用性。如正好满足您的项目，我们深感荣幸。
 *我们有更专业稳定强大的发行版本，期待和您进一步的合作。
 *
 *Email: support@lansongtech.com
 *****************************************************************/

package com.LanSoSdk.Play;

import android.annotation.TargetApi;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.support.annotation.MainThread;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

@SuppressWarnings("unused")
public interface IPlayVout {
    interface Callback {
        /**
         * This callback is called when the native vout call request a new Layout.
         *
         * @param playVout playVout
         * @param width Frame width
         * @param height Frame height
         * @param visibleWidth Visible frame width
         * @param visibleHeight Visible frame height
         * @param sarNum Surface aspect ratio numerator
         * @param sarDen Surface aspect ratio denominator
         */
        @MainThread
        void onNewLayout(IPlayVout playVout, int width, int height);

        /**
         * This callback is called when surfaces are created.
         */
        @MainThread
        void onSurfacesCreated(IPlayVout playVout);

        /**
         * This callback is called when surfaces are destroyed.
         */
        @MainThread
        void onSurfacesDestroyed(IPlayVout playVout);
    }

    /**
     * Set a surfaceView used for video out.
     * @see #attachViews()
     */
    @MainThread
    void setVideoView(SurfaceView videoSurfaceView);
    
    /**
     * Set a TextureView used for video out.
     * @see #attachViews()
     */
    @MainThread
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    void setVideoView(TextureView videoTextureView);

    /**
     * Set a surface used for video out.
     * @param videoSurface if surfaceHolder is null, this surface must be valid and attached.
     * @param surfaceHolder optional, used to configure buffers geometry before Android ICS
     * and to get notified when surface is destroyed.
     * @see #attachViews()
     */
    @MainThread
    void setVideoSurface(Surface videoSurface, SurfaceHolder surfaceHolder);

    /**
     * Set a SurfaceTexture used for video out.
     * @param videoSurfaceTexture this surface must be valid and attached.
     * @see #attachViews()
     */
    @MainThread
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    void setVideoSurface(SurfaceTexture videoSurfaceTexture);
    
    /**
     * Set a surfaceView used for video out.
     * @see #attachViews()
     */
    @MainThread
    void setVideoView2(SurfaceView videoSurfaceView);
    
    /**
     * Set a TextureView used for video out.
     * @see #attachViews()
     */
    @MainThread
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    void setVideoView2(TextureView videoTextureView);

    /**
     * Set a surface used for video out.
     * @param videoSurface if surfaceHolder is null, this surface must be valid and attached.
     * @param surfaceHolder optional, used to configure buffers geometry before Android ICS
     * and to get notified when surface is destroyed.
     * @see #attachViews()
     */
    @MainThread
    void setVideoSurface2(Surface videoSurface, SurfaceHolder surfaceHolder);

    /**
     * Set a SurfaceTexture used for video out.
     * @param videoSurfaceTexture this surface must be valid and attached.
     * @see #attachViews()
     */
    @MainThread
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    void setVideoSurface2(SurfaceTexture videoSurfaceTexture);


    void setVideoSurface2Showing(boolean isShow);
    /**
     * =====================================复制4个方法结束.
     */
    
    
    
    
    
    
    
    
    /**
     * Set a surfaceView used for subtitles out.
     * @see #attachViews()
     */
    @MainThread
    void setSubtitlesView(SurfaceView subtitlesSurfaceView);

    /**
     * Set a TextureView used for subtitles out.
     * @see #attachViews()
     */
    @MainThread
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    void setSubtitlesView(TextureView subtitlesTextureView);

    /**
     * Set a surface used for subtitles out.
     * @param subtitlesSurface if surfaceHolder is null, this surface must be valid and attached.
     * @param surfaceHolder optional, used to configure buffers geometry before Android ICS
     * and to get notified when surface is destroyed.
     * @see #attachViews()
     */
    @MainThread
    void setSubtitlesSurface(Surface subtitlesSurface, SurfaceHolder surfaceHolder);

    /**
     * Set a SurfaceTexture used for subtitles out.
     * @param subtitlesSurfaceTexture this surface must be valid and attached.
     * @see #attachViews()
     */
    @MainThread
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    void setSubtitlesSurface(SurfaceTexture subtitlesSurfaceTexture);

    /**
     * Attach views previously set by setVideoView, setSubtitlesView, setVideoSurface, setSubtitleSurface
     * @see #setVideoView(SurfaceView)
     * @see #setVideoView(TextureView)
     * @see #setVideoSurface(Surface, SurfaceHolder)
     * @see #setSubtitlesView(SurfaceView)
     * @see #setSubtitlesView(TextureView)
     * @see #setSubtitlesSurface(Surface, SurfaceHolder)
     */
    @MainThread
    void attachViews();

    /**
     * Detach views previously attached.
     * This will be called automatically when surfaces are destroyed.
     */
    @MainThread
    void detachViews();

    /**
     * Return true if views are attached. If surfaces were destroyed, this will return false.
     */
    @MainThread
    boolean areViewsAttached();

    /**
     * Add a callback to receive {@link Callback#onNewLayout} events.
     */
    @MainThread
    void addCallback(Callback callback);

    /**
     * Remove a callback.
     */
    @MainThread
    void removeCallback(Callback callback);

    /**
     * Send a mouse event to the native vout.
     * @param action see ACTION_* in {@link android.view.MotionEvent}.
     * @param button see BUTTON_* in {@link android.view.MotionEvent}.
     * @param x x coordinate.
     * @param y y coordinate.
     */
    @MainThread
    void sendMouseEvent(int action, int button, int x, int y);

    /**
     * Send the the window size to the native vout.
     * @param width width of the window.
     * @param height height of the window.
     */
    @MainThread
    void setWindowSize(int width, int height);
}
