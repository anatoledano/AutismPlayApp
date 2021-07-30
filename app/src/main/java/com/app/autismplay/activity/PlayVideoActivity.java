package com.app.autismplay.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.autismplay.R;
import com.app.autismplay.helper.ConfigurationFirebase;
import com.app.autismplay.helper.ConfigurationYoutube;
import com.app.autismplay.helper.ConfigurationsConstants;
import com.app.autismplay.models.VideoUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class PlayVideoActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {

    private String idVideo;

    private YouTubePlayerView youTubePlayerView;

    private FirebaseUser user;
    private DatabaseReference databaseReference;

    private ImageView imgLike,imgDesLike;
    private TextView like,deslike;

    private VideoUser video=null;

    private boolean wasEvaluated = false;
    private int statusLike=0;
    private int counter =0;


    //DETECTAR AÇÕES DO PLAYER
    private  YouTubePlayer.PlaybackEventListener playbackEventListener;

    //AÇÕES DE CARREGAMENTO
    private YouTubePlayer.PlayerStateChangeListener playerStateChangeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_video);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            idVideo = bundle.getString("idVideo");
        }else{
            //VIDEO PADRÃO QUANDO DÁ ERRO
            idVideo = "xFxvRgoQJBM";
        }
        statusLike = 0;
        imgDesLike = findViewById(R.id.imgBtnDesLike);
        imgLike = findViewById(R.id.imgBtnLike);
        like = findViewById(R.id.textViewLike);
        deslike = findViewById(R.id.textViewDeslike);

        youTubePlayerView = findViewById(R.id.viewYoutubePlayer);
        youTubePlayerView.initialize(
                ConfigurationYoutube.CHAVE_YOUTUBE_API,
                this
        );

        imgLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(statusLike!=2){
                    if(video==null && statusLike ==0){
                        likeAction();
                    }else if(video!=null && statusLike == -1){
                        updateLikeAction(2);
                    }

                }

            }
        });
        imgDesLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(statusLike!=1){
                    if(video==null && statusLike ==0){
                        deslikeAction();
                    }else if(video!=null && statusLike == 2){
                        updateLikeAction(-1);
                    }

                }

            }
        });


        user = ConfigurationFirebase.getReferenceAutentication().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference(ConfigurationsConstants.NODE_RECOMENDATIONS).child(user.getUid());
        Query query = databaseReference.orderByChild("videoId").equalTo(idVideo).limitToFirst(1);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot ds:snapshot.getChildren() ){
                    if (ds.child("videoId").exists()) {
                        video = ds.getValue(VideoUser.class);
                        if(video.getRating()==2){
                            likeInteracion();
                            wasEvaluated = true;
                        }else if(video.getRating()==-1){
                            deslikeInteracion();
                            wasEvaluated = true;
                        }
                    } else {
                        video = null;
                    }
                }
                if(video!=null){
                    counter++;
                    if(counter<=1){
                        video.setNumberViewsByUser(video.getNumberViewsByUser()+1);
                        video.setResultRelevance();
                        video.setTimeView(video.getTimeView()+100);
                        HashMap<String,Object> result = new HashMap<>();
                        result.put("numberViewsByUser",video.getNumberViewsByUser());
                        result.put("resultRelevance",video.getResultRelevance());
                        result.put("timeView",video.getTimeView());

                        databaseReference = FirebaseDatabase.getInstance().getReference(ConfigurationsConstants.NODE_RECOMENDATIONS).child(user.getUid()).child(video.getId());
                        databaseReference.updateChildren(result);
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });




        playbackEventListener = new YouTubePlayer.PlaybackEventListener() {
            @Override
            public void onPlaying() {

            }

            @Override
            public void onPaused() {

            }

            @Override
            public void onStopped() {

            }

            @Override
            public void onBuffering(boolean b) {

            }

            @Override
            public void onSeekTo(int i) {

            }
        };

        playerStateChangeListener = new YouTubePlayer.PlayerStateChangeListener() {
            @Override
            public void onLoading() {

            }

            @Override
            public void onLoaded(String s) {

            }

            @Override
            public void onAdStarted() {

            }

            @Override
            public void onVideoStarted() {

            }

            @Override
            public void onVideoEnded() {

            }

            @Override
            public void onError(YouTubePlayer.ErrorReason errorReason) {

            }
        };
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {

        youTubePlayer.setPlaybackEventListener(playbackEventListener);
        youTubePlayer.setPlayerStateChangeListener(playerStateChangeListener);

        if(!wasRestored){
            youTubePlayer.cueVideo(idVideo);
        }

    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        Toast.makeText(this,
                "ERRO AO INICIAR O PLAYER: \n"+youTubeInitializationResult.toString(),
                Toast.LENGTH_LONG
                ).show();
    }

    private void likeAction(){
        if(video == null){
            likeInteracion();
            databaseReference = FirebaseDatabase.getInstance().getReference(ConfigurationsConstants.NODE_RECOMENDATIONS).child(user.getUid());
            VideoUser videoUser = new VideoUser();
            videoUser.setNumberViewsByUser(1);
            videoUser.setTimeView(1000);
            videoUser.setRating(2);
            videoUser.setVideoId(idVideo);
            videoUser.setResultRelevance();

            videoUser.setId(databaseReference.push().getKey());
            databaseReference.child(videoUser.getId()).setValue(videoUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(PlayVideoActivity.this, "Você deu sua nota!", Toast.LENGTH_LONG).show();
                        wasEvaluated = true;
                    }else{
                        Toast.makeText(PlayVideoActivity.this, "Erro, não foi possivel dar sua nota!", Toast.LENGTH_LONG).show();
                        wasEvaluated = false;
                    }
                }
            });


        }
    }
    private void deslikeAction(){
        if(video == null){
            deslikeInteracion();
            databaseReference = FirebaseDatabase.getInstance().getReference(ConfigurationsConstants.NODE_RECOMENDATIONS).child(user.getUid());
            VideoUser videoUser = new VideoUser();
            videoUser.setNumberViewsByUser(1);
            videoUser.setTimeView(50);
            videoUser.setRating(-1);
            videoUser.setVideoId(idVideo);
            videoUser.setResultRelevance();
            videoUser.setId(databaseReference.push().getKey());
            databaseReference.child(videoUser.getId()).setValue(videoUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(PlayVideoActivity.this, "Você deu sua avaliação!", Toast.LENGTH_LONG).show();
                        wasEvaluated = true;
                    }else{
                        Toast.makeText(PlayVideoActivity.this, "Erro, não foi possivel dar sua avaliação!", Toast.LENGTH_LONG).show();
                        wasEvaluated = false;
                    }
                }
            });


        }
    }

    private void likeInteracion(){
        statusLike = 2;
        like.setTextColor(Color.YELLOW);
        deslike.setTextColor(Color.GRAY);

    }
    private void deslikeInteracion(){
        statusLike = -1;
        deslike.setTextColor(Color.RED);
        like.setTextColor(Color.GRAY);
    }

    private void updateLikeAction(int rating){
        HashMap<String,Object> result = new HashMap<>();
        video.setRating(rating);
        video.setResultRelevance();
        result.put("rating",rating);
        result.put("resultRelevance",video.getResultRelevance());
        databaseReference = FirebaseDatabase.getInstance().getReference(ConfigurationsConstants.NODE_RECOMENDATIONS).child(user.getUid()).child(video.getId());
        databaseReference.updateChildren(result).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(PlayVideoActivity.this, "Mudou de Ideia ?!\n Sua nova avaliação foi salva", Toast.LENGTH_LONG).show();
            }
        });

    }
}