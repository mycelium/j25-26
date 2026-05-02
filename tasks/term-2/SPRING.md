## Spring Framework task

### Common requirements (for all variants):

#### 1. Sources and Artifacts
- **Version Control and Collaboration**
  - All code must be committed to a GitHub repository (one repository per team).
  - The repository must include a README.md file with a detailed guide on how to:
    - Set up the project locally.
    - Build and run the application.
    - Deploy the application using Docker.
    - Interact with the Telegram bot.
  - Commit history should reflect meaningful contributions from all team members.
- **Target Artifacts**
  - The application must produce a runnable fat JAR as the primary artifact.
  - The application must be deployable as a Docker container.
  - Use a build tool (Gradle, Maven or SBT) to manage dependencies, build, and package the application.
#### 2. Technology Stack
- **Core Technologies**
  - Telegram API: The application must integrate with the Telegram API to provide bot functionality.
  - LLM API: The application must integrate with an LLM API (e.g., OpenAI, Anthropic, GigaChat) to provide AI-powered functionality.
  - Programming Language (use one of the following):
    - Java 25
    - Kotlin
    - Scala 3
  - Framework:
    - Use Spring 7 for dependency injection and application structure.
    - **Spring Boot is strictly prohibited for all students.**
  - Message queue
    - Use message queue specified in google doc. **IF NEEDED**
- **Deployment and Containerization**
  - Use Docker to containerize the application.
  - Provide a Docker Compose file for local development and testing.
  - Publish the Docker image to Docker Hub for deployment.

#### 3. Code Quality and Best Practices
- **Code Style**
  - Adhere to a recognized Java code style guide (e.g., Google Java Style Guide, Oracle Code Conventions).
  - Bonus: Use static code analysis tools (e.g., Checkstyle, PMD, or SonarQube) to enforce code quality.
- **Modern Language Features**
  - Use modern language constructs and features available in the chosen language.
  - Code written in outdated styles (e.g., Java 1.5 style) will not be accepted.
- **Clean Code Principles**
  - Follow clean code principles: meaningful variable names, proper encapsulation, and modular design.
  - Avoid code duplication and ensure proper error handling.

#### 4. Submission Guidelines
- **To submit the coursework, provide the following:**
  - Requirements
  - Architecture design
  - GitHub Repository Link:
    - Include a link to the GitHub repository with the complete codebase.
    - Ensure the repository has a clear and detailed README.md file.
  - Docker Hub Link:
    - Provide a link to the Docker Hub repository where the Docker image is published.
  - Telegram Bot Link:
    - Share the link or username of the Telegram bot for testing and interaction.
- **Submission process**
  - **Step 1: Requirements**
    - Provide a detailed requirements document that outlines:
      - The purpose and functionality of the application.
      - Key features of the Telegram bot.
      - Non-functional requirements (e.g., performance, scalability, etc.).
      - Any assumptions or constraints.
    - This document should be clear, concise, and well-structured.
  - **Step 2: Architecture**
    - Submit an architecture document that includes:
      - A high-level overview of the system design.
      - Diagrams (e.g., component diagrams, sequence diagrams, or flowcharts) to illustrate the architecture.
      - Explanation of the chosen technology stack and its justification.
      - Description of how the application will be built, deployed, and run.
    - The architecture document should demonstrate a clear understanding of the system's structure and design decisions.
  - **Step 3: Full Project**
#### 5. Additional Notes
- **Teamwork:** All team members must contribute equally. The commit history should reflect individual contributions.
- **Documentation:** Proper documentation (in-code comments, README, etc.) is mandatory.
- **Testing:** While not explicitly required, writing unit tests or integration tests is highly encouraged.
- **Deadline:** Ensure the coursework is submitted before the deadline. Late submissions will not be accepted.
- **Specific requirements** (technical restrictions, etc): [google sheet](https://docs.google.com/spreadsheets/d/1ubs3omt2wO_xVBQOxGvMOkwLpKx6d-UFb9lJPf4FcTs/edit?gid=1080477820#gid=1080477820)
#### 6. Functional requirements
- **Common**
  - Authorization
    - via telegram
    - via http
  - Roles
    - `User`
    - `Admin`
  - Healthcheck endpoint (no authorization required)
    - `/healthcheck`
      - server status
      - list of authors (students)
  - Users endpoint (only for admins)
    - List of all users
- **Variant specific**
  - Must be done by yourself

### Variants

#### 1. Google Form Quiz Solver Bot

**1.1. Form Management**
- Add a Google Form by link.
- List saved forms (title, link, date added).
- Remove forms.

**1.2. Quiz Solving**
- Solve a quiz from a saved form using LLM.
- Show generated answers with confidence level for each question.
- Re-solve a form (get a new set of LLM answers).

**1.3. History**
- Show solving history (form, date, generated answers).
- Show all solved forms for a specified period.

**1.4. Bonus Features**
- Submit answers automatically to the Google Form on behalf of the user.
- Show LLM reasoning explanation for each answer.
- Schedule automatic solving at a specified time.

**1.5. Notes**
- Use Google Forms API to read form structure and questions.
- Use any LLM API (e.g., OpenAI, Anthropic, GigaChat) for answer generation.

#### 2. Restaurant Advisor Bot

**2.1. Preferences**
- Add dietary preferences (e.g., vegetarian, allergies, cuisine types).
- Show current preferences.
- Delete preferences.

**2.2. Location Management**
- Set location (city or geomarker).
- Show current location.
- Update location.

**2.3. Restaurant Search**
- Request a restaurant with query (cuisine, keywords, etc.).
- Use preferences and current location if criteria are not specified.
- Get LLM-powered explanation of why a restaurant matches the user's preferences.
- Request a random restaurant near the current location.

**2.4. Visit List**
- Add restaurants to visit list.
- Show visit list.
- Mark restaurants as visited.
- Remove restaurants from visit list.

**2.5. Bonus Features**
- LLM-generated personalized summaries of restaurant reviews.
- Create LLM-suggested routes for visiting multiple restaurants.
- Add ratings and personal notes to visited restaurants.

**2.6. Notes**
- Use any restaurant or maps API (e.g., Google Places, Foursquare, Yelp) for restaurant data.
- Use any LLM API for recommendations and explanations.

#### 3. Cryptocurrency Investment Advisor Bot

**3.1. Preferences**
- Set default cryptocurrency (e.g., BTC, ETH).
- Set default fiat currency (e.g., USD, EUR).

**3.2. Market Data**
- Show current price of a cryptocurrency.
- Show price history for a specified period (e.g., last 7 days, 30 days).
- Compare prices of multiple cryptocurrencies.

**3.3. Portfolio Management**
- Add cryptocurrencies to a portfolio (with purchase price and amount).
- Track portfolio value over time.
- Show profit/loss for each cryptocurrency in the portfolio.

**3.4. LLM Advisor**
- Get LLM-based investment analysis for a specific cryptocurrency.
- Get a portfolio review with LLM recommendations.
- Ask the advisor a free-form question about cryptocurrency markets.

**3.5. Alerts**
- Set price alerts for specific cryptocurrencies (e.g., notify when BTC > $50,000).
- Automatic alerts when a significant price change occurs.
- Show and delete active alerts.

**3.6. Bonus Features**
- News integration: fetch and display the latest cryptocurrency news.
- LLM sentiment analysis of recent news for a specific cryptocurrency.
- Show market cap, trading volume, and other key metrics.

**3.7. Notes**
- Use any cryptocurrency API (e.g., CoinGecko, CoinMarketCap) for market data.
- Use any LLM API for investment advice and analysis.

#### 4. Anime Tracker and Advisor Bot

**4.1. Preferences**
- Set favorite genres (e.g., action, romance, fantasy, shonen).
- Set favorite studios (e.g., Studio Ghibli, MAPPA).
- Set preferred release years.
- Delete preferences.

**4.2. Watchlist**
- Add anime to watchlist.
- Show watchlist.
- Mark anime as watched.
- Remove anime from watchlist.
- Track watched episodes for ongoing anime.

**4.3. Coming Anime**
- Browse upcoming seasonal anime.
- Subscribe to release notifications for followed series.
- Show the release schedule for the current season.

**4.4. LLM Advisor**
- Get personalized recommendations based on preferences and watch history.
- Find similar anime to a specific title using LLM.
- Ask the advisor a free-form question about anime.

**4.5. Bonus Features**
- Add ratings and reviews to watched anime.
- Community features: create groups and share watchlists with friends.
- Manga integration: search for manga related to an anime, track reading progress.

**4.6. Notes**
- Use any anime API (e.g., AniList, MyAnimeList, Jikan) for anime data.
- Use any LLM API for recommendations and analysis.

#### 5. Movie Tracker and Advisor Bot

**5.1. Preferences**
- Set favorite genres, actors, and directors.
- Show current preferences.
- Delete preferences.

**5.2. Watchlist**
- Add films to watchlist.
- Show watchlist.
- Mark films as watched.
- Remove films from watchlist.

**5.3. Film Search**
- Search for a film by query (genre, actors, keywords, etc.).
- Use preferences if criteria are not set.
- Request a random film.

**5.4. LLM Advisor**
- Get personalized movie recommendations based on preferences and watch history.
- Find similar films to a specific title using LLM.
- Ask the advisor a free-form question about movies.

**5.5. Bonus Features**
- Add ratings to watched films (-10 to 10).
- Show all watched films for a specified period.
- Share watchlist with other users.
- Provide links to streaming platforms (e.g., Netflix, Kinopoisk).

**5.6. Notes**
- Use any movie database API (e.g., TMDB, OMDB) for film data.
- Use any LLM API for recommendations and analysis.

#### 6. Book Tracker and Advisor Bot

**6.1. Preferences**
- Set favorite genres and authors.
- Show current preferences.
- Delete preferences.

**6.2. Reading List**
- Add books with status (to read / reading / finished).
- Show reading list filtered by status.
- Update reading progress (current page or chapter).
- Remove books from reading list.

**6.3. Book Search**
- Search for a book by query (genre, author, keywords, etc.).
- Use preferences if criteria are not set.
- Request a random book.

**6.4. LLM Advisor**
- Get personalized book recommendations based on preferences and reading history.
- Find similar books to a specific title using LLM.
- Get an LLM-generated overview or summary of a book.
- Ask the advisor a free-form question about books.

**6.5. Bonus Features**
- Add ratings and personal notes to finished books.
- Save and show favorite quotes from books.
- Share reading list with other users.
- Reading reminders (e.g., daily reading goal notifications).

**6.6. Notes**
- Use any book API (e.g., Google Books, Open Library) for book data.
- Use any LLM API for recommendations and summaries.

#### 7. Quiz Bot with LLM Question Generator

**7.1. Question Management**
- Add questions manually (with answer and tags/domains).
- Generate questions using LLM (by topic/domain and difficulty level).
- List, update, and delete questions.

**7.2. Quiz Functionality**
- Ask for a random question.
- Ask for a random question from a specific domain (by tags).
- Automatically send one random question by schedule.
- Get LLM explanation of the correct answer after responding.

**7.3. Score Management**
- Show user score.
- Reset score (drop to zero).
- Show score by domain (tags).

**7.4. Bonus Features**
- Groups:
  - Create groups and invite members.
  - Group score table.
  - Group question by schedule (random question from all group members).
- LLM-generated hints for difficult questions.
- Adaptive difficulty: LLM adjusts question complexity based on user performance.

#### 8. Clothes Advisor Bot

**8.1. Profile**
- Set gender.
- Add typical activities (e.g., office work, outdoor sports, casual outings).
- Update and delete profile settings.

**8.2. Weather Integration**
- Set home location (city or geomarker).
- Show current weather at the set location.
- Update location.

**8.3. Outfit Recommendations**
- Get an outfit recommendation for today based on current weather, gender, and selected activity.
- Get an outfit recommendation for a specified date and activity.
- Ask the advisor a free-form question about clothing choices.

**8.4. Wardrobe**
- Add clothing items (category, color, season).
- Show wardrobe.
- Remove items from wardrobe.
- Get outfit recommendations using items from the personal wardrobe.

**8.5. Bonus Features**
- Outfit history: show past recommendations.
- Shopping suggestions: LLM recommends items to buy based on wardrobe gaps.
- Accept live user location for real-time weather-based recommendations.

**8.6. Notes**
- Use any weather API (e.g., OpenWeatherMap) for weather data.
- Use any LLM API for outfit recommendations.

#### 9. Collaborative Novel Writing Bot

**9.1. Novel Management**
- Create a novel (title, description, genre).
- List all novels (own and collaborative).
- Delete a novel.

**9.2. Collaboration**
- Invite co-authors to a novel.
- Show list of authors for a novel.
- Remove co-authors.

**9.3. Writing**
- Add chapters or sections to a novel.
- Show the full text of a novel or a specific chapter.
- Update and delete chapters.
- View edit history for a chapter.

**9.4. LLM Co-author**
- Get LLM-generated continuation suggestions for the current text.
- Ask LLM for plot or character development advice.
- Generate a chapter draft using LLM based on a user-provided prompt.

**9.5. Bonus Features**
- LLM consistency check: detect contradictions between chapters.
- Export a novel as a plain text or PDF file.
- Writing schedule reminders (e.g., write a chapter per week).

#### 10. Lessons and Exams Timetable Bot

**10.1. Timetable**
- Import timetable from a file (e.g., CSV, iCal) or an external API.
- Show schedule: today, tomorrow, this week.
- Manually add, update, and delete lessons and exams.

**10.2. Task Management**
- Add study tasks (exercises, course-works, deadlines) linked to subjects.
- Show tasks by subject and by deadline (today, tomorrow, week).
- Mark tasks as done.

**10.3. Reminders**
- Remind about upcoming lessons and exams.
- Remind about approaching task deadlines.

**10.4. LLM Advisor**
- Get LLM-based advice on study priority (what to focus on first).
- Get an LLM-generated study plan for an upcoming exam.
- Ask the advisor a free-form question about study planning.

**10.5. Bonus Features**
- Calendar integration (Google Calendar, Yandex Calendar):
  - Sync timetable and tasks with external calendars.
- Study time tracking: log study sessions per subject.
- LLM analysis of study habits with suggestions for improvement.

**10.6. Notes**
- Use any calendar or schedule API available at the student's institution if applicable.
- Use any LLM API for planning advice and study plan generation.
