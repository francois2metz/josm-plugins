#! /usr/bin/perl -w

# Written by Dirk Stöcker <openstreetmap@dstoecker.de>
# Public domain, no rights reserved.

use strict;

my $item;
my $comment = 0;

# This is a simple conversion and in no way a complete XML parser
# but it works with a default Perl installation

# Print a header to write valid Java code.  No line break,
# so that the input and output line numbers will match.
print "class trans_surveyor { void tr(String s){} void f() {";

while(my $line = <>)
{
  chomp($line);
  print "tr(\"---DUMMY-MARKER---\"); ";
  if($line =~ /^#(.*)$/)
  {
    print "/* $1 */\n";
  }
  elsif($line =~ /^$/)
  {
    print "\n";
  }
  elsif($line =~ /<button label=\"(.*?)\"/)
  {
    print "/* $line*/ tr(\"$1\");\n";
  }
  else
  {
    print "/* $line */\n";
  }
}

print "}}\n";
