package jvl.mediaformat;


public class MediaFormatParserPluginVersion
{
    private final static String BUILDTIME = "10/10/2022 19:33:24";
    private final static String BUILDNUMBER = "126";
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
