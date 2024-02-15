# MortyBot

### An IRC bot built on the [PircBotX](https://github.com/pircbotx/pircbotx) framework

This is a simple IRC bot I wrote for fun and to learn Java. It is designed for [EFnet](http://www.efnet.org) and so does not leverage services.

## Implemented Features

* User and channel management
* SQLite persistence
* Command handler
* DCC CHAT with party line
* Auto-op
* Link shortening and title display
* Google search
* Weather lookups
* Stock symbol lookups
* IMDB and Rotten Tomatoes search
* GeoIP2 address location lookups
* Merriam-Webster dictionary and word-of-the-day lookups
* Urban Dictionary lookups
* Wordle game
* Shodan host lookups
* Sports scores for MLB, NBA, NFL, and NHL

## Building and Installing

Requirements:

* JDK 21+
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

Most configuration of the bot is done via the conf/bot.properties file. You should edit this file to your liking prior to starting the bot.

Note: You can change the location of the config directory by passing the `-Dmortybot.config.dir` argument to the JVM when starting the bot (see run.sh or run.bat).

### Features Requiring API Keys

There are a number of features that require (free) API keys to function. They can be set via properties file or environment variable. Visit each service's official website for information on how to obtain a key.

| Feature                                    | Property             | Environment Variable |
|--------------------------------------------|----------------------|----------------------|
| Link shortening with bit.ly                | bitly.api.key        | BITLY_API_KEY        |
| MaxMind GeoLite IP lookups (GEOIP command) | maxmind.api.key      | MAXMIND_API_KEY      |
| Shodan host lookups (HOST command)         | shodan.api.key       | SHODAN_API_KEY       |

## Running the Bot

To start the bot, execute one of the provided run scripts based on your platform.

UNIX:
> ./run.sh

Windows:
> run.bat

## Usage

### First run

The first time you run the bot, it is strongly recommended that you send a REGISTER command to the bot once it has connected to the network. This will create a bot user profile with your current hostmask and grant admin privileges (only the first user to register will be given admin privileges). It is also recommended to set a password with the PASS command via a private message to the bot.

Once registered, the admin user can add, remove, and modify the bot's channels and users via the CHANNEL and USER commands.

### Help

To get a list of commands and basic usage information, issue the HELP command in a channel, private message, or DCC chat with the bot.

For example:
> &lt;rick&gt; .help<br/>
> &lt;morty&gt; Commands: CHAT, CHPASS, DICT, GEOIP, GOO, GOOGLE, HELP, HOST, IDENT, IDENTIFY, IMDB, MLB, MST, NBA, NFL, NHL, PASS, REGISTER, RT, STOCK, UFC, URB, URBAN, WEATHER, WHO, WHOAMI, WORDLE, WOTD, WTR, YEAR<br/>
> &lt;morty&gt; Admin commands: BAN, BANKICK, CHANNEL, JOIN, KICK, KICKBAN, MSG, NICK, OP, PART, QUIT, TEST, USER<br/>
> &lt;morty&gt; Type .HELP &lt;command&gt; to get more information about a command<br/>
> &lt;rick&gt; .help register<br/>
> &lt;morty&gt; Registers yourself with the bot using your current hostname<br/>
> &lt;morty&gt; Usage: REGISTER &#91;name&#93;<br/>
> &lt;rick&gt; .help wtr<br/>
> &lt;morty&gt; Shows the weather for a location<br/>
> &lt;morty&gt; Usage: WTR &#91;-d&#93; &#91;location&#93;<br/>
> &lt;morty&gt; If the -d option is present the bot will attempt to save your default location (requires being registered with the bot)

## License

This project is licensed under GNU GPL v3 to be compatible with the PircBotX license.
