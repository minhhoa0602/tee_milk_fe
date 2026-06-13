package com.example.myapplication.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.model.Address;

import java.util.ArrayList;
import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private List<Address> addressList = new ArrayList<>();

    public AddressAdapter(List<Address> addressList) {
        this.addressList = addressList != null ? addressList : new ArrayList<>();
    }

    public void setAddressList(List<Address> newList) {
        if (newList == null) newList = new ArrayList<>();
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            final List<Address> old = addressList;
            final List<Address> next = newList;

            @Override public int getOldListSize() { return old.size(); }
            @Override public int getNewListSize() { return next.size(); }

            @Override
            public boolean areItemsTheSame(int oldPos, int newPos) {
                return old.get(oldPos).getId() == next.get(newPos).getId();
            }

            @Override
            public boolean areContentsTheSame(int oldPos, int newPos) {
                Address a = old.get(oldPos), b = next.get(newPos);
                return a.isDefault() == b.isDefault() &&
                        strEq(a.getAddressLine(), b.getAddressLine());
            }

            private boolean strEq(String a, String b) {
                return a == null ? b == null : a.equals(b);
            }
        });
        addressList = newList;
        diff.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        Address address = addressList.get(position);
        String text = address.getAddressLine();
        if (address.isDefault()) text += " ✓ Mặc định";
        holder.tvAddress.setText(text);
    }

    @Override
    public int getItemCount() { return addressList.size(); }

    static class AddressViewHolder extends RecyclerView.ViewHolder {
        TextView tvAddress;
        AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAddress = itemView.findViewById(android.R.id.text1);
        }
    }
}
