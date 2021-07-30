package com.app.autismplay.adapter;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.autismplay.R;
import com.app.autismplay.activity.HomeActivity;
import com.app.autismplay.helper.ConfigurationFirebase;
import com.app.autismplay.helper.ConfigurationProgressDialog;
import com.app.autismplay.helper.ConfigurationsConstants;
import com.app.autismplay.models.Animal;
import com.app.autismplay.responseyoutube.Item;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AnimalsAdapter extends RecyclerView.Adapter<AnimalsAdapter.MyViewHolder> {

    private Context context;
    private List<Animal> animals = new ArrayList<>();
    private DatabaseReference databaseReference;

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_animals,parent,false);
        return new AnimalsAdapter.MyViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {

        Animal animal = animals.get(position);
        if(!animal.getCor().isEmpty()){
            holder.cor.setText(animal.getCor());
        }else{
            holder.cor.setText("Não Inserido");
        }

        if(!animal.getRaca().isEmpty()){
            holder.raca.setText(animal.getRaca());
        }else{
            holder.raca.setText("Não Inserido");
        }

        if(!animal.getNome().isEmpty()){
            holder.nome.setText(animal.getNome());
        }else{
            holder.nome.setText("Não Inserido");
        }
        holder.imgActionDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builderDelete = new AlertDialog.Builder(context);
                builderDelete.setTitle("Deseja excluir esse item?");
                builderDelete.setPositiveButton("SIM", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteItem(animal.getId());
                    }
                });
                builderDelete.setNegativeButton("NÃO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builderDelete.create().show();

            }
        });

        holder.imgActionEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builderEdit = new AlertDialog.Builder(context);
                builderEdit.setTitle("Animal:");
                LinearLayout linearLayout = new LinearLayout(((Activity) context));
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                linearLayout.setPadding(10,10,10,10);

                EditText txtNome = new EditText(((Activity) context));
                txtNome.setText(animal.getNome());
                txtNome.setHint("Nome do Animal");
                EditText txtCor = new EditText(((Activity) context));
                txtCor.setHint("Cor do Animal");
                txtCor.setText(animal.getCor());
                EditText txtRaca = new EditText(((Activity) context));
                txtRaca.setHint("Raça do Animal");
                txtRaca.setText(animal.getRaca());

                linearLayout.addView(txtNome);
                linearLayout.addView(txtCor);
                linearLayout.addView(txtRaca);

                builderEdit.setView(linearLayout);

                builderEdit.setPositiveButton("SALVAR", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (txtNome.getText().toString().equals("")){
                            Toast.makeText(
                                    ((Activity) context),
                                    "Dados não Inseridos corretemente !\n Coloque ao menos o Nome",
                                    Toast.LENGTH_LONG
                            ).show();
                        }else{
                            updateItem(animal.getId(),txtNome.getText().toString().trim(),txtCor.getText().toString().trim(),txtRaca.getText().toString().trim());
                        }

                    }
                });

                builderEdit.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                builderEdit.create().show();


            }
        });


    }
    @Override
    public int getItemCount() {
        return animals.size();
    }


    public AnimalsAdapter(List<Animal> animals, Context context) {
        this.animals = animals;
        this.context = context;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView nome,cor,raca;
        private ImageView imgActionDelete,imgActionEdit;


        public MyViewHolder(View itemView){
            super(itemView);
            nome = itemView.findViewById(R.id.txtNamAnim);
            cor = itemView.findViewById(R.id.txtCorAnim);
            raca = itemView.findViewById(R.id.txtRacaAnim);
            imgActionDelete = itemView.findViewById(R.id.btnDeleteAnim);
            imgActionEdit = itemView.findViewById(R.id.btnEditAnim);

        }
    }

    private void deleteItem(String id){
        ProgressDialog pd = ConfigurationProgressDialog.getProgressDialog(context);
        pd.show();
        String userId = ConfigurationFirebase.getReferenceAutentication().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("Preferences").child(userId).child("Animals");
        databaseReference.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                databaseReference.child(id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context,"Animal excluido com sucesso !",Toast.LENGTH_LONG).show();
                        pd.dismiss();
                        context.startActivity(new Intent(context, HomeActivity.class));
                        ((Activity)context).finish();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                pd.dismiss();
            }
        });
    }

    private void updateItem(String id,String nome,String cor,String raca){
        HashMap<String, Object> result = new HashMap<>();
        result.put("nome",nome);
        result.put("cor",cor);
        result.put("raca",raca);
        databaseReference = FirebaseDatabase.getInstance()
                .getReference(ConfigurationsConstants.NODE_PREFERENCES)
                .child(ConfigurationFirebase.getReferenceAutentication().getCurrentUser().getUid())
                .child(ConfigurationsConstants.NODE_ANIMALS)
                .child(id);
        databaseReference.updateChildren(result).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(context, "Seus dados Foram Alterados !!", Toast.LENGTH_LONG).show();
                context.startActivity(new Intent(context, HomeActivity.class));
                ((Activity)context).finish();

            }
        });


    }


}
