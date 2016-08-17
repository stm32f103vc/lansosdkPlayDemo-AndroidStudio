/*****************************************************************
 *Email: support@lansongtech.com
 * public class IAWindowNativeHandler.java
 *
 *
 *这个程序仅仅是演示版本，仅是功能上的呈现，不保证性能和适用性。如正好满足您的项目，我们深感荣幸。
 *我们有更专业稳定强大的发行版本，期待和您进一步的合作。
 *
 *Email:  support@lansongtech.com
 *****************************************************************/

package com.LanSoSdk.Play;

import android.view.Surface;

interface IAWindowNativeHandler {
    /**
     * Callback called from {@link IPlayVout#sendMouseEvent}.
     *
     * @param nativeHandle handle passed by {@link #setCallback}.
     * @param action see ACTION_* in {@link android.view.MotionEvent}.
     * @param button see BUTTON_* in {@link android.view.MotionEvent}.
     * @param x x coordinate.
     * @param y y coordinate.
     */
    void nativeOnMouseEvent(long nativeHandle, int action, int button, int x, int y);

    /**
     * Callback called from {@link IPlayVout#setWindowSize}.
     *
     * @param nativeHandle handle passed by {@link #setCallback}.
     * @param width width of the window.
     * @param height height of the window.
     */
    void nativeOnWindowSize(long nativeHandle, int width, int height);

    /**
     * Get the valid Video surface.
     *
     * @return can be null if the surface was destroyed.
     */
    @SuppressWarnings("unused") /* Used by JNI */
    Surface getVideoSurface();

    @SuppressWarnings("unused") /* Used by JNI  */
    Surface getVideoSurface2();
    /**
     * Get the valid Subtitles surface.
     *
     * @return can be null if the surface was destroyed.
     */
    @SuppressWarnings("unused") /* Used by JNI */
    Surface getSubtitlesSurface();

    /**
     * Set a callback in order to receive {@link #nativeOnMouseEvent} and {@link #nativeOnWindowSize} events.
     *
     * @param nativeHandle native Handle passed by {@link #nativeOnMouseEvent} and {@link #nativeOnWindowSize}
     * @return true if callback was successfully registered
     */
    @SuppressWarnings("unused") /* Used by JNI */
    boolean setCallback(long nativeHandle);

    /**
     * This method is only used for ICS and before since ANativeWindow_setBuffersGeometry doesn't work before.
     * It is synchronous.
     *
     * @param surface surface returned by getVideoSurface or getSubtitlesSurface
     * @param width surface width
     * @param height surface height
     * @param format color format (or PixelFormat)
     * @return true if buffersGeometry were set (only before ICS)
     */
    @SuppressWarnings("unused") /* Used by JNI */
    boolean setBuffersGeometry(Surface surface, int width, int height, int format);

    /**
     * Set the window Layout.
     * This call will result of {@link IPlayVout.Callback#onNewLayout} being called from the main thread.
     *
     * @param width Frame width
     * @param height Frame height
     * @param visibleWidth Visible frame width
     * @param visibleHeight Visible frame height
     * @param sarNum Surface aspect ratio numerator
     * @param sarDen Surface aspect ratio denominator
     */
    @SuppressWarnings("unused") /* Used by JNI */
    void setWindowLayout(int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen);
}