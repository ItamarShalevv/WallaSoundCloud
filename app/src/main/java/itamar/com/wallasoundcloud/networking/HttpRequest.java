package itamar.com.wallasoundcloud.networking;

import java.util.List;

import itamar.com.wallasoundcloud.data.Song;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;


public interface HttpRequest {

    @GET("tracks?client_id=" + HttpServer.CLIENT_ID + "&format=json")
     Call<List<Song>>  searchSongs(@Query("q") String query, @Query("limit") Integer limitCount);

    @GET("tracks?client_id=" + HttpServer.CLIENT_ID + "&format=json")
     Call<List<Song>>  searchSongs(@Query("q") String query,@Query("offset") int offset, @Query("limit") int limitCount );



}