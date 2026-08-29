package com.example.module_android_demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

/**
 * شاشة تسجيل الدخول - أول شاشة تفتح في التطبيق.
 * بتستخدم Firebase Authentication (بريد إلكتروني / كلمة مرور) بنفس الحساب
 * المستخدم في تطبيق TRAC-GOLD (Flutter)، عشان القطع والبيانات تكون واحدة.
 */
public class LoginActivity extends Activity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ProgressBar pbLogin;
    private TextView tvError;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        etEmail = (EditText) findViewById(R.id.et_email);
        etPassword = (EditText) findViewById(R.id.et_password);
        btnLogin = (Button) findViewById(R.id.btn_login);
        pbLogin = (ProgressBar) findViewById(R.id.pb_login);
        tvError = (TextView) findViewById(R.id.tv_login_error);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // لو المستخدم مسجل دخول بالفعل (Session محفوظة)، اتخطى شاشة اللوجين على طول
        if (mAuth.getCurrentUser() != null) {
            goToMain();
        }
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            showError("اكتب البريد الإلكتروني وكلمة المرور");
            return;
        }

        setLoading(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(this, authResult -> {
                    setLoading(false);
                    goToMain();
                })
                .addOnFailureListener(this, e -> {
                    setLoading(false);
                    showError(friendlyError(e));
                });
    }

    private String friendlyError(Exception e) {
        if (e instanceof FirebaseAuthInvalidUserException) {
            return "الحساب غير موجود";
        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
            return "البريد الإلكتروني أو كلمة المرور غير صحيحة";
        }
        return "فشل تسجيل الدخول، حاول مرة أخرى";
    }

    private void showError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
    }

    private void setLoading(boolean loading) {
        pbLogin.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
    }

    private void goToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
