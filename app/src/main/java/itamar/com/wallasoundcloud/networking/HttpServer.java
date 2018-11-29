package itamar.com.wallasoundcloud.networking;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HttpServer {

    static final String CLIENT_ID = "80a4bb0faf1a266b43ed13de89656b60";
    private static final String BASE_URL = "http://api.soundcloud.com/";

    private static Retrofit createRetrofit() {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    public static HttpRequest getInstance() {
        return createRetrofit().create(HttpRequest.class);
    }


}
