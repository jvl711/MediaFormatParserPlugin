package jvl.mediaformat;


public class MediaFormatParserPluginVersion
{
    private final static String BUILDTIME = "09/03/2020 15:50:56";
    private final static String BUILDNUMBER = "63";
    private final static String VERSION = "0.5";
    
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
