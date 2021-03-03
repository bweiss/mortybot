#!/usr/bin/env sh

# Uncomment the following line to set the bot's base directory, or set this in your shell's rc file.
#export MORTYBOT_HOME=/path/to/mortybot

export JAVA_OPTS="-Dlogback.configurationFile=conf/logback.xml"

java $JAVA_OPTS -jar mortybot.jar
