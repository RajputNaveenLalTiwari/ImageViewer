package examples.com.imageviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

import java.io.IOException;

/**
 * Created by 2114 on 09-01-2017.
 */

public class PinchZoomImageView extends ImageView
{
    private Bitmap bitmap;
    private int imageWidth;
    private int imageHeight;

    private static final float minimumZoomValue = 1.f;
    private static final float maximumZoomValue = 3.f;
    private float scaleFactorToZoom = 1.f;
    private ScaleGestureDetector scaleGestureDetector;

    private static final int NONE = 0;
    private static final int PAN = 1;
    private static final int PINCH_ZOOM = 2;
    private int touchEventState;
    private float startX = 0;
    private float startY = 0;
    private float translateX = 0;
    private float translateY = 0;
    private float previousTranslateX = 0;
    private float previousTranslateY = 0;

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener
    {
        @Override
        public boolean onScale(ScaleGestureDetector detector)
        {
            scaleFactorToZoom *= detector.getScaleFactor();

            scaleFactorToZoom = Math.min(minimumZoomValue,Math.min(maximumZoomValue,scaleFactorToZoom));

           // invalidate();

           // requestLayout();

            return super.onScale(detector);
        }
    }

    public PinchZoomImageView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        scaleGestureDetector = new ScaleGestureDetector(getContext(),new ScaleListener());
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        canvas.save();

        if( (translateX * -1) < 0 )
        {
            translateX = 0;
        }
        else if( (translateX * -1) > ( ((imageWidth * scaleFactorToZoom)-getWidth()) * -1 ) )
        {
            translateX = ( ((imageWidth * scaleFactorToZoom)-getWidth()) * -1 );
        }

        if( (translateY * -1) < 0 )
        {
            translateY = 0;
        }
        else if( (translateY * -1) > ( ((imageHeight * scaleFactorToZoom)-getHeight()) * -1 ) )
        {
            translateY = ( ((imageHeight * scaleFactorToZoom)-getHeight()) * -1 );
        }

        canvas.scale(scaleFactorToZoom,scaleFactorToZoom);
        canvas.translate(translateX/scaleFactorToZoom,translateY/scaleFactorToZoom);

//        canvas.scale(scaleFactorToZoom,scaleFactorToZoom,scaleGestureDetector.getFocusX(),scaleGestureDetector.getFocusY());

        canvas.drawBitmap(bitmap,0,0,null);

        canvas.restore();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int imageWidthLocal = MeasureSpec.getSize(widthMeasureSpec);
        int imageHeightLocal = MeasureSpec.getSize(heightMeasureSpec);

        int scaledImageWidth = Math.round(imageWidth * scaleFactorToZoom);
        int scaledImageHeight = Math.round(imageHeight * scaleFactorToZoom);

        setMeasuredDimension(   Math.min(imageWidthLocal,scaledImageWidth),
                                Math.min(imageHeightLocal,scaledImageHeight)
                                );
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        switch ( event.getAction() & MotionEvent.ACTION_MASK )
        {
            case MotionEvent.ACTION_UP:

                touchEventState = NONE;

                previousTranslateX = translateX;
                previousTranslateY = translateY;

                break;
            case MotionEvent.ACTION_DOWN:

                touchEventState = PAN;

                startX = event.getX() - previousTranslateX;
                startY = event.getY() - previousTranslateY;

                break;
            case MotionEvent.ACTION_POINTER_DOWN:

                touchEventState = PINCH_ZOOM;

                break;
            case MotionEvent.ACTION_MOVE:

                translateX = event.getX() - startX;
                translateY = event.getY() - startY;

                break;
        }
        scaleGestureDetector.onTouchEvent(event);

        if( (touchEventState == PAN && scaleFactorToZoom != minimumZoomValue) ||
                (touchEventState == PINCH_ZOOM))
        {
            invalidate();
            requestLayout();
        }

        return true;
    }

    public void setImageUri(Uri uri)
    {
        try
        {
            Bitmap bitmapLocal = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(),uri);

            float aspectRatio = ( ((float) bitmapLocal.getHeight()) / ((float) bitmapLocal.getWidth()) );

            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

            imageWidth = displayMetrics.widthPixels;

            imageHeight = Math.round( imageWidth * aspectRatio );

            bitmap = Bitmap.createScaledBitmap( bitmapLocal,imageWidth,imageHeight,false );

            invalidate();

            requestLayout();
        }
        catch (IOException e)
        {
            Log.e("setImageUri(Uri uri)","Error while setting image uri");
            e.printStackTrace();
        }
    }
}
