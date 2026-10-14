# note流し込み（note-rich-paste）AI向けガイド

## 概要
- index.html / bookmarklet.html：GitHub Pages用のWebツール（Markdown→note用）
- app/：Androidアプリ「note流し込み」。note.comをWebViewで開き、右下の「流し込む」ボタンでクリップボードのMarkdownをHTMLに変換して編集欄のカーソル位置に挿入する

## 主要ファイル
- app/src/main/java/com/latte/nrp/MainActivity.kt：WebView・ボタン・クリップボード読み取り
- app/src/main/assets/nrp.js：Markdown変換と挿入処理（window.nrpInsert）

## 配布
- main にpushすれば GitHub Actions がAPKをビルドし、Release「latest」とGoogle Driveに自動配布
- 完了報告に入れるリンク
  - https://github.com/m0a2t0u7/note-rich-paste/releases/latest/download/note-rich-paste.apk
  - https://github.com/m0a2t0u7/note-rich-paste/actions
  - Google Drive「自作アプリAPK/note-rich-paste」

## 注意
- app/debug.keystore と app/build.gradle.kts の signingConfigs は消さない（上書きインストールに必要）
