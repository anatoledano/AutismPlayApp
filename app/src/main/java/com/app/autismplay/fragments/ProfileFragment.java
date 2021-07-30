package com.app.autismplay.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.app.autismplay.MainActivity;
import com.app.autismplay.R;
import com.app.autismplay.adapter.AnimalsAdapter;
import com.app.autismplay.adapter.ObjectsAdapter;
import com.app.autismplay.adapter.VideoAdapter;
import com.app.autismplay.helper.ConfigurationFirebase;
import com.app.autismplay.helper.ConfigurationProgressDialog;
import com.app.autismplay.helper.ConfigurationsConstants;
import com.app.autismplay.models.Animal;
import com.app.autismplay.models.Object;
import com.app.autismplay.models.User;
import com.app.autismplay.models.Video;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private ImageView imgNewAnimal,imgNewObject;
    private TextView txtNamParents,txtNamKid,txtAgeKid,txtEmail,txtSex;

    private ProgressDialog pd;

    private FirebaseUser user;
    private DatabaseReference databaseReference;

    private RecyclerView recyclerAnimals,recyclerObjects;
    private ObjectsAdapter objectsAdapter;
    private AnimalsAdapter animalsAdapter;

    private List<Object> objectsList;
    private List<Animal> animalList;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        imgNewAnimal = view.findViewById(R.id.addAnimal);
        imgNewObject = view.findViewById(R.id.addObj);
        txtNamKid = view.findViewById(R.id.txtNamChi);
        txtNamParents = view.findViewById(R.id.txtNamPar);
        txtAgeKid = view.findViewById(R.id.txtAge);
        txtEmail = view.findViewById(R.id.txtEma);
        txtSex = view.findViewById(R.id.txtSex);
        recyclerAnimals = view.findViewById(R.id.recycleAnimals);
        recyclerObjects = view.findViewById(R.id.recycleObjects);

        pd = ConfigurationProgressDialog.getProgressDialog(getContext());

        databaseReference = FirebaseDatabase.getInstance().getReference(ConfigurationsConstants.NODE_USERS);
        user = ConfigurationFirebase.getReferenceAutentication().getCurrentUser();

        objectsList = new ArrayList<>();
        animalList = new ArrayList<>();

                imgNewAnimal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newAnimal();
            }
        });
        imgNewObject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newObject();
            }
        });

        getProfileData();

        return view;

    }
    private void getProfileData(){
        pd.show();
        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren() ){
                    User userCurrent = ds.getValue(User.class);
                    txtAgeKid.setText(String.valueOf(userCurrent.getAge()));
                    txtEmail.setText(userCurrent.getEmail());
                    txtNamKid.setText(userCurrent.getNameKid());
                    txtNamParents.setText(userCurrent.getNameParent());
                    txtSex.setText(userCurrent.getSex());
                    getPreferencesDataAnimals(userCurrent.getId());

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                pd.dismiss();
            }
        });
        //pd.show();
    }
    private void getPreferencesDataAnimals(String userId){
        animalList.clear();
        databaseReference = FirebaseDatabase.getInstance().getReference("Preferences");
        databaseReference.child(userId).child("Animals").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren() ){
                    Animal animal = ds.getValue(Animal.class);
                    animalList.add(animal);
                }
                recyclerAnimals.setHasFixedSize(true);
                recyclerAnimals.setLayoutManager(new LinearLayoutManager((getContext())));
                animalsAdapter = new AnimalsAdapter(animalList,getContext());
                recyclerAnimals.setAdapter(animalsAdapter);
                getPreferencesDataObjects(userId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                pd.dismiss();
            }
        });
    }

    private void getPreferencesDataObjects(String userId){
        objectsList.clear();
        databaseReference = FirebaseDatabase.getInstance().getReference("Preferences");
        databaseReference.child(userId).child("Objects").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren() ){
                    Object object = ds.getValue(Object.class);
                    objectsList.add(object);
                }
                recyclerObjects.setHasFixedSize(true);
                recyclerObjects.setLayoutManager(new LinearLayoutManager((getContext())));
                objectsAdapter = new ObjectsAdapter(objectsList,getContext());
                recyclerObjects.setAdapter(objectsAdapter);
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                pd.dismiss();
            }
        });
    }

    private void newAnimal(){


        databaseReference = FirebaseDatabase.getInstance().getReference("Preferences");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Novo Animal: ");
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);


        EditText txtNome = new EditText(getActivity());
        txtNome.setHint("Nome do Animal");
        EditText txtCor = new EditText(getActivity());
        txtCor.setHint("Cor do Animal");
        EditText txtRaca = new EditText(getActivity());
        txtRaca.setHint("Raça do Animal");



        linearLayout.addView(txtNome);
        linearLayout.addView(txtCor);
        linearLayout.addView(txtRaca);

        builder.setView(linearLayout);

        builder.setPositiveButton("SALVAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (txtNome.getText().toString().equals("")){
                    Toast.makeText(
                            getContext(),
                            "Dados não Inseridos corretemente !\n Coloque ao menos o Nome",
                            Toast.LENGTH_LONG
                    ).show();
                }else{
                    Animal animal = new Animal();
                    animal.setNome(txtNome.getText().toString().trim());
                    animal.setRaca(txtRaca.getText().toString().trim());
                    animal.setCor(txtCor.getText().toString().trim());
                    animal.setId(databaseReference.push().getKey());
                    animal.setRelevance(10f);
                    pd.show();
                    databaseReference.child(user.getUid()).child("Animals").child(animal.getId()).setValue(animal)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    animalList.clear();
                                    getPreferencesDataAnimals(user.getUid());
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Animal Iserido", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Animal Não Iserido", Toast.LENGTH_SHORT).show();
                                }
                            });
                }

            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.create().show();

    }
    private void newObject(){
        databaseReference = FirebaseDatabase.getInstance().getReference("Preferences");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Novo Objeto: ");
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);


        EditText txtNome = new EditText(getActivity());
        txtNome.setHint("Nome do Objeto");
        EditText txtCor = new EditText(getActivity());
        txtCor.setHint("Cor do Objeto");


        linearLayout.addView(txtNome);
        linearLayout.addView(txtCor);

        builder.setView(linearLayout);

        builder.setPositiveButton("SALVAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (txtNome.getText().toString().equals("")){
                    Toast.makeText(
                            getContext(),
                            "Dados não Inseridos corretemente !\n Coloque ao menos o Nome",
                            Toast.LENGTH_LONG
                    ).show();
                }else{
                    Object object = new Object();
                    object.setNome(txtNome.getText().toString().trim());
                    object.setCor(txtCor.getText().toString().trim());
                    object.setId(databaseReference.push().getKey());
                    object.setRelevance(10f);
                    pd.show();
                    databaseReference.child(user.getUid()).child("Objects").child(object.getId()).setValue(object)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    objectsList.clear();
                                    getPreferencesDataObjects(user.getUid());
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Objeto Iserido", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Objeto Não Iserido", Toast.LENGTH_SHORT).show();
                                }
                            });
                }

            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.create().show();
    }

}