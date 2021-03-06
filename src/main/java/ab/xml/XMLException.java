/*
 * AsoBrain XML Library
 * Copyright (C) 1999-2011 Peter S. Heijnen
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package ab.xml;

/**
 * Indicates that an XML-related operation cannot be completed.
 *
 * @author G. Meinders
 */
public class XMLException
extends Exception
{
	/** Serialized data version. */
	private static final long serialVersionUID = 2576150199808703846L;

	/**
	 * Constructs a new instance.
	 */
	public XMLException()
	{
	}

	/**
	 * Constructs a new instance.
	 *
	 * @param message Detail message.
	 */
	public XMLException( final String message )
	{
		super( message );
	}

	/**
	 * Constructs a new instance.
	 *
	 * @param message Detail message.
	 * @param cause   Cause of the exception.
	 */
	public XMLException( final String message, final Throwable cause )
	{
		super( message, cause );
	}

	/**
	 * Constructs a new instance.
	 *
	 * @param cause Cause of the exception.
	 */
	public XMLException( final Throwable cause )
	{
		super( cause.getMessage(), cause );
	}
}
