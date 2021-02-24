package com.hms.demo.hmscoursedemoa.ui.iap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.hms.demo.hmscoursedemoa.databinding.ProductBinding;
import com.huawei.hms.iap.entity.ProductInfo;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductVH> {
    private List<ProductInfo> products;

    private final ProductItemListener listener;
    public ProductAdapter(ProductItemListener listener){
        this.listener=listener;
    }

    public void setProducts(List<ProductInfo> products) {
        this.products = products;
    }

    @NonNull
    @Override
    public ProductVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(parent.getContext());
        ProductBinding binding=ProductBinding.inflate(inflater,parent,false);
        return new ProductVH(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductVH holder, int position) {
        holder.bind(products.get(position));
    }

    @Override
    public int getItemCount() {
        if(products!=null){
            return products.size();
        } return 0;
    }

    class ProductVH extends RecyclerView.ViewHolder{
        private final ProductBinding binding;
        public ProductVH(ProductBinding binding) {
            super(binding.getRoot());
            this.binding=binding;
        }

        public void bind(ProductInfo item){
            binding.setListener(listener);
            binding.setItem(item);
        }
    }

    public interface ProductItemListener{
        void onItemClick(ProductInfo item);
    }
}
