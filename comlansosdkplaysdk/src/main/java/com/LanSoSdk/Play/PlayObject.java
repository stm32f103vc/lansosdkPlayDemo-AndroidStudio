/************************************************************
 *Email: support@lansongtech.com
 * PlayObject.java
 *
 *
 *这个程序仅仅是演示版本，仅是功能上的呈现，不保证性能和适用性。如正好满足您的项目，我们深感荣幸。
 *我们有更专业稳定强大的发行版本，期待和您进一步的合作。
 *
 *
 ************************************************************/

package com.LanSoSdk.Play;

import android.os.Handler;
import android.os.Looper;

import java.lang.ref.WeakReference;

abstract class PlayObject<T extends PlayEvent> {
    private PlayEvent.Listener<T> mEventListener = null;
    private Handler mHandler = null;
    private int mNativeRefCount = 1;

    /**
     * Returns true if native object is released
     */
    public synchronized boolean isReleased() {
        return mNativeRefCount == 0;
    }

    /**
     * Increment internal ref count of the native object.
     * @return true if media is retained
     */
    public synchronized final boolean retain() {
        if (mNativeRefCount > 0) {
            mNativeRefCount++;
            return true;
        } else
            return false;
    }

    /**
     * Release the native object if ref count is 1.
     *
     * After this call, native calls are not possible anymore.
     * You can still call others methods to retrieve cached values.
     * For example: if you parse, then release a media, you'll still be able to retrieve all Metas or Tracks infos.
     */
    public final void release() {
        int refCount = -1;
        synchronized (this) {
            if (mNativeRefCount == 0)
                return;
            if (mNativeRefCount > 0) {
                refCount = --mNativeRefCount;
            }
            // clear event list
            if (refCount == 0)
                setEventListener(null);
        }
        if (refCount == 0) {
            nativeDetachEvents();
            synchronized (this) {
                onReleaseNative();
            }
        }
    }


   

    /**
     * Called when native object is released (refcount is 0).
     *
     * This is where you must release native resources.
     */
    protected abstract void onReleaseNative();

    
    //=========================================================================================================
    
    /**
     * Set an event listener.
     * Events are sent via the android main thread.
     *
     * @param listener see {@link PlayEvent.Listener}
     */
    protected synchronized void setEventListener(PlayEvent.Listener<T> listener) {
        if (mHandler != null)
            mHandler.removeCallbacksAndMessages(null);
        mEventListener = listener;
        if (mEventListener != null && mHandler == null)
            mHandler = new Handler(Looper.getMainLooper());
    }

    
    /**
     * Called when libplay send events.
     *
     * @param eventType event type
     * @param arg1 first argument
     * @param arg2 second argument
     * @return Event that will be dispatched to listeners
     */
    protected abstract T onEventNative(int eventType, long arg1, float arg2);
    
    /* JNI */
    @SuppressWarnings("unused") /* Used from JNI */
    private long mInstance = 0;
    private synchronized void dispatchEventFromNative(int eventType, long arg1, float arg2) {  
        if (isReleased())
            return;
        final T event = onEventNative(eventType, arg1, arg2); 

        class EventRunnable implements Runnable 
        {
            private final PlayEvent.Listener<T> listener;
            private final T event;

            private EventRunnable(PlayEvent.Listener<T> listener, T event) {
                this.listener = listener;
                this.event = event;
            }
            @Override
            public void run() {
                listener.onEvent(event);
            }
        }
        if (event != null && mEventListener != null && mHandler != null)
            mHandler.post(new EventRunnable(mEventListener, event));
    }
    private native void nativeDetachEvents();
  
}
