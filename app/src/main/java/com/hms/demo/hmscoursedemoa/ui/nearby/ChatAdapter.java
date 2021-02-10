package com.hms.demo.hmscoursedemoa.ui.nearby;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.hms.demo.hmscoursedemoa.R;
import com.hms.demo.hmscoursedemoa.databinding.MessageBinding;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatVH> {

    private ArrayList<MessageBean> items= new ArrayList();

    public void setItems(ArrayList<MessageBean> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ChatVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(parent.getContext());
        MessageBinding binding=MessageBinding.inflate(inflater,parent,false);
        return new ChatVH(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatVH holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        if(items!=null)return items.size();
        return 0;
    }

    public class ChatVH extends RecyclerView.ViewHolder{
        private MessageBinding binding;
        public ChatVH(MessageBinding binding) {
            super(binding.getRoot());
            this.binding=binding;
        }

        public void bind(MessageBean item){
            binding.setItem(item);
            if (item.isSend()) {


                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                );
                params.gravity = Gravity.END;
                ConstraintLayout container = binding.container;
                container.setBackground(ContextCompat.getDrawable(binding.getRoot().getContext(), R.drawable.purple_rec));
                container.setLayoutParams(params);

                //binding.container.foregroundGravity

            }
        }


    }
}
