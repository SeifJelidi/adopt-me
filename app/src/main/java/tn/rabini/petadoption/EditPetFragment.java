package tn.rabini.petadoption;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.Task;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.CompositeDateValidator;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.tiper.MaterialSpinner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

public class EditPetFragment extends Fragment {

    ActivityResultLauncher<Intent> startImageIntent;
    private TextInputLayout nameLayout, descriptionLayout, raceLayout;
    private TextInputEditText nameInput, descriptionInput, raceInput;
    private String nameValue, raceValue, ageValue, descriptionValue, latValue, lngValue, imageValue,
            genderValue, typeValue, petId;
    private boolean readyValue;
    private TextView errorView;
    private Button submitButton;
    private SwitchMaterial readySwitch;
    private Uri imagePath;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private CircularProgressIndicator spinner;
    private LinearLayout submitCancelLayout;
    private MaterialSpinner typeLayout;
    private MapFragment mapFragment;
    private DatabaseReference mUserPetsReference;
    private ValueEventListener mUserPetsListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getParentFragmentManager().setFragmentResultListener("MAPPED", this, (requestKey, result) -> {
            latValue = result.getString("latValue");
            lngValue = result.getString("lngValue");
        });
        if (getArguments() != null) {
            petId = getArguments().getString("id");
            nameValue = getArguments().getString("name");
            raceValue = getArguments().getString("race");
            ageValue = getArguments().getString("age");
            genderValue = getArguments().getString("gender");
            typeValue = getArguments().getString("type");
            descriptionValue = getArguments().getString("description");
            latValue = getArguments().getString("lat");
            lngValue = getArguments().getString("lng");
            imageValue = getArguments().getString("image");
            readyValue = getArguments().getBoolean("ready");
        }
        startImageIntent = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result != null
                            && result.getResultCode() == Activity.RESULT_OK
                            && result.getData() != null) {
                        imagePath = result.getData().getData();
                    }
                });
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        mAuth = FirebaseAuth.getInstance();
        mUserPetsReference = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(mAuth.getCurrentUser().getUid())
                .child("pets");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_edit_pet, container, false);
        nameLayout = v.findViewById(R.id.nameLayout);
        raceLayout = v.findViewById(R.id.raceLayout);
        typeLayout = v.findViewById(R.id.typeLayout);
        Button ageButton = v.findViewById(R.id.ageButton);
        MaterialSpinner genderLayout = v.findViewById(R.id.genderLayout);
        descriptionLayout = v.findViewById(R.id.descriptionLayout);
        nameInput = v.findViewById(R.id.nameInput);
        descriptionInput = v.findViewById(R.id.descriptionInput);
        raceInput = v.findViewById(R.id.raceInput);
        errorView = v.findViewById(R.id.errorView);
        Button imageButton = v.findViewById(R.id.imagePickerButton);
        readySwitch = v.findViewById(R.id.readySwitch);
        submitCancelLayout = v.findViewById(R.id.submitCancelLayout);
        submitButton = v.findViewById(R.id.submitButton);
        Button cancelButton = v.findViewById(R.id.cancelButton);
        spinner = v.findViewById(R.id.spinner);

        Button locationButton = v.findViewById(R.id.locationButton);
        locationButton.setOnClickListener(view -> {
            mapFragment = new MapFragment(true, Double.parseDouble(latValue), Double.parseDouble(lngValue));
            mapFragment.show(requireActivity().getSupportFragmentManager(), null);
        });

        nameInput.setText(nameValue);

        MaterialDatePicker<Long> agePicker = handleCalendar();

        ageButton.setOnClickListener(view -> agePicker.show(getParentFragmentManager(), "BIRTHDAY_PICKER"));

        agePicker.addOnPositiveButtonClickListener(selection -> {
            TimeZone timeZoneUTC = TimeZone.getDefault();
            int offsetFromUTC = timeZoneUTC.getOffset(new Date().getTime()) * -1;
            SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date date = new Date(selection + offsetFromUTC);
            ageValue = simpleFormat.format(date);
        });

        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.genders, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderLayout.setAdapter(genderAdapter);

        genderLayout.setSelection(genderAdapter.getPosition(genderValue));

        genderLayout.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(@NonNull MaterialSpinner materialSpinner, View view, int i, long l) {
                genderValue = materialSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(@NonNull MaterialSpinner materialSpinner) {
            }
        });

        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.pet_types, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeLayout.setAdapter(typeAdapter);

        typeLayout.setSelection(typeAdapter.getPosition(typeValue));

        typeLayout.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(@NonNull MaterialSpinner materialSpinner, View view, int i, long l) {
                typeValue = materialSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(@NonNull MaterialSpinner materialSpinner) {
            }
        });

        descriptionInput.setText(descriptionValue);
        raceInput.setText(raceValue);
        readySwitch.setChecked(readyValue);
        imagePath = Uri.parse(imageValue);

        imageButton.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startImageIntent.launch(intent);
        });
        submitButton.setOnClickListener(view -> onSubmit());
        cancelButton.setOnClickListener(view -> switchTo());
        return v;
    }

    private MaterialDatePicker<Long> handleCalendar() {
        MaterialDatePicker.Builder<Long> agePickerBuilder = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select birthday")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds());
        Calendar minDate = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) - 20,
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        CalendarConstraints.Builder calendarConstraintBuilder = new CalendarConstraints.Builder();
        CalendarConstraints.DateValidator dateValidatorMin = DateValidatorPointForward.from(minDate.getTimeInMillis());
        CalendarConstraints.DateValidator dateValidatorMax = DateValidatorPointBackward.now();
        ArrayList<CalendarConstraints.DateValidator> listValidators =
                new ArrayList<>();
        listValidators.add(dateValidatorMin);
        listValidators.add(dateValidatorMax);
        CalendarConstraints.DateValidator validators = CompositeDateValidator.allOf(listValidators);
        calendarConstraintBuilder.setValidator(validators);
        agePickerBuilder.setCalendarConstraints(calendarConstraintBuilder.build());
        return agePickerBuilder.build();
    }

    private void onSubmit() {
        nameValue = nameInput.getText().toString().trim();
        descriptionValue = descriptionInput.getText().toString().trim();
        raceValue = raceInput.getText().toString().trim();
        readyValue = readySwitch.isChecked();
        if (formValid()) {
            submitButton.setEnabled(false);
            submitCancelLayout.setVisibility(View.GONE);
            spinner.setVisibility(View.VISIBLE);
            String imageID = UUID.randomUUID().toString();
            StorageReference ref = storageReference.child("images/" + imageID);

            if (imagePath.toString().equals(imageValue)) {
                editPet(imageValue);
            } else {
                ref.putFile(imagePath)
                        .addOnSuccessListener(taskSnapshot -> {
                            final Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                            firebaseUri.addOnSuccessListener(uri -> FirebaseStorage.getInstance()
                                    .getReferenceFromUrl(imageValue)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> editPet(uri.toString()))
                                    .addOnFailureListener(e -> {
                                        spinner.setVisibility(View.INVISIBLE);
                                        submitCancelLayout.setVisibility(View.VISIBLE);
                                        submitButton.setEnabled(true);
                                        Snackbar.make(requireActivity().findViewById(R.id.coordinatorLayout), e.getMessage(), Snackbar.LENGTH_LONG)
                                                .setAnchorView(requireActivity().findViewById(R.id.bottom_navigation))
                                                .show();
                                    }));
                        })
                        .addOnFailureListener(e -> {
                            spinner.setVisibility(View.INVISIBLE);
                            submitCancelLayout.setVisibility(View.VISIBLE);
                            submitButton.setEnabled(true);
                            Snackbar.make(requireActivity().findViewById(R.id.coordinatorLayout), e.getMessage(), Snackbar.LENGTH_LONG)
                                    .setAnchorView(requireActivity().findViewById(R.id.bottom_navigation))
                                    .show();
                        });
            }
        }
    }

    private void editPet(String imageUrl) {
        mUserPetsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String petID = dataSnapshot.getValue(String.class);
                    if (petID != null) {
                        if (petID.equals(petId)) {
                            Map<String, Object> petUpdates = new HashMap<>();
                            petUpdates.put("description", descriptionValue);
                            petUpdates.put("gender", genderValue);
                            petUpdates.put("name", nameValue.substring(0, 1).toUpperCase() + nameValue.substring(1));
                            petUpdates.put("race", raceValue);
                            petUpdates.put("type", typeValue);
                            petUpdates.put("age", ageValue);
                            petUpdates.put("lat", latValue);
                            petUpdates.put("lng", lngValue);
                            petUpdates.put("image", imageUrl);
                            petUpdates.put("ready", readyValue);
                            FirebaseDatabase.getInstance()
                                    .getReference("Pets")
                                    .child(petId)
                                    .updateChildren(petUpdates, (error1, ref1) -> {
                                        if (error1 != null) {
                                            spinner.setVisibility(View.INVISIBLE);
                                            submitCancelLayout.setVisibility(View.VISIBLE);
                                            submitButton.setEnabled(true);
                                            Snackbar.make(requireActivity().findViewById(R.id.coordinatorLayout), error1.getMessage(), Snackbar.LENGTH_LONG)
                                                    .setAnchorView(requireActivity().findViewById(R.id.bottom_navigation))
                                                    .show();
                                        } else {
                                            Snackbar.make(requireActivity().findViewById(R.id.coordinatorLayout), "Pet updated successfully!", Snackbar.LENGTH_LONG)
                                                    .setAnchorView(requireActivity().findViewById(R.id.bottom_navigation))
                                                    .show();
                                            switchTo();
                                        }
                                    });
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        mUserPetsReference.addListenerForSingleValueEvent(mUserPetsListener);
    }

    private void switchTo() {
        Bundle flipBundle = new Bundle();
        flipBundle.putString("flip", "ToProfile");
        flipBundle.putString("userID", mAuth.getCurrentUser().getUid());
        getParentFragmentManager().setFragmentResult("flipResult", flipBundle);
    }

    private boolean formValid() {
        nameLayout.setError(null);
        descriptionLayout.setError(null);
        errorView.setVisibility(View.INVISIBLE);
        if (nameValue == null || nameValue.length() < 2 || nameValue.length() > 20) {
            nameLayout.setError("2 < name < 20");
            return false;
        }
        if (raceValue == null || raceValue.length() == 0) {
            raceLayout.setError("Race required.");
            return false;
        }
        if (ageValue == null || ageValue.length() == 0) {
            errorView.setText(getString(R.string.age_required));
            errorView.setVisibility(View.VISIBLE);
            return false;
        }
        if (descriptionValue == null || descriptionValue.length() == 0) {
            descriptionLayout.setError("Description required.");
            return false;
        }
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mUserPetsListener != null)
            mUserPetsReference.removeEventListener(mUserPetsListener);
    }
}