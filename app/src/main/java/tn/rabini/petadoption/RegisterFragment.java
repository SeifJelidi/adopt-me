package tn.rabini.petadoption;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import tn.rabini.petadoption.models.User;

public class RegisterFragment extends Fragment {


    private TextView errorView;
    private CircularProgressIndicator spinner;
    private TextInputEditText usernameInput, emailInput, phoneInput, passwordInput, confirmPasswordInput;
    private TextInputLayout usernameLayout, emailLayout, phoneLayout, passwordLayout, confirmPasswordLayout;
    private FirebaseAuth mAuth;
    private Button signUpButton;
    private final DatabaseReference mUsersReference = FirebaseDatabase.getInstance().getReference("Users");
    private ValueEventListener mUsersListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_register, container, false);
        errorView = v.findViewById(R.id.errorView);
        spinner = v.findViewById(R.id.spinner);

        usernameInput = v.findViewById(R.id.usernameInput);
        emailInput = v.findViewById(R.id.emailInput);
        phoneInput = v.findViewById(R.id.phoneInput);
        passwordInput = v.findViewById(R.id.passwordInput);
        confirmPasswordInput = v.findViewById(R.id.confirmPasswordInput);

        usernameLayout = v.findViewById(R.id.usernameLayout);
        emailLayout = v.findViewById(R.id.emailLayout);
        phoneLayout = v.findViewById(R.id.phoneLayout);
        passwordLayout = v.findViewById(R.id.passwordLayout);
        confirmPasswordLayout = v.findViewById(R.id.confirmPasswordLayout);

        TextView signInLink = v.findViewById(R.id.signInLink);
        signInLink.setOnClickListener(view -> switchTo("ToLogin", null));


        signUpButton = v.findViewById(R.id.signUpButton);
        signUpButton.setOnClickListener(view -> onSignUp());

        return v;
    }


    private void onSignUp() {
        signUpButton.setEnabled(false);
        usernameLayout.setError(null);
        emailLayout.setError(null);
        phoneLayout.setError(null);
        passwordLayout.setError(null);
        confirmPasswordLayout.setError(null);
        errorView.setVisibility(View.INVISIBLE);

        String usernameValue = usernameInput.getText().toString().trim();
        String emailValue = emailInput.getText().toString().trim();
        String phoneValue = phoneInput.getText().toString().trim();
        String passwordValue = passwordInput.getText().toString().trim();
        String confirmPasswordValue = confirmPasswordInput.getText().toString().trim();

        if (formValid(usernameLayout, emailLayout, phoneLayout, passwordLayout, confirmPasswordLayout, usernameValue, emailValue, phoneValue, passwordValue, confirmPasswordValue)) {
            spinner.setVisibility(View.VISIBLE);
            usernameLayout.setError(null);
            emailLayout.setError(null);
            phoneLayout.setError(null);
            passwordLayout.setError(null);
            confirmPasswordLayout.setError(null);
            errorView.setVisibility(View.INVISIBLE);

            createUser(usernameValue, emailValue, phoneValue, passwordValue);

        } else {
            signUpButton.setEnabled(true);
        }
    }

    public void createUser(String usernameValue, String emailValue, String phoneValue, String passwordValue) {
        mUsersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        if (user.getPhone().equals(phoneValue) || user.getUsername().equals(usernameValue)) {
                            if (user.getPhone().equals(phoneValue)) {
                                phoneLayout.setError(getString(R.string.phone_exists));
                            } else {
                                usernameLayout.setError(getString(R.string.username_exists));
                            }
                            spinner.setVisibility(View.GONE);
                            signUpButton.setEnabled(true);
                            return;
                        }
                    }
                }
                mAuth.createUserWithEmailAndPassword(emailValue, passwordValue)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                User user = new User(usernameValue, emailValue, phoneValue, "https://firebasestorage.googleapis.com/v0/b/dogadoption-94cad.appspot.com/o/images%2Fdefault_profile_picture.png?alt=media&token=8c2794b6-2f3a-40fd-9b0c-9964a212bcf4");
                                mUsersReference
                                        .child(mAuth.getCurrentUser().getUid())
                                        .setValue(user)
                                        .addOnSuccessListener(aVoid -> mAuth.getCurrentUser().sendEmailVerification()
                                                .addOnSuccessListener(aVoid1 -> {
                                                    spinner.setVisibility(View.INVISIBLE);
                                                    Snackbar.make(requireActivity().findViewById(R.id.coordinatorLayout), "An email verification has been sent. Please verify your email.", Snackbar.LENGTH_LONG)
                                                            .setAnchorView(requireView().findViewById(R.id.bottom_navigation))
                                                            .show();
                                                    signUpButton.setEnabled(true);
                                                    switchTo("ToProfile", mAuth.getCurrentUser().getUid());
                                                })
                                                .addOnFailureListener(e -> {
                                                    spinner.setVisibility(View.INVISIBLE);
                                                    Snackbar.make(requireActivity().findViewById(R.id.coordinatorLayout), e.getMessage(), Snackbar.LENGTH_LONG)
                                                            .setAnchorView(requireView().findViewById(R.id.bottom_navigation))
                                                            .show();
                                                    signUpButton.setEnabled(true);
                                                    switchTo("ToProfile", mAuth.getCurrentUser().getUid());
                                                }))
                                        .addOnFailureListener(e -> {
                                            errorView.setText(e.getMessage());
                                            spinner.setVisibility(View.INVISIBLE);
                                            errorView.setVisibility(View.VISIBLE);
                                            signUpButton.setEnabled(true);
                                        });
                            } else {
                                spinner.setVisibility(View.INVISIBLE);
                                signUpButton.setEnabled(true);
                                try {
                                    throw Objects.requireNonNull(task.getException());
                                } catch (FirebaseAuthUserCollisionException e) {
                                    emailLayout.setError(e.getMessage());
                                } catch (Exception e) {
                                    errorView.setText(e.getMessage());
                                    errorView.setVisibility(View.VISIBLE);
                                }
                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                errorView.setText(error.getMessage());
                spinner.setVisibility(View.INVISIBLE);
                errorView.setVisibility(View.VISIBLE);
                signUpButton.setEnabled(true);
            }
        };
        mUsersReference.addListenerForSingleValueEvent(mUsersListener);
    }

    private boolean formValid(TextInputLayout usernameLayout,
                              TextInputLayout emailLayout,
                              TextInputLayout phoneLayout,
                              TextInputLayout passwordLayout,
                              TextInputLayout confirmPasswordLayout,
                              String usernameValue,
                              String emailValue,
                              String phoneValue,
                              String passwordValue,
                              String confirmPasswordValue) {
        if (usernameValue == null || usernameValue.length() < 2 || usernameValue.length() > 30) {
            usernameLayout.setError(getString(R.string.username_error));
            return false;
        }
        if (emailValue == null || !Patterns.EMAIL_ADDRESS.matcher(emailValue).matches()) {
            emailLayout.setError(getString(R.string.email_error));
            return false;
        }

        if (phoneValue == null || !validPhone(phoneValue)) {
            phoneLayout.setError(getString(R.string.phone_error));
            return false;
        }

        if (passwordValue == null || passwordValue.length() < 6) {
            passwordLayout.setError(getString(R.string.password_short));
            return false;
        }
        if (confirmPasswordValue == null || !confirmPasswordValue.equals(passwordValue)) {
            confirmPasswordLayout.setError(getString(R.string.password_error));
            return false;
        }
        return true;
    }

    private boolean validPhone(String phoneValue) {
        return (phoneValue.startsWith("9")
                || phoneValue.startsWith("7")
                || phoneValue.startsWith("5")
                || phoneValue.startsWith("4")
                || phoneValue.startsWith("2"))
                && phoneValue.length() == 8;
    }

    private void switchTo(String fragmentName, @Nullable String userID) {
        Bundle flipBundle = new Bundle();
        flipBundle.putString("flip", fragmentName);
        if (fragmentName.equals("ToProfile"))
            flipBundle.putString("userID", userID);
        getParentFragmentManager().setFragmentResult("flipResult", flipBundle);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mUsersListener != null)
            mUsersReference.removeEventListener(mUsersListener);
    }
}