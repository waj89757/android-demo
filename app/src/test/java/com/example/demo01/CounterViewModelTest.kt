package com.example.demo01

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * ★★★ ViewModel 单元测试 ★★★
 *
 * 这里测的是 CounterViewModel（最简单的 ViewModel）
 * 跑在普通 JVM 上，不需要模拟器，秒出结果。
 *
 * 运行方式：
 *   在类名或方法名旁边点绿色三角形 ▶
 *   或右键 → Run 'CounterViewModelTest'
 *
 * ★ 和 MVP 单测的对比：
 *
 *   MVP 测的是"行为"：Presenter 有没有调 view.showXxx()？
 *     → 需要 mock 一个假 View
 *
 *   MVVM 测的是"状态"：LiveData.value 最终是什么？
 *     → 直接读变量值，不需要 mock 任何东西
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CounterViewModelTest {

    /**
     * ★ 这个 Rule 是测试 LiveData 的必要配置
     *
     * 问题背景：
     *   LiveData 内部用了 Android 主线程（MainLooper）来切换线程。
     *   但单元测试跑在普通 JVM 上，没有 Android 主线程。
     *   不加这个 Rule → LiveData.value 永远是 null → 测试失败。
     *
     * InstantTaskExecutorRule 的作用：
     *   把所有 LiveData 的操作变成同步执行（不再依赖 Android 主线程）。
     *   赋值后立刻可以读到新值。
     */
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // 被测试的对象
    private lateinit var viewModel: CounterViewModel

    @Before
    fun setUp() {
        // 每个测试方法运行前都重新创建一个干净的 ViewModel
        viewModel = CounterViewModel()
    }

    /**
     * ★ 测试1：初始值应该是 0
     *
     * 验证 ViewModel 创建后，count 的初始值是 0
     */
    @Test
    fun `初始值是0`() {
        // ★ 直接读 LiveData 的 value，不需要 Activity，不需要 observe()
        val initialValue = viewModel.count.value

        assertEquals(0, initialValue)
        // 翻译：期望值=0，实际值=initialValue，不相等就报错
    }

    /**
     * ★ 测试2：调用 increment() 后，值应该加1
     */
    @Test
    fun `increment后值加1`() {
        // 执行操作
        viewModel.increment()

        // 验证 LiveData 的值变了
        assertEquals(1, viewModel.count.value)
    }

    /**
     * ★ 测试3：连续调用3次 increment()，值应该是3
     */
    @Test
    fun `连续increment三次后值是3`() {
        viewModel.increment()
        viewModel.increment()
        viewModel.increment()

        assertEquals(3, viewModel.count.value)
    }

    /**
     * ★ 测试4：increment() 后再 reset()，值应该回到0
     */
    @Test
    fun `increment后reset回到0`() {
        viewModel.increment()
        viewModel.increment()
        assertEquals(2, viewModel.count.value)  // 先确认是2

        viewModel.reset()
        assertEquals(0, viewModel.count.value)  // reset后是0
    }

    /**
     * ★ 测试5：LiveData 不为 null（确保初始化正确）
     */
    @Test
    fun `count LiveData不为null`() {
        assertNotNull(viewModel.count)
    }
}


/**
 * ★★★ MVP Presenter 单测（对比用）★★★
 *
 * 对比上面的 ViewModel 测试，感受区别：
 *   ViewModel 测试：直接断言 LiveData.value
 *   Presenter 测试：需要造一个假 View，验证方法有没有被调用
 */
class PostMvpPresenterTest {

    // ★ 造一个假的 View（手动实现接口，记录方法有没有被调用）
    // 这就是 MVP 测试的额外工作量
    private class FakeView : PostMvpContract.View {
        var showLoadingCalled = false
        var hideLoadingCalled = false
        var showErrorCalled = false
        var receivedPosts: List<Post>? = null

        override fun showLoading() { showLoadingCalled = true }
        override fun hideLoading() { hideLoadingCalled = true }
        override fun showPosts(posts: List<Post>) { receivedPosts = posts }
        override fun showError(message: String) { showErrorCalled = true }
    }

    /**
     * ★ 测试：Presenter.onDestroy() 后，view 引用应该被清空
     *
     * 验证 onDestroy 可以安全调用（不会 NPE）
     */
    @Test
    fun `onDestroy后调用不崩溃`() {
        val fakeView = FakeView()
        val presenter = PostMvpPresenter(fakeView)

        // 只验证 onDestroy 本身不会崩溃
        // view 会被置为 null，后续的 view?.xxx() 安全调用不会 NPE
        presenter.onDestroy()

        // 走到这里说明没有抛异常，测试通过
        assertTrue(true)
    }
}
