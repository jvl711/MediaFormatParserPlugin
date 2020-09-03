
package jvl.mediaformat;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import jvl.FFmpeg.jni.AVCodec;
import jvl.FFmpeg.jni.AVCodecParameters;
import jvl.FFmpeg.jni.AVFormatContext;
import jvl.FFmpeg.jni.AVMediaType;
import jvl.FFmpeg.jni.AVPacket;
import jvl.FFmpeg.jni.AVStream;
import sage.SageTV;
import sage.SageTVPlugin;
import sage.media.format.AudioFormat;
import sage.media.format.BitstreamFormat;
import sage.media.format.ContainerFormat;
import sage.media.format.FormatParser;
import sage.media.format.SubpictureFormat;
import sage.media.format.VideoFormat;

/**
 * This utilizes the JavaFFmpeg wrapper to perform media format parsing for SageTV.
 * This will require a specific version of Sage.jar that includes the MediaFormatPlusing
 * enhancements as well as the forced subtitles.
 * 
 * @author Joshua Lewis (jvl711)
 */
public class MediaFormatParserPlugin implements sage.media.format.FormatParserPlugin, SageTVPlugin 
{
    private static HashMap<String, String> codecSubsitution;
    private static HashMap<String, String> formatSubsitution;
    
    public MediaFormatParserPlugin(sage.SageTVPluginRegistry stpr, boolean reset){}
    
    
    static
    {
        codecSubsitution = new HashMap<String, String>();
        addCodecSubsitution("MP3FLOAT", "MP3");
        
        formatSubsitution = new HashMap<String, String>();
        //matroska,webm
        addFormatSubsitution("MATROSKA,WEBM", "MATROSKA");
    }
    
    public MediaFormatParserPlugin()
    { 
        //This is a blank constructructor called when creating the FormatParserPlugin instance
        System.out.println("Constructing jvl.mediaformat.MediaFormatParserPlugin");
        System.out.println("MediaFormatParserPlugin Version: " + jvl.mediaformat.MediaFormatParserPluginVersion.getVersion());
        System.out.println("MediaFormatParserPlugin Build Number: " + jvl.mediaformat.MediaFormatParserPluginVersion.getBuildNumber());
        System.out.println("MediaFormatParserPlugin Build DateTime: " + jvl.mediaformat.MediaFormatParserPluginVersion.getBuildTime());
        System.out.println("JavaFFmpeg Version: " + jvl.FFmpeg.jni.Version.getVersion());
        System.out.println("JavaFFmpeg Build Number: " + jvl.FFmpeg.jni.Version.getBuildNumber());
        System.out.println("JavaFFmpeg Build DateTime: " + jvl.FFmpeg.jni.Version.getBuildTime());
    }

    
    /**
     * Takes a lookup value, and returns the santized value that SageTV expects.
     * If you try and add a lookup value that already exists, it will be replaced
     * 
     * @param lookupValue Value to lookup
     * @param subsitution Substitution valye to be used
     */
    private static void addCodecSubsitution(String lookupValue, String subsitution)
    {
        codecSubsitution.put(lookupValue.toUpperCase(), subsitution.toUpperCase());
    }
    
    /**
     * Takes a lookup value, and returns the santized value that SageTV expects.
     * If you try and add a lookup value that already exists, it will be replaced
     * 
     * @param lookupValue Value to lookup
     * @param subsitution Substitution value to be used
     */
    private static void addFormatSubsitution(String lookupValue, String subsitution)
    {
        formatSubsitution.put(lookupValue.toUpperCase(), subsitution.toUpperCase());
    }
    
    /**
     * First tries the SageTV substitution, then tries our internal substitution.
     * 
     * @param codec The lookup value
     * @return Returns the substituted value, or the input value is a substitue 
     * was not available
     */
    private static String substitueCodec(String codec)
    {
        String ret = codec.toUpperCase();
        
        ret = FormatParser.substituteName(ret);
        
        if(codecSubsitution.containsKey(ret))
        {
            ret = codecSubsitution.get(ret);
        }
        
        return ret.toUpperCase();
    }
    
    /**
     * First tries the SageTV substitution, then tries our internal substitution.
     * Comparisons are done in a case insensitive way
     * 
     * @param format The lookup value
     * @return Returns the substituted value, or the input value is a substitute 
     * was not available
     */
    private static String substitueFormat(String format)
    {
        String ret = format.toUpperCase();
        
        ret = FormatParser.substituteName(ret);
        
        if(formatSubsitution.containsKey(ret))
        {
            ret = formatSubsitution.get(ret);
        }
        
        return ret.toUpperCase();
    }
    
    @Override
    public ContainerFormat parseFormat(File file)
    {
        ContainerFormat format = new ContainerFormat();
     
        boolean isDebug = Boolean.parseBoolean(this.getProperty("jvl.MediaFormatParserPlugin.Debug", "false"));
        boolean isDisabled = Boolean.parseBoolean(this.getProperty("jvl.MediaFormatParserPlugin.Disable", "false"));
        
        
        if(isDisabled)
        {
            System.out.println("MediaFormatParserPlugin has been disabled.");
            return null;
        }
        
        AVFormatContext avformat = null;
        ArrayList<BitstreamFormat> streams = new ArrayList<BitstreamFormat>();
        
        try
        {
            System.out.println("MediaFormatParserPlugin processing: " + file.getAbsolutePath());
            avformat = AVFormatContext.buildAVFormatInputContext(file.getAbsolutePath());
        
            format.setFormatName(FormatParser.substituteName(avformat.getFormatName()));
            
            if(isDebug) System.out.println("FormatName: " + avformat.getFormatName());
            if(isDebug) System.out.println("FormatName (Substitution): " + substitueFormat(avformat.getFormatName()));
            
            long duration = (avformat.getDuration() / 1000);
            
            if(isDebug) System.out.println("Duration: " + duration);
            
            if(duration < 0)
            {
                duration = 0;
                System.out.println("MediaFormatParserPlugin: Duration was less than 0");
            }
            
            format.setDuration(duration);
            if(isDebug) System.out.println("Bitrate: " + avformat.getBitrate());
            format.setBitrate((int)avformat.getBitrate());
           
            if(isDebug) System.out.println("Number of Streams: " + avformat.getNumberOfStreams());
            
            for(int i = 0; i < avformat.getNumberOfStreams(); i++)
            {
                 AVCodecParameters avparm = avformat.getAVCodecParameters(i);
                 AVCodec avcodec = AVCodec.getAVCodecDecoder(avparm);
                 AVStream avstream = avformat.getAVStream(i);

                 if(avparm.getCodecType() == AVMediaType.VIDEO)
                 {
                     if(isDebug) System.out.println("Processing Video (" + i + ")");
                     if(avstream.isAttachedPicture())
                     {
                         //Will need to keep an eye on if we need to ignore this track index.
                         //ffmpeg treats this as a track, but I am not sure it should be
                         AVPacket picture = avstream.getAttachedPicturePacket();
                         
                         if(picture.getSize() > 0)
                         {
                             if(isDebug) System.out.println("\tProcessing Video as Attached Picture.");
                             
                             if(isDebug) System.out.println("\tThumbnailSize: " + picture.getSize());
                             format.addMetadata("ThumbnailSize", picture.getSize() + "");
                             if(isDebug) System.out.println("\tThumbnailOffset: " +  picture.getPosition() );
                             format.addMetadata("ThumbnailOffset", picture.getPosition() + "");
                             picture.free();
                         }
                         
                     }
                     else
                     {
                        int arDen =0;
                        int arNum =0;

                        VideoFormat video = new VideoFormat();
                        if(isDebug) System.out.println("\tVideo Codes: " + avcodec.getName());
                        if(isDebug) System.out.println("\tVideo Codes (Substitution): " + substitueCodec(avcodec.getName()));
                        video.setFormatName(substitueCodec(avcodec.getName()));

                        if(avparm.getAspectRatioString().length() > 0)
                        {
                            try
                            {
                                arNum = Integer.parseInt(avparm.getAspectRatioString().split(":")[0]);
                                arDen = Integer.parseInt(avparm.getAspectRatioString().split(":")[1]);
                            }
                            catch(Exception ex){}
                        }

                        if(isDebug) System.out.println("\tAspect Ration Den: " + arDen);
                        if(isDebug) System.out.println("\tAspect Ration Num: " + arNum);
                        video.setArDen(arDen);
                        video.setArNum(arNum);
                        if(isDebug) System.out.println("\tAspect Ration Num: " + avparm.getAspectRatio());
                        video.setAspectRatio((float)avparm.getAspectRatio());
                        if(isDebug) System.out.println("\tFramerate: " + avstream.getFramerate().getValue());
                        video.setFps((float)avstream.getFramerate().getValue());
                        if(isDebug) System.out.println("\tWidth: " + avparm.getWidth());
                        video.setWidth(avparm.getWidth());
                        if(isDebug) System.out.println("\tHeight: " + avparm.getHeight());
                        video.setHeight(avparm.getHeight());
                        if(isDebug) System.out.println("\tInterlaced: " + avparm.getFieldOrder().isInterlaced());
                        video.setInterlaced(avparm.getFieldOrder().isInterlaced());
                        if(isDebug) System.out.println("\tSetID: " + avstream.getIDHex());
                        video.setId(avstream.getIDHex());
                        video.setOrderIndex(i);

                        //TODO: Add colorspace

                        streams.add(video);
                     }
                 }
                 else if(avparm.getCodecType() == AVMediaType.AUDIO)
                 {
                  
                     //Work around a weird flac issue where it is counting the wrong number of streams
                     if(avcodec.getName().equalsIgnoreCase("flac") && avparm.getChannels() == 0 && i > 0)
                     {
                        if(isDebug) System.out.println("Ignoring flac audio track with no channels and index > 0 (" + i + ")");
                     }
                     else
                     {
                        if(isDebug) System.out.println("Processing Audio (" + i + ")");
                        AudioFormat audio = new AudioFormat();

                        if(isDebug) System.out.println("\tAudio Codec: " + avcodec.getName());
                        if(isDebug) System.out.println("\tAudio Codec (Substitution): " + substitueCodec(avcodec.getName()));
                        audio.setFormatName(substitueCodec(avcodec.getName()));
                        //audio.setAudioTransport(); TODO: See if I can find this 
                        if(isDebug) System.out.println("\tChannels: " + avparm.getChannels());
                        audio.setChannels(avparm.getChannels());
                        if(isDebug) System.out.println("\tSample Rats: " + avparm.getSampleRate());
                        audio.setSamplingRate(avparm.getSampleRate());
                        if(isDebug) System.out.println("\tBitrate: " + avparm.getBitrate());
                        audio.setBitrate((int)avparm.getBitrate());
                        if(isDebug) System.out.println("\tLanguage: " + avstream.getLanguage());
                        audio.setLanguage(avstream.getLanguage());
                        audio.setOrderIndex(i);
                        if(isDebug) System.out.println("\tSetIDHex: " + avstream.getIDHex());
                        audio.setId(avstream.getIDHex());

                        streams.add(audio);
                        }
                 }
                 else if(avparm.getCodecType() == AVMediaType.SUBTITLE)
                 {
                     if(isDebug) System.out.println("Processing Subtitle (" + i + ")");
                     SubpictureFormat subpicture = new SubpictureFormat();

                     if(isDebug) System.out.println("\tCodec: " + avcodec.getName());
                     if(isDebug) System.out.println("\tCodec (Substitution): " + substitueCodec(avcodec.getName()));
                     subpicture.setFormatName(substitueCodec(avcodec.getName()));
                     if(isDebug) System.out.println("\tLanguage: " + avstream.getLanguage());
                     subpicture.setLanguage(avstream.getLanguage());
                     if(isDebug) System.out.println("\tForced: " + avstream.isForced());
                     subpicture.setForced(avstream.isForced());

                     subpicture.setOrderIndex(i);
                     if(isDebug) System.out.println("\tSetIDHex: " + avstream.getIDHex());
                     subpicture.setId(avstream.getIDHex());
                     
                     streams.add(subpicture);
                 }
                 else if(avparm.getCodecType() == AVMediaType.DATA) { if(isDebug) System.out.println("Processing Data (" + i + ")"); }
                 else if(avparm.getCodecType() == AVMediaType.ATTACHMENT) { if(isDebug) System.out.println("Processing Attachment (" + i + ")"); }
                 else if(avparm.getCodecType() == AVMediaType.NB) { if(isDebug) System.out.println("Processing NB (" + i + ")"); }
                 else { /* Unknown */ if(isDebug) System.out.println("Processing Unknown (" + i + ")"); }
            }
           
            if(avformat.getMetadataCount() > 0)
            {
                if(isDebug) System.out.println("MediaFormatParserPlugin - Adding the container metadata");
                
                HashMap<String, String> metadata = avformat.getMetadata();
                
                for(String key : metadata.keySet())
                {
                    if(isDebug) System.out.println("\tKey: " + key + " Value: " + metadata.get(key));
                }
                
                format.addMetadata(metadata);
            }
            
            format.setStreamFormats((BitstreamFormat[])streams.toArray(new BitstreamFormat[0]));
        }
        catch(Throwable ex)
        {
            System.out.println("There was an unhandled exception processing the file: " + file.getName() + " " + ex.getMessage());
            
             ex.printStackTrace(System.out);
                
            //System.out.println(ex.getStackTrace().toString());
        }
        finally
        {
            if(avformat != null)
            {
                avformat.close();
            }
        }
        
        System.out.println("MediaFormatParserPlugin processing complete: " + file.getAbsolutePath());
        
        return format;
    }

    @Override
    public void start()
    {
        //Set the property string
        Object ret;
        try
        {
            SageTV.api("SetServerProperty", new Object [] {"mediafile_mediaformat_parser_plugin", "jvl.mediaformat.MediaFormatParserPlugin"});
        } 
        catch (InvocationTargetException ex)
        {
            System.out.println("Error setting server property for MediaFormatParserPlugin");
        }
    }

    @Override
    public void stop(){}

    @Override
    public void destroy() {}

    @Override
    public String[] getConfigSettings()
    {
        System.out.println("getConfigSetting");
        return new String[]{"Debug", "DisableInternalFormatDetector", "DisableParserPlugin"};
    }

    @Override
    public String getConfigValue(String config)
    { 
        String value = "";

        if(config.equals("Debug"))
        {
            value = this.getProperty("jvl.MediaFormatParserPlugin.Debug", "false");
        }
        else if(config.equals("DisableInternalFormatDetector"))
        {
            value = this.getProperty("skip_internal_format_parser", "false");
        }
        else if(config.equals("DisableParserPlugin"))
        {
            value = this.getProperty("jvl.MediaFormatParserPlugin.Disable", "false");
        }
        else
        {
            System.out.println(" getConfigValue: NO MATCHING CONFIG)");
        }
 
        return value;
    }

    @Override
    public String[] getConfigValues(String config)
    {
        return null;
    }

    @Override
    public int getConfigType(String config)
    {   
        if(config.equals("Debug"))
        {
            return SageTVPlugin.CONFIG_BOOL;
        }
        else if(config.equals("DisableInternalFormatDetector"))
        {
            return SageTVPlugin.CONFIG_BOOL;
        }
        else if(config.equals("DisableParserPlugin"))
        {
            return SageTVPlugin.CONFIG_BOOL;
        }
        else
        {
            return SageTVPlugin.CONFIG_TEXT;
        }
    }

    @Override
    public void setConfigValue(String config, String value)
    {
        if(config.equals("Debug"))
        {
            this.setProperty("jvl.MediaFormatParserPlugin.Debug", value);
        }
        else if(config.equals("DisableInternalFormatDetector"))
        {
            this.setProperty("skip_internal_format_parser", value);
        }
        else if(config.equals("DisableParserPlugin"))
        {
            this.setProperty("jvl.MediaFormatParserPlugin.Disable", value);
        }
        else
        {
            System.out.println(" setConfigValue: NO MATCHING CONFIG)");
        }
        
    }

    @Override
    public void setConfigValues(String config, String[] strings) { }

    @Override
    public String[] getConfigOptions(String config){ return null; }

    @Override
    public String getConfigHelpText(String config)
    {
        if(config.equals("Debug"))
        {
            return "Add verbose logging to the SageTV debug log for.";
        }
        else if(config.equals("DisableInternalFormatDetector"))
        {
            return "Disables  the internal format detector that processes .ps/.ts files.  This will allow this plugin to be able to process these file types.";
        }
        else if(config.equals("DisableParserPlugin"))
        {
            return "Disable the parser plugin and allow SageTV external parser to parse the files";
        }
        else
        {
            return "";
        }
    }

    @Override
    public String getConfigLabel(String config)
    {
        if(config.equals("Debug"))
        {
            return "Debug logging";
        }
        else if(config.equals("DisableInternalFormatDetector"))
        {
            return "Disable internal format detector";
        }
        else if(config.equals("DisableParserPlugin"))
        {
            return "Disable parser plugin";
        }
        else
        {
            return "";
        }
    }

    @Override
    public void resetConfig()
    {
    
    }

    @Override
    public void sageEvent(String string, Map map){}

    
    
    private void setProperty(String property, String value)
    {
        Object ret;
        
        try
        {
            SageTV.api("SetServerProperty", new Object [] {property, value});
        } 
        catch (InvocationTargetException ex)
        {
            System.out.println("Error setting server property for MediaFormatParserPlugin property");
        }
    }
    
    private String getProperty(String property, String defaultvalue)
    {
        Object ret = null;
        
        try
        {
            ret = SageTV.api("GetServerProperty", new Object [] {property, defaultvalue});
        } 
        catch (InvocationTargetException ex)
        {
            System.out.println("Error setting server property for MediaFormatParserPlugin property");
        }
        
        return (String)ret;
    }
    
    
    
}
