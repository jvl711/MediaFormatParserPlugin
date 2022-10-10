package jvl.mediaformat;


public class MediaFormatParserPluginVersion
{
    private final static String BUILDTIME = "10/09/2022 20:18:46";
    private final static String BUILDNUMBER = "110";
    private final static String VERSION = "0.7";
    
    public static String getVersion()
    {
        return VERSION;
    }
    
    public static String getBuildNumber()
    {
        return BUILDNUMBER;
    }

    public static String getBuildTime()
    {
        return BUILDTIME;
    }
}
