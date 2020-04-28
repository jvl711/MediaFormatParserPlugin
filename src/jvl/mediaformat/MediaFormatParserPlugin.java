
package jvl.mediaformat;

import java.io.File;
import java.util.ArrayList;
import jvl.FFmpeg.jni.AVCodec;
import jvl.FFmpeg.jni.AVCodecParameters;
import jvl.FFmpeg.jni.AVFormatContext;
import jvl.FFmpeg.jni.AVMediaType;
import jvl.FFmpeg.jni.AVStream;
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
public class MediaFormatParserPlugin implements sage.media.format.FormatParserPlugin
{

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
                     //subpicture.setForced(avstream.isForced()); TODO: Add back when I have the right Sage.jar version
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
    
}
