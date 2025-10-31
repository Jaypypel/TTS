package com.example.neptune.ttsapp;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neptune.ttsapp.databinding.TaskAcceptanceRowItemBinding;

import java.util.Objects;

public class TaskDelegatedListCustomAdapter extends ListAdapter<TaskDataModel, TaskDelegatedListCustomAdapter.TaskDelegatedViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(TaskDataModel item);
    }

    private final OnItemClickListener listener;

    public TaskDelegatedListCustomAdapter(OnItemClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
        setHasStableIds(true); // Optional but good for large lists
    }

    // Use stable IDs for better recycling
    @Override
    public long getItemId(int position) {
        //ensure getItem(position) is not null before accessing getId() in production
        TaskDataModel item = getItem(position);
        if(item != null) return item.getId(); //Ensure getId() returns a unique, stable value
        return RecyclerView.NO_ID; // Fallback if item is somehow null
    }

    // ViewHolder
    static class TaskDelegatedViewHolder extends RecyclerView.ViewHolder {
//     private final   TextView taskName, taskReceivedUser, taskDate, taskStatus;
        private final TaskAcceptanceRowItemBinding binding;
        public TaskDelegatedViewHolder(@NonNull TaskAcceptanceRowItemBinding itemBinding) {
                super(itemBinding.getRoot());
                this.binding = itemBinding;
//
//            super(itemView);
//            taskName = itemView.findViewById(R.id.taskDeligateTaskName);
//            taskReceivedUser = itemView.findViewById(R.id.taskDeligateOwnerName);
//            taskDate = itemView.findViewById(R.id.taskDeligateTaskDate);
//            taskStatus = itemView.findViewById(R.id.taskDeligateTaskStatus);
        }

        public void bind(final TaskDataModel item, final OnItemClickListener listener) {
            if(item == null){
                Log.w("Adapter", "Attemtping to bind null item in ViewHolder.");
                binding.taskDeligateTaskName.setText("");
                binding.taskDeligateOwnerName.setText("");
                binding.taskDeligateTaskDate.setText("");
                binding.taskDeligateTaskStatus.setText("");
                binding.getRoot().setOnClickListener(null);
                return;
            }

            binding.taskDeligateTaskName.setText(item.getTaskName());
            binding.taskDeligateOwnerName.setText(item.getTaskReceivedUserID());
            binding.taskDeligateTaskDate.setText(item.getExpectedDate());
            binding.taskDeligateTaskStatus.setText(item.getStatus());
            binding.getRoot().setOnClickListener(v -> {
                if (listener!=null) listener.onItemClick(item);
            });
//            taskName.setText(item.getTaskName());
//                    taskReceivedUser.setText(item.getTaskReceivedUserID());
//            taskDate.setText(item.getExpectedDate());
//            taskStatus.setText(item.getStatus());
//            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }

    // Create ViewHolder
    @NonNull
    @Override
    public TaskDelegatedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.task_acceptance_row_item, parent, false); // Replace with your item layout
//        return new TaskDelegatedViewHolder(view);
        TaskAcceptanceRowItemBinding binding = TaskAcceptanceRowItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new TaskDelegatedViewHolder(binding);
    }

    // Bind ViewHolder
    @Override
    public void onBindViewHolder(@NonNull TaskDelegatedViewHolder holder, int position) {
        TaskDataModel item = getItem(position);
        Log.d("Adapter", "Binding item at position " + position + ": " + item.getTaskName());
        holder.bind(item, listener);
    }

    // DiffUtil Callback
    public static final DiffUtil.ItemCallback<TaskDataModel> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull TaskDataModel oldItem, @NonNull TaskDataModel newItem) {
            return Objects.equals(oldItem.getId(), newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull TaskDataModel oldItem, @NonNull TaskDataModel newItem) {
            return oldItem.equals(newItem); // Override equals() properly in YourModel
        }
    };
}
