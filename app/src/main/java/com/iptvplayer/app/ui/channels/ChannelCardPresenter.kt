package com.iptvplayer.app.ui.channels

import android.view.ViewGroup
import android.widget.ImageView
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.iptvplayer.app.R
import com.iptvplayer.app.data.model.Channel

class ChannelCardPresenter : Presenter() {

    private val CARD_WIDTH = 200
    private val CARD_HEIGHT = 150

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val cardView = ImageCardView(parent.context)
        cardView.setMainImageScaleType(ImageView.ScaleType.CENTER_CROP)
        cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT)
        cardView.isFocusable = true
        cardView.isFocusableInTouchMode = true
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any?) {
        val channel = item as? Channel ?: return
        val cardView = viewHolder.view as ImageCardView

        cardView.titleText = channel.name
        cardView.contentText = channel.group

        if (channel.logoUrl.isNotBlank()) {
            Glide.with(cardView.context)
                .load(channel.logoUrl)
                .transition(DrawableTransitionOptions.withCrossFade())
                .error(R.drawable.ic_tv_placeholder)
                .placeholder(R.drawable.ic_tv_placeholder)
                .into(cardView.mainImageView!!)
        } else {
            cardView.mainImageView?.setImageResource(R.drawable.ic_tv_placeholder)
        }

        if (channel.isFavorite) {
            cardView.badgeImage = cardView.context.getDrawable(R.drawable.ic_star)
        } else {
            cardView.badgeImage = null
        }
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val cardView = viewHolder.view as ImageCardView
        Glide.with(cardView.context).clear(cardView.mainImageView)
        cardView.mainImageView?.setImageDrawable(null)
        cardView.badgeImage = null
    }
}
