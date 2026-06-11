package com.example.myapplication.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.model.Address;
import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private List<Address> addressList;

    public AddressAdapter(List<Address> addressList) {
        this.addressList = addressList;
    }

    public void setAddressList(List<Address> addressList) {
        this.addressList = addressList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        Address address = addressList.get(position);
        String text = address.getAddressLine();
        if (address.isDefault()) {
            text += " (Mặc định)";
        }
        holder.tvAddress.setText(text);
    }

    @Override
    public int getItemCount() {
        return addressList == null ? 0 : addressList.size();
    }

    static class AddressViewHolder extends RecyclerView.ViewHolder {
        TextView tvAddress;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAddress = itemView.findViewById(android.R.id.text1);
        }
    }
}
