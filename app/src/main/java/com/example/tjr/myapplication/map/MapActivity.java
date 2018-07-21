package com.example.tjr.myapplication.map;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.transition.TransitionManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tjr.myapplication.MainActivity;
import com.example.tjr.myapplication.R;
import com.example.tjr.myapplication.home.options.SelectVehicleFragment;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapActivity extends BaseActivity implements SelectVehicleFragment.OnFragmentInteractionListener {

    @BindView(R.id.rootFrame)
    FrameLayout rootFrame;

    @BindView(R.id.rootll)
    LinearLayout rootll;

    @BindView(R.id.viewPager)
    ViewPager viewPager;

    @BindView(R.id.rlwhere)
    RelativeLayout rlWhere;


    @BindView(R.id.tvWhereTo)
    TextView tvWhereto;

    @BindView(R.id.ride_options_bar)
    LinearLayout rideOptionsBar;


    ArgbEvaluator argbEvaluator;

    private LatLng destination;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ButterKnife.bind(this);
        context = MapActivity.this;

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapActivity.this);

        argbEvaluator = new ArgbEvaluator();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int devHeight = displayMetrics.heightPixels;
        int devWidth = displayMetrics.widthPixels;

        setUpPagerAdapter();
        viewPager.setClipToPadding(false);
        viewPager.setPageMargin(-devWidth / 2);

        viewPager.addOnPageChangeListener(pageChangeListener);
        viewPager.setPageTransformer(true, pageTransformer);


    }

    ViewPager.PageTransformer pageTransformer = new ViewPager.PageTransformer() {
        @Override
        public void transformPage(View page, float position) {


            if (position < -1) { // [-Infinity,-1)


            } else if (position <= 1) { // [-1,1]

                if (position >= -1 && position < 0) {

                    LinearLayout uberEco = (LinearLayout) page.findViewById(R.id.lluberEconomy);
                    TextView uberEcoTv = (TextView) page.findViewById(R.id.tvuberEconomy);

                    if (uberEco != null && uberEcoTv != null) {

                        uberEcoTv.setTextColor((Integer) argbEvaluator.evaluate(-2 * position, getResources().getColor(R.color.black)
                                , getResources().getColor(R.color.grey)));

                        uberEcoTv.setTextSize(16 + 4 * position);
                        uberEco.setX((page.getWidth() * position));

                    }

                } else if (position >= 0 && position <= 1) {

                    TextView uberPreTv = (TextView) page.findViewById(R.id.tvuberPre);
                    LinearLayout uberPre = (LinearLayout) page.findViewById(R.id.llUberPre);

                    if (uberPreTv != null && uberPre != null) {

                        uberPreTv.setTextColor((Integer) new ArgbEvaluator().evaluate((1 - position), getResources().getColor(R.color.grey)
                                , getResources().getColor(R.color.black)));

                        uberPreTv.setTextSize(12 + 4 * (1 - position));
                        uberPre.setX(uberPre.getLeft() + (page.getWidth() * (position)));


                    }


                }

            }
        }
    };


    ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {


        }

        @Override
        public void onPageSelected(int position) {

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    void showViewPagerWithTransition() {

        TransitionManager.beginDelayedTransition(rootFrame);
        rideOptionsBar.setVisibility(View.VISIBLE);
        rlWhere.setVisibility(View.INVISIBLE);

        mMap.setPadding(0, 0, 0, viewPager.getHeight());

    }

    @OnClick(R.id.rlwhere)
    void openPlacesView() {
        openPlaceAutoCompleteView();
    }

    boolean isBarShown = false;

    @SuppressLint("ResourceType")
    @OnClick(R.id.selected_vehicle_btn)
    void showVehicleSelectionFragment() {
        isBarShown = !isBarShown;

        SelectVehicleFragment fragment = SelectVehicleFragment.newInstance();
        FragmentManager fm = getSupportFragmentManager();
        if (isBarShown)
            fm.beginTransaction()
                    .replace(R.id.selection_fragment_holder, fragment)
                    .setCustomAnimations(android.R.anim.slide_in_left, R.anim.slide_out_right)
                    .commit();
        else
            fm.beginTransaction().remove(fragment);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    void startRevealAnimation() {

        int cx = rootFrame.getMeasuredWidth() / 2;
        int cy = rootFrame.getMeasuredHeight() / 2;

        Animator anim =
                ViewAnimationUtils.createCircularReveal(rootll, cx, cy, 50, rootFrame.getWidth());

        anim.setDuration(500);
        anim.setInterpolator(new AccelerateInterpolator(2));
        anim.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {

                super.onAnimationStart(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                rlWhere.setVisibility(View.VISIBLE);
            }
        });

        anim.start();
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);
        if (rootll.isAttachedToWindow()) {
            startRevealAnimation();
        } else {
            this.rootll.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {
                    startRevealAnimation();
                }

                @Override
                public void onViewDetachedFromWindow(View v) {

                }
            });
        }


    }

    @Override
    protected void setUpPolyLine(LatLng source, LatLng destination) {

        if (source != null && destination != null) {

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://maps.googleapis.com/maps/api/directions/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            getPolyline polyline = retrofit.create(getPolyline.class);

            polyline.getPolylineData(source.latitude + "," + source.longitude, destination.latitude + "," + destination.longitude)
                    .enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {

                            JsonObject gson = new JsonParser().parse(response.body().toString()).getAsJsonObject();
                            try {

                                Single.just(parse(new JSONObject(gson.toString())))
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Consumer<List<List<HashMap<String, String>>>>() {
                                            @Override
                                            public void accept(List<List<HashMap<String, String>>> lists) throws Exception {

                                                drawPolyline(lists);
                                            }
                                        });

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<JsonObject> call, Throwable t) {

                        }
                    });
        } else
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
    }

    private void setUpPagerAdapter() {

        List<Integer> data = Arrays.asList(0, 1);
        CarsPagerAdapter adapter = new CarsPagerAdapter(data);
        viewPager.setAdapter(adapter);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onBackPressed() {

        if (rideOptionsBar.getVisibility() == View.VISIBLE) {

            TransitionManager.beginDelayedTransition(rootFrame);
            rideOptionsBar.setVisibility(View.INVISIBLE);
            mMap.setPadding(0, 0, 0, 0);
            rlWhere.setVisibility(View.VISIBLE);

            return;
        }

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                LatLng source = new LatLng(getUserLocation().getLatitude(), getUserLocation().getLongitude());
                destination = place.getLatLng();
                setUpPolyLine(source, destination);
                showViewPagerWithTransition();
                showOptions();

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Toast.makeText(this, "Error " + status, Toast.LENGTH_SHORT).show();

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    void showOptions() {
        addRideOption("Cheapest", "UBER", "400");
        addRideOption("Nearest", "PICK ME", "500");
        addRideOption("Other", "Kangaroo", "700");


    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    void addRideOption(String speciality, String provider, String priceStr) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager()
                .getDefaultDisplay()
                .getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int margins = screenWidth / 24;
        LinearLayout rideOption = new LinearLayout(this);
        LinearLayout.LayoutParams rideOptionParam = new LinearLayout.LayoutParams((screenWidth / 4), ViewGroup.LayoutParams.WRAP_CONTENT);
        rideOptionParam.setMargins(margins, 10, margins, 10);
        rideOption.setLayoutParams(rideOptionParam);
        rideOption.setOrientation(LinearLayout.VERTICAL);
        rideOption.setBackground(getResources().getDrawable(R.drawable.rounded_corners_background));

        TextView specialityText = new TextView(context);
        LinearLayout.LayoutParams specialityTextParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        specialityText.setLayoutParams(specialityTextParams);
        specialityText.setText(speciality);
        specialityText.setPadding(5, 5, 5, 5);
        specialityText.setTypeface(Typeface.DEFAULT_BOLD);
        specialityText.setTextColor(Color.BLACK);
        specialityText.setGravity(Gravity.CENTER);
        rideOption.addView(specialityText);

        ImageView imageView = new ImageView(context);
        LinearLayout.LayoutParams imageViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        imageView.setLayoutParams(imageViewParams);
        imageView.setPadding(10, 10, 10, 10);
        imageView.setImageDrawable(getResources().getDrawable(R.drawable.cab2));
        rideOption.addView(imageView);

        TextView providerText = new TextView(context);
        LinearLayout.LayoutParams providerParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        providerText.setLayoutParams(specialityTextParams);
        providerText.setText(provider);
        providerText.setPadding(5, 5, 5, 5);
        providerText.setTypeface(Typeface.DEFAULT);
        providerText.setTextColor(Color.BLACK);
        providerText.setGravity(Gravity.CENTER);
        rideOption.addView(providerText);


        TextView priceText = new TextView(context);
        LinearLayout.LayoutParams priceParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        priceText.setLayoutParams(specialityTextParams);
        priceText.setText("Rs. " + priceStr);
        priceText.setPadding(5, 5, 5, 5);
        priceText.setTypeface(Typeface.DEFAULT);
        priceText.setTextColor(Color.BLACK);
        priceText.setGravity(Gravity.CENTER);
        rideOption.addView(priceText);

        rideOptionsBar.addView(rideOption);

    }

}
