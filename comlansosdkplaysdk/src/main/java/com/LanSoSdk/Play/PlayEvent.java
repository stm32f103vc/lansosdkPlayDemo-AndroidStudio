/*****************************************************************
 *Email: support@lansongtech.com
 * PlayEvent.java
 *
 *
 *这个程序仅仅是演示版本，仅是功能上的呈现，不保证性能和适用性。如正好满足您的项目，我们深感荣幸。
 *我们有更专业稳定强大的发行版本，期待和您进一步的合作。
 *
 *Email: support@lansongtech.com
 *****************************************************************/
package com.LanSoSdk.Play;

abstract class PlayEvent {
    public final int type;
    protected PlayEvent(int type) {
        this.type = type;
    }

    /**
     * Listener for libplay events
     *
     * @see PlayEvent
     */
    public interface Listener<T extends PlayEvent> {
        void onEvent(T event);
    }
}
