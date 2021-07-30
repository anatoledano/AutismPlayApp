package com.app.autismplay.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.app.autismplay.MainActivity;
import com.app.autismplay.R;
import com.app.autismplay.adapter.VideoAdapter;
import com.app.autismplay.helper.ConfigurationFirebase;
import com.app.autismplay.helper.ConfigurationProgressDialog;
import com.app.autismplay.helper.ConfigurationRetrofit;
import com.app.autismplay.helper.ConfigurationYoutube;
import com.app.autismplay.helper.ConfigurationsConstants;
import com.app.autismplay.models.Animal;
import com.app.autismplay.models.Object;
import com.app.autismplay.models.User;
import com.app.autismplay.models.VideoUser;
import com.app.autismplay.responseyoutube.Item;
import com.app.autismplay.responseyoutube.Result;
import com.app.autismplay.services.YoutubeService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class RegisterActivity extends AppCompatActivity {


    private EditText txtNameParent,txtNameKid,txtAge,txtEMail,txtPassword;
    private EditText nameA,colorA,racaA;
    private EditText nameO,colorO;
    private Spinner spinnerSex;

    private String animalRecomendation,objectRecomendation;

    private ProgressDialog pd;

    private DatabaseReference db;
    private FirebaseAuth mAuth;


    private Retrofit retrofit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        setContentView(R.layout.activity_register);

        //BINDING DATA USER
        spinnerSex = findViewById(R.id.SpinnerSex);
        txtAge = findViewById(R.id.txtAge);
        txtEMail = findViewById(R.id.txtEma);
        txtNameKid = findViewById(R.id.txtNamChi);
        txtNameParent = findViewById(R.id.txtNamPar);
        txtPassword = findViewById(R.id.txtPas);

        //BINDING DATA ANIMAIL PREF
        nameA = findViewById(R.id.txtNomeAnimal);
        colorA = findViewById(R.id.txtCorAnimal);
        racaA = findViewById(R.id.txtRacaAniimal);
        //BINDING DATA OBJECT PREF
        nameO = findViewById(R.id.txtNomeObjeto);
        colorO = findViewById(R.id.txtCorObjeto);

        //PROGRESS DIALOG CONFIG
        pd = ConfigurationProgressDialog.getProgressDialog(this);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.sex_select,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSex.setAdapter(adapter);

        //FIREBASE AUTH
        mAuth = ConfigurationFirebase.getReferenceAutentication();
        //FIREBASE DB
        db =FirebaseDatabase.getInstance().getReference("Users");
        //CONFIG RETROFIG
        retrofit = ConfigurationRetrofit.getRetrofit();
    }


    public void validRegistAndSave(View view){
        String email = txtEMail.getText().toString().trim();
        String age = txtAge.getText().toString().trim();
        String nameChild = txtNameKid.getText().toString().trim();
        String nameParent = txtNameParent.getText().toString().trim();
        String password = txtPassword.getText().toString().trim();
        String selectedValue = spinnerSex.getSelectedItem().toString();
        if(age.equals("")){
            age="0";
        }
        if(
                email.equals("")
                || Integer.parseInt(age)<1
                || nameChild == ""
                || nameParent == ""
                || password == ""
                || nameO.getText().toString().trim().equals("")
                || nameA.getText().toString().trim().equals("")
        ){
            String resultado = "INSIRA OS DADOS CORRETAMENTE !!";
            if(nameA.getText().toString().trim().equals("")){
                resultado+="\nInsira ao menos o Nome do Animal";
            }
            if(nameO.getText().toString().trim().equals("")){
                resultado+="\nInsira ao menos o Nome do Objeto";
            }
            if(password == ""){
                resultado+= "\nInsira a Senha";
            }
            if(nameParent == ""){
                resultado+="\nInsira o Nome do Responsavel";
            }
            if(nameChild == ""){
                resultado+="\nInsira o Nome da Criança";
            }
            if(Integer.parseInt(age)<1){
                resultado+="\nInsira a Idade Corretamente";
            }
            Toast.makeText(RegisterActivity.this,resultado,Toast.LENGTH_LONG).show();

        }else{
            User user = new User(null,nameChild,nameParent,Integer.parseInt(age),selectedValue,email);
            pd.show();
            mAuth.createUserWithEmailAndPassword(user.getEmail(),password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){

                        FirebaseUser newUser = mAuth.getCurrentUser();
                        user.setId(newUser.getUid());
                        db.child(user.getId()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(RegisterActivity.this,"Dados Registrados com Sucesso !!",Toast.LENGTH_LONG).show();
                                    createAnimalsAndObjects(user.getId());

                                }
                            }
                        });

                    }else{
                        pd.dismiss();
                        String erroExcecao = "";

                        try{
                            throw task.getException();
                        }catch (FirebaseAuthWeakPasswordException e){
                            erroExcecao = "Digite uma senha mais forte!";
                        }catch (FirebaseAuthInvalidCredentialsException e){
                            erroExcecao = "Por favor, digite um e-mail válido";
                        }catch (FirebaseAuthUserCollisionException e){
                            erroExcecao = "Este conta já foi cadastrada";
                        } catch (Exception e) {
                            erroExcecao = "ao cadastrar usuário: "  + e.getMessage();
                            e.printStackTrace();
                        }

                        Toast.makeText(RegisterActivity.this,erroExcecao,Toast.LENGTH_LONG).show();

                    }
                }
            });
        }

    }
    private void createAnimalsAndObjects(String userId){
        Animal anim = new Animal();

        anim.setNome(nameA.getText().toString().trim());
        anim.setCor(colorA.getText().toString().trim());
        anim.setRaca(racaA.getText().toString().trim());
        anim.setRelevance(9f);
        Object object = new Object();
        object.setNome(nameO.getText().toString().trim());
        object.setCor(colorO.getText().toString().trim());
        object.setRelevance(9f);

        db = FirebaseDatabase.getInstance().getReference(ConfigurationsConstants.NODE_PREFERENCES);

        anim.setId(db.push().getKey());
        object.setId(db.push().getKey());

        db.child(userId).child("Animals").child(anim.getId()).setValue(anim);
        db.child(userId).child("Objects").child(object.getId()).setValue(object);

        obterPrimeiroVideos(anim,object,userId);

    }

    public void returnMain(View view){
        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
        finish();
    }

    private void obterPrimeiroVideos(Animal animal,Object object,String userId){
        YoutubeService youtubeService = retrofit.create(YoutubeService.class);
        final String queryAnimal = generateAnimalQuery(animal);
        final String queryObject = generateObjectQuery(object);
        //BUSCA POR VIDEO QUE SEJA RECOMENDADO (ANIMAL)
        youtubeService.recurperarVideos(
                "id",
                "relevance",
                "1",
                ConfigurationYoutube.CHAVE_YOUTUBE_API,
                "",
                queryAnimal+"infantil",
                "strict",
                "BR"

        ).enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                if (response.isSuccessful()) {
                    pd.dismiss();
                    Result result = response.body();
                    animalRecomendation = result.items.get(0).id.videoId;
                    //BUSCA POR VIDEO QUE SEJA RECOMENDADO (OBJECT)
                    youtubeService.recurperarVideos(
                            "id",
                            "relevance",
                            "1",
                            ConfigurationYoutube.CHAVE_YOUTUBE_API,
                            "",
                            queryObject+"infantil",
                            "strict",
                            "BR"

                    ).enqueue(new Callback<Result>() {
                        @Override
                        public void onResponse(Call<Result> call, Response<Result> response) {

                            if (response.isSuccessful()) {
                                pd.dismiss();
                                Result result = response.body();
                                objectRecomendation = result.items.get(0).id.videoId;

                                createRecomendationsInitial(userId);

                            }else{
                                pd.dismiss();
                                Toast.makeText(
                                        RegisterActivity.this,
                                        "Erro na Chave de Api: "+
                                                response.toString(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Result> call, Throwable t) {
                            pd.dismiss();
                        }
                    });

                }else{
                    pd.dismiss();
                    Toast.makeText(
                            RegisterActivity.this,
                            "Erro na Chave de Api: "+
                                    response.toString(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                pd.dismiss();
            }
        });
    }

    private String generateAnimalQuery(Animal animal) {
        String q = "";
        if (!animal.getNome().isEmpty()) {
            q +=animal.getNome();
        }
        if (!animal.getCor().isEmpty()) {
            q +="+"+animal.getCor();
        }
        if (!animal.getRaca().isEmpty()) {
            q +="+"+animal.getRaca();
        }
        return q;
    }

    private String generateObjectQuery(Object obj) {
        String q = "";
        if (!obj.getNome().isEmpty()) {
            q += obj.getNome();
        }
        if (!obj.getCor().isEmpty()) {
            q += "+" + obj.getCor();
        }
        return q;
    }


    private void createRecomendationsInitial(String userId){

        db = FirebaseDatabase.getInstance().getReference(ConfigurationsConstants.NODE_RECOMENDATIONS);
        VideoUser videoUserAnimal = new VideoUser();
        VideoUser videoUserObjects = new VideoUser();

        videoUserAnimal.setId(db.push().getKey());
        videoUserObjects.setId(db.push().getKey());

        videoUserAnimal.setVideoId(animalRecomendation);
        videoUserObjects.setVideoId(objectRecomendation);

        videoUserAnimal.setRating(0);
        videoUserObjects.setRating(0);
        videoUserAnimal.setTimeView(100);
        videoUserObjects.setTimeView(100);
        videoUserAnimal.setNumberViewsByUser(1);
        videoUserObjects.setNumberViewsByUser(1);
        videoUserAnimal.setResultRelevance();
        videoUserObjects.setResultRelevance();

        db.child(userId).child(videoUserAnimal.getId()).setValue(videoUserAnimal);
        db.child(userId).child(videoUserObjects.getId()).setValue(videoUserObjects);

        pd.dismiss();
        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
        finish();
    }

}