16.09.2012, 0.3a1:
~ Исправлена логика протокола binkp, добавлена парралельная отправка и получение. В 0.2 было сначала одно, а потом другое.
~ Добавлены индексы в базу для ускорения поиска, втч unique индексы.
+ Добавлена дюполовка по msgid ( пока без CRC )
+ Добавлен модуль rewrite для перезаписи сообщений по маске ( аля FTrack )
17.09.2012, 0.3.1:
+ Добавлен модуль роботов ( в том числе подключаемых извне )
17.09.2012, 0.3.2:
+ Написана первая версия робота AreaFix (+/-/list/help)
17.09.2012, 0.3.3:
+ Добавлен crash-роутинг нетмейла
~ Получение, передача и тоссинг теперь происходят в разных потоках
~ Добавлено сохранение файлов ( не бандлов ) в папку inbound
18.09.2012, 0.3.4:
~ Исправлена ошибка передачи фрагментированных пакетов
~ Исправлен рескан бандлов после reob/leob
+ Добавлены маски и %RESCAN для AreaFix
18.09.2012, 0.3.4.1:
+ Добавлены переменные в конфиг: log.level, binkp.inbound, binkp.server, binkp.client
~ Под проект создан отдельный репозиторий на github: https://github.com/kreon/jnode.git

