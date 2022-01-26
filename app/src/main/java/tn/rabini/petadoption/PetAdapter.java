package tn.rabini.petadoption;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;

import java.util.Locale;

import tn.rabini.petadoption.models.Pet;

public class PetAdapter extends BaseAdapter<Pet, PetAdapter.PetViewHolder> {

    private final Context context;
    private final double lat, lng;

    public PetAdapter(@NonNull FirebaseRecyclerOptions<Pet> options, Context context, double lat, double lng) {
        super(options);
        this.context = context;
        this.lat = lat;
        this.lng = lng;
    }

    @Override
    protected void onBindViewHolder(@NonNull PetAdapter.PetViewHolder holder, int position, @NonNull Pet model) {
        glideHandle(context, model.getImage(), holder.petImage);
        double distance = getDistance(lat, lng, Double.parseDouble(model.getLat()), Double.parseDouble(model.getLng()));
        holder.itemView.setOnClickListener(view -> switchToDetails(context, model, distance, 0));
        holder.petRace.setText(model.getRace());
        holder.petName.setText(model.getName());
        holder.petLocation.setText(context.getString(R.string.distance, String.format(Locale.CANADA, "%.2f", distance / 1000)));
        if (isNew(model.getPublishedDate()) < 0)
            holder.petPublished.setText("");
        else
            holder.petPublished.setText(context.getString(R.string.new_word));
    }

    @NonNull
    @Override
    public PetAdapter.PetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_pet, parent, false);
        return new PetAdapter.PetViewHolder(v);
    }

    public static class PetViewHolder extends RecyclerView.ViewHolder {

        ImageView petImage;
        TextView petRace, petName, petLocation, petPublished;

        public PetViewHolder(@NonNull View itemView) {
            super(itemView);
            petImage = itemView.findViewById(R.id.petImage);
            petRace = itemView.findViewById(R.id.petRace);
            petName = itemView.findViewById(R.id.petName);
            petLocation = itemView.findViewById(R.id.petLocation);
            petPublished = itemView.findViewById(R.id.petPublished);
        }
    }
}