# MortyBot

### An IRC bot built on the [PircBotX](https://github.com/pircbotx/pircbotx) framework

This is a simple IRC bot I wrote for fun and to learn Java. It is designed for [EFnet](http://www.efnet.org) and so does not leverage services.

## Implemented Features

* Bot user management and persistence
* Command handler
* Auto-op
* Link shortening
* Weather lookups
* Stock symbol lookups
* IMDB and rotten tomatoes search
* Support for 005 numeric

## Building and Installing

Requirements:

* JDK 11 or higher
* Maven (for building and packaging)

To build and package the bot, use maven:

> mvn clean package

You can then find the packaged archives in the target directory.

> mortybot.tar<br>
> mortybot.tar.bz2<br>
> mortybot.tar.gz<br>
> mortybot.zip<br>

To install, simply unpack the archive of your choice into the desired directory.

## Configuring

At minimum, you will need to edit the appropriate run script for your platform and set the value of the MORTYBOT_HOME environment variable.

I also recommend having a look through conf/bot.properties and to setup an admin user for yourself in conf/users.conf.

## Running the Bot

To start the bot, execute one of the provided run scripts based on your platform.

UNIX:

> ./run.sh

Windows:

> run.bat

## License

This project is licensed under GNU GPL v3 to be compatible with the PircBotX license.
