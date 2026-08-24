package com.bugbd.pdfocr.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bugbd.pdfocr.databinding.ItemOnbordingBinding
import com.bugbd.pdfocr.model.OnboardingItem

class OnboardingAdapter(
    private val items: List<OnboardingItem>
) : RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    inner class OnboardingViewHolder(private val binding: ItemOnbordingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: OnboardingItem) {
            when {
                item.gifRes != null -> {
                    binding.lottieAnimation.visibility = View.GONE
                    binding.imageOnboarding.visibility = View.VISIBLE
                    Glide.with(binding.imageOnboarding.context)
                        .asGif()
                        .load(item.gifRes)
                        .into(binding.imageOnboarding)
                }
                item.lottieRes != null -> {
                    binding.lottieAnimation.setAnimation(item.lottieRes)
                    binding.lottieAnimation.visibility = View.VISIBLE
                    binding.imageOnboarding.visibility = View.GONE
                }
                else -> {
                    binding.imageOnboarding.setImageResource(item.imageRes)
                    binding.imageOnboarding.visibility = View.VISIBLE
                    binding.lottieAnimation.visibility = View.GONE
                }
            }
            binding.textTitle.text = item.title
            binding.textDescription.text = item.description
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val binding = ItemOnbordingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OnboardingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}
