package com.example.tjr.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.tjr.myapplication.map.MapActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        
    }

    @OnClick(R.id.signin_button)
    void showMap(){
        Intent myIntent = new Intent(this, MapActivity.class);
        this.startActivity(myIntent);
    }

    @OnClick(R.id.signup_text)
    void showSignUp(){
        Intent myIntent = new Intent(this, SignupActivity.class);
        this.startActivity(myIntent);
    }
}
