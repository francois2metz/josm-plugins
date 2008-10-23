#!/bin/bash

# Creates an josm-setup-xy.exe File
#
# for working on a debian-unix system install the nsis package with
# apt-get install nsis
# replace the  /usr/share/nsis/Plugins/System.dll with the Version from the nsis .zip File
# The one comming with the debian package is missing the Call:: Function
# See also /usr/share/doc/nsis/README.Debian 
#
# Then download launch4j from http://launch4j.sourceforge.net/ 
# wget http://mesh.dl.sourceforge.net/sourceforge/launch4j/launch4j-3.0.0-pre2-linux.tgz
# and unpack it to /usr/share/launch4j

## settings ##
LAUNCH4J="java -jar /usr/share/launch4j/launch4j.jar"

svncorerevision=`svnversion ../core`
svnpluginsrevision=`svnversion ../plugins`
svnrevision="$svncorerevision-$svnpluginsrevision"

#export VERSION=latest
export VERSION=custom-${svnrevision}                                         

echo "Creating Windows Installer for josm-$VERSION"

##################################################################
### Build the Complete josm + Plugin Stuff
if true; then
    (
	echo "Build the Complete josm Stuff"
	
	echo "Compile Josm"
	cd ../core
	ant -q clean
	ant -q compile || exit -1
	cd ..
	
	echo "Compile Josm Plugins"
	cd plugins
	ant -q clean
	ant -q dist || exit -1
	) || exit -1
fi

echo 
echo "##################################################################"
echo "### convert jar to exe with launch4j"
# (an exe file makes attaching to file extensions a lot easier)
# launch4j - http://launch4j.sourceforge.net/
# delete old exe file first
rm -f josm.exe
$LAUNCH4J ./launch4j.xml

if ! [ -s josm.exe ]; then
    echo "NO Josm File Created"
    exit -1
fi

echo 
echo "##################################################################"
echo "### create the installer exe with makensis"
# NSIS - http://nsis.sourceforge.net/Main_Page
# apt-get install nsis
makensis -V2 -DVERSION=$VERSION josm.nsi

# delete the intermediate file, just to avoid confusion
rm josm-intermediate.exe
mv josm.exe josm-intermediate.exe
