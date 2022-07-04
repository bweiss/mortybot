# MortyBot

### An IRC bot built on the [PircBotX](https://github.com/pircbotx/pircbotx) framework

This is a simple IRC bot I wrote for fun and to learn Java. It is designed for [EFnet](http://www.efnet.org) and so does not leverage services.

## Implemented Features

* Bot user management and persistence
* Command handler
* DCC CHAT with party line
* Auto-op
* Link shortening/title display/tweet lookups
* Google search
* Weather lookups
* Stock symbol lookups
* IMDB and Rotten Tomatoes search
* GeoIP2 address location lookups
* Merriam-Webster dictionary and word-of-the-day lookups
* BottleBlueBook search
* Urban Dictionary lookups
* Wordle game
* Shodan host lookups

## Building and Installing

Requirements:

* JDK 17+
* Maven

To build and package the bot, use maven:

> mvn clean package

You can then find the packaged archives in the target directory.

> mortybot.tar<br>
> mortybot.tar.bz2<br>
> mortybot.tar.gz<br>
> mortybot.zip<br>

To install, simply unpack the archive of your choice into the desired directory and ensure you've configured a suitable Java runtime (you should set JAVA_HOME in one of the provided run scripts or at the system level).

## Configuring

Most configuration of the bot is done via the conf/bot.properties file. You should edit this file to your liking prior to starting the bot. These properties can be modified while the bot is running via the CONFIG command.

Note: You can change the location of the config directory by passing the -Dmortybot.config.dir argument to the JVM when starting the bot (see run.sh or run.bat).

### Bot Users

After starting the bot for the first time, it is highly recommended that you issue a REGISTER command in a private message to the bot. This will register you with the bot using your current hostmask and admin rights.

Further users can be added by an admin user via the USER ADD command (see HELP USER) or a user can register themselves by issuing a REGISTER command in a public or private message.

Supported flags:

| Flag   | Description                                                                       |
|--------|-----------------------------------------------------------------------------------|
| ADMIN  | Marks the user as an admin of the bot, allowing access to restricted commands     |
| AOP    | Automatically grant operator status (+o) when this user joins one of our channels |
| DCC    | Allows the user to establish a DCC CHAT connection with the bot                   |
| IGNORE | Ignore commands and links from this user (typically used to ignore other bots)    |

### Features Requiring API Keys

There are a number of features that require API keys to function. They can be set via properties file or environment variable.

| Feature                                    | Property             | Environment Variable |
|--------------------------------------------|----------------------|----------------------|
| Link shortening with bit.ly                | bitly.api.key        | BITLY_API_KEY        |
| MaxMind GeoLite IP lookups (GEOIP command) | maxmind.api.key      | MAXMIND_API_KEY      |
| Shodan host lookups (HOST command)         | shodan.api.key       | SHODAN_API_KEY       |
| Tweet lookups (link shortener)             | twitter.bearer.token | TWITTER_BEARER_TOKEN |

## Running the Bot

To start the bot, execute one of the provided run scripts based on your platform.

UNIX:

> ./run.sh

Windows:

> run.bat

## Usage

To get a list of commands and basic usage information, issue the HELP command in a channel or private message with the bot.

For example:

> &lt;rick&gt; .help<br/>
> &lt;morty&gt; Commands: BOTTLE, CHAT, CONFIG, DICT, GEOIP, GOO, HELP, HOST, IMDB, JOIN, MSG, OP, PART, QUIT, REGISTER, RT, STOCK, URB, USER, WHO, WORDLE, WOTD, WTR, YEAR<br/>
> &lt;morty&gt; Type .HELP &lt;command&gt; to get more information about a command<br/>
> &lt;rick&gt; .help wtr<br/>
> &lt;morty&gt; Shows the weather for an area<br/>
> &lt;morty&gt; Usage: WTR &lt;query&gt;<br/>

## License

This project is licensed under GNU GPL v3 to be compatible with the PircBotX license.
