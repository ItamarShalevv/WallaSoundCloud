package itamar.com.wallasoundcloud.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import itamar.com.wallasoundcloud.R;
import itamar.com.wallasoundcloud.adapter.SongAdapter;
import itamar.com.wallasoundcloud.data.Song;
import itamar.com.wallasoundcloud.global.Utils;
import itamar.com.wallasoundcloud.networking.HttpServer;
import itamar.com.wallasoundcloud.viewmodel.ViewModelSongList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static itamar.com.wallasoundcloud.global.Utils.MAX_LOADING_SONG;

public class SearchActivity extends AppCompatActivity {

    private boolean isChangeConfig;
    private String mTypeLayoutManager;
    private EditText editTextSearch;
    private ImageView imageViewSearch;
    private RecyclerView recyclerSongs;
    private RecyclerView.LayoutManager mLayoutManager;
    private ImageView imageViewGrid;
    private ImageView imageViewLinear;
    private SongAdapter mSongAdapter;
    private ViewModelSongList mViewModelSongList;
    private Snackbar mSnackBar;
    private DividerItemDecoration mDividerItemDecoration;
    private String mQuery;

    /**
     * Initializes all objects and views
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.ActivityUtils.hideActionBar(this);
        setContentView(R.layout.activity_search);
        initViews();
        initListeners();
        initObjects();
        initRecyclerView();
    }

    /**
     * Initializes all the views
     */
    private void initViews() {
        editTextSearch = findViewById(R.id.edit_text_search);
        imageViewSearch = findViewById(R.id.image_view_search);
        recyclerSongs = findViewById(R.id.recycler_songs);
        imageViewGrid = findViewById(R.id.image_view_grid);
        imageViewLinear = findViewById(R.id.image_view_linear);
    }


    /**
     * Initializes all the objects
     */
    private void initObjects() {
        mTypeLayoutManager = Utils.SharedPreferencesUtils.loadCurrentLayoutType(getApplicationContext());
        mLayoutManager = layoutManagerFromType(mTypeLayoutManager);
        mViewModelSongList = ViewModelProviders.of(this).get(ViewModelSongList.class);
        mDividerItemDecoration = new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL);
    }

    /**
     * Initializes all the RecyclerView and the layout manager by the shard preference
     */
    private void initRecyclerView() {
        mSongAdapter = new SongAdapter(this, mViewModelSongList.getSongsArrayList(), mTypeLayoutManager);
        recyclerSongs.setHasFixedSize(true);
        recyclerSongs.setLayoutManager(mLayoutManager);
        recyclerSongs.setAdapter(mSongAdapter);
        if (mTypeLayoutManager.equals(Utils.SharedPreferencesUtils.TYPE_LAYOUT_MANAGER_LINEAR_VERTICAL)) {
            recyclerSongs.addItemDecoration(mDividerItemDecoration);
        }
        recyclerSongs.setItemAnimator(null);
    }

    /**
     * Initializes all listener image for play and pause, button next and button previous
     */
    private void initListeners() {
        listenerButtonSearch();
        imageViewGrid.setOnClickListener(new ChangeLayoutManagerListener(Utils.SharedPreferencesUtils.TYPE_LAYOUT_MANAGER_GRID));
        imageViewLinear.setOnClickListener(new ChangeLayoutManagerListener(Utils.SharedPreferencesUtils.TYPE_LAYOUT_MANAGER_LINEAR_VERTICAL));

    }

    /**
     * @param typeLayoutManager
     * @see Utils.SharedPreferencesUtils#TYPE_LAYOUT_MANAGER_LINEAR_VERTICAL
     * @see Utils.SharedPreferencesUtils#TYPE_LAYOUT_MANAGER_GRID
     */
    private void changeLayoutManager(@NonNull String typeLayoutManager) {
        int lastIndex = 0;

        if (recyclerSongs.getLayoutManager() instanceof LinearLayoutManager) {
            lastIndex = ((LinearLayoutManager) recyclerSongs.getLayoutManager()).findFirstVisibleItemPosition();

        } else if (recyclerSongs.getLayoutManager() instanceof GridLayoutManager) {
            lastIndex = ((GridLayoutManager) recyclerSongs.getLayoutManager()).findLastVisibleItemPosition();

        }

        RecyclerView.LayoutManager layoutManager = layoutManagerFromType(typeLayoutManager);
        mSongAdapter.setTypeLayoutManager(mTypeLayoutManager);
        recyclerSongs.setLayoutManager(layoutManager);
        if (isLayoutManagerLinearVertical()) {
            recyclerSongs.addItemDecoration(mDividerItemDecoration);
        } else {
            recyclerSongs.removeItemDecoration(mDividerItemDecoration);
        }
        recyclerSongs.scrollToPosition(lastIndex);
    }

    /**
     * @return true if is layout manager linear vertical
     * and false if is grid layout manager
     */
    private boolean isLayoutManagerLinearVertical() {
        return mTypeLayoutManager.equals(Utils.SharedPreferencesUtils.TYPE_LAYOUT_MANAGER_LINEAR_VERTICAL);
    }

    /**
     * When clicked after give the query search the query on the server (SoundCloud)
     */
    private void listenerButtonSearch() {
        imageViewSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchSong();
            }
        });
    }

    /**
     * SearchSong check if the query empty or ok
     */
    private void searchSong() {
        Utils.ActivityUtils.hideKeyboard(SearchActivity.this);
        String text = editTextSearch.getText().toString().trim();
        if (text.isEmpty()) {
            Utils.ActivityUtils.makeAndShowSnackBar(SearchActivity.this, getString(R.string.text_is_empty), Snackbar.LENGTH_SHORT);
        } else {
            searchSongsByQuery(text);
        }
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
     * @param query search on the server
     */
    private void searchSongsByQuery(String query) {
        mQuery = query;
        showSnackBarLoading();
        HttpServer.getInstance().searchSongs(query, MAX_LOADING_SONG).enqueue(new Callback<List<Song>>() {
            @Override
            public void onResponse(Call<List<Song>> call, Response<List<Song>> response) {
                if (response.isSuccessful()) {
                    editTextSearch.setText("");
                    List<Song> dataBodyList = response.body();
                    if (dataBodyList != null && !dataBodyList.isEmpty()) {
                        mSongAdapter.setQuery(mQuery);
                        mViewModelSongList.getSongsArrayList().clear();
                        mViewModelSongList.getSongsArrayList().addAll(dataBodyList);
                        mSongAdapter.notifyDataSetChanged();
                    } else {
                        Utils.ActivityUtils.makeAndShowSnackBar(SearchActivity.this, getString(R.string.not_found), Snackbar.LENGTH_SHORT);
                    }
                } else {
                    Utils.ActivityUtils.makeAndShowSnackBar(SearchActivity.this, getString(R.string.request_fail), Snackbar.LENGTH_SHORT);
                }
                if (mSnackBar != null) {
                    mSnackBar.dismiss();
                    mSnackBar = null;
                }
            }

            @Override
            public void onFailure(Call<List<Song>> call, Throwable t) {
                Utils.ActivityUtils.makeAndShowSnackBar(SearchActivity.this, getString(R.string.request_fail), Snackbar.LENGTH_SHORT);
                if (mSnackBar != null) {
                    mSnackBar.dismiss();
                    mSnackBar = null;
                }
            }
        });
    }


    /**
     * @see Utils.SharedPreferencesUtils#TYPE_LAYOUT_MANAGER_LINEAR_VERTICAL
     * @see Utils.SharedPreferencesUtils#TYPE_LAYOUT_MANAGER_GRID
     * @return the layout manager object
     */
    private RecyclerView.LayoutManager layoutManagerFromType(@NonNull String typeLayoutManager) {
        RecyclerView.LayoutManager layoutManager;
        switch (typeLayoutManager) {
            case Utils.SharedPreferencesUtils.TYPE_LAYOUT_MANAGER_GRID:
                layoutManager = new GridLayoutManager(this, 2, RecyclerView.VERTICAL, false);
                break;
            case Utils.SharedPreferencesUtils.TYPE_LAYOUT_MANAGER_LINEAR_VERTICAL:
            default:
                layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false) {
                    @Override
                    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
                        try {
                            super.onLayoutChildren(recycler, state);
                        } catch (Exception e) {
                            Log.e("probe", "meet a IOOBE in RecyclerView");
                        }
                    }

                    @Override
                    public boolean supportsPredictiveItemAnimations() {
                        return false;
                    }
                };
                break;
        }
        return layoutManager;
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
     * Call when the activity destroy save if this the first visit or not
     */
    @Override
    protected void onDestroy() {
        if (!isChangeConfig) {
            Utils.SharedPreferencesUtils.setFirstVisit(getApplicationContext(), false);
        }
        super.onDestroy();
    }

    /**
     * @see MusicActivity#KEY_SONGS_DATA
     * get new songs if music activity download
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
            ArrayList<Song> newArrayList = data.getExtras().getParcelableArrayList(MusicActivity.KEY_SONGS_DATA);
            int oldSize = mViewModelSongList.getSongsArrayList().size() - 1;
            for (int i = oldSize; i < newArrayList.size() - 1; i++) {
                mViewModelSongList.getSongsArrayList().add(newArrayList.get(i));
            }
            mSongAdapter.notifyItemRangeInserted(oldSize, newArrayList.size() - 1 - oldSize);
        }
    }

    /**
     * To avoid duplicate code, it is responsible for saving the value of layout manager preference
     */
    class ChangeLayoutManagerListener implements View.OnClickListener {
        private String typeLayoutManager;

        ChangeLayoutManagerListener(String typeLayoutManager) {
            this.typeLayoutManager = typeLayoutManager;
        }

        @Override
        public void onClick(View v) {
            mTypeLayoutManager = typeLayoutManager;
            Utils.SharedPreferencesUtils.saveCurrentLayoutType(getApplicationContext(), mTypeLayoutManager);
            changeLayoutManager(mTypeLayoutManager);
        }
    }
}
