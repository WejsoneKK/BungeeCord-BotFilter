package ru.leymooo.botfilter.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Settings extends Config
{

    @Ignore
    public static final Settings IMP = new Settings();

    @Comment(
        {
        "Please submit all errors, bugs, suggestions and other requests on github "
        })
    @Final
    public final String ISSUES = "https://github.com/WejsoneKK/BungeeCord-BotFilter/issues";
    @Final
    public final String HELP = "http://wiki.wejsonekk.eu/en/BungeeCord-BotFilter/help";
    @Final
    public String BOT_FILTER_VERSION = "3.8.14-Custom";

    @Create
    public MESSAGES MESSAGES;
    @Create
    public DIMENSIONS DIMENSIONS;
    @Create
    public GEO_IP GEO_IP;
    @Create
    public PING_CHECK PING_CHECK;
    @Create
    public SERVER_PING_CHECK SERVER_PING_CHECK;
    @Create
    public PROTECTION PROTECTION;
    @Create
    public SQL SQL;
    @Comment(
        {
        "How many players / bots should join in a minute, to enable the protection",
        "Recommended settings without advertising: ",
        "Under 150 players - 25, under 250 - 30, under 350 - 35, under 550 - 40,45, higher - adjust for yourself ",
        "During advertising or when just-just setted up the protection, it is recommended to set high these valu. es"
        })
    public int PROTECTION_THRESHOLD = 30;
    @Comment("How long is automatic protection active? In milliseconds. 1 second = 1000")
    public int PROTECTION_TIME = 120000;
    @Comment("Whether to force check the bot when joining the server during a bot attack, no matter whether the check is passed or not")
    public boolean FORCE_CHECK_ON_ATTACK = true;
    @Comment("Should we show the player count from the filter?")
    public boolean SHOW_ONLINE = true;
    @Comment("How much time does the player have to pass the verification. In milliseconds. 1 second = 1000")
    public int TIME_OUT = 12700;
    @Comment("Enable fix for 'Team 'xxx' already exist in this scoreboard'")
    public boolean FIX_SCOREBOARD_TEAMS = true;
    @Comment("Should the IP addresses of the players / bots who failed the verification be logged to a file?")
    public boolean SAVE_FAILED_IPS_TO_FILE = true;

    public void reload(File file)
    {
        load( file );
        save( file );
    }

    @Comment("Please don't use '\\n', use %nl%")
    public static class MESSAGES
    {

        public String PREFIX = "&b&lAnti&3&lBot";
        public String CHECKING = "%prefix%&7>> &aZaczekaj na zakończenie weryfikacji...";
        public String CHECKING_CAPTCHA = "%prefix%&7>> &aWprowadź numer z obrazka do czatu";
        public String CHECKING_CAPTCHA_WRONG = "%prefix%&7>> &cWprowadzono nieprawidłowy captcha, spróbuj ponownie. Masz &a%s &c%s";
        public String SUCCESSFULLY = "%prefix%&7>> &aWeryfikacja przeszła pomyślnie";
        public String KICK_MANY_CHECKS = "%prefix%%nl%%nl%&cWykryliśmy podejrzaną aktywność z Twojego adresu ip%nl%%nl%&6Prosimy spróbować ponownie za 10 minut.";
        public String KICK_NOT_PLAYER = "%prefix%%nl%%nl%&cYNie przeszedłeś weryfikacji, być może jesteś botem%nl%&7&oJeśli tak nie jest, spróbuj ponownie";
        public String KICK_COUNTRY = "%prefix%%nl%%nl%&cTwój kraj został zablokowany na serwerze";
        public String KICK_BIG_PING = "%prefix%%nl%%nl%&cTwój ping jest bardzo wysoki, najprawdopodobniej jesteś botem";
        @Comment(
            {
            "Title%nl%Subtitle", "You can leave blank to disable the title (ex: CHECKING_TITLE = \"\") ",
            "Disabling titles may slightly improve performance"
            })
        public String CHECKING_TITLE = "&b&lAnti&3&lBot%nl%&aSprawdzanie...";
        public String CHECKING_TITLE_SUS = "&rWeryfikacja przeszła pomyślnie%nl%&aMiłej gry";
        public String CHECKING_TITLE_CAPTCHA = " %nl%&rWprowadź captch na czacie!";
    }

    @Comment("Enable or disable GeoIp")
    public static class GEO_IP
    {

        @Comment(
            {
            "When does the check works",
            "0 - Always",
            "1 - Only during bot attacks",
            "2 - Disable"
            })
        public int MODE = 1;
        @Comment(
            {
            "How exactly does GeoIp work",
            "0 - White list(Only those countries that are on the list can join)",
            "1 - Black list(Only countries that are not on the list can join)"
            })
        public int TYPE = 0;
        @Comment(
            {
            "Where to download GEOIP from",
            "Change the link if for some reason it doesn't download from this one",
            "The file must end in .mmdb or be packed into .tar.gz"
            })
        public String NEW_GEOIP_DOWNLOAD_URL = "https://download.maxmind.com/app/geoip_download?edition_id=GeoLite2-Country&license_key=%license_key%&suffix=tar.gz";
        @Comment(
            {
            "If the key stops working, you need to register at https://www.maxmind.com/",
            "and generate a new key here https://www.maxmind.com/en/accounts/current/license-key"
            })
        public String MAXMIND_LICENSE_KEY = "P5g0fVdAQIq8yQau";
        @Comment("Разрешённые странны")
        public List<String> ALLOWED_COUNTRIES = Arrays.asList( "RU", "UA", "BY", "KZ", "EE", "MD", "KG", "AZ", "LT", "LV", "GE", "PL" );
    }

    @Comment("Enable or disable high ping check")
    public static class PING_CHECK
    {

        @Comment(
            {
            "When does the check works",
            "0 - Always",
            "1 - Only during bot attacks",
            "2 - Disable"
            })
        public int MODE = 1;
        @Comment("Maximum allowed ping")
        public int MAX_PING = 350;
    }

    @Comment("Enable or disable Direct Connect check")
    public static class SERVER_PING_CHECK
    {

        @Comment(
            {
            "When the check works",
            "0 - Always",
            "1 - Only during bot attacks",
            "2 - Disable",
            "Disabled by default, because it is very unstable against strong attacks"
            })
        public int MODE = 2;
        @Comment("When is it allowed to join the server after receiving server's motd")
        public int CACHE_TIME = 12;
        public List<String> KICK_MESSAGE = new ArrayList()
        {
            {
                add( "%nl%" );
                add( "%nl%" );
                add( "&cZostałeś wyrzucony! Nie używaj bezpośredniego połączenia" );
                add( "%nl%" );
                add( "%nl%" );
                add( "&bAby dołączyć do serwera: freezemc.pl" );
                add( "%nl%" );
                add( "&71) &rDodanie serwera do listy &lserver." );
                add( "%nl%" );
                add( "&lNasze IP &8>> &b&lFreezeMC.PL" );
                add( "%nl%" );
                add( "%nl%" );
                add( "&72) &rOdśwież listę serwerów. " );
                add( "%nl%" );
                add( "&oAby ją odświeżyć, kliknij przycisk &c&lRefresh&r" );
                add( "%nl%" );
                add( "%nl%" );
                add( "&73) &rOdczekaj &c1-3&r sekund i dołącz do serwera!" );

            }
        };
    }

    @Comment(
        {
        "Setting up exactly how the protection will work",
        "0 - Only check with captcha",
        "1 - Fall check + captcha",
        "2 - Fall check, if it failed, then captcha"
        })
    public static class PROTECTION
    {

        @Comment("Operation mode while there is no attack")
        public int NORMAL = 2;
        @Comment("Operation mode during the attack")
        public int ON_ATTACK = 1;
        @Comment(
            {
            "Enable constant checking of the players?",
            "When enabling this feature, don't forget to set the protection-threshold value higher"
            })
        public boolean ALWAYS_CHECK = false;

        @Comment(
            {
            "Check players with ip 127.0.0.1?", "May be useful when using Geyser",
            "0 - check", "1 - don't check", "2 - check every time"
            })
        public int CHECK_LOCALHOST = 0;

        @Comment("Disable check for Geyser-standalone clients? auth-type must be set to floodgate")
        public boolean SKIP_GEYSER = false;
        /*
        @Comment(
                {
                    "Когда работают дополнительные проверки по протоколу",
                    "    (Пакеты на которые клиент должен всегда отвечать)",
                    "0 - Всегда",
                    "1 - Только во время бот атаки",
                    "2 - Отключить"
                })
        public int ADDITIONAL_CHECKS = 1;
         */
    }

    @Comment("Database configuration")
    public static class SQL
    {

        @Comment("Database type. sqlite or mysql")
        public String STORAGE_TYPE = "sqlite";
        @Comment("After how many days, remove players from the database who have passed the verification and never logged in again. 0 or less to disable it")
        public int PURGE_TIME = 14;
        @Comment("Mysql configuration")
        public String HOSTNAME = "127.0.0.1";
        public int PORT = 3306;
        public String USER = "user";
        public String PASSWORD = "password";
        public String DATABASE = "database";
        @Comment("Interval in millieseconds. How often it will synchronize data bases if multibungee is used.")
        public int SYNC_INTERVAL = -1;
    }

    @Comment("Virtual world settings")
    public static class DIMENSIONS
    {
        @Comment(
            {
            "Which world to use",
            "0 - Standard world",
            "1 - Nether",
            "2 - End"
            })
        public int TYPE = 0;
    }
}
