package com.miaozi.messagedragview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * created by panshimu
 * on 2019/8/27
 * 监听view的触摸事件
 */
public class DragViewTouchListener implements View.OnTouchListener, DragView.DragViewCallBackListener {
    //记录自己的view
    private View mView;
    private WindowManager mWindowManager;
    //构造的view
    private DragView mDragView;
    private WindowManager.LayoutParams mParams;
    //爆炸容器
    private FrameLayout mBombLayout;
    private ImageView mBombImage;
    private DragViewListener mDragViewListener;

    public DragViewTouchListener(View view, DragViewListener dragViewListener){
        mDragViewListener = dragViewListener;
        mView = view;
        mWindowManager = (WindowManager) view.getContext().getSystemService(Context.WINDOW_SERVICE);
        mDragView = new DragView(view.getContext());
        mDragView.setDragViewCallBackListener(this);
        //全屏拖动
        mParams = new WindowManager.LayoutParams();
        //设置背景透明
        mParams.format = PixelFormat.TRANSLUCENT;
        mBombLayout = new FrameLayout(view.getContext());
        mBombImage = new ImageView(view.getContext());
        mBombLayout.addView(mBombImage);
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                //按下 把自己隐藏
                mView.setVisibility(View.INVISIBLE);
                //在windowManager中构造一个一样的view
                mWindowManager.addView(mDragView,mParams);
                //初始化点  raw 相对屏幕
                //初始化固定圆的点 应该取 mView的中心点作为中心点
                int[] location = new int[2];
                mView.getLocationOnScreen(location);
                Bitmap bitmap = getBitmapByView(mView);
                mDragView.initPoint(location[0] + bitmap.getWidth()/2,location[1] + bitmap.getHeight()/2 - getStatusBarHeight(v.getContext()));
                mDragView.setDragBitmap(bitmap);

                break;
            case MotionEvent.ACTION_MOVE:
                mDragView.updatePoint(event.getRawX(),event.getRawY() - getStatusBarHeight(v.getContext()));
                break;
            case MotionEvent.ACTION_UP:
                mDragView.handleActionUp();
                break;
        }
        return true;
    }

    /**
     * 从一个view中获取bitmap
     * @param mView
     * @return
     */
    private Bitmap getBitmapByView(View mView) {
        mView.buildDrawingCache();
        Bitmap bitmap = mView.getDrawingCache();
        return bitmap;
    }

    private int getStatusBarHeight(Context context){
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if(resourceId > 0){
            return context.getResources().getDimensionPixelSize(resourceId);
        }
        return dip2px(25,context);
    }

    private int dip2px(int i, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,i,context.getResources().getDisplayMetrics());
    }

    @Override
    public void restore() {
        //显示原来的控件
        mView.setVisibility(View.VISIBLE);
        //移除添加的控件
        mWindowManager.removeView(mDragView);
    }

    /**
     * 消失回调 执行爆炸效果
     */
    @Override
    public void dismiss() {
        Log.d("TAG","90909");
        //移除添加的控件
        mWindowManager.removeView(mDragView);
//        mWindowManager.addView(mBombLayout,mParams);
//        mBombImage.setBackgroundResource();
        //执行完爆炸动画后 通知外面dismiss回调
        if(mDragViewListener!=null){
            mDragViewListener.dismiss(mView);
        }
    }

    public interface DragViewListener{
        void dismiss(View view);
    }
}
