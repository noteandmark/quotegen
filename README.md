# QuotegenApp
## Quote Generator REST API and Client Telegram Bot
*The automated book citation generation system QuoteGen is 
a sophisticated software application designed to optimise 
the random citation generation process and manage 
a digital library of books. It has the ability to scan catalogues, 
extract book information and create comprehensive quotation catalogues. 
The system provides access via a telegram bot to implement all features 
of the application's REST API and publish posts to the telegram group 
either immediately or deferred.*

*QuoteGen features an automated book scanning process that swiftly 
scans designated directories for eBook files in various formats, 
including EPUB, FB2, DOC, DOCX and PDF. When you request a quote, 
QuoteGen generates and saves to the database a cache of 30 (default)
quotes from randomly selected books in the entire catalogue. 
In each book, the application randomly takes a 500 character section
of text (customisable) and provides it to the user. The user can 
edit the quote, accept or delete it. It is also specified: 
publish immediately or postpone.*

*For each quote, we use the Google API to look for an image. You can 
choose from 10 (optional) suggested images.*

---
*Technologies used:*
- Java20, Spring Boot
- Spring Data JPA, PostgreSQL
- Logging, Hibernate Validator
- Libraries for parsing different book formats
- Telegram API, GoogleCustomSearch API
# Main features
- automated book scanning
- parsing text from eBook formats: EPUB, FB2, DOC, DOCX and PDF
- generating a random quote from a randomly selected book
- ability to edit a quote, accept or reject
- publication to the telegram group can be done immediately or delayed
- schedule check of pending quotes
- search for images for text using Google Custom Search engine API
- menu commands: help, signup, signout, da_net, stats, version, quotes_for_week, getquote, requestquote
- get a random yes-no answer to your question in the form of a gif image after accessing the site's API
- random bot greetings before sending a quote, added 356 variants
---
RU:

# Quote Generator REST API and Client Telegram Bot

version 1.3.7 (24.10.2023)

author: Andrei M.

*email to contact: noteandmark@gmail.com*

"Книголюб" - часть автоматизированной системы по генерации цитат из книг:
1. QuoteGen - приложение, которое генерирует случайные цитаты из случайно выбранной книги в электронной библиотеке пользователя.
   Приложение сканирует предложенный каталог и все подпапки, находит книги в форматах EPUB, FB2, DOC, DOCX, PDF и сохраняет информацию о них в базу данных.
2. QuoteBot - телеграм-бот, предоставляет доступ ко всем точкам REST API приложения.

Использованные технологии:
Java, Spring Boot, Spring Data JPA, PostgreSQL, Telegram API, Google Custom Search API

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
- отложить случайным образом по алгоритму [в разработке]

Команды для бота:
- /start - запуск
- /help - отобразить справочную информацию
- /signup - зарегистрироваться
- /signout - сброс логина, пароля
- /da_net - задай вопрос, узнай ответ: да или нет
- /stats - разная статистика
- /version - версия и список новшеств
- /quotes_for_week - все цитаты за неделю (user)
- /getquote - получить цитату из опубликованных (user)
- /requestquote - сгенерировать цитату (admin)

/da_net - по этой команде вы можете задать вопрос про себя или вслух, 
и получить случайный ответ "да" или "нет" в виде анимированного изображения
в формате gif.
Бот обращается к API сайта https://yesno.wtf и получает рандомную гифку. 
С шансом 1 к 10000 может выпасть ответ "наверное".

Бот живет по адресу:
https://t.me/rimay_bot

Приятного пользования! И хороших книг!