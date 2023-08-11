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

## Building and Installing

Requirements:

* JDK 17+
* Maven
* ds-generator-core-1.2.0.jar in lib/ (I had to build a jar from [ds-generator-core on github](https://github.com/yuchengxin/mybatis-ds-generator))

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

Further users can be added by an admin via the USER ADD command (see HELP USER), or a user can register themselves by issuing a REGISTER command in a public or private message to the bot. Access is controlled by setting flags with the USER ADDFLAG and USER REMOVEFLAG commands.

For example:

> &lt;rick&gt; .user addflag beth admin,dcc<br/>
> &lt;morty&gt; Flags for beth: ADMIN, DCC<br/>

Bot User Flags:

| Flag   | Description                                                                       |
|--------|-----------------------------------------------------------------------------------|
| ADMIN  | Marks the user as an admin of the bot, allowing access to restricted commands     |
| DCC    | Allows the user to establish a DCC CHAT connection with the bot                   |
| IGNORE | Ignore everything from this user (good for assholes or other bots)                |

Per-channel user flags can be set for your managed channels via the USER ADDCHANFLAG and USER REMOVECHANFLAG commands.

Managed Channel User Flags:

| Flag       | Description                                                                       |
|------------|-----------------------------------------------------------------------------------|
| AUTO_OP    | The user should automatically receive operator status (+o) in the channel on join |
| AUTO_VOICE | The user should automatically receive voice status (+v) in the channel on join    |

### Managed Channels

You can also add a channel as a "managed channel" via the CHANNEL command. This allows you to set channel-specific settings to control how the bot behaves in that channel.

For example:

> &lt;rick&gt; .channel add #blipsandchitz<br/>
> &lt;morty&gt; Added channel #blipsandchitz with flags AUTO_JOIN, SHORTEN_LINKS<br/>
> &lt;rick&gt; .channel addflag #blipsandchitz show_titles,show_tweets<br/>
> &lt;morty&gt; Flags for #blipsandchitz: AUTO_JOIN, SHORTEN_LINKS, SHOW_TITLES<br/>

Managed Channel Flags:

| Flag          | Description                                 |
|---------------|---------------------------------------------|
| AUTO_JOIN     | Automatically join the channel              |
| ENFORCE_BANS  | Enforce channel bans (not implemented yet)  |
| ENFORCE_MODES | Enforce channel modes (not implemented yet) |
| SHORTEN_LINKS | Shorten links on the channel                |
| SHOW_TITLES   | Fetch and display titles for links          |

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

To get a list of commands and basic usage information, issue the HELP command in a channel or private message with the bot.

For example:

> &lt;rick&gt; .help<br/>
> &lt;morty&gt; Commands: CHAT, DICT, GEOIP, GOO, GOOGLE, HELP, HOST, IMDB, NICK, REGISTER, RT, STOCK, URB, URBAN, WEATHER, WHO, WORDLE, WOTD, WTR, YEAR<br/>
> &lt;morty&gt; Admin commands: BAN, BANKICK, CHANNEL, CONFIG, JOIN, KICK, KICKBAN, MSG, NICK, OP, PART, QUIT, USER<br/>
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
