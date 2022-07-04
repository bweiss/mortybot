#!/usr/bin/env sh

# Uncomment the following line if you need to set the bot's base directory. Otherwise it will attempt to autodetect.
#export MORTYBOT_HOME=/path/to/mortybot

# Note: You can change the location of the config directory by passing the -Dmortybot.config.dir argument
java -Dlogback.configurationFile=conf/logback.xml -jar mortybot.jar
