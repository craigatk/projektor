package projektor.schedule

import net.javacrumbs.shedlock.core.DefaultLockingTaskExecutor
import net.javacrumbs.shedlock.core.LockConfiguration
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider
import java.time.Instant
import javax.sql.DataSource

class SchedulerLock(dataSource: DataSource) {
    private val lockProvider = JdbcTemplateLockProvider(dataSource)
    private val executor = DefaultLockingTaskExecutor(lockProvider)

    fun executeWithLock(runnable: Runnable, lockConfig: SchedulerLockConfig) {
        val lockAtMostUntil: Instant = Instant.now().plusSeconds(lockConfig.lockMinutes * 60)
        executor.executeWithLock(runnable, LockConfiguration(lockConfig.lockName, lockAtMostUntil))
    }
}

data class SchedulerLockConfig(val lockMinutes: Long, val lockName: String)
