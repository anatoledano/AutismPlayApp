package com.app.autismplay.helper;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ConfigurationFirebase {

    private static DatabaseReference referenceFirebase;
    private static FirebaseAuth referenceAutentication;

    public static DatabaseReference getFirebase(){
        if(referenceFirebase==null){
            referenceFirebase = FirebaseDatabase.getInstance().getReference();
        }
        return referenceFirebase;
    }

    public static FirebaseAuth getReferenceAutentication(){
        if(referenceAutentication==null){
            referenceAutentication = FirebaseAuth.getInstance();
        }
        return referenceAutentication;
    }
}
