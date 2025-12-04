package rs.edu.raf.cloud.backend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@Configuration
@EnableAsync
@EnableScheduling
class AsyncConfig {

    // name -> an argument which lets functions annotated with @Async become tasks to be executed by threads of this Executor thread pool
    @Bean(name = ["taskExecutor"])
    fun taskExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        // Standard thread pool size
        executor.corePoolSize = 5
        // Increased thread pool size if queue is full
        executor.maxPoolSize = 10
        // Queue size for tasks to be waiting in if there is currently no thread to process them
        // Note - if the queue is full and all 10 threads are busy, the task is rejected and an exception is thrown
        executor.queueCapacity = 500
        executor.setThreadNamePrefix("MachineAsync-")
        executor.initialize()
        return executor
    }
}