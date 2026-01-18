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

import java.math.*;
import java.util.*;
import javax.xml.datatype.*;

import lombok.*;

/**
 * Provides conversion of Java types to XML syntax. Replacement for {@code
 * javax.xml.bind.DatatypeConverter}, which is not available on all target
 * platforms.
 *
 * <p>This implementation uses the {@code javax.xml.datatype} package. On
 * Android, this requires at least API Level 8 (Android 2.2).
 *
 * @author G. Meinders
 */
@NoArgsConstructor( access = AccessLevel.PRIVATE )
@SuppressWarnings( "unused" )
public class DatatypeConverter
{
	/**
	 * Cached {@link DatatypeFactory} instance.
	 */
	private static DatatypeFactory datatypeFactory = null;

	/**
	 * Returns a {@link DatatypeFactory}.
	 *
	 * @return {@link DatatypeFactory} instance.
	 */
	private static DatatypeFactory getDatatypeFactory()
	{
		var result = datatypeFactory;
		if ( result != null )
		{
			return result;
		}

		try
		{
			result = DatatypeFactory.newInstance();
			datatypeFactory = result;
			return result;
		}
		catch ( DatatypeConfigurationException e )
		{
			throw new RuntimeException( e );
		}
	}

	/**
	 * Converts the given calendar's date and time into a valid lexical value
	 * for the XML Schema {@code dateTime} data type.
	 *
	 * @param calendar Calendar to be converted.
	 *
	 * @return String representation of the calendar's date and time.
	 */
	public static String printDateTime( Calendar calendar )
	{
		GregorianCalendar gregorianCalendar;
		if ( calendar instanceof GregorianCalendar existing )
		{
			gregorianCalendar = existing;
		}
		else
		{
			gregorianCalendar = new GregorianCalendar( calendar.getTimeZone() );
			gregorianCalendar.setTime( calendar.getTime() );
		}
		var datatypeFactory = getDatatypeFactory();
		var xmlGregorianCalendar = datatypeFactory.newXMLGregorianCalendar( gregorianCalendar );
		return xmlGregorianCalendar.toXMLFormat();
	}

	/**
	 * Converts the given data-time value to a calendar instance. The given
	 * value must be a valid lexical value of the XML Schema {@code dateTime}
	 * data type.
	 *
	 * @param value Value to be parsed.
	 *
	 * @return String representation of the calendar's date and time.
	 */
	@SuppressWarnings( "UnusedReturnValue" )
	public static Calendar parseDateTime( String value )
	{
		var datatypeFactory = getDatatypeFactory();
		var xmlGregorianCalendar = datatypeFactory.newXMLGregorianCalendar( value );
		return xmlGregorianCalendar.toGregorianCalendar();
	}

	/**
	 * Converts the given floating point value to a {@link float}. The given
	 * value must be a valid lexical value of the XML Schema {@code float}
	 * data type.
	 *
	 * @param value Value to be parsed.
	 *
	 * @return Parsed value.
	 *
	 * @throws NumberFormatException {@code value} is not properly formatted.
	 */
	public static float parseFloat( String value )
	{
		var trimmed = value.trim();
		return switch ( trimmed )
		{
			case "INF" -> Float.POSITIVE_INFINITY;
			case "-INF" -> Float.NEGATIVE_INFINITY;
			case "NaN" -> Float.NaN;
			default -> Float.parseFloat( trimmed );
		};
	}

	/**
	 * Converts the given floating point value to a {@link double}. The given
	 * value must be a valid lexical value of the XML Schema {@code double}
	 * data type.
	 *
	 * @param value Value to be parsed.
	 *
	 * @return Parsed value.
	 *
	 * @throws NumberFormatException {@code value} is not properly formatted.
	 */
	public static double parseDouble( String value )
	{
		var trimmed = value.trim();
		return switch ( trimmed )
		{
			case "INF" -> Double.POSITIVE_INFINITY;
			case "-INF" -> Double.NEGATIVE_INFINITY;
			case "NaN" -> Double.NaN;
			default -> Double.parseDouble( trimmed );
		};
	}

	/**
	 * Converts the given decimal value to a {@link BigDecimal} instance. The
	 * given value must be a valid lexical value of the XML Schema {@code
	 * decimal} data type.
	 *
	 * @param value Value to be parsed.
	 *
	 * @return {@link BigDecimal}.
	 *
	 * @throws NumberFormatException {@code value} is not properly formatted.
	 */
	public static BigDecimal parseDecimal( String value )
	{
		return new BigDecimal( value.trim() );
	}

	/**
	 * Converts the given value into a valid lexical value for the XML Schema
	 * {@code int} data type.
	 *
	 * @param v Value to be converted.
	 *
	 * @return String representation of the value.
	 */
	public static String printInt( int v )
	{
		return String.valueOf( v );
	}

	/**
	 * Converts the given value into a valid lexical value for the XML Schema
	 * {@code float} data type.
	 *
	 * @param v Value to be converted.
	 *
	 * @return String representation of the value.
	 */
	public static String printFloat( float v )
	{
		return ( v == Float.POSITIVE_INFINITY ) ? "INF" :
		       ( v == Float.NEGATIVE_INFINITY ) ? "-INF" :
		       Float.isNaN( v ) ? "NaN" :
		       String.valueOf( v );
	}

	/**
	 * Converts the given value into a valid lexical value for the XML Schema
	 * {@code double} data type.
	 *
	 * @param v Value to be converted.
	 *
	 * @return String representation of the value.
	 */
	public static String printDouble( double v )
	{
		return ( v == Double.POSITIVE_INFINITY ) ? "INF" :
		       ( v == Double.NEGATIVE_INFINITY ) ? "-INF" :
		       Double.isNaN( v ) ? "NaN" :
		       String.valueOf( v );
	}

	/**
	 * Converts a {@link BigDecimal} value the lexical value of the XML Schema
	 * {@code decimal} data type.
	 *
	 * @param value Value to print.
	 *
	 * @return String with lexical representation of {@code decimal}.
	 */
	public static String printDecimal( BigDecimal value )
	{
		return value.toPlainString();
	}

}
