// LoginActivity.java
// ══════════════════════════════════════════════════
// Applies a programmatic blur to the background image
// using Glide + BitmapTransformation (no extra library needed)
// ══════════════════════════════════════════════════

package com.redants.smartfinance;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import java.security.MessageDigest;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make the activity full screen (no status bar)
        getWindow().setFlags(
                android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
                android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        setContentView(R.layout.activity_login);

        ImageView bgImage = findViewById(R.id.bgImage);

        // Load the background image with blur using Glide
        Glide.with(this)
                .asBitmap()
                .load(R.drawable.hero_finance_banner)
                .transform(new BlurTransformation(this, 18f)) // blur radius 1–25
                .into(bgImage);

        // Button click listeners
        findViewById(R.id.btnLogin).setOnClickListener(v -> {
            // Navigate to LoginFormActivity or Dashboard
            // startActivity(new Intent(this, LoginFormActivity.class));
        });

        findViewById(R.id.btnSignUp).setOnClickListener(v -> {
            // Navigate to SignUpActivity
            // startActivity(new Intent(this, SignUpActivity.class));
        });
    }

    // ── Inner class: Glide Blur Transformation ──────────────────
    static class BlurTransformation extends BitmapTransformation {
        private final android.content.Context context;
        private final float radius;

        BlurTransformation(android.content.Context context, float radius) {
            this.context = context;
            this.radius = Math.min(radius, 25f); // RenderScript max is 25
        }

        @Override
        protected Bitmap transform(BitmapPool pool, Bitmap toTransform,
                                   int outWidth, int outHeight) {
            Bitmap blurred = toTransform.copy(Bitmap.Config.ARGB_8888, true);
            RenderScript rs = RenderScript.create(context);
            Allocation input = Allocation.createFromBitmap(rs, blurred);
            Allocation output = Allocation.createTyped(rs, input.getType());
            ScriptIntrinsicBlur script =
                    ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
            script.setRadius(radius);
            script.setInput(input);
            script.forEach(output);
            output.copyTo(blurred);
            rs.destroy();
            return blurred;
        }

        @Override
        public void updateDiskCacheKey(MessageDigest messageDigest) {
            messageDigest.update(("blur" + radius).getBytes());
        }
    }
}