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

/**
 * Indicates that a factory instance could not be created.
 *
 * @author G. Meinders
 */
@NoArgsConstructor
public class FactoryException
	extends RuntimeException
{
	@Serial
	private static final long serialVersionUID = 7574420257480434915L;

	public FactoryException( String message )
	{
		super( message );
	}

	public FactoryException( Throwable cause )
	{
		super( cause.getMessage(), cause );
	}
}
