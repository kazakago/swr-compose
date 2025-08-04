# SWR for Compose

[![Maven Central](https://img.shields.io/maven-central/v/com.kazakago.swr.compose/swr.svg)](https://central.sonatype.com/namespace/com.kazakago.swr.compose)
[![javadoc](https://javadoc.io/badge2/com.kazakago.swr.compose/swr/javadoc.svg)](https://javadoc.io/doc/com.kazakago.swr.compose/swr)
[![Test](https://github.com/KazaKago/swr-compose/actions/workflows/test.yml/badge.svg?branch=main)](https://github.com/KazaKago/swr-compose/actions/workflows/test.yml?query=branch%3Amain)
[![License](https://img.shields.io/github/license/kazakago/swr-compose.svg)](LICENSE)

[React SWR](https://swr.vercel.app)を[Jetpack Compose](https://developer.android.com/jetpack/compose)と[Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/) 向けに移植したクローンライブラリです。  
サポートされているプラットフォームはAndroid、Desktop(JVM)、iOS、Webです。  

オリジナルの`React SWR`のAPI仕様をできる限り踏襲しており、オプションの大部分をサポートしています。  

## "SWR"とは？

React SWRによると、"SWR"とは下記を指します。  

> “SWR” という名前は、 [HTTP RFC 5861](https://www.rfc-editor.org/rfc/rfc5861) で提唱された HTTP キャッシュ無効化戦略である stale-while-revalidate に由来しています。 SWR は、まずキャッシュからデータを返し（stale）、次にフェッチリクエストを送り（revalidate）、最後に最新のデータを持ってくるという戦略です。

## インストール

下記の `*.*.*` を最新のバージョンに置き換えてbuild.gradleのdependenciesに追加してください。 [![Maven Central](https://img.shields.io/maven-central/v/com.kazakago.swr.compose/swr.svg)](https://central.sonatype.com/namespace/com.kazakago.swr.compose)  

```kotlin
implementation("com.kazakago.swr.compose:swr:*.*.*")
```

## クイックスタート

オリジナルの`React SWR`と同様に、fetcher関数を用意したら`useSWR()`にキーとともにセットするだけです。  
戻り値に対してKotlinの[分解宣言](https://kotlinlang.org/docs/destructuring-declarations.html) を用いると`React SWR`と同じように記述できます。  

```kotlin
private val fetcher: suspend (key: String) -> String = {
    getNameApi.execute(key)
}

@Composable
fun Profile() {
    val (data, error) = useSWR("/api/user", fetcher)

    if (error != null) {
        Text("failed to load")
    } else if (data == null) {
        Text("loading...")
    } else {
        Text("hello $data!")
    }
}
```

## 例

詳細な実装例は[**exampleモジュール**](composeApp)を参照して下さい。Compose Multiplatformアプリとして実行できます。

## サポートされている機能

| Feature名                                                                      | サポート                    | 補足                        |
|-------------------------------------------------------------------------------|-------------------------|---------------------------|
| [Options](https://swr.vercel.app/docs/options)                                | [下記参照](#サポートされているオプション) |                           |
| [Global Configuration](https://swr.vercel.app/docs/global-configuration)      | ✅                       |                           |
| [Data Fetching](https://swr.vercel.app/docs/data-fetching)                    | ✅                       |                           |
| [Error Handling](https://swr.vercel.app/docs/error-handling)                  | ✅                       |                           |
| [Auto Revalidation](https://swr.vercel.app/docs/revalidation)                 | ✅                       |                           |
| [Conditional Data Fetching](https://swr.vercel.app/docs/conditional-fetching) | ✅                       |                           |
| [Arguments](https://swr.vercel.app/docs/arguments)                            | ✅                       |                           |
| [Mutation](https://swr.vercel.app/docs/mutation)                              | ✅️                      |                           |
| [Pagination](https://swr.vercel.app/docs/pagination)                          | ✅                       |                           |
| [Prefetching Data](https://swr.vercel.app/docs/prefetching)                   | ✅️                      | `useSWRPreload()`から利用できます |
| [Suspense](https://swr.vercel.app/docs/suspense)                              | ❌                       |                           |
| [Middleware](https://swr.vercel.app/docs/middleware)                          | ❌                       |                           |

## サポートされているオプション

React SWRに対して、以下のオプションをサポートしています。  
https://swr.vercel.app/docs/options

| オプション名                                                    | サポート | デフォルト値                                                              |
|-----------------------------------------------------------|------|---------------------------------------------------------------------|
| suspense                                                  | ❌    |                                                                     |
| fetcher(args)                                             | ✅    |                                                                     |
| revalidateIfStale                                         | ✅    | true                                                                |
| revalidateOnMount                                         | ✅    | `fallbackData`が設定されていないときのみtrue                                     |
| revalidateOnFocus                                         | ✅    | true                                                                |
| revalidateOnReconnect                                     | ✅    | true                                                                |
| refreshInterval                                           | ✅    | 0.seconds (無効)                                                      |
| refreshWhenHidden                                         | ✅    | false                                                               |
| refreshWhenOffline                                        | ✅    | false                                                               |
| shouldRetryOnError                                        | ✅    | true                                                                |
| dedupingInterval                                          | ✅    | 2.seconds                                                           |
| focusThrottleInterval                                     | ✅    | 5.seconds                                                           |
| loadingTimeout                                            | ✅    | 3.seconds                                                           |
| errorRetryInterval                                        | ✅    | 5.seconds                                                           |
| errorRetryCount                                           | ✅    |                                                                     |
| fallback                                                  | ✅    |                                                                     |
| fallbackData                                              | ✅    |                                                                     |
| keepPreviousData                                          | ✅    | false                                                               |
| onLoadingSlow(key, config)                                | ✅    |                                                                     |
| onSuccess(data, key, config)                              | ✅    |                                                                     |
| onError(err, key, config)                                 | ✅    |                                                                     |
| onErrorRetry(err, key, config, revalidate, revalidateOps) | ✅    | [指数バックオフ](https://en.wikipedia.org/wiki/Exponential_backoff) アルゴリズム |
| compare(a, b)                                             | ❌    |                                                                     |
| isPaused()                                                | ✅    | false                                                               |
| use                                                       | ❌    |                                                                     |

## パフォーマンスについて

[Deduplication](https://swr.vercel.app/docs/advanced/performance#deduplication) や[Deep Comparison](https://swr.vercel.app/docs/advanced/performance#deep-comparison) はサポートされていますが、Kotlinの言語仕様上[Dependency Collection](https://swr.vercel.app/docs/advanced/performance#dependency-collection) には対応しないため、本家React SWRに比べると単純な再レンダリング（re-compose）の回数は多くなることがあります。  

しかしながら、子のComposable関数へ渡す引数を（例えばdataのみにするなど）絞ることでパフォーマンスの低下を防ぐことが可能です。  
