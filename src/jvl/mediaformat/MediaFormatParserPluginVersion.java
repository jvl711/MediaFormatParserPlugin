package jvl.mediaformat;


public class MediaFormatParserPluginVersion
{
    private final static String BUILDTIME = "10/15/2022 07:47:25";
    private final static String BUILDNUMBER = "129";
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
