/*
 * YukiHookAPI - An efficient Kotlin version of the Xposed Hook API.
 * Copyright (C) 2019-2022 HighCapable
 * https://github.com/fankes/YukiHookAPI
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * This file is Created by fankes on 2022/3/27.
 */
@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.highcapable.yukihookapi.hook.core.finder.type

import java.lang.reflect.Member
import java.lang.reflect.Modifier

/**
 * 这是一个 [Class]、[Member] 描述符定义类
 *
 * 可对 R8 混淆后的 [Class]、[Member] 进行更加详细的定位
 */
class ModifierRules @PublishedApi internal constructor() {

    /** 描述声明使用 */
    private var isPublic = false

    /** 描述声明使用 */
    private var isPrivate = false

    /** 描述声明使用 */
    private var isProtected = false

    /** 描述声明使用 */
    private var isStatic = false

    /** 描述声明使用 */
    private var isFinal = false

    /** 描述声明使用 */
    private var isSynchronized = false

    /** 描述声明使用 */
    private var isVolatile = false

    /** 描述声明使用 */
    private var isTransient = false

    /** 描述声明使用 */
    private var isNative = false

    /** 描述声明使用 */
    private var isInterface = false

    /** 描述声明使用 */
    private var isAbstract = false

    /** 描述声明使用 */
    private var isStrict = false

    /**
     * - ❗此方法已弃用 - 在之后的版本中将直接被删除
     *
     * - ❗请现在转移到 [isPublic]
     */
    @Deprecated(message = "请使用新的命名方法", replaceWith = ReplaceWith(expression = "isPublic()"))
    fun asPublic() = isPublic()

    /**
     * - ❗此方法已弃用 - 在之后的版本中将直接被删除
     *
     * - ❗请现在转移到 [isPrivate]
     */
    @Deprecated(message = "请使用新的命名方法", replaceWith = ReplaceWith(expression = "isPrivate()"))
    fun asPrivate() = isPrivate()

    /**
     * - ❗此方法已弃用 - 在之后的版本中将直接被删除
     *
     * - ❗请现在转移到 [isProtected]
     */
    @Deprecated(message = "请使用新的命名方法", replaceWith = ReplaceWith(expression = "isProtected()"))
    fun asProtected() = isProtected()

    /**
     * - ❗此方法已弃用 - 在之后的版本中将直接被删除
     *
     * - ❗请现在转移到 [isStatic]
     */
    @Deprecated(message = "请使用新的命名方法", replaceWith = ReplaceWith(expression = "isStatic()"))
    fun asStatic() = isStatic()

    /**
     * - ❗此方法已弃用 - 在之后的版本中将直接被删除
     *
     * - ❗请现在转移到 [isFinal]
     */
    @Deprecated(message = "请使用新的命名方法", replaceWith = ReplaceWith(expression = "isFinal()"))
    fun asFinal() = isFinal()

    /**
     * - ❗此方法已弃用 - 在之后的版本中将直接被删除
     *
     * - ❗请现在转移到 [isSynchronized]
     */
    @Deprecated(message = "请使用新的命名方法", replaceWith = ReplaceWith(expression = "isSynchronized()"))
    fun asSynchronized() = isSynchronized()

    /**
     * - ❗此方法已弃用 - 在之后的版本中将直接被删除
     *
     * - ❗请现在转移到 [isVolatile]
     */
    @Deprecated(message = "请使用新的命名方法", replaceWith = ReplaceWith(expression = "isVolatile()"))
    fun asVolatile() = isVolatile()

    /**
     * - ❗此方法已弃用 - 在之后的版本中将直接被删除
     *
     * - ❗请现在转移到 [isTransient]
     */
    @Deprecated(message = "请使用新的命名方法", replaceWith = ReplaceWith(expression = "isTransient()"))
    fun asTransient() = isTransient()

    /**
     * - ❗此方法已弃用 - 在之后的版本中将直接被删除
     *
     * - ❗请现在转移到 [isNative]
     */
    @Deprecated(message = "请使用新的命名方法", replaceWith = ReplaceWith(expression = "isNative()"))
    fun asNative() = isNative()

    /**
     * - ❗此方法已弃用 - 在之后的版本中将直接被删除
     *
     * - ❗请现在转移到 [isInterface]
     */
    @Deprecated(message = "请使用新的命名方法", replaceWith = ReplaceWith(expression = "isInterface()"))
    fun asInterface() = isInterface()

    /**
     * - ❗此方法已弃用 - 在之后的版本中将直接被删除
     *
     * - ❗请现在转移到 [isAbstract]
     */
    @Deprecated(message = "请使用新的命名方法", replaceWith = ReplaceWith(expression = "isAbstract()"))
    fun asAbstract() = isAbstract()

    /**
     * - ❗此方法已弃用 - 在之后的版本中将直接被删除
     *
     * - ❗请现在转移到 [isStrict]
     */
    @Deprecated(message = "请使用新的命名方法", replaceWith = ReplaceWith(expression = "isStrict()"))
    fun asStrict() = isStrict()

    /** 添加描述 [Class]、[Member] 类型包含 public */
    fun isPublic() {
        isPublic = true
    }

    /** 添加描述 [Class]、[Member] 类型包含 private */
    fun isPrivate() {
        isPrivate = true
    }

    /** 添加描述 [Class]、[Member] 类型包含 protected */
    fun isProtected() {
        isProtected = true
    }

    /**
     * 添加描述 [Class]、[Member] 类型包含 static
     *
     * 对于任意的静态 [Class]、[Member] 可添加此描述进行确定
     *
     * - ❗注意 Kotlin → Jvm 后的 object 类中的方法并不是静态的
     */
    fun isStatic() {
        isStatic = true
    }

    /**
     * 添加描述 [Class]、[Member] 类型包含 final
     *
     * - ❗注意 Kotlin → Jvm 后没有 open 标识的 [Class]、[Member] 和没有任何关联的 [Class]、[Member] 都将为 final
     */
    fun isFinal() {
        isFinal = true
    }

    /** 添加描述 [Class]、[Member] 类型包含 synchronized */
    fun isSynchronized() {
        isSynchronized = true
    }

    /** 添加描述 [Class]、[Member] 类型包含 volatile */
    fun isVolatile() {
        isVolatile = true
    }

    /** 添加描述 [Class]、[Member] 类型包含 transient */
    fun isTransient() {
        isTransient = true
    }

    /**
     * 添加描述 [Class]、[Member] 类型包含 native
     *
     * 对于任意 JNI 对接的 [Class]、[Member] 可添加此描述进行确定
     */
    fun isNative() {
        isNative = true
    }

    /** 添加描述 [Class]、[Member] 类型包含 interface */
    fun isInterface() {
        isInterface = true
    }

    /**
     * 添加描述 [Class]、[Member] 类型包含 abstract
     *
     * 对于任意的抽象 [Class]、[Member] 可添加此描述进行确定
     */
    fun isAbstract() {
        isAbstract = true
    }

    /** 添加描述 [Class]、[Member] 类型包含 strict */
    fun isStrict() {
        isStrict = true
    }

    /**
     * 对比 [Class]、[Member] 类型是否符合条件
     * @param reflects 实例 - 只能是 [Class] or [Member]
     * @return [Boolean] 是否符合条件
     */
    @PublishedApi
    internal fun contains(reflects: Any): Boolean {
        var conditions = true
        Reflects(reflects).also {
            if (isPublic) conditions = Modifier.isPublic(it.modifiers)
            if (isPrivate) conditions = conditions && Modifier.isPrivate(it.modifiers)
            if (isProtected) conditions = conditions && Modifier.isProtected(it.modifiers)
            if (isStatic) conditions = conditions && Modifier.isStatic(it.modifiers)
            if (isFinal) conditions = conditions && Modifier.isFinal(it.modifiers)
            if (isSynchronized) conditions = conditions && Modifier.isSynchronized(it.modifiers)
            if (isVolatile) conditions = conditions && Modifier.isVolatile(it.modifiers)
            if (isTransient) conditions = conditions && Modifier.isTransient(it.modifiers)
            if (isNative) conditions = conditions && Modifier.isNative(it.modifiers)
            if (isInterface) conditions = conditions && Modifier.isInterface(it.modifiers)
            if (isAbstract) conditions = conditions && Modifier.isAbstract(it.modifiers)
            if (isStrict) conditions = conditions && Modifier.isStrict(it.modifiers)
        }
        return conditions
    }

    override fun toString(): String {
        var conditions = ""
        if (isPublic) conditions += "<public> "
        if (isPrivate) conditions += "<private> "
        if (isProtected) conditions += "<protected> "
        if (isStatic) conditions += "<static> "
        if (isFinal) conditions += "<final> "
        if (isSynchronized) conditions += "<synchronized> "
        if (isVolatile) conditions += "<volatile> "
        if (isTransient) conditions += "<transient> "
        if (isNative) conditions += "<native> "
        if (isInterface) conditions += "<interface> "
        if (isAbstract) conditions += "<abstract> "
        if (isStrict) conditions += "<strict> "
        return "[${conditions.trim()}]"
    }

    /**
     * 实例化反射对象接口实现类
     * @param reflects 反射对象实例
     */
    private class Reflects(private val reflects: Any) {

        /**
         * 获取当前对象的类型描述符
         * @return [Int]
         */
        val modifiers
            get() = when (reflects) {
                is Member -> reflects.modifiers
                is Class<*> -> reflects.modifiers
                else -> error("Invalid reflects type")
            }
    }
}