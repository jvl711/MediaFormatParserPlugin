# MediaFormatParserPlugin
This is a SageTV MediaFormatParser plugin that utilizes FFmpeg

## Installation Instructions

*Note: As of right now this is only available for x64 windows installs of SageTV*

- Shutdown sagetv service
- Backup sagetv folder
- Download the pre-release version of the Sage.jar and place it into the root of the SageTV folder
  - [sage.jar alpha build](https://github.com/jvl711/MediaFormatParserPlugin/releases/download/v.0.1-alpha/Sage.jar)
  - *Note: There is new functionallity that is required for this plugin to work.  Upgrading the jar is required.*
- Download the latest version of the JavaFFmpegLibrary
  - https://github.com/jvl711/JavaFFmpegLibrary/releases
  - Unzip the release and place all of the .dll files in the root of the SageTV folder
  - Place all .jar files in the JAR folder that is in the root of the SageTV folder
- Download the latest version MediaFormatParserPlugin
  - https://github.com/jvl711/MediaFormatParserPlugin/releases
  - download MediaFormatParserPlugin.jar file into the JAR folder in the root of the SageTV folder
- Configure plugin in sage.properties
  - add line mediafile_mediaformat_parser_plugin=jvl.mediaformat.MediaFormatParserPlugin
- **Optional:** you can redetect the format of all of your media files by changing this setting in Sage.properties
  - force_full_content_reindex=true
- Turn sagetv service back on
  
## Disabling Instructions
- Shutdown sagetv service
- Modify sage.properties
  - remove line mediafile_mediaformat_parser_plugin=jvl.mediaformat.MediaFormatParserPlugin
- Startup sagetv service
  
## FAQ
Q: Does this allow for forced subtitles to be auto selected by SageTV

A: Yes. With the changes that were applied to SageTV (Not yet in a release build), and the format detector plugin that I wrote, forced subtitles properly marked will auto select

Q: What is the reason for the media format plugin.

A: The external format detector in SageTV is built on a pretty old version of FFmpeg. I initial wrote this plugin to make forced subtitles work, and quickly realized that it would also allow for detection of newer formats like HEVC. There are a number of additional codecs/containers that sage does not recognize now that this may. It also appears to be quicker at identifying.
