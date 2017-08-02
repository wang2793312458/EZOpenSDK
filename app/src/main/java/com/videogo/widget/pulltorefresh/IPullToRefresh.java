/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.videogo.widget.pulltorefresh;

import android.view.View;
import android.view.animation.Interpolator;

public interface IPullToRefresh<T extends View> {

    public static enum Mode {

        /**
         * Disable all Pull-to-Refresh gesture and Refreshing handling
         */
        DISABLED(0x0),

        /**
         * Only allow the user to Pull from the start of the Refreshable View to refresh. The start
         * is either the Top or Left, depending on the scrolling direction.
         */
        PULL_FROM_START(0x1),

        /**
         * Only allow the user to Pull from the end of the Refreshable View to refresh. The start is
         * either the Bottom or Right, depending on the scrolling direction.
         */
        PULL_FROM_END(0x2),

        /**
         * Allow the user to both Pull from the start, from the end to refresh.
         */
        BOTH(0x3),

        /**
         * Disables Pull-to-Refresh gesture handling, but allows manually setting the Refresh state
         * via {@link PullToRefreshBase#setRefreshing() setRefreshing()}.
         */
        MANUAL_REFRESH_ONLY(0x4);

        /**
         * Maps an int to a specific mode. This is needed when saving state, or inflating the view
         * from XML where the mode is given through a attr int.
         * 
         * @param modeInt
         *            - int to map a Mode to
         * @return Mode that modeInt maps to, or PULL_FROM_START by default.
         */
        static Mode mapIntToValue(final int modeInt) {
            for (Mode value : Mode.values()) {
                if (modeInt == value.getIntValue()) {
                    return value;
                }
            }

            // If not, return default
            return getDefault();
        }

        static Mode getDefault() {
            return DISABLED;
        }

        private int mIntValue;

        // The modeInt values need to match those from attrs.xml
        Mode(int modeInt) {
            mIntValue = modeInt;
        }

        /**
         * @return true if the mode permits Pull-to-Refresh
         */
        boolean permitsPullToRefresh() {
            return !(this == DISABLED || this == MANUAL_REFRESH_ONLY);
        }

        /**
         * @return true if this mode wants the Loading Layout Header to be shown
         */
        public boolean showHeaderLoadingLayout() {
            return this == PULL_FROM_START || this == BOTH;
        }

        /**
         * @return true if this mode wants the Loading Layout Footer to be shown
         */
        public boolean showFooterLoadingLayout() {
            return this == PULL_FROM_END || this == BOTH || this == MANUAL_REFRESH_ONLY;
        }

        int getIntValue() {
            return mIntValue;
        }
    }

    public static enum State {

        /**
         * When the UI is in a state which means that user is not interacting with the
         * Pull-to-Refresh function.
         */
        RESET(0x0),

        /**
         * When the UI is being pulled by the user, but has not been pulled far enough so that it
         * refreshes when released.
         */
        PULL_TO_REFRESH(0x1),

        /**
         * When the UI is being pulled by the user, and <strong>has</strong> been pulled far enough
         * so that it will refresh when released.
         */
        RELEASE_TO_REFRESH(0x2),

        /**
         * When the UI is currently refreshing, caused by a pull gesture.
         */
        REFRESHING(0x8),

        /**
         * When the UI is currently refreshing, caused by a call to
         * {@link PullToRefreshBase#setRefreshing() setRefreshing()}.
         */
        MANUAL_REFRESHING(0x9),

        /**
         * When the UI is currently overscrolling, caused by a fling on the Refreshable View.
         */
        OVERSCROLLING(0x10);

        /**
         * Maps an int to a specific state. This is needed when saving state.
         * 
         * @param stateInt
         *            - int to map a State to
         * @return State that stateInt maps to
         */
        static State mapIntToValue(final int stateInt) {
            for (State value : State.values()) {
                if (stateInt == value.getIntValue()) {
                    return value;
                }
            }

            // If not, return default
            return RESET;
        }

        private int mIntValue;

        State(int intValue) {
            mIntValue = intValue;
        }

        int getIntValue() {
            return mIntValue;
        }
    }

    /**
     * Simple Listener to listen for any callbacks to Refresh.
     * 
     * @author Chris Banes
     */
    public static interface OnRefreshListener<V extends View> {

        /**
         * onRefresh will be called for both a Pull from start, and Pull from end
         */
        public void onRefresh(final PullToRefreshBase<V> refreshView, boolean headerOrFooter);

    }

    /**
     * Listener that allows you to be notified when the user has started or finished a touch event.
     * Useful when you want to append extra UI events (such as sounds). See (
     * {@link PullToRefreshAdapterViewBase#setOnPullEventListener}.
     * 
     * @author Chris Banes
     */
    public static interface OnPullEventListener<V extends View> {

        /**
         * Called when the internal state has been changed, usually by the user pulling.
         * 
         * @param refreshView
         *            - View which has had it's state change.
         * @param state
         *            - The new state of View.
         * @param direction
         *            - One of {@link Mode#PULL_FROM_START} or {@link Mode#PULL_FROM_END} depending
         *            on which direction the user is pulling. Only useful when <var>state</var> is
         *            {@link State#PULL_TO_REFRESH} or {@link State#RELEASE_TO_REFRESH}.
         */
        public void onPullEvent(final PullToRefreshBase<V> refreshView, State state, Mode direction);

    }

    /**
     * Demos the Pull-to-Refresh functionality to the user so that they are aware it is there. This
     * could be useful when the user first opens your app, etc. The animation will only happen if
     * the Refresh View (ListView, ScrollView, etc) is in a state where a Pull-to-Refresh could
     * occur by a user's touch gesture (i.e. scrolled to the top/bottom).
     * 
     * @return true - if the Demo has been started, false if not.
     */
    public boolean demo();

    /**
     * Get the mode that this view is currently in. This is only really useful when using
     * <code>Mode.BOTH</code>.
     * 
     * @return Mode that the view is currently in
     */
    public Mode getCurrentMode();

    /**
     * Returns whether the Touch Events are filtered or not. If true is returned, then the View will
     * only use touch events where the difference in the Y-axis is greater than the difference in
     * the X-axis. This means that the View will not interfere when it is used in a horizontal
     * scrolling View (such as a ViewPager).
     * 
     * @return boolean - true if the View is filtering Touch Events
     */
    public boolean getFilterTouchEvents();

    /**
     * Returns a proxy object which allows you to call methods on all of the LoadingLayouts (the
     * Views which show when Pulling/Refreshing).
     * <p />
     * You should not keep the result of this method any longer than you need it.
     * 
     * @return Object which will proxy any calls you make on it, to all of the LoadingLayouts.
     */
    public LoadingLayoutProxy getLoadingLayoutProxy();

    /**
     * Returns a proxy object which allows you to call methods on the LoadingLayouts (the Views
     * which show when Pulling/Refreshing). The actual LoadingLayout(s) which will be affected, are
     * chosen by the parameters you give.
     * <p />
     * You should not keep the result of this method any longer than you need it.
     * 
     * @param includeStart
     *            - Whether to include the Start/Header Views
     * @param includeEnd
     *            - Whether to include the End/Footer Views
     * @return Object which will proxy any calls you make on it, to the LoadingLayouts included.
     */
    public LoadingLayoutProxy getLoadingLayoutProxy(boolean includeStart, boolean includeEnd);

    /**
     * Get the mode that this view has been set to. If this returns <code>Mode.BOTH</code>, you can
     * use <code>getCurrentMode()</code> to check which mode the view is currently in
     * 
     * @return Mode that the view has been set to
     */
    public Mode getMode();

    /**
     * Get the Wrapped Refreshable View. Anything returned here has already been added to the
     * content view.
     * 
     * @return The View which is currently wrapped
     */
    public T getRefreshableView();

    /**
     * Get whether the 'Refreshing' View should be automatically shown when refreshing. Returns true
     * by default.
     * 
     * @return - true if the Refreshing View will be show
     */
    public boolean getShowViewWhileRefreshing();

    /**
     * @return - The state that the View is currently in.
     */
    public State getState();

    /**
     * Whether Pull-to-Refresh is enabled
     * 
     * @return enabled
     */
    public boolean isPullToRefreshEnabled();

    /**
     * Gets whether Overscroll support is enabled. This is different to Android's standard
     * Overscroll support (the edge-glow) which is available from GINGERBREAD onwards
     * 
     * @return true - if both PullToRefresh-OverScroll and Android's inbuilt OverScroll are enabled
     */
    public boolean isPullToRefreshOverScrollEnabled();

    /**
     * Returns whether the Widget is currently in the Refreshing mState
     * 
     * @return true if the Widget is currently refreshing
     */
    public boolean isRefreshing();

    /**
     * Returns whether the widget has enabled scrolling on the Refreshable View while refreshing.
     * 
     * @return true if the widget has enabled scrolling while refreshing
     */
    public boolean isScrollingWhileRefreshingEnabled();

    /**
     * Mark the current Refresh as complete. Will Reset the UI and hide the Refreshing View
     */
    public void onRefreshComplete();

    /**
     * Set the Touch Events to be filtered or not. If set to true, then the View will only use touch
     * events where the difference in the Y-axis is greater than the difference in the X-axis. This
     * means that the View will not interfere when it is used in a horizontal scrolling View (such
     * as a ViewPager), but will restrict which types of finger scrolls will trigger the View.
     * 
     * @param filterEvents
     *            - true if you want to filter Touch Events. Default is true.
     */
    public void setFilterTouchEvents(boolean filterEvents);

    /**
     * Set the mode of Pull-to-Refresh that this view will use.
     * 
     * @param mode
     *            - Mode to set the View to
     */
    public void setMode(Mode mode);

    /**
     * Set OnPullEventListener for the Widget
     * 
     * @param listener
     *            - Listener to be used when the Widget has a pull event to propogate.
     */
    public void setOnPullEventListener(OnPullEventListener<T> listener);

    /**
     * Set OnRefreshListener for the Widget
     * 
     * @param listener
     *            - Listener to be used when the Widget is set to Refresh
     */
    public void setOnRefreshListener(OnRefreshListener<T> listener);

    /**
     * Sets whether Overscroll support is enabled. This is different to Android's standard
     * Overscroll support (the edge-glow). This setting only takes effect when running on device
     * with Android v2.3 or greater.
     * 
     * @param enabled
     *            - true if you want Overscroll enabled
     */
    public void setPullToRefreshOverScrollEnabled(boolean enabled);

    /**
     * Sets the Widget to be in the refresh state. The UI will be updated to show the 'Refreshing'
     * view, and be scrolled to show such.
     */
    public void setRefreshing();

    /**
     * Sets the Widget to be in the refresh state. The UI will be updated to show the 'Refreshing'
     * view.
     * 
     * @param doScroll
     *            - true if you want to force a scroll to the Refreshing view.
     */
    public void setRefreshing(boolean doScroll);

    /**
     * Sets the Animation Interpolator that is used for animated scrolling. Defaults to a
     * DecelerateInterpolator
     * 
     * @param interpolator
     *            - Interpolator to use
     */
    public void setScrollAnimationInterpolator(Interpolator interpolator);

    /**
     * By default the Widget disables scrolling on the Refreshable View while refreshing. This
     * method can change this behaviour.
     * 
     * @param scrollingWhileRefreshingEnabled
     *            - true if you want to enable scrolling while refreshing
     */
    public void setScrollingWhileRefreshingEnabled(boolean scrollingWhileRefreshingEnabled);

    /**
     * A mutator to enable/disable whether the 'Refreshing' View should be automatically shown when
     * refreshing.
     * 
     * @param showView
     */
    public void setShowViewWhileRefreshing(boolean showView);

}