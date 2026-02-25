# SWR for Compose

[![Maven Central](https://img.shields.io/maven-central/v/com.kazakago.swr/swr-compose.svg)](https://central.sonatype.com/namespace/com.kazakago.swr)
[![javadoc](https://javadoc.io/badge2/com.kazakago.swr/swr-compose/javadoc.svg)](https://javadoc.io/doc/com.kazakago.swr/swr-compose)
[![Check](https://github.com/kazakago/swr-compose/actions/workflows/check.yml/badge.svg?branch=main)](https://github.com/kazakago/swr-compose/actions/workflows/check.yml?query=branch%3Amain)
[![License](https://img.shields.io/github/license/kazakago/swr-compose.svg)](LICENSE)

This library is inspired by [React SWR](https://swr.vercel.app) ported for [Jetpack Compose](https://developer.android.com/jetpack/compose) and [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/).  
Supported platforms are Android, Desktop, iOS and Web.

The API specification of `React SWR` is followed as much as possible.  
Options are also supported for the most part.

## What's "SWR"？

According to `React SWR`, "SWR" refers to

> The name “SWR” is derived from stale-while-revalidate, a HTTP cache invalidation strategy popularized by [HTTP RFC 5861](https://www.rfc-editor.org/rfc/rfc5861). SWR is a strategy to first return the data from cache (stale), then send the fetch request (revalidate), and finally come with the up-to-date data.

## Requirement

- Android
    - Android 6.0 (API level 23) or later
- iOS/Desktop/Web
    - https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-compatibility-and-versioning.html#supported-platforms

## Install

Add the following gradle dependency exchanging `*.*.*` for the latest release. [![Maven Central](https://img.shields.io/maven-central/v/com.kazakago.swr/swr-compose.svg)](https://central.sonatype.com/namespace/com.kazakago.swr)

```kotlin
implementation("com.kazakago.swr:swr-compose:*.*.*")
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

Live demo of Kotlin/Wasm is [**here**](https://kazakago.github.io/swr-compose/).

Refer to the [**example module**](exampleApp) for details. This module works as a Compose Multiplatform app.

## Supported Features

| Feature name                                                                  | Note                                    |
|-------------------------------------------------------------------------------|-----------------------------------------|
| [Global Configuration](https://swr.vercel.app/docs/global-configuration)      |                                         |
| [Data Fetching](https://swr.vercel.app/docs/data-fetching)                    |                                         |
| [Error Handling](https://swr.vercel.app/docs/error-handling)                  |                                         |
| [Auto Revalidation](https://swr.vercel.app/docs/revalidation)                 |                                         |
| [Conditional Data Fetching](https://swr.vercel.app/docs/conditional-fetching) |                                         |
| [Arguments](https://swr.vercel.app/docs/arguments)                            |                                         |
| [Mutation](https://swr.vercel.app/docs/mutation)                              | Available by `rememberSWRMutation()`    |
| [Pagination](https://swr.vercel.app/docs/pagination)                          | Available by `rememberSWRInfinite()`    |
| [Prefetching Data](https://swr.vercel.app/docs/prefetching)                   | Available by `rememberSWRPreload()`     |

## Supported Options

The following options are supported for React SWR.  
https://swr.vercel.app/docs/options

| Option name           | Default value                                                              | Description                                        |
|-----------------------|----------------------------------------------------------------------------|----------------------------------------------------|
| revalidateIfStale     | true                                                                       | Revalidate when cached data is stale               |
| revalidateOnMount     | true                                                                       | Revalidate when component mounts                   |
| revalidateOnFocus     | true                                                                       | Revalidate when app regains focus                  |
| revalidateOnReconnect | true                                                                       | Revalidate when network reconnects                 |
| refreshInterval       | 0.seconds                                                                  | Polling interval for periodic refresh (0=disabled) |
| refreshWhenHidden     | false                                                                      | Continue polling when app is in background          |
| refreshWhenOffline    | false                                                                      | Continue polling when offline                       |
| shouldRetryOnError    | true                                                                       | Retry fetching on error                            |
| dedupingInterval      | 2.seconds                                                                  | Time window to deduplicate identical requests      |
| focusThrottleInterval | 5.seconds                                                                  | Min interval between focus-triggered revalidations |
| loadingTimeout        | 3.seconds                                                                  | Timeout before `onLoadingSlow` callback fires      |
| errorRetryInterval    | 5.seconds                                                                  | Wait time between error retries                    |
| errorRetryCount       |                                                                            | Maximum number of error retries                    |
| fallbackData          |                                                                            | Data to display while loading                      |
| onLoadingSlow         |                                                                            | Callback when loading exceeds `loadingTimeout`     |
| onSuccess             |                                                                            | Callback on successful data fetch                  |
| onError               |                                                                            | Callback on fetch error                            |
| onErrorRetry          | [Exponential backoff](https://en.wikipedia.org/wiki/Exponential_backoff)   | Custom error retry handler                         |

## Mutation

`rememberSWRMutation()` provides a way to mutate remote data. Unlike `rememberSWR()`, it does not automatically fetch data — mutations are triggered manually.

```kotlin
private val fetcher: suspend (key: String, arg: String) -> String = { key, arg ->
    updateNameApi.execute(key, arg)
}

@Composable
fun UpdateProfile() {
    val (trigger, isMutating) = rememberSWRMutation("/api/user", fetcher)

    Button(
        onClick = { scope.launch { trigger("new name") } },
        enabled = !isMutating,
    ) {
        Text(if (isMutating) "Updating..." else "Update")
    }
}
```

### Mutation Options

| Option name     | Default value | Description                                          |
|-----------------|---------------|------------------------------------------------------|
| optimisticData  |               | Data to display immediately before mutation resolves |
| revalidate      | false         | Revalidate cached data after mutation                |
| populateCache   | false         | Update cache with mutation result                    |
| rollbackOnError | true          | Revert to previous data if mutation fails            |
| onSuccess       |               | Callback on successful mutation                      |
| onError         |               | Callback on mutation error                           |

## Pagination / Infinite Loading

`rememberSWRInfinite()` provides paginated data fetching with dynamic page management.

```kotlin
@Composable
fun UserList() {
    val (data, error, isValidating, _, size, setSize) = rememberSWRInfinite(
        getKey = { pageIndex, previousPageData ->
            if (previousPageData != null && previousPageData.isEmpty()) null // reached the end
            else "/api/users?page=$pageIndex"
        },
        fetcher = { key -> fetchUsers(key) },
    )

    // data is List<List<User>?> — one entry per page
    data?.flatten()?.forEach { user ->
        Text(user.name)
    }

    Button(onClick = { setSize(size + 1) }) {
        Text("Load More")
    }
}
```

Pagination-specific options can be set in the config block:

| Option name         | Default value | Description                              |
|---------------------|---------------|------------------------------------------|
| initialSize         | 1             | Number of pages to load initially        |
| revalidateAll       | false         | Revalidate all loaded pages              |
| revalidateFirstPage | true          | Revalidate the first page on changes     |
| persistSize         | false         | Persist the page count across re-mounts  |

## Immutable Data

`rememberSWRImmutable()` fetches data once and never revalidates automatically. Useful for data that doesn't change (e.g., static resources, user settings loaded once).

```kotlin
@Composable
fun StaticContent() {
    val (data, error) = rememberSWRImmutable("/api/config", fetcher)
    // ...
}
```

This is equivalent to `rememberSWR()` with all revalidation options disabled.

## Persistent Cache

You can provide a `Persister` to save fetched data to local storage (e.g., database, file system). This allows data to survive app restarts.

```kotlin
val persister = Persister<String, User>(
    loadData = { key -> database.getUser(key) },
    saveData = { key, data -> database.saveUser(key, data) },
)

@Composable
fun Profile() {
    val (data, error) = rememberSWR("/api/user", fetcher, persister)
    // On first load, persister.loadData is called for immediate display,
    // then fetcher runs in background to revalidate.
}
```

## Notice for performance

[Deduplication](https://swr.vercel.app/docs/advanced/performance#deduplication) and [Deep Comparison](https://swr.vercel.app/docs/advanced/performance#deep-comparison) are supported, but [Dependency Collection](https://swr.vercel.app/docs/advanced/performance#dependency-collection) is not supported due to Kotlin's language specification.  
Therefore, the number of re-rendering (re-compose) may be higher than in the original `React SWR`.

However, it is possible to prevent performance degradation by limiting the number of arguments passed to child Composable functions (e.g., only data).  
