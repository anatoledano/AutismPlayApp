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
import com.app.autismplay.activity.PlayVideoActivity;
import com.app.autismplay.helper.ConfigurationFirebase;
import com.app.autismplay.helper.ConfigurationProgressDialog;
import com.app.autismplay.helper.ConfigurationsConstants;
import com.app.autismplay.models.Animal;
import com.app.autismplay.models.Object;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ObjectsAdapter  extends RecyclerView.Adapter<ObjectsAdapter.MyViewHolder> {

    private Context context;
    private List<Object> objects = new ArrayList<>();
    private DatabaseReference databaseReference;

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_objects,parent,false);
        return new ObjectsAdapter.MyViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {

        Object object = objects.get(position);
        if(!object.getCor().isEmpty()){
            holder.cor.setText(object.getCor());
        }else{
            holder.cor.setText("Não Inserido");
        }

        if(!object.getNome().isEmpty()){
            holder.nome.setText(object.getNome());
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
                        deleteItem(object.getId());
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
                builderEdit.setTitle("Edite Objeto:");
                LinearLayout linearLayout = new LinearLayout(((Activity) context));
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                linearLayout.setPadding(10,10,10,10);

                EditText txtNome = new EditText(((Activity) context));
                txtNome.setText(object.getNome());
                txtNome.setHint("Nome do Objeto");
                EditText txtCor = new EditText(((Activity) context));
                txtCor.setHint("Cor do Objeto");
                txtCor.setText(object.getCor());

                linearLayout.addView(txtNome);
                linearLayout.addView(txtCor);

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
                            updateItem(object.getId(),txtNome.getText().toString().trim(),txtCor.getText().toString().trim());
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
        return objects.size();
    }


    public ObjectsAdapter(List<Object> obj, Context context) {
        this.objects = obj;
        this.context = context;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView nome,cor;
        private ImageView imgActionDelete,imgActionEdit;


        public MyViewHolder(View itemView){
            super(itemView);
            nome = itemView.findViewById(R.id.txtNamObjs);
            cor = itemView.findViewById(R.id.txtCorObj);
            imgActionDelete = itemView.findViewById(R.id.btnDeleteObj);
            imgActionEdit = itemView.findViewById(R.id.btnEditObj);
        }
    }
    private void deleteItem(String id){
        ProgressDialog pd = ConfigurationProgressDialog.getProgressDialog(context);
        pd.show();
        String userId = ConfigurationFirebase.getReferenceAutentication().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("Preferences").child(userId).child("Objects");
        databaseReference.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                databaseReference.child(id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        pd.dismiss();
                        Toast.makeText(context,"Objeto excluido com sucesso !",Toast.LENGTH_LONG).show();
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

    private void updateItem(String id,String nome,String cor){
        HashMap<String, java.lang.Object> result = new HashMap<>();
        result.put("nome",nome);
        result.put("cor",cor);
        databaseReference = FirebaseDatabase.getInstance()
                .getReference(ConfigurationsConstants.NODE_PREFERENCES)
                .child(ConfigurationFirebase.getReferenceAutentication().getCurrentUser().getUid())
                .child(ConfigurationsConstants.NODE_OBJECTS)
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
