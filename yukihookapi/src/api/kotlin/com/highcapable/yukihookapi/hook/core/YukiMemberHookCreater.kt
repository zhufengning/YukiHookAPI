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
 * This file is Created by fankes on 2022/2/2.
 */
@file:Suppress("MemberVisibilityCanBePrivate", "unused", "PropertyName")

package com.highcapable.yukihookapi.hook.core

import android.os.SystemClock
import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.hook.bean.HookClass
import com.highcapable.yukihookapi.hook.core.finder.ConstructorFinder
import com.highcapable.yukihookapi.hook.core.finder.FieldFinder
import com.highcapable.yukihookapi.hook.core.finder.MethodFinder
import com.highcapable.yukihookapi.hook.core.finder.base.BaseFinder
import com.highcapable.yukihookapi.hook.factory.allConstructors
import com.highcapable.yukihookapi.hook.factory.allMethods
import com.highcapable.yukihookapi.hook.log.yLoggerE
import com.highcapable.yukihookapi.hook.log.yLoggerI
import com.highcapable.yukihookapi.hook.log.yLoggerW
import com.highcapable.yukihookapi.hook.param.HookParam
import com.highcapable.yukihookapi.hook.param.PackageParam
import com.highcapable.yukihookapi.hook.param.type.HookEntryType
import com.highcapable.yukihookapi.hook.param.wrapper.HookParamWrapper
import com.highcapable.yukihookapi.hook.type.java.*
import com.highcapable.yukihookapi.hook.xposed.bridge.YukiHookBridge
import com.highcapable.yukihookapi.hook.xposed.bridge.factory.YukiHookHelper
import com.highcapable.yukihookapi.hook.xposed.bridge.factory.YukiHookPriority
import com.highcapable.yukihookapi.hook.xposed.bridge.factory.YukiMemberHook
import com.highcapable.yukihookapi.hook.xposed.bridge.factory.YukiMemberReplacement
import java.lang.reflect.Field
import java.lang.reflect.Member

/**
 * [YukiHookAPI] 的 [Member] 核心 Hook 实现类
 *
 * 核心 API 对接 [YukiHookHelper] 实现
 * @param packageParam 需要传入 [PackageParam] 实现方法调用
 * @param hookClass 要 Hook 的 [HookClass] 实例
 */
class YukiMemberHookCreater(@PublishedApi internal val packageParam: PackageParam, @PublishedApi internal val hookClass: HookClass) {

    /** 默认 Hook 回调优先级 */
    val PRIORITY_DEFAULT = YukiHookPriority.PRIORITY_DEFAULT

    /** 延迟回调 Hook 方法结果 */
    val PRIORITY_LOWEST = YukiHookPriority.PRIORITY_LOWEST

    /** 更快回调 Hook 方法结果 */
    val PRIORITY_HIGHEST = YukiHookPriority.PRIORITY_HIGHEST

    /** [hookClass] 找不到时出现的错误回调 */
    private var onHookClassNotFoundFailureCallback: ((Throwable) -> Unit)? = null

    /** 是否对当前 [YukiMemberHookCreater] 禁止执行 Hook 操作 */
    @PublishedApi
    internal var isDisableCreaterRunHook = false

    /** 设置要 Hook 的方法、构造方法 */
    @PublishedApi
    internal var preHookMembers = HashMap<String, MemberHookCreater>()

    /**
     * 得到当前被 Hook 的 [Class]
     *
     * - ❗不推荐直接使用 - 万一得不到 [Class] 对象则会无法处理异常导致崩溃
     * @return [Class]
     * @throws IllegalStateException 如果当前 [Class] 未被正确装载
     */
    val instanceClass
        get() = hookClass.instance ?: error("Cannot get hook class \"${hookClass.name}\" cause ${hookClass.throwable?.message}")

    /**
     * 注入要 Hook 的方法、构造方法
     * @param priority Hook 优先级 - 默认 [PRIORITY_DEFAULT]
     * @param tag 可设置标签 - 在发生错误时方便进行调试
     * @param initiate 方法体
     * @return [MemberHookCreater.Result]
     */
    inline fun injectMember(priority: Int = PRIORITY_DEFAULT, tag: String = "Default", initiate: MemberHookCreater.() -> Unit) =
        MemberHookCreater(priority, tag, packageParam.exhibitName).apply(initiate).apply { preHookMembers[toString()] = this }.build()

    /**
     * Hook 执行入口
     * @throws IllegalStateException 如果必要参数没有被设置
     * @return [Result]
     */
    @PublishedApi
    internal fun hook() = when {
        YukiHookBridge.hasXposedBridge.not() -> Result()
        /** 过滤 [HookEntryType.ZYGOTE] 与 [HookEntryType.PACKAGE] 或 [HookParam.isCallbackCalled] 已被执行 */
        packageParam.wrapper?.type == HookEntryType.RESOURCES && HookParam.isCallbackCalled.not() -> Result()
        preHookMembers.isEmpty() -> error("Hook Members is empty, hook aborted")
        else -> Result().also {
            warnTerribleHookClass()
            Thread {
                /** 延迟使得方法取到返回值 */
                SystemClock.sleep(1)
                when {
                    isDisableCreaterRunHook.not() && hookClass.instance != null -> {
                        it.onPrepareHook?.invoke()
                        preHookMembers.forEach { (_, m) -> m.hook() }
                    }
                    isDisableCreaterRunHook.not() && hookClass.instance == null ->
                        if (onHookClassNotFoundFailureCallback == null)
                            yLoggerE(msg = "[${packageParam.exhibitName}] HookClass [${hookClass.name}] not found", e = hookClass.throwable)
                        else onHookClassNotFoundFailureCallback?.invoke(hookClass.throwable ?: Throwable("[${hookClass.name}] not found"))
                }
            }.start()
        }
    }

    /** 打印不应该被 Hook 警告范围内的 [HookClass] 对象 */
    private fun warnTerribleHookClass() {
        when (hookClass.name) {
            AnyType.name -> yLoggerW(
                msg = "Hook [Object] Class is a dangerous behavior! " +
                        "This is the parent Class of all objects, if you hook it, it may cause a lot of memory leaks"
            )
            JavaClassLoader.name -> yLoggerW(
                msg = "Hook [ClassLoader] Class is a dangerous behavior! " +
                        "If you only want to listen to \"loadClass\" use \"ClassLoader.fetching\" instead it"
            )
            JavaClass.name, JavaMethodClass.name, JavaFieldClass.name,
            JavaConstructorClass.name, JavaMemberClass.name -> yLoggerW(
                msg = "Hook [Class/Method/Field/Constructor/Member] Class is a dangerous behavior! " +
                        "Those Class should not be hooked, it may cause StackOverflow errors"
            )
        }
    }

    /**
     * Hook 核心功能实现类
     *
     * 查找和处理需要 Hook 的方法、构造方法
     * @param priority Hook 优先级
     * @param tag 当前设置的标签
     * @param packageName 当前 Hook 的 APP 包名
     */
    inner class MemberHookCreater @PublishedApi internal constructor(
        private val priority: Int, internal val tag: String, internal val packageName: String
    ) {

        /** Hook 结果实例 */
        private var result: Result? = null

        /** 是否已经执行 Hook */
        private var isHooked = false

        /** [beforeHook] 回调 */
        private var beforeHookCallback: (HookParam.() -> Unit)? = null

        /** [afterHook] 回调 */
        private var afterHookCallback: (HookParam.() -> Unit)? = null

        /** [replaceAny]、[replaceUnit] 回调 */
        private var replaceHookCallback: (HookParam.() -> Any?)? = null

        /** Hook 成功时回调 */
        private var onHookedCallback: ((Member) -> Unit)? = null

        /** 重复 Hook 时回调 */
        private var onAlreadyHookedCallback: ((Member) -> Unit)? = null

        /** 找不到 [members] 出现错误回调 */
        private var onNoSuchMemberFailureCallback: ((Throwable) -> Unit)? = null

        /** Hook 过程中出现错误回调 */
        private var onConductFailureCallback: ((HookParam, Throwable) -> Unit)? = null

        /** Hook 开始时出现错误回调 */
        private var onHookingFailureCallback: ((Throwable) -> Unit)? = null

        /** 全部错误回调 */
        private var onAllFailureCallback: ((Throwable) -> Unit)? = null

        /** 是否为替换 Hook 模式 */
        private var isReplaceHookMode = false

        /** 是否对当前 [MemberHookCreater] 禁止执行 Hook 操作 */
        @PublishedApi
        internal var isDisableMemberRunHook = false

        /** 查找过程中发生的异常 */
        @PublishedApi
        internal var findingThrowable: Throwable? = null

        /** 标识是否已经设置了要 Hook 的 [members] */
        @PublishedApi
        internal var isHookMemberSetup = false

        /** 当前的查找实例 */
        @PublishedApi
        internal var finder: BaseFinder? = null

        /** 当前被 Hook 的方法、构造方法实例数组 */
        private val memberUnhooks = HashSet<YukiMemberHook.Unhook>()

        /** 当前需要 Hook 的方法、构造方法 */
        internal val members = HashSet<Member>()

        /**
         * 手动指定要 Hook 的方法、构造方法
         *
         * 你可以调用 [instanceClass] 来手动查询要 Hook 的方法
         *
         * - ❗不建议使用此方法设置目标需要 Hook 的 [Member] 对象 - 你可以使用 [method] 或 [constructor] 方法
         *
         * - ❗在同一个 [injectMember] 中你只能使用一次 [members]、[allMembers]、[method]、[constructor] 方法 - 否则结果会被替换
         * @param member 要指定的 [Member] 或 [Member] 数组
         * @throws IllegalStateException 如果 [member] 参数为空
         */
        fun members(vararg member: Member?) {
            if (member.isEmpty()) error("Custom Hooking Members is empty")
            members.clear()
            member.forEach { it?.also { members.add(it) } }
        }

        /**
         * 查找并 Hook [hookClass] 中指定 [name] 的全部方法
         *
         * - ❗此方法已弃用 - 在之后的版本中将直接被删除
         *
         * - ❗请现在转移到 [MethodFinder]
         * @param name 方法名称
         * @return [ArrayList]<[MethodFinder.Result.Instance]>
         */
        @Deprecated("请使用新方式来实现 Hook 所有方法", ReplaceWith(expression = "method { this.name = name }.all()"))
        fun allMethods(name: String) = method { this.name = name }.all()

        /**
         * 查找并 Hook [hookClass] 中的全部构造方法
         *
         * - ❗此方法已弃用 - 在之后的版本中将直接被删除
         *
         * - ❗请现在转移到 [ConstructorFinder]
         * @return [ArrayList]<[ConstructorFinder.Result.Instance]>
         */
        @Deprecated("请使用新方式来实现 Hook 所有构造方法", ReplaceWith(expression = "constructor().all()"))
        fun allConstructors() = constructor().all()

        /**
         * 查找并 Hook [hookClass] 中的全部方法、构造方法
         *
         * - ❗在同一个 [injectMember] 中你只能使用一次 [members]、[allMembers]、[method]、[constructor] 方法 - 否则结果会被替换
         *
         * - ❗警告：无法准确处理每个方法的返回值和 param - 建议使用 [method] or [constructor] 对每个方法单独 Hook
         *
         * - ❗如果 [hookClass] 中没有方法可能会发生错误
         */
        fun allMembers() {
            members.clear()
            hookClass.instance?.allConstructors { _, constructor -> members.add(constructor) }
            hookClass.instance?.allMethods { _, method -> members.add(method) }
            isHookMemberSetup = true
        }

        /**
         * 查找 [hookClass] 需要 Hook 的方法
         *
         * - ❗在同一个 [injectMember] 中你只能使用一次 [members]、[allMembers]、[method]、[constructor] 方法 - 否则结果会被替换
         * @param initiate 方法体
         * @return [MethodFinder.Result]
         */
        inline fun method(initiate: MethodFinder.() -> Unit) = try {
            isHookMemberSetup = true
            MethodFinder(hookInstance = this, hookClass.instance).apply(initiate).apply { finder = this }.build(isBind = true)
        } catch (e: Throwable) {
            findingThrowable = e
            MethodFinder(hookInstance = this).failure(e)
        }

        /**
         * 查找 [hookClass] 需要 Hook 的构造方法
         *
         * - ❗在同一个 [injectMember] 中你只能使用一次 [members]、[allMembers]、[method]、[constructor] 方法 - 否则结果会被替换
         * @param initiate 方法体
         * @return [ConstructorFinder.Result]
         */
        inline fun constructor(initiate: ConstructorFinder.() -> Unit = { emptyParam() }) = try {
            isHookMemberSetup = true
            ConstructorFinder(hookInstance = this, hookClass.instance).apply(initiate).apply { finder = this }.build(isBind = true)
        } catch (e: Throwable) {
            findingThrowable = e
            ConstructorFinder(hookInstance = this).failure(e)
        }

        /**
         * 使用当前 [hookClass] 查找并得到 [Field]
         * @param initiate 方法体
         * @return [FieldFinder.Result]
         */
        inline fun HookParam.field(initiate: FieldFinder.() -> Unit) =
            if (hookClass.instance == null) FieldFinder(hookInstance = this@MemberHookCreater).failure(hookClass.throwable)
            else FieldFinder(hookInstance = this@MemberHookCreater, hookClass.instance).apply(initiate).build()

        /**
         * 使用当前 [hookClass] 查找并得到方法
         * @param initiate 方法体
         * @return [MethodFinder.Result]
         */
        inline fun HookParam.method(initiate: MethodFinder.() -> Unit) =
            if (hookClass.instance == null) MethodFinder(hookInstance = this@MemberHookCreater).failure(hookClass.throwable)
            else MethodFinder(hookInstance = this@MemberHookCreater, hookClass.instance).apply(initiate).build()

        /**
         * 使用当前 [hookClass] 查找并得到构造方法
         * @param initiate 方法体
         * @return [ConstructorFinder.Result]
         */
        inline fun HookParam.constructor(initiate: ConstructorFinder.() -> Unit = { emptyParam() }) =
            if (hookClass.instance == null) ConstructorFinder(hookInstance = this@MemberHookCreater).failure(hookClass.throwable)
            else ConstructorFinder(hookInstance = this@MemberHookCreater, hookClass.instance).apply(initiate).build()

        /**
         * 注入要 Hook 的方法、构造方法 (嵌套 Hook)
         * @param priority Hook 优先级 - 默认 [PRIORITY_DEFAULT]
         * @param tag 可设置标签 - 在发生错误时方便进行调试
         * @param initiate 方法体
         * @return [MemberHookCreater.Result]
         */
        inline fun HookParam.injectMember(
            priority: Int = PRIORITY_DEFAULT,
            tag: String = "InnerDefault",
            initiate: MemberHookCreater.() -> Unit
        ) = this@YukiMemberHookCreater.injectMember(priority, tag, initiate).also { this@YukiMemberHookCreater.hook() }

        /**
         * 在方法执行完成前 Hook
         *
         * - 不可与 [replaceAny]、[replaceUnit]、[replaceTo] 同时使用
         * @param initiate [HookParam] 方法体
         */
        fun beforeHook(initiate: HookParam.() -> Unit) {
            isReplaceHookMode = false
            beforeHookCallback = initiate
        }

        /**
         * 在方法执行完成后 Hook
         *
         * - 不可与 [replaceAny]、[replaceUnit]、[replaceTo] 同时使用
         * @param initiate [HookParam] 方法体
         */
        fun afterHook(initiate: HookParam.() -> Unit) {
            isReplaceHookMode = false
            afterHookCallback = initiate
        }

        /**
         * 拦截并替换此方法内容 - 给出返回值
         *
         * - 不可与 [beforeHook]、[afterHook] 同时使用
         * @param initiate [HookParam] 方法体
         */
        fun replaceAny(initiate: HookParam.() -> Any?) {
            isReplaceHookMode = true
            replaceHookCallback = initiate
        }

        /**
         * 拦截并替换此方法内容 - 没有返回值 ([Unit])
         *
         * - 不可与 [beforeHook]、[afterHook] 同时使用
         * @param initiate [HookParam] 方法体
         */
        fun replaceUnit(initiate: HookParam.() -> Unit) {
            isReplaceHookMode = true
            replaceHookCallback = initiate
        }

        /**
         * 拦截并替换方法返回值
         *
         * - 不可与 [beforeHook]、[afterHook] 同时使用
         * @param any 要替换为的返回值对象
         */
        fun replaceTo(any: Any?) {
            isReplaceHookMode = true
            replaceHookCallback = { any }
        }

        /**
         * 拦截并替换方法返回值为 true
         *
         * - ❗确保替换方法的返回对象为 [Boolean]
         *
         * - 不可与 [beforeHook]、[afterHook] 同时使用
         */
        fun replaceToTrue() {
            isReplaceHookMode = true
            replaceHookCallback = { true }
        }

        /**
         * 拦截并替换方法返回值为 false
         *
         * - ❗确保替换方法的返回对象为 [Boolean]
         *
         * - 不可与 [beforeHook]、[afterHook] 同时使用
         */
        fun replaceToFalse() {
            isReplaceHookMode = true
            replaceHookCallback = { false }
        }

        /**
         * 拦截此方法
         *
         * - ❗这将会禁止此方法执行并返回 null
         *
         * - 不可与 [beforeHook]、[afterHook] 同时使用
         */
        fun intercept() {
            isReplaceHookMode = true
            replaceHookCallback = { null }
        }

        /**
         * 移除当前注入的 Hook 方法、构造方法 (解除 Hook)
         *
         * - ❗你只能在 Hook 回调方法中使用此功能
         * @param result 回调是否成功
         */
        fun removeSelf(result: (Boolean) -> Unit = {}) = this.result?.remove(result) ?: result(false)

        /**
         * Hook 创建入口
         * @return [Result]
         */
        @PublishedApi
        internal fun build() = Result().apply { result = this }

        /** Hook 执行入口 */
        @PublishedApi
        internal fun hook() {
            if (YukiHookBridge.hasXposedBridge.not() || isHooked || isDisableMemberRunHook) return
            isHooked = true
            finder?.printLogIfExist()
            if (hookClass.instance == null) {
                (hookClass.throwable ?: Throwable("HookClass [${hookClass.name}] not found")).also {
                    onHookingFailureCallback?.invoke(it)
                    onAllFailureCallback?.invoke(it)
                    if (isNotIgnoredHookingFailure) onHookFailureMsg(it)
                }
                return
            }
            members.takeIf { it.isNotEmpty() }?.forEach { member ->
                runCatching {
                    member.hook().also {
                        when {
                            it.first?.member == null -> error("Hook Member [$member] failed")
                            it.second -> onAlreadyHookedCallback?.invoke(it.first?.member!!)
                            else -> it.first?.also { e ->
                                memberUnhooks.add(e)
                                onHookedCallback?.invoke(e.member!!)
                            }
                        }
                    }
                }.onFailure {
                    onHookingFailureCallback?.invoke(it)
                    onAllFailureCallback?.invoke(it)
                    if (isNotIgnoredHookingFailure) onHookFailureMsg(it, member)
                }
            } ?: Throwable("Finding Error isSetUpMember [$isHookMemberSetup] [$tag]").also {
                onNoSuchMemberFailureCallback?.invoke(it)
                onHookingFailureCallback?.invoke(it)
                onAllFailureCallback?.invoke(it)
                if (isNotIgnoredNoSuchMemberFailure) yLoggerE(
                    msg = "$hostTagName " + (if (isHookMemberSetup)
                        "Hooked Member with a finding error by $hookClass [$tag]"
                    else "Hooked Member cannot be non-null by $hookClass [$tag]"),
                    e = findingThrowable ?: it
                )
            }
        }

        /**
         * Hook 方法、构造方法
         * @return [Pair] - ([YukiMemberHook.Unhook] or null,[Boolean] 是否已经 Hook)
         */
        private fun Member.hook(): Pair<YukiMemberHook.Unhook?, Boolean> {
            /** 定义替换 Hook 的 [HookParam] */
            val replaceHookParam = HookParam(createrInstance = this@YukiMemberHookCreater)

            /** 定义替换 Hook 回调方法体 */
            val replaceMent = object : YukiMemberReplacement(priority) {
                override fun replaceHookedMember(wrapper: HookParamWrapper) =
                    replaceHookParam.assign(wrapper).let { param ->
                        try {
                            if (replaceHookCallback != null) onHookLogMsg(msg = "Replace Hook Member [${this@hook}] done [$tag]")
                            replaceHookCallback?.invoke(param).also { HookParam.invoke() }
                        } catch (e: Throwable) {
                            onConductFailureCallback?.invoke(param, e)
                            onAllFailureCallback?.invoke(e)
                            if (onConductFailureCallback == null && onAllFailureCallback == null) onHookFailureMsg(e)
                            null
                        }
                    }
            }

            /** 定义前 Hook 的 [HookParam] */
            val beforeHookParam = HookParam(createrInstance = this@YukiMemberHookCreater)

            /** 定义后 Hook 的 [HookParam] */
            val afterHookParam = HookParam(createrInstance = this@YukiMemberHookCreater)

            /** 定义前后 Hook 回调方法体 */
            val beforeAfterHook = object : YukiMemberHook(priority) {
                override fun beforeHookedMember(wrapper: HookParamWrapper) {
                    beforeHookParam.assign(wrapper).also { param ->
                        runCatching {
                            beforeHookCallback?.invoke(param)
                            if (beforeHookCallback != null) onHookLogMsg(msg = "Before Hook Member [${this@hook}] done [$tag]")
                            HookParam.invoke()
                        }.onFailure {
                            onConductFailureCallback?.invoke(param, it)
                            onAllFailureCallback?.invoke(it)
                            if (onConductFailureCallback == null && onAllFailureCallback == null) onHookFailureMsg(it)
                        }
                    }
                }

                override fun afterHookedMember(wrapper: HookParamWrapper) {
                    afterHookParam.assign(wrapper).also { param ->
                        runCatching {
                            afterHookCallback?.invoke(param)
                            if (afterHookCallback != null) onHookLogMsg(msg = "After Hook Member [${this@hook}] done [$tag]")
                            HookParam.invoke()
                        }.onFailure {
                            onConductFailureCallback?.invoke(param, it)
                            onAllFailureCallback?.invoke(it)
                            if (onConductFailureCallback == null && onAllFailureCallback == null) onHookFailureMsg(it)
                        }
                    }
                }
            }
            return YukiHookHelper.hookMethod(hookMethod = this, if (isReplaceHookMode) replaceMent else beforeAfterHook)
        }

        /**
         * Hook 过程中开启了 [YukiHookAPI.Configs.isDebug] 输出调试信息
         * @param msg 调试日志内容
         */
        private fun onHookLogMsg(msg: String) {
            if (YukiHookAPI.Configs.isDebug) yLoggerI(msg = "$hostTagName $msg")
        }

        /**
         * Hook 失败但未设置 [onAllFailureCallback] 将默认输出失败信息
         * @param throwable 异常信息
         * @param member 异常 [Member] - 可空
         */
        private fun onHookFailureMsg(throwable: Throwable, member: Member? = null) = yLoggerE(
            msg = "$hostTagName Try to hook [${hookClass.instance ?: hookClass.name}]${member?.let { "[$it]" } ?: ""} got an Exception [$tag]",
            e = throwable
        )

        /**
         * 判断是否没有设置 Hook 过程中的任何异常拦截
         * @return [Boolean] 没有设置任何异常拦截
         */
        private val isNotIgnoredHookingFailure get() = onHookingFailureCallback == null && onAllFailureCallback == null

        /**
         * 判断是否没有设置 Hook 过程中 [members] 找不到的任何异常拦截
         * @return [Boolean] 没有设置任何异常拦截
         */
        internal val isNotIgnoredNoSuchMemberFailure get() = onNoSuchMemberFailureCallback == null && isNotIgnoredHookingFailure

        /**
         * 获取 Hook APP (宿主) 标签
         * @return [String]
         */
        internal val hostTagName get() = if (packageParam.appUserId != 0) "[$packageName][${packageParam.appUserId}]" else "[$packageName]"

        override fun toString() = "[tag] $tag [priority] $priority [class] $hookClass [members] $members"

        /**
         * 监听 Hook 结果实现类
         *
         * 可在这里处理失败事件监听
         */
        inner class Result internal constructor() {

            /**
             * 创建监听事件方法体
             * @param initiate 方法体
             * @return [Result] 可继续向下监听
             */
            inline fun result(initiate: Result.() -> Unit) = apply(initiate)

            /**
             * 添加执行 Hook 需要满足的条件
             *
             * 不满足条件将直接停止 Hook
             * @param condition 条件方法体
             * @return [Result] 可继续向下监听
             */
            inline fun by(condition: () -> Boolean): Result {
                isDisableMemberRunHook = (runCatching { condition() }.getOrNull() ?: false).not()
                if (isDisableMemberRunHook) ignoredAllFailure()
                return this
            }

            /**
             * 监听 [members] Hook 成功的回调方法
             *
             * 在首次 Hook 成功后回调
             *
             * 在重复 Hook 时会回调 [onAlreadyHooked]
             * @param result 回调被 Hook 的 [Member]
             * @return [Result] 可继续向下监听
             */
            fun onHooked(result: (Member) -> Unit): Result {
                onHookedCallback = result
                return this
            }

            /**
             * 监听 [members] 重复 Hook 的回调方法
             *
             * - ❗同一个 [hookClass] 中的同一个 [members] 不会被 API 重复 Hook - 若由于各种原因重复 Hook 会回调此方法
             * @param result 回调被重复 Hook 的 [Member]
             * @return [Result] 可继续向下监听
             */
            fun onAlreadyHooked(result: (Member) -> Unit): Result {
                onAlreadyHookedCallback = result
                return this
            }

            /**
             * 监听 [members] 不存在发生错误的回调方法
             * @param result 回调错误
             * @return [Result] 可继续向下监听
             */
            fun onNoSuchMemberFailure(result: (Throwable) -> Unit): Result {
                onNoSuchMemberFailureCallback = result
                return this
            }

            /**
             * 忽略 [members] 不存在发生的错误
             * @return [Result] 可继续向下监听
             */
            fun ignoredNoSuchMemberFailure() = onNoSuchMemberFailure {}

            /**
             * 监听 Hook 进行过程中发生错误的回调方法
             * @param result 回调错误 - ([HookParam] 当前 Hook 实例,[Throwable] 异常)
             * @return [Result] 可继续向下监听
             */
            fun onConductFailure(result: (HookParam, Throwable) -> Unit): Result {
                onConductFailureCallback = result
                return this
            }

            /**
             * 忽略 Hook 进行过程中发生的错误
             * @return [Result] 可继续向下监听
             */
            fun ignoredConductFailure() = onConductFailure { _, _ -> }

            /**
             * 监听 Hook 开始时发生错误的回调方法
             * @param result 回调错误
             * @return [Result] 可继续向下监听
             */
            fun onHookingFailure(result: (Throwable) -> Unit): Result {
                onHookingFailureCallback = result
                return this
            }

            /**
             * 忽略 Hook 开始时发生的错误
             * @return [Result] 可继续向下监听
             */
            fun ignoredHookingFailure() = onHookingFailure {}

            /**
             * 监听全部 Hook 过程发生错误的回调方法
             * @param result 回调错误
             * @return [Result] 可继续向下监听
             */
            fun onAllFailure(result: (Throwable) -> Unit): Result {
                onAllFailureCallback = result
                return this
            }

            /**
             * 忽略全部 Hook 过程发生的错误
             * @return [Result] 可继续向下监听
             */
            fun ignoredAllFailure() = onAllFailure {}

            /**
             * 移除当前注入的 Hook 方法、构造方法 (解除 Hook)
             *
             * - ❗你只能在 Hook 成功后才能解除 Hook - 可监听 [onHooked] 事件
             * @param result 回调是否成功
             */
            fun remove(result: (Boolean) -> Unit = {}) {
                memberUnhooks.takeIf { it.isNotEmpty() }?.apply {
                    forEach {
                        it.remove()
                        onHookLogMsg(msg = "Remove Hooked Member [${it.member}] done [$tag]")
                    }
                    runCatching { preHookMembers.remove(this@MemberHookCreater.toString()) }
                    clear()
                    result(true)
                } ?: result(false)
            }
        }
    }

    /**
     * 监听全部 Hook 结果实现类
     *
     * 可在这里处理失败事件监听
     */
    inner class Result internal constructor() {

        /** Hook 开始时的监听事件回调 */
        internal var onPrepareHook: (() -> Unit)? = null

        /**
         * 创建监听事件方法体
         * @param initiate 方法体
         * @return [Result] 可继续向下监听
         */
        inline fun result(initiate: Result.() -> Unit) = apply(initiate)

        /**
         * 添加执行 Hook 需要满足的条件
         *
         * 不满足条件将直接停止 Hook
         * @param condition 条件方法体
         * @return [Result] 可继续向下监听
         */
        inline fun by(condition: () -> Boolean): Result {
            isDisableCreaterRunHook = (runCatching { condition() }.getOrNull() ?: false).not()
            return this
        }

        /**
         * 监听 [hookClass] 存在时准备开始 Hook 的操作
         * @param callback 准备开始 Hook 后回调
         * @return [Result] 可继续向下监听
         */
        fun onPrepareHook(callback: () -> Unit): Result {
            onPrepareHook = callback
            return this
        }

        /**
         * 监听 [hookClass] 找不到时发生错误的回调方法
         * @param result 回调错误
         * @return [Result] 可继续向下监听
         */
        fun onHookClassNotFoundFailure(result: (Throwable) -> Unit): Result {
            onHookClassNotFoundFailureCallback = result
            return this
        }

        /**
         * 忽略 [hookClass] 找不到时出现的错误
         * @return [Result] 可继续向下监听
         */
        fun ignoredHookClassNotFoundFailure(): Result {
            by { hookClass.instance != null }
            return this
        }
    }
}