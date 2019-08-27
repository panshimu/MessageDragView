package com.miaozi.messagedragview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;


/**
 * created by panshimu
 * on 2019/8/26
 */
public class MessageDragView extends View {
    //固定点
    private PointF mFixationPointF;
    //拖拽点
    private PointF mDragPointF;
    //拖拽圆的半径
    private int mDragRadius = 10;
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
    public MessageDragView(Context context) {
        this(context,null);
    }

    public MessageDragView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MessageDragView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        //画拖拽圆
        canvas.drawCircle(mDragPointF.x, mDragPointF.y, mDragRadius, mDragPaint);
        //画一个固定的圆 根据拖拽距离改固定圆的半径

        Path bezierPath = getBezierPath();
        if(bezierPath != null){
            canvas.drawCircle(mFixationPointF.x, mFixationPointF.y, mFixationRadius, mFixationPaint);
            canvas.drawPath(bezierPath,mFixationPaint);
        }
    }

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

    private void initPoint(float downX,float downY) {
        mFixationPointF = new PointF(downX,downY);
        mDragPointF = new PointF(downX,downY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                float downX = event.getX();
                float downY = event.getY();
                initPoint(downX,downY);
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                float moveY = event.getY();
                updatePoint(moveX,moveY);
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        invalidate();
        return true;
    }

    private void updatePoint(float moveX, float moveY) {
        mDragPointF.x = moveX;
        mDragPointF.y = moveY;
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

    private Path getBezierPath(){
        double distance = calculateDistance();
        mFixationRadius = (int) (mFixationMaxRadius - distance / 14);
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
}
