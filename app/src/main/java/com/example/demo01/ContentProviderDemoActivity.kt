package com.example.demo01

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

/**
 * ★★★ ContentProvider Demo ★★★
 *
 * 演示两个场景：
 *   1. 自定义 ContentProvider（NoteContentProvider）：CRUD 操作
 *   2. 读系统 ContentProvider（手机联系人）：真实跨 App 数据访问
 *
 * 核心 API：contentResolver（ContentProvider 的客户端）
 *   contentResolver.query()   → 查
 *   contentResolver.insert()  → 增
 *   contentResolver.delete()  → 删
 *   contentResolver.update()  → 改
 *
 * contentResolver 底层通过 Binder 调用到 ContentProvider.query/insert/delete/update()
 * 如果 Provider 在同一进程 → 直接调用（无 Binder 开销）
 * 如果 Provider 在其他进程 → 跨进程 Binder + 共享内存传输数据
 */
class ContentProviderDemoActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private lateinit var tvNotesResult: TextView
    private lateinit var tvContactsResult: TextView

    // 记录最后插入的 URI，用于演示删除
    private var lastInsertedUri: Uri? = null

    // ★ 运行时权限申请（READ_CONTACTS 需要用户主动授权）
    private val requestContactsPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) readContacts()
            else Toast.makeText(this, "未授权联系人权限，无法读取", Toast.LENGTH_SHORT).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content_provider_demo)

        etTitle          = findViewById(R.id.et_note_title)
        etContent        = findViewById(R.id.et_note_content)
        tvNotesResult    = findViewById(R.id.tv_notes_result)
        tvContactsResult = findViewById(R.id.tv_contacts_result)

        setupButtons()

        // 进入页面立即查询一次，展示预置数据
        queryAllNotes()
    }

    private fun setupButtons() {

        // ★ 插入笔记（调用自定义 ContentProvider 的 insert()）
        findViewById<Button>(R.id.btn_insert).setOnClickListener {
            val title   = etTitle.text.toString().trim()
            val content = etContent.text.toString().trim()
            if (title.isEmpty()) { Toast.makeText(this, "标题不能为空", Toast.LENGTH_SHORT).show(); return@setOnClickListener }

            val values = ContentValues().apply {
                put(NoteContentProvider.COL_TITLE,   title)
                put(NoteContentProvider.COL_CONTENT, content)
            }
            // ★ contentResolver.insert → Binder → NoteContentProvider.insert()
            lastInsertedUri = contentResolver.insert(NoteContentProvider.CONTENT_URI, values)
            etTitle.text.clear()
            etContent.text.clear()
            Toast.makeText(this, "已插入，URI: $lastInsertedUri", Toast.LENGTH_SHORT).show()
            queryAllNotes()
        }

        // ★ 查询所有笔记（调用自定义 ContentProvider 的 query()）
        findViewById<Button>(R.id.btn_query_all).setOnClickListener {
            queryAllNotes()
        }

        // ★ 删除最后插入的笔记（调用自定义 ContentProvider 的 delete()）
        findViewById<Button>(R.id.btn_delete_last).setOnClickListener {
            val uri = lastInsertedUri
            if (uri == null) {
                Toast.makeText(this, "还没有插入过数据，先点插入", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val deleted = contentResolver.delete(uri, null, null)
            Toast.makeText(this, "删除了 $deleted 条，URI: $uri", Toast.LENGTH_SHORT).show()
            lastInsertedUri = null
            queryAllNotes()
        }

        // ★ 读取系统联系人（系统内置 ContentProvider）
        findViewById<Button>(R.id.btn_read_contacts).setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
                readContacts()
            } else {
                requestContactsPermission.launch(Manifest.permission.READ_CONTACTS)
            }
        }

        findViewById<Button>(R.id.btn_back).setOnClickListener { finish() }
    }

    /**
     * ★ 查询自定义 ContentProvider 的所有笔记
     *
     * contentResolver.query() 参数和 SQL SELECT 一一对应：
     *   uri         → FROM（表/Provider）
     *   projection  → SELECT（选哪些列，null=全部）
     *   selection   → WHERE
     *   selectionArgs → WHERE 参数（防注入）
     *   sortOrder   → ORDER BY
     */
    private fun queryAllNotes() {
        val cursor = contentResolver.query(
            NoteContentProvider.CONTENT_URI,  // content://com.example.demo01.notes/notes
            null,    // SELECT * （所有列）
            null,    // 无 WHERE 条件
            null,
            null
        )

        val sb = StringBuilder()
        cursor?.use { c ->
            sb.appendLine("共 ${c.count} 条笔记：\n")
            val idCol      = c.getColumnIndex(NoteContentProvider.COL_ID)
            val titleCol   = c.getColumnIndex(NoteContentProvider.COL_TITLE)
            val contentCol = c.getColumnIndex(NoteContentProvider.COL_CONTENT)

            while (c.moveToNext()) {
                val id      = c.getLong(idCol)
                val title   = c.getString(titleCol)
                val content = c.getString(contentCol)
                sb.appendLine("[$id] $title")
                sb.appendLine("     → $content\n")
            }
        }

        tvNotesResult.text = sb.toString().ifEmpty { "（暂无数据）" }
    }

    /**
     * ★ 读取系统联系人（调用系统内置 ContentProvider）
     *
     * ContactsContract.Contacts.CONTENT_URI =
     *   content://com.android.contacts/contacts
     *
     * 这是系统提供的 ContentProvider，规则和自定义的完全一样，
     * 只是 Authority 和列名不同。
     */
    private fun readContacts() {
        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,   // 系统联系人 URI
            arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.HAS_PHONE_NUMBER
            ),
            null, null,
            "${ContactsContract.Contacts.DISPLAY_NAME} ASC"  // 按姓名排序
        )

        val sb = StringBuilder()
        cursor?.use { c ->
            sb.appendLine("手机联系人（共 ${c.count} 个）：\n")
            val nameCol  = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            val phoneCol = c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)

            var count = 0
            while (c.moveToNext() && count < 20) {  // 最多展示20个
                val name     = c.getString(nameCol) ?: "（无名称）"
                val hasPhone = c.getInt(phoneCol) > 0
                sb.appendLine("• $name${if (hasPhone) " 📞" else ""}")
                count++
            }
            if (c.count > 20) sb.appendLine("... 还有 ${c.count - 20} 个")
        }

        tvContactsResult.text = sb.toString().ifEmpty { "（联系人为空）" }
    }
}
