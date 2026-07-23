package com.example.demo

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri

/**
 * ★★★ NoteContentProvider ★★★
 *
 * 自定义 ContentProvider，把"笔记数据"通过标准 URI 接口对外暴露。
 * 类比：把内部数据库包装成 REST API，外部通过 content:// URI 访问。
 *
 * URI 设计（类似 REST 路由）：
 *   content://com.example.demo01.notes/notes        → 所有笔记（集合）
 *   content://com.example.demo01.notes/notes/1      → ID=1 的笔记（单条）
 *
 * 对应 REST：
 *   GET    /notes      → query(NOTES_URI)
 *   GET    /notes/1    → query(NOTES_URI/1)
 *   POST   /notes      → insert(NOTES_URI, values)
 *   PUT    /notes/1    → update(NOTES_URI/1, values)
 *   DELETE /notes/1    → delete(NOTES_URI/1)
 */
class NoteContentProvider : ContentProvider() {

    companion object {
        // Authority：类似域名，在 Manifest 里注册，全局唯一
        const val AUTHORITY = "com.example.demo01.notes"

        // 对外暴露的 URI 常量（调用方用这个访问数据）
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/notes")

        // 列名（类似数据库字段名）
        const val COL_ID      = "_id"
        const val COL_TITLE   = "title"
        const val COL_CONTENT = "content"
        const val COL_TIME    = "create_time"

        // UriMatcher：把 URI 映射成整数 code，方便 when 分支处理
        // 类似路由匹配器：/notes → 1，/notes/* → 2
        private const val CODE_NOTES    = 1  // 匹配 .../notes（集合）
        private const val CODE_NOTE_ID  = 2  // 匹配 .../notes/123（单条）

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "notes",    CODE_NOTES)    // /notes
            addURI(AUTHORITY, "notes/#",  CODE_NOTE_ID)  // /notes/数字
        }
    }

    // ★ 内存数据存储（模拟数据库，实际项目用 SQLite / Room）
    // key = id，value = 一行数据（map）
    private val notesStore = mutableMapOf<Long, Map<String, String>>()
    private var nextId = 1L

    override fun onCreate(): Boolean {
        // ContentProvider.onCreate() 在 Application.onCreate() 之前调用
        // ★ 大厂 SDK（Firebase/WorkManager）利用这个时机自动初始化

        // 预置一些数据
        insertNote("Android 四大组件", "Activity / Service / Broadcast / ContentProvider")
        insertNote("Binder 原理", "Android IPC 机制，基于共享内存，只拷贝一次")
        insertNote("SurfaceFlinger", "系统合成器，把所有图层合成到屏幕")
        return true
    }

    /**
     * ★ query：查询数据（对应 REST GET）
     *
     * @param uri        要查询的 URI（集合或单条）
     * @param projection 要返回哪些列（null = 全部，类似 SELECT *）
     * @param selection  WHERE 条件
     * @param selectionArgs WHERE 参数（防 SQL 注入）
     * @param sortOrder  ORDER BY
     * @return Cursor（结果集，类似数据库 ResultSet）
     */
    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor {
        val cursor = MatrixCursor(arrayOf(COL_ID, COL_TITLE, COL_CONTENT, COL_TIME))

        when (uriMatcher.match(uri)) {
            CODE_NOTES -> {
                // 查所有笔记：SELECT * FROM notes
                notesStore.forEach { (id, note) ->
                    cursor.addRow(arrayOf(id, note[COL_TITLE], note[COL_CONTENT], note[COL_TIME]))
                }
            }
            CODE_NOTE_ID -> {
                // 查单条：SELECT * FROM notes WHERE _id = ?
                val id = ContentUris.parseId(uri)
                notesStore[id]?.let { note ->
                    cursor.addRow(arrayOf(id, note[COL_TITLE], note[COL_CONTENT], note[COL_TIME]))
                }
            }
        }
        return cursor
    }

    /**
     * ★ insert：插入数据（对应 REST POST）
     * @return 新插入数据的 URI（包含新 ID）
     */
    override fun insert(uri: Uri, values: ContentValues?): Uri {
        val title   = values?.getAsString(COL_TITLE)   ?: ""
        val content = values?.getAsString(COL_CONTENT) ?: ""
        val id = insertNote(title, content)

        // ★ notifyChange：通知所有监听这个 URI 的观察者数据变了
        // 类似 LiveData.setValue()，注册了 ContentObserver 的地方会自动刷新
        context?.contentResolver?.notifyChange(CONTENT_URI, null)

        return ContentUris.withAppendedId(CONTENT_URI, id)  // content://.../notes/新id
    }

    /**
     * ★ delete：删除数据（对应 REST DELETE）
     * @return 删除的行数
     */
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return when (uriMatcher.match(uri)) {
            CODE_NOTE_ID -> {
                val id = ContentUris.parseId(uri)
                val removed = notesStore.remove(id)
                if (removed != null) {
                    context?.contentResolver?.notifyChange(CONTENT_URI, null)
                    1
                } else 0
            }
            else -> 0
        }
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        // 简化：不实现 update，实际项目直接操作 SQLite
        return 0
    }

    override fun getType(uri: Uri): String {
        // MIME 类型：集合用 vnd.android.cursor.dir，单条用 vnd.android.cursor.item
        return when (uriMatcher.match(uri)) {
            CODE_NOTES   -> "vnd.android.cursor.dir/notes"
            CODE_NOTE_ID -> "vnd.android.cursor.item/notes"
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    private fun insertNote(title: String, content: String): Long {
        val id = nextId++
        notesStore[id] = mapOf(
            COL_TITLE   to title,
            COL_CONTENT to content,
            COL_TIME    to System.currentTimeMillis().toString()
        )
        return id
    }
}
