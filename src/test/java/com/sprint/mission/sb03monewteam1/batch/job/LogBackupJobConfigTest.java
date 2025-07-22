package com.sprint.mission.sb03monewteam1.batch.job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.sprint.mission.sb03monewteam1.util.S3Util;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.transaction.PlatformTransactionManager;

class LogBackupJobConfigTest {

    @Mock
    private JobRepository jobRepository;
    @Mock
    private PlatformTransactionManager transactionManager;
    @Mock
    private S3Util s3Util;
    @Mock
    private StepContribution contribution;
    @Mock
    private ChunkContext chunkContext;

    @TempDir
    Path tempDir;

    private LogBackupJobConfig config;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        config = new LogBackupJobConfig(jobRepository, transactionManager, s3Util);

        setField(config, "backupBucket", "test-bucket");
        setField(config, "logPrefix", "logs");
        setField(config, "logDir", tempDir.toString());
    }

    private void setField(Object target, String field, Object value) {
        try {
            var f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void 백업대상_파일_없으면_정상종료() throws Exception {
        RepeatStatus status = config.logBackupTasklet().execute(contribution, chunkContext);
        assertEquals(RepeatStatus.FINISHED, status);
        verifyNoInteractions(s3Util);
    }

    @Test
    void main_로그_파일_업로드_정상동작() throws Exception {
        String dateStr = LocalDate.now().minusDays(1)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Path logFile = Files.createFile(tempDir.resolve("main-" + dateStr + ".log.gz"));
        Files.writeString(logFile, "test log");

        doNothing().when(s3Util)
            .upload(anyString(), anyString(), any(InputStream.class), anyLong(), anyString());

        RepeatStatus status = config.logBackupTasklet().execute(contribution, chunkContext);

        assertEquals(RepeatStatus.FINISHED, status);
        verify(s3Util, times(1)).upload(
            eq("test-bucket"),
            contains("/json/"),
            any(InputStream.class),
            anyLong(),
            eq("application/gzip")
        );
    }

    @Test
    void debug_로그_파일_업로드_정상동작() throws Exception {
        String dateStr = LocalDate.now().minusDays(1)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Path logFile = Files.createFile(tempDir.resolve("debug-" + dateStr + ".log.gz"));
        Files.writeString(logFile, "test log");

        doNothing().when(s3Util)
            .upload(anyString(), anyString(), any(InputStream.class), anyLong(), anyString());

        RepeatStatus status = config.logBackupTasklet().execute(contribution, chunkContext);

        assertEquals(RepeatStatus.FINISHED, status);
        verify(s3Util, times(1)).upload(
            eq("test-bucket"),
            contains("/debug/"),
            any(InputStream.class),
            anyLong(),
            eq("application/gzip")
        );
    }

    @Test
    void 파일_여러개_모두_업로드() throws Exception {
        String dateStr = LocalDate.now().minusDays(1)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Files.createFile(tempDir.resolve("main-" + dateStr + ".log.gz"));
        Files.createFile(tempDir.resolve("debug-" + dateStr + ".log.gz"));

        doNothing().when(s3Util)
            .upload(anyString(), anyString(), any(InputStream.class), anyLong(), anyString());

        RepeatStatus status = config.logBackupTasklet().execute(contribution, chunkContext);

        assertEquals(RepeatStatus.FINISHED, status);
        verify(s3Util, times(2)).upload(anyString(), anyString(), any(InputStream.class), anyLong(),
            anyString());
    }

    @Test
    void 디렉토리_읽기_실패시_예외발생() {
        Path file = tempDir.resolve("notADir");
        try {
            Files.writeString(file, "not a dir");
            setField(config, "logDir", file.toString());
            assertThrows(RuntimeException.class,
                () -> config.logBackupTasklet().execute(contribution, chunkContext));
        } catch (IOException e) {
            fail(e);
        }
    }
}
