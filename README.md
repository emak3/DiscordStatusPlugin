# DiscordStatusPlugin

PaperMC サーバーの起動・終了時に、指定した Discord チャンネルの名前を自動変更するプラグインです。

## 機能

- **サーバー起動時**: 指定チャンネルの名前を「オンライン」用の名前に変更
- **サーバー停止時**: 指定チャンネルの名前を「オフライン」用の名前に変更
- `/discordstatus reload` で設定を再読み込み

## 必要環境

- Java 21+
- PaperMC 1.21+

## セットアップ手順

### 1. Discord Bot の作成

1. [Discord Developer Portal](https://discord.com/developers/applications) にアクセス
2. 「New Application」をクリックしてアプリを作成
3. 左メニュー「Bot」→「Add Bot」でBotを作成
4. **Token** をコピーして保存（後で使用）
5. 「Privileged Gateway Intents」は不要（今回は使いません）

### 2. Bot にチャンネル管理権限を付与

Bot をサーバーに招待する際、以下の権限が必要です：
- `Manage Channels`（チャンネルの管理）

招待URL例（`CLIENT_ID` を自分のIDに変更）:
```
https://discord.com/api/oauth2/authorize?client_id=CLIENT_ID&permissions=16&scope=bot
```

### 3. チャンネルID の取得

1. Discord の「ユーザー設定」→「詳細設定」→「開発者モード」をON
2. 変更したいチャンネルを右クリック
3. 「IDをコピー」を選択

### 4. config.yml の設定

プラグインを起動すると `plugins/DiscordStatusPlugin/config.yml` が生成されます:

```yaml
# Discord Bot Token
bot-token: "YOUR_BOT_TOKEN_HERE"  # ← ここにBotトークンを入力

# 変更対象のDiscordチャンネルID
channel-id: "YOUR_CHANNEL_ID_HERE"  # ← ここにチャンネルIDを入力

# サーバー起動時のチャンネル名
online-channel-name: "🟢サーバーオンライン"

# サーバー停止時のチャンネル名
offline-channel-name: "🔴サーバーオフライン"

# Discord監査ログに表示される理由
audit-log-reason: "Minecraft Server Status Update"
```

> ⚠️ **注意**: Bot Token は秘密情報です。他人に見せないでください！

## ビルド方法

```bash
./gradlew build
```

ビルドされた JAR は `build/libs/DiscordStatusPlugin-1.0.0-all.jar` に生成されます。

## コマンド

| コマンド | 説明 | 権限 |
|---------|------|------|
| `/discordstatus reload` | 設定ファイルを再読み込み | `discordstatus.reload` (デフォルト: OP) |

## トラブルシューティング

| エラーメッセージ | 原因と対処法 |
|---------------|------------|
| `Bot Token が設定されていません` | `config.yml` の `bot-token` を設定してください |
| `Channel ID が設定されていません` | `config.yml` の `channel-id` を設定してください |
| `チャンネルの変更権限がありません (403)` | BotにDiscordサーバーで「チャンネルの管理」権限を付与してください |
| `チャンネルIDが見つかりません (404)` | `channel-id` が正しいか確認してください |
| `レート制限 (429)` | Discord APIの制限です。頻繁な再起動は避けてください |

## ライセンス

GPL-3.0