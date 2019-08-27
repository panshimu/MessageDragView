package com.miaozi.messagedragview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.OvershootInterpolator;


/**
 * created by panshimu
 * on 2019/8/26
 */
public class DragView extends View {
    //固定点
    private PointF mFixationPointF;
    //拖拽点
    private PointF mDragPointF;
    //拖拽圆的半径
    private int mDragRadius = 15;
    //固定圆的半径
    private int mFixationRadius;
    //固定圆最大半径
    private int mFixationMaxRadius = 8;
    //固定圆最小半径
    private int mFixationMinRadius = 2;
    //固定圆的画笔
    private Paint mFixationPaint;
    //拖拽圆画笔
    private Paint mDragPaint;
    private Bitmap mDragBitmap;

    public DragView(Context context) {
        this(context,null);
    }

    public DragView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public DragView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mDragRadius = dip2px(mDragRadius);
        mFixationMaxRadius = dip2px(mFixationMaxRadius);
        mFixationMinRadius = dip2px(mFixationMinRadius);
        initPaint();
    }

    private int dip2px(int mDragRadius) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,mDragRadius,getResources().getDisplayMetrics());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mDragPointF == null || mDragPaint == null) {
            return;
        }
        if(this.mDragBitmap != null){
            int width = mDragBitmap.getWidth() > mDragBitmap.getHeight() ? mDragBitmap.getWidth() : mDragBitmap.getHeight();
            mDragRadius = width / 4;
        }
        //画拖拽圆
        canvas.drawCircle(mDragPointF.x, mDragPointF.y, mDragRadius, mDragPaint);

        //画一个固定的圆 根据拖拽距离改固定圆的半径
        canvas.drawCircle(mFixationPointF.x, mFixationPointF.y, mFixationRadius, mFixationPaint);

        Path bezierPath = getBezierPath();
        if(bezierPath != null){
            canvas.drawPath(bezierPath,mFixationPaint);
        }

        if(this.mDragBitmap != null){
            canvas.drawBitmap(mDragBitmap,mDragPointF.x - mDragBitmap.getWidth()/2,mDragPointF.y-mDragBitmap.getHeight()/2,null);
        }
    }

    /**
     * 初始化 画笔
     */
    private void initPaint() {
        mDragPaint = new Paint();
        mDragPaint.setColor(Color.RED);
        mDragPaint.setAntiAlias(true);
        //设置防抖动
        mDragPaint.setDither(true);

        mFixationPaint = new Paint();
        mFixationPaint.setColor(Color.RED);
        mFixationPaint.setAntiAlias(true);
        //设置防抖动
        mFixationPaint.setDither(true);
    }

    /**
     * 初始化座标点
     * @param downX
     * @param downY
     */
    public void initPoint(float downX,float downY) {
        mFixationPointF = new PointF(downX,downY);
        mDragPointF = new PointF(downX,downY);
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        switch (event.getAction()){
//            case MotionEvent.ACTION_DOWN:
//                float downX = event.getX();
//                float downY = event.getY();
//                initPoint(downX,downY);
//                break;
//            case MotionEvent.ACTION_MOVE:
//                float moveX = event.getX();
//                float moveY = event.getY();
//                updatePoint(moveX,moveY);
//                break;
//            case MotionEvent.ACTION_UP:
//                break;
//        }
//        invalidate();
//        return true;
//    }

    /**
     * 更新座标点
     * @param moveX
     * @param moveY
     */
    public void updatePoint(float moveX, float moveY) {
        mDragPointF.x = moveX;
        mDragPointF.y = moveY;
        invalidate();
    }

    /**
     * 计算两个点之前的距离
     * @return
     */
    private double calculateDistance(){
        double dx = Math.pow(mDragPointF.x - mFixationPointF.x,2);
        double dy = Math.pow(mDragPointF.y - mFixationPointF.y,2);
        return Math.sqrt(dx + dy);
    }

    /**
     * 获取贝塞尔曲线的 path
     * @return
     */
    private Path getBezierPath(){

        double distance = calculateDistance();

        //比例 这个数可以自己随便定义一个数 比如 14 主要是看效果是否符合自己的效果 测试就知道了
        int bili = mDragRadius;
        mFixationRadius = (int) (mFixationMaxRadius - distance / bili);
        if (mFixationRadius < mFixationMinRadius) {
            return null;
        }
        Path bezierPath = new Path();

        float dy = mDragPointF.y - mFixationPointF.y;
        float dx = mDragPointF.x - mFixationPointF.x;

        float tanA = dy / dx;
        //求角A 反tanA
        double arcTanA = Math.atan(tanA);
        //p0
        float p0x = (float) (mFixationPointF.x + mFixationRadius * Math.sin(arcTanA));
        float p0y = (float)(mFixationPointF.y - mFixationRadius * Math.cos(arcTanA));

        //p1
        float p1x = (float)(mDragPointF.x + mDragRadius * Math.sin(arcTanA));
        float p1y = (float)(mDragPointF.y - mDragRadius * Math.cos(arcTanA));

        //p2
        float p2x = (float)(mDragPointF.x - mDragRadius * Math.sin(arcTanA));
        float p2y = (float)(mDragPointF.y + mDragRadius * Math.cos(arcTanA));

        //p3
        float p3x = (float) (mFixationPointF.x - mFixationRadius * Math.sin(arcTanA));
        float p3y = (float)(mFixationPointF.y + mFixationRadius * Math.cos(arcTanA));

        //拼装 贝塞尔曲线
        bezierPath.moveTo(p0x,p0y);
        //获取控制点座标 定在 两点的中心点
        PointF controllerPointF = getControllerPointF();
        //画第一条
        bezierPath.quadTo(controllerPointF.x,controllerPointF.y,p1x,p1y);
        //画第二条
        bezierPath.lineTo(p2x,p2y);
        bezierPath.quadTo(controllerPointF.x,controllerPointF.y,p3x,p3y);

        bezierPath.close();

        return bezierPath;
    }

    /**
     * 获取控制点（中心点）
     * @return
     */
    private PointF getControllerPointF() {
        return new PointF((mDragPointF.x + mFixationPointF.x)/2,(mDragPointF.y + mFixationPointF.y)/2);
    }


    /**
     * 绑定可以拖拽的控件
     * @param view
     * @param dragViewListener
     */
    public static void bindDragView(View view, DragViewTouchListener.DragViewListener dragViewListener) {
        view.setOnTouchListener(new DragViewTouchListener(view,dragViewListener));
    }

    /**
     * 设置bitmao
     * @param bitmap
     */
    public void setDragBitmap(Bitmap bitmap) {
        this.mDragBitmap = bitmap;
        invalidate();
    }

    /**
     * 处理手指松开
     */
    public void handleActionUp() {
        //回弹
        if(mFixationRadius > mFixationMinRadius){
            ValueAnimator animator = ObjectAnimator.ofFloat(1);
            animator.setDuration(350);
            final PointF start = new PointF(mDragPointF.x,mDragPointF.y);
            final PointF end = new PointF(mFixationPointF.x,mFixationPointF.y);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float animatedValue = (float) animation.getAnimatedValue();
                    PointF pointByPercent = getPointByPercent(start, end, animatedValue);
                    updatePoint(pointByPercent.x,pointByPercent.y);
                }
            });
            //差值器 回到原来的位置都向前甩 然后回到原来位置
            animator.setInterpolator(new OvershootInterpolator(3f));
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if(mDragViewCallBackListener!=null){
                        mDragViewCallBackListener.restore();
                    }
                }
            });
            animator.start();
        }else {
            //爆炸
            if(mDragViewCallBackListener!=null){
                mDragViewCallBackListener.dismiss();
            }
        }
    }

    /**
     * 根据百分比计算点的座标
     * @param start
     * @param end
     * @param percent
     * @return
     */
    private PointF getPointByPercent(PointF start,PointF end,float percent){
        return new PointF(evaluateValue(start.x,end.x,percent),evaluateValue(start.y,end.y,percent));
    }
    private float evaluateValue(Number start,Number end,float fraction){
        return start.floatValue() + (end.floatValue() - start.floatValue()) * fraction;
    }

    public DragViewCallBackListener mDragViewCallBackListener;

    public void setDragViewCallBackListener(DragViewCallBackListener dragViewCallBackListener) {
        this.mDragViewCallBackListener = dragViewCallBackListener;
    }

    public interface DragViewCallBackListener{
        void restore();
        void dismiss();
    }
}
