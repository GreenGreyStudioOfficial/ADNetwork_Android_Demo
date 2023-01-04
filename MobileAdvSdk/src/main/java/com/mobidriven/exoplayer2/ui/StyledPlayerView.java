/*
 * Copyright 2019 The Android Open Source Project
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
package com.mobidriven.exoplayer2.ui;

import static com.mobidriven.exoplayer2.Player.COMMAND_SET_VIDEO_SURFACE;
import static com.mobidriven.exoplayer2.util.Assertions.checkNotNull;
import static java.lang.annotation.ElementType.TYPE_USE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.opengl.GLSurfaceView;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import com.mobidriven.exoplayer2.C;
import com.mobidriven.exoplayer2.MediaMetadata;
import com.mobidriven.exoplayer2.PlaybackException;
import com.mobidriven.exoplayer2.Player;
import com.mobidriven.exoplayer2.Player.DiscontinuityReason;
import com.mobidriven.exoplayer2.Timeline;
import com.mobidriven.exoplayer2.Timeline.Period;
import com.mobidriven.exoplayer2.TracksInfo;
import com.mobidriven.exoplayer2.text.Cue;
import com.mobidriven.exoplayer2.ui.AspectRatioFrameLayout.ResizeMode;
import com.mobidriven.exoplayer2.util.Assertions;
import com.mobidriven.exoplayer2.util.ErrorMessageProvider;
import com.mobidriven.exoplayer2.util.Util;
import com.mobidriven.exoplayer2.video.VideoSize;
import com.google.common.collect.ImmutableList;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;
import org.checkerframework.checker.nullness.qual.EnsuresNonNullIf;
import org.checkerframework.checker.nullness.qual.RequiresNonNull;
import com.mobidriven.R;

public class StyledPlayerView extends FrameLayout implements AdViewProvider {

  /**
   * Determines when the buffering view is shown. One of {@link #SHOW_BUFFERING_NEVER}, {@link
   * #SHOW_BUFFERING_WHEN_PLAYING} or {@link #SHOW_BUFFERING_ALWAYS}.
   */
  @Documented
  @Retention(RetentionPolicy.SOURCE)
  @Target(TYPE_USE)
  @IntDef({SHOW_BUFFERING_NEVER, SHOW_BUFFERING_WHEN_PLAYING, SHOW_BUFFERING_ALWAYS})
  public @interface ShowBuffering {}
  /** The buffering view is never shown. */
  public static final int SHOW_BUFFERING_NEVER = 0;
  /**
   * The buffering view is shown when the player is in the {@link Player#STATE_BUFFERING buffering}
   * state and {@link Player#getPlayWhenReady() playWhenReady} is {@code true}.
   */
  public static final int SHOW_BUFFERING_WHEN_PLAYING = 1;
  /**
   * The buffering view is always shown when the player is in the {@link Player#STATE_BUFFERING
   * buffering} state.
   */
  public static final int SHOW_BUFFERING_ALWAYS = 2;

  private static final int SURFACE_TYPE_NONE = 0;
  private static final int SURFACE_TYPE_SURFACE_VIEW = 1;
  private static final int SURFACE_TYPE_TEXTURE_VIEW = 2;
  private static final int SURFACE_TYPE_SPHERICAL_GL_SURFACE_VIEW = 3;
  private static final int SURFACE_TYPE_VIDEO_DECODER_GL_SURFACE_VIEW = 4;

  private final ComponentListener componentListener;
  @Nullable private final AspectRatioFrameLayout contentFrame;
  @Nullable private final View shutterView;
  @Nullable private final View surfaceView;
  private final boolean surfaceViewIgnoresVideoAspectRatio;
  @Nullable private final ImageView artworkView;
  @Nullable private final View bufferingView;
  @Nullable private final TextView errorMessageView;
  @Nullable private final FrameLayout adOverlayFrameLayout;
  @Nullable private final FrameLayout overlayFrameLayout;

  @Nullable private Player player;
  private boolean useArtwork;
  @Nullable private Drawable defaultArtwork;
  private @ShowBuffering int showBuffering;
  private boolean keepContentOnPlayerReset;
  @Nullable private ErrorMessageProvider<? super PlaybackException> errorMessageProvider;
  @Nullable private CharSequence customErrorMessage;
  private int controllerShowTimeoutMs;
  private boolean controllerAutoShow;
  private boolean controllerHideDuringAds;
  private boolean controllerHideOnTouch;
  private int textureViewRotation;
  private boolean isTouching;
  private static final int PICTURE_TYPE_FRONT_COVER = 3;
  private static final int PICTURE_TYPE_NOT_SET = -1;

  public StyledPlayerView(Context context) {
    this(context, /* attrs= */ null);
  }

  public StyledPlayerView(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, /* defStyleAttr= */ 0);
  }

  @SuppressWarnings({"nullness:argument", "nullness:method.invocation"})
  public StyledPlayerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    componentListener = new ComponentListener();

    if (isInEditMode()) {
      contentFrame = null;
      shutterView = null;
      surfaceView = null;
      surfaceViewIgnoresVideoAspectRatio = false;
      artworkView = null;
      bufferingView = null;
      errorMessageView = null;
      adOverlayFrameLayout = null;
      overlayFrameLayout = null;
      ImageView logo = new ImageView(context);
      addView(logo);
      return;
    }

    boolean shutterColorSet = false;
    int shutterColor = 0;
    int playerLayoutId = R.layout.styled_player_view;
    boolean useArtwork = true;
    int defaultArtworkId = 0;
    boolean useController = true;
    int surfaceType = SURFACE_TYPE_SURFACE_VIEW;
    int resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT;
    boolean controllerHideOnTouch = true;
    boolean controllerAutoShow = true;
    boolean controllerHideDuringAds = true;
    int showBuffering = SHOW_BUFFERING_NEVER;
    if (attrs != null) {
      TypedArray a =
          context
              .getTheme()
              .obtainStyledAttributes(
                  attrs, R.styleable.StyledPlayerView, defStyleAttr, /* defStyleRes= */ 0);
      try {
        shutterColorSet = a.hasValue(R.styleable.StyledPlayerView_shutter_background_color);
        shutterColor =
            a.getColor(R.styleable.StyledPlayerView_shutter_background_color, shutterColor);
        playerLayoutId =
            a.getResourceId(R.styleable.StyledPlayerView_player_layout_id, playerLayoutId);
        useArtwork = a.getBoolean(R.styleable.StyledPlayerView_use_artwork, useArtwork);
        defaultArtworkId =
            a.getResourceId(R.styleable.StyledPlayerView_default_artwork, defaultArtworkId);
        useController = a.getBoolean(R.styleable.StyledPlayerView_use_controller, useController);
        surfaceType = a.getInt(R.styleable.StyledPlayerView_surface_type, surfaceType);
        resizeMode = a.getInt(R.styleable.StyledPlayerView_resize_mode, resizeMode);
        controllerShowTimeoutMs =
            a.getInt(R.styleable.StyledPlayerView_show_timeout, controllerShowTimeoutMs);
        controllerHideOnTouch =
            a.getBoolean(R.styleable.StyledPlayerView_hide_on_touch, controllerHideOnTouch);
        controllerAutoShow =
            a.getBoolean(R.styleable.StyledPlayerView_auto_show, controllerAutoShow);
        showBuffering = a.getInteger(R.styleable.StyledPlayerView_show_buffering, showBuffering);
        keepContentOnPlayerReset =
            a.getBoolean(
                R.styleable.StyledPlayerView_keep_content_on_player_reset,
                keepContentOnPlayerReset);
        controllerHideDuringAds =
            a.getBoolean(R.styleable.StyledPlayerView_hide_during_ads, controllerHideDuringAds);
      } finally {
        a.recycle();
      }
    }

    LayoutInflater.from(context).inflate(playerLayoutId, this);
    setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);

    // Content frame.
    contentFrame = findViewById(R.id.exo_content_frame);
    if (contentFrame != null) {
      setResizeModeRaw(contentFrame, resizeMode);
    }

    // Shutter view.
    shutterView = findViewById(R.id.exo_shutter);
    if (shutterView != null && shutterColorSet) {
      shutterView.setBackgroundColor(shutterColor);
    }

    // Create a surface view and insert it into the content frame, if there is one.
    boolean surfaceViewIgnoresVideoAspectRatio = false;
    if (contentFrame != null && surfaceType != SURFACE_TYPE_NONE) {
      ViewGroup.LayoutParams params =
          new ViewGroup.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
      switch (surfaceType) {
        case SURFACE_TYPE_TEXTURE_VIEW:
          surfaceView = new TextureView(context);
          break;
        case SURFACE_TYPE_SPHERICAL_GL_SURFACE_VIEW:
          try {
            Class<?> clazz =
                Class.forName(
                    "com.google.android.exoplayer2.video.spherical.SphericalGLSurfaceView");
            surfaceView = (View) clazz.getConstructor(Context.class).newInstance(context);
          } catch (Exception e) {
            throw new IllegalStateException(
                "spherical_gl_surface_view requires an ExoPlayer dependency", e);
          }
          surfaceViewIgnoresVideoAspectRatio = true;
          break;
        case SURFACE_TYPE_VIDEO_DECODER_GL_SURFACE_VIEW:
          try {
            Class<?> clazz =
                Class.forName("com.google.android.exoplayer2.video.VideoDecoderGLSurfaceView");
            surfaceView = (View) clazz.getConstructor(Context.class).newInstance(context);
          } catch (Exception e) {
            throw new IllegalStateException(
                "video_decoder_gl_surface_view requires an ExoPlayer dependency", e);
          }
          break;
        default:
          surfaceView = new SurfaceView(context);
          break;
      }
      surfaceView.setLayoutParams(params);
      // We don't want surfaceView to be clickable separately to the StyledPlayerView itself, but we
      // do want to register as an OnClickListener so that surfaceView implementations can propagate
      // click events up to the StyledPlayerView by calling their own performClick method.
      surfaceView.setOnClickListener(componentListener);
      surfaceView.setClickable(false);
      contentFrame.addView(surfaceView, 0);
    } else {
      surfaceView = null;
    }
    this.surfaceViewIgnoresVideoAspectRatio = surfaceViewIgnoresVideoAspectRatio;

    // Ad overlay frame layout.
    adOverlayFrameLayout = findViewById(R.id.exo_ad_overlay);

    // Overlay frame layout.
    overlayFrameLayout = findViewById(R.id.exo_overlay);

    // Artwork view.
    artworkView = findViewById(R.id.exo_artwork);
    this.useArtwork = useArtwork && artworkView != null;
    if (defaultArtworkId != 0) {
      defaultArtwork = ContextCompat.getDrawable(getContext(), defaultArtworkId);
    }

    // Buffering view.
    bufferingView = findViewById(R.id.exo_buffering);
    if (bufferingView != null) {
      bufferingView.setVisibility(View.GONE);
    }
    this.showBuffering = showBuffering;

    // Error message view.
    errorMessageView = findViewById(R.id.exo_error_message);
    if (errorMessageView != null) {
      errorMessageView.setVisibility(View.GONE);
    }

    updateContentDescription();
  }

  /**
   * Switches the view targeted by a given {@link Player}.
   *
   * @param player The player whose target view is being switched.
   * @param oldPlayerView The old view to detach from the player.
   * @param newPlayerView The new view to attach to the player.
   */
  public static void switchTargetView(
      Player player,
      @Nullable StyledPlayerView oldPlayerView,
      @Nullable StyledPlayerView newPlayerView) {
    if (oldPlayerView == newPlayerView) {
      return;
    }
    // We attach the new view before detaching the old one because this ordering allows the player
    // to swap directly from one surface to another, without transitioning through a state where no
    // surface is attached. This is significantly more efficient and achieves a more seamless
    // transition when using platform provided video decoders.
    if (newPlayerView != null) {
      newPlayerView.setPlayer(player);
    }
    if (oldPlayerView != null) {
      oldPlayerView.setPlayer(null);
    }
  }

  /** Returns the player currently set on this view, or null if no player is set. */
  @Nullable
  public Player getPlayer() {
    return player;
  }

  /**
   * Sets the {@link Player} to use.
   *
   * <p>To transition a {@link Player} from targeting one view to another, it's recommended to use
   * {@link #switchTargetView(Player, StyledPlayerView, StyledPlayerView)} rather than this method.
   * If you do wish to use this method directly, be sure to attach the player to the new view
   * <em>before</em> calling {@code setPlayer(null)} to detach it from the old one. This ordering is
   * significantly more efficient and may allow for more seamless transitions.
   *
   * @param player The {@link Player} to use, or {@code null} to detach the current player. Only
   *     players which are accessed on the main thread are supported ({@code
   *     player.getApplicationLooper() == Looper.getMainLooper()}).
   */
  public void setPlayer(@Nullable Player player) {
    Assertions.checkState(Looper.myLooper() == Looper.getMainLooper());
    Assertions.checkArgument(
        player == null || player.getApplicationLooper() == Looper.getMainLooper());
    if (this.player == player) {
      return;
    }
    @Nullable Player oldPlayer = this.player;
    if (oldPlayer != null) {
      oldPlayer.removeListener(componentListener);
      if (surfaceView instanceof TextureView) {
        oldPlayer.clearVideoTextureView((TextureView) surfaceView);
      } else if (surfaceView instanceof SurfaceView) {
        oldPlayer.clearVideoSurfaceView((SurfaceView) surfaceView);
      }
    }

    this.player = player;

    updateBuffering();
    updateErrorMessage();
    updateForCurrentTrackSelections(/* isNewPlayer= */ true);
    if (player != null) {
      if (player.isCommandAvailable(COMMAND_SET_VIDEO_SURFACE)) {
        if (surfaceView instanceof TextureView) {
          player.setVideoTextureView((TextureView) surfaceView);
        } else if (surfaceView instanceof SurfaceView) {
          player.setVideoSurfaceView((SurfaceView) surfaceView);
        }
        updateAspectRatio();
      }

      player.addListener(componentListener);
      maybeShowController(false);
    } else {
      hideController();
    }
  }

  @Override
  public void setVisibility(int visibility) {
    super.setVisibility(visibility);
    if (surfaceView instanceof SurfaceView) {
      // Work around https://github.com/google/ExoPlayer/issues/3160.
      surfaceView.setVisibility(visibility);
    }
  }

  /**
   * Sets the {@link ResizeMode}.
   *
   * @param resizeMode The {@link ResizeMode}.
   */
  public void setResizeMode(@ResizeMode int resizeMode) {
    Assertions.checkStateNotNull(contentFrame);
    contentFrame.setResizeMode(resizeMode);
  }

  /** Returns the {@link ResizeMode}. */
  public @ResizeMode int getResizeMode() {
    Assertions.checkStateNotNull(contentFrame);
    return contentFrame.getResizeMode();
  }

  /** Returns whether artwork is displayed if present in the media. */
  public boolean getUseArtwork() {
    return useArtwork;
  }

  /**
   * Sets whether artwork is displayed if present in the media.
   *
   * @param useArtwork Whether artwork is displayed.
   */
  public void setUseArtwork(boolean useArtwork) {
    Assertions.checkState(!useArtwork || artworkView != null);
    if (this.useArtwork != useArtwork) {
      this.useArtwork = useArtwork;
      updateForCurrentTrackSelections(/* isNewPlayer= */ false);
    }
  }

  /** Returns the default artwork to display. */
  @Nullable
  public Drawable getDefaultArtwork() {
    return defaultArtwork;
  }

  /**
   * Sets the default artwork to display if {@code useArtwork} is {@code true} and no artwork is
   * present in the media.
   *
   * @param defaultArtwork the default artwork to display
   */
  public void setDefaultArtwork(@Nullable Drawable defaultArtwork) {
    if (this.defaultArtwork != defaultArtwork) {
      this.defaultArtwork = defaultArtwork;
      updateForCurrentTrackSelections(/* isNewPlayer= */ false);
    }
  }





  /**
   * Sets the background color of the {@code exo_shutter} view.
   *
   * @param color The background color.
   */
  public void setShutterBackgroundColor(int color) {
    if (shutterView != null) {
      shutterView.setBackgroundColor(color);
    }
  }

  /**
   * Sets whether the currently displayed video frame or media artwork is kept visible when the
   * player is reset. A player reset is defined to mean the player being re-prepared with different
   * media, the player transitioning to unprepared media or an empty list of media items, or the
   * player being replaced or cleared by calling {@link #setPlayer(Player)}.
   *
   * <p>If enabled, the currently displayed video frame or media artwork will be kept visible until
   * the player set on the view has been successfully prepared with new media and loaded enough of
   * it to have determined the available tracks. Hence enabling this option allows transitioning
   * from playing one piece of media to another, or from using one player instance to another,
   * without clearing the view's content.
   *
   * <p>If disabled, the currently displayed video frame or media artwork will be hidden as soon as
   * the player is reset. Note that the video frame is hidden by making {@code exo_shutter} visible.
   * Hence the video frame will not be hidden if using a custom layout that omits this view.
   *
   * @param keepContentOnPlayerReset Whether the currently displayed video frame or media artwork is
   *     kept visible when the player is reset.
   */
  public void setKeepContentOnPlayerReset(boolean keepContentOnPlayerReset) {
    if (this.keepContentOnPlayerReset != keepContentOnPlayerReset) {
      this.keepContentOnPlayerReset = keepContentOnPlayerReset;
      updateForCurrentTrackSelections(/* isNewPlayer= */ false);
    }
  }

  /**
   * Sets whether a buffering spinner is displayed when the player is in the buffering state. The
   * buffering spinner is not displayed by default.
   *
   * @param showBuffering The mode that defines when the buffering spinner is displayed. One of
   *     {@link #SHOW_BUFFERING_NEVER}, {@link #SHOW_BUFFERING_WHEN_PLAYING} and {@link
   *     #SHOW_BUFFERING_ALWAYS}.
   */
  public void setShowBuffering(@ShowBuffering int showBuffering) {
    if (this.showBuffering != showBuffering) {
      this.showBuffering = showBuffering;
      updateBuffering();
    }
  }

  /**
   * Sets the optional {@link ErrorMessageProvider}.
   *
   * @param errorMessageProvider The error message provider.
   */
  public void setErrorMessageProvider(
      @Nullable ErrorMessageProvider<? super PlaybackException> errorMessageProvider) {
    if (this.errorMessageProvider != errorMessageProvider) {
      this.errorMessageProvider = errorMessageProvider;
      updateErrorMessage();
    }
  }

  /**
   * Sets a custom error message to be displayed by the view. The error message will be displayed
   * permanently, unless it is cleared by passing {@code null} to this method.
   *
   * @param message The message to display, or {@code null} to clear a previously set message.
   */
  public void setCustomErrorMessage(@Nullable CharSequence message) {
    Assertions.checkState(errorMessageView != null);
    customErrorMessage = message;
    updateErrorMessage();
  }

  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    if (player != null && player.isPlayingAd()) {
      return super.dispatchKeyEvent(event);
    }

    boolean isDpadKey = isDpadKey(event.getKeyCode());
    boolean handled = false;
     if (dispatchMediaKeyEvent(event) || super.dispatchKeyEvent(event)) {
      // The key event was handled as a media key or by the super class. We should also show the
      // controller, or extend its show timeout if already visible.
      maybeShowController(true);
      handled = true;
    } else if (isDpadKey && useController()) {
      // The key event wasn't handled, but we should extend the controller's show timeout.
      maybeShowController(true);
    }
    return handled;
  }

  /**
   * Called to process media key events. Any {@link KeyEvent} can be passed but only media key
   * events will be handled. Does nothing if playback controls are disabled.
   *
   * @param event A key event.
   * @return Whether the key event was handled.
   */
  public boolean dispatchMediaKeyEvent(KeyEvent event) {
    return false;
  }


  /**
   * Shows the playback controls. Does nothing if playback controls are disabled.
   *
   * <p>The playback controls are automatically hidden during playback after {{@link
   * #getControllerShowTimeoutMs()}}. They are shown indefinitely when playback has not started yet,
   * is paused, has ended or failed.
   */
  public void showController() {
    showController(shouldShowControllerIndefinitely());
  }

  /** Hides the playback controls. Does nothing if playback controls are disabled. */
  public void hideController() {
  }

  /**
   * Returns the playback controls timeout. The playback controls are automatically hidden after
   * this duration of time has elapsed without user input and with playback or buffering in
   * progress.
   *
   * @return The timeout in milliseconds. A non-positive value will cause the controller to remain
   *     visible indefinitely.
   */
  public int getControllerShowTimeoutMs() {
    return controllerShowTimeoutMs;
  }

  /**
   * Sets the playback controls timeout. The playback controls are automatically hidden after this
   * duration of time has elapsed without user input and with playback or buffering in progress.
   *
   * @param controllerShowTimeoutMs The timeout in milliseconds. A non-positive value will cause the
   *     controller to remain visible indefinitely.
   */

  /** Returns whether the playback controls are hidden by touch events. */
  public boolean getControllerHideOnTouch() {
    return controllerHideOnTouch;
  }


  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (!useController() || player == null) {
      return false;
    }
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        isTouching = true;
        return true;
      case MotionEvent.ACTION_UP:
        if (isTouching) {
          isTouching = false;
          return performClick();
        }
        return false;
      default:
        return false;
    }
  }

  @Override
  public boolean performClick() {
    super.performClick();
    return toggleControllerVisibility();
  }

  @Override
  public boolean onTrackballEvent(MotionEvent ev) {
    if (!useController() || player == null) {
      return false;
    }
    maybeShowController(true);
    return true;
  }

  /**
   * Should be called when the player is visible to the user, if the {@code surface_type} extends
   * {@link GLSurfaceView}. It is the counterpart to {@link #onPause()}.
   *
   * <p>This method should typically be called in {@code Activity.onStart()}, or {@code
   * Activity.onResume()} for API versions &lt;= 23.
   */
  public void onResume() {
    if (surfaceView instanceof GLSurfaceView) {
      ((GLSurfaceView) surfaceView).onResume();
    }
  }

  /**
   * Should be called when the player is no longer visible to the user, if the {@code surface_type}
   * extends {@link GLSurfaceView}. It is the counterpart to {@link #onResume()}.
   *
   * <p>This method should typically be called in {@code Activity.onStop()}, or {@code
   * Activity.onPause()} for API versions &lt;= 23.
   */
  public void onPause() {
    if (surfaceView instanceof GLSurfaceView) {
      ((GLSurfaceView) surfaceView).onPause();
    }
  }

  /**
   * Called when there's a change in the desired aspect ratio of the content frame. The default
   * implementation sets the aspect ratio of the content frame to the specified value.
   *
   * @param contentFrame The content frame, or {@code null}.
   * @param aspectRatio The aspect ratio to apply.
   */
  protected void onContentAspectRatioChanged(
      @Nullable AspectRatioFrameLayout contentFrame, float aspectRatio) {
    if (contentFrame != null) {
      contentFrame.setAspectRatio(aspectRatio);
    }
  }

  // AdsLoader.AdViewProvider implementation.

  @Override
  public ViewGroup getAdViewGroup() {
    return Assertions.checkStateNotNull(
        adOverlayFrameLayout, "exo_ad_overlay must be present for ad playback");
  }

  @Override
  public List<AdOverlayInfo> getAdOverlayInfos() {
    List<AdOverlayInfo> overlayViews = new ArrayList<>();
    if (overlayFrameLayout != null) {
      overlayViews.add(
          new AdOverlayInfo(
              overlayFrameLayout,
              AdOverlayInfo.PURPOSE_NOT_VISIBLE,
              /* detailedReason= */ "Transparent overlay does not impact viewability"));
    }
    return ImmutableList.copyOf(overlayViews);
  }

  // Internal methods.

  @EnsuresNonNullIf(expression = "controller", result = true)
  private boolean useController() {
    return false;
  }

  @EnsuresNonNullIf(expression = "artworkView", result = true)
  private boolean useArtwork() {
    return false;
  }

  private boolean toggleControllerVisibility() {
    return false;
  }

  /** Shows the playback controls, but only if forced or shown indefinitely. */
  private void maybeShowController(boolean isForced) {
    if (isPlayingAd() && controllerHideDuringAds) {
      return;
    }
  }

  private boolean shouldShowControllerIndefinitely() {
    if (player == null) {
      return true;
    }
    int playbackState = player.getPlaybackState();
    return controllerAutoShow
        && !player.getCurrentTimeline().isEmpty()
        && (playbackState == Player.STATE_IDLE
            || playbackState == Player.STATE_ENDED
            || !checkNotNull(player).getPlayWhenReady());
  }

  private void showController(boolean showIndefinitely) {
    return;
  }

  private boolean isPlayingAd() {
    return player != null && player.isPlayingAd() && player.getPlayWhenReady();
  }

  private void updateForCurrentTrackSelections(boolean isNewPlayer) {
    @Nullable Player player = this.player;
    if (player == null || player.getCurrentTracksInfo().getTrackGroupInfos().isEmpty()) {
      if (!keepContentOnPlayerReset) {
        hideArtwork();
        closeShutter();
      }
      return;
    }

    if (isNewPlayer && !keepContentOnPlayerReset) {
      // Hide any video from the previous player.
      closeShutter();
    }

    if (player.getCurrentTracksInfo().isTypeSelected(C.TRACK_TYPE_VIDEO)) {
      // Video enabled, so artwork must be hidden. If the shutter is closed, it will be opened
      // in onRenderedFirstFrame().
      hideArtwork();
      return;
    }

    // Video disabled so the shutter must be closed.
    closeShutter();
    // Display artwork if enabled and available, else hide it.
    if (useArtwork()) {
      if (setArtworkFromMediaMetadata(player.getMediaMetadata())) {
        return;
      }
      if (setDrawableArtwork(defaultArtwork)) {
        return;
      }
    }
    // Artwork disabled or unavailable.
    hideArtwork();
  }

  @RequiresNonNull("artworkView")
  private boolean setArtworkFromMediaMetadata(MediaMetadata mediaMetadata) {
    if (mediaMetadata.artworkData == null) {
      return false;
    }
    Bitmap bitmap =
        BitmapFactory.decodeByteArray(
            mediaMetadata.artworkData, /* offset= */ 0, mediaMetadata.artworkData.length);
    return setDrawableArtwork(new BitmapDrawable(getResources(), bitmap));
  }

  @RequiresNonNull("artworkView")
  private boolean setDrawableArtwork(@Nullable Drawable drawable) {
    if (drawable != null) {
      int drawableWidth = drawable.getIntrinsicWidth();
      int drawableHeight = drawable.getIntrinsicHeight();
      if (drawableWidth > 0 && drawableHeight > 0) {
        float artworkAspectRatio = (float) drawableWidth / drawableHeight;
        onContentAspectRatioChanged(contentFrame, artworkAspectRatio);
        artworkView.setImageDrawable(drawable);
        artworkView.setVisibility(VISIBLE);
        return true;
      }
    }
    return false;
  }

  private void hideArtwork() {
    if (artworkView != null) {
      artworkView.setImageResource(android.R.color.transparent); // Clears any bitmap reference.
      artworkView.setVisibility(INVISIBLE);
    }
  }

  private void closeShutter() {
    if (shutterView != null) {
      shutterView.setVisibility(View.VISIBLE);
    }
  }

  private void updateBuffering() {
    if (bufferingView != null) {
      boolean showBufferingSpinner =
          player != null
              && player.getPlaybackState() == Player.STATE_BUFFERING
              && (showBuffering == SHOW_BUFFERING_ALWAYS
                  || (showBuffering == SHOW_BUFFERING_WHEN_PLAYING && player.getPlayWhenReady()));
      bufferingView.setVisibility(showBufferingSpinner ? View.VISIBLE : View.GONE);
    }
  }

  private void updateErrorMessage() {
    if (errorMessageView != null) {
      if (customErrorMessage != null) {
        errorMessageView.setText(customErrorMessage);
        errorMessageView.setVisibility(View.VISIBLE);
        return;
      }
      @Nullable PlaybackException error = player != null ? player.getPlayerError() : null;
      if (error != null && errorMessageProvider != null) {
        CharSequence errorMessage = errorMessageProvider.getErrorMessage(error).second;
        errorMessageView.setText(errorMessage);
        errorMessageView.setVisibility(View.VISIBLE);
      } else {
        errorMessageView.setVisibility(View.GONE);
      }
    }
  }

  private void updateContentDescription() {

  }

  private void updateControllerVisibility() {
    if (isPlayingAd() && controllerHideDuringAds) {
      hideController();
    } else {
      maybeShowController(false);
    }
  }

  private void updateAspectRatio() {
    VideoSize videoSize = player != null ? player.getVideoSize() : VideoSize.UNKNOWN;
    int width = videoSize.width;
    int height = videoSize.height;
    int unappliedRotationDegrees = videoSize.unappliedRotationDegrees;
    float videoAspectRatio =
        (height == 0 || width == 0) ? 0 : (width * videoSize.pixelWidthHeightRatio) / height;

    if (surfaceView instanceof TextureView) {
      // Try to apply rotation transformation when our surface is a TextureView.
      if (videoAspectRatio > 0
          && (unappliedRotationDegrees == 90 || unappliedRotationDegrees == 270)) {
        // We will apply a rotation 90/270 degree to the output texture of the TextureView.
        // In this case, the output video's width and height will be swapped.
        videoAspectRatio = 1 / videoAspectRatio;
      }
      if (textureViewRotation != 0) {
        surfaceView.removeOnLayoutChangeListener(componentListener);
      }
      textureViewRotation = unappliedRotationDegrees;
      if (textureViewRotation != 0) {
        // The texture view's dimensions might be changed after layout step.
        // So add an OnLayoutChangeListener to apply rotation after layout step.
        surfaceView.addOnLayoutChangeListener(componentListener);
      }
      applyTextureViewRotation((TextureView) surfaceView, textureViewRotation);
    }

    onContentAspectRatioChanged(
        contentFrame, surfaceViewIgnoresVideoAspectRatio ? 0 : videoAspectRatio);
  }



  @SuppressWarnings("ResourceType")
  private static void setResizeModeRaw(AspectRatioFrameLayout aspectRatioFrame, int resizeMode) {
    aspectRatioFrame.setResizeMode(resizeMode);
  }

  /** Applies a texture rotation to a {@link TextureView}. */
  private static void applyTextureViewRotation(TextureView textureView, int textureViewRotation) {
    Matrix transformMatrix = new Matrix();
    float textureViewWidth = textureView.getWidth();
    float textureViewHeight = textureView.getHeight();
    if (textureViewWidth != 0 && textureViewHeight != 0 && textureViewRotation != 0) {
      float pivotX = textureViewWidth / 2;
      float pivotY = textureViewHeight / 2;
      transformMatrix.postRotate(textureViewRotation, pivotX, pivotY);

      // After rotation, scale the rotated texture to fit the TextureView size.
      RectF originalTextureRect = new RectF(0, 0, textureViewWidth, textureViewHeight);
      RectF rotatedTextureRect = new RectF();
      transformMatrix.mapRect(rotatedTextureRect, originalTextureRect);
      transformMatrix.postScale(
          textureViewWidth / rotatedTextureRect.width(),
          textureViewHeight / rotatedTextureRect.height(),
          pivotX,
          pivotY);
    }
    textureView.setTransform(transformMatrix);
  }

  @SuppressLint("InlinedApi")
  private boolean isDpadKey(int keyCode) {
    return keyCode == KeyEvent.KEYCODE_DPAD_UP
        || keyCode == KeyEvent.KEYCODE_DPAD_UP_RIGHT
        || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
        || keyCode == KeyEvent.KEYCODE_DPAD_DOWN_RIGHT
        || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
        || keyCode == KeyEvent.KEYCODE_DPAD_DOWN_LEFT
        || keyCode == KeyEvent.KEYCODE_DPAD_LEFT
        || keyCode == KeyEvent.KEYCODE_DPAD_UP_LEFT
        || keyCode == KeyEvent.KEYCODE_DPAD_CENTER;
  }

  private final class ComponentListener
      implements Player.Listener,
          OnLayoutChangeListener,
          OnClickListener{

    private final Period period;
    private @Nullable Object lastPeriodUidWithTracks;

    public ComponentListener() {
      period = new Period();
    }

    // Player.Listener implementation

    @Override
    public void onCues(List<Cue> cues) {

    }

    @Override
    public void onVideoSizeChanged(VideoSize videoSize) {
      updateAspectRatio();
    }

    @Override
    public void onRenderedFirstFrame() {
      if (shutterView != null) {
        shutterView.setVisibility(INVISIBLE);
      }
    }

    @Override
    public void onTracksInfoChanged(TracksInfo tracksInfo) {
      // Suppress the update if transitioning to an unprepared period within the same window. This
      // is necessary to avoid closing the shutter when such a transition occurs. See:
      // https://github.com/google/ExoPlayer/issues/5507.
      Player player = checkNotNull(StyledPlayerView.this.player);
      Timeline timeline = player.getCurrentTimeline();
      if (timeline.isEmpty()) {
        lastPeriodUidWithTracks = null;
      } else if (!player.getCurrentTracksInfo().getTrackGroupInfos().isEmpty()) {
        lastPeriodUidWithTracks =
            timeline.getPeriod(player.getCurrentPeriodIndex(), period, /* setIds= */ true).uid;
      } else if (lastPeriodUidWithTracks != null) {
        int lastPeriodIndexWithTracks = timeline.getIndexOfPeriod(lastPeriodUidWithTracks);
        if (lastPeriodIndexWithTracks != C.INDEX_UNSET) {
          int lastWindowIndexWithTracks =
              timeline.getPeriod(lastPeriodIndexWithTracks, period).windowIndex;
          if (player.getCurrentMediaItemIndex() == lastWindowIndexWithTracks) {
            // We're in the same media item. Suppress the update.
            return;
          }
        }
        lastPeriodUidWithTracks = null;
      }

      updateForCurrentTrackSelections(/* isNewPlayer= */ false);
    }

    @Override
    public void onPlaybackStateChanged(@Player.State int playbackState) {
      updateBuffering();
      updateErrorMessage();
      updateControllerVisibility();
    }

    @Override
    public void onPlayWhenReadyChanged(
        boolean playWhenReady, @Player.PlayWhenReadyChangeReason int reason) {
      updateBuffering();
      updateControllerVisibility();
    }

    @Override
    public void onPositionDiscontinuity(
        Player.PositionInfo oldPosition,
        Player.PositionInfo newPosition,
        @DiscontinuityReason int reason) {
      if (isPlayingAd() && controllerHideDuringAds) {
        hideController();
      }
    }

    // OnLayoutChangeListener implementation

    @Override
    public void onLayoutChange(
        View view,
        int left,
        int top,
        int right,
        int bottom,
        int oldLeft,
        int oldTop,
        int oldRight,
        int oldBottom) {
      applyTextureViewRotation((TextureView) view, textureViewRotation);
    }

    // OnClickListener implementation

    @Override
    public void onClick(View view) {
      toggleControllerVisibility();
    }
  }
}