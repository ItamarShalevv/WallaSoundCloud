package itamar.com.wallasoundcloud.adapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import itamar.com.wallasoundcloud.R;
import itamar.com.wallasoundcloud.activities.MusicActivity;
import itamar.com.wallasoundcloud.data.Song;
import itamar.com.wallasoundcloud.global.BounceInterpolator;
import itamar.com.wallasoundcloud.global.Utils;
import itamar.com.wallasoundcloud.networking.HttpServer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static itamar.com.wallasoundcloud.global.Utils.MAX_LOADING_SONG;

public class SongAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int REQUEST_CODE = 24;
    private Activity mActivity;
    private LayoutInflater mLayoutInflater;
    private List<Song> mSongList;
    private boolean needToLoadingMore;
    private int mMaxLoading;
    private String mQuery;
    private String mTypeLayoutManager;
    private Call<List<Song>> callListSong;
    private View.OnClickListener itemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View viewClicked) {

            int position = ((int) viewClicked.getTag());
            if (position == -1) {
                return;
            }
            Utils.AnimationUtils.setAnimationClicked(viewClicked, new BounceInterpolator(), 3000);
            ArrayList<Song> songArrayList = mSongList instanceof ArrayList ? ((ArrayList<Song>) mSongList) : new ArrayList<>(mSongList);

            Intent intent = new Intent(mActivity, MusicActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(MusicActivity.KEY_SONGS_DATA, songArrayList);
            bundle.putInt(MusicActivity.KEY_POSITION, position);
            bundle.putInt(MusicActivity.KEY_MAX_LOADING, mMaxLoading);
            bundle.putBoolean(MusicActivity.KEY_NEED_LOADING_MORE, needToLoadingMore);
            bundle.putString(MusicActivity.KEY_QUERY, mQuery);
            if (callListSong != null) {
                callListSong.cancel();
            }
            intent.putExtras(bundle);
            mActivity.startActivityForResult(intent, REQUEST_CODE);
        }
    };

    public SongAdapter(Activity activity, List<Song> songList, @NonNull String typeLayoutManager) {
        this.mActivity = activity;
        this.mLayoutInflater = LayoutInflater.from(mActivity);
        this.mSongList = songList;
        this.mTypeLayoutManager = typeLayoutManager;
        needToLoadingMore = true;
        boolean isFirstVisit = Utils.SharedPreferencesUtils.isFirstVisit(activity);
        mMaxLoading = isFirstVisit ? 20 : 100;


    }

    @Override
    public int getItemViewType(int position) {
        if (mSongList.size() == position && needToLoadingMore && mSongList.size() < mMaxLoading) {
            return -1;
        }
        if (mTypeLayoutManager.equals(Utils.SharedPreferencesUtils.TYPE_LAYOUT_MANAGER_GRID)) {
            return 0;
        }
        int viewType;
        switch (mSongList.get(position).getFormat()) {
            case Utils.TYPE_RAW:
                viewType = 1;
                break;
            case Utils.TYPE_MP3:
                viewType = 2;
                break;
            case Utils.TYPE_WAV:
                viewType = 3;
                break;
            default:
                viewType = 4;
        }
        return viewType;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        RecyclerView.ViewHolder holder;
        switch (viewType) {
            case -1:
                itemView = new ProgressBar(mActivity);
                ViewGroup.LayoutParams param = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100);
                itemView.setLayoutParams(param);
                holder = new RecyclerView.ViewHolder(itemView) {
                };
                break;
            case 0:
                itemView = mLayoutInflater.inflate(R.layout.row_image_only_grid, parent, false);
                holder = new HolderImageOnly(itemView);
                break;
            case 1:
                itemView = mLayoutInflater.inflate(R.layout.row_only_text, parent, false);
                holder = new HolderTextOnly(itemView);
                break;
            case 2:
                itemView = mLayoutInflater.inflate(R.layout.row_small_image_and_text, parent, false);
                holder = new HolderTextWithImage(itemView);
                break;
            case 3:
                itemView = mLayoutInflater.inflate(R.layout.row_big_image_and_text_on, parent, false);
                holder = new HolderTextWithImage(itemView);
                break;
            case 4:
            default:
                itemView = mLayoutInflater.inflate(R.layout.row_big_image_and_text_under, parent, false);
                holder = new HolderTextWithImage(itemView);
                break;
        }

        itemView.setOnClickListener(itemClickListener);
        return holder;
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == -1) {
            loadMoreIfNeed(holder);
            holder.itemView.setTag(-1);
            return;
        }
        Song currentSong = mSongList.get(holder.getAdapterPosition());
        holder.itemView.setTag(holder.getAdapterPosition());
        switch (holder.getItemViewType()) {
            case 0:
                showImageDetails(currentSong, ((HolderImageOnly) holder).imageViewSong);
                break;
            case 1:
            case 2:
            case 3:
            case 4:
                if (holder.getItemViewType() != 1) {
                    showImageDetails(currentSong, ((HolderTextWithImage) holder).imageViewSong);
                }
                ((HolderTextOnly) holder).textViewSongName.setText(currentSong.getName());
        }
    }

    private void loadMoreIfNeed(RecyclerView.ViewHolder holder) {
        if (mSongList.size() < mMaxLoading && needToLoadingMore) {
            holder.itemView.setVisibility(View.VISIBLE);
            if (callListSong == null) {
                callListSong = HttpServer.getInstance().searchSongs(mQuery, mSongList.size(), MAX_LOADING_SONG);
                callListSong.enqueue(new Callback<List<Song>>() {
                    @Override
                    public void onResponse( Call<List<Song>> call, Response<List<Song>> response) {
                        if (response.isSuccessful()) {
                            List<Song> dataBodyList = response.body();
                            if (dataBodyList != null) {
                                if (!dataBodyList.isEmpty()) {
                                    List<Song> songList = new ArrayList<>(mSongList);
                                    songList.addAll(dataBodyList);
                                    mSongList.clear();
                                    notifyDataSetChanged();
                                    mSongList.addAll(songList);
                                    notifyItemRangeInserted(0, mSongList.size());
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
            holder.itemView.setVisibility(View.GONE);
            needToLoadingMore = false;
        }
    }

    public void setQuery(String query) {
        this.mQuery = query;
    }

    private void showImageDetails(Song song, ImageView imageView) {
        if (song.getImageUrl() == null || song.getImageUrl().isEmpty()) {
            imageView.setImageResource(Utils.PLACE_HOLDER_IMAGE_SONG);
        } else {
            String urlImage = song.getImageUrl();
            Picasso.get().load(urlImage).into(imageView);
        }
    }

    @Override
    public int getItemCount() {
        int itemCount = mSongList.size();
        if (needToLoadingMore && mSongList.size() > 0) {
            itemCount++;
        }
        return itemCount;
    }

    @Override
    public long getItemId(int position) {
        return mSongList.get(position).hashCode();
    }

    public void setTypeLayoutManager(@NonNull String typeLayoutManager) {
        this.mTypeLayoutManager = typeLayoutManager;
    }


    static class HolderTextOnly extends RecyclerView.ViewHolder {
        final TextView textViewSongName;

        HolderTextOnly(@NonNull View itemView) {
            super(itemView);
            textViewSongName = itemView.findViewById(R.id.text_view_song_name);
        }
    }

    static class HolderTextWithImage extends HolderTextOnly {
        final ImageView imageViewSong;

        HolderTextWithImage(@NonNull View itemView) {
            super(itemView);
            imageViewSong = itemView.findViewById(R.id.image_view_song);
        }
    }

    static class HolderImageOnly extends RecyclerView.ViewHolder {
        final ImageView imageViewSong;

        HolderImageOnly(@NonNull View itemView) {
            super(itemView);
            imageViewSong = itemView.findViewById(R.id.image_view_song);
        }
    }
}
