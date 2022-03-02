package org.bounce.text.xml;

/**
 * @author: Vladimir Nikic
 * Date: May 23, 2007
 */
public class XmlParserUtils {

    /**
     * @param source
     * @return Identifier that stands for tag name or tag attribute at the start
     *         of specified string.
     */
    public static String getIdentifier(String source) {
        if (source == null && source.length() == 0) {
            return null;
        }
        
        StringBuffer buffer = new StringBuffer();
        int index = 0;
        int len = source.length();
        boolean charOk = true;
        do {
            char ch = source.charAt(index);
            charOk = ( index == 0 && Character.isJavaIdentifierStart(ch) ) ||
                     ( Character.isJavaIdentifierPart(ch) || ch == '-' || ch == ':' || ch == '.' );
            if (charOk) {
                buffer.append(ch);
            }
            index++;
        } while (charOk && index < len);

        String identifier = buffer.toString();
        return identifier.length() > 0 ? identifier : null;
    }

    /**
     * Tries to find out if the line finishes with an element start
     * @param line
     */
    public static String getStartElement(String line) {
        int first = line.lastIndexOf( "<");
        int last = line.lastIndexOf( ">");

        if ( last < first) { // In the Tag
            return null;
        } else {
            int firstEnd = line.lastIndexOf( "</");
            int lastEnd = line.lastIndexOf( "/>");

            // Last Tag is not an End Tag
            if ( (firstEnd != first) && ((lastEnd + 1) != last)) {
                return XmlParserUtils.getIdentifier( line.substring(first + 1) );
            }
        }

        return null;
    }
    
}