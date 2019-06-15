package com.example.mohammad.apufodd;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.type.Date;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DailySale extends AppCompatActivity {
    public class food{
        String name;
        Double count;
        Double price;
        Double total;
    }
    List<food> food_list = new ArrayList<food>();
    Activity activity;
    FirebaseFirestore db;
    CalendarView calendarView;
    TextView tTotal;
    ListView lv;
    ImageButton chat;
    ImageButton qr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_sale);
        activity=this;
        db = FirebaseFirestore.getInstance();
        calendarView=(CalendarView) findViewById(R.id.calendarView);
        tTotal=(TextView) findViewById(R.id.textView22);
        lv=(ListView) findViewById(R.id.ListView8);
        getFoodList();
        chat=(ImageButton) findViewById(R.id.imageButton6);
        qr=(ImageButton) findViewById(R.id.imageButton7);
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent adIntent = new Intent(activity, ChatList.class);
                activity.startActivity(adIntent);
            }
        });
        qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent adIntent = new Intent(activity, ScanQr.class);
                activity.startActivity(adIntent);
            }
        });
        java.util.Date curr= new java.util.Date();
        calendarView.setDate(curr.getTime());

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {

            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month,
                                            int dayOfMonth) {
                loadOrders(year,month,dayOfMonth);
            }
        });
    }
    public void loadOrders(final int year, final int month, final int day)
    {
        db.collection("orders")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                java.util.Date date=document.getDate("datetime");
                                int my=date.getYear()+1900;
                                int mm=date.getMonth();
                                int md=date.getDate();
                                if(my==year && md==day && mm==month) {
                                    Map<String, HashMap> orders= (Map<String, HashMap>) document.get("order");
                                    for(Map.Entry<String, HashMap> entry : orders.entrySet()) {
                                        HashMap value = entry.getValue();
                                        for(int i=0;i<food_list.size();i++)
                                        {
                                            if(food_list.get(i).name.equals(value.get("name").toString()))
                                            {
                                                food_list.get(i).count+=(Double)value.get("num");
                                                food_list.get(i).total+=((Double)value.get("num")*(Double)value.get("price"));
                                                break;
                                            }
                                        }
                                        // In your case, another loop.
                                    }
                                }

                            }
                            List<String> sale_list = new ArrayList<String>();
                            Double total=0.0;
                            for(int i=0;i<food_list.size();i++)
                            {
                                if(food_list.get(i).count>0)
                                {
                                    sale_list.add(food_list.get(i).count+"X  "+food_list.get(i).name+"  /  RM"+food_list.get(i).total);
                                    total+=food_list.get(i).total;
                                    food_list.get(i).count=0.0;
                                    food_list.get(i).total=0.0;
                                }
                            }
                            tTotal.setText("Total Sale Amount: RM"+total);
                            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                                    (activity, android.R.layout.simple_list_item_1, sale_list);
                            lv.setAdapter(arrayAdapter);


                        } else {
                            Log.w("aba", "Error getting documents.", task.getException());
                        }
                    }
                });


    }
    public void getFoodList()
    {
        db.collection("foods")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int userType=0;

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                food tempFood= new food();
                                tempFood.name=document.getString("name");
                                tempFood.price=document.getDouble("price");
                                tempFood.count=0.0;
                                tempFood.total=0.0;
                                food_list.add(tempFood);
                            }
                        } else {
                            Log.w("aba", "Error getting documents.", task.getException());
                        }
                    }
                });

    }
}
