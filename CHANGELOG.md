# Changelog
## [Unreleased] - yyyy-mm-dd
### Added
- протестировать новые методы
- вопросник 1-10
### Changed
### Fixed

## [2.0.2] - 2024-01-14
### Fixed
- added css folder to security config permit-all
- added text field to nickname in profile page
## [2.0.1] - 2024-01-14
### Fixed
- added the ability to automatically create an ngrok tunnel in the event of a shutdown
- readme command
- signout command, delete by usertgid
## [2.0.0-RELEASE] - 2024-01-13
### Added
- added clearing the database of books with DELETED status
- added private-policy.md
### Changed
- user access to the web version
### Fixed
- fixed errors in tests
## [1.7.2] - 2024-01-12
### Added
- added a command to get a link to go to the web version
## [1.7.1] - 2024-01-12
### Added
- added getting dates of all expected quotes for publication
## [1.7.0] - 2024-01-12
### Added
- added option 'suggested quote' for telegram bot
## [1.6.0] - 2024-01-10
### Added
- added the ability for a user to suggest a quote for publication (web version)
## [1.5.0] - 2024-01-09
### Added
- added ngrok tunnel plugin
## [1.4.5] - 2024-01-04
### Added
- added the report web page and bot check option
## [1.4.4] - 2024-01-02
### Added
- added the ability to delete a user
- ## [1.4.3] - 2023-12-22
### Added
- added admin greeting page
## [1.4.2] - 2023-12-22
### Added
- added a nickname to the user profile, you can change it
## [1.4.1] - 2023-12-16
### Added
- quotes page for admin: view the whole list with pagination, got an individual quote, edit, delete
## [1.4.0] - 2023-12-10
### Added
- implemented the core functionality of the web access application
## [1.3.7] - 2023-10-24
### Added
### Fixed
- changed number of citations per day for publications to 1
## [1.3.6] - 2023-09-26
### Added
### Fixed
- fixed an issue where a book could not be read on divination and nothing was sent to the user
## [1.3.5] - 2023-09-01
### Added
### Fixed
- fix character limit when sending a quote
- merged the delayed action methods
## [1.3.4] - 2023-08-30
### Added
### Fixed
- fix some bugs
- some stylistic changes
## [1.3.3] - 2023-08-29
### Added
- add random publishing pending quotes
### Fixed
- fix method checkAndPopulateCache
## [1.3.2] - 2023-08-25
### Added
### Fixed
- changed schedules and start conditions in ScheduledQuoteSenderService
- checking for user role
- fix method calculateEndPosition
## [1.3.1] - 2023-08-22
### Added
- command to send a message about feedback, bugs, suggestions to the admin
## [1.3.0] - 2023-08-21
### Added
- added fortune-telling on a random book with page and line indication
## [1.2.0] - 2023-08-20
### Added
- added possibility to send a bot a quote once a day to admin chat
## [1.1.0] - 2023-08-19
### Added
- add 356 greetings for bot before sending quote
- spring profiles for development and production
### Fixed
- error when generating a citation if the book catalogue is not scanned
## [1.0.0] - 2023-08-18
Release version. The main features are realized
### Added
- gettind version info from readme file and changelog file
- getting a random quote from previously posted
- getting all published quotes from the week
### Fixed
- updated field in quote model from Date to LocalDateTime
## [0.9.0] - 2023-08-16
### Added
- implemented gif retrieval on request to the site api, which answers the question with a dynamic image "yes" or "no"
- added possibility to get statistics on quotes and books
### Fixed
- immediate and delayed posting
## [0.8.0] - 2023-08-15
The bot already knows how to pick a random quote, edit it, and post it to the group immediately.
### Added
- starting work with authorization
- realising publishing into telegram group
- work with publishing quote to chat
## [0.7.0] - 2023-08-12
### Added
- implemented selection of a quote and its image
- implemented image retrieval by keywords with google api
- successful sending of the edited quote and saving it to the database
### Changed
- put keyboard creation into a separate class
- refactoring, implement methods edit,confirm,reject
- changes in method parameters
## [0.6.1] - 2023-08-10
### Added
- working variant of changing the text of a quote sent by bot
- success removing keyboard after decision in message
- realise buttons accept and reject
### Changed
- refactoring
## [0.6.0] - 2023-08-09
### Added
- successfully receiving a response from rest api with a quote
- initial commit telegram bot
### Changed
- stylistic edits
## [0.5.0] - 2023-08-08
### Added
- first version of the project implementation
- realising parsing books and get quotes from api
- the success of getting a random quote from a doc file
### Changed
- updated mapping, add custom MapperConvert
## [0.4.0] - 2023-08-07
### Added
- add business logic for service layer
- add logging
- add tests for ScanService
## [0.3.0] - 2023-08-05
### Added
- realise scanBooks method multi-tasked
- first simple realisation parsing all of needed formats
## [0.2.0] - 2023-08-03
### Added
- successful parsing of fb2 format
## [0.1.0] - 2023-08-03
### Added
- add model, dto, create sql tables
- initial project commit