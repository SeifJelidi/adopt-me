package tn.rabini.petadoption;

import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import tn.rabini.petadoption.models.Pet;

public class BaseAdapter<T, H extends RecyclerView.ViewHolder> extends FirebaseRecyclerAdapter<T, H> {

    public BaseAdapter(@NonNull FirebaseRecyclerOptions<T> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull H holder, int position, @NonNull T model) {

    }

    @NonNull
    @Override
    public H onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    public void glideHandle(Context context, String imgUrl, ImageView imageView) {
        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(context);
        circularProgressDrawable.setStrokeWidth(5f);
        circularProgressDrawable.setCenterRadius(30f);
        circularProgressDrawable.start();
        Glide.with(context)
                .load(imgUrl)
                .fitCenter()
                .circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(circularProgressDrawable)
                .error(R.drawable.ic_baseline_error_24)
                .into(imageView);
    }

    public double getDistance(double lat1, double lng1, double lat2, double lng2) {
        LatLng loc1 = new LatLng(lat1, lng1);
        LatLng loc2 = new LatLng(lat2, lng2);
        return SphericalUtil.computeDistanceBetween(loc1, loc2);
    }

    public void switchToDetails(Context context, Pet model, double distance, int previousFragment) {
        Bundle flipBundle = new Bundle();
        flipBundle.putString("flip", "ToPetDetails");
        flipBundle.putInt("previous_fragment", previousFragment);
        flipBundle.putString("id", model.getId());
        flipBundle.putString("image", model.getImage());
        flipBundle.putString("name", model.getName());
        flipBundle.putString("race", model.getRace());
        flipBundle.putString("age", model.getAge());
        flipBundle.putString("gender", model.getGender());
        flipBundle.putString("description", model.getDescription());
        flipBundle.putString("distance", String.format(Locale.CANADA, "%.2f", distance / 1000)+"km away");
        flipBundle.putBoolean("ready", model.isReady());
        flipBundle.putString("owner", model.getOwner());
        flipBundle.putString("published_at", getDate(model.getPublishedDate()));
        ((AppCompatActivity) context).getSupportFragmentManager().setFragmentResult("flipResult", flipBundle);
    }

    public String getDate(long l1) {
        Date date = new Date();
        date.setTime(l1);
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy", Locale.CANADA);
        return formatter.format(date);
    }

    public long isNew(long l1) {

        // LAST WEEK FROM NOW
        Date now = new Date();
        Calendar lastWeek = Calendar.getInstance();
        lastWeek.setTime(now);
        lastWeek.add(Calendar.WEEK_OF_YEAR, -1);

        // PUBLISHED DATE
        Date pd = new Date();
        pd.setTime(l1);

        return TimeUnit.DAYS.convert(pd.getTime() - lastWeek.getTime().getTime(), TimeUnit.MILLISECONDS);
    }
}
