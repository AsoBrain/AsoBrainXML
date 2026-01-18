/*
 * AsoBrain XML Library
 * Copyright (C) 1999-2026 Peter S. Heijnen
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

import java.io.*;

import lombok.*;
import org.jspecify.annotations.*;
import org.xmlpull.v1.*;

/**
 * XML reader implementation that uses XML Pull.
 *
 * @author Gerrit Meinders
 */
@RequiredArgsConstructor
class XmlPullReader
	implements XMLReader
{
	/**
	 * XML Pull parser to be used.
	 */
	private final XmlPullParser parser;

	/**
	 * Event type returned by the last call to {@link #next()}.
	 */
	private XMLEventType eventType = XMLEventType.START_DOCUMENT;

	/**
	 * Target of the current processing instruction, if any.
	 */
	private @Nullable String piTarget = null;

	/**
	 * Data of the current processing instruction, if any.
	 */
	private @Nullable String piData = null;

	/**
	 * Text content of the current character data event. If set, the state of
	 * the underlying parser should be ignored and a character data event with
	 * the content of this string should be reported instead.
	 */
	private @Nullable String characterData = null;

	/**
	 * Used to coalesce consecutive character data that is returned as separate
	 * events by {@link XmlPullParser#nextToken()}.
	 */
	@SuppressWarnings( "StringBufferField" )
	private final StringBuilder characterDataBuilder = new StringBuilder();

	@Override
	public XMLEventType getEventType()
	{
		return eventType;
	}

	@Override
	public XMLEventType next()
		throws XMLException
	{
		if ( eventType == XMLEventType.END_DOCUMENT )
		{
			throw new IllegalStateException( "Not allowed after %s event.".formatted( XMLEventType.END_DOCUMENT ) );
		}

		XMLEventType result;
		do
		{
			int token;
			if ( characterData == null )
			{
				token = parseNextToken();
			}
			else
			{
				token = getParsedEventType();
				characterData = null;
			}

			result = switch ( token )
			{
				case XmlPullParser.START_DOCUMENT -> XMLEventType.START_DOCUMENT;
				case XmlPullParser.END_DOCUMENT -> XMLEventType.END_DOCUMENT;
				case XmlPullParser.START_TAG -> XMLEventType.START_ELEMENT;
				case XmlPullParser.END_TAG -> XMLEventType.END_ELEMENT;
				case XmlPullParser.TEXT,
				     XmlPullParser.CDSECT,
				     XmlPullParser.ENTITY_REF -> XMLEventType.CHARACTERS;
				case XmlPullParser.IGNORABLE_WHITESPACE, XmlPullParser.COMMENT -> null;
				case XmlPullParser.PROCESSING_INSTRUCTION -> XMLEventType.PROCESSING_INSTRUCTION;
				case XmlPullParser.DOCDECL -> XMLEventType.DTD;
				default -> throw new XMLException( "Unknown token: " + token );
			};
		}
		while ( result == null );

		eventType = result;
		updateProcessingInstructionFields();

		if ( result == XMLEventType.CHARACTERS )
		{
			coalesceCharacterData();
		}

		return result;
	}

	private int parseNextToken()
		throws XMLException
	{
		try
		{
			return parser.nextToken();
		}
		catch ( IOException | XmlPullParserException e )
		{
			throw new XMLException( e );
		}
	}

	private int getParsedEventType()
		throws XMLException
	{
		try
		{
			return parser.getEventType();
		}
		catch ( XmlPullParserException e )
		{
			throw new XMLException( e );
		}
	}

	/**
	 * Updates the {@link #piTarget} and {@link #piData} fields. Must be
	 * called after {@link #eventType} is updated.
	 */
	private void updateProcessingInstructionFields()
	{
		if ( eventType != XMLEventType.PROCESSING_INSTRUCTION )
		{
			piTarget = null;
			piData = null;
			return;
		}

		var text = parser.getText();
		var length = text.length();

		var targetEnd = 1;
		while ( targetEnd < length )
		{
			var c = text.charAt( targetEnd );
			if ( c == ' ' || c == '\t' || c == '\r' || c == '\n' )
			{
				break;
			}
			targetEnd++;
		}

		if ( targetEnd == length )
		{
			piTarget = text;
			piData = "";
			return;
		}

		var dataStart = targetEnd + 1;
		while ( dataStart < length )
		{
			var c = text.charAt( dataStart );
			if ( c != ' ' && c != '\t' && c != '\r' && c != '\n' )
			{
				break;
			}
			dataStart++;
		}

		piTarget = text.substring( 0, targetEnd );
		piData = text.substring( dataStart );
	}

	/**
	 * Combines the current character data event and any following character
	 * data events into a single event.
	 *
	 * @throws XMLException if an XML-related exception occurs.
	 */
	private void coalesceCharacterData()
		throws XMLException
	{
		var builder = characterDataBuilder;
		builder.append( parser.getText() );
		while ( true )
		{
			var token = parseNextToken();
			if ( token == XmlPullParser.TEXT ||
			     token == XmlPullParser.CDSECT ||
			     token == XmlPullParser.ENTITY_REF )
			{
				builder.append( parser.getText() );
			}
			else
			{
				break;
			}
		}

		characterData = builder.toString();
		characterDataBuilder.setLength( 0 );
	}

	@Override
	public String getNamespaceURI()
	{
		if ( eventType != XMLEventType.START_ELEMENT &&
		     eventType != XMLEventType.END_ELEMENT )
		{
			throw new IllegalStateException( "Not allowed for " + eventType );
		}

		return parser.getNamespace( parser.getPrefix() );
	}

	@Override
	public String getLocalName()
	{
		if ( eventType != XMLEventType.START_ELEMENT &&
		     eventType != XMLEventType.END_ELEMENT )
		{
			throw new IllegalStateException( "Not allowed for " + eventType );
		}

		return parser.getName();
	}

	@Override
	public int getAttributeCount()
	{
		if ( eventType != XMLEventType.START_ELEMENT )
		{
			throw new IllegalStateException( "Not allowed for " + eventType );
		}

		return parser.getAttributeCount();
	}

	@Override
	public String getAttributeNamespaceURI( int index )
	{
		if ( eventType != XMLEventType.START_ELEMENT )
		{
			throw new IllegalStateException( "Not allowed for " + eventType );
		}

		if ( index < 0 || index >= getAttributeCount() )
		{
			throw new IndexOutOfBoundsException( index + " (attributeCount: " + getAttributeCount() + ')' );
		}

		return parser.getAttributePrefix( index ) == null ? null : parser.getAttributeNamespace( index );
	}

	@Override
	public String getAttributeLocalName( int index )
	{
		if ( eventType != XMLEventType.START_ELEMENT )
		{
			throw new IllegalStateException( "Not allowed for " + eventType );
		}

		if ( index < 0 || index >= getAttributeCount() )
		{
			throw new IndexOutOfBoundsException( index + " (attributeCount: " + getAttributeCount() + ')' );
		}

		return parser.getAttributeName( index );
	}

	@Override
	public String getAttributeValue( int index )
	{
		if ( eventType != XMLEventType.START_ELEMENT )
		{
			throw new IllegalStateException( "Not allowed for " + eventType );
		}

		if ( index < 0 || index >= getAttributeCount() )
		{
			throw new IndexOutOfBoundsException( index + " (attributeCount: " + getAttributeCount() + ')' );
		}

		return parser.getAttributeValue( index );
	}

	@Override
	public String getAttributeValue( String localName )
	{
		if ( eventType != XMLEventType.START_ELEMENT )
		{
			throw new IllegalStateException( "Not allowed for " + eventType );
		}

		var attributeCount = parser.getAttributeCount();
		for ( var i = 0; i < attributeCount; i++ )
		{
			if ( localName.equals( parser.getAttributeName( i ) ) )
			{
				return parser.getAttributeValue( i );
			}
		}

		return null;
	}

	@Override
	public String getAttributeValue( String namespaceURI, String localName )
	{
		if ( eventType != XMLEventType.START_ELEMENT )
		{
			throw new IllegalStateException( "Not allowed for " + eventType );
		}

		return parser.getAttributeValue( namespaceURI, localName );
	}

	@Override
	public String getText()
	{
		if ( eventType != XMLEventType.CHARACTERS )
		{
			throw new IllegalStateException( "Not allowed for " + eventType );
		}

		return characterData != null ? characterData : parser.getText();
	}

	@Override
	public String getPITarget()
	{
		if ( eventType != XMLEventType.PROCESSING_INSTRUCTION )
		{
			throw new IllegalStateException( "Not allowed for " + eventType );
		}

		// noinspection ConstantConditions
		return piTarget;
	}

	@Override
	public String getPIData()
	{
		if ( eventType != XMLEventType.PROCESSING_INSTRUCTION )
		{
			throw new IllegalStateException( "Not allowed for " + eventType );
		}

		// noinspection ConstantConditions
		return piData;
	}

	@Override
	public int getLineNumber()
	{
		return parser.getLineNumber();
	}

	@Override
	public int getColumnNumber()
	{
		return parser.getColumnNumber();
	}
}
