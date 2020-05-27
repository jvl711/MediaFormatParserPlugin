
package jvl.mediaformat;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jvl.FFmpeg.jni.AVCodec;
import jvl.FFmpeg.jni.AVCodecParameters;
import jvl.FFmpeg.jni.AVFormatContext;
import jvl.FFmpeg.jni.AVMediaType;
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
    public MediaFormatParserPlugin(sage.SageTVPluginRegistry stpr, boolean reset)
    {
        
    }

    @Override
    public ContainerFormat parseFormat(File file)
    {
        ContainerFormat format = new ContainerFormat();
        AVFormatContext avformat = null;
        ArrayList<BitstreamFormat> streams = new ArrayList<BitstreamFormat>();
        
        try
        {
            System.out.println("MediaFormatParserPlugin processing: " + file.getAbsolutePath());
            avformat = AVFormatContext.buildAVFormatContext();
            avformat.openInput(file.getAbsolutePath());
        
            format.setFormatName(FormatParser.substituteName(avformat.getFormatName()));
            format.setDuration(avformat.getDuration() / 1000);
            format.setBitrate((int)avformat.getBitrate());
           
            for(int i = 0; i < avformat.getNumberOfStreams(); i++)
            {
                 AVCodecParameters avparm = avformat.getAVCodecParameters(i);
                 AVCodec avcodec = AVCodec.getAVCodec(avparm);
                 AVStream avstream = avformat.getAVStream(i);

                 if(avparm.getCodecType() == AVMediaType.VIDEO)
                 {
                     int arDen =0;
                     int arNum =0;

                     VideoFormat video = new VideoFormat();
                     video.setFormatName(FormatParser.substituteName(avcodec.getName()));

                     if(avparm.getAspectRatioString().length() > 0)
                     {
                         try
                         {
                             arNum = Integer.parseInt(avparm.getAspectRatioString().split(":")[0]);
                             arDen = Integer.parseInt(avparm.getAspectRatioString().split(":")[1]);
                         }
                         catch(Exception ex){}
                     }

                     video.setArDen(arDen);
                     video.setArNum(arNum);
                     video.setAspectRatio((float)avparm.getAspectRatio());
                     video.setFps((float)avstream.getFramerate());
                     video.setWidth(avparm.getWidth());
                     video.setHeight(avparm.getHeight());
                     video.setInterlaced(avparm.getFieldOrder().isInterlaced());
                     video.setOrderIndex(i);
                     //TODO: Add colorspace

                     streams.add(video);
                 }
                 else if(avparm.getCodecType() == AVMediaType.AUDIO)
                 {
                     
                     AudioFormat audio = new AudioFormat();

                     /*
                      * If the first stream is audio lets not process for now.
                      * Need to properly handle metadata and audio files that
                      * include video tracks.
                      */
                     if(i == 0)
                     {
                         return null;
                     }
                     
                     audio.setFormatName(FormatParser.substituteName(avcodec.getName()));
                     //audio.setAudioTransport(); TODO: See if I can find this 
                     audio.setChannels(avparm.getChannels());
                     audio.setSamplingRate(avparm.getSampleRate());
                     audio.setBitrate((int)avparm.getBitrate());
                     audio.setLanguage(avstream.getLanguage());
                     audio.setOrderIndex(i);

                     streams.add(audio);
                 }
                 else if(avparm.getCodecType() == AVMediaType.SUBTITLE)
                 {
                     SubpictureFormat subpicture = new SubpictureFormat();

                     subpicture.setFormatName(FormatParser.substituteName(avcodec.getName()));
                     subpicture.setLanguage(avstream.getLanguage());

                     subpicture.setOrderIndex(i);
                     subpicture.setForced(avstream.isForced());
                     streams.add(subpicture);
                 }
                 else if(avparm.getCodecType() == AVMediaType.DATA) { }
                 else if(avparm.getCodecType() == AVMediaType.ATTACHMENT) { }
                 else if(avparm.getCodecType() == AVMediaType.NB) { }
                 else { /* Unknown */ }
            }
           
            format.setStreamFormats((BitstreamFormat[])streams.toArray(new BitstreamFormat[0]));
        }
        catch(Throwable ex)
        {
            System.out.println("There was an unhandled exception processing the file: " + file.getName() + " " + ex.getMessage());
            ex.printStackTrace();
        }
        finally
        {
            if(avformat != null)
            {
                avformat.closeInput();
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
    public String[] getConfigSettings(){return null;}

    @Override
    public String getConfigValue(String string){ return null; }

    @Override
    public String[] getConfigValues(String string){return null;}

    @Override
    public int getConfigType(String string){return 1;}

    @Override
    public void setConfigValue(String string, String string1){}

    @Override
    public void setConfigValues(String string, String[] strings){}

    @Override
    public String[] getConfigOptions(String string){return null;}

    @Override
    public String getConfigHelpText(String string){return null;}

    @Override
    public String getConfigLabel(String string){return null;}

    @Override
    public void resetConfig(){}

    @Override
    public void sageEvent(String string, Map map){}
    
}
