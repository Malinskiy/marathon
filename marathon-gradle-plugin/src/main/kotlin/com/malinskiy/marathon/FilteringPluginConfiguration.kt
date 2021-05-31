package com.malinskiy.marathon

import com.malinskiy.marathon.execution.AnnotationDataFilter
import com.malinskiy.marathon.execution.AnnotationFilter
import com.malinskiy.marathon.execution.FilteringConfiguration
import com.malinskiy.marathon.execution.FullyQualifiedClassnameFilter
import com.malinskiy.marathon.execution.SimpleClassnameFilter
import com.malinskiy.marathon.execution.TestFilter
import com.malinskiy.marathon.execution.TestMethodFilter
import com.malinskiy.marathon.execution.TestPackageFilter
import groovy.lang.Closure

open class FilteringPluginConfiguration {
    //groovy
    var groovyAllowList: Wrapper? = null
    var groovyBlockList: Wrapper? = null

    fun allowlist(closure: Closure<*>) {
        groovyAllowList = Wrapper()
        closure.delegate = groovyAllowList
        closure.call()
    }

    fun blocklist(closure: Closure<*>) {
        groovyBlockList = Wrapper()
        closure.delegate = groovyBlockList
        closure.call()
    }

    //kts
    var allowlist: MutableCollection<TestFilter> = mutableListOf()
    var blocklist: MutableCollection<TestFilter> = mutableListOf()
    fun allowlist(block: MutableCollection<TestFilter>.() -> Unit) {
        allowlist.also(block)
    }

    fun blocklist(block: MutableCollection<TestFilter>.() -> Unit) {
        blocklist.also(block)
    }
}

open class Wrapper {
    open var simpleClassNameFilter: ArrayList<String>? = null
    open var fullyQualifiedClassnameFilter: ArrayList<String>? = null
    open var testPackageFilter: ArrayList<String>? = null
    open var testMethodFilter: ArrayList<String>? = null
    open var annotationFilter: ArrayList<String>? = null
    open var annotationDataFilter: ArrayList<String>? = null
}

fun Wrapper.toList(): List<TestFilter> {
    val mutableList = mutableListOf<TestFilter>()
    this.annotationFilter?.map { AnnotationFilter(it.toRegex()) }?.let {
        mutableList.addAll(it)
    }
    this.fullyQualifiedClassnameFilter?.map { FullyQualifiedClassnameFilter(it.toRegex()) }?.let {
        mutableList.addAll(it)
    }
    this.testPackageFilter?.map { TestPackageFilter(it.toRegex()) }?.let {
        mutableList.addAll(it)
    }
    this.testMethodFilter?.map { TestMethodFilter(it.toRegex()) }?.let {
        mutableList.addAll(it)
    }
    this.simpleClassNameFilter?.map { SimpleClassnameFilter(it.toRegex()) }?.let {
        mutableList.addAll(it)
    }
    this.annotationDataFilter?.map {
        val currentData = it.split(",")
        AnnotationDataFilter(currentData.first().toRegex(), currentData[1].toRegex())
    }?.let {
        mutableList.addAll(it)
    }
    return mutableList
}

fun FilteringPluginConfiguration.toFilteringConfiguration(): FilteringConfiguration {
    if (groovyAllowList != null || groovyBlockList != null) {
        val allow = groovyAllowList?.toList() ?: emptyList()

        val block = groovyBlockList?.toList() ?: emptyList()
        return FilteringConfiguration(allow, block)
    }
    return FilteringConfiguration(allowlist, blocklist)
}

