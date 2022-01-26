package tn.rabini.petadoption;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class LoginFragment extends Fragment {

    private TextView errorView;
    private TextInputLayout emailLayout, passwordLayout;
    private TextInputEditText emailInput, passwordInput;
    private CircularProgressIndicator spinner;
    private FirebaseAuth mAuth;
    private Button signInButton;


    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_login, container, false);
        emailLayout = v.findViewById(R.id.emailLayout);
        passwordLayout = v.findViewById(R.id.passwordLoginLayout);
        emailInput = v.findViewById(R.id.emailInput);
        passwordInput = v.findViewById(R.id.passwordLoginInput);
        errorView = v.findViewById(R.id.errorView);
        spinner = v.findViewById(R.id.spinner);
        TextView resetLink = v.findViewById(R.id.resetLink);

        resetLink.setOnClickListener(view -> resetPassword());

        TextView signUpLink = v.findViewById(R.id.signUpLink);
        signUpLink.setOnClickListener(view -> switchTo("ToRegister"));


        signInButton = v.findViewById(R.id.signInButton);
        signInButton.setOnClickListener(view -> onSignIn());

        return v;
    }

    private void onSignIn() {
        signInButton.setEnabled(false);
        errorView.setVisibility(View.INVISIBLE);
        String emailValue = emailInput.getText().toString().trim();
        String passwordValue = passwordInput.getText().toString().trim();
        if (TextUtils.isEmpty(emailValue) || TextUtils.isEmpty(passwordValue)) {
            if (TextUtils.isEmpty(emailValue)) {
                emailLayout.setError(getString(R.string.field_empty));
            }

            if (TextUtils.isEmpty(passwordValue)) {
                passwordLayout.setError(getString(R.string.field_empty));
            }

            signInButton.setEnabled(true);

            return;
        }

        spinner.setVisibility(View.VISIBLE);
        emailLayout.setError(null);
        passwordLayout.setError(null);
        errorView.setVisibility(View.INVISIBLE);

        mAuth.signInWithEmailAndPassword(emailValue, passwordValue).addOnSuccessListener(authResult -> {
            spinner.setVisibility(View.INVISIBLE);
            signInButton.setEnabled(true);
            switchTo("ToHome");
        }).addOnFailureListener(e -> {
            spinner.setVisibility(View.INVISIBLE);
            errorView.setText(e.getMessage());
            errorView.setVisibility(View.VISIBLE);
            signInButton.setEnabled(true);
        });
    }

    private void resetPassword() {
        AlertDialog resetBuilder = new MaterialAlertDialogBuilder(requireContext())
                .setView(R.layout.reset_password)
                .setPositiveButton("Submit", null)
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                .create();

        resetBuilder.setOnShowListener(dialogInterface -> {
            Button saveButton = ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE);
            CircularProgressIndicator spinner = ((AlertDialog) dialogInterface).findViewById(R.id.spinner);
            TextView passwordResetError = ((AlertDialog) dialogInterface).findViewById(R.id.passwordResetError);
            TextInputLayout passwordResetLayout = ((AlertDialog) dialogInterface).findViewById(R.id.passwordResetLayout);
            saveButton.setOnClickListener(view -> {
                TextInputEditText passwordResetInput = ((AlertDialog) dialogInterface).findViewById(R.id.passwordResetInput);
                String emailValue = passwordResetInput.getText().toString().trim();
                passwordResetLayout.setError(null);
                passwordResetError.setVisibility(View.GONE);

                if (emailValue == null || !Patterns.EMAIL_ADDRESS.matcher(emailValue).matches()) {
                    passwordResetLayout.setError(getString(R.string.email_error));
                    return;
                }

                spinner.setVisibility(View.VISIBLE);
                FirebaseAuth.getInstance().sendPasswordResetEmail(emailValue).addOnSuccessListener(aVoid -> {
                    Snackbar.make(requireActivity().findViewById(R.id.coordinatorLayout), requireContext().getString(R.string.reset_sent), Snackbar.LENGTH_LONG)
                            .setAnchorView(requireActivity().findViewById(R.id.bottom_navigation))
                            .show();
                    dialogInterface.dismiss();
                }).addOnFailureListener(e -> Snackbar.make(requireActivity().findViewById(R.id.coordinatorLayout), requireContext().getString(R.string.reset_sent), Snackbar.LENGTH_LONG)
                        .setAnchorView(requireActivity().findViewById(R.id.bottom_navigation))
                        .show());
            });
        });

        resetBuilder.show();
    }

    private void switchTo(String fragmentName) {
        Bundle flipBundle = new Bundle();
        flipBundle.putString("flip", fragmentName);
        getParentFragmentManager().setFragmentResult("flipResult", flipBundle);
    }
}