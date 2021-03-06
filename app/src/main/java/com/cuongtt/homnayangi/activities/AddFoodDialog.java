package com.cuongtt.homnayangi.activities;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.cuongtt.homnayangi.R;
import com.cuongtt.homnayangi.models.Ingredients;
import com.cuongtt.homnayangi.models.RecipesIngredients;
import com.cuongtt.homnayangi.network.APIService;
import com.cuongtt.homnayangi.network.ApiUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

public class AddFoodDialog extends AppCompatDialogFragment {

    ArrayList<Ingredients> ListIngredients = new ArrayList<Ingredients>();
    Spinner spinnerIngredients, spinnerUnit;
    RecipesIngredients recipe_ingredients;
    EditText edtAmount;
    OnMyDialogResult mDialogResult;
    AlertDialog dialog;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInnstanceState) {
        AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_layout_add_recipe_ingredient, null);


        SharedPreferences prefs = getContext().getSharedPreferences("MyUserInfo", MODE_PRIVATE);
        final String UserID = prefs.getString("UserId", "No id defined");
        String email = prefs.getString("UserEmail", "No email defined");
        String fullName = prefs.getString("UserFullName", "No name defined");

        Log.d("DEBUGGGGGGGG", "Dialog - get preference - USERINFO ("+UserID+", "+email+","+fullName+")");


        fetchAllIngredients();
        spinnerIngredients = view.findViewById(R.id.spinnerSearch);
        spinnerUnit = view.findViewById(R.id.unit_spinner);

        recipe_ingredients = new RecipesIngredients();


        spinnerIngredients.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, ListIngredients));

        spinnerIngredients.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                Ingredients itemSelected = (Ingredients) adapterView.getItemAtPosition(i);

                Log.d("DEBUGGGGG"," New Recipe Activity - dialog - search ingredients - "  + itemSelected.toString());

                recipe_ingredients.setIngredient(itemSelected);

                spinnerUnit.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, itemSelected.getPossible_unit()));
                spinnerUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        String unitSelected = adapterView.getItemAtPosition(i).toString();
                        recipe_ingredients.setUnit(unitSelected);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        edtAmount = view.findViewById(R.id.editTextAmount);

        builder.setView(view).setTitle("Thêm thực phẩm vào kho")
                .setNegativeButton("Huỷ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("Thêm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String amount = edtAmount.getText().toString();
                        Double parse = Double.parseDouble(amount);
                        recipe_ingredients.setAmount(Float.parseFloat(parse.toString() ));
                        System.out.println(recipe_ingredients.getIngredient().getId());
                        System.out.println(recipe_ingredients.toString());

                        Gson gson = new Gson();
                        String json = gson.toJson(recipe_ingredients);


                        addFoodToContainer(UserID, recipe_ingredients.getIngredient().getId(), Float.parseFloat(amount), recipe_ingredients.getUnit(), "None" );

                    }
                });


        return builder.create();
    }

    public void setDialogResult(OnMyDialogResult dialogResult){
        mDialogResult = dialogResult;
    }

    public interface OnMyDialogResult{
        void finish(String result);
    }

    APIService mAPIService = ApiUtils.getAPIService();

    public void fetchAllIngredients(){
        mAPIService.getIngrdientsList().enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if(response.isSuccessful()){
                    Gson gson = new Gson();

                    Type IngredientsListType  = new TypeToken<ArrayList<Ingredients>>(){}.getType();

                    ArrayList<Ingredients> items = gson.fromJson(response.body().toString(), IngredientsListType);

                    ListIngredients.clear();
                    ListIngredients.addAll(items);
                }

            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable throwable) {

            }
        });

    }

    public void addFoodToContainer(String userID, String foodID, float amount, String unit, String note){

        JsonObject objBody = new JsonObject();

        objBody.addProperty("user", userID);
        objBody.addProperty("food", foodID);
        objBody.addProperty("amount", amount);
        objBody.addProperty("unit", unit);
        objBody.addProperty("note", "none");

        mAPIService.addFoodToContainer(userID, objBody).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if(response.isSuccessful()){
                    Log.d("DEBUGGGGGGG", "Add food success");
                    mDialogResult.finish("success");
                    AddFoodDialog.this.dismiss();
                }else{
                    Log.d("DEBUGGGGGGG", "Add food failed - "+response.message());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable throwable) {
                Log.d("DEBUGGGGGGG", "Add food api call failed - "+throwable.getMessage());
            }
        });
    }

}
