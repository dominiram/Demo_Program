package com.example.cardgame.models

import com.google.gson.annotations.SerializedName

data class NewDeckResponse(
    @field:SerializedName("success")
    val success: Boolean,

    @field:SerializedName("deck_id")
    val deckId: String,

    @field:SerializedName("shuffled")
    val shuffled: Boolean,

    @field:SerializedName("remaining")
    val remaining: Int
)

data class NewCardResponse(
    @field:SerializedName("cards")
    val cards: Array<CardInfo>,

    @field:SerializedName("remaining")
    val remaining: String,

    @field:SerializedName("deck_id")
    val deckId: String,

    @field:SerializedName("success")
    val success: Boolean
)

data class CardInfo(
    @field:SerializedName("images")
    val images: ImageData,

    @field:SerializedName("image")
    val image: String,

    @field:SerializedName("value")
    val value: String,

    @field:SerializedName("code")
    val code: String,

    @field:SerializedName("suit")
    val suit: String
)

data class ImageData(

    @field:SerializedName("svg")
    val svg: String,

    @field:SerializedName("png")
    val png: String
)
