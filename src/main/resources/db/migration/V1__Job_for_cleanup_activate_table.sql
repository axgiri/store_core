CREATE EXTENSION IF NOT EXISTS pgagent;

-- Добавляем Job для очистки таблицы active в 00:00 UTC+5 (19:00 UTC)
DO $$
DECLARE
job_id integer;
BEGIN
    -- Создаем задание (Job)
INSERT INTO pgagent.pga_job(
    jobjclid,
    jobname,
    jobdesc,
    jobhostagent,
    jobenabled,
    jobcreated,
    jobchanged
) VALUES (
             1,
             'Cleanup Activate Table (UTC+5)',
             'Deletes old records from "active" table at 00:00 UTC+5 (19:00 UTC)',
             'localhost',
             true,
             now(),
             now()
         ) RETURNING jobid INTO job_id;

-- Добавляем расписание (Schedule)
-- UTC+5 = 19:00 UTC (так как PG Agent работает по UTC)
INSERT INTO pgagent.pga_schedule(
    jscjobid,
    jscname,
    jscdesc,
    jscminutes, -- 0-я минута
    jschours,   -- 19-й час (19:00 UTC = 00:00 UTC+5)
    jscmonthdays,
    jscmonths,
    jscweekdays,
    jscenabled,
    jscstart,
    jscend
) VALUES (
             job_id,
             'Daily at 00:00 UTC+5',
             'Runs every day at midnight UTC+5 (19:00 UTC)',
             '{7}',      -- 0-я минута
             '{21}',     -- 19-й час (19:00 UTC)
             '{}',       -- Каждый день месяца
             '{}',       -- Каждый месяц
             '{}',       -- Каждый день недели
             true,
             now(),      -- Начать с текущего момента
             NULL        -- Без окончания
         );

-- Добавляем шаг (Step)
INSERT INTO pgagent.pga_jobstep(
    jstjobid,
    jstname,
    jstdesc,
    jstkind,    -- 's' = SQL-запрос
    jstonerror, -- 'f' = fail (остановить при ошибке)
    jstcode,
    jstconnstr
) VALUES (
             job_id,
             'Delete old records',
             'Deletes entries older than current date',
             's',
             'f',
             'DELETE FROM active WHERE created_at < CURRENT_DATE;',
             ''          -- Пустая строка = текущее подключение
         );
END $$;