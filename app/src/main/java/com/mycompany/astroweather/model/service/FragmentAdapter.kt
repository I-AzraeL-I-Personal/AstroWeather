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

    fun add(classes: Map<Int, KClass<out Fragment>>) {
        classes.forEach {
            fragments.add(it.key, it.value)
            fragmentIds.add(it.key, fragments.hashCode().toLong())
            notifyItemRangeChanged(it.key, fragments.size)
        }
        notifyDataSetChanged()
    }

    fun remove(vararg positions: Int) {
        positions.forEach {
            fragments.removeAt(it)
            fragmentIds.removeAt(it)
            notifyItemRangeChanged(it, fragments.size)
        }
        notifyDataSetChanged()
    }
}