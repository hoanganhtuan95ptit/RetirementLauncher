package com.simple.launcher.retirement.utils.text

class RichTextBuilder(val text: String) {

    val richStyles: ArrayList<RichStyle> = arrayListOf()

    fun add(vararg richStyle: RichStyle) {
        richStyles.addAll(richStyle)
    }

}