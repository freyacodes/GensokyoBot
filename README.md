![GensokyoBot](https://sixaiy.com/images/gb-banner.png)
# GensokyoBot [![Github commits (since latest release)](https://img.shields.io/github/commits-since/Frederikam/FredBoat/latest.svg)]()
```md
Frederiham no longer maintains this bot from his github.
This bot is a fork of GensokyoBot with upgrades to keep the bot stable.

GensokyoBot is a Radio Music Bot playing music from gensokyoradio.net
The bot is optimized only to play GensokyoRadio Music.
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

# Selfhosting
1. Java 10 __JDK__: [Oracle](http://www.oracle.com/technetwork/java/javase/downloads/jdk10-downloads-4416644.html) or [OpenJDK](http://jdk.java.net/10/)
2. [Git](https://www.atlassian.com/git/tutorials/install-git)
3. [JDK added to your PATH](https://www.tutorialspoint.com/maven/maven_environment_setup.htm)

Installing and running the bot
```md
sudo apt -y update && apt -y upgrade
sudo apt -y install maven
git clone https://github.com/SixAiy/GensokyoBot.git
cd GensokyoBot
mvn package shade:shade
mv target/GensokyoBot-1.0.jar /home/YOUR_USER/GensokyoBot
java -jar GensokyoBot-1.0.jar
```

Hosting GensokyoBot with Systemd
```md
cp gensokyobot.service /etc/systemd/system/
systemctl daemon-reload
systemctl enable gensokyobot
systemctl start gensokyobot
```

# Credits
Credits to [Frederikham](https://github.com/Frederikam) for the creation of GensokyoBot
