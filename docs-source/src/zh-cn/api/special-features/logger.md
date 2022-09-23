# 调试日志

> 日志是调试过程最重要的一环，`YukiHookAPI` 为开发者封装了一套稳定高效的调试日志功能。

## 普通日志

你可以调用 `loggerD`、`loggerI`、`loggerW` 来向控制台打印普通日志。

使用方法如下所示。

> 示例如下

```kotlin
loggerD(msg = "This is a log")
```

此时，`YukiHookAPI` 会调用 `android.util.Log` 与 `XposedBridge.log` 同时打印这条日志。

日志默认的 `TAG` 为你在 `YukiHookAPI.Configs.debugTag` 中设置的值。

你也可以动态自定义这个值，但是不建议轻易修改 `TAG` 防止过滤不到日志。

> 示例如下

```kotlin
loggerD(tag = "YukiHookAPI", msg = "This is a log")
```

打印的结果为如下所示。

> 示例如下

```:no-line-numbers
[YukiHookAPI][D][宿主包名]--> This is a log
```

你还可以使用 `LoggerType` 自定义日志打印的类型，可选择使用 `android.util.Log` 还是 `XposedBridge.log` 来打印日志。

默认类型为 `LoggerType.BOTH`，含义为同时使用这两个方法来打印日志。

比如我们仅使用 `android.util.Log` 来打印日志。

> 示例如下

```kotlin
loggerD(tag = "YukiHookAPI", msg = "This is a log", type = LoggerType.LOGD)
```

或又仅使用 `XposedBridge.log` 来打印日志，此方法仅可在 (Xposed) 宿主环境使用。

> 示例如下

```kotlin
loggerD(tag = "YukiHookAPI", msg = "This is a log", type = LoggerType.XPOSEDBRIDGE)
```

若你想智能区分 (Xposed) 宿主环境与模块环境，可以写为如下形式。

> 示例如下

```kotlin
loggerD(tag = "YukiHookAPI", msg = "This is a log", type = LoggerType.SCOPE)
```

这样 API 就会在不同环境智能选择指定的方法类型去打印这条日志。

::: tip

更多功能请参考 [loggerD](../public/com/highcapable/yukihookapi/hook/log/LoggerFactory#loggerd-method)、[loggerI](../public/com/highcapable/yukihookapi/hook/log/LoggerFactory#loggeri-method) 及 [loggerW](../public/com/highcapable/yukihookapi/hook/log/LoggerFactory#loggerw-method) 方法。

:::

## 错误日志

你可以调用 `loggerE` 来向控制台打印 `E` 级别的日志。

使用方法如下所示。

> 示例如下

```kotlin
loggerE(msg = "This is an error")
```

错误日志的级别是最高的，无论你有没有过滤仅为 `E` 级别的日志。

对于错误级别的日志，你还可以在后面加上一个异常堆栈。

```kotlin
// 假设这就是被抛出的异常
val throwable = Throwable(...)
// 打印日志
loggerE(msg = "This is an error", e = throwable)
```

打印的结果为如下所示。

> 示例如下

```:no-line-numbers
[YukiHookAPI][E][宿主包名]--> This is an error
```

同时，日志会帮你打印整个异常堆栈。

> 示例如下

```:no-line-numbers
java.lang.Throwable
        at com.demo.Test.<init>(...) 
        at com.demo.Test.doTask(...) 
        at com.demo.Test.stop(...) 
        at com.demo.Test.init(...) 
        at a.a.a(...) 
        ... 3 more
```

在错误日志中，你同样也可以使用 `LoggerType` 来指定当前打印日志所用到的方法类型。

::: tip

更多功能请参考 [loggerE](../public/com/highcapable/yukihookapi/hook/log/LoggerFactory#loggere-method) 方法。

:::

## 保存日志与自定义元素

你可以使用 `YukiHookLogger.saveToFile` 方法直接保存当前已打印的全部日志到文件。

> 示例如下

```kotlin
// 请注意保存的文件路径必须拥有读写权限，否则会抛出异常
YukiHookLogger.saveToFile("/sdcard/Documents/debug_log.log")
```

你还可以使用 `YukiHookLogger.contents` 获取当前已打印的全部日志文件内容。

> 示例如下

```kotlin
// 获取当前已打印的全部日志文件内容
val fileContent = YukiHookLogger.contents
```

以上功能需要启用 `YukiHookLogger.Configs.isRecord`。

你还可以使用 `YukiHookLogger.Configs.elements` 自定义调试日志对外显示的元素。

此功能需要在 Hook 入口类的 `onInit` 中对 `YukiHookAPI.Configs` 进行配置。

> 示例如下

```kotlin
override fun onInit() = configs {
    debugLog {
        // ...
        elements(TAG, PRIORITY, PACKAGE_NAME, USER_ID)
    }
    // ...
}
```

::: tip

更多功能请参考 [YukiHookLogger.contents](../public/com/highcapable/yukihookapi/hook/log/LoggerFactory#contents-field)、[YukiHookLogger.saveToFile](../public/com/highcapable/yukihookapi/hook/log/LoggerFactory#savetofile-method) 方法以及 [YukiHookLogger.Configs](../public/com/highcapable/yukihookapi/hook/log/LoggerFactory#configs-object)。

:::