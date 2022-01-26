package tn.rabini.petadoption;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Objects;

import tn.rabini.petadoption.models.Pet;

public class MyPetAdapter extends BaseAdapter<String, MyPetAdapter.MyPetViewHolder> {

    private final Context context;
    private final FragmentActivity activity;
    private final boolean isUser;
    private final double lat, lng;
    private DatabaseReference mPetReference, mPetsReference;
    private ValueEventListener mPetListener, mPetsListener;

    public MyPetAdapter(@NonNull FirebaseRecyclerOptions<String> options, Context context, FragmentActivity activity, boolean isUser, double lat, double lng) {
        super(options);
        this.context = context;
        this.activity = activity;
        this.isUser = isUser;
        this.lat = lat;
        this.lng = lng;
    }

    @Override
    protected void onBindViewHolder(@NonNull MyPetAdapter.MyPetViewHolder holder, int position, @NonNull String model) {
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
                    holder.itemView.setOnClickListener(view -> switchToDetails(context, pet, distance, 2));
                    holder.petName.setText(pet.getName());
                    if (isUser) {
                        holder.editButton.setVisibility(View.VISIBLE);
                        holder.deleteButton.setVisibility(View.VISIBLE);

                        holder.editButton.setOnClickListener(view -> {
                            Bundle flipBundle = new Bundle();
                            flipBundle.putString("flip", "ToEditPet");
                            flipBundle.putString("id", pet.getId());
                            flipBundle.putString("image", pet.getImage());
                            flipBundle.putString("name", pet.getName());
                            flipBundle.putString("race", pet.getRace());
                            flipBundle.putString("age", pet.getAge());
                            flipBundle.putString("gender", pet.getGender());
                            flipBundle.putString("type", pet.getType());
                            flipBundle.putString("description", pet.getDescription());
                            flipBundle.putBoolean("ready", pet.isReady());
                            flipBundle.putString("lat", pet.getLat());
                            flipBundle.putString("lng", pet.getLng());
                            ((AppCompatActivity) context).getSupportFragmentManager().setFragmentResult("flipResult", flipBundle);
                        });

                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                        holder.deleteButton.setOnClickListener(view -> {
                            MaterialAlertDialogBuilder deleteBuilder = new MaterialAlertDialogBuilder(context)
                                    .setTitle(context.getString(R.string.delete_dialog))
                                    .setPositiveButton("Yes", (dialogInterface, i) ->
                                            FirebaseStorage.getInstance()
                                                    .getReferenceFromUrl(pet.getImage())
                                                    .delete()
                                                    .addOnSuccessListener(aVoid -> {
                                                        if (currentUser != null) {
                                                            mPetsListener = new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                                                        String petID = dataSnapshot.getValue(String.class);
                                                                        if (petID != null) {
                                                                            if (petID.equals(model)) {
                                                                                dataSnapshot.getRef().removeValue()
                                                                                        .addOnSuccessListener(aVoid -> FirebaseDatabase
                                                                                                .getInstance()
                                                                                                .getReference("Pets")
                                                                                                .child(petID)
                                                                                                .removeValue()
                                                                                                .addOnSuccessListener(aVoid1 -> FirebaseDatabase.getInstance()
                                                                                                        .getReference("Pets")
                                                                                                        .child(model)
                                                                                                        .removeValue()
                                                                                                        .addOnSuccessListener(aVoid2 -> Snackbar.make(activity.findViewById(R.id.coordinatorLayout), "Pet deleted successfully!", Snackbar.LENGTH_LONG)
                                                                                                                .setAnchorView(activity.findViewById(R.id.bottom_navigation))
                                                                                                                .show())
                                                                                                        .addOnFailureListener(e -> Snackbar.make(activity.findViewById(R.id.coordinatorLayout), Objects.requireNonNull(e.getMessage()), Snackbar.LENGTH_LONG)
                                                                                                                .setAnchorView(activity.findViewById(R.id.bottom_navigation))
                                                                                                                .show()))
                                                                                                .addOnFailureListener(e -> Snackbar.make(activity.findViewById(R.id.coordinatorLayout), Objects.requireNonNull(e.getMessage()), Snackbar.LENGTH_LONG)
                                                                                                        .setAnchorView(activity.findViewById(R.id.bottom_navigation))
                                                                                                        .show()))
                                                                                        .addOnFailureListener(e -> Snackbar.make(activity.findViewById(R.id.coordinatorLayout), Objects.requireNonNull(e.getMessage()), Snackbar.LENGTH_LONG)
                                                                                                .setAnchorView(activity.findViewById(R.id.bottom_navigation))
                                                                                                .show());
                                                                            }
                                                                        }
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError error) {
                                                                    Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
                                                                }
                                                            };
                                                            mPetsReference = FirebaseDatabase.getInstance()
                                                                    .getReference("Users")
                                                                    .child(currentUser.getUid())
                                                                    .child("pets");
                                                            mPetsReference.addListenerForSingleValueEvent(mPetsListener);
                                                        }
                                                    }))
                                    .setNegativeButton("No", (dialog, which) -> dialog.cancel());
                            deleteBuilder.show();
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
        mPetReference.addListenerForSingleValueEvent(mPetListener);

    }

    @NonNull
    @Override
    public MyPetAdapter.MyPetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_my_pet, parent, false);
        return new MyPetAdapter.MyPetViewHolder(v);
    }

    public void cleanupListeners() {
        if (mPetListener != null)
            mPetReference.removeEventListener(mPetListener);
        if (mPetsListener != null)
            mPetsReference.removeEventListener(mPetsListener);
    }

    public static class MyPetViewHolder extends RecyclerView.ViewHolder {

        ImageView petImage;
        TextView petName;
        Button editButton, deleteButton;

        public MyPetViewHolder(@NonNull View itemView) {
            super(itemView);
            petImage = itemView.findViewById(R.id.petImage);
            petName = itemView.findViewById(R.id.petName);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
