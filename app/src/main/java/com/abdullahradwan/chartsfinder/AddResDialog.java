package com.abdullahradwan.chartsfinder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class AddResDialog extends DialogFragment {

    // Define type
    private String type;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Set builder
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Inflate the layout
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Set view
        View view = inflater.inflate(R.layout.dialog_addres, null);

        // Set variables
        final EditText orderEdit = view.findViewById(R.id.orderEdit);

        final EditText urlEdit = view.findViewById(R.id.urlEdit);

        final Spinner typeSpinner = view.findViewById(R.id.typeSpinner);

        final String[] types = new String[]{"Normal", "Folder"};

        // Set type spinner

        // Set adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, types);

        // Set spinner style
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Link spinner with its adapter
        typeSpinner.setAdapter(adapter);

        // On click listener
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Set type variable from types list
                type = types[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Set dialog
        builder.setView(view)
                // Add button on click
                .setPositiveButton(getResources().getString(R.string.add_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // Get order text
                        String orderText = orderEdit.getText().toString();

                        // Get URL
                        String url = urlEdit.getText().toString();

                        // If fields not empty
                        if(!orderText.equals("") && !url.equals("")){

                            try {

                                // Make order integer
                                int order = Integer.parseInt(orderText);

                                // if order is less than last item and bigger than -1
                                if (order < MainActivity.resources.size() && order >=0) {

                                    // Add resource method
                                    SettingsActivity.addRes(order, url, type);

                                    // Hide dialog
                                    AddResDialog.this.getDialog().cancel();

                                // If order is more than the list item or lower than 0
                                } else {
                                    Toast.makeText(getActivity(), getResources().getString(R.string.correctorder_message),
                                            Toast.LENGTH_SHORT).show();
                                }
                            // If order can't casted to integer
                            } catch (Exception ex){
                                Toast.makeText(getActivity(), getResources().getString(R.string.correctorder_message),
                                    Toast.LENGTH_SHORT).show();
                            }

                        // If one of the fields is empty
                        } else{Toast.makeText(getActivity(),getResources().getString(R.string.fillin_message), Toast.LENGTH_SHORT).show();}}})
                // Cancel button on click, will hide the dialog
                .setNegativeButton(getResources().getString(R.string.cancel_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {AddResDialog.this.getDialog().cancel();}});

        // Show dialog
        return builder.create();

    }
}
