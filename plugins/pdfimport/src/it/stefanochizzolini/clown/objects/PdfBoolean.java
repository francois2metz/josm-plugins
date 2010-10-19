/*
  Copyright 2006-2009 Stefano Chizzolini. http://clown.stefanochizzolini.it

  Contributors:
    * Stefano Chizzolini (original code developer, http://www.stefanochizzolini.it)

  This file should be part of the source code distribution of "PDF Clown library"
  (the Program): see the accompanying README files for more info.

  This Program is free software; you can redistribute it and/or modify it under the terms
  of the GNU Lesser General Public License as published by the Free Software Foundation;
  either version 3 of the License, or (at your option) any later version.

  This Program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY,
  either expressed or implied; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the License for more details.

  You should have received a copy of the GNU Lesser General Public License along with this
  Program (see README files); if not, go to the GNU website (http://www.gnu.org/licenses/).

  Redistribution and use, with or without modification, are permitted provided that such
  redistributions retain the above copyright notice, license and disclaimer, along with
  this list of conditions.
*/

package it.stefanochizzolini.clown.objects;

import it.stefanochizzolini.clown.bytes.IOutputStream;
import it.stefanochizzolini.clown.files.File;
import it.stefanochizzolini.clown.util.NotImplementedException;

/**
  PDF boolean object [PDF:1.6:3.2.1].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.8
*/
public class PdfBoolean
  extends PdfAtomicObject<Boolean>
{
  // <class>
  // <static>
  // <fields>
  public static final PdfBoolean False = new PdfBoolean(false);
  public static final PdfBoolean True = new PdfBoolean(true);
  // </fields>
  // </static>

  // <dynamic>
  // <constructors>
  public PdfBoolean(
    )
  {}

  public PdfBoolean(
    boolean value
    )
  {setRawValue(value);}
  // </constructors>

  // <interface>
  // <public>
  @Override
  public Object clone(
    File context
    )
  {
    // Shallow copy.
    PdfBoolean clone = (PdfBoolean)super.clone();

    // Deep copy.
    /* NOTE: No mutable object to be cloned. */

    return clone;
  }

  @Override
  public int compareTo(
    PdfDirectObject obj
    )
  {throw new NotImplementedException();}

  @Override
  public void writeTo(
    IOutputStream stream
    )
  {stream.write(toPdf(getRawValue()));}
  // </public>

  // <private>
  private String toPdf(
    boolean value
    )
  {return value ? "true" : "false";}
  // </private>
  // </interface>
  // </dynamic>
  // </class>
}