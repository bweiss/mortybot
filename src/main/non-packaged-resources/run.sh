#!/usr/bin/env sh

# Uncomment the following line to set the bot's base directory. Otherwise it will attempt to autodetect.
#export MORTYBOT_HOME=/path/to/mortybot

java -Dlogback.configurationFile=conf/logback.xml -jar mortybot.jar
