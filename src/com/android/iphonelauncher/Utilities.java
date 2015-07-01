/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.iphonelauncher;

import android.graphics.Paint.FontMetrics;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.TableMaskFilter;
import android.graphics.Typeface;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.R.anim;
import android.content.res.Resources;
import android.content.Context;
import android.os.SystemProperties;

import java.util.Random;

import com.android.iphonelauncher.R;

/**
 * Various utilities shared amongst the Launcher's classes.
 */
final public class Utilities {
    private static final String TAG = "Launcher2.Utilities";

    static final boolean DEBUG_LOADERS = false;

    static final boolean DEBUG_TEXTURE = false;

    private static final boolean TEXT_BURN = false;

    private static int sIconWidth = -1;

    private static int sIconHeight = -1;

    public static int sIconTextureWidth = -1;

    public static int sIconTextureHeight = -1;

    private static int sMaskWidth = -1;

    private static int sMaskHeight = -1;

    private static int sFolderIconIconWidth = -1;

    private static int sFolderIconIconHeight = -1;

    private static int sFolderIconIconLeftPadding = -1;

    private static int sFolderIconIconTopPadding = -1;

    private static int sFolderIconIconRowMargin = -1;

    private static int sFolderIconIconColumnMargin = -1;

    // for GLES1.1 begin
    /*private static int sTitleMargin_11 = -1;

    private static int sIconTestureWidth_11 = -1;

    private static int sIconTextureHeight_11 = -1;

    private static float sBlurRadius_11 = -1;

    private static Bitmap sBmpAllApp_11 = null;

    private static Canvas sCanvasAllApp_11 = null;

    // for GLES1.1 end
    private static final Paint sPaint = new Paint();

    private static final Paint sBlurPaint = new Paint();

    private static final Paint sGlowColorPressedPaint = new Paint();

    private static final Paint sGlowColorFocusedPaint = new Paint();

    private static final Paint sDisabledPaint = new Paint();
*/
    private static final Rect sBounds = new Rect();

    private static final Rect sOldBounds = new Rect();

    private static final Canvas sCanvas = new Canvas();

	static {
		sCanvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG,
				Paint.FILTER_BITMAP_FLAG));
	}
//
//	static Bitmap centerToFit(Bitmap bitmap, int width, int height,
//			Context context) {
//		final int bitmapWidth = bitmap.getWidth();
//		final int bitmapHeight = bitmap.getHeight();
//
//		if (bitmapWidth < width || bitmapHeight < height) {
//			int color = context.getResources().getColor(
//					R.color.window_background);
//
//			Bitmap centered = Bitmap.createBitmap(bitmapWidth < width ? width
//					: bitmapWidth, bitmapHeight < height ? height
//					: bitmapHeight, Bitmap.Config.RGB_565);
//			centered.setDensity(bitmap.getDensity());
//			Canvas canvas = new Canvas(centered);
//			canvas.drawColor(color);
//			canvas.drawBitmap(bitmap, (width - bitmapWidth) / 2.0f,
//					(height - bitmapHeight) / 2.0f, null);
//
//			bitmap = centered;
//		}
//
//		return bitmap;
//	}
//
	static int sColors[] = { 0xffff0000, 0xff00ff00, 0xff0000ff };

	static int sColorIndex = 0;

	/**
	 * jz
	 */
	static Bitmap createIconBitmapEx(Drawable icon, Context context){
		synchronized (sCanvas) { // we share the statics :-(
			if (sIconWidth == -1) {
				initStatics(context);
			}
			
			BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
			Bitmap bm = bitmapDrawable.getBitmap();
			//android.util.Log.d("QsLog", "createIconBitmapEx()==newW:"+sMaskWidth+"==newH:"+sMaskHeight+"=oldW:"+bm.getWidth()+"=oldH:"+bm.getHeight());
			
			if (bm.getWidth() == sMaskWidth
					&& bm.getHeight() == sMaskHeight) {
				return bm;
			} 
			
			return Bitmap.createScaledBitmap(bm, sMaskWidth, sMaskHeight, true); 
		}
	}
	
	static Bitmap createIconBitmap(Drawable icon, Context context) {
		synchronized (sCanvas) { // we share the statics :-(
			if (sIconWidth == -1) {
				initStatics(context);
			}

			int maskWidth = sMaskWidth;
			int maskHeight = sMaskHeight;

			Random random = new Random(System.currentTimeMillis());
			final int maskId = IPHONE_MASKS;/*[Math.abs(random.nextInt())
					% (IPHONE_MASKS.length - 1)];*/
			final Drawable normalBg = context.getResources()
					.getDrawable(maskId);
			final Drawable frontBg = context.getResources().getDrawable(
					R.drawable.icon_mask_front);
			normalBg.setBounds(0, 0, maskWidth, maskHeight);
			frontBg.setBounds(0, 0, maskWidth, maskHeight);

			final Bitmap iconBitmap = Bitmap.createBitmap(maskWidth,
					maskHeight, Config.ARGB_8888);
			final Canvas canvas = sCanvas;
			canvas.setBitmap(iconBitmap);
			normalBg.draw(canvas);
			frontBg.draw(canvas);

			int width = sIconWidth;
			int height = sIconHeight;

			if (icon instanceof PaintDrawable) {
				PaintDrawable painter = (PaintDrawable) icon;
				painter.setIntrinsicWidth(width);
				painter.setIntrinsicHeight(height);
			} else if (icon instanceof BitmapDrawable) {
				// Ensure the bitmap has a density.
				BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
				Bitmap bitmap = bitmapDrawable.getBitmap();
				if (bitmap.getDensity() == Bitmap.DENSITY_NONE) {
					bitmapDrawable.setTargetDensity(context.getResources()
							.getDisplayMetrics());
				}
			}
			int sourceWidth = icon.getIntrinsicWidth();
			int sourceHeight = icon.getIntrinsicHeight();

			if (sourceWidth > 0 && sourceWidth > 0) {
				// There are intrinsic sizes.
				if (width < sourceWidth || height < sourceHeight) {
					// It's too big, scale it down.
					final float ratio = (float) sourceWidth / sourceHeight;
					if (sourceWidth > sourceHeight) {
						height = (int) (width / ratio);
					} else if (sourceHeight > sourceWidth) {
						width = (int) (height * ratio);
					}
				} else if (sourceWidth < width && sourceHeight < height) {
					// It's small, use the size they gave us.
					width = sourceWidth;
					height = sourceHeight;
				}
			}

			// no intrinsic size --> use default size
			// int textureWidth = sIconTextureWidth;
			// int textureHeight = sIconTextureHeight;

			// final Bitmap bitmap = Bitmap.createBitmap(textureWidth,
			// textureHeight,
			// Bitmap.Config.ARGB_8888);
			//
			// canvas.setBitmap(bitmap);

			final int left = (maskWidth - width) / 2;
			final int top = (maskHeight - height) / 2;

			if (DEBUG_TEXTURE) {
				// draw a big box for the icon for debugging
				canvas.drawColor(sColors[sColorIndex]);
				if (++sColorIndex >= sColors.length)
					sColorIndex = 0;
				Paint debugPaint = new Paint();
				debugPaint.setColor(0xffcccc00);
				canvas.drawRect(left, top, left + width, top + height,
						debugPaint);
			}

			sOldBounds.set(icon.getBounds());
			icon.setBounds(0, 0, width, height);
			canvas.translate(left, top);
			icon.draw(canvas);
			canvas.translate(-left, -top);
			icon.setBounds(sOldBounds);

			return iconBitmap;
		}
	}

//	static void drawSelectedAllAppsBitmap(Canvas dest, int destWidth,
//			int destHeight, boolean pressed, Bitmap src) {
//		synchronized (sCanvas) { // we share the statics :-(
//			if (sIconWidth == -1) {
//				// We can't have gotten to here without src being initialized,
//				// which
//				// comes from this file already. So just assert.
//				// initStatics(context);
//				throw new RuntimeException(
//						"Assertion failed: Utilities not initialized");
//			}
//
//			dest.drawColor(0, PorterDuff.Mode.CLEAR);
//
//			int[] xy = new int[2];
//			Bitmap mask = src.extractAlpha(sBlurPaint, xy);
//
//			float px = (destWidth - src.getWidth()) / 2;
//			float py = (destHeight - src.getHeight()) / 2;
//			dest.drawBitmap(mask, px + xy[0], py + xy[1],
//					pressed ? sGlowColorPressedPaint : sGlowColorFocusedPaint);
//
//			mask.recycle();
//		}
//	}
//
//	/**
//	 * Returns a Bitmap representing the thumbnail of the specified Bitmap. The
//	 * size of the thumbnail is defined by the dimension
//	 * android.R.dimen.launcher_application_icon_size.
//	 * 
//	 * @param bitmap
//	 *            The bitmap to get a thumbnail of.
//	 * @param context
//	 *            The application's context.
//	 * @return A thumbnail for the specified bitmap or the bitmap itself if the
//	 *         thumbnail could not be created.
//	 */
	static Bitmap resampleIconBitmap(Bitmap bitmap, Context context) {
		synchronized (sCanvas) { // we share the statics :-(
			if (sIconWidth == -1) {
				initStatics(context);
			}

			if (bitmap.getWidth() == sIconWidth
					&& bitmap.getHeight() == sIconHeight) {
				return bitmap;
			} else {
				return createIconBitmap(new BitmapDrawable(bitmap), context);
			}
		}
	}
//
//	static Bitmap drawDisabledBitmap(Bitmap bitmap, Context context) {
//		synchronized (sCanvas) { // we share the statics :-(
//			if (sIconWidth == -1) {
//				initStatics(context);
//			}
//			final Bitmap disabled = Bitmap.createBitmap(bitmap.getWidth(),
//					bitmap.getHeight(), Bitmap.Config.ARGB_8888);
//			final Canvas canvas = sCanvas;
//			canvas.setBitmap(disabled);
//
//			canvas.drawBitmap(bitmap, 0.0f, 0.0f, sDisabledPaint);
//
//			return disabled;
//		}
//	}

	// for GLES1.1 begin
	/**
	 * Returns a Bitmap that can be support GLES 1.1. including icon bitmap and
	 * title bitmap merge to one bitmap. Create temp bitmap object when the
	 * first using,only for load thread to use. no thread safe,you need to sync
	 * external. if you call it ,you can call freeBitmap_11 to force GC.
	 * 
	 * @param bmpIcon
	 *            The bitmap of shortcut icon.
	 * @param bmpTitle
	 *            then bitmap of
	 * @return A bitmap of the combination title and icon.
	 * 
	 * 
	 *         static Bitmap createAllAppBitmap_11(Bitmap bmpIcon, Bitmap
	 *         bmpTitle) { if (sBmpAllApp_11 == null) { sBmpAllApp_11 =
	 *         Bitmap.createBitmap(sIconTestureWidth_11, sIconTextureHeight_11,
	 *         Bitmap.Config.ARGB_8888); } if (sCanvasAllApp_11 == null) {
	 *         sCanvasAllApp_11 = new Canvas(sBmpAllApp_11); }
	 *         sCanvasAllApp_11.drawColor(0, PorterDuff.Mode.CLEAR);
	 * 
	 *         if (DEBUG_TEXTURE) { sCanvasAllApp_11.drawColor(0xff00ff00);
	 *         Paint debugPaint = new Paint(); debugPaint.setColor(0xffcccc00);
	 *         sCanvasAllApp_11 .drawRect(0, 0, sIconTestureWidth_11,
	 *         sIconTextureHeight_11, debugPaint); }
	 * 
	 *         int leftIcon = (sIconTestureWidth_11 - bmpIcon.getWidth()) / 2;
	 *         int topIcon = (int) sBlurRadius_11 + 1;
	 * 
	 *         int leftTitle = (sIconTestureWidth_11 - bmpTitle.getWidth()) / 2;
	 *         int topTitle = topIcon + sTitleMargin_11 + bmpIcon.getHeight();
	 * 
	 *         sCanvasAllApp_11.drawBitmap(bmpIcon, leftIcon, topIcon, null);
	 *         sCanvasAllApp_11.drawBitmap(bmpTitle, leftTitle, topTitle, null);
	 * 
	 *         return sBmpAllApp_11; }
	 */
	/**
	 * Draw selected focus icon that can be support GLES 1.1. when user pressed
	 * icon or move to screen by key.
	 * 
	 * @param dest
	 *            : input canvas object reference to update focus icon.
	 * @param destWidth
	 *            : the width of dest
	 * @param destHeight
	 *            : the height of dest
	 * @param pressed
	 *            : Been pressed or only focused
	 * @param src
	 *            : the src bitmap
	 * @return void.
	 * 
	 *         static void drawSelectedAllAppsBitmap_11(Canvas dest, int
	 *         destWidth, int destHeight, boolean pressed, Bitmap src) {
	 *         synchronized (sCanvas) { if (sIconWidth == -1) { throw new
	 *         RuntimeException("Assertion failed: Utilities not initialized");
	 *         }
	 * 
	 *         dest.drawColor(0, PorterDuff.Mode.CLEAR);
	 * 
	 *         int[] xy = new int[2]; Bitmap mask = src.extractAlpha(sBlurPaint,
	 *         xy);
	 * 
	 *         float px = (destWidth - src.getWidth()) / 2; float py = (int)
	 *         sBlurRadius_11 + 1; dest.drawBitmap(mask, px + xy[0], py + xy[1],
	 *         pressed ? sGlowColorPressedPaint : sGlowColorFocusedPaint);
	 * 
	 *         mask.recycle(); } }
	 */
	/**
	 * To force to GC this bitmap resource when ensure
	 * 
	 * @param NA
	 *            .
	 * @return void.
	 * 
	 *         static void freeBitmap_11() { if (sBmpAllApp_11 != null) {
	 *         sBmpAllApp_11.recycle(); sBmpAllApp_11 = null; } }
	 */
    // for GLES1.1 end

    private static void initStatics(Context context) {
        final Resources resources = context.getResources();
        final DisplayMetrics metrics = resources.getDisplayMetrics();
        final float density = metrics.density;

        // add zf -qs
        sMaskWidth = sMaskHeight = (int) resources.getDimensionPixelSize(R.dimen.iphone_mask_size);

        sIconWidth = sIconHeight = (int) resources.getDimension(android.R.dimen.app_icon_size);

        sFolderIconIconWidth = (int) resources
                .getDimensionPixelSize(R.dimen.iphone_folder_icon_icon_width);

        sFolderIconIconHeight = (int) resources
                .getDimensionPixelSize(R.dimen.iphone_folder_icon_icon_height);

        sFolderIconIconLeftPadding = (int) resources
                .getDimensionPixelSize(R.dimen.iphone_folder_icon_left_padding);

        sFolderIconIconTopPadding = (int) resources
                .getDimensionPixelSize(R.dimen.iphone_folder_icon_top_padding);

        sFolderIconIconRowMargin = (int) resources
                .getDimensionPixelSize(R.dimen.iphone_folder_icon_row_margin);

        sFolderIconIconColumnMargin = (int) resources
                .getDimensionPixelSize(R.dimen.iphone_folder_icon_column_margin);

        /*String allAppsGrid = SystemProperties.get("launcher2.allappsgrid", "2d");
        if (!allAppsGrid.equals("3d_20")) {
            if (sIconWidth <= 32) {
                sIconWidth = sIconHeight = 36;
            }
        }*/

        // sIconTextureWidth = sIconTextureHeight = sIconWidth + 2;
        sIconTextureWidth = sIconTextureHeight = sIconWidth;

        /*if (!allAppsGrid.equals("3d_20")) {
            sIconTextureHeight_11 = sIconTestureWidth_11 = roundToPow2(sIconTextureWidth);
            sBlurRadius_11 = 5 * density;
            sTitleMargin_11 = (int) (1 * density);

            if (DisplayMetrics.DENSITY_DEVICE == 160) {
                sIconTextureHeight_11 = sIconTestureWidth_11 = 128;
            } else if (DisplayMetrics.DENSITY_DEVICE == 120) {
                sBlurRadius_11 = 0;

                if (metrics.widthPixels > metrics.heightPixels) {
                    sTitleMargin_11 = -3;
                }
            }
        }*/

//        sBlurPaint.setMaskFilter(new BlurMaskFilter(5 * density, BlurMaskFilter.Blur.NORMAL));
//        sGlowColorPressedPaint.setColor(0xffffc300);
//        sGlowColorPressedPaint.setMaskFilter(TableMaskFilter.CreateClipTable(0, 30));
//        sGlowColorFocusedPaint.setColor(0xffff8e00);
//        sGlowColorFocusedPaint.setMaskFilter(TableMaskFilter.CreateClipTable(0, 30));
//
//        ColorMatrix cm = new ColorMatrix();
//        cm.setSaturation(0.2f);
//        sDisabledPaint.setColorFilter(new ColorMatrixColorFilter(cm));
//        sDisabledPaint.setAlpha(0x88);
    }

    static class BubbleText {
        private static final int MAX_LINES = 2;

        private final TextPaint mTextPaint;

        private final RectF mBubbleRect = new RectF();

        private final float mTextWidth;

        private final int mLeading;

        private final int mFirstLineY;

        private final int mLineHeight;

        private final int mBitmapWidth;

        private final int mBitmapHeight;

        private final int mDensity;

        BubbleText(Context context) {
            final Resources resources = context.getResources();

            final DisplayMetrics metrics = resources.getDisplayMetrics();
            final float scale = metrics.density;
            mDensity = metrics.densityDpi;

            final float paddingLeft = 2.0f * scale;
            final float paddingRight = 2.0f * scale;
            final float cellWidth = resources.getDimension(R.dimen.title_texture_width);

            RectF bubbleRect = mBubbleRect;
            bubbleRect.left = 0;
            bubbleRect.top = 0;
            bubbleRect.right = (int) cellWidth - paddingLeft - paddingRight;

            /*String allAppsGrid = SystemProperties.get("launcher2.allappsgrid", "2d");
            if (!allAppsGrid.equals("3d_20")) {
                if (DisplayMetrics.DENSITY_DEVICE == 120) {
                    bubbleRect.right = cellWidth;
                }
            }*/

            mTextWidth = cellWidth - paddingLeft - paddingRight;

            TextPaint textPaint = mTextPaint = new TextPaint();
            textPaint.setTypeface(Typeface.DEFAULT);

            //if (!allAppsGrid.equals("3d_20")) {
                if (scale < 0.75) {
                    textPaint.setTextSize(10);
                } else {
                    textPaint.setTextSize(13 * scale);
                }
                if (DisplayMetrics.DENSITY_DEVICE == 120) {
                    textPaint.setTextSize(10);
                } else if (DisplayMetrics.DENSITY_DEVICE == 160) {
                    textPaint.setTextSize(14);
                }
            /*} else {
                textPaint.setTextSize(13 * scale);
            }*/

            textPaint.setColor(0xffffffff);
            textPaint.setAntiAlias(true);
            if (TEXT_BURN) {
                textPaint.setShadowLayer(8, 0, 0, 0xff000000);
            }

            FontMetrics fm = textPaint.getFontMetrics();
            float ascent = -fm.top;
            float descent = fm.bottom;
            float leading = 0.0f;

            /*
             * if (DisplayMetrics.DENSITY_DEVICE == 120 && (metrics.widthPixels
             * > metrics.heightPixels ||
             * (float)metrics.widthPixels/(float)metrics.heightPixels >= 0.75f))
             * { leading = -1.5f; }
             */

            if (DisplayMetrics.DENSITY_DEVICE == 240) {
                leading = -2.5f;
            }

            mLeading = (int) (leading + 0.5f);
            mFirstLineY = (int) (ascent);
            mLineHeight = (int) (leading + ascent + descent + 0.5f);

            mBitmapWidth = (int) (mBubbleRect.width() + 0.5f);
            int bitmapHeight = (int) ((MAX_LINES * mLineHeight) + leading + 0.5f);
            mBitmapHeight = roundToPow2(bitmapHeight);
            mBubbleRect.offsetTo((mBitmapWidth - mBubbleRect.width()) / 2, 0);

            if (false) {
                Log.d(TAG, "mBitmapWidth=" + mBitmapWidth + " mBitmapHeight=" + mBitmapHeight
                        + " w=" + mBitmapWidth + " h=" + bitmapHeight);
            }
        }

        /** You own the bitmap after this and you must call recycle on it. */
        Bitmap createTextBitmap(String text) {
            Bitmap b = null;
            //String allAppsGrid = SystemProperties.get("launcher2.allappsgrid", "2d");
            //if (!allAppsGrid.equals("3d_20")) {
                b = Bitmap.createBitmap(mBitmapWidth, mBitmapHeight, Bitmap.Config.ARGB_8888);
            //} else {
           //     b = Bitmap.createBitmap(mBitmapWidth, mBitmapHeight, Bitmap.Config.ALPHA_8);
           // }
            b.setDensity(mDensity);
            Canvas c = new Canvas(b);

            StaticLayout layout = new StaticLayout(text, mTextPaint, (int) mTextWidth,
                    Alignment.ALIGN_CENTER, 1, 0, true);
            int lineCount = layout.getLineCount();
            if (lineCount > MAX_LINES) {
                lineCount = MAX_LINES;
            }

            if (DEBUG_TEXTURE) {
                c.drawColor(0x22004400);
                Paint debugPaint = new Paint();
                debugPaint.setColor(0xffcccc00);
                c.drawRect(0, 0, mBitmapWidth, mBitmapHeight, debugPaint);
            }

            for (int i = 0; i < lineCount; i++) {
                final String lineText = text
                        .substring(layout.getLineStart(i), layout.getLineEnd(i));
                int x = (int) (mBubbleRect.left + ((mBubbleRect.width() - mTextPaint
                        .measureText(lineText)) * 0.5f));
                int y = mFirstLineY + (i * mLineHeight);
                if (DisplayMetrics.DENSITY_DEVICE == 240) {
                    y -= 3;
                } /*
                   * else if ( DisplayMetrics.DENSITY_DEVICE == 120) { y -= 2; }
                   */
                c.drawText(lineText, x, y, mTextPaint);
            }

            return b;
        }

        private int height(int lineCount) {
            return (int) ((lineCount * mLineHeight) + mLeading + mLeading + 0.0f);
        }

        int getBubbleWidth() {
            return (int) (mBubbleRect.width() + 0.5f);
        }

        int getMaxBubbleHeight() {
            return height(MAX_LINES);
        }

        int getBitmapWidth() {
            return mBitmapWidth;
        }

        int getBitmapHeight() {
            return mBitmapHeight;
        }
    }

    /** Only works for positive numbers. */
    static int roundToPow2(int n) {
        int orig = n;
        n >>= 1;
        int mask = 0x8000000;
        while (mask != 0 && (n & mask) == 0) {
            mask >>= 1;
        }
        while (mask != 0) {
            n |= mask;
            mask >>= 1;
        }
        n += 1;
        if (n != orig) {
            n <<= 1;
        }
        return n;
    }

    // add by zf - qs
    public static Bitmap makeGridFolderIcons(Bitmap[] folderIcons, Context context) {
        if (sIconWidth == -1) {
            initStatics(context);
        }

        final Drawable icon_bg = context.getResources().getDrawable(R.drawable.folder_icon_bg);

        int textureWidth = sMaskWidth;
        int textureHeight = sMaskHeight;

        final Bitmap grid = Bitmap.createBitmap(textureWidth, textureHeight,
                Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(grid);

        icon_bg.setBounds(0, 0, textureWidth, textureHeight);
        icon_bg.draw(canvas);

        final int iconW = sFolderIconIconWidth;
        final int iconH = sFolderIconIconHeight;

        final int left = sFolderIconIconLeftPadding;
        final int top = sFolderIconIconTopPadding;

        final int N = folderIcons.length;
        for (int i = 0; i < N; i++) {
            final Bitmap icon = folderIcons[i];
            final Rect scrRect = new Rect(0, 0, icon.getWidth(), icon.getHeight());
            final Rect dstRect = new Rect();

            dstRect.left = left + (i % 3) * sFolderIconIconColumnMargin + (i % 3) * iconW;
            dstRect.right = dstRect.left + iconW;
            dstRect.top = top + (i / 3) * sFolderIconIconRowMargin + (i / 3) * iconH;
            dstRect.bottom = dstRect.top + iconH;

            canvas.drawBitmap(folderIcons[i], scrRect, dstRect, null);
        }
        // canvas.save(Canvas.ALL_SAVE_FLAG);
        // canvas.restore();
        return grid;
    }

    /**
     * 0 normal 1 press
     * 
     * @param icon
     * @param context
     * @return
     */
//    private static final int[] IPHONE_MASKS = {
//            R.drawable.icon_mask0_bg, R.drawable.icon_mask1_bg, R.drawable.icon_mask2_bg,
//            R.drawable.icon_mask3_bg, R.drawable.icon_mask4_bg
//    };
    
    private static final int IPHONE_MASKS = R.drawable.icon_mask_bg;

    public static Bitmap makeIphoneIcon(Bitmap icon, Context context) {

        if (sIconWidth == -1) {
            initStatics(context);
        }

        int textureWidth = sMaskWidth;
        int textureHeight = sMaskHeight;

        Random random = new Random(System.currentTimeMillis());
        final int maskId = IPHONE_MASKS;//[Math.abs(random.nextInt()) % (IPHONE_MASKS.length - 1)];
        final Drawable normalBg = context.getResources().getDrawable(maskId);
        // final Drawable pressBg =
        // context.getResources().getDrawable(R.drawable.icon_mask0_bg);
        final Drawable frontBg = context.getResources().getDrawable(R.drawable.icon_mask_front);

        normalBg.setBounds(0, 0, textureWidth, textureHeight);
        // pressBg.setBounds(0, 0, textureWidth, textureHeight);
        frontBg.setBounds(0, 0, textureWidth, textureHeight);

        int left = (textureWidth - icon.getWidth()) / 2;
        int top = (textureHeight - icon.getHeight()) / 2;

        final Bitmap normalBitmap = Bitmap.createBitmap(textureWidth, textureHeight,
                Config.ARGB_8888);
        Canvas canvas = new Canvas(normalBitmap);
        normalBg.draw(canvas);
        canvas.drawBitmap(icon, left, top, null);
        frontBg.draw(canvas);

        // final Bitmap pressBitmap = Bitmap.createBitmap(textureWidth,
        // textureHeight,
        // Config.ARGB_8888);
        // canvas = new Canvas(pressBitmap);
        // pressBg.draw(canvas);
        // canvas.drawBitmap(icon, left, top, null);
        // frontBg.draw(canvas);

        return normalBitmap;
    }

    public static Drawable[] makeIphoneIconDrawables(Drawable icon, Context context) {

        if (sIconWidth == -1) {
            initStatics(context);
        }

        int textureWidth = sMaskWidth;
        int textureHeight = sMaskHeight;

        final Drawable normalBg = context.getResources().getDrawable(R.drawable.icon_mask2_bg);
        final Drawable pressBg = context.getResources().getDrawable(R.drawable.icon_mask0_bg);
        final Drawable frontBg = context.getResources().getDrawable(R.drawable.icon_mask_front);

        normalBg.setBounds(0, 0, textureWidth, textureHeight);
        pressBg.setBounds(0, 0, textureWidth, textureHeight);
        frontBg.setBounds(0, 0, textureWidth, textureHeight);

        icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());

        int left = (textureWidth - icon.getIntrinsicWidth()) / 2;
        int top = (textureHeight - icon.getIntrinsicHeight()) / 2;

        final Bitmap normalBitmap = Bitmap.createBitmap(textureWidth, textureHeight,
                Config.ARGB_8888);
        Canvas canvas = new Canvas(normalBitmap);
        normalBg.draw(canvas);
        canvas.translate(left, top);
        icon.draw(canvas);
        canvas.translate(-left, -top);
        frontBg.draw(canvas);

        final Bitmap pressBitmap = Bitmap.createBitmap(textureWidth, textureHeight,
                Config.ARGB_8888);
        canvas = new Canvas(normalBitmap);
        pressBg.draw(canvas);
        canvas.translate(left, top);
        icon.draw(canvas);
        canvas.translate(-left, -top);
        frontBg.draw(canvas);

        return new Drawable[] {
                new FastBitmapDrawable(normalBitmap), new FastBitmapDrawable(pressBitmap)
        };
    }

    public static Bitmap createReflectionImage(Bitmap bitmap) {

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Matrix matrix = new Matrix();
        matrix.preScale(1, -1);

        Bitmap reflectionImage = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
        Bitmap newBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);

        Canvas canvas = new Canvas(newBitmap);
        canvas.drawBitmap(reflectionImage, 0, 0, null);

        Paint paint = new Paint();
        LinearGradient shader = new LinearGradient(0, 0, 0, height, 0xacffffff, 0x00ffffff,
                TileMode.CLAMP);
        paint.setShader(shader);
        // Set the Transfer mode to be porter duff and destination in
        paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
        // Draw a rectangle using the paint with our linear gradient
        canvas.drawRect(0, 0, width, height, paint);

        return newBitmap;
    }
}
