package tn.rabini.petadoption;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import tn.rabini.petadoption.models.Pet;
import tn.rabini.petadoption.models.User;

public class LikedPetAdapter extends BaseAdapter<String, LikedPetAdapter.LikedPetViewHolder> {

    private final Context context;
    private final FragmentActivity activity;
    private final double lat, lng;
    private final DatabaseReference mCurrentUserReference = FirebaseDatabase.getInstance()
            .getReference("Users")
            .child(FirebaseAuth.getInstance().getCurrentUser().getUid()),
            mLikedPetsReference = mCurrentUserReference.child("likedPets");
    private ValueEventListener mLikedPetsListener, mPetListener, mCurrentUserListener;
    private DatabaseReference mPetReference;

    public LikedPetAdapter(@NonNull FirebaseRecyclerOptions<String> options, Context context, FragmentActivity activity, double lat, double lng) {
        super(options);
        this.context = context;
        this.activity = activity;
        this.lat = lat;
        this.lng = lng;
    }

    @Override
    protected void onBindViewHolder(@NonNull LikedPetAdapter.LikedPetViewHolder holder, int position, @NonNull String model) {

        mPetReference = FirebaseDatabase.getInstance()
                .getReference("Pets")
                .child(model);
        mPetListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Pet pet = snapshot.getValue(Pet.class);
                if (pet != null) {
                    glideHandle(context, pet.getImage(), holder.petImage);
                    double distance = getDistance(lat, lng, Double.parseDouble(pet.getLat()), Double.parseDouble(pet.getLng()));
                    holder.petName.setText(pet.getName());
                    holder.itemView.setOnClickListener(view -> switchToDetails(context, pet, distance, 1));
                } else {
                    mLikedPetsListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                if (dataSnapshot.getValue(String.class).equals(model)) {
                                    dataSnapshot.getRef().removeValue();
                                    return;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    };
                    mLikedPetsReference.addListenerForSingleValueEvent(mLikedPetsListener);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        mPetReference.addListenerForSingleValueEvent(mPetListener);

        holder.unlikeButton.setOnClickListener(view -> {
            mCurrentUserListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        HashMap<String, String> likedPets;
                        if (user.getLikedPets() != null) {
                            likedPets = user.getLikedPets();
                            for (String k : likedPets.keySet()) {
                                String petID = likedPets.get(k);
                                if (petID != null) {
                                    if (petID.equals(model)) {
                                        likedPets.remove(k);
                                        break;
                                    }
                                }
                            }
                            user.setLikedPets(likedPets);
                            Map<String, Object> userUpdates = new HashMap<>();
                            userUpdates.put("likedPets", user.getLikedPets());
                            FirebaseDatabase.getInstance()
                                    .getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .updateChildren(userUpdates, (error, ref) -> {
                                        if (error != null) {
                                            Snackbar.make(activity.findViewById(R.id.coordinatorLayout), error.getMessage(), Snackbar.LENGTH_LONG)
                                                    .setAnchorView(activity.findViewById(R.id.bottom_navigation))
                                                    .show();
                                        } else {
                                            Snackbar.make(activity.findViewById(R.id.coordinatorLayout), "Pet removed from favorites!", Snackbar.LENGTH_LONG)
                                                    .setAnchorView(activity.findViewById(R.id.bottom_navigation))
                                                    .show();
                                        }
                                    });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
            mCurrentUserReference.addListenerForSingleValueEvent(mCurrentUserListener);
        });

    }

    @NonNull
    @Override
    public LikedPetAdapter.LikedPetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_liked_pet, parent, false);
        return new LikedPetAdapter.LikedPetViewHolder(v);
    }

    public void cleanupListeners() {
        if (mCurrentUserListener != null)
            mCurrentUserReference.removeEventListener(mCurrentUserListener);
        if (mPetListener != null)
            mPetReference.removeEventListener(mPetListener);
        if (mLikedPetsListener != null)
            mLikedPetsReference.removeEventListener(mLikedPetsListener);
    }

    public static class LikedPetViewHolder extends RecyclerView.ViewHolder {

        ImageView petImage;
        TextView petName;
        Button unlikeButton;

        public LikedPetViewHolder(@NonNull View itemView) {
            super(itemView);
            petImage = itemView.findViewById(R.id.petImage);
            petName = itemView.findViewById(R.id.petName);
            unlikeButton = itemView.findViewById(R.id.unlikeButton);
        }
    }
}
