package com.akhil.whatsapp.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.akhil.whatsapp.Adapters.UsersAdapter;
import com.akhil.whatsapp.Models.Users;
import com.akhil.whatsapp.R;

import com.akhil.whatsapp.databinding.FragmentChatsBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;


public class ChatsFragment extends Fragment {


    public ChatsFragment() {

        // Required empty public constructor
    }


    ArrayList<Users> list = new ArrayList<>();
    FirebaseDatabase database;
    RecyclerView chatRecyclerView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        chatRecyclerView = view.findViewById(R.id.chatRecyclerView);
        database = FirebaseDatabase.getInstance();
        UsersAdapter adapter = new UsersAdapter(list, getContext());
        chatRecyclerView.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        chatRecyclerView.setLayoutManager(layoutManager);

      database.getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @Override
           public void onDataChange(@NonNull DataSnapshot snapshot) {
               list.clear();
               for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                 Users users = dataSnapshot.getValue(Users.class);
                    users.getUserId();
                 list.add(users);
              }
                 adapter.notifyDataSetChanged();

            }

            @Override
           public void onCancelled(@NonNull DatabaseError error) {

            }
       });


        return view;


    }


}