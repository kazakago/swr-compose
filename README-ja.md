# SWR for Compose

[![Maven Central](https://img.shields.io/maven-central/v/com.kazakago.swr/swr-compose.svg)](https://central.sonatype.com/namespace/com.kazakago.swr)
[![javadoc](https://javadoc.io/badge2/com.kazakago.swr/swr-compose/javadoc.svg)](https://javadoc.io/doc/com.kazakago.swr/swr-compose)
[![Check](https://github.com/kazakago/swr-compose/actions/workflows/check.yml/badge.svg?branch=main)](https://github.com/kazakago/swr-compose/actions/workflows/check.yml?query=branch%3Amain)
[![License](https://img.shields.io/github/license/kazakago/swr-compose.svg)](LICENSE)

[React SWR](https://swr.vercel.app) にインスパイアされた、[Jetpack Compose](https://developer.android.com/jetpack/compose) および [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/) 向けのデータフェッチングライブラリです。  
対応プラットフォームは Android、Desktop、iOS、Web です。

`React SWR` の API 仕様にできる限り準拠しています。  
オプションについてもほぼ対応しています。

## 「SWR」とは？

`React SWR` によると、「SWR」とは以下を指します。

> 「SWR」という名前は、[HTTP RFC 5861](https://www.rfc-editor.org/rfc/rfc5861) で広まった HTTP キャッシュ無効化戦略である stale-while-revalidate に由来しています。SWR は、まずキャッシュからデータを返し（stale）、次にフェッチリクエストを送り（revalidate）、最終的に最新のデータを提供する戦略です。

## 動作要件

- Android
    - Android 6.0（API レベル 23）以降
- iOS/Desktop/Web
    - https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-compatibility-and-versioning.html#supported-platforms

## インストール

以下の Gradle 依存関係を追加してください。`*.*.*` は最新バージョンに置き換えてください。 [![Maven Central](https://img.shields.io/maven-central/v/com.kazakago.swr/swr-compose.svg)](https://central.sonatype.com/namespace/com.kazakago.swr)

```kotlin
implementation("com.kazakago.swr:swr-compose:*.*.*")
```

## はじめに

`React SWR` と同様に、「fetcher 関数」を実装し、キーとともに `rememberSWR()` に設定します。  
Kotlin の[分割宣言](https://kotlinlang.org/docs/destructuring-declarations.html)を使うことで、`React SWR` と同じような書き方ができます。

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

## サンプル

Kotlin/Wasm のライブデモは[**こちら**](https://kazakago.github.io/swr-compose/)です。

詳細は [**example モジュール**](exampleApp) を参照してください。このモジュールは Compose Multiplatform アプリとして動作します。

## 対応機能

| 機能名                                                                         | 備考                                     |
|-------------------------------------------------------------------------------|------------------------------------------|
| [グローバル設定](https://swr.vercel.app/docs/global-configuration)              |                                          |
| [データフェッチング](https://swr.vercel.app/docs/data-fetching)                  |                                          |
| [エラーハンドリング](https://swr.vercel.app/docs/error-handling)                 |                                          |
| [自動再検証](https://swr.vercel.app/docs/revalidation)                          |                                          |
| [条件付きデータフェッチング](https://swr.vercel.app/docs/conditional-fetching)    |                                          |
| [引数](https://swr.vercel.app/docs/arguments)                                  |                                          |
| [ミューテーション](https://swr.vercel.app/docs/mutation)                         | `rememberSWRMutation()` で利用可能        |
| [ページネーション](https://swr.vercel.app/docs/pagination)                       | `rememberSWRInfinite()` で利用可能        |
| [データのプリフェッチ](https://swr.vercel.app/docs/prefetching)                   | `rememberSWRPreload()` で利用可能         |

## 対応オプション

React SWR の以下のオプションに対応しています。  
https://swr.vercel.app/docs/options

| オプション名            | デフォルト値                                                                   | 説明                                             |
|------------------------|------------------------------------------------------------------------------|--------------------------------------------------|
| revalidateIfStale      | true                                                                         | キャッシュが古い場合に再検証する                     |
| revalidateOnMount      | true                                                                         | コンポーネントのマウント時に再検証する                |
| revalidateOnFocus      | true                                                                         | アプリがフォーカスを取得した際に再検証する             |
| revalidateOnReconnect  | true                                                                         | ネットワーク再接続時に再検証する                      |
| refreshInterval        | 0.seconds                                                                    | 定期リフレッシュの間隔（0=無効）                     |
| refreshWhenHidden      | false                                                                        | バックグラウンド時もポーリングを継続する               |
| refreshWhenOffline     | false                                                                        | オフライン時もポーリングを継続する                    |
| shouldRetryOnError     | true                                                                         | エラー時にフェッチをリトライする                      |
| dedupingInterval       | 2.seconds                                                                    | 同一リクエストを重複排除する時間窓                    |
| focusThrottleInterval  | 5.seconds                                                                    | フォーカス起因の再検証の最小間隔                      |
| loadingTimeout         | 3.seconds                                                                    | `onLoadingSlow` コールバック発火までのタイムアウト     |
| errorRetryInterval     | 5.seconds                                                                    | エラーリトライの待機時間                              |
| errorRetryCount        |                                                                              | エラーリトライの最大回数                              |
| fallbackData           |                                                                              | ローディング中に表示するデータ                        |
| onLoadingSlow          |                                                                              | ローディングが `loadingTimeout` を超えた時のコールバック |
| onSuccess              |                                                                              | データフェッチ成功時のコールバック                     |
| onError                |                                                                              | フェッチエラー時のコールバック                        |
| onErrorRetry           | [指数バックオフ](https://en.wikipedia.org/wiki/Exponential_backoff)            | カスタムエラーリトライハンドラ                        |

## ミューテーション

`rememberSWRMutation()` はリモートデータを変更する手段を提供します。  
`rememberSWR()` とは異なり、自動的にデータをフェッチせず、ミューテーションは手動でトリガーします。

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

### ミューテーションオプション

| オプション名    | デフォルト値 | 説明                                            |
|----------------|------------|------------------------------------------------|
| optimisticData |            | ミューテーション完了前に即座に表示するデータ         |
| revalidate     | false      | ミューテーション後にキャッシュデータを再検証する      |
| populateCache  | false      | ミューテーション結果でキャッシュを更新する            |
| rollbackOnError | true      | ミューテーション失敗時に以前のデータに戻す            |
| onSuccess      |            | ミューテーション成功時のコールバック                  |
| onError        |            | ミューテーションエラー時のコールバック                |

## ページネーション / 無限ローディング

`rememberSWRInfinite()` は動的なページ管理を備えたページネーションデータフェッチングを提供します。

```kotlin
@Composable
fun UserList() {
    val (data, error, isLoading, _, size, setSize) = rememberSWRInfinite(
        getKey = { pageIndex, previousPageData ->
            if (previousPageData != null && previousPageData.isEmpty()) null // 末尾に到達
            else "/api/users?page=$pageIndex"
        },
        fetcher = { key -> fetchUsers(key) },
    )

    // data は List<List<User>?> — ページごとに1エントリ
    data?.flatten()?.forEach { user ->
        Text(user.name)
    }

    Button(onClick = { setSize(size + 1) }) {
        Text("Load More")
    }
}
```

ページネーション固有のオプションは config ブロックで設定できます。

| オプション名          | デフォルト値 | 説明                                  |
|----------------------|------------|---------------------------------------|
| initialSize          | 1          | 初期ロード時のページ数                   |
| revalidateAll        | false      | ロード済みの全ページを再検証する           |
| revalidateFirstPage  | true       | 変更時に最初のページを再検証する           |
| persistSize          | false      | 再マウント時にページ数を永続化する          |

## 不変データ

`rememberSWRImmutable()` はデータを一度だけフェッチし、自動的な再検証を行いません。  
変更されないデータ（静的リソース、一度だけ読み込むユーザー設定など）に適しています。

```kotlin
@Composable
fun StaticContent() {
    val (data, error) = rememberSWRImmutable("/api/config", fetcher)
    // ...
}
```

これは `rememberSWR()` の全再検証オプションを無効にした場合と同等です。

## 永続キャッシュ

`Persister` を提供することで、フェッチしたデータをローカルストレージ（データベース、ファイルシステムなど）に保存できます。  
これによりアプリの再起動後もデータを維持できます。

```kotlin
val persister = Persister<String, User>(
    loadData = { key -> database.getUser(key) },
    saveData = { key, data -> database.saveUser(key, data) },
)

@Composable
fun Profile() {
    val (data, error) = rememberSWR("/api/user", fetcher, persister)
    // 初回ロード時、persister.loadData が呼ばれて即座に表示され、
    // その後バックグラウンドで fetcher が再検証のために実行されます。
}
```

## パフォーマンスに関する注意

[リクエストの重複排除](https://swr.vercel.app/docs/advanced/performance#deduplication)と[ディープ比較](https://swr.vercel.app/docs/advanced/performance#deep-comparison)には対応していますが、[依存関係の収集](https://swr.vercel.app/docs/advanced/performance#dependency-collection)は Kotlin の言語仕様上対応していません。  
そのため、オリジナルの `React SWR` と比較して再レンダリング（再コンポーズ）の回数が多くなる場合があります。

ただし、子 Composable 関数に渡す引数を制限する（例：data のみを渡す）ことで、パフォーマンスの低下を防ぐことができます。
