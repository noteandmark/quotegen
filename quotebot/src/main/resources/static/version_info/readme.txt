# Quote Generator REST API and Client Telegram Bot

**Version:** 2.0.0 (Released on 13.01.2024)

**Author:** andmark

**Contact Email:** noteandmark@gmail.com

**LinkedIn:** [linkedin.com/in/noteandmark/](https://linkedin.com/in/noteandmark/)

**Telegram:** [t.me/note_and_mark](https://t.me/note_and_mark)

"Книголюб" - часть автоматизированной системы по генерации цитат из книг:
1. QuoteGen - приложение, которое генерирует случайные цитаты из случайно выбранной книги в электронной библиотеке пользователя.
   Приложение сканирует предложенный каталог и все подпапки, находит книги в форматах EPUB, FB2, DOC, DOCX, PDF и сохраняет информацию о них в базу данных.
2. QuoteBot - телеграм-бот, предоставляет доступ ко всем точкам REST API приложения.
3. QuoteGen web - веб-приложение для доступа ко всем возможностям программы через браузер

Использованные технологии и библиотеки:
**Язык программирования:**
- Java 20, JavaScript

**Фреймворки:**
- Spring Boot 3.1.2
- Spring Security 6
- Spring Data JPA

**Инструменты сборки проекта:**
- Maven

**Базы данных:**
- PostgreSQL
- H2 database
- Hibernate

**Логирование:**
- SLF4J logging

**Тестирование:**
- JUnit5
- Mockito

**Система контроля версий:**
- Git

**Инструменты разработки и тестирования API:**
- Postman

**Интеграция с внешними сервисами:**
- Telegram API
- Google Custom Search API

**Шаблонизация и представление:**
- HTML, CSS, Thymeleaf

**Фронтенд:**
- Bootstrap 5

**Упрощение разработки:**
- Lombok

**Маппинг объектов:**
- ModelMapper

**Отправка email:**
- Jakarta Mail API

**Доступ через интернет:**
- Ngrok API

**Работа с форматами FB2, EPUB, PDF, Microsoft Office, HTML:**
- com.kursx.parser.fb2
- nl.siegmann.epublib
- org.apache.pdfbox
- org.apache.poi
- org.jsoup

Пользователю доступны возможности:
- получить статистику
- получить ответ в виде gif изображения "да" или "нет"
- погадать на книге: назвать страницу и строку, и бот, случайно выбрав книгу, вернет указанное место
- получить случайную цитату из опубликованных ранее
- получить все цитаты за текущую неделю
- прочитать о версии и списке изменений
- отправить сообщение админу: баг, отзыв, предложения

Админ может:
- сканировать директорию на наличие электронных книг и сохранять их в БД
- делать запрос о генерации случайной цитаты
- получать по расписанию случайную цитату

Также по расписанию бот проверяет отложенные к публикации цитаты по заданному интервалу времени.

При запросе о генерации цитаты приложение после сканирования и обновления данных:
- выбирает случайный список книг,
- выбирает случайный фрагмент в книге,
- генерирует кэш цитат (если нужно),
- выбирает цитату, и с ее текстом обращается к Google Custom Search engine API,
- берет 10 изображений (или меньше, если ответ приходит такой),
- выдает админу цитату и предоставляет возможность выбрать изображение.
  У админа есть возможность:
- отредактировать
- принять
- отклонить
  После принятия можно:
- опубликовать сразу
- отложить на определенную дату-время
- отложить случайным образом по алгоритму

Команды для бота:
- /start - запуск
- /help - отобразить справочную информацию
- /da_net - задай вопрос, узнай ответ: да или нет
- /divination - гадание на книгах (user)
- /getquote - получить цитату из опубликованных (user)
- /quotes_for_week - все цитаты за неделю (user)
- /suggestquote - предложить цитату (user)
- /weblink - ссылка на веб-версию программы (user)
- /stats - разная статистика
- /readme - о программе, список изменений, пользовательское соглашение
- /signup - зарегистрироваться
- /signout - сброс логина, пароля
- /reset - сброс до настроек по умолчанию
- /report - сообщить админу что-либо (баги, предложения, отзывы, др.)
- /requestquote - сгенерировать цитату (admin)
- /pendingquotes - получить даты ожидаемых публикаций (админ)
- /scanbooks - поиск электронных книг (админ)

/da_net - по этой команде вы можете задать вопрос про себя или вслух,
и получить случайный ответ "да" или "нет" в виде анимированного изображения
в формате gif.
Бот обращается к API сайта https://yesno.wtf и получает рандомную гифку.
С шансом 1 к 10000 может выпасть ответ "наверное".

Бот живет по адресу:
https://t.me/rimay_bot
в телеграм-канале:
https://t.me/note_and_mark

Приятного пользования! И хороших книг!