package tn.rabini.petadoption;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import tn.rabini.petadoption.models.User;


public class PetDetailsFragment extends Fragment {

    private String id, name, race, age, gender, description, distance, image, owner, contactNumber, publishedDate;
    private int previousFragment;
    private ToggleButton likeButton;
    private TextView petOwner, petContact;
    private FirebaseAuth mAuth;
    private boolean ready;
    private CircularProgressIndicator spinner;
    private RelativeLayout allLayouts;
    private DatabaseReference mUserReference, mLikedPetsReference, mCurrentUserReference;
    private ValueEventListener mUserListener, mLikedPetsListener, mCurrentUserListener;

    public PetDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            id = getArguments().getString("id");
            name = getArguments().getString("name");
            race = getArguments().getString("race");
            age = getArguments().getString("age");
            gender = getArguments().getString("gender");
            description = getArguments().getString("description");
            distance = getArguments().getString("distance");
            image = getArguments().getString("image");
            ready = getArguments().getBoolean("ready");
            owner = getArguments().getString("owner");
            publishedDate = getArguments().getString("published_at");
            previousFragment = getArguments().getInt("previous_fragment");
            mUserReference = FirebaseDatabase.getInstance().getReference("Users").child(owner);
        }
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            mCurrentUserReference = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getCurrentUser().getUid());
            mLikedPetsReference = mCurrentUserReference.child("likedPets");
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_pet_details, container, false);
        spinner = v.findViewById(R.id.spinner);
        allLayouts = v.findViewById(R.id.allLayouts);
        ScrollView parentScroll = v.findViewById(R.id.parentScroll);
        ImageView arrowBack = v.findViewById(R.id.arrowBack);
        ImageView petImage = v.findViewById(R.id.petImage);
        likeButton = v.findViewById(R.id.likeButton);
        TextView petName = v.findViewById(R.id.petName);
        TextView petGender = v.findViewById(R.id.petGender);
        TextView petRace = v.findViewById(R.id.petRace);
        TextView petAge = v.findViewById(R.id.petAge);
        TextView petDescription = v.findViewById(R.id.petDescription);
        TextView petLocation = v.findViewById(R.id.petLocation);
        TextView petReady = v.findViewById(R.id.petReady);
        TextView publishedAt = v.findViewById(R.id.publishedAt);
        petOwner = v.findViewById(R.id.petOwner);
        petContact = v.findViewById(R.id.petContact);
        petDescription.setMovementMethod(new ScrollingMovementMethod());

        arrowBack.setOnClickListener(view -> {
            switch (previousFragment) {
                case 0:
                    switchTo("ToHome");
                    break;
                case 1:
                    switchTo("ToFavorites");
                    break;
                case 2:
                    switchTo("ToProfile");
                    break;
                default:
                    break;
            }
        });

        // IMAGE FULLSCREEN ON CLICK
        petImage.setOnClickListener(v1 -> {
            AlertDialog fullscreenBuilder = new MaterialAlertDialogBuilder(requireContext())
                    .setNegativeButton("Close", (dialogInterface, i) -> dialogInterface.dismiss())
                    .setView(R.layout.fullscreen_image)
                    .create();

            fullscreenBuilder.setOnShowListener(dialogInterface -> {
                ImageView fullscreenImage = ((AlertDialog) dialogInterface).findViewById(R.id.fullscreenImage);
                assert fullscreenImage != null;
                Glide.with(requireContext())
                        .load(image)
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .error(R.drawable.ic_baseline_error_24)
                        .into(fullscreenImage);
            });
            fullscreenBuilder.show();
        });

        petName.setText(name);
        petRace.setText(race);
        petLocation.setText(distance);
        petReady.setText(ready ? "Available" : "Not available at the moment");
        publishedAt.setText(getString(R.string.published_at, publishedDate));

        // SET AVAILABLE OR NOT IMAGE
        petReady.setCompoundDrawablesWithIntrinsicBounds(ready ? ContextCompat.getDrawable(requireContext(), R.drawable.baseline_check_circle_24)
                : ContextCompat.getDrawable(requireContext(), R.drawable.baseline_report_gmailerrorred_24), null, null, null);

        // CALCULATE DATE
        String[] dates = age.split("-");
        LocalDate l = LocalDate.of(Integer.parseInt(dates[0]), Integer.parseInt(dates[1]), Integer.parseInt(dates[2])); //specify year, month, date directly
        LocalDate now = LocalDate.now();
        Period diff = Period.between(l, now);

        String years = diff.getYears() > 0 ? diff.getYears() + " years, " : "";
        String days = diff.getDays() > 0 ? diff.getDays() + " days" : "";
        String months = diff.getMonths() > 0 ? days.equals("") ? diff.getMonths() + " months" : diff.getMonths() + " months, " : "";
        String fullAge = years + months + days;
        petAge.setText(fullAge);

        petGender.setText(getString(R.string.gender_detail, gender));
        petDescription.setText(description);


        // GET OWNER INFO
        mUserListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    if (mAuth.getCurrentUser() == null) {
                        petOwner.setText(getString(R.string.login_to_show));
                        petContact.setText(getString(R.string.login_to_show));
                    } else if (!mAuth.getCurrentUser().isEmailVerified()) {
                        petOwner.setText(getString(R.string.verify_to_show));
                        petContact.setText(getString(R.string.verify_to_show));
                    } else {
                        petOwner.setText(user.getUsername());
                        petContact.setText(user.getPhone());
                    }
                    contactNumber = user.getPhone();

                    Glide.with(requireContext())
                            .load(image)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    spinner.setVisibility(View.GONE);
                                    allLayouts.setVisibility(View.VISIBLE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    spinner.setVisibility(View.GONE);
                                    allLayouts.setVisibility(View.VISIBLE);
                                    return false;
                                }
                            })
                            .fitCenter()
                            .transform(new CenterCrop(), new RoundedCorners(30))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .error(R.drawable.ic_baseline_error_24)
                            .into(petImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        mUserReference.addListenerForSingleValueEvent(mUserListener);

        petOwner.setOnClickListener(view -> {
            Bundle flipBundle = new Bundle();
            if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().isEmailVerified()) {
                flipBundle.putString("flip", "ToProfile");
                flipBundle.putString("userID", owner);
                getParentFragmentManager().setFragmentResult("flipResult", flipBundle);
            } else if (mAuth.getCurrentUser() == null) {
                flipBundle.putString("flip", "ToLogin");
                getParentFragmentManager().setFragmentResult("flipResult", flipBundle);
            }
        });

        petContact.setOnClickListener(view -> {
            if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().isEmailVerified()) {
                Intent dialIntent = new Intent();
                dialIntent.setAction(Intent.ACTION_DIAL);
                dialIntent.setData(Uri.parse("tel:" + contactNumber));
                startActivity(dialIntent);
            }
        });

        // CHECK IF PET ALREADY LIKED OR NOT
        if (mAuth.getCurrentUser() == null) {
            likeButton.setBackgroundResource(R.drawable.ic_baseline_favorite_border_24);
            likeButton.setVisibility(View.VISIBLE);
        } else {
            mLikedPetsListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String petID = dataSnapshot.getValue(String.class);
                        if (petID != null) {
                            if (petID.equals(id)) {
                                likeButton.setBackgroundResource
                                        (R.drawable.ic_baseline_favorite_24);
                                likeButton.setVisibility(View.VISIBLE);
                                return;
                            }
                        }
                    }
                    likeButton.setBackgroundResource
                            (R.drawable.ic_baseline_favorite_border_24);
                    likeButton.setVisibility(View.VISIBLE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
            mLikedPetsReference.addListenerForSingleValueEvent(mLikedPetsListener);
        }

        // ON LIKE BUTTON CLICK
        likeButton.setOnClickListener(view -> {
            if (mAuth.getCurrentUser() == null) {
                Bundle flipBundle = new Bundle();
                flipBundle.putString("flip", "ToLogin");
                getParentFragmentManager().setFragmentResult("flipResult", flipBundle);
                return;
            }
            view.setEnabled(false);
            if (view.getBackground().getConstantState().equals(Objects.requireNonNull(ResourcesCompat
                    .getDrawable(getResources(), R.drawable.ic_baseline_favorite_border_24
                            , null)).getConstantState())) {
                mCurrentUserListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            HashMap<String, String> likedPets = new HashMap<>();
                            if (user.getLikedPets() != null) {
                                likedPets = user.getLikedPets();
                            }
                            likedPets.put(UUID.randomUUID().toString(), id);
                            user.setLikedPets(likedPets);
                            Map<String, Object> userUpdates = new HashMap<>();
                            userUpdates.put("likedPets", user.getLikedPets());
                            FirebaseDatabase.getInstance()
                                    .getReference("Users")
                                    .child(mAuth.getCurrentUser().getUid())
                                    .updateChildren(userUpdates, (error, ref) -> {
                                        if (error != null) {
                                            Snackbar.make(requireActivity().findViewById(R.id.coordinatorLayout), error.getMessage(), Snackbar.LENGTH_LONG)
                                                    .setAnchorView(requireActivity().findViewById(R.id.bottom_navigation))
                                                    .show();
                                        } else {
                                            Snackbar.make(requireActivity().findViewById(R.id.coordinatorLayout), "Pet added to favorites!", Snackbar.LENGTH_SHORT)
                                                    .setAnchorView(requireActivity().findViewById(R.id.bottom_navigation))
                                                    .show();
                                            view.setBackgroundResource(R.drawable.ic_baseline_favorite_24);
                                        }
                                        view.setEnabled(true);
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        view.setEnabled(true);
                    }
                };
            } else {
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
                                        if (petID.equals(id)) {
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
                                        .child(mAuth.getCurrentUser().getUid())
                                        .updateChildren(userUpdates, (error, ref) -> {
                                            if (error != null) {
                                                Snackbar.make(requireActivity().findViewById(R.id.coordinatorLayout), error.getMessage(), Snackbar.LENGTH_LONG)
                                                        .setAnchorView(requireActivity().findViewById(R.id.bottom_navigation))
                                                        .show();
                                            } else {
                                                Snackbar.make(requireActivity().findViewById(R.id.coordinatorLayout), "Pet removed from favorites!", Snackbar.LENGTH_SHORT)
                                                        .setAnchorView(requireActivity().findViewById(R.id.bottom_navigation))
                                                        .show();
                                                view.setBackgroundResource(R.drawable.ic_baseline_favorite_border_24);
                                            }
                                            view.setEnabled(true);
                                        });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        view.setEnabled(true);
                    }
                };
            }
            mCurrentUserReference.addListenerForSingleValueEvent(mCurrentUserListener);
        });

        parentScroll.setOnTouchListener((view, motionEvent) -> {
            petDescription.getParent().requestDisallowInterceptTouchEvent(false);
            view.performClick();
            return false;
        });

        petDescription.setOnTouchListener((view, motionEvent) -> {
            petDescription.getParent().requestDisallowInterceptTouchEvent(true);
            view.performClick();
            return false;
        });

        return v;
    }

    private void switchTo(String fragmentName) {
        Bundle flipBundle = new Bundle();
        flipBundle.putString("flip", fragmentName);
        if (fragmentName.equals("ToProfile"))
            flipBundle.putString("userID", owner);
        getParentFragmentManager().setFragmentResult("flipResult", flipBundle);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mUserListener != null)
            mUserReference.removeEventListener(mUserListener);
        if (mLikedPetsListener != null)
            mLikedPetsReference.removeEventListener(mLikedPetsListener);
        if (mCurrentUserListener != null)
            mCurrentUserReference.removeEventListener(mCurrentUserListener);
    }
}