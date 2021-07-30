package com.app.autismplay.helper;

import android.app.ProgressDialog;
import android.content.Context;

public class ConfigurationProgressDialog {

    public static ProgressDialog getProgressDialog(Context context){
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("CARREGANDO ");
        progressDialog.setMessage("Aguarde um momento ...");
        progressDialog.setCancelable(false);
        return  progressDialog;
    }
}
