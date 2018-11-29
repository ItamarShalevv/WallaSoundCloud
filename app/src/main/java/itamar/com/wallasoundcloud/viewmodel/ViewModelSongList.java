package itamar.com.wallasoundcloud.viewmodel;

import androidx.lifecycle.ViewModel;
import itamar.com.wallasoundcloud.data.Song;

import java.util.ArrayList;

public class ViewModelSongList extends ViewModel {

    private final ArrayList<Song> songsArrayList;

    public ViewModelSongList() {
        this.songsArrayList = new ArrayList<>();
    }

    public ArrayList<Song> getSongsArrayList() {
        return songsArrayList;
    }

    @Override
    protected void onCleared() {
        songsArrayList.clear();
        super.onCleared();
    }
}
