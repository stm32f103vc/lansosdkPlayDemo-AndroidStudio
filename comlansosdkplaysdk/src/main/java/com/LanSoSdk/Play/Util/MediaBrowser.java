/*****************************************************************************
 * MediaBrowser.java
 *****************************************************************************/

package com.LanSoSdk.Play.Util;

import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;

import com.LanSoSdk.Play.LibPlay;
import com.LanSoSdk.Play.Media;
import com.LanSoSdk.Play.MediaList;

public class MediaBrowser {

    private final LibPlay mLibPlay;
    private final ArrayList<Media> mDiscovererMediaArray = new ArrayList<Media>();
    private MediaList mBrowserMediaList;
    private Media mMedia;
    private EventListener mEventListener;
    private boolean mAlive;

    private static final String IGNORE_LIST_OPTION =  ":ignore-filetypes=";
    private String mIgnoreList = "db,nfo,ini,jpg,jpeg,ljpg,gif,png,pgm,pgmyuv,pbm,pam,tga,bmp,pnm,xpm,xcf,pcx,tif,tiff,lbm,sfv,txt,sub,idx,srt,cue,ssa";

    /**
     * Listener called when medias are added or removed.
     */
    public interface EventListener {
        /**
         * Received when a new media is added.
         * @param index
         * @param media
         */
        public void onMediaAdded(int index, Media media);
        /**
         * Received when a media is removed (Happens only when you discover networks)
         * @param index
         * @param media Released media, but cached attributes are still
         * available (like media.getMrl())
         */
        public void onMediaRemoved(int index, Media media);
        /**
         * Called when browse ended.
         * It won't be called when you discover networks
         */
        public void onBrowseEnd();
    }

    public MediaBrowser(LibPlay libplay, EventListener listener) {
        mLibPlay = libplay;
        mLibPlay.retain();
        mEventListener = listener;
        mAlive = true;
    }

    private synchronized void reset() {
        mDiscovererMediaArray.clear();
        if (mMedia != null) {
            mMedia.release();
            mMedia = null;
        }

        if (mBrowserMediaList != null) {
            mBrowserMediaList.release();
            mBrowserMediaList = null;
        }
    }

    /**
     * Release the MediaBrowser.
     */
    public synchronized void release() {
        reset();
        if (!mAlive)
            throw new IllegalStateException("MediaBrowser released more than one time");
        mLibPlay.release();
        mAlive = false;
    }

    /**
     * Reset this media browser and register a new EventListener
     * @param eventListener new EventListener for this browser
     */
    public synchronized void changeEventListener(EventListener eventListener){
        reset();
        mEventListener = eventListener;
    }


    /**
     * Discover networks shares using available MediaDiscoverers
     */
    public synchronized void discoverNetworkShares() {
    }

    /**
     * Discover networks shares using specified MediaDiscoverer
     * @param discovererName
     */
    public synchronized void discoverNetworkShares(String discovererName) {
        reset();
    }

    /**
     * Browse to the specified local path starting with '/'.
     *
     * @param path
     */
    public synchronized void browse(String path) {
        final Media media = new Media(mLibPlay, path);
        browse(media);
        media.release();
    }

    /**
     * Browse to the specified uri.
     *
     * @param uri
     */
    public synchronized void browse(Uri uri) {
        final Media media = new Media(mLibPlay, uri);
        browse(media);
        media.release();
    }

    /**
     * Browse to the specified media.
     *
     * @param media Can be a media returned by MediaBrowser.
     */
    public synchronized void browse(Media media) {
        /* media can be associated with a medialist,
         * so increment ref count in order to don't clean it with the medialist
         */
        media.retain();
        media.addOption(IGNORE_LIST_OPTION+mIgnoreList);
        reset();
        mBrowserMediaList = media.subItems();
        mBrowserMediaList.setEventListener(mBrowserMediaListEventListener);
        media.parseAsync(Media.Parse.ParseNetwork);
        mMedia = media;
    }

    /**
     * Get the number or media.
     */
    public synchronized int getMediaCount() {
        return mBrowserMediaList != null ? mBrowserMediaList.getCount() : mDiscovererMediaArray.size();
    }

    /**
     * Get a media at a specified index. Should be released with {@link #release()}.
     */
    public synchronized Media getMediaAt(int index) {
        if (index < 0 || index >= getMediaCount())
            throw new IndexOutOfBoundsException();
        final Media media = mBrowserMediaList != null ? mBrowserMediaList.getMediaAt(index) :
                mDiscovererMediaArray.get(index);
        media.retain();
        return media;
    }

    /**
     * Override the extensions list to be ignored in browsing
     * default is "db,nfo,ini,jpg,jpeg,ljpg,gif,png,pgm,pgmyuv,pbm,pam,tga,bmp,pnm,xpm,xcf,pcx,tif,tiff,lbm,sfv,txt,sub,idx,srt,cue,ssa"
     *
     * @param list files extensions to be ignored by browser
     */
    public synchronized void setIgnoreFileTypes(String list) {
        mIgnoreList = list;
    }

    private final MediaList.EventListener mBrowserMediaListEventListener = new MediaList.EventListener() {
        @Override
        public void onEvent(MediaList.Event event) {
            if (mEventListener == null)
                return;
            final MediaList.Event mlEvent = (MediaList.Event) event;

            /*
             * We use an intermediate array here since more than one MediaDiscoverer can be used
             */
            switch (mlEvent.type) {
            case MediaList.Event.ItemAdded:
                mEventListener.onMediaAdded(mlEvent.index, mlEvent.media);
                break;
            case MediaList.Event.ItemDeleted:
                mEventListener.onMediaRemoved(mlEvent.index, mlEvent.media);
                break;
            case MediaList.Event.EndReached:
                mEventListener.onBrowseEnd();
            }
        }
    };

    private final MediaList.EventListener mDiscovererMediaListEventListener = new MediaList.EventListener() {
        @Override
        public void onEvent(MediaList.Event event) {
            if (mEventListener == null)
                return;
            final MediaList.Event mlEvent = (MediaList.Event) event;
            int index = -1;

            /*
             * We use an intermediate array here since more than one MediaDiscoverer can be used
             */
            switch (mlEvent.type) {
            case MediaList.Event.ItemAdded:
                synchronized (MediaBrowser.this) {
                    /* one item can be found by severals discoverers */
                    boolean found = false;
                    for (Media media : mDiscovererMediaArray) {
                        if (media.getUri().equals(mlEvent.media.getUri())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        mDiscovererMediaArray.add(mlEvent.media);
                        index = mDiscovererMediaArray.size() - 1;
                    }
                }
                if (index != -1)
                    mEventListener.onMediaAdded(index, mlEvent.media);
                break;
            case MediaList.Event.ItemDeleted:
                synchronized (MediaBrowser.this) {
                    index = mDiscovererMediaArray.indexOf(mlEvent.media);
                    if (index != -1)
                        mDiscovererMediaArray.remove(index);
                }
                if (index != -1)
                    mEventListener.onMediaRemoved(index, mlEvent.media);
                break;
            case MediaList.Event.EndReached:
                mEventListener.onBrowseEnd();
            }
        }
    };
}
