//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.2.5 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2012.03.08 à 06:24:59 PM CET 
//


package neptune;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour PtAccessPointTypeType.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="PtAccessPointTypeType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="In"/>
 *     &lt;enumeration value="Out"/>
 *     &lt;enumeration value="InOut"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "PtAccessPointTypeType")
@XmlEnum
public enum PtAccessPointTypeType {

    @XmlEnumValue("In")
    IN("In"),
    @XmlEnumValue("Out")
    OUT("Out"),
    @XmlEnumValue("InOut")
    IN_OUT("InOut");
    private final String value;

    PtAccessPointTypeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static PtAccessPointTypeType fromValue(String v) {
        for (PtAccessPointTypeType c: PtAccessPointTypeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
