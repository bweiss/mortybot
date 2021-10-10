#!/usr/bin/env sh

# Uncomment the following line to set the bot's base directory, or set this in your shell's rc file.
#export MORTYBOT_HOME=/path/to/mortybot

java -Dlogback.configurationFile=conf/logback.xml -jar mortybot.jar
