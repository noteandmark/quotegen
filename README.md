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
- generating a quote from a randomly selected book
- ability to edit a quote, accept, reject
- publication to the telegram group can be done immediately or delayed
- search for images for text using Google Custom Search engine API