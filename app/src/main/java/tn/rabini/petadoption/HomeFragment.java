package tn.rabini.petadoption;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.common.ChangeEventType;
import com.firebase.ui.database.ChangeEventListener;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.ObservableSnapshotArray;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tiper.MaterialSpinner;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import tn.rabini.petadoption.models.Pet;

public class HomeFragment extends Fragment {

    private final DatabaseReference mPetsReference = FirebaseDatabase.getInstance().getReference().child("Pets");
    private final FirebaseRecyclerOptions<Pet> adapterOptions = new FirebaseRecyclerOptions.Builder<Pet>()
            .setQuery(mPetsReference, Pet.class)
            .build();
    private final String currentOption = "race";
    private ObservableSnapshotArray<Pet> petsArray = adapterOptions.getSnapshots();
    private RecyclerView petList;
    private PetAdapter petAdapter;
    private CircularProgressIndicator spinner;
    private String optionSelected = "race";
    private String searchQuery = "";
    private String petSelected = "All";
    private boolean distanceAdded = false;
    private LinearLayout searchBar;
    private List<DataSnapshot> list = new ArrayList<>();
    private Double lat, lng;
    private final BroadcastReceiver cordReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle result = intent.getExtras();
            if (result != null) {
                lat = result.getDouble("lat");
                lng = result.getDouble("lng");
                if (getActivity() != null) {
                    petAdapter = new PetAdapter(adapterOptions, requireContext(), lat, lng);
                    petList.swapAdapter(petAdapter, true);
                    petAdapter.startListening();
                }
            }
        }
    };

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            lat = getArguments().getDouble("lat");
            lng = getArguments().getDouble("lng");
        }
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(cordReceiver,
                new IntentFilter("my-cord"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        searchBar = v.findViewById(R.id.searchBar);
        SearchView searchView = v.findViewById(R.id.searchView);
        MaterialSpinner searchOptions = v.findViewById(R.id.searchOptions);
        MaterialSpinner petOptions = v.findViewById(R.id.petOptions);
        spinner = v.findViewById(R.id.spinner);
        petList = v.findViewById(R.id.petList);

        ArrayAdapter<CharSequence> searchOptionsAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.search_options, android.R.layout.simple_spinner_item);
        searchOptionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        searchOptions.setAdapter(searchOptionsAdapter);

        searchOptions.setSelection(0);

        searchOptions.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(@NonNull MaterialSpinner materialSpinner, View view, int i, long l) {
                optionSelected = materialSpinner.getSelectedItem().toString();
                updateSearch(false);
            }

            @Override
            public void onNothingSelected(@NonNull MaterialSpinner materialSpinner) {
                optionSelected = "race";
            }
        });

        ArrayAdapter<CharSequence> petOptionsAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.pet_options, android.R.layout.simple_spinner_item);
        petOptionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        petOptions.setAdapter(petOptionsAdapter);

        petOptions.setSelection(0);

        petOptions.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(@NonNull MaterialSpinner materialSpinner, View view, int i, long l) {
                petSelected = materialSpinner.getSelectedItem().toString();
                updateSearch(false);
            }

            @Override
            public void onNothingSelected(@NonNull MaterialSpinner materialSpinner) {
                petSelected = "Dog";
            }
        });

        petList.setLayoutManager(new LinearLayoutManager(requireContext()));
        petAdapter = new PetAdapter(adapterOptions, requireContext(), lat, lng) {
            @Override
            public void onDataChanged() {
                super.onDataChanged();
                spinner.setVisibility(View.GONE);
                petList.setVisibility(View.VISIBLE);
                searchBar.setVisibility(View.VISIBLE);
            }
        };
        petList.setAdapter(petAdapter);

        Button addPetButton = v.findViewById(R.id.addPetButton);
        addPetButton.setOnClickListener(view -> {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                switchTo("ToLogin");
            } else if (!FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
                MaterialAlertDialogBuilder warningBuilder = new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.warning_verify_email)).setPositiveButton("Done", (dialogInterface, i) -> {
                            FirebaseAuth.getInstance().signOut();
                            switchTo("ToLogin");
                            dialogInterface.dismiss();
                        });
                warningBuilder.show();
            } else {
                switchTo("ToAddPet");
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (!optionSelected.equals("distance")) {
                    searchQuery = s;
                    updateSearch(true);
                }
                return false;
            }
        });

        return v;
    }

    private String capitalize(String s) {
        return s.length() < 1 ? s : s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public ObservableSnapshotArray<Pet> initArray(List<DataSnapshot> l) {
        SnapshotParser<Pet> snapshotParser = snapshot -> Objects.requireNonNull(snapshot.getValue(Pet.class));
        return new ObservableSnapshotArray<Pet>(snapshotParser) {
            @NonNull
            @Override
            protected List<DataSnapshot> getSnapshots() {
                return l;
            }
        };
    }

    public void sortByDistance() {
        if (!distanceAdded) {
            list = new ArrayList<>();
            for (int i = 0; i < petsArray.size(); i++)
                list.add(petsArray.getSnapshot(i));
            Collections.sort(list, Pet.distanceComparator(lat, lng));
//            petsArray = initArray();
            distanceAdded = true;
        }
        FirebaseRecyclerOptions<Pet> newOptions = new FirebaseRecyclerOptions.Builder<Pet>()
                .setSnapshotArray(petsArray)
                .build();
        petAdapter.updateOptions(newOptions);
//        petsArray = initArray();
    }

    public void sortBy() {
        new FirebaseRecyclerOptions.Builder<Pet>()
                .setQuery(FirebaseDatabase.getInstance().getReference().child("Pets"), Pet.class)
                .build().getSnapshots().addChangeEventListener(new ChangeEventListener() {
            private final List<DataSnapshot> aList = new ArrayList<>();

            @Override
            public void onChildChanged(@NonNull @NotNull ChangeEventType type, @NonNull @NotNull DataSnapshot snapshot, int newIndex, int oldIndex) {
                aList.add(snapshot);
            }

            @Override
            public void onDataChanged() {
                Log.v("aaalisssssssssssssst000", String.valueOf(aList.size()));
                petsArray = initArray(aList);
                Log.v("arrrrrrrrrrrrayyyy000", String.valueOf(petsArray.size()));
                List<DataSnapshot> auxList = new ArrayList<>();
                Log.v("arrrrrrrrrrrrayyyy111", String.valueOf(petsArray.size()));
                for (int i = 0; i < petsArray.size(); i++) {
                    DataSnapshot petSnap = petsArray.getSnapshot(i);
                    Pet pet = petSnap.getValue(Pet.class);
                    if (pet != null && (pet.getType().equals(petSelected) || petSelected.equals("All")))
                        auxList.add(petSnap);
                    Log.v("peeeeeeeeeeeeeeetttt000", pet.toString());
                }
                Collections.sort(auxList, optionSelected.equals("distance") ? Pet.distanceComparator(lat, lng)
                        : optionSelected.equals("race") ? Pet.raceComparator()
                        : Pet.nameComparator());
                Log.v("lisssssssssssssst000", String.valueOf(auxList.size()));
                list.clear();
                list.addAll(auxList);
                Log.v("lisssssssssssssst111", String.valueOf(auxList.size()));
                petsArray = initArray(list);
                Log.v("arrrrrrrrrrrrayyyy222", String.valueOf(petsArray.size()));
                FirebaseRecyclerOptions<Pet> newOptions = new FirebaseRecyclerOptions.Builder<Pet>()
                        .setSnapshotArray(petsArray)
                        .build();
//                petAdapter.updateOptions(newOptions);

                petAdapter = new PetAdapter(newOptions, requireContext(), lat, lng);
                petList.swapAdapter(petAdapter, true);
                petAdapter.startListening();

                Log.v("arrrrrrrrrrrrayyyy333", String.valueOf(petsArray.size()));
                //        petsArray = initArray();
            }

            @Override
            public void onError(@NonNull @NotNull DatabaseError databaseError) {

            }
        });
    }

    private void updateSearch(boolean typing) {
        sortBy();
//        if (optionSelected.equals("distance")) {
//            if (!optionSelected.equals(currentOption)) {
//                sortByDistance();
//                currentOption = optionSelected;
//            }
//        } else {
//            if (!optionSelected.equals(currentOption) || typing) {
//                Query newRef = mPetsReference.orderByChild(optionSelected)
//                        .startAt(capitalize(searchQuery)).endAt(capitalize(searchQuery) + "\uf8ff");
//                FirebaseRecyclerOptions<Pet> newOptions = new FirebaseRecyclerOptions.Builder<Pet>()
//                        .setQuery(newRef, Pet.class)
//                        .build();
//                petAdapter = new PetAdapter(newOptions, requireContext(), lat, lng);
//                petList.swapAdapter(petAdapter, true);
//                petAdapter.startListening();
//                currentOption = optionSelected;
//            }
//        }
    }

    @Override
    public void onStart() {
        super.onStart();
        petAdapter.startListening();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        petAdapter.stopListening();
    }

    private void switchTo(String fragmentName) {
        Bundle flipBundle = new Bundle();
        flipBundle.putString("flip", fragmentName);
        getParentFragmentManager().setFragmentResult("flipResult", flipBundle);
    }

}