package examples.com.imageviewer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.IOException;

public class ImageViewMainActivity extends AppCompatActivity
{
    ImageView imageView;
    private static final int REQUEST_OPEN_RESULT_CODE = 0;

    PinchZoomImageView pinchZoomImageView;

    private Uri imageUri;
    private Animator animator;
    private int animationDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view_main);

        imageView = (ImageView) findViewById(R.id.imageview);

        pinchZoomImageView = (PinchZoomImageView) findViewById(R.id.pinchZoomImageView);

        animationDuration = getResources().getInteger(android.R.integer.config_longAnimTime);

        imageView.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                Toast.makeText(getApplicationContext(),"Long Pressed On Image",Toast.LENGTH_LONG).show();

                //zoomImageFromThumb();

                pinchZoomPan();

                return true;
            }
        });

        Intent  intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        intent.addCategory(Intent.CATEGORY_OPENABLE);

        intent.setType("image/*");

        startActivityForResult(intent,REQUEST_OPEN_RESULT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData)
    {
        if( requestCode == REQUEST_OPEN_RESULT_CODE && resultCode == RESULT_OK )
        {

            if( resultData != null )
            {
                imageUri = resultData.getData();
                try
                {
                    Bitmap bitmap = getBitmapFromUri(imageUri);

                    imageView.setImageBitmap(bitmap);

//                    pinchZoomImageView.setImageBitmap(bitmap);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    /*@Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);

        View decorView = getWindow().getDecorView();

        if( hasFocus )
        {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    |View.SYSTEM_UI_FLAG_FULLSCREEN
                    |View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    |View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }*/

    private Bitmap getBitmapFromUri(Uri uri ) throws IOException
    {
        ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(uri,"r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return bitmap;
    }

    private void zoomImageFromThumb()
    {
        if( animator != null )
        {
            animator.cancel();
        }

        try {
            pinchZoomImageView.setImageBitmap(getBitmapFromUri(imageUri));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        Rect startBounds = new Rect();
        Rect finalBounds = new Rect();
        Point globalOffset = new Point();

        imageView.getGlobalVisibleRect(startBounds);

        findViewById(R.id.container).getGlobalVisibleRect(finalBounds,globalOffset);

        startBounds.offset(-globalOffset.x,-globalOffset.y);
        finalBounds.offset(-globalOffset.x,-globalOffset.y);

        float startScale;

        if( ((float) (finalBounds.width()/finalBounds.height())) > (float) (startBounds.width()/startBounds.height()) )
        {
            startScale = (float) (startBounds.height()/finalBounds.height());

            float startWidth = (float) (startScale*finalBounds.width());
            float deltaWidth =  ((startWidth-startBounds.width()) / 2);

            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        }
        else
        {
            startScale = (float) (startBounds.width()/finalBounds.width());

            float startheight = (float) (startScale*finalBounds.height());
            float deltaHeight =  ((startheight-startBounds.height()) / 2);

            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        imageView.setAlpha(0f);

        pinchZoomImageView.setVisibility(View.VISIBLE);

        pinchZoomImageView.setPivotX(0f);
        pinchZoomImageView.setPivotY(0f);

        AnimatorSet animatorSet = new AnimatorSet();

        /*animatorSet.play(ObjectAnimator.ofFloat(pinchZoomImageView,View.X,startBounds.left,finalBounds.left)
                        .with(ObjectAnimator.ofFloat(pinchZoomImageView,View.Y,startBounds.top,finalBounds.top)
                        .with(ObjectAnimator.ofFloat(pinchZoomImageView,View.SCALE_X,startScale,1f)
                                .with(ObjectAnimator.ofFloat(pinchZoomImageView,View.SCALE_Y,startScale,1f));*/

        animatorSet.play(ObjectAnimator.ofFloat(pinchZoomImageView,View.X,startBounds.left,finalBounds.left))
                .with(ObjectAnimator.ofFloat(pinchZoomImageView,View.Y,startBounds.top,finalBounds.top))
                        .with(ObjectAnimator.ofFloat(pinchZoomImageView,View.SCALE_X,startScale,1f))
                                .with(ObjectAnimator.ofFloat(pinchZoomImageView,View.SCALE_Y,startScale,1f));

        animatorSet.setDuration(animationDuration);
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.addListener(new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationCancel(Animator animation)
            {
                super.onAnimationCancel(animation);

                animator = null;
            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
                super.onAnimationEnd(animation);
                animator = null;
            }
        });
        animatorSet.start();
        animator = animatorSet;


    }

    private void pinchZoomPan()
    {
        pinchZoomImageView.setImageUri(imageUri);

        imageView.setAlpha(0.f);

        pinchZoomImageView.setVisibility(View.VISIBLE);
    }
}
