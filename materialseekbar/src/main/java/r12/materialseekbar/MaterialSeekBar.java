package r12.materialseekbar;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.SeekBar;

import java.util.concurrent.TimeUnit;

/**
 * Created by R12 on 09.03.2018.
 */

public class MaterialSeekBar extends android.support.v7.widget.AppCompatSeekBar {

    private static final long DEFAULT_SHOW_ANIM_DURATION_IN_MILLIS = TimeUnit.SECONDS.toMillis(2);
    private static final long DEFAULT_HIDE_ANIM_DURATION_IN_MILLIS = DEFAULT_SHOW_ANIM_DURATION_IN_MILLIS / 3;
    private static final int MIN_ALPHA = 0;
    private static final int MAX_ALPHA = 255;

    private Drawable bubbleDrawable;
    private float bubblePadding;
    private Paint progressTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect progressTextRect = new Rect();
    private Paint minTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect minTextRect = new Rect();
    private Paint maxTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect maxTextRect = new Rect();
    private float minMaxPadding;
    private MinMaxPositionEnum minMaxPosition;
    private boolean showMinMax;
    private int minValue;
    private int maxValue;
    @Nullable
    private OnSeekBarChangeListener onSeekBarChangeListener;
    @Nullable
    private ValueAnimator startAnimator;
    private final ValueAnimator.AnimatorUpdateListener startAnimatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            updateBubbleAlpha((Integer) animation.getAnimatedValue());
        }
    };
    @Nullable
    private ValueAnimator hideAnimator;
    private final ValueAnimator.AnimatorUpdateListener endAnimatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            updateBubbleAlpha((Integer) animation.getAnimatedValue());
            invalidate();
        }
    };
    private int currentBubbleAlpha;
    private TimeInterpolator showBubbleInterpolator = new DecelerateInterpolator();
    private TimeInterpolator hideBubbleInterpolator = new AccelerateInterpolator();

    private boolean alwaysShowBubble;
    private long showBubbleAnimDuration;
    private long hideBubbleAnimDuration;
    @Nullable
    private BubbleProgressUpdater bubbleProgressUpdater;

    public MaterialSeekBar(Context context) {
        super(context);
        init(context, null);
    }

    public MaterialSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MaterialSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
        if (!alwaysShowBubble) {
            updateBubbleAlpha(0);
        }
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        if (attrs != null) {
            Resources resources = getResources();
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MaterialSeekBar);
            // init bubble
            Drawable drawable = array.getDrawable(R.styleable.MaterialSeekBar_bubbleDrawable);
            if (drawable == null) {
                drawable = ContextCompat.getDrawable(context, R.drawable.material_bubble_grey);
            }
            assert drawable != null;
            bubbleDrawable = DrawableCompat.wrap(drawable);
            bubbleDrawable.mutate();
            int bubbleColor = array.getColor(R.styleable.MaterialSeekBar_bubbleColor,
                    ContextCompat.getColor(context, R.color.material_light_grey));
            DrawableCompat.setTint(bubbleDrawable, bubbleColor);
            bubblePadding = array.getDimensionPixelSize(R.styleable.MaterialSeekBar_bubblePadding,
                    (int) resources.getDimension(R.dimen.material_seek_bar_bubble_padding));

            progressTextPaint.setTypeface(Typeface.DEFAULT);
            progressTextPaint.setTextSize(array.getDimensionPixelSize(R.styleable.MaterialSeekBar_progressTextSize,
                    (int) resources.getDimension(R.dimen.material_seek_bar_progress_text_size)));
            progressTextPaint.setColor(array.getColor(R.styleable.MaterialSeekBar_progressTextColor, Color.BLACK));

            int minMaxTextSize = array.getDimensionPixelSize(R.styleable.MaterialSeekBar_minMaxTextSize,
                    (int) resources.getDimension(R.dimen.material_seek_bar_min_max_text_size));

            minTextPaint.setTypeface(Typeface.DEFAULT);
            minTextPaint.setTextSize(minMaxTextSize);
            minTextPaint.setColor(array.getColor(R.styleable.MaterialSeekBar_minTextColor, Color.BLUE));

            maxTextPaint.setTypeface(Typeface.DEFAULT);
            maxTextPaint.setTextSize(minMaxTextSize);
            maxTextPaint.setColor(array.getColor(R.styleable.MaterialSeekBar_maxTextColor, Color.BLUE));

            setMinHintValue(array.getInt(R.styleable.MaterialSeekBar_android_min, 0));
            setMaxHintValue(array.getInt(R.styleable.MaterialSeekBar_android_max, getMax()));

            minMaxPadding = array.getDimensionPixelSize(R.styleable.MaterialSeekBar_minMaxPadding,
                    (int) resources.getDimension(R.dimen.material_seek_bar_min_max_padding));
            minMaxPosition = MinMaxPositionEnum.values()[array.getInt(R.styleable.MaterialSeekBar_minMaxPosition, MinMaxPositionEnum.IN_LINE.ordinal())];
            showMinMax = array.getBoolean(R.styleable.MaterialSeekBar_showMinMax, true);

            alwaysShowBubble = array.getBoolean(R.styleable.MaterialSeekBar_alwaysShowBubble, false);
            showBubbleAnimDuration = array.getInt(R.styleable.MaterialSeekBar_showBubbleAnimDuration, (int) DEFAULT_SHOW_ANIM_DURATION_IN_MILLIS);
            hideBubbleAnimDuration = array.getInt(R.styleable.MaterialSeekBar_hideBubbleAnimDuration, (int) DEFAULT_HIDE_ANIM_DURATION_IN_MILLIS);

            updateBubbleAlpha(alwaysShowBubble ? MAX_ALPHA : MIN_ALPHA);

            array.recycle();
        }
        updatePaddings(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom());
        initOnSeekBarChangeListener();
    }

    private void updatePaddings(int paddingLeft, int paddingTop, int paddingRight, int paddingBottom) {
        int left = paddingLeft;
        int right = paddingRight;
        int top = paddingTop;
        int bottom = paddingBottom;
        int halfBubbleWidth = bubbleDrawable.getIntrinsicWidth() / 2;

        if (showMinMax) {
            float minTextWidth = minTextRect.width() + minMaxPadding;
            float maxTextWidth = maxTextRect.width() + minMaxPadding;

            switch (minMaxPosition) {
                case IN_LINE:
                    break;
                case UNDER:
                    bottom += minTextRect.height() + minMaxPadding;
                    break;
                case UNDER_WITH_OFFSET:
                    bottom += minTextRect.height() + minMaxPadding;
                    minTextWidth = minTextRect.width() / 2;
                    maxTextWidth = maxTextRect.width() / 2;
                    break;
            }
            if (halfBubbleWidth > minTextWidth) {
                left += halfBubbleWidth;
            } else {
                left += minTextWidth;
            }
            if (halfBubbleWidth > maxTextWidth) {
                right += halfBubbleWidth;
            } else {
                right += maxTextWidth;
            }
        } else {
            left += halfBubbleWidth;
            right += halfBubbleWidth;
        }
        top += bubbleDrawable.getIntrinsicHeight() + bubblePadding;

        super.setPadding(left, top, right, bottom);
    }

    private void initOnSeekBarChangeListener() {
        super.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (onSeekBarChangeListener != null) {
                    onSeekBarChangeListener.onProgressChanged(seekBar, progress, fromUser);
                }
                if (startAnimator != null) startAnimator.cancel();
                if (hideAnimator != null) hideAnimator.cancel();
                updateBubbleAlpha(MAX_ALPHA);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (onSeekBarChangeListener != null) {
                    onSeekBarChangeListener.onStartTrackingTouch(seekBar);
                }
                if (!alwaysShowBubble) {
                    if (hideAnimator != null) hideAnimator.cancel();
                    reinitShowAnimator().start();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (onSeekBarChangeListener != null) {
                    onSeekBarChangeListener.onStopTrackingTouch(seekBar);
                }
                if (!alwaysShowBubble) {
                    if (startAnimator != null) startAnimator.cancel();
                    reinitHideAnimator().start();
                }
            }
        });
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        updatePaddings(left, top, right, bottom);
    }

    private ValueAnimator reinitShowAnimator() {
        startAnimator = ValueAnimator.ofInt(currentBubbleAlpha, MAX_ALPHA);
        assert startAnimator != null;
        startAnimator.setInterpolator(showBubbleInterpolator);
        startAnimator.addUpdateListener(startAnimatorUpdateListener);
        startAnimator.setDuration(calculateStartAnimDuration());
        return startAnimator;
    }

    private int calculateStartAnimDuration() {
        double remainPercent = 100 - (currentBubbleAlpha * 100 / MAX_ALPHA);
        return (int) (showBubbleAnimDuration * (remainPercent / 100));
    }

    private ValueAnimator reinitHideAnimator() {
        hideAnimator = ValueAnimator.ofInt(currentBubbleAlpha, MIN_ALPHA);
        assert hideAnimator != null;
        hideAnimator.addUpdateListener(endAnimatorUpdateListener);
        hideAnimator.setInterpolator(hideBubbleInterpolator);
        hideAnimator.setDuration(calculateEndAnimDuration());
        return hideAnimator;
    }

    private int calculateEndAnimDuration() {
        double remainPercent = 100 - (currentBubbleAlpha * 100 / MAX_ALPHA);
        return (int) (hideBubbleAnimDuration - (hideBubbleAnimDuration * (remainPercent / 100)));
    }

    private void updateBubbleAlpha(@IntRange(from = 0, to = 255) int alpha) {
        bubbleDrawable.setAlpha(alpha);
        progressTextPaint.setAlpha(alpha);
        currentBubbleAlpha = alpha;
    }

    @Override
    public synchronized void setMin(int min) {
        setMinHintValue(min);
        super.setMin(min);
    }

    public void setMinHintValue(int min) {
        // minTextPaint == null when called first time from superclass
        if (minTextPaint != null) {
            minValue = min;
            String valueString = String.valueOf(min);
            minTextPaint.getTextBounds(valueString, 0, valueString.length(), minTextRect);
        }
    }

    @Override
    public synchronized void setMax(int max) {
        setMaxHintValue(max);
        super.setMax(max);
    }

    public void setMaxHintValue(int max) {
        // maxTextPaint == null when called first time from superclass
        if (maxTextPaint != null) {
            maxValue = max;
            String valueString = String.valueOf(max);
            maxTextPaint.getTextBounds(valueString, 0, valueString.length(), maxTextRect);
        }
    }

    private Rect getProgressTextRect(int value) {
        String valueString = String.valueOf(value);
        progressTextPaint.getTextBounds(valueString, 0, valueString.length(), progressTextRect);
        return progressTextRect;
    }

    public void setBubbleColor(@ColorRes int color) {
        DrawableCompat.setTint(bubbleDrawable, ContextCompat.getColor(getContext(), color));
        invalidate();
    }

    public void setBubbleDrawable(Drawable drawable) {
        bubbleDrawable = DrawableCompat.wrap(drawable);
        bubbleDrawable.mutate();
        invalidate();
    }

    public void setBubbleDrawable(@DrawableRes int drawable) {
        setBubbleDrawable(ContextCompat.getDrawable(getContext(), drawable));
    }

    public void setAlwaysShowBubble(boolean alwaysShowBubble) {
        this.alwaysShowBubble = alwaysShowBubble;
        invalidate();
    }

    public void setShowBubbleAnimDuration(long duration) {
        showBubbleAnimDuration = duration;
        startAnimator = null;
    }

    public void setHideBubbleAnimDuration(long duration) {
        hideBubbleAnimDuration = duration;
        hideAnimator = null;
    }

    public void setBubbleProgressUpdater(@Nullable BubbleProgressUpdater updater) {
        bubbleProgressUpdater = updater;
    }

    public void setShowBubbleInterpolator(TimeInterpolator interpolator) {
        showBubbleInterpolator = interpolator;
        if (startAnimator != null) startAnimator.setInterpolator(showBubbleInterpolator);
    }

    public void setHideBubbleInterpolator(TimeInterpolator interpolator) {
        hideBubbleInterpolator = interpolator;
        if (hideAnimator != null) hideAnimator.setInterpolator(hideBubbleInterpolator);
    }

    /**
     * Set the progress (inside bubble) text color. Note that the color is an int containing alpha
     * as well as r,g,b. This 32bit value is not premultiplied, meaning that
     * its alpha can be any value, regardless of the values of r,g,b.
     * See the Color class for more details.
     *
     * @param id The desired resource identifier, as generated by the aapt
     *           tool. This integer encodes the package, type, and resource
     *           entry. The value 0 is an invalid identifier.
     */
    public void setProgressTextColor(@ColorRes int id) {
        progressTextPaint.setColor(ContextCompat.getColor(getContext(),id));
        invalidate();
    }

    /**
     * Set the progress (inside bubble) text size. This value must be > 0
     *
     * @param textSize set the progress text size in pixel units.
     */
    public void setProgressTextSize(float textSize) {
        progressTextPaint.setTextSize(textSize);
        invalidate();
    }

    @Override
    public void setOnSeekBarChangeListener(@Nullable OnSeekBarChangeListener listener) {
        onSeekBarChangeListener = listener;
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int paddingRight = getPaddingRight();
        int height = canvas.getHeight();
        int width = canvas.getWidth();
        int backgroundWidth = height - paddingTop - paddingBottom;
        int centerY = paddingTop + backgroundWidth / 2;

        drawMinMax(canvas, paddingLeft, paddingRight, paddingBottom, height, width, centerY);
        drawBubble(canvas, paddingTop);
    }

    private void drawMinMax(Canvas canvas,
                            int paddingLeft, int paddingRight, int paddingBottom,
                            int height, int width, int centerY) {
        Drawable progressDrawable = getProgressDrawable();
        if (!showMinMax || progressDrawable == null) return;

        float positionXMin = 0;
        float positionY = 0;
        float positionXMax = 0;
        switch (minMaxPosition) {
            case IN_LINE:
                positionXMin = paddingLeft - minTextRect.width() - minMaxPadding;
                positionXMax = width - paddingRight + minMaxPadding;
                positionY = centerY + minTextRect.height() / 2;
                break;
            case UNDER:
                positionXMin = paddingLeft - minTextRect.width() - minMaxPadding;
                positionXMax = width - paddingRight + minMaxPadding;
                positionY = height - paddingBottom + minMaxPadding;
                break;
            case UNDER_WITH_OFFSET:
                positionXMin = paddingLeft - minTextRect.width() / 2;
                positionXMax = width - paddingRight - maxTextRect.width() / 2;
                positionY = height - paddingBottom + minMaxPadding;
                break;
        }
        canvas.drawText(String.valueOf(minValue), positionXMin, positionY, minTextPaint);
        canvas.drawText(String.valueOf(maxValue), positionXMax, positionY, maxTextPaint);
    }

    private void drawBubble(Canvas canvas, int paddingTop) {
        Drawable thumb = getThumb();
        if (thumb == null) return;

        Rect thumbRect = getThumb().getBounds();
        int bubbleWidth = bubbleDrawable.getIntrinsicWidth();
        int bubbleHeight = bubbleDrawable.getIntrinsicHeight();

        int left = thumbRect.left + getPaddingLeft() - bubbleWidth / 2;
        int right = left + bubbleWidth;
        int bottom = (int) (paddingTop - bubblePadding);
        int top = bottom - bubbleHeight;
        bubbleDrawable.setBounds(left, top, right, bottom);
        bubbleDrawable.draw(canvas);

        int progress = getProgress();
        if (bubbleProgressUpdater != null) {
            progress = bubbleProgressUpdater.getBubbleProgress(progress);
        }
        Rect progressTextRect = getProgressTextRect(progress);
        int positionTextX = left + bubbleWidth / 2 - progressTextRect.width() / 2;
        int positionY = top + bubbleHeight / 2;
        canvas.drawText(String.valueOf(progress), (float) positionTextX, (float) positionY, progressTextPaint);
    }

    public interface BubbleProgressUpdater {
        int getBubbleProgress(int realProgress);
    }
}
