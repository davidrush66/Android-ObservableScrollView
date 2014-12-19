/*
 * Copyright 2014 Soichiro Kashima
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.ksoichiro.android.observablescrollview.samples;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.nineoldandroids.view.ViewHelper;

public class SlidingUpScrollViewActivity extends ActionBarActivity implements ObservableScrollViewCallbacks {

    private View mHeader;
    private ObservableScrollView mScrollView;
    private ScrollInterceptionFrameLayout mInterceptionLayout;
    private int mIntersectionHeight;
    private int mHeaderBarHeight;
    private float mScrollYOnDownMotion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slidingupscrollview);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        mIntersectionHeight = getResources().getDimensionPixelSize(R.dimen.intersection_height);
        mHeaderBarHeight = getResources().getDimensionPixelSize(R.dimen.header_bar_height);

        mHeader = findViewById(R.id.header);
        mScrollView = (ObservableScrollView) findViewById(R.id.scroll);
        mScrollView.setScrollViewCallbacks(this);
        mInterceptionLayout = (ScrollInterceptionFrameLayout) findViewById(R.id.scroll_wrapper);
        mInterceptionLayout.setScrollInterceptionListener(mInterceptionListener);
        ((TextView) findViewById(R.id.title)).setText(getTitle());
        setTitle(null);

        ViewTreeObserver vto = mInterceptionLayout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    mInterceptionLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    mInterceptionLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                ViewHelper.setTranslationY(mInterceptionLayout, getScreenHeight() - mHeaderBarHeight);
            }
        });
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        // Translate header
        ViewHelper.setTranslationY(mHeader, scrollY);
    }

    @Override
    public void onDownMotionEvent() {
    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
    }

    private ScrollInterceptionFrameLayout.ScrollInterceptionListener mInterceptionListener = new ScrollInterceptionFrameLayout.ScrollInterceptionListener() {
        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        @Override
        public boolean shouldInterceptTouchEvent(MotionEvent ev, boolean moving, float diffY) {
            final int minInterceptionLayoutY = -mIntersectionHeight;
            return minInterceptionLayoutY < (int) ViewHelper.getY(mInterceptionLayout)
                    || !moving
                    // canScrollVertically is API level 14
                    || !mScrollView.canScrollVertically((int) -diffY);
        }

        @Override
        public void onDownMotionEvent(MotionEvent ev) {
            mScrollYOnDownMotion = mScrollView.getCurrentScrollY();
        }

        @Override
        public void onMoveMotionEvent(MotionEvent ev, float y, float diffY) {
            float translationY = ViewHelper.getTranslationY(mInterceptionLayout) - mScrollYOnDownMotion + diffY;
            if (translationY < -mIntersectionHeight) {
                translationY = -mIntersectionHeight;
            } else if (getScreenHeight() - mHeaderBarHeight < translationY) {
                translationY = getScreenHeight() - mHeaderBarHeight;
            }

            ViewHelper.setTranslationY(mInterceptionLayout, translationY);

            if (translationY < 0) {
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mInterceptionLayout.getLayoutParams();
                lp.height = (int) -translationY + getScreenHeight();
                mInterceptionLayout.requestLayout();
            }
        }

        @Override
        public void onUpOrCancelMotionEvent(MotionEvent ev) {
        }
    };

    private int getScreenHeight() {
        return findViewById(android.R.id.content).getHeight();
    }
}