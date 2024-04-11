package projektor.schedule

import net.javacrumbs.shedlock.core.DefaultLockingTaskExecutor
import net.javacrumbs.shedlock.core.LockConfiguration
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider
import java.time.Duration
import java.time.Instant
import javax.sql.DataSource

class SchedulerLock(dataSource: DataSource) {
    private val lockProvider = JdbcTemplateLockProvider(dataSource)
    private val executor = DefaultLockingTaskExecutor(lockProvider)

    fun executeWithLock(runnable: Runnable, lockConfig: SchedulerLockConfig) {
        val lockAtMostFor: Duration = Duration.ofMinutes(lockConfig.lockMinutes)
        val lockAtLeastFor: Duration = Duration.ofSeconds(15)
        executor.executeWithLock(runnable, LockConfiguration(Instant.now(), lockConfig.lockName, lockAtMostFor, lockAtLeastFor))
    }
}

data class SchedulerLockConfig(val lockMinutes: Long, val lockName: String)
