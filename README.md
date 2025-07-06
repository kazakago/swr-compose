# SWR for Compose

[![Maven Central](https://img.shields.io/maven-central/v/com.kazakago.swr.compose/swr.svg)](https://central.sonatype.com/namespace/com.kazakago.swr.compose)
[![javadoc](https://javadoc.io/badge2/com.kazakago.swr.compose/swr/javadoc.svg)](https://javadoc.io/doc/com.kazakago.swr.compose/swr)
[![Check](https://github.com/KazaKago/swr-compose/actions/workflows/check.yml/badge.svg?branch=main)](https://github.com/KazaKago/swr-compose/actions/workflows/check.yml?query=branch%3Amain)
[![License](https://img.shields.io/github/license/kazakago/swr-compose.svg)](LICENSE)

This library is inspired by [React SWR](https://swr.vercel.app) ported for [Jetpack Compose](https://developer.android.com/jetpack/compose) and [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/).  
Supported platforms are Android, Desktop, iOS and Web.

The API specification of `React SWR` is followed as much as possible.  
Options are also supported for the most part.

## What's "SWR"？

According to `React SWR`, "SWR" refers to

> The name “SWR” is derived from stale-while-revalidate, a HTTP cache invalidation strategy popularized by [HTTP RFC 5861](https://www.rfc-editor.org/rfc/rfc5861). SWR is a strategy to first return the data from cache (stale), then send the fetch request (revalidate), and finally come with the up-to-date data.

## Install

Add the following gradle dependency exchanging `*.*.*` for the latest release. [![Maven Central](https://img.shields.io/maven-central/v/com.kazakago.swr.compose/swr.svg)](https://central.sonatype.com/namespace/com.kazakago.swr.compose)

```kotlin
implementation("com.kazakago.swr.compose:swr-compose:*.*.*")
```

## Getting Started

As with `React SWR`, implement the "fetcher function" and set it with the key to `rememberSWR()`.  
Using Kotlin's [Destructuring declarations](https://kotlinlang.org/docs/destructuring-declarations.html) for return values can be written in the same way as in `React SWR`.

```kotlin
private val fetcher: suspend (key: String) -> String = {
    getNameApi.execute(key)
}

@Composable
fun Profile() {
    val (data, error) = rememberSWR("/api/user", fetcher)

    if (error != null) {
        Text("failed to load")
    } else if (data == null) {
        Text("loading...")
    } else {
        Text("hello $data!")
    }
}
```

## Example

Refer to the [**example module**](exampleApp) for details. This module works as an Compose Multiplatform app.

## Supported Features

| Feature name                                                                  | Note                           |
|-------------------------------------------------------------------------------|--------------------------------|
| [Global Configuration](https://swr.vercel.app/docs/global-configuration)      |                                |
| [Data Fetching](https://swr.vercel.app/docs/data-fetching)                    |                                |
| [Error Handling](https://swr.vercel.app/docs/error-handling)                  |                                |
| [Auto Revalidation](https://swr.vercel.app/docs/revalidation)                 |                                |
| [Conditional Data Fetching](https://swr.vercel.app/docs/conditional-fetching) |                                |
| [Arguments](https://swr.vercel.app/docs/arguments)                            |                                |
| [Mutation](https://swr.vercel.app/docs/mutation)                              |                                |
| [Pagination](https://swr.vercel.app/docs/pagination)                          |                                |
| [Prefetching Data](https://swr.vercel.app/docs/prefetching)                   | Available by `useSWRPreload()` |

## Supported Options

The following options are supported for React SWR.  
https://swr.vercel.app/docs/options

| Option name                                               | Default value                                                                      |
|-----------------------------------------------------------|------------------------------------------------------------------------------------|
| revalidateIfStale                                         | true                                                                               |
| revalidateOnMount                                         | true                                                                               |
| revalidateOnFocus                                         | true                                                                               |
| revalidateOnReconnect                                     | true                                                                               |
| refreshInterval                                           | 0.seconds (disable)                                                                |
| refreshWhenHidden                                         | false                                                                              |
| refreshWhenOffline                                        | false                                                                              |
| shouldRetryOnError                                        | true                                                                               |
| dedupingInterval                                          | 2.seconds                                                                          |
| focusThrottleInterval                                     | 5.seconds                                                                          |
| loadingTimeout                                            | 3.seconds                                                                          |
| errorRetryInterval                                        | 5.seconds                                                                          |
| errorRetryCount                                           |                                                                                    |
| fallback                                                  |                                                                                    |
| keepPreviousData                                          | false                                                                              |
| onLoadingSlow(key, config)                                |                                                                                    |
| onSuccess(data, key, config)                              |                                                                                    |
| onError(err, key, config)                                 |                                                                                    |
| onErrorRetry(err, key, config, revalidate, revalidateOps) | [Exponential backoff](https://en.wikipedia.org/wiki/Exponential_backoff) algorithm |

## Notice for performance

[Deduplication](https://swr.vercel.app/docs/advanced/performance#deduplication) and [Deep Comparison](https://swr.vercel.app/docs/advanced/performance#deep-comparison) are supported, but [Dependency Collection](https://swr.vercel.app/docs/advanced/performance#dependency-collection) is not supported due to Kotlin's language specification.  
Therefore, the number of re-rendering (re-compose) may be higher than in the original `React SWR`.

However, it is possible to prevent performance degradation by limiting the number of arguments passed to child Composable functions (e.g., only data).  
