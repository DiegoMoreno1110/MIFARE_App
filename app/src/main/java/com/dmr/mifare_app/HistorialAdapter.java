package com.dmr.mifare_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

    public class HistorialAdapter extends RecyclerView.Adapter<HistorialAdapter.ViewHolder> {

        private Context mCtx;
        private List<Historial> historialList;

        public HistorialAdapter(Context mCtx, List<Historial> historialList) {
            this.mCtx = mCtx;
            this.historialList = historialList;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            // Create a new view, which defines the UI of the list item
            LayoutInflater inflater = LayoutInflater.from(mCtx);
            View view = inflater.inflate(R.layout.historial_list_layout, viewGroup, false);

            return new ViewHolder(view);
        }


        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder viewHolder, final int position) {

            Historial historial = historialList.get(position);

            viewHolder.textViewPrecio.setText("$" + String.valueOf(historial.getPrecio()));
            viewHolder.textViewMovimiento.setText(historial.getMovimiento());
            viewHolder.textViewFecha.setText(historial.getFecha());
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return historialList.size();
        }

        /**
         * Provide a reference to the type of views that you are using
         * (custom ViewHolder).
         */
        public static class ViewHolder extends RecyclerView.ViewHolder {

            TextView textViewPrecio;
            TextView textViewMovimiento;
            TextView textViewFecha;

            public ViewHolder(View view) {
                super(view);
                // Define click listener for the ViewHolder's View

                textViewPrecio = (TextView) view.findViewById(R.id.textViewCantidad);
                textViewMovimiento = (TextView) view.findViewById(R.id.textViewMovimiento);
                textViewFecha = (TextView) view.findViewById(R.id.textViewFecha);

            }

        }

    }


