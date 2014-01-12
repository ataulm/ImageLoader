package com.novoda.imageloader.core.model;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;

public interface ImageLoadableView {

    void setImageResource(int resId);

    void setImageURI(Uri uri);

    void setImageDrawable(Drawable drawable);

    void setImageBitmap(Bitmap bitmap);

}
