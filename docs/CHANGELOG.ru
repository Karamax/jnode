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
19.09.2012, 0.3.4.2:
~ Исправлен regexp для Origin
~ Исправлена обработка CRAM-XXX/XXX ( было только для CRAM-MD5 ) (thx 2 Andre Gruenberg, 2:2411/525)
20.09.2012, 0.3.5:
+ Добавлено индексирование нодлиста и проверка нод на соответствие
+ Добавлены переменные в конфиг: nodelist.path и nodelist.index
+ Добавлен приём непакованного нетмейла для нод, которых нет в линках
~ Роутинг нетмейла теперь проверяет существование Origin и Dest в нодлисте
21.09.2012, 0.3.5.1:
~ Добавлен контролируемый таймаут на соедиенения
21.09.2012, 0.4.0:
~ Сильно переработан тоссер, функции разнесены по разным классам, ускорена работа системы в несколько раз.
~ В связи с этим обновлена мажорная версия системы
22.09.2012, 0.4.1:
+ Hовый механизм поллов - теперь ведется единая очередь поллов.
+ Реализован Crash flavour для Echomail.
~ Исправлены ошибки несовместимости с Argus (тестировалось на 2:203/0 )
22.09.2012, 0.4.2:
+ Добавлена возможность указывать различные опции для линков.
+ Добавлена команда %query для AreaFix
23.09.2012, 0.4.3:
~ Кривые пакеты теперь не удаляются, а сохраняются в inbound
! Исправлено несоответствие FTC-0048, вызывавшее ошибки при контакте со старым софтом. ( Спасибо Bjorn Felten, 2:203/2 )
+ Добавлена логика для пяти опций:
- AreaAutoCreate - автоматически создавать эхоарии для этого линка
- CrashEchomail  - делать полл при появлении нового эхосмейла для линка
- CrashNetmail   - делать полл при появлении нового нетмейла для линка
- PackNetmail    - паковать нетмейл в архивы
- PackEchomail   - паковать эхомейл в архивы
- IgnorePktPwd   - не проверять пароль в заголовке пакета ( Спасибо Pavel Gulchouck, 2:463/68 )
28.09.2012, 0.4.5(c):
! Исправлена ошибка в AreaAutoCreate, теперь эхи создаются.
+ Добавлены группы для эх.
30.09.2012, 0.4.6:
+ Добавлена обработка pkt из локального инбаунда
+ Добавлена поддержка файл-аттачей в netmail
02.10.2012, 0.4.6(d):
~ Исправлен крэш на старых JVM
~ Если распаковка пакета не удалась, посылается m_skip
+ Добавлена опция Groups и проверка доступа для эх
+ Добавлена опция AreaFix для включения/отключения робота AreaFix
+ Добавлена опция PollByTimeout для включения/отключения полла клиентом.
16.10.2012, 0.4.8:
~ Изменен внутренний фреймворк
16.10.2012, 0.4.9:
~ Логи переведены на английский
~ Сильно ускорено быстродействие тоссинга, изменена логика
~ Исправлена логика роутинга и дропа нетмейла
17.10.2012, 0.5.0:
+ Добавлена обработка файлэх
+ Добавлена логика для трех опций:
- CrashFilemail - делать полл при появлении нового файла в фэхе
- FileFix - включить/отключить робота FileFix
- FileAutoCreate - автоматически создавать файлэхи
17.10.2012, 0.5.1:
+ Добавлен робот FileFix
16.01.2013, 0.5.5.7
+ Запуск пользовательских скриптов по расписанию