package com.abdullahradwan.chartsfinder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class AddResDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_addres, null);

        final EditText orderEdit = view.findViewById(R.id.orderEdit);

        final EditText urlEdit = view.findViewById(R.id.urlEdit);

        builder.setView(view)
                .setPositiveButton(getResources().getString(R.string.add_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String order = orderEdit.getText().toString();

                        String url = urlEdit.getText().toString();

                        if(!order.equals("") && !url.equals("")){

                            int orderInt = Integer.parseInt(order);

                            if(orderInt < MainActivity.resources.size()) {

                                SettingsActivity.addRes(Integer.parseInt(orderEdit.getText().toString()), urlEdit.getText().toString());

                                AddResDialog.this.getDialog().cancel();

                            }

                            else{Toast.makeText(getActivity(),getResources().getString(R.string.correctorder_message), Toast.LENGTH_SHORT).show();}

                        }

                        else{Toast.makeText(getActivity(),getResources().getString(R.string.fillin_message), Toast.LENGTH_SHORT).show();}}})
                .setNegativeButton(getResources().getString(R.string.cancel_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {AddResDialog.this.getDialog().cancel();}});

        return builder.create();

    }
}
