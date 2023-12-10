# QuotegenApp
# Quote Generator REST API and Client Telegram Bot

**Version:** 1.4.0 (Released on 10.12.2023)

**Author:** andmark

**Contact Email:** noteandmark@gmail.com

**LinkedIn:** [linkedin.com/in/noteandmark/](https://linkedin.com/in/noteandmark/)

**Telegram:** [t.me/note_and_mark](https://t.me/note_and_mark)

---

"Книголюб" is a part of an automated system for generating quotes from books:

1. **QuoteGen:** An application that generates random quotes from a randomly selected book in the user's electronic library. The application scans the provided catalog and all subfolders, identifies books in EPUB, FB2, DOC, DOCX, PDF formats, and saves information about them in the database.

2. **QuoteBot:** A Telegram bot providing access to all REST API endpoints of the application.

3. **QuoteGen Web:** A web application providing access to all features of the program through a browser.

---

**Technologies and Libraries Used:**

**Programming Languages:**
- Java 20, JavaScript

**Frameworks:**
- Spring Boot 3.1.2
- Spring Security 6
- Spring Data JPA

**Build Tools:**
- Maven

**Databases:**
- PostgreSQL
- H2 Database
- Hibernate

**Logging:**
- SLF4J logging

**Testing:**
- JUnit5
- Mockito

**Version Control System:**
- Git

**Development and API Testing Tools:**
- Postman

**Integration with External Services:**
- Telegram API
- Google Custom Search API

**Templating and Presentation:**
- HTML, CSS, Thymeleaf

**Frontend:**
- Bootstrap 5

**Simplifying Development:**
- Lombok

**Object Mapping:**
- ModelMapper

**Handling Formats (FB2, EPUB, PDF, Microsoft Office, HTML):**
- com.kursx.parser.fb2
- nl.siegmann.epublib
- org.apache.pdfbox
- org.apache.poi
- org.jsoup

---

**User Features:**
- Obtain statistics
- Receive responses in the form of gif images for "yes" or "no"
- Divine on a book: specify the page and line, and the bot, randomly selecting a book, will return the specified place
- Get a random quote from previously published ones
- Retrieve all quotes for the current week
- Read about the version and changelog
- Send messages to the admin: report bugs, provide feedback, make suggestions

**Admin Capabilities:**
- Scan a directory for electronic books and save them in the database
- Request the generation of a random quote
- Receive a random quote on a schedule

Additionally, the bot checks scheduled quotes for publication at specified intervals.

When requesting a quote generation, the application, after scanning and updating data:
- Selects a random list of books
- Chooses a random fragment in the book
- Generates a cache of quotes (if needed)
- Selects a quote and, with its text, queries the Google Custom Search Engine API
- Retrieves 10 images (or fewer if the response is such)
- Presents the admin with the quote and the option to choose an image.
  The admin can:
- Edit
- Accept
- Reject
  After acceptance, it is possible to:
- Publish immediately
- Schedule for a specific date and time
- Randomly schedule using an algorithm

---

**Bot Commands:**
- /start - Launch the bot
- /help - Display help information
- /stats - Display various statistics
- /da_net - Ask a question, get an answer: yes or no
- /divination - Book divination (user)
- /getquote - Get a quote from published ones (user)
- /quotes_for_week - All quotes for the week (user)
- /version - Display version and changelog
- /signup - Register
- /signout - Reset login and password
- /reset - Reset to default settings
- /report - Report to the admin (bugs, suggestions, feedback, etc.)
- /scanbooks - Search for electronic books (admin)
- /requestquote - Generate a quote (admin)

**/da_net Command:**
Use this command to ask a question silently or out loud and receive a random "yes" or "no" answer in the form of an animated gif.
The bot queries the [yesno.wtf](https://yesno.wtf) API and retrieves a random gif.
There is a 1 in 10,000 chance of getting the answer "maybe."

**Bot Locations:**
- [Telegram](https://t.me/rimay_bot)
- [Telegram Channel](https://t.me/note_and_mark)

Enjoy your experience! And happy reading!

---

RU:

# Quote Generator REST API and Client Telegram Bot

**Version:** 1.4.0 (Released on 10.12.2023)

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
- /stats - разная статистика
- /da_net - задай вопрос, узнай ответ: да или нет
- /divination - гадание на книгах (user)
- /getquote - получить цитату из опубликованных (user)
- /quotes_for_week - все цитаты за неделю (user)
- /version - версия и список новшеств
- /signup - зарегистрироваться
- /signout - сброс логина, пароля
- /reset - сброс до настроек по умолчанию
- /report - сообщить админу что-либо (баги, предложения, отзывы, др.)
- /scanbooks - поиск электронных книг (админ)
- /requestquote - сгенерировать цитату (admin)

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