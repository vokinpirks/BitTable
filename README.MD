## Contents
- [What's this?](#whats-this)
- [Dependencies](#dependencies)
- [Installation](#installation)
- [Settings](#settings)
  - [Global Settings](#global-settings)
  - [Project-Level Settings](#project-level-settings)
- [Usage](#usage)
- [Examples](#examples)

***
## What's this?
**BitTable** is an extension for Bitwig Studio which acts as a GUI for a wonderful command-line utility called [okwt](https://github.com/drzhnn/okwt). It integrates okwt functionality into Bitwig allowing a user to convert audio files and non-audio files to wavetables without leaving their favourite DAW. Check [Examples](#examples) out to see it in action.

***
## Dependencies
- [okwt](https://github.com/drzhnn/okwt) (required)
- [ffmpeg](https://github.com/FFmpeg/FFmpeg) (optional, only needed if you want to convert audio files of formats other than WAW, namely _AIFF, MP3, FLAC)_.

***
## Installation
- Download [latest release](https://github.com/vokinpirks/BitTable/releases/latest)
- [Install](https://www.bitwig.com/support/technical_support/how-do-i-add-a-controller-extension-or-script-17/) the bwextension file you've just downloaded as a Bitwig Studio extension
- Activate the extension. Settings -> Controllers -> Add Controller -> select Hardware Vendor 'Utilities' -> BitTable -> Add 

***
## Settings
### Global Settings
Settings -> Controllers -> BitTable -> Settings 

- **Paths**
  - _okwt_
    
    The folder where the okwt executable is located. 

    <sub><sup>To find out where okwt is installed, type `which okwt` in the terminal/cmd.<sub><sup>
  
    <sub><sup>In Windows there is no need to specify this setting if okwt is accessible from everywhere (i.e. added to the global PATH environment variable). </sup></sub>

  - _ffmpeg_ (optional) 
    
    The folder where the ffmpeg executables are located. Fortunately, ffmpeg is already included with Bitwig Studio so normally you don't have to install ffmpeg yourself. Just specify the path to the folder where the Bitwig Studio executable files are located. In Windows, it can be something like '_C:\Program Files\Bitwig Studio\5.0.5\bin_'. You can use the placeholder `${bws.version}` instead of the version part, i.e. '_C:\Program Files\Bitwig Studio\${bws.version}\bin_'. When the extension performs conversion, the placeholder is replaced with the actual version of Bitwig Studio in which the extension is running, so if you install the newer version of BWS and delete the older one, the path you've set still works regardless of BWS version you currently use. If you don't want to use the ffmpeg package that's shipped with BWS by some unexplainable reason, you can install ffmpeg on your own and set the path accordingly.
  
- **Save Locations**
    - _WT Files_

      Where resulting .wt files will be saved. 

      <sub><sup>Tip: create a dedicated folder in User Library/Wavetables, i.e. User Library/Wavetables/okwt. This way converted files will be accessible in the wavetable browser.</sub></sup> 
    
    - _WAV files_
  
      Where resulting .wav files will be saved.
  
      <sub><sup>Tip: create a dedicated folder in User Library/Samples.<sub><sup>
  
- **Sample locations**

    To convert a sample from Sampler or the popup browser the extension needs to 'know' a full path to the sample in order to pass it to okwt as a parameter. Bitwig doesn't provide such information to extensions through the API, only filenames. But if you add folders that contains samples to one of the Sample Locations fields, BitTable will scan them recursively and build 'sample name' -> 'file path' mapping internally. Ideally, any sample that is accessible through the Bitwig browser should be visible to BitTable too. Good candidates to add here are as follows:
  - User Library (Settings -> Locations -> My Library, or, in the browser, Locations -> My Library -> right click -> Show in Explorer/Finder -> copy path)
  - installed-packages (Settings -> Locations -> Downloaded Packages, or, in the browser, Locations -> Packages -> right click -> Show in Explorer/Finder -> copy path)
  - All your Sound Content locations
  - your Projects folder (this is needed for example if you want to convert bounce audio files)
  - Temporary projects folder (for the same reason)
  
- **Force Reindex**

  If, by some reason, BitTable wasn't able to determine the full path to a sample when it should do, push this button and try again.

- **Misc**
  - _Debug_
    
    Prints additional information in the console window for debugging purposes
  
  - _Append date and time_
    
    Each generated file will have a date and time suffix in its name. Useful if you convert the same sample multiple times with different settings so the resulting file won't be overwritten with each conversion.

***
### Project-level Settings
Are accessible in the controller pane.

- _Resize algorithm_
- _Normalize_ 
- _Maximize_ 
- _Fade_ 
- _Trim_ 
- _Trim Threshold_
- _Shuffle_
- _Shuffle groups_

  These are wavetable conversion parameters that are passed to okwt as-is. Check their descriptions in the [okwt manual](https://github.com/drzhnn/okwt#usage). 

 - **File**

    Full path to a sample you want to convert (only takes effect when the popup-browser is inactive and Sampler is not selected). 

 - **Save as** 

    Specify output file format, it can be either *WAV* or *WT*. It only takes effect when you click the **Save** button.

***
### Usage
There are a few options what you can do with resulting wavetables (corresponding buttons are available in the extension's controller pane):

- **Save**

  Just converts a sample and saves the resulting wavetable file in a folder as specified in Global Settings -> Save Locations.

- **Load new Sampler**

  Converts a sample, saves the wavetable as a WAW file and loads it into a new Sampler instance in the currently selected track. Why would one load a WT file into the Sampler, you may ask? This can be useful if you already have an existing Polymer/Grid instance set up into which you'd like to load the new wavetable. When conversion is done, just drag the sample from Sampler and drop it into the WT osc of your existing Polymer/Grid.

- **Load new Polymer**

  Converts a sample, saves the wavetable as a WT file and loads it into new Polymer instance in the currently selected track.

There are also several ways how a source file can be chosen:
- from **Popup browser**
  
  Select a sample in the Popup Browser, then click any of the **Convert and**  buttons.

- from **Sampler**
  
  Select a Sampler instance with a sample loaded in it, then click any of the **Convert and** buttons.

- **Arbitrary file** from the file system

  If you want to convert an external file, i.e. an image that you have found in your file manager, copy its full path, paste that into the **File** field and click any of the **Convert and** buttons.

***
### Examples

https://user-images.githubusercontent.com/17597433/267144052-fee305ff-093e-4844-b3f4-d43f6c533ba3.mp4

https://user-images.githubusercontent.com/17597433/267144097-154485dd-8525-4b9a-b49c-892f928d38db.mp4

https://user-images.githubusercontent.com/17597433/267144115-8a6aebf0-787c-497c-b4ff-d82412caacb9.mp4


