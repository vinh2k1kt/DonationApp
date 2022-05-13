package ie.app.api;

import java.util.List;

import ie.app.models.Donation;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

    @GET("Donation/")
    Call<List<Donation>> getDonation(@Query("id") int id);

    @DELETE("Donation/{id}")
    Call<Donation> deleteDonation(@Path("id") int id);

    @GET("Donation")
    Call<List<Donation>> getAnswers();

    @POST("Donation")
    Call<Donation> addDonation(@Body Donation donation);
}
