/*
 * Copyright (C) 2017 The Android Open Source Project
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
package com.mobidriven.exoplayer2.video;

import static java.lang.annotation.ElementType.TYPE_USE;

import android.os.Bundle;
import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import com.mobidriven.exoplayer2.Bundleable;
import com.mobidriven.exoplayer2.C;
import com.mobidriven.exoplayer2.Format;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import org.checkerframework.dataflow.qual.Pure;

/** Stores color info. */
public final class ColorInfo implements Bundleable {

  /**
   * Returns the {@link C.ColorSpace} corresponding to the given ISO color primary code, as per
   * table A.7.21.1 in Rec. ITU-T T.832 (03/2009), or {@link Format#NO_VALUE} if no mapping can be
   * made.
   */
  @Pure
  public static @C.ColorSpace int isoColorPrimariesToColorSpace(int isoColorPrimaries) {
    switch (isoColorPrimaries) {
      case 1:
        return C.COLOR_SPACE_BT709;
      case 4: // BT.470M.
      case 5: // BT.470BG.
      case 6: // SMPTE 170M.
      case 7: // SMPTE 240M.
        return C.COLOR_SPACE_BT601;
      case 9:
        return C.COLOR_SPACE_BT2020;
      default:
        return Format.NO_VALUE;
    }
  }

  /**
   * Returns the {@link C.ColorTransfer} corresponding to the given ISO transfer characteristics
   * code, as per table A.7.21.2 in Rec. ITU-T T.832 (03/2009), or {@link Format#NO_VALUE} if no
   * mapping can be made.
   */
  @Pure
  public static @C.ColorTransfer int isoTransferCharacteristicsToColorTransfer(
      int isoTransferCharacteristics) {
    switch (isoTransferCharacteristics) {
      case 1: // BT.709.
      case 6: // SMPTE 170M.
      case 7: // SMPTE 240M.
        return C.COLOR_TRANSFER_SDR;
      case 16:
        return C.COLOR_TRANSFER_ST2084;
      case 18:
        return C.COLOR_TRANSFER_HLG;
      default:
        return Format.NO_VALUE;
    }
  }

  /**
   * The color space of the video. Valid values are {@link C#COLOR_SPACE_BT601}, {@link
   * C#COLOR_SPACE_BT709}, {@link C#COLOR_SPACE_BT2020} or {@link Format#NO_VALUE} if unknown.
   */
  public final @C.ColorSpace int colorSpace;

  /**
   * The color range of the video. Valid values are {@link C#COLOR_RANGE_LIMITED}, {@link
   * C#COLOR_RANGE_FULL} or {@link Format#NO_VALUE} if unknown.
   */
  public final @C.ColorRange int colorRange;

  /**
   * The color transfer characteristics of the video. Valid values are {@link C#COLOR_TRANSFER_HLG},
   * {@link C#COLOR_TRANSFER_ST2084}, {@link C#COLOR_TRANSFER_SDR} or {@link Format#NO_VALUE} if
   * unknown.
   */
  public final @C.ColorTransfer int colorTransfer;

  /** HdrStaticInfo as defined in CTA-861.3, or null if none specified. */
  @Nullable public final byte[] hdrStaticInfo;

  // Lazily initialized hashcode.
  private int hashCode;

  /**
   * Constructs the ColorInfo.
   *
   * @param colorSpace The color space of the video.
   * @param colorRange The color range of the video.
   * @param colorTransfer The color transfer characteristics of the video.
   * @param hdrStaticInfo HdrStaticInfo as defined in CTA-861.3, or null if none specified.
   */
  public ColorInfo(
      @C.ColorSpace int colorSpace,
      @C.ColorRange int colorRange,
      @C.ColorTransfer int colorTransfer,
      @Nullable byte[] hdrStaticInfo) {
    this.colorSpace = colorSpace;
    this.colorRange = colorRange;
    this.colorTransfer = colorTransfer;
    this.hdrStaticInfo = hdrStaticInfo;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    ColorInfo other = (ColorInfo) obj;
    return colorSpace == other.colorSpace
        && colorRange == other.colorRange
        && colorTransfer == other.colorTransfer
        && Arrays.equals(hdrStaticInfo, other.hdrStaticInfo);
  }

  @Override
  public String toString() {
    return "ColorInfo("
        + colorSpace
        + ", "
        + colorRange
        + ", "
        + colorTransfer
        + ", "
        + (hdrStaticInfo != null)
        + ")";
  }

  @Override
  public int hashCode() {
    if (hashCode == 0) {
      int result = 17;
      result = 31 * result + colorSpace;
      result = 31 * result + colorRange;
      result = 31 * result + colorTransfer;
      result = 31 * result + Arrays.hashCode(hdrStaticInfo);
      hashCode = result;
    }
    return hashCode;
  }

  // Bundleable implementation

  @Documented
  @Retention(RetentionPolicy.SOURCE)
  @Target(TYPE_USE)
  @IntDef({
    FIELD_COLOR_SPACE,
    FIELD_COLOR_RANGE,
    FIELD_COLOR_TRANSFER,
    FIELD_HDR_STATIC_INFO,
  })
  private @interface FieldNumber {}

  private static final int FIELD_COLOR_SPACE = 0;
  private static final int FIELD_COLOR_RANGE = 1;
  private static final int FIELD_COLOR_TRANSFER = 2;
  private static final int FIELD_HDR_STATIC_INFO = 3;

  @Override
  public Bundle toBundle() {
    Bundle bundle = new Bundle();
    bundle.putInt(keyForField(FIELD_COLOR_SPACE), colorSpace);
    bundle.putInt(keyForField(FIELD_COLOR_RANGE), colorRange);
    bundle.putInt(keyForField(FIELD_COLOR_TRANSFER), colorTransfer);
    bundle.putByteArray(keyForField(FIELD_HDR_STATIC_INFO), hdrStaticInfo);
    return bundle;
  }

  public static final Creator<ColorInfo> CREATOR =
      bundle ->
          new ColorInfo(
              bundle.getInt(keyForField(FIELD_COLOR_SPACE), Format.NO_VALUE),
              bundle.getInt(keyForField(FIELD_COLOR_RANGE), Format.NO_VALUE),
              bundle.getInt(keyForField(FIELD_COLOR_TRANSFER), Format.NO_VALUE),
              bundle.getByteArray(keyForField(FIELD_HDR_STATIC_INFO)));

  private static String keyForField(@FieldNumber int field) {
    return Integer.toString(field, Character.MAX_RADIX);
  }
}
