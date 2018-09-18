# GensokyoBot
This bot is a fork of FredBoat. Most features of FredBoat have been removed or replaced in favour of offering streaming from a single source.
The bot is optimized in such a way that only one stream is opened to the source.

# Documentation
1. Java 10 __JDK__: [Oracle](http://www.oracle.com/technetwork/java/javase/downloads/jdk10-downloads-4416644.html) or [OpenJDK](http://jdk.java.net/10/)
2. [Git](https://www.atlassian.com/git/tutorials/install-git)
3. [JDK added to your PATH](https://www.tutorialspoint.com/maven/maven_environment_setup.htm)

Installing and running the bot
```md
sudo apt -y update && apt -y upgrade
sudo apt -y install maven
git clone https://github.com/SixAiy/GensokyoBot.git #due to the version upgrade my version of GensokyoBot is more updated.
cd GensokyoBot
mvn package shade:shade
mv target/GensokyoBot-1.0.jar /home/YOUR_USER/GensokyoBot
java -jar GensokyoBot-1.0.jar
```

# Service
```md
cp gensokyobot.service /etc/systemd/system/
systemctl daemon-reload
systemctl enable gensokyobot
systemctl start gensokyobot
```

# Commands
```md
< Music Commands >
,,join
#Joins your voice chat and begin playing.
,,leave
#Leaves the voice chat, stopping the music
,,np
#Shows the song currently playing in a nice embed
,,stats
#Displays stats about this bot
,,shards
#Displays the version of JDA on this bot
,,invite
#Displays invite link for this bot
,,help
#Displays this help message


Invite this bot: https://discordapp.com/oauth2/authorize?&client_id=302857939910131712&scope=bot
Source code: https://github.com/Frederikam/GensokyoBot

Content provided by gensokyoradio.net.
The GR logo is a trademark of Gensokyo Radio.
Gensokyo Radio is © LunarSpotlight.
```
