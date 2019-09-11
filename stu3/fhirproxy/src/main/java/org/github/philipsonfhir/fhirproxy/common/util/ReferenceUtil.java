package org.github.philipsonfhir.fhirproxy.common.util;


import lombok.Getter;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.utilities.Utilities;

public class ReferenceUtil {
    public static ParsedReference parseReference(Reference reference ){
        return new ParsedReference(reference);
    }

    @Getter
    public static class ParsedReference {
        private String url;
        private String resourceId = null;
        private String version = null;
        private String resourceType = null;

        public ParsedReference(Reference reference) {
            String ref = reference.getReference();
            if ( Utilities.isURL(ref) ){
                this.url = ref;
            } else {
                String[] parts = ref.split("/");
                switch (parts.length) {
                    case 3:
                        this.version = parts[2];
                    case 2:
                        this.resourceId = parts[1];
                    case 1:
                        this.resourceType = parts[0];
                }
            }
        }

        public boolean hasResourceId() {
            return resourceId!=null;
        }

        public boolean isUrl() {
            return url!=null;
        }
    }
}
