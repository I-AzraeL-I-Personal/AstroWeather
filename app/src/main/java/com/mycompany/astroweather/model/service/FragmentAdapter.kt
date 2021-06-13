package com.mycompany.astroweather.model.service

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance


class FragmentAdapter(fragmentManager: FragmentManager,
                              lifecycle: Lifecycle,
                              private val fragments: MutableList<KClass<out Fragment>>)
    : FragmentStateAdapter(fragmentManager, lifecycle) {

    private val fragmentIds = fragments.map { it.hashCode().toLong() }.toMutableList()

    override fun getItemCount() = fragments.size

    override fun createFragment(position: Int): Fragment {
        return fragments[position].createInstance()
    }

    override fun getItemId(position: Int): Long {
        return fragmentIds[position]
    }

    override fun containsItem(itemId: Long): Boolean {
        return fragmentIds.contains(itemId)
    }

    fun add(index: Int, fragment: KClass<out Fragment>) {
        fragments.add(index, fragment)
        fragmentIds.add(index, fragments.hashCode().toLong())
        notifyItemRangeChanged(index, fragments.size)
        notifyDataSetChanged()
    }

    fun remove(index: Int) {
        fragments.removeAt(index)
        fragmentIds.removeAt(index)
        notifyItemRangeChanged(index, fragments.size)
        notifyDataSetChanged()
    }
}