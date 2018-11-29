package itamar.com.wallasoundcloud.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import androidx.annotation.NonNull;


public class Song implements Parcelable {

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };
    @SerializedName("title")
    private final String name;
    @SerializedName("artwork_url")
    private final String imageUrl;
    @SerializedName("stream_url")
    private final String streamUrl;
    @SerializedName("original_format")
    private final String format;

    private Song(Parcel in) {
        name = in.readString();
        imageUrl = in.readString();
        streamUrl = in.readString();
        format = in.readString();
    }


    public String getName() {
        return name;
    }


    public String getStreamUrl() {
        return streamUrl;
    }


    public String getFormat() {
        return format;
    }


    public String getImageUrl() {
        return imageUrl;
    }


    @Override
    public String toString() {
        return format;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(imageUrl);
        dest.writeString(streamUrl);
        dest.writeString(format);
    }
}
