package com.sziti.dashboardview;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.constraintlayout.solver.widgets.Rectangle;

/**
 * 自定义仪表盘
 */
public class DashBoardView extends View {
	private final String[] texts = {"10", "20", "30", "40", "50", "60", "70", "80", "90", "100", "110", "120", "130"};//显示的字体
	private int mRadius; // 外圆环的最短高度
	private int mStrokeWidth; // 画笔宽度
	private int mTextSize = dp2px(20);
	private int mLength1; // 长刻度的相对圆弧的距离
	private int mLength2; // 刻度读数顶部的相对圆弧的距离
	private int progressWidth;//进度条宽度
	private Paint mPaint;
	private RectF mRectFArc;//外圆环 实际上是一个带圆角的矩形
	private RectF mRectFInnerLeftArc;//内左圆环
	private RectF mRectFInnerRightArc;//内右圆环
	private RectF mRectFInnerRect;//内矩形
	private RectF mRectFShowArc;//提示信息显示
	private int mPadding = 30;//内边距基准值
	private int mInnerCircleWidth = dp2px(80);//左右圆弧的距离  用于控制中间矩形的宽度大小
	private float mCircleLeftX, mCircleLeftY; // 左圆弧中心坐标
	private float mCircleRightX, mCircleRightY;//右圆弧中心坐标
	private int mViewWidth; // 控件宽度
	private int mViewHeight; // 控件高度
	private float mViewCenterX, mViewCenterY;
	private float[][] text_points = new float[texts.length][2];//保存各个text的位置对应的指示线的位置
	private int texts_index = 0;//作为刻度坐标位置的指针
	private int progress = 0;

	public DashBoardView(Context context) {
		this(context, null);
	}

	public DashBoardView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DashBoardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public DashBoardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	private void init() {
		mStrokeWidth = dp2px(50);
		progressWidth = dp2px(10);
//		mLength1 = dp2px(8) + mStrokeWidth;
//		mLength2 = mLength1 + dp2px(4);

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
//		mPaint.setStrokeCap(Paint.Cap.ROUND);

		mRectFArc = new RectF();
		//内圆环的各个部件坐标
		mRectFInnerLeftArc = new RectF();
		mRectFInnerRect = new RectF();
		mRectFInnerRightArc = new RectF();

		mRectFShowArc = new RectF();
//		mRectText = new Rect();

//		mTexts = new String[mSection + 1]; // 需要显示mSection + 1个刻度读数
//		for (int i = 0; i < mTexts.length; i++) {
//			int n = (mMax - mMin) / mSection;
//			mTexts[i] = String.valueOf(mMin + i * n);
//		}

//		mColors = new int[]{ContextCompat.getColor(getContext(), R.color.color_green),
//			ContextCompat.getColor(getContext(), R.color.color_yellow),
//			ContextCompat.getColor(getContext(), R.color.color_red)};
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//		mPadding = Math.max(
//			Math.max(getPaddingLeft(), getPaddingTop()),
//			Math.max(getPaddingRight(), getPaddingBottom())
//		);

		Log.i("zxb", "内边距:" + mPadding);
		setPadding(dp2px(mPadding * 2), dp2px(mPadding), dp2px(mPadding * 2), dp2px(mPadding));

		int widthMode = MeasureSpec.getMode(widthMeasureSpec);//从约束规范中获取模式
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);//从约束规范中获取尺寸
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		//在布局中设置了具体值
		if (widthMode == MeasureSpec.EXACTLY)
			mViewWidth = widthSize;

		//在布局中设置 wrap_content，控件就取能完全展示内容的宽度（同时需要考虑屏幕的宽度）
		if (widthMode == MeasureSpec.AT_MOST)
			mViewWidth = Math.min(mViewWidth, widthSize);

		//高度和宽度是2:1的关系
		mViewHeight = mViewWidth / 2;
//		if (heightMode == MeasureSpec.EXACTLY) {
//			mViewHeight = heightSize;
//		} else {
////			float[] point1 = getCoordinatePoint(mRadius, mStartAngle);
////			float[] point2 = getCoordinatePoint(mRadius, mStartAngle + mSweepAngle);
////			float maxY = Math.max(Math.abs(point1[1]) - mCenterY, Math.abs(point2[1]) - mCenterY);
////			float f = mCircleRadius + dpToPx(2) + dpToPx(25);
////			float max = Math.max(maxY, f);
////			mViewHeight = (int) (max + mRadius + getPaddingTop() + getPaddingBottom() + dpToPx(2) * 2);
//			//暂时将高度设置为控件宽度的一半
//			mViewHeight = mRadius + getPaddingTop() * 2 + mStrokeWidth * 2 + dp2px(2) * 2;
//			Log.i("zxb", "计算高度:" + mViewHeight);
//			if (heightMode == MeasureSpec.AT_MOST)
//				mViewHeight = Math.min(mViewHeight, heightSize);
//		}
		//保存测量宽度和测量高度
		setMeasuredDimension(mViewWidth, mViewHeight);

		mRadius = mViewHeight / 4;
		mViewCenterX = mViewWidth / 2;
		mViewCenterY = mViewHeight / 2;
		//设置左圆圆弧圆心坐标
		mCircleLeftX = mViewCenterX - mInnerCircleWidth / 2f - mRadius;
		mCircleLeftY = mViewCenterY;

		mCircleRightX = mViewCenterX + mInnerCircleWidth / 2f + mRadius;
		mCircleRightY = mViewCenterY;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		//画内部左圆环
		mPaint.setStyle(Paint.Style.FILL);//充满
		mPaint.setColor(Color.GRAY);
		canvas.drawArc(mCircleLeftX - mRadius, mCircleLeftY - mRadius, mCircleLeftX + mRadius, mCircleLeftY + mRadius,
			90, 180, true, mPaint);
		//画中间的矩形
		canvas.drawRect(mCircleLeftX, mCircleLeftY - mRadius, mCircleRightX, mCircleRightY + mRadius, mPaint);
		//画内部右圆环
		canvas.drawArc(mCircleRightX - mRadius, mCircleRightY - mRadius, mCircleRightX + mRadius, mCircleRightY + mRadius,
			270, 180, true, mPaint);

		//画外部的左圆环
		mPaint.setStrokeWidth(mStrokeWidth);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setColor(Color.BLACK);
		canvas.drawArc(mCircleLeftX - mRadius - progressWidth - mStrokeWidth / 2f, mCircleLeftY - mRadius - progressWidth - mStrokeWidth / 2f,
			mCircleLeftX + mRadius + progressWidth + mStrokeWidth / 2f, mCircleLeftY + mRadius + progressWidth + mStrokeWidth / 2f, 89, 182, false, mPaint);
		//画外部的右圆环
		canvas.drawArc(mCircleRightX - mRadius - progressWidth - mStrokeWidth / 2f, mCircleRightY - mRadius - progressWidth - mStrokeWidth / 2f,
			mCircleRightX + mRadius + progressWidth + mStrokeWidth / 2f, mCircleRightY + mRadius + progressWidth + mStrokeWidth / 2f,
			269, 182, false, mPaint);
		//画连接左右圆环的直线
		canvas.drawLine(mCircleLeftX, mCircleLeftY - mRadius - mStrokeWidth / 2f - progressWidth,
			mCircleRightX, mCircleRightY - mRadius - progressWidth - mStrokeWidth / 2f, mPaint);

		canvas.drawLine(mCircleLeftX, mCircleLeftY + mRadius + mStrokeWidth / 2f + progressWidth,
			mCircleRightX, mCircleRightY + mRadius + progressWidth + mStrokeWidth / 2f, mPaint);

		//画进度条 直线=》圆弧=》直线=》圆弧=》直线
		mPaint.setStrokeWidth(progressWidth);
		mPaint.setColor(Color.GREEN);


		drawProgress(canvas, null);

//		canvas.drawLine(mViewCenterX - mRadius / 2f - dp2px(10), mCircleLeftY + mRadius + progressWidth / 2f, mCircleLeftX, mCircleLeftY + mRadius + progressWidth / 2f, mPaint);
//		canvas.drawArc(mCircleLeftX - mRadius - progressWidth / 2f, mCircleLeftY - mRadius - progressWidth / 2f,
//			mCircleLeftX + mRadius + progressWidth / 2f, mCircleLeftY + mRadius + progressWidth / 2f, 89, 182, false, mPaint);
//		canvas.drawLine(mCircleLeftX, mCircleLeftY - mRadius - progressWidth / 2f,
//			mCircleRightX, mCircleRightY - mRadius - progressWidth / 2f, mPaint);
//		canvas.drawArc(mCircleRightX - mRadius - progressWidth / 2f, mCircleRightY - mRadius - progressWidth / 2f,
//			mCircleRightX + mRadius + progressWidth / 2f, mCircleRightY + mRadius + progressWidth / 2f, 269, 182, false, mPaint);
//		canvas.drawLine(mCircleRightX, mCircleRightY + mRadius + progressWidth / 2f, mViewCenterX + mRadius / 2f + dp2px(10), mCircleRightY + mRadius + progressWidth / 2f, mPaint);
		//画显示矩形
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setColor(Color.GREEN);
		canvas.drawRoundRect(mViewCenterX - mRadius / 2f - dp2px(10), mViewCenterY + mRadius / 2f, mViewCenterX + mRadius / 2f + dp2px(10), mViewCenterY + mRadius + progressWidth + mStrokeWidth + dp2px(10), 10, 10, mPaint);
		//画文字刻度
		mPaint.setTextSize(mTextSize);
		for (int i = 0; i < texts.length; i++) {
			if (i == 0 || i == 12) {
				if (i == 0) {
					canvas.drawText(texts[0], mCircleLeftX - mTextSize / 2f, mCircleLeftY + mRadius + progressWidth + mStrokeWidth / 2f + mTextSize / 2f, mPaint);
//					text_points[0][0] = mCircleLeftX;
//					text_points[0][1] = mCircleLeftY + mRadius + progressWidth / 2f + mStrokeWidth / 2f;
				}
				if (i == 12) {
					canvas.drawText(texts[12], mCircleRightX - mTextSize / 2f, mCircleLeftY + mRadius + progressWidth + mStrokeWidth / 2f + mTextSize / 2f, mPaint);
//					text_points[12][0] = mCircleRightX;
//					text_points[12][1] = mCircleLeftY + mRadius + progressWidth / 2f + mStrokeWidth / 2f;
				}
			}
			if (i > 0 && i <= 3) {
				for (int j = i; j <= 4; j++) {
					float[] f = getCoordinatePoint(mCircleLeftX, mCircleLeftY, mRadius + mStrokeWidth / 2 + progressWidth + mTextSize / 2, (j - 1) * 30 + 130);
					canvas.drawText(texts[j], f[0], f[1], mPaint);
//					float[] f0 = getCoordinatePoint(mCircleLeftX, mCircleLeftY, mRadius + mStrokeWidth / 2 + progressWidth / 2, (j - 1) * 30 + 130);
//					text_points[i][0] = f0[0];
//					text_points[i][1] = f0[1];
				}
			}
			if (i > 4 && i <= 7) {
				for (int j = i; j <= 7; j++) {
					canvas.drawText(texts[j], mCircleLeftX + (j - 5) * ((mCircleRightX - mCircleLeftX) / 2) - mTextSize / 2f, mCircleLeftY - mRadius - progressWidth - mStrokeWidth / 2f + mTextSize / 2f, mPaint);
				}
			}
			if (i > 7 && i <= 11) {
				for (int j = i; j <= 11; j++) {
					float[] f = getCoordinatePoint(mCircleRightX - mTextSize, mCircleRightY, mRadius + mStrokeWidth / 2 + progressWidth + mTextSize / 2, (j - 8) * 30 + 320);
					canvas.drawText(texts[j], f[0], f[1], mPaint);
				}
			}
		}
	}

	/**
	 * 画进度条的方法
	 * progress 进度 范围在0-140之间
	 */
	private void drawProgress(Canvas c, float[] endPoint) {
		//0到10范围
		int t1 = texts_index / 10;
		int t2 = texts_index % 10;
		//将整个结构划分为五个部分
		for (int i = 0; i <= 5; ) {
			if (i < 1) {
				if (t1 > 0) {//画完0-10 并且画下一阶段
					c.drawLine(mViewCenterX - mRadius / 2f - dp2px(10), mCircleLeftY + mRadius + progressWidth / 2f, mCircleLeftX, mCircleLeftY + mRadius + progressWidth / 2f, mPaint);
					i += 1;
					continue;
				} else {//画完0-10 直接结束
					c.drawLine(mViewCenterX - mRadius / 2f - dp2px(10), mCircleLeftY + mRadius + progressWidth / 2f, mViewCenterX - mRadius / 2f - dp2px(10) - t2 * ((mViewCenterX - mRadius / 2f - dp2px(10) - mCircleLeftX) / 10), mCircleLeftY + mRadius + progressWidth / 2f, mPaint);
					break;
				}
			}
			if (i < 2) {
				//特殊刻度特殊处理
				if (t1 >= 6) {
					c.drawArc(mCircleLeftX - mRadius - progressWidth / 2f, mCircleLeftY - mRadius - progressWidth / 2f,
						mCircleLeftX + mRadius + progressWidth / 2f, mCircleLeftY + mRadius + progressWidth / 2f, 89, 182, false, mPaint);
					i++;
					continue;
				} else {
					//很蠢的实现方式 暂时这么写 没想到更好的方法
					if (t1 > 4 && t2 > 0) {
						c.drawArc(mCircleLeftX - mRadius - progressWidth / 2f, mCircleLeftY - mRadius - progressWidth / 2f,
							mCircleLeftX + mRadius + progressWidth / 2f, mCircleLeftY + mRadius + progressWidth / 2f, 89, 3 * 30 + 41 + t2 * 5.1f, false, mPaint);
					} else if (t1 == 1 && t2 > 0) {
						c.drawArc(mCircleLeftX - mRadius - progressWidth / 2f, mCircleLeftY - mRadius - progressWidth / 2f,
							mCircleLeftX + mRadius + progressWidth / 2f, mCircleLeftY + mRadius + progressWidth / 2f, 89, t2 * 4.1f, false, mPaint);
					} else {
						c.drawArc(mCircleLeftX - mRadius - progressWidth / 2f, mCircleLeftY - mRadius - progressWidth / 2f,
							mCircleLeftX + mRadius + progressWidth / 2f, mCircleLeftY + mRadius + progressWidth / 2f, 89, (t1 - 2) * 30 + 41 + t2 * 3f, false, mPaint);
					}
					break;
				}

			}
			if (i < 3) {
				if (t1 >= 8) {
					c.drawLine(mCircleLeftX, mCircleLeftY - mRadius - progressWidth / 2f, mCircleRightX, mCircleLeftY - mRadius - progressWidth / 2f, mPaint);
					if (t1 == 8 && t2 == 0) break;
					i++;
					continue;
				} else {
					c.drawLine(mCircleLeftX, mCircleLeftY - mRadius - progressWidth / 2f, mCircleLeftX + (t1 - 6) * ((mCircleRightX - mCircleLeftX) / 2) + t2 * ((mCircleRightX - mCircleLeftX) / 20), mCircleLeftY - mRadius - progressWidth / 2f, mPaint);
					break;
				}
			}
			if (i < 4) {
				//特殊刻度特殊处理
				if (t1 > 12) {
					c.drawArc(mCircleRightX - mRadius - progressWidth / 2f, mCircleRightY - mRadius - progressWidth / 2f,
						mCircleRightX + mRadius + progressWidth / 2f, mCircleRightY + mRadius + progressWidth / 2f, 269, 182, false, mPaint);
					i++;
					continue;
				} else {
					//很蠢的实现方式 暂时这么写 没想到更好的方法
					if (t1 == 8 && t2 > 0) {
						c.drawArc(mCircleRightX - mRadius - progressWidth / 2f, mCircleRightY - mRadius - progressWidth / 2f,
							mCircleRightX + mRadius + progressWidth / 2f, mCircleRightY + mRadius + progressWidth / 2f, 269, t2 * 5.1f, false, mPaint);
					} else if (t1 == 12) {
						c.drawArc(mCircleRightX - mRadius - progressWidth / 2f, mCircleRightY - mRadius - progressWidth / 2f,
							mCircleRightX + mRadius + progressWidth / 2f, mCircleRightY + mRadius + progressWidth / 2f, 269, 51 + 3 * 30 + t2 * 4.1f, false, mPaint);
					} else {
						c.drawArc(mCircleRightX - mRadius - progressWidth / 2f, mCircleRightY - mRadius - progressWidth / 2f,
							mCircleRightX + mRadius + progressWidth / 2f, mCircleRightY + mRadius + progressWidth / 2f, 269, (t1 - 9) * 30 + 51 + t2 * 3f, false, mPaint);
					}
					break;
				}
			}
			if (i < 5) {
				if (t1 >= 14)
					c.drawLine(mCircleRightX, mCircleRightY + mRadius + progressWidth / 2f, mViewCenterX + mRadius / 2f + dp2px(10), mCircleRightY + mRadius + progressWidth / 2f, mPaint);
				if (t1 == 13 && t2 > 0)
					c.drawLine(mCircleRightX, mCircleRightY + mRadius + progressWidth / 2f, mCircleRightX - t2 * ((mCircleRightX - (mViewCenterX + mRadius / 2f + dp2px(10))) / 10), mCircleRightY + mRadius + progressWidth / 2f, mPaint);
				break;
			}
		}
	}

	//给控件设置进度
	public void setProgress(int progress) {
		if (progress < 0 || progress > 140) return;
		texts_index = progress;
		postInvalidate();
	}

	private int dp2px(int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
			Resources.getSystem().getDisplayMetrics());
	}

	private int sp2px(int sp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
			Resources.getSystem().getDisplayMetrics());
	}

	//依圆心坐标，半径，扇形角度，计算出扇形终射线与圆弧交叉点的xy坐标
	public float[] getCoordinatePoint(float mCenterX, float mCenterY, int radius, float angle) {
		float[] point = new float[2];

		double arcAngle = Math.toRadians(angle); //将角度转换为弧度
		if (angle < 90) {
			point[0] = (float) (mCenterX + Math.cos(arcAngle) * radius);
			point[1] = (float) (mCenterY + Math.sin(arcAngle) * radius);
		} else if (angle == 90) {
			point[0] = mCenterX;
			point[1] = mCenterY + radius;
		} else if (angle > 90 && angle < 180) {
			arcAngle = Math.PI * (180 - angle) / 180.0;
			point[0] = (float) (mCenterX - Math.cos(arcAngle) * radius);
			point[1] = (float) (mCenterY + Math.sin(arcAngle) * radius);
		} else if (angle == 180) {
			point[0] = mCenterX - radius;
			point[1] = mCenterY;
		} else if (angle > 180 && angle < 270) {
			arcAngle = Math.PI * (angle - 180) / 180.0;
			point[0] = (float) (mCenterX - Math.cos(arcAngle) * radius);
			point[1] = (float) (mCenterY - Math.sin(arcAngle) * radius);
		} else if (angle == 270) {
			point[0] = mCenterX;
			point[1] = mCenterY - radius;
		} else {
			arcAngle = Math.PI * (360 - angle) / 180.0;
			point[0] = (float) (mCenterX + Math.cos(arcAngle) * radius);
			point[1] = (float) (mCenterY - Math.sin(arcAngle) * radius);
		}
		return point;
	}
}
