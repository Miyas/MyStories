package com.mjumel.mystories.tools;


import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.mjumel.mystories.R;

/**
 * This class does most of the work of wrapping the {@link PopupWindow} so it's simpler to use.
 *
 * @author qberticus
 *
 */
public class BetterPopupWindow implements OnClickListener {
        protected final View anchor;
        private final PopupWindow window;
        private View root;
        private Drawable background = null;
        protected SpannableString span = null;
        protected int spanGravity = Gravity.CENTER;

        /**
         * Create a BetterPopupWindow
         *
         * @param anchor
         *            the view that the BetterPopupWindow will be displaying 'from'
         */
        public BetterPopupWindow(View anchor) {
            this.anchor = anchor;
            this.window = new PopupWindow(anchor.getContext());

            // when a touch even happens outside of the window
            // make the window go away
            /*this.window.setTouchInterceptor(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    //BetterPopupWindow.this.window.dismiss();
                    return false;
                }
            });*/

            onCreate();
        }
        
        /**
         * Create a BetterPopupWindow
         *
         * @param anchor
         *            the view that the BetterPopupWindow will be displaying 'from'
         */
        public BetterPopupWindow(View anchor, SpannableString span, int gravity) {
            this.span = span;
            this.spanGravity = gravity;
            this.anchor = anchor;
            this.window = new PopupWindow(anchor.getContext());

            // when a touch even happens outside of the window
            // make the window go away
            /*this.window.setTouchInterceptor(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                        BetterPopupWindow.this.window.dismiss();
                        return true;
                    }
                    return false;
                }
            });*/

            onCreate();
        }
        
        
        /**
         * Anything you want to have happen when created. Probably should create a view and setup the event listeners on
         * child views.
         */
        protected void onCreate() {
        	Gen.appendLog("EventListFragment$TutorialPopup::onCreate> Starting");
            Context context = this.anchor.getContext();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            ViewGroup popupView = (ViewGroup) inflater.inflate(R.layout.popup_tutorial_layout, null);
            TextView tv = (TextView) popupView.findViewById(R.id.popup_tutorial_text);
			if (this.span != null) {
				tv.setText(this.span);
				tv.setGravity(spanGravity);
			}
            tv.setOnClickListener(this);
            this.setContentView(popupView);
        }

        /**
         * In case there is stuff to do right before displaying.
         */
        protected void onShow() {}

        @SuppressWarnings("deprecation")
		private void preShow() {
            if(this.root == null) {
                throw new IllegalStateException("setContentView was not called with a view to display.");
            }
            onShow();

            if(this.background == null) {
                this.window.setBackgroundDrawable(new BitmapDrawable());
            } else {
                this.window.setBackgroundDrawable(this.background);
            }

            // if using PopupWindow#setBackgroundDrawable this is the only values of the width and hight that make it work
            // otherwise you need to set the background of the root viewgroup
            // and set the popupwindow background to an empty BitmapDrawable
            this.window.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
            this.window.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
            this.window.setTouchable(true);
            this.window.setFocusable(true);
            this.window.setOutsideTouchable(true);

            this.window.setContentView(this.root);
        }

        public void setBackgroundDrawable(Drawable background) {
            this.background = background;
        }

        /**
         * Sets the content view. Probably should be called from {@link onCreate}
         *
         * @param root
         *            the view the popup will display
         */
        public void setContentView(View root) {
            this.root = root;
            this.window.setContentView(root);
        }

        /**
         * Will inflate and set the view from a resource id
         *
         * @param layoutResID
         */
        public void setContentView(int layoutResID) {
            LayoutInflater inflator =
                (LayoutInflater) this.anchor.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.setContentView(inflator.inflate(layoutResID, null));
        }

        /**
         * If you want to do anything when {@link dismiss} is called
         *
         * @param listener
         */
        public void setOnDismissListener(PopupWindow.OnDismissListener listener) {
            this.window.setOnDismissListener(listener);
        }
        
        /**
         * Displays like a QuickAction from the anchor view.
         *
         * @param xOffset
         *            offset in the X direction
         * @param yOffset
         *            offset in the Y direction
         */
        public void showLikeQuickAction(int xPos, int yPos) {
        	Gen.appendLog("BetterPopupWindow::showLikeQuickAction> Starting ("+xPos+","+yPos+")");
            this.preShow();

            this.window.setAnimationStyle(R.style.Animations_GrowFromBottom);

            int[] location = new int[2];
            this.anchor.getLocationOnScreen(location);

            Rect anchorRect =
                new Rect(location[0], location[1], location[0] + this.anchor.getWidth(), location[1]
            		+ this.anchor.getHeight());

            this.root.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            int rootWidth = this.root.getMeasuredWidth();
            int rootHeight = this.root.getMeasuredHeight();

            int screenWidth = this.anchor.getContext().getResources().getDisplayMetrics().widthPixels;

            int x, y;
            if (xPos == Gravity.RIGHT)
            	x = screenWidth;
            else if(xPos == Gravity.LEFT)
            	x = 0;
            else
            	x = ((screenWidth - rootWidth) / 2);
            
            if (yPos == Gravity.TOP)
            	y = anchorRect.top;
            else if (yPos == Gravity.BOTTOM)
            	y = anchorRect.bottom;
            else
            	y = ((anchorRect.bottom - anchorRect.top + rootHeight) / 2);

            Gen.appendLog("BetterPopupWindow::showLikeQuickAction> Creating window on "+x+","+y);
            this.window.showAtLocation(this.anchor, Gravity.NO_GRAVITY, x, y);
        }

        public void dismiss() {
            this.window.dismiss();
        }

		@Override
		public void onClick(View v) {
			dismiss();			
		}
}
