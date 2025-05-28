package com.agnux.tms.repository.model;

import com.agnux.tms.errors.ErrorCodes;
import com.agnux.tms.errors.TmsException;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TmsBasicModel {

    protected UUID id;
    protected UUID tenantId;

    private static final Pattern MULTIPLE_SPACES = Pattern.compile("\\s{2,}");

    public Optional<UUID> getId() {
        return Optional.ofNullable(this.id);
    }

    public UUID getTenantId() {
        return this.tenantId;
    }

    public void validate() throws TmsException {
        if (tenantId == null) {
            throw new TmsException("tenantId must not be null", ErrorCodes.INVALID_DATA);
        }
    }

    protected static String removeMultipleSpaces(String text) {
        if (text == null) {
            return null;
        }
        Matcher matcher = MULTIPLE_SPACES.matcher(text);
        return matcher.replaceAll(" ");
    }
}
