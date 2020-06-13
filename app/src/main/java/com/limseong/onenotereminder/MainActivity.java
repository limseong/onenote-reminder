package com.limseong.onenotereminder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

import com.limseong.onenotereminder.sections.SectionsFragment;
import com.limseong.onenotereminder.sections.SectionsPresenter;
import com.limseong.onenotereminder.settings.SettingsFragment;
import com.limseong.onenotereminder.settings.SettingsPresenter;
import com.limseong.onenotereminder.util.AuthenticationHelper;
import com.limseong.onenotereminder.util.GraphHelper;
import com.limseong.onenotereminder.util.IAuthenticationHelperCreatedListener;
import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.IAuthenticationResult;
import com.microsoft.identity.client.exception.MsalClientException;
import com.microsoft.identity.client.exception.MsalException;
import com.microsoft.identity.client.exception.MsalServiceException;
import com.microsoft.identity.client.exception.MsalUiRequiredException;

import com.microsoft.graph.concurrency.ICallback;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.models.extensions.User;

public class MainActivity extends AppCompatActivity {
    private static final String SAVED_IS_SIGNED_IN = "isSignedIn";
    private static final String SAVED_USER_NAME = "userName";
    private static final String SAVED_USER_EMAIL = "userEmail";

    private DrawerLayout mDrawer;
    private NavigationView mNavigationView;
    private View mHeaderView;

    private boolean mIsSignedIn = false;
    private String mUserName = null;
    private String mUserEmail = null;
    private AuthenticationHelper mAuthHelper = null;
    private boolean mAttemptInteractiveSignIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawer = findViewById(R.id.drawer_layout);

        // Add the hamburger menu icon
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();


        mNavigationView = findViewById(R.id.nav_view);
        setupDrawerContent(mNavigationView); // setup listener

        // Set user name and email
        mHeaderView = mNavigationView.getHeaderView(0);
        setSignedInState(mIsSignedIn);


        // Load the home fragment by default on startup
        if (savedInstanceState == null) {
            openHomeFragment(mUserName);

        } else {
            // Restore state
            mIsSignedIn = savedInstanceState.getBoolean(SAVED_IS_SIGNED_IN);
            mUserName = savedInstanceState.getString(SAVED_USER_NAME);
            mUserEmail = savedInstanceState.getString(SAVED_USER_EMAIL);
            setSignedInState(mIsSignedIn);
        }

        showProgressBar();

        // Get the authentication helper
        AuthenticationHelper.getInstance(getApplicationContext(),
                new IAuthenticationHelperCreatedListener() {
                    @Override
                    public void onCreated(AuthenticationHelper authHelper) {
                        mAuthHelper = authHelper;
                        if (!mIsSignedIn) {
                            doSilentSignIn(false);
                        } else {
                            hideProgressBar();
                        }
                    }

                    @Override
                    public void onError(MsalException exception) {
                        Log.e("AUTH", "Error creating auth helper", exception);
                    }
                });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_IS_SIGNED_IN, mIsSignedIn);
        outState.putString(SAVED_USER_NAME, mUserName);
        outState.putString(SAVED_USER_EMAIL, mUserEmail);
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        // Load the fragment that corresponds to the selected item
                        switch (menuItem.getItemId()) {
                            case R.id.nav_home:
                                openHomeFragment(mUserName);
                                break;
                            case R.id.nav_signin:
                                signIn();
                                break;
                            case R.id.nav_signout:
                                signOut();
                                break;
                            case R.id.nav_sections:
                                openSectionsFragment();
                                break;
                            case R.id.nav_settings:
                                openSettingsFragment();
                                break;
                            case R.id.nav_license:
                                startActivity(new Intent(getApplicationContext(), OssLicensesMenuActivity.class));
                                break;
                            default:
                                break;
                        }

                        mDrawer.closeDrawer(GravityCompat.START);
                        return true;
                    }
                });
    }

    public void showProgressBar()
    {
        FrameLayout container = findViewById(R.id.fragment_container);
        ProgressBar progressBar = findViewById(R.id.progressbar);
        container.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar()
    {
        FrameLayout container = findViewById(R.id.fragment_container);
        ProgressBar progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.GONE);
        container.setVisibility(View.VISIBLE);
    }

    // Update the menu and get the user's name and email
    private void setSignedInState(boolean isSignedIn) {
        mIsSignedIn = isSignedIn;

        mNavigationView.getMenu().clear();
        mNavigationView.inflateMenu(R.menu.drawer_menu);

        Menu menu = mNavigationView.getMenu();

        // Hide/show the Sign in, Calendar, and Sign Out buttons
        if (isSignedIn) {
            menu.removeItem(R.id.nav_signin);
        } else {
            menu.removeItem(R.id.nav_home);
            menu.removeItem(R.id.nav_sections);
            menu.removeItem(R.id.nav_settings);
            menu.removeItem(R.id.nav_signout);
        }

        // Set the user name and email in the nav drawer
        TextView userName = mHeaderView.findViewById(R.id.user_name);
        TextView userEmail = mHeaderView.findViewById(R.id.user_email);

        if (isSignedIn) {
            userName.setText(mUserName);
            userEmail.setText(mUserEmail);
        } else {
            mUserName = null;
            mUserEmail = null;

            userName.setText("Please sign in");
            userEmail.setText("");
        }
    }

    // Load the "Home" fragment
    public void openHomeFragment(String userName) {
        HomeFragment fragment = HomeFragment.createInstance(userName);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
        mNavigationView.setCheckedItem(R.id.nav_home);
    }

    private void openSectionsFragment() {
        Fragment currentFragment = getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);

        SectionsFragment sectionsFragment = null;
        if (!(currentFragment instanceof SectionsFragment)) {
            // make view
            sectionsFragment = new SectionsFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, sectionsFragment)
                    //.add(R.id.fragment_container, sectionsFragment)
                    .commit();

            // make presenter
            new SectionsPresenter(sectionsFragment, getApplicationContext());
        }

        mNavigationView.setCheckedItem(R.id.nav_sections);
    }

    private void openSettingsFragment() {
        Fragment currentFragment = getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);

        SettingsFragment settingsFragment = null;
        if (!(currentFragment instanceof SettingsFragment)) {
            // make view
            settingsFragment = new SettingsFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, settingsFragment)
                    .commit();

            // make presenter
            new SettingsPresenter(settingsFragment, getApplicationContext());
        }

        mNavigationView.setCheckedItem(R.id.nav_settings);
    }

    private void signIn() {
        showProgressBar();
        // Attempt silent sign in first
        // if this fails, the callback will handle doing
        // interactive sign in
        doSilentSignIn(true);
    }

    private void signOut() {
        mAuthHelper.signOut();

        setSignedInState(false);
        openHomeFragment(mUserName);
    }

    // Silently sign in - used if there is already a
    // user account in the MSAL cache
    private void doSilentSignIn(boolean shouldAttemptInteractive) {
        mAttemptInteractiveSignIn = shouldAttemptInteractive;
        mAuthHelper.acquireTokenSilently(getAuthCallback());
    }

    // Prompt the user to sign in
    private void doInteractiveSignIn() {
        mAuthHelper.acquireTokenInteractively(this, getAuthCallback());
    }

    // Handles the authentication result
    public AuthenticationCallback getAuthCallback() {
        return new AuthenticationCallback() {

            // <OnSuccessSnippet>
            @Override
            public void onSuccess(IAuthenticationResult authenticationResult) {
                // Log the token for debug purposes
                String accessToken = authenticationResult.getAccessToken();
                Log.d("AUTH", String.format("Access token: %s", accessToken));

                // Get Graph client and get user
                GraphHelper graphHelper = GraphHelper.getInstance();
                graphHelper.getUser(accessToken, getUserCallback());
            }
            // </OnSuccessSnippet>

            @Override
            public void onError(MsalException exception) {
                // Check the type of exception and handle appropriately
                if (exception instanceof MsalUiRequiredException) {
                    Log.d("AUTH", "Interactive login required");
                    if (mAttemptInteractiveSignIn) {
                        doInteractiveSignIn();
                    }

                } else if (exception instanceof MsalClientException) {
                    if (exception.getErrorCode() == "no_current_account" ||
                            exception.getErrorCode() == "no_account_found") {
                        Log.d("AUTH", "No current account, interactive login required");
                        if (mAttemptInteractiveSignIn) {
                            doInteractiveSignIn();
                        }
                    } else {
                        // Exception inside MSAL, more info inside MsalError.java
                        Log.e("AUTH", "Client error authenticating", exception);
                    }
                } else if (exception instanceof MsalServiceException) {
                    // Exception when communicating with the auth server, likely config issue
                    Log.e("AUTH", "Service error authenticating", exception);
                }

                hideProgressBar();
            }

            @Override
            public void onCancel() {
                // User canceled the authentication
                Log.d("AUTH", "Authentication canceled");
                hideProgressBar();
            }
        };
    }

    private ICallback<User> getUserCallback() {
        return new ICallback<User>() {
            @Override
            public void success(User user) {
                Log.d("AUTH", "User: " + user.displayName);

                mUserName = user.displayName;
                mUserEmail = user.mail == null ? user.userPrincipalName : user.mail;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgressBar();

                        setSignedInState(true);
                        openHomeFragment(mUserName);
                    }
                });

            }

            @Override
            public void failure(ClientException ex) {
                Log.e("AUTH", "Error getting /me", ex);
                mUserName = "ERROR";
                mUserEmail = "ERROR";

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgressBar();

                        setSignedInState(true);
                        openHomeFragment(mUserName);
                    }
                });
            }
        };
    }
}
