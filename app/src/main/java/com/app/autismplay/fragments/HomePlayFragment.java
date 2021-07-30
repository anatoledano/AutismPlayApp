package com.app.autismplay.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.autismplay.MainActivity;
import com.app.autismplay.R;
import com.app.autismplay.activity.PlayVideoActivity;
import com.app.autismplay.adapter.VideoAdapter;
import com.app.autismplay.helper.ConfigurationFirebase;
import com.app.autismplay.helper.ConfigurationProgressDialog;
import com.app.autismplay.helper.ConfigurationRetrofit;
import com.app.autismplay.helper.ConfigurationYoutube;
import com.app.autismplay.helper.ConfigurationsConstants;
import com.app.autismplay.helper.RecyclerItemClickListener;
import com.app.autismplay.models.Animal;
import com.app.autismplay.models.Object;
import com.app.autismplay.models.User;
import com.app.autismplay.models.VideoUser;
import com.app.autismplay.responseyoutube.Item;
import com.app.autismplay.responseyoutube.Result;
import com.app.autismplay.models.Video;
import com.app.autismplay.services.YoutubeService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.security.acl.LastOwnerException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class HomePlayFragment extends Fragment {

    //private SearchView searchView;
    private TextView txtSair;
    private RecyclerView recyclerVideo;
    private List<Item> itemsYoutubeRequestList = new ArrayList<>();
    private VideoAdapter videoAdapter;
    private Retrofit retrofit;
    private ProgressDialog pd;
    private DatabaseReference databaseReference;
    private String query = "";
    private int resultNum = 1;
    private int counter = 0;

    public HomePlayFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_play, container, false);

        FirebaseAuth auth = ConfigurationFirebase.getReferenceAutentication();

        recyclerVideo = view.findViewById(R.id.recyclerVideo);
        txtSair = view.findViewById(R.id.txtSair);
        txtSair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                startActivity(new Intent(getContext(), MainActivity.class));
                getActivity().finish();
            }
        });
        //searchView = view.findViewById(R.id.searchViewYoutube);

        pd = ConfigurationProgressDialog.getProgressDialog(getContext());
        databaseReference = FirebaseDatabase.getInstance().getReference("Preferences");
        retrofit = ConfigurationRetrofit.getRetrofit();

        getVideoList();
        return view;
    }

    private void getVideoList() {
        pd.show();
        Random random = new Random();
        //GERAR RANDOMICAMENTE A STRING OBJETO OU ANIMAL
        final String optionRandowNode = ConfigurationsConstants.getNodePreferencesOptionsRandow();
        final String userId = ConfigurationFirebase.getReferenceAutentication().getCurrentUser().getUid();
        //NUMERO DE RESULTADOS


        ArrayList<Animal> animalArrayList = new ArrayList<>();
        ArrayList<Object> objectArrayList = new ArrayList<>();

        databaseReference = FirebaseDatabase.getInstance().getReference("Preferences").child(userId).child(optionRandowNode);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                counter = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    counter++;
                }
                resultNum = counter;
                if(counter >=5) resultNum = 5;
                Log.wtf("NUMERO DE RESULTADOS",""+resultNum);

                //PESQUISAR POR RELEVANCIA OS PRIMEIROS N* VALORES
                databaseReference = FirebaseDatabase.getInstance().getReference(ConfigurationsConstants.NODE_PREFERENCES).child(userId).child(optionRandowNode);
                Query queryDb = databaseReference.orderByChild("relevance").limitToFirst(resultNum);
                queryDb.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            if (optionRandowNode == ConfigurationsConstants.NODE_ANIMALS) {
                                Animal animal = ds.getValue(Animal.class);
                                animalArrayList.add(animal);
                            }
                            if (optionRandowNode == ConfigurationsConstants.NODE_OBJECTS) {
                                Object object = ds.getValue(Object.class);
                                objectArrayList.add(object);
                            }
                        }

                        int chosenValue = random.nextInt(resultNum );

                        Log.wtf("ESCOLHEU O ITEM DE IDENTIFICAO","valor:"+chosenValue);

                        if (optionRandowNode == ConfigurationsConstants.NODE_ANIMALS) {
                            query = generateAnimalQuery(animalArrayList.get(chosenValue));
                        }
                        if(optionRandowNode == ConfigurationsConstants.NODE_OBJECTS)
                            query = generateObjectQuery(objectArrayList.get(chosenValue));

                        Log.wtf("QUERY GERADA",query);
                        YoutubeService youtubeService = retrofit.create(YoutubeService.class);
                        youtubeService.recurperarVideos(
                                "snippet",
                                "relevance",
                                "10",
                                ConfigurationYoutube.CHAVE_YOUTUBE_API,
                                "",
                                query+"+infantil",
                                "strict",
                                "BR"

                        ).enqueue(new Callback<Result>() {
                            @Override
                            public void onResponse(Call<Result> call, Response<Result> response) {

                                if (response.isSuccessful()) {
                                    Result result = response.body();
                                    itemsYoutubeRequestList = result.items;
                                    //ADICIONAR SISTEMA DE RECOMENDACAO
                                    getVideoRecomendation(userId);
                                }else{
                                    pd.dismiss();
                                    if(response.code()==403){
                                        Toast.makeText(
                                                getContext(),
                                                "Erro na Chave de Api:\n Cota excedida",
                                                Toast.LENGTH_LONG
                                        ).show();
                                    }
                                    Toast.makeText(
                                            getContext(),
                                            "Erro na Chave de Api: "+
                                                    response.toString(),
                                            Toast.LENGTH_LONG
                                    ).show();
                                    Log.wtf("ERR0 NA LISTAGEM DE PREFERENCIAS",response.toString());
                                }
                            }

                            @Override
                            public void onFailure(Call<Result> call, Throwable t) {
                                pd.dismiss();
                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {
                        pd.dismiss();
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                pd.dismiss();
            }
        });


    }

    public void configRecycleView() {
        recyclerVideo.setHasFixedSize(true);
        recyclerVideo.setLayoutManager(new LinearLayoutManager((getContext())));
        videoAdapter = new VideoAdapter(itemsYoutubeRequestList, getContext());
        recyclerVideo.setAdapter(videoAdapter);

        //CONFIGURAÇÃO DE EVENTO DE CLICK
        recyclerVideo.addOnItemTouchListener(new RecyclerItemClickListener(
                getContext(),
                recyclerVideo,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Item video = itemsYoutubeRequestList.get(position);
                        String idVideo = video.id.videoId;
                        startActivity(
                                new Intent(getContext(), PlayVideoActivity.class)
                                        .putExtra("idVideo", idVideo)
                        );
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
                }
        ));
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

    private void getVideoRecomendation(String userId){
        Random random = new Random();
        counter = 0;
        resultNum = 1;
        ArrayList<VideoUser> videosPreSelect = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference(ConfigurationsConstants.NODE_RECOMENDATIONS).child(userId);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot ds:snapshot.getChildren() ){
                    counter ++;
                }
                resultNum = counter;
                if(counter >=5) resultNum = 5;
                Query queryDb = databaseReference
                        .orderByChild("resultRelevance")
                        .limitToFirst(resultNum);




                queryDb.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for (DataSnapshot ds:snapshot.getChildren() ){
                            VideoUser videoUserRecoemendation = ds.getValue(VideoUser.class);
                            videosPreSelect.add(videoUserRecoemendation);
                        }
                        int chosenValue = random.nextInt(resultNum);

                        recomendationVideoList(videosPreSelect.get(chosenValue).getVideoId());
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {
                        pd.dismiss();
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }
    private void recomendationVideoList(String idVideo){

        YoutubeService youtubeService = retrofit.create(YoutubeService.class);
        youtubeService.recurperarVideosrRelacionados(
                "snippet",
                "relevance",
                "10",
                ConfigurationYoutube.CHAVE_YOUTUBE_API,
                "strict",
                "BR",
                idVideo,
                "video"

        ).enqueue(new Callback<Result>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                if (response.isSuccessful()) {
                    Result result = response.body();
                    itemsYoutubeRequestList.forEach(item -> {
                        result.items.add(item);
                    });
                    itemsYoutubeRequestList = result.items;
                    pd.dismiss();
                    configRecycleView();

                }else{
                    pd.dismiss();
                    Toast.makeText(
                            getContext(),
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
}