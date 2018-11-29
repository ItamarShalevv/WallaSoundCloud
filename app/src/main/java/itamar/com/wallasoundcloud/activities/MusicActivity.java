package itamar.com.wallasoundcloud.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import itamar.com.wallasoundcloud.R;
import itamar.com.wallasoundcloud.data.Song;
import itamar.com.wallasoundcloud.global.BounceInterpolator;
import itamar.com.wallasoundcloud.global.Utils;
import itamar.com.wallasoundcloud.networking.HttpServer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static itamar.com.wallasoundcloud.global.Utils.MAX_LOADING_SONG;
import static itamar.com.wallasoundcloud.global.Utils.PLACE_HOLDER_IMAGE_SONG;

public class MusicActivity extends AppCompatActivity {

    public static final String KEY_POSITION = "KEY_POSITION";
    public static final String KEY_SONGS_DATA = "KEY_SONGS_DATA";
    public static final String KEY_NEED_LOADING_MORE = "KEY_NEED_LOADING_MORE";
    public static final String KEY_QUERY = "KEY_QUERY";
    public static final String KEY_MAX_LOADING = "KEY_MAX_LOADING";
    public static final int DURATION_ANIMATION_IMAGE_SONG_CLICKED = 3000;
    public static final int DURATION_FADE_IMAGE_ACTION = 200;


    private ImageView imageViewSong;
    private ImageView imageViewPrevious;
    private ImageView imageViewAction;
    private ImageView imageViewNext;
    private MediaPlayer mMediaPlayer;
    private CardView parentImageViewSong;
    private Snackbar mSnackBar;

    private ArrayList<Song> mSongList;
    private Song mCurrentSong;
    private String mQuery;

    private boolean hasNewSong;
    private int mCurrentPosition;
    private int mMaxLoading;
    private boolean needToPlay;
    private boolean isChangeConfig;
    private boolean isPrepared;
    private boolean needToLoadingMore;
    private Call<List<Song>> callListSong;


    /**
     * Initializes all objects and views
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.ActivityUtils.hideActionBar(this);
        initObjects();
        initLayoutAndViewByOrientation();
        showDetailsSong();
        changeSong(mCurrentSong.getStreamUrl());
    }

    /**
     * Initializes all the views
     */
    private void initViews() {
        imageViewSong = findViewById(R.id.image_view_song);
        imageViewPrevious = findViewById(R.id.image_view_previous);
        imageViewAction = findViewById(R.id.image_view_action);
        imageViewNext = findViewById(R.id.image_view_next);
        parentImageViewSong = findViewById(R.id.parent_image_view_song);
    }


    /**
     * Initializes all the objects and get other object from the intent
     * @see #KEY_NEED_LOADING_MORE and other key
     */
    private void initObjects() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mCurrentPosition = extras.getInt(KEY_POSITION, 0);
            mSongList = extras.getParcelableArrayList(KEY_SONGS_DATA);
            needToLoadingMore = extras.getBoolean(KEY_NEED_LOADING_MORE);
            mQuery = extras.getString(KEY_QUERY);
            mMaxLoading = extras.getInt(KEY_MAX_LOADING);
        }

        mMediaPlayer = new MediaPlayer();
        mCurrentSong = mSongList.get(mCurrentPosition);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }


    /**
     * Initializes all listener image for play and pause, button next and button previous
     */
    private void initListeners() {
        listenerImageSong();
        imageViewNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewClicked) {
                Utils.AnimationUtils.setAnimationClicked(viewClicked, new BounceInterpolator(), 3000);
                skipToNextSong();

            }
        });
        imageViewPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewClicked) {
                Utils.AnimationUtils.setAnimationClicked(viewClicked, new BounceInterpolator(), 3000);
                skipToPreviousSong();
            }
        });
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (needToPlay) {
                    playMusic();
                }
                cancelSnackBar();
                isPrepared = true;
                imageViewSong.setEnabled(true);
            }
        });
    }

    /**
     * Initializes the layout by the orientation
     */
    private void initLayoutAndViewByOrientation() {
        boolean isPortraitScreen = Utils.ActivityUtils.isPortraitScreen(getApplicationContext());
        if (isPortraitScreen) {
            setContentView(R.layout.activity_controller_media_portrait);
        } else {
            setContentView(R.layout.activity_controller_media_landscape);
        }
        initViews();
        initListeners();
    }

    /**
     * Initializes the layout and call when the Orientation change
     * @see MusicActivity#initLayoutAndViewByOrientation
     * */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        initLayoutAndViewByOrientation();
    }


    /**
     * cancel snackBar loading
     * @see MusicActivity#mSnackBar
     */
    private void cancelSnackBar() {
        if (mSnackBar != null) {
            mSnackBar.dismiss();
            mSnackBar = null;
        }
    }

    /**
     * Return back in the playlist
     */
    private void skipToPreviousSong() {
        mCurrentPosition = mCurrentPosition - 1;
        if (mCurrentPosition < 0) {
            mCurrentPosition = mSongList.size() - 1;
        }
        skipToSong(mCurrentPosition);
    }

    /**
     * One skips back in playlist
     */
    private void skipToNextSong() {
        mCurrentPosition += 1;
        if (mCurrentPosition == mSongList.size() && needToLoadingMore) {
            loadMoreIfNeed();
        } else {
            if (mCurrentPosition >= mSongList.size()) {
                mCurrentPosition = 0;
            }
            skipToSong(mCurrentPosition);
        }
    }

    /**
     * If have more songs to load and all the condition is ok so load more {@value Utils#MAX_LOADING_SONG}
     */
    private void loadMoreIfNeed() {
        if (mSongList.size() < mMaxLoading && needToLoadingMore) {
            showSnackBarLoading();
            if (callListSong == null) {
                int countLimit;
                if (mSongList.size() + MAX_LOADING_SONG > mMaxLoading) {
                    countLimit = (mSongList.size() + MAX_LOADING_SONG) % mMaxLoading;
                } else {
                    countLimit = MAX_LOADING_SONG;
                }
                callListSong = HttpServer.getInstance().searchSongs(mQuery, mSongList.size(), countLimit);
                callListSong.enqueue(new Callback<List<Song>>() {
                    @Override
                    public void onResponse(Call<List<Song>> call, Response<List<Song>> response) {
                        if (response.isSuccessful()) {
                            List<Song> dataBodyList = response.body();
                            if (dataBodyList != null) {
                                if (!dataBodyList.isEmpty()) {
                                    mSongList.addAll(dataBodyList);
                                    hasNewSong = true;
                                    skipToNextSong();
                                    if (dataBodyList.size() < MAX_LOADING_SONG) {
                                        needToLoadingMore = false;
                                    }
                                } else {
                                    needToLoadingMore = false;
                                }
                            }
                        }
                        callListSong = null;
                    }

                    @Override
                    public void onFailure(Call<List<Song>> call, Throwable t) {
                        callListSong = null;
                    }
                });
            }

        } else {
            cancelSnackBar();
        }
    }

    /**
     * If the song not prepared show snackBar prepared but not started yet, so start, else pause
     */
    private void listenerImageSong() {
        imageViewSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.AnimationUtils.setAnimationClicked(parentImageViewSong, new BounceInterpolator(), DURATION_ANIMATION_IMAGE_SONG_CLICKED);
                if (!mMediaPlayer.isPlaying()) {
                    needToPlay = true;
                    if (!isPrepared) {
                        showSnackBarLoading();
                    } else {
                        playMusic();
                    }
                } else {
                    if (isPrepared) {
                        needToPlay = false;
                    }
                    pauseMusic();
                }
            }
        });
    }


    /**
     * Show snackBar with progressBar
     * @see MusicActivity#mSnackBar
     * @see Utils.ActivityUtils#makeAndShowDialogProgress(android.app.Activity,String)
     */
    private void showSnackBarLoading() {
        mSnackBar = Utils.ActivityUtils.makeAndShowDialogProgress(this, getString(R.string.loading));
    }

    /**
     * Play the song and animated the button action
     */
    private void playMusic() {
        cancelSnackBar();
        needToPlay = true;
        mMediaPlayer.start();
        imageViewAction.animate().alpha(0).setDuration(DURATION_FADE_IMAGE_ACTION).withEndAction(new Runnable() {
            @Override
            public void run() {
                imageViewAction.setImageResource(R.drawable.ic_pause_primary);
                imageViewAction.animate().alpha(1).setDuration(DURATION_FADE_IMAGE_ACTION).start();
            }
        }).start();
    }

    /**
     * Pause the song and animated the button action
     */
    private void pauseMusic() {
        mMediaPlayer.pause();
        imageViewAction.animate().alpha(0).setDuration(DURATION_FADE_IMAGE_ACTION).withEndAction(new Runnable() {
            @Override
            public void run() {
                imageViewAction.setImageResource(R.drawable.ic_play_arrow_primary);
                imageViewAction.animate().alpha(1).setDuration(DURATION_FADE_IMAGE_ACTION).start();
            }
        }).start();

    }

    /**
     * @param url stream of the song
     * after prepared start auto
     */
    private void changeSong(String url) {
        try {
            mMediaPlayer.setDataSource(url + "?client_id=80a4bb0faf1a266b43ed13de89656b60");
            mMediaPlayer.prepareAsync();

        } catch (IOException e) {
            if (Utils.NetworkingUtils.isOnline(this)) {
                Utils.ActivityUtils.makeAndShowSnackBar(this, getString(R.string.error_try_again), Snackbar.LENGTH_SHORT);
            } else {
                Utils.ActivityUtils.makeAndShowSnackBar(this, getString(R.string.internet_not_available), Snackbar.LENGTH_SHORT);

            }

        }
    }

    /**
     * Show the image of song, if no have, show the default
     * @see Utils#PLACE_HOLDER_IMAGE_SONG
     */
    private void showDetailsSong() {
        if (mCurrentSong.getImageUrl() == null || mCurrentSong.getImageUrl().isEmpty()) {
            imageViewSong.setImageResource(PLACE_HOLDER_IMAGE_SONG);
        } else {
            String imageUrl = mCurrentSong.getImageUrl();
            imageUrl = imageUrl.replace("large", "t500x500");
            Picasso.get().load(imageUrl).placeholder(PLACE_HOLDER_IMAGE_SONG).into(imageViewSong);
        }
    }

    /**
     * Reset and release the music
     * @see #onDestroy()
     */
    private void releaseMusic() {
        mMediaPlayer.reset();
        mMediaPlayer.release();
    }


    /**
     * @param position of song from the list
     */
    private void skipToSong(int position) {
        mCurrentSong = mSongList.get(position);

        showDetailsSong();
        pauseMusic();
        mMediaPlayer.reset();
        showSnackBarLoading();
        needToPlay = true;
        imageViewSong.setEnabled(false);
        changeSong(mCurrentSong.getStreamUrl());
    }


    /**
     * If the activity enter to on destroy check if because of the screen rendering or that the user wanted to exit
     */
    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        isChangeConfig = true;
        return super.onRetainCustomNonConfigurationInstance();
    }

    /**
     * If the screen not front of user pause the music
     */
    @Override
    protected void onPause() {
        super.onPause();
        pauseMusic();
    }

    /**
     * After the user back play again if stop when he go out
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (needToPlay) {
            playMusic();
        }
    }

    /**
     * Call when the activity destroy, release the media player and return new songs if exits
     */
    @Override
    protected void onDestroy() {
        if (!isChangeConfig) {
            if (callListSong != null) {
                callListSong.cancel();
            }
            releaseMusic();
            if (hasNewSong) {
                Bundle extras = new Bundle();
                extras.putParcelableArrayList(KEY_SONGS_DATA, mSongList);
                Intent data = new Intent().putExtras(extras);
                setResult(RESULT_OK, data);
            }
        }
        super.onDestroy();
    }


}
